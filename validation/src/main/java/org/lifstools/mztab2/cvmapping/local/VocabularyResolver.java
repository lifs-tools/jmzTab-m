/*
 * Copyright 2026 LIFS Tools Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lifstools.mztab2.cvmapping.local;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.lifstools.mztab2.cvmapping.AncestorSource;
import org.lifstools.mztab2.cvmapping.local.LookupPolicy.Source;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * The ancestors of a validation run's terms, each from the version of its
 * vocabulary that design §4.4 chooses, under the strict or forced policy of
 * §4.5. One resolver serves one run (Ruling P2), for one file's
 * {@code cv[n]} entries.
 *
 * <p>
 * A vocabulary is resolved when the first of its terms is asked for, and no
 * earlier (Ruling P4): a declared but unused vocabulary is never downloaded.
 * Labels are matched ignoring case, against the file's {@code cv[n]-label}
 * and against the labels and OBO prefix of the resources file. Resolving a
 * label:</p>
 * <ul>
 * <li>a label in neither the resources file nor the file's {@code cv[n]}
 * entries is no vocabulary: each of its terms is a 404, the per-rule error of
 * today, and nothing is recorded;</li>
 * <li>a label of the resources file that the file does not declare (its
 * Error-3013 comes from elsewhere) is resolved as if declared without a
 * version, so the rules are still checked;</li>
 * <li>a label the file declares but the resources file does not know is
 * {@link Reason#UNKNOWN_VOCABULARY};</li>
 * <li>with {@link Source#OLS}, the cache is never used: OLS gives no
 * declared version ({@link Reason#ONLY_OLS}).</li>
 * </ul>
 *
 * <p>
 * A version that cannot be had fails the label. Strict, every term of the
 * label then throws the same {@link VocabularyUnavailableException}, which
 * carries the one Error-3017. Forced, the substitutes are tried in order: the
 * nearest release newer than the one wanted, the latest release (neither when
 * the latest was wanted), then the policy's {@link CurrentReleaseSource}.
 * Failures on the way are not reported; one Warn-3018 names the version
 * wanted, why it failed, and the version and source used. If the current
 * source names no version either, nothing is left: the label fails as in a
 * strict run.</p>
 *
 * <p>
 * Offline, with no latest release kept by an earlier online run, the latest
 * release is the newest one the cache holds (Ruling R-AF): a pre-filled cache
 * answers an undeclared or unrecognised version (Info-3022 says the latest
 * came from the cache), and a forced run substitutes from it. A declared date
 * still needs the release list, so offline without one it fails as before.</p>
 *
 * <p>
 * The terms of a cached release are answered from its index, which holds its
 * OWL imports too: OBI's terms under MSIO come from MSIO's release. Each
 * ancestor carries the vocabulary's OBO prefix as its cv label, as OLS gives
 * the prefix of the ontology asked (MSIO for OBI:0000094 under MSIO), so
 * {@code CvMappingUtils.isEqualTo} compares as it does with OLS. A term not
 * in the release is a 404, as OLS reports it (Ruling P3).</p>
 *
 * <p>
 * {@link #messages()} gives, in the order they arose, Warn-3023 for each
 * further {@code cv[n]} entry of a label (made when the resolver is built,
 * since it is a property of the file, so it is reported even if the label is
 * never looked up), then for each label as it is resolved Warn-3021,
 * Warn-3019 or Warn-3020, Info-3022 once the latest release has been
 * resolved (a latest that fails gives only the 3018 or the 3017), and
 * Warn-3018. The Error-3017 is only in the exception.</p>
 *
 * <p>
 * Each label is resolved once, also when asked from several threads at
 * once; {@link #resolved()} and {@link #messages()} return snapshots.</p>
 */
public final class VocabularyResolver implements AncestorSource {

    /** The source named in Warn-3018 for a substitute from the cache. */
    private static final String LOCAL = "local";
    /**
     * The source named in Warn-3018 for the newest release in the cache,
     * used offline when no latest release is kept (Ruling R-AF).
     */
    private static final String LOCAL_NEWEST = "the local cache (its newest release)";
    /** Said in Info-3022 of a latest release that is the newest in the cache (Ruling R-AF). */
    private static final String CACHED_LATEST = " (the newest release in the cache, as downloads are off)";
    private static final String LATEST = "latest";
    private static final String ONLY_OLS =
        "only OLS could answer it, and OLS serves only its current release";
    private static final String OLS_REASON = "OLS";
    /**
     * The reason for an unlisted ISO date that is no release, read as the
     * release current on that date (Ruling R-Y, amended).
     */
    static final String FILE_DATE_FALLBACK = "FILE_DATE_FALLBACK";

    private static final Logger LOG = LoggerFactory.getLogger(VocabularyResolver.class);

    private final LookupPolicy policy;
    /** The first cv entry of each label, by its key. */
    private final Map<String, CV> declared;
    private final Map<String, String> mappingVersions;
    private final ConcurrentMap<String, Label> labels = new ConcurrentHashMap<>();
    private final List<MZTabError> messages = Collections.synchronizedList(new ArrayList<>());
    private final List<ResolvedVocabulary> resolved = Collections.synchronizedList(new ArrayList<>());

    /**
     * Builds a resolver and reports duplicate cv entries (Warn-3023): the
     * first entry of a label is used.
     *
     * @param policy where and how strictly versions are resolved
     * @param fileCvs the file's {@code cv[n]} entries
     * @param mappingVersions the mapping's version per vocabulary, by its
     * {@code cvIdentifier}; empty in phase 1
     */
    public VocabularyResolver(LookupPolicy policy, List<CV> fileCvs, Map<String, String> mappingVersions) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.mappingVersions = Map.copyOf(mappingVersions);
        Map<String, CV> first = new LinkedHashMap<>();
        for (CV cv : fileCvs) {
            if (cv == null || cv.getLabel() == null || cv.getLabel().isBlank()) {
                continue;
            }
            CV kept = first.putIfAbsent(key(cv.getLabel()), cv);
            if (kept != null) {
                messages.add(new MZTabError(CrossCheckErrorType.CvDeclaredTwice, -1, kept.getLabel(),
                    kept.getVersion(), cv.getVersion()));
            }
        }
        this.declared = first;
    }

    /**
     * Resolves the term's vocabulary on first use, then answers from it.
     *
     * @throws HttpClientErrorException 404 for a term not in the release, or a
     * label that names no vocabulary
     * @throws VocabularyUnavailableException if the vocabulary cannot be had
     */
    @Override
    public List<Parameter> ancestors(Parameter term) throws HttpClientErrorException {
        String label = term.getCvLabel();
        if (label == null || label.isBlank()) {
            throw notFound(term.getCvAccession() + " has no cv label");
        }
        return labels.computeIfAbsent(key(label), k -> new Label()).answer(label).ancestors(term);
    }

    /**
     * The vocabularies used so far.
     *
     * @return a snapshot, in the order they were resolved
     */
    public List<ResolvedVocabulary> resolved() {
        synchronized (resolved) {
            return List.copyOf(resolved);
        }
    }

    /**
     * The version messages so far: Warn-3018 to Warn-3023 and Info-3022.
     *
     * @return a snapshot, in the order they arose
     */
    public List<MZTabError> messages() {
        synchronized (messages) {
            return List.copyOf(messages);
        }
    }

    private static String key(String label) {
        return label.toUpperCase(Locale.ROOT);
    }

    static HttpClientErrorException notFound(String text) {
        return HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", new HttpHeaders(),
            text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    /** Where the terms of one label are answered from. */
    private interface Answer {

        List<Parameter> ancestors(Parameter term);
    }

    /** One label, resolved on first use and once. */
    private final class Label {

        private Answer answer;

        synchronized Answer answer(String label) {
            if (answer == null) {
                answer = resolve(label);
            }
            return answer;
        }
    }

    private Answer resolve(String label) {
        CV cv = declared.get(key(label));
        Optional<VocabularyEntry> entry = policy.resources().forLabel(label);
        if (cv == null && entry.isEmpty()) {
            return term -> {
                throw notFound(term.getCvAccession() + ": the cv label " + label
                    + " names no vocabulary of the resources file or of the file's cv entries");
            };
        }
        Resolution r = new Resolution(label, cv, entry);
        try {
            return r.resolve();
        } finally {
            messages.addAll(r.messages);
            if (r.used != null) {
                resolved.add(r.used);
            }
        }
    }

    /** A version lookup that may fail. */
    @FunctionalInterface
    private interface VersionLookup {

        Optional<Pick> version() throws ResolutionException;
    }

    /**
     * A version chosen; cached is true for the newest release in the cache,
     * taken offline as the latest (Ruling R-AF).
     */
    private record Pick(String version, boolean cached) {
    }

    /** A forced run's substitute: why it is used, and which version it is. */
    private record Substitute(String reason, VersionLookup lookup) {
    }

    /** The resolution of one label: its messages and the vocabulary used. */
    private final class Resolution {

        /** The label as asked, for the current-release source. */
        private final String label;
        /** The label as the file declares it, else as asked. */
        private final String name;
        private final String declaredText;
        private final Optional<VocabularyEntry> entry;
        private final Optional<String> mapping;
        private Optional<String> normalised = Optional.empty();
        private final List<MZTabError> messages = new ArrayList<>();
        private ResolvedVocabulary used;

        Resolution(String label, CV cv, Optional<VocabularyEntry> entry) {
            this.label = label;
            this.name = cv == null ? label : cv.getLabel();
            this.declaredText = cv == null ? null : cv.getVersion();
            this.entry = entry;
            this.mapping = mappingVersion(label, entry);
        }

        Answer resolve() {
            if (policy.source() == Source.OLS) {
                return unavailable(wantedAsDeclared(), Reason.ONLY_OLS, ONLY_OLS, null, List.of());
            }
            if (entry.isEmpty()) {
                return unavailable(wantedAsDeclared(), Reason.UNKNOWN_VOCABULARY,
                    "the resources file has no entry for it", null, List.of());
            }
            return resolveLocal(entry.get());
        }

        private Answer resolveLocal(VocabularyEntry e) {
            VocabularyCache cache = policy.cache();
            List<Release> releases;
            ResolutionException unlisted = null;
            try {
                releases = cache.releases(e);
            } catch (ResolutionException x) {
                releases = List.of();
                unlisted = x;
            }
            DeclaredVersion version = DeclaredVersion.of(declaredText, e.scheme(), releases);
            if (unlisted != null && version.kind() == DeclaredVersion.Kind.NOT_RECOGNISED
                && DeclaredVersion.isDate(declaredText)) {
                // a date names a release only through the release list; any other value is undeclared
                return unavailable(declaredText.trim(), unlisted.reason(),
                    "its releases cannot be listed, so the declared date cannot be turned into a release: "
                    + unlisted.getMessage(), unlisted, List.of(latestSubstitute(e)));
            }
            normalised = version.normalised();
            VersionChoice choice = VersionRule.choose(name, version, mapping, e.scheme());
            messages.addAll(choice.messages());
            String wanted = choice.version();
            String latestNote = "";
            if (wanted == null) {
                Optional<Pick> latest;
                try {
                    latest = latest(e);
                } catch (ResolutionException x) {
                    return unavailable(LATEST, x.reason(), x.getMessage(), x, List.of());
                }
                if (latest.isEmpty()) {
                    return unavailable(LATEST, Reason.NO_SUCH_RELEASE, "no latest release can be named", null,
                        List.of());
                }
                wanted = latest.get().version();
                latestNote = latest.get().cached() ? CACHED_LATEST : "";
            }
            try {
                Answer answer = local(e, wanted, choice.reason().name());
                if (choice.reason() == VersionChoice.Reason.LATEST) {
                    messages.add(new MZTabError(CrossCheckErrorType.CvVersionNotDeclared, -1, name, wanted,
                        latestNote));
                }
                return answer;
            } catch (ResolutionException x) {
                ResolutionException failure = x;
                Optional<String> fallback = version.dateFallback();
                if (x.reason() == Reason.NO_SUCH_RELEASE && choice.reason() == VersionChoice.Reason.FILE
                    && fallback.isPresent() && version.normalised().equals(Optional.of(wanted))) {
                    // Ruling R-Y (amended): an unlisted ISO date that is no release is read as a date
                    Optional<String> asRelease = normalised;
                    normalised = fallback;
                    try {
                        return local(e, fallback.get(), FILE_DATE_FALLBACK);
                    } catch (ResolutionException y) {
                        normalised = asRelease;
                        failure = new ResolutionException(y.reason(), x.getMessage() + "; read as a date it is "
                            + fallback.get() + ", which cannot be had either: " + y.getMessage(), y);
                    }
                }
                List<Substitute> substitutes = choice.reason() == VersionChoice.Reason.LATEST
                    ? List.of()
                    : List.of(newer(wanted, releases, e.scheme()), latestSubstitute(e));
                return unavailable(wanted, failure.reason(), failure.getMessage(), failure, substitutes);
            }
        }

        /** A cached release and its index; records it as used. */
        private Answer local(VocabularyEntry e, String version, String reason) throws ResolutionException {
            CachedRelease release = policy.cache().get(e, version);
            TermIndex index;
            try {
                index = TermIndexStore.open(release);
            } catch (IOException x) {
                throw new ResolutionException(Reason.PARSE_FAILED,
                    "its index " + release.index() + " cannot be read: " + x.getMessage(), x);
            }
            used = new ResolvedVocabulary(name, declaredText, normalised, mapping, version, reason,
                Optional.of(release), Optional.empty(), Optional.empty(), true);
            String what = e.key() + " " + version;
            String prefix = e.oboPrefix();
            return term -> {
                String id = term.getCvAccession();
                if (id == null || index.term(id).isEmpty()) {
                    throw notFound(id + " is not a term of " + what);
                }
                return index.ancestors(id).stream()
                    .map(t -> new Parameter().cvLabel(prefix).cvAccession(t.id())
                        .name(t.label().isEmpty() ? null : t.label()))
                    .toList();
            };
        }

        /**
         * The version wanted cannot be had: strict, the label fails; forced,
         * the first substitute that can be had, then the current release.
         */
        private Answer unavailable(String wanted, Reason kind, String reason, Throwable cause,
            List<Substitute> substitutes) {
            if (!policy.forced()) {
                return failed(new VocabularyUnavailableException(name, wanted, kind, reason, cause));
            }
            Set<String> tried = new HashSet<>(Set.of(wanted));
            for (Substitute s : substitutes) {
                try {
                    Optional<Pick> pick = s.lookup().version();
                    if (pick.isEmpty() || !tried.add(pick.get().version())) {
                        continue;
                    }
                    String version = pick.get().version();
                    Answer answer = local(entry.orElseThrow(), version, s.reason());
                    messages.add(new MZTabError(CrossCheckErrorType.CvVersionSubstituted, -1, name, wanted, reason,
                        version, pick.get().cached() ? LOCAL_NEWEST : LOCAL));
                    return answer;
                } catch (ResolutionException x) {
                    LOG.debug("{} substitute for {} {} cannot be had: {}", s.reason(), name, wanted,
                        x.getMessage());
                }
            }
            CurrentReleaseSource current = policy.current();
            Instant asked = Instant.now();
            Optional<String> version;
            try {
                version = current.currentVersion(label);
            } catch (RuntimeException x) {
                LOG.debug("{} names no current version of {}: {}", current.name(), label, x.toString());
                version = Optional.empty();
            }
            if (version.isEmpty()) {
                return failed(new VocabularyUnavailableException(name, wanted, kind, reason
                    + "; no substitute could be had, and " + current.name() + " names no current release", cause));
            }
            messages.add(new MZTabError(CrossCheckErrorType.CvVersionSubstituted, -1, name, wanted, reason,
                version.get(), current.name()));
            used = new ResolvedVocabulary(name, declaredText, normalised, mapping, version.get(), OLS_REASON,
                Optional.empty(), version, Optional.of(asked), false);
            return current::ancestors;
        }

        private Answer failed(VocabularyUnavailableException e) {
            return term -> {
                throw e;
            };
        }

        /** The declared version, else the mapping's, else latest: for a label no release list is read for. */
        private String wantedAsDeclared() {
            if (declaredText != null && !declaredText.isBlank()) {
                return declaredText.trim();
            }
            return mapping.orElse(LATEST);
        }

        private Substitute newer(String version, List<Release> releases, VersionScheme scheme) {
            return new Substitute("SUBSTITUTE_NEWER", () -> releases.stream()
                .map(Release::version)
                .filter(v -> scheme.compare(v, version).map(c -> c > 0).orElse(false))
                .min((a, b) -> scheme.compare(a, b).orElse(0))
                .map(v -> new Pick(v, false)));
        }

        private Substitute latestSubstitute(VocabularyEntry e) {
            return new Substitute("SUBSTITUTE_LATEST", () -> latest(e));
        }

        /**
         * The latest release. Offline, without a kept latest release or
         * release index, it is the newest release in the cache (Ruling R-AF):
         * a pre-filled cache then answers an undeclared version, and a forced
         * run substitutes from it.
         */
        private Optional<Pick> latest(VocabularyEntry e) throws ResolutionException {
            VocabularyCache cache = policy.cache();
            try {
                return cache.latest(e).map(v -> new Pick(v, false));
            } catch (ResolutionException x) {
                if (x.reason() != Reason.OFFLINE_NOT_CACHED) {
                    throw x;
                }
                Optional<String> newest = cache.newestCached(e);
                if (newest.isEmpty()) {
                    throw new ResolutionException(x.reason(), x.getMessage() + ", nor any release of " + e.key(), x);
                }
                LOG.info("Offline, the latest release of {} is taken to be {}, the newest in the cache ({})",
                    e.key(), newest.get(), x.getMessage());
                return newest.map(v -> new Pick(v, true));
            }
        }
    }

    /** The mapping's version of a vocabulary, matched by the resources entry's identifiers, else by label. */
    private Optional<String> mappingVersion(String label, Optional<VocabularyEntry> entry) {
        for (Map.Entry<String, String> m : mappingVersions.entrySet()) {
            String id = m.getKey();
            boolean matches = entry.map(e -> e.key().equalsIgnoreCase(id) || e.isKnownAs(id))
                .orElse(id.equalsIgnoreCase(label));
            if (matches && !m.getValue().isBlank()) {
                return Optional.of(m.getValue());
            }
        }
        return Optional.empty();
    }
}

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.lifstools.mztab2.cvmapping.local.LookupPolicy.Source;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Parameter;
import org.springframework.web.client.HttpClientErrorException;

/**
 * The local lookup gives the ancestors OLS gives (design §8): for each term
 * that {@link OlsRecorder} recorded, the vocabulary is resolved at the version
 * OLS reported ({@code versions.json}'s {@code version}: config.version where
 * the vocabulary can resolve it, else the version the versionIri names, as
 * {@code versionFrom} says; Ruling R-Z) through the vocabulary cache and a
 * strict {@link VocabularyResolver}, and the ancestor id sets are compared.
 *
 * <p>
 * The releases are downloaded into the folder that the system property
 * {@value #CACHE_PROPERTY} or the environment variable {@value #CACHE_ENV}
 * names, else into {@code target/ols-parity-cache}; NCBITaxon and the other
 * large vocabularies are hundreds of MB, so a kept folder saves the
 * downloads.</p>
 *
 * <p>
 * An ontology whose OLS version cannot be resolved locally is skipped with an
 * assumption naming it, the version and the reason (Ruling R-X); a difference
 * only OWL semantics the readers lack could remove is listed in
 * {@link #KNOWN_DIFFERENCES}, and the test then requires exactly that
 * difference (Ruling R-H).</p>
 */
@Tag("network")
public class OlsParityTest {

    static final String CACHE_PROPERTY = "olsParity.cache";
    static final String CACHE_ENV = "OLS_PARITY_CACHE";
    private static final String FIXTURES = "/cv/ols-parity/";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final PreferredResources RESOURCES = PreferredResources.defaults();
    private static final Map<String, Resolution> RESOLUTIONS = new ConcurrentHashMap<>();
    private static volatile VocabularyCache cache;

    /** A difference the readers cannot remove, which the test requires as it is. */
    record KnownDifference(Set<String> onlyOls, Set<String> onlyLocal, String reason) {
    }

    /**
     * By fixture, {@code <ontology>/<SHORTFORM>}. The difference names no term
     * a mapping rule could name, so no rule check differs.
     */
    static final Map<String, KnownDifference> KNOWN_DIFFERENCES = Map.of(
        "obi/OBI_0000094", new KnownDifference(Set.of("OIO:ObsoleteClass"),
            Set.of("http://www.geneontology.org/formats/oboInOwl#ObsoleteClass"),
            "OBI:0000094 is obsolete in OBI, under oboInOwl:ObsoleteClass; OLS gives that class, which is outside"
                + " the OBO PURL space, the curie OIO:ObsoleteClass, while the OWL reader keeps any IRI outside"
                + " that space whole"));

    /** One recorded term. */
    record Fixture(String ontology, String name, String oboId, String label, String prefix, Set<String> olsAncestors,
        Set<String> olsPrefixes) {

        @Override
        public String toString() {
            return ontology + "/" + name;
        }
    }

    /** A vocabulary resolved for the parity, or why it is skipped. */
    private record Resolution(VocabularyResolver resolver, String olsVersion, Optional<String> skipped) {
    }

    static Stream<Fixture> fixtures() throws IOException {
        Path root = fixtureRoot();
        List<Fixture> fixtures = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path p : files.filter(p -> p.getParent() != null && !p.getParent().equals(root)
                && p.toString().endsWith(".json")).sorted().toList()) {
                fixtures.add(read(p));
            }
        }
        if (fixtures.isEmpty()) {
            fail("no fixtures under " + root);
        }
        return fixtures.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    public void localAncestorsMatchOls(Fixture f) throws IOException {
        List<Parameter> local = localAncestors(f);
        Set<String> localIds = local.stream().map(Parameter::getCvAccession)
            .collect(Collectors.toCollection(TreeSet::new));
        Set<String> onlyOls = new TreeSet<>(f.olsAncestors());
        onlyOls.removeAll(localIds);
        Set<String> onlyLocal = new TreeSet<>(localIds);
        onlyLocal.removeAll(f.olsAncestors());
        KnownDifference known = KNOWN_DIFFERENCES.get(f.toString());
        if (known != null) {
            assertEquals(known.onlyOls(), onlyOls, f + ": the known difference (" + known.reason()
                + ") changed; ids only in OLS");
            assertEquals(known.onlyLocal(), onlyLocal, f + ": the known difference (" + known.reason()
                + ") changed; ids only local");
            return;
        }
        if (!onlyOls.isEmpty() || !onlyLocal.isEmpty()) {
            fail(mismatch(f, onlyOls, onlyLocal));
        }
    }

    /**
     * MSIO's terms and the OBI term its rule names (OBI:0000094) come from
     * MSIO's release, as OLS answers them under MSIO: the same ancestors, each
     * with MSIO's prefix as cv label, as OLS gives MSIO as their
     * {@code ontology_prefix}.
     */
    @Test
    public void msioAndItsObiImportMatchOls() throws IOException {
        List<Fixture> msio = fixtures().filter(f -> f.ontology().equals("msio")).toList();
        assertTrue(msio.stream().anyMatch(f -> f.oboId().equals("OBI:0000094")), "no msio/OBI_0000094 fixture");
        for (Fixture f : msio) {
            localAncestorsMatchOls(f);
            assertEquals(Set.of("MSIO"), f.olsPrefixes(), f + ": OLS's ontology_prefix");
            Set<String> labels = localAncestors(f).stream().map(Parameter::getCvLabel).collect(Collectors.toSet());
            assertEquals(f.olsPrefixes(), labels, f + ": the cv labels of the local ancestors");
            ResolvedVocabulary used = resolution(f).resolver().resolved().get(0);
            assertEquals("MSIO", used.release().orElseThrow().key(), f + ": answered from MSIO's release");
        }
    }

    private static List<Parameter> localAncestors(Fixture f) {
        Resolution r = resolution(f);
        r.skipped().ifPresent(reason -> Assumptions.abort(f.ontology() + " at OLS's version " + r.olsVersion()
            + " is skipped (Ruling R-X): " + reason));
        try {
            return r.resolver().ancestors(parameter(f));
        } catch (HttpClientErrorException x) {
            throw new AssertionError(f + ": " + f.oboId() + " is not in the local release "
                + used(r) + ": " + x.getResponseBodyAsString(), x);
        }
    }

    /** Resolves the fixture's vocabulary at OLS's version once per ontology, with its first term. */
    private static Resolution resolution(Fixture f) {
        return RESOLUTIONS.computeIfAbsent(f.ontology(), o -> resolve(f));
    }

    private static Resolution resolve(Fixture f) {
        JsonNode recorded = versions().path("ontologies").path(f.ontology());
        String version = recorded.path("version").isTextual() ? recorded.path("version").asText() : null;
        String from = recorded.path("versionFrom").asText("?");
        if (version == null) {
            return new Resolution(null, "(none)", Optional.of("OLS names no version (config.version "
                + recorded.path("configVersion") + ", versionIri " + recorded.path("versionIri") + ")"));
        }
        VocabularyEntry entry = RESOURCES.forLabel(f.ontology()).orElseThrow(
            () -> new AssertionError(f.ontology() + " is no vocabulary of the shipped resources"));
        assertEquals(entry, RESOURCES.forLabel(f.prefix()).orElseThrow(), f + ": the term's prefix " + f.prefix()
            + " names another vocabulary");
        if (!entry.scheme().accepts(version) && !DeclaredVersion.isDate(version)) {
            return new Resolution(null, version, Optional.of(version + " is no " + entry.scheme()
                + " version and no date"));
        }
        LookupPolicy policy = new LookupPolicy(Source.LOCAL, false, cache(), RESOURCES,
            CurrentReleaseSource.offline("OLS"));
        CV cv = new CV().label(f.prefix()).fullName(f.prefix()).version(version)
            .uri(URI.create("http://purl.obolibrary.org/obo/" + f.ontology()));
        VocabularyResolver resolver = new VocabularyResolver(policy, List.of(cv), Map.of());
        long start = System.nanoTime();
        try {
            resolver.ancestors(parameter(f));
        } catch (VocabularyUnavailableException x) {
            if (x.kind().filter(k -> k != Reason.NO_SUCH_RELEASE).isPresent()) {
                throw new AssertionError(f.ontology() + " " + version + " cannot be had: " + x.reason(), x);
            }
            return new Resolution(resolver, version, Optional.of(x.reason()));
        } catch (HttpClientErrorException x) {
            // the term is missing: localAncestorsMatchOls reports it
        }
        ResolvedVocabulary used = resolver.resolved().get(0);
        System.out.printf("resolved %s at OLS's %s (from %s): %s %s (%s) in %d ms%n", f.ontology(), version, from,
            entry.key(), used.versionUsed(), used.reason(), (System.nanoTime() - start) / 1_000_000);
        if (!used.reason().equals("FILE") && !used.reason().equals("FILE_DATE")) {
            return new Resolution(resolver, version, Optional.of("the resolver used " + used.versionUsed()
                + " (" + used.reason() + "), not OLS's version"));
        }
        return new Resolution(resolver, version, Optional.empty());
    }

    private static String used(Resolution r) {
        return r.resolver().resolved().stream().findFirst()
            .map(v -> v.label() + " " + v.versionUsed()).orElse("(none)");
    }

    /** The term, the ids on each side only, and the relation the local index has for each such id. */
    private static String mismatch(Fixture f, Set<String> onlyOls, Set<String> onlyLocal) {
        Resolution r = resolution(f);
        TermIndex index;
        try {
            index = TermIndexStore.open(r.resolver().resolved().get(0).release().orElseThrow());
        } catch (IOException x) {
            throw new AssertionError(f + ": the index cannot be opened", x);
        }
        List<TermRecord> closure = new ArrayList<>();
        index.term(f.oboId()).ifPresent(closure::add);
        closure.addAll(index.ancestors(f.oboId()));
        f.olsAncestors().forEach(id -> index.term(id).filter(t -> !closure.contains(t)).ifPresent(closure::add));
        StringBuilder text = new StringBuilder(f + " " + f.oboId() + " (" + f.label() + ") in " + used(r)
            + ": the ancestors differ from OLS's " + r.olsVersion());
        text.append("\n  only in OLS: ").append(onlyOls);
        text.append("\n  only local: ").append(onlyLocal);
        for (String id : union(onlyOls, onlyLocal)) {
            text.append("\n  ").append(id).append(": ").append(relations(id, closure, index));
        }
        return text.toString();
    }

    private static String relations(String id, List<TermRecord> closure, TermIndex index) {
        List<String> found = new ArrayList<>();
        for (TermRecord t : closure) {
            t.parentsByRelation().forEach((relation, parents) -> {
                if (parents.contains(id)) {
                    found.add(relation + " from " + t.id());
                }
            });
        }
        if (!found.isEmpty()) {
            return String.join(", ", found);
        }
        return "none" + (index.term(id).isPresent() ? " (the term is in the index)" : " (not in the index)");
    }

    private static Set<String> union(Set<String> a, Set<String> b) {
        Set<String> u = new TreeSet<>(a);
        u.addAll(b);
        return u;
    }

    private static Parameter parameter(Fixture f) {
        return new Parameter().cvLabel(f.prefix()).cvAccession(f.oboId()).name(f.label());
    }

    private static Fixture read(Path p) throws IOException {
        JsonNode json = JSON.readTree(p.toFile());
        JsonNode term = json.path("term");
        Set<String> ancestors = new TreeSet<>();
        Set<String> prefixes = new TreeSet<>();
        json.path("ancestors").forEach(a -> {
            ancestors.add(a.path("obo_id").isTextual() ? a.path("obo_id").asText() : a.path("iri").asText());
            prefixes.add(a.path("ontology_prefix").asText());
        });
        String name = p.getFileName().toString().replaceFirst("\\.json$", "");
        return new Fixture(p.getParent().getFileName().toString(), name, term.path("obo_id").asText(),
            term.path("label").asText(), term.path("ontology_prefix").asText(), ancestors, prefixes);
    }

    private static Path fixtureRoot() {
        try {
            return Path.of(OlsParityTest.class.getResource(FIXTURES + "versions.json").toURI()).getParent();
        } catch (URISyntaxException x) {
            throw new IllegalStateException(x);
        }
    }

    private static JsonNode versions() {
        try {
            return JSON.readTree(fixtureRoot().resolve("versions.json").toFile());
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    private static synchronized VocabularyCache cache() {
        if (cache == null) {
            String named = Optional.ofNullable(System.getProperty(CACHE_PROPERTY)).filter(s -> !s.isBlank())
                .orElse(System.getenv(CACHE_ENV));
            Path root = named != null && !named.isBlank() ? Path.of(named)
                : Path.of(System.getProperty("basedir", "."), "target", "ols-parity-cache");
            System.out.println("vocabulary cache " + root.toAbsolutePath());
            cache = new VocabularyCache(root, Downloader.http(Duration.ofMinutes(2)), false,
                VocabularyCache.DEFAULT_LATEST_TTL);
        }
        return cache;
    }
}

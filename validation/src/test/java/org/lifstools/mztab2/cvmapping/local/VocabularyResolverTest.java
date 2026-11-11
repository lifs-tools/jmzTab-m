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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.psidev.cvmapping.CvMappingRule;
import info.psidev.cvmapping.CvReference;
import info.psidev.cvmapping.CvTerm;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.cvmapping.JxPathElement;
import org.lifstools.mztab2.cvmapping.local.LookupPolicy.Source;
import org.lifstools.mztab2.cvmapping.local.ResolutionException.Reason;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.validation.handlers.ResolvingCvRuleHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * The resolver against vocabularies served by a local HTTP server, or cached
 * before the run: no network.
 *
 * <p>
 * MS has the releases 4.1.100, 4.1.150 and 4.1.200 (the latest), each the
 * mini PSI-MS file. The fake current-release source names 4.1.300.</p>
 */
public class VocabularyResolverTest {

    private static final String PURL = "http://purl.obolibrary.org/";
    private static final String V100 = "4.1.100";
    private static final String V150 = "4.1.150";
    private static final String V200 = "4.1.200";
    private static final List<String> MS_RELEASES = List.of(V100, V150, V200);
    private static final String CURRENT = "4.1.300";
    private static final Duration DAY = Duration.ofHours(24);

    private static final Parameter ESI = param("MS", "MS:1000073", "electrospray ionization");
    private static final Parameter VELOS = param("MS", "MS:1001742", "LTQ Orbitrap Velos");

    @TempDir
    Path temp;

    private final List<TestServer> servers = new ArrayList<>();
    private byte[] miniMs;

    @BeforeEach
    public void start() throws IOException {
        miniMs = resource("/cv/mini-ms.obo");
    }

    @AfterEach
    public void stop() {
        servers.forEach(TestServer::close);
    }

    // ---- the tests of the brief ----

    @Test
    public void aVocabularyIsResolvedOnlyWhenOneOfItsTermsIsAskedFor() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(false, cv("MS", V150));

        assertEquals(0, f.server.totalHits());
        assertEquals(List.of(), resolver.resolved());

        List<Parameter> ancestors = resolver.ancestors(ESI);

        assertEquals(List.of("MS:1000008 ionization type", "MS:1000458 source", "MS:1000463 instrument",
            "MS:0000000 Proteomics Standards Initiative Mass Spectrometry Vocabularies"), describe(ancestors));
        assertTrue(ancestors.stream().allMatch(p -> "MS".equals(p.getCvLabel())));
        assertEquals(1, f.server.hits(f.msPath(V150)));
        resolver.ancestors(VELOS);
        assertEquals(1, f.server.totalHits(), "a label is resolved once");
        ResolvedVocabulary ms = only(resolver.resolved());
        assertEquals("MS", ms.label());
        assertEquals(V150, ms.declaredOriginal());
        assertEquals(Optional.of(V150), ms.declaredNormalised());
        assertEquals(Optional.empty(), ms.mappingVersion());
        assertEquals(V150, ms.versionUsed());
        assertEquals("FILE", ms.reason());
        assertEquals(V150, ms.release().orElseThrow().version());
        assertEquals(URI.create(f.server.base() + f.msPath(V150)), ms.release().orElseThrow().source());
        assertEquals(Optional.empty(), ms.olsVersion());
        assertEquals(Optional.empty(), ms.olsDate());
        assertTrue(ms.reRunnable());
        assertEquals(List.of(), resolver.messages());
        assertEquals(0, f.current.versionCalls.get());
    }

    @Test
    public void aDeclaredButUnusedVocabularyIsNeverDownloaded() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        f.server.file("/msio/v1.0.1/msio.owl", resource("/cv/mini-msio.owl"));
        VocabularyResolver resolver = f.resolver(false, cv("MS", V200), cv("MSIO", "1.0.1"));

        resolver.ancestors(ESI);

        assertEquals(0, f.server.hits("/msio/v1.0.1/msio.owl"));
        assertEquals(List.of("MS"), resolver.resolved().stream().map(ResolvedVocabulary::label).toList());
        assertFalse(Files.exists(f.root.resolve("MSIO")));
    }

    @ParameterizedTest
    @EnumSource(value = Reason.class, names = {"NO_SUCH_RELEASE", "DOWNLOAD_FAILED", "CHECKSUM_MISMATCH",
        "PARSE_FAILED", "OFFLINE_NOT_CACHED", "UNKNOWN_VOCABULARY"})
    public void eachStrictFailureReasonIsOneError3017(Reason reason) throws Exception {
        Fixture f = new Fixture(reason, Set.of(V100));
        VocabularyResolver resolver = f.resolver(false, cv("MS", V100));

        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> resolver.ancestors(ESI));

        assertEquals(Optional.of(reason), e.kind());
        assertEquals("MS", e.label());
        assertEquals(V100, e.versionWanted());
        MZTabError error = e.error();
        assertEquals(CrossCheckErrorType.CvVersionUnavailable, error.getType());
        assertEquals(MZTabErrorType.Level.Error, error.getType().getLevel());
        assertTrue(error.getMessage().startsWith("The controlled vocabulary \"MS\" in version \"4.1.100\" is not"
            + " available: " + e.reason() + "."), error.getMessage());
        assertFalse(e.reason().endsWith("."), e.reason());
        // every later term of the label gets the same failure, and no download is retried
        int hits = f.server.totalHits();
        assertSame(e, assertThrows(VocabularyUnavailableException.class, () -> resolver.ancestors(VELOS)));
        assertEquals(hits, f.server.totalHits());
        // the 3017 lives in the exception only, and nothing was used
        assertEquals(List.of(), resolver.messages());
        assertEquals(List.of(), resolver.resolved());
        assertEquals(0, f.current.versionCalls.get());
    }

    @ParameterizedTest
    @EnumSource(value = Reason.class, names = {"NO_SUCH_RELEASE", "DOWNLOAD_FAILED", "CHECKSUM_MISMATCH",
        "PARSE_FAILED", "OFFLINE_NOT_CACHED", "UNKNOWN_VOCABULARY"})
    public void eachForcedFailureReasonIsOneWarning3018AndASubstitute(Reason reason) throws Exception {
        if (reason != Reason.UNKNOWN_VOCABULARY) {
            // the nearest newer release first
            Fixture newer = new Fixture(reason, Set.of(V100));
            VocabularyResolver resolver = newer.resolver(true, cv("MS", V100));
            assertFalse(resolver.ancestors(ESI).isEmpty());
            assertSubstitute(resolver, V100, "SUBSTITUTE_NEWER", V150, "local");
            assertTrue(only(resolver.resolved()).reRunnable());
            assertEquals(0, newer.current.versionCalls.get());

            // then the latest
            Fixture latest = new Fixture(reason, Set.of(V100, V150));
            resolver = latest.resolver(true, cv("MS", V100));
            assertFalse(resolver.ancestors(ESI).isEmpty());
            assertSubstitute(resolver, V100, "SUBSTITUTE_LATEST", V200, "local");
            assertTrue(only(resolver.resolved()).reRunnable());
            assertEquals(0, latest.current.versionCalls.get());
        }
        // then the current release
        Fixture none = new Fixture(reason, Set.copyOf(MS_RELEASES));
        VocabularyResolver resolver = none.resolver(true, cv("MS", V100));
        Instant before = Instant.now();
        assertEquals(FakeCurrent.ANSWER, resolver.ancestors(ESI));
        assertEquals(List.of(ESI), none.current.asked);
        ResolvedVocabulary ms = assertSubstitute(resolver, V100, "OLS", CURRENT, "FAKE");
        assertEquals(Optional.of(CURRENT), ms.olsVersion());
        assertFalse(ms.olsDate().orElseThrow().isBefore(before));
        assertEquals(Optional.empty(), ms.release());
        assertFalse(ms.reRunnable());
        assertEquals(List.of("MS"), none.current.versionLabels);
    }

    @Test
    public void aForcedRunWithNoSubstituteAtAllIsStillAnError() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.copyOf(MS_RELEASES));
        f.current.version = Optional.empty();
        VocabularyResolver resolver = f.resolver(true, cv("MS", V100));

        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> resolver.ancestors(ESI));

        assertEquals(Optional.of(Reason.NO_SUCH_RELEASE), e.kind());
        assertTrue(e.reason().contains("FAKE names no current release"), e.reason());
        assertEquals(List.of(), resolver.messages());
        assertEquals(List.of(), resolver.resolved());
    }

    @Test
    public void aFailingLatestGoesStraightToTheCurrentRelease() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of(V200));
        VocabularyResolver resolver = f.resolver(true);

        assertEquals(FakeCurrent.ANSWER, resolver.ancestors(ESI));

        // no older or newer release is tried for a latest that fails
        assertEquals(0, f.server.hits(f.msPath(V150)));
        assertEquals(0, f.server.hits(f.msPath(V100)));
        // no 3022: the latest was not used, and the 3018 says what was
        assertEquals(List.of(CrossCheckErrorType.CvVersionSubstituted), types(resolver.messages()));
        assertTrue(resolver.messages().get(0).getMessage().contains("\"MS\" in version \"4.1.200\" is not"
            + " available"), resolver.messages().get(0).getMessage());
        assertEquals("OLS", only(resolver.resolved()).reason());
        // strict: only the 3017
        VocabularyResolver strict = f.resolver(false);
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> strict.ancestors(ESI));
        assertEquals(V200, e.versionWanted());
        assertEquals(List.of(), strict.messages());
    }

    @Test
    public void olsUnderStrictIsAnError() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(Source.OLS, false, cv("MS", V200));

        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> resolver.ancestors(ESI));

        assertEquals(Optional.of(Reason.ONLY_OLS), e.kind());
        assertEquals(V200, e.versionWanted());
        assertTrue(e.error().getMessage().contains("only OLS could answer it, and OLS serves only its current"
            + " release."), e.error().getMessage());
        assertEquals(0, f.server.totalHits());
        assertFalse(Files.exists(f.root));
        assertEquals(List.of(), f.current.asked);
        assertEquals(List.of(), resolver.resolved());
    }

    @Test
    public void olsWhenForcedIsAWarningAndNotReRunnable() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(Source.OLS, true, cv("MS", V200));

        assertEquals(FakeCurrent.ANSWER, resolver.ancestors(ESI));

        ResolvedVocabulary ms = assertSubstitute(resolver, V200, "OLS", CURRENT, "FAKE");
        assertEquals(V200, ms.declaredOriginal());
        assertEquals(Optional.of(CURRENT), ms.olsVersion());
        assertTrue(ms.olsDate().isPresent());
        assertFalse(ms.reRunnable());
        assertTrue(resolver.messages().get(0).getMessage().contains("only OLS could answer it"),
            resolver.messages().get(0).getMessage());
        assertEquals(0, f.server.totalHits());
        assertFalse(Files.exists(f.root));
    }

    @Test
    public void anUnknownTermIsA404WithTodaysMessage() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(false, cv("MS", V200));
        Parameter unknown = param("MS", "MS:1999999", "no such term");

        HttpClientErrorException e = assertThrows(HttpClientErrorException.class, () -> resolver.ancestors(unknown));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        assertEquals("Not Found", e.getStatusText());
        assertTrue(e.getResponseBodyAsString().contains("MS:1999999"), e.getResponseBodyAsString());

        // the rule handler words it as it does for OLS's 404
        MzTab mzTab = new MzTab().metadata(new Metadata());
        mzTab.getMetadata().addInstrumentItem(new Instrument().id(1).name(unknown));
        IllegalArgumentException ruleError = assertThrows(IllegalArgumentException.class,
            () -> new ResolvingCvRuleHandler(new CvParameterLookupService(resolver)).handleRule(instrumentNameRule(),
                JxPathElement.toList(JXPathContext.newContext(mzTab), "/metadata/instrument/@name",
                    Parameter.class)));
        assertEquals("Could not retrieve parents for cv with label 'MS' and term accession 'MS:1999999' at path"
            + " /metadata/instrument[1]/name! Please check, whether the cv label and term accession in your file"
            + " contain a typo or use lowercase/uppercase! Check https://www.ebi.ac.uk/ols/search?q=MS:1999999 for"
            + " details on the term!", ruleError.getMessage());
        assertInstanceOf(HttpClientErrorException.class, ruleError.getCause());
        // an unknown term is no vocabulary failure
        assertEquals(List.of(), resolver.messages());
        assertEquals("FILE", only(resolver.resolved()).reason());
    }

    @Test
    public void anUnknownLabelStaysAPerRuleError() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(false, cv("MS", V200));

        HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
            () -> resolver.ancestors(param("XYZ", "XYZ:0000001", "made up")));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        assertEquals("Not Found", e.getStatusText());
        assertEquals(0, f.server.totalHits());
        assertEquals(List.of(), resolver.messages());
        assertEquals(List.of(), resolver.resolved());
        assertEquals(0, f.current.versionCalls.get());
        // forced too: the label is not a vocabulary that failed
        VocabularyResolver forced = f.resolver(true, cv("MS", V200));
        assertThrows(HttpClientErrorException.class, () -> forced.ancestors(param("XYZ", "XYZ:0000001", "")));
        assertEquals(List.of(), forced.messages());
        assertEquals(List.of(), f.current.asked);
    }

    @Test
    public void labelsMatchTheResourcesEntryIgnoringCase() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(false, cv("ms", V150));

        List<Parameter> ancestors = resolver.ancestors(param("ms", "MS:1000073", "electrospray ionization"));
        resolver.ancestors(param("Ms", "MS:1001742", "LTQ Orbitrap Velos"));

        assertEquals("MS:1000008 ionization type", describe(ancestors).get(0));
        // the ancestors carry the vocabulary's prefix, as OLS's do
        assertEquals("MS", ancestors.get(0).getCvLabel());
        ResolvedVocabulary ms = only(resolver.resolved());
        assertEquals("ms", ms.label());
        assertEquals(V150, ms.versionUsed());
        assertEquals("FILE", ms.reason());
        assertEquals(1, f.server.totalHits());
        assertEquals(List.of(), resolver.messages());
    }

    @Test
    public void aSecondCvEntryForTheSameLabelIsIgnoredWithAWarning() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(false, cv("MS", V150), cv("ms", V200), cv("MSIO", "1.0.1"));

        // a property of the file: reported before any lookup
        assertEquals(1, resolver.messages().size());
        MZTabError warning = resolver.messages().get(0);
        assertEquals(CrossCheckErrorType.CvDeclaredTwice, warning.getType());
        assertTrue(warning.getMessage().startsWith("The controlled vocabulary \"MS\" is declared twice, in version"
            + " \"4.1.150\" and in version \"4.1.200\"; the first entry is used"), warning.getMessage());

        resolver.ancestors(ESI);

        assertEquals(V150, only(resolver.resolved()).versionUsed());
        assertEquals(0, f.server.hits(f.msPath(V200)));
        assertEquals(1, resolver.messages().size());
    }

    @Test
    public void anImportedTermIsAnswered() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        f.server.file("/msio/v1.0.1/msio.owl", resource("/cv/mini-msio.owl"));
        f.server.file("/obo/mini-obi.owl", resource("/cv/mini-obi.owl"));
        VocabularyResolver resolver = f.resolver(false, cv("MSIO", "1.0.1"));

        List<Parameter> ancestors = resolver.ancestors(param("MSIO", "OBI:0000094", "material processing"));

        assertEquals(List.of("OBI:0000011 planned process"), describe(ancestors));
        // OLS gives an imported term's ancestors with the importing ontology's prefix
        assertEquals("MSIO", ancestors.get(0).getCvLabel());
        ResolvedVocabulary msio = only(resolver.resolved());
        assertEquals("1.0.1", msio.versionUsed());
        assertEquals(List.of(URI.create(PURL + "obo/mini-obi.owl")),
            msio.release().orElseThrow().imports().stream().map(CachedRelease::source).toList());
    }

    @Test
    public void latestIsRecordedAsAConcreteVersion() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        // MS declared without a version
        VocabularyResolver declared = f.resolver(false, cv("MS", ""));
        declared.ancestors(ESI);
        assertLatest(declared, "");
        // MS used by a parameter but not declared at all (Error-3013 elsewhere)
        VocabularyResolver undeclared = f.resolver(false);
        undeclared.ancestors(ESI);
        assertLatest(undeclared, null);
        // a value that names no release: undeclared, with its warning first
        VocabularyResolver unrecognised = f.resolver(false, cv("MS", "final"));
        unrecognised.ancestors(ESI);
        assertEquals(List.of(CrossCheckErrorType.CvVersionNotRecognised, CrossCheckErrorType.CvVersionNotDeclared),
            types(unrecognised.messages()));
        assertEquals(V200, only(unrecognised.resolved()).versionUsed());
    }

    @Test
    public void withoutAReleaseListOnlyADeclaredDateFails() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        // the release index (GitHub tags) fails; the latest comes from the unversioned file's header
        f.server.route("/api/repos/o/ms/tags", x -> TestServer.send(x, 500, new byte[0]));
        f.server.file("/ms/psi-ms.obo", miniMs);
        PreferredResources unlisted = PreferredResources.load(f.writeResources(List.of("""
            "MS": {
              "labels": ["MS"],
              "oboPrefix": "MS",
              "location": {"format": "OBO", "urlTemplate": "%1$s/ms/v{version}/psi-ms.obo"},
              "scheme": "DOTTED",
              "releases": {"kind": "githubTags", "repo": "o/ms", "tagPattern": "v{version}", "dates": "githubReleases"},
              "latest": {"kind": "header", "url": "%1$s/ms/psi-ms.obo"}
            }""".formatted(f.server.base()))));

        // a value that names nothing is undeclared, as with a release list: 3021, then the latest
        VocabularyResolver junk = f.resolver(unlisted, false, cv("MS", "final"));
        assertFalse(junk.ancestors(ESI).isEmpty());
        assertEquals(List.of(CrossCheckErrorType.CvVersionNotRecognised, CrossCheckErrorType.CvVersionNotDeclared),
            types(junk.messages()));
        ResolvedVocabulary ms = only(junk.resolved());
        assertEquals("LATEST", ms.reason());
        assertEquals(V200, ms.versionUsed());
        // a plain release name needs no list either
        VocabularyResolver release = f.resolver(unlisted, false, cv("MS", V150));
        assertFalse(release.ancestors(ESI).isEmpty());
        assertEquals("FILE", only(release.resolved()).reason());

        // a date names a release only through the list: strict, the list's failure
        VocabularyResolver date = f.resolver(unlisted, false, cv("MS", "14-06-2024"));
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> date.ancestors(ESI));
        assertEquals(Optional.of(Reason.DOWNLOAD_FAILED), e.kind());
        assertEquals("14-06-2024", e.versionWanted());
        assertTrue(e.reason().startsWith("its releases cannot be listed"), e.reason());
        assertEquals(List.of(), date.messages());
    }

    // ---- assertions ----

    private static void assertLatest(VocabularyResolver resolver, String declaredOriginal) {
        ResolvedVocabulary ms = only(resolver.resolved());
        assertEquals(V200, ms.versionUsed());
        assertEquals("LATEST", ms.reason());
        assertEquals(declaredOriginal, ms.declaredOriginal());
        assertEquals(Optional.empty(), ms.declaredNormalised());
        assertEquals(V200, ms.release().orElseThrow().version());
        assertEquals(1, resolver.messages().size());
        MZTabError info = resolver.messages().get(0);
        assertEquals(CrossCheckErrorType.CvVersionNotDeclared, info.getType());
        assertEquals("No version is declared for the controlled vocabulary \"MS\"; the latest version \"4.1.200\""
            + " was used.", info.getMessage());
    }

    /** One 3018 naming the wanted version, the version used and its source; one vocabulary used. */
    private static ResolvedVocabulary assertSubstitute(VocabularyResolver resolver, String wanted, String reason,
        String used, String source) {
        assertEquals(List.of(CrossCheckErrorType.CvVersionSubstituted), types(resolver.messages()));
        MZTabError warning = resolver.messages().get(0);
        assertEquals(MZTabErrorType.Level.Warn, warning.getType().getLevel());
        String m = warning.getMessage();
        String prefix = "The controlled vocabulary \"MS\" in version \"" + wanted + "\" is not available (";
        String suffix = "); version \"" + used + "\" from " + source + " was used instead.";
        assertTrue(m.startsWith(prefix) && m.endsWith(suffix), m);
        assertFalse(m.substring(0, m.length() - suffix.length()).endsWith("."), m);
        ResolvedVocabulary ms = only(resolver.resolved());
        assertEquals(reason, ms.reason());
        assertEquals(used, ms.versionUsed());
        if (!"OLS".equals(reason)) {
            assertEquals(used, ms.release().orElseThrow().version());
            assertEquals(Optional.empty(), ms.olsVersion());
        }
        return ms;
    }

    private static <T> T only(List<T> list) {
        assertEquals(1, list.size(), list.toString());
        return list.get(0);
    }

    private static List<MZTabErrorType> types(List<MZTabError> messages) {
        return messages.stream().map(MZTabError::getType).toList();
    }

    private static List<String> describe(List<Parameter> terms) {
        return terms.stream().map(p -> p.getCvAccession() + " " + p.getName()).toList();
    }

    // ---- fixtures ----

    /**
     * A server and a cache folder of their own, with MS's releases served
     * except those that fail for the given reason.
     */
    private final class Fixture {

        final TestServer server;
        final Path root;
        final FakeCurrent current = new FakeCurrent();
        final Reason reason;
        final Set<String> failing;
        final PreferredResources resources;

        Fixture(Reason reason, Set<String> failing) throws Exception {
            this.server = new TestServer();
            servers.add(server);
            this.root = Files.createTempDirectory(temp, "fixture").resolve("cache");
            this.reason = reason;
            this.failing = failing;
            Map<String, String> knownSha256 = new HashMap<>();
            for (String v : MS_RELEASES) {
                if (!failing.contains(v)) {
                    server.file(msPath(v), miniMs);
                    continue;
                }
                switch (reason) {
                    case DOWNLOAD_FAILED -> server.route(msPath(v),
                        x -> TestServer.send(x, 500, "broken".getBytes(StandardCharsets.UTF_8)));
                    case CHECKSUM_MISMATCH -> {
                        server.file(msPath(v), miniMs);
                        knownSha256.put(v, "0".repeat(64));
                    }
                    case PARSE_FAILED -> server.file(msPath(v),
                        "<html><body>moved</body></html>".getBytes(StandardCharsets.UTF_8));
                    case UNKNOWN_VOCABULARY -> server.file(msPath(v), miniMs);
                    default -> {
                        // NO_SUCH_RELEASE and OFFLINE_NOT_CACHED: not served
                    }
                }
            }
            this.resources = PreferredResources.load(writeResources(reason == Reason.UNKNOWN_VOCABULARY
                ? List.of(msioEntry()) : List.of(msEntry(knownSha256), msioEntry())));
            if (reason == Reason.OFFLINE_NOT_CACHED) {
                VocabularyEntry ms = resources.forLabel("MS").orElseThrow();
                VocabularyCache online = cache(false);
                for (String v : MS_RELEASES) {
                    if (!failing.contains(v)) {
                        online.get(ms, v);
                    }
                }
            }
        }

        String msPath(String version) {
            return "/ms/v" + version + "/psi-ms.obo";
        }

        VocabularyCache cache(boolean offline) {
            Downloader http = Downloader.http(Duration.ofSeconds(10));
            Downloader purlToServer = uri -> http.get(URI.create(uri.toString().replace(PURL, server.base() + "/")));
            return new VocabularyCache(root, purlToServer, offline, DAY, Clock.systemUTC(), server.api(),
                Optional.empty());
        }

        VocabularyResolver resolver(boolean forced, CV... cvs) {
            return resolver(Source.LOCAL, forced, cvs);
        }

        VocabularyResolver resolver(Source source, boolean forced, CV... cvs) {
            return resolver(source, resources, forced, cvs);
        }

        VocabularyResolver resolver(PreferredResources resources, boolean forced, CV... cvs) {
            return resolver(Source.LOCAL, resources, forced, cvs);
        }

        private VocabularyResolver resolver(Source source, PreferredResources resources, boolean forced, CV... cvs) {
            LookupPolicy policy = new LookupPolicy(source, forced, cache(reason == Reason.OFFLINE_NOT_CACHED),
                resources, current);
            return new VocabularyResolver(policy, List.of(cvs), Map.of());
        }

        private String msEntry(Map<String, String> knownSha256) {
            String releases = MS_RELEASES.stream().map(v -> "{\"version\": \"" + v + "\"}")
                .collect(Collectors.joining(", "));
            String sha = knownSha256.entrySet().stream().map(e -> "\"" + e.getKey() + "\": \"" + e.getValue() + "\"")
                .collect(Collectors.joining(", "));
            return """
                "MS": {
                  "labels": ["MS", "PSI-MS"],
                  "oboPrefix": "MS",
                  "location": {"format": "OBO", "urlTemplate": "%s/ms/v{version}/psi-ms.obo"},
                  "scheme": "DOTTED",
                  "releases": {"kind": "listed", "releases": [%s]},
                  "latest": {"kind": "newestRelease"},
                  "knownSha256": {%s}
                }""".formatted(server.base(), releases, sha);
        }

        private String msioEntry() {
            return """
                "MSIO": {
                  "labels": ["MSIO"],
                  "oboPrefix": "MSIO",
                  "location": {"format": "OWL_RDFXML", "urlTemplate": "%s/msio/v{version}/msio.owl"},
                  "scheme": "DOTTED",
                  "releases": {"kind": "listed", "releases": [{"version": "1.0.1"}]},
                  "latest": {"kind": "newestRelease"}
                }""".formatted(server.base());
        }

        Path writeResources(List<String> entries) throws IOException {
            Path file = Files.createTempFile(temp, "resources", ".json");
            Files.writeString(file, "{\"vocabularies\": {" + String.join(", ", entries) + "}}",
                StandardCharsets.UTF_8);
            return file;
        }
    }

    /** A current-release source that names 4.1.300 and answers every term with one ancestor. */
    private static final class FakeCurrent implements CurrentReleaseSource {

        static final List<Parameter> ANSWER = List.of(param("MS", "MS:1000008", "ionization type"));

        final AtomicInteger versionCalls = new AtomicInteger();
        final List<String> versionLabels = new CopyOnWriteArrayList<>();
        final List<Parameter> asked = new CopyOnWriteArrayList<>();
        volatile Optional<String> version = Optional.of(CURRENT);

        @Override
        public String name() {
            return "FAKE";
        }

        @Override
        public Optional<String> currentVersion(String cvLabel) {
            versionCalls.incrementAndGet();
            versionLabels.add(cvLabel);
            return version;
        }

        @Override
        public List<Parameter> ancestors(Parameter term) {
            asked.add(term);
            return ANSWER;
        }
    }

    private static CvMappingRule instrumentNameRule() {
        CvMappingRule rule = new CvMappingRule();
        rule.setId("instrument_name_must");
        rule.setCvElementPath("/metadata/instrument/@name");
        rule.setRequirementLevel(CvMappingRule.RequirementLevel.MUST);
        rule.setScopePath("/metadata/instrument");
        rule.setCvTermsCombinationLogic(CvMappingRule.CvTermsCombinationLogic.OR);
        CvTerm term = new CvTerm();
        term.setTermAccession("MS:1000031");
        term.setTermName("instrument model");
        term.setIsRepeatable(Boolean.FALSE);
        term.setAllowChildren(true);
        term.setUseTerm(false);
        CvReference ref = new CvReference();
        ref.setCvIdentifier("MS");
        ref.setCvName("PSI-MS");
        term.setCvIdentifierRef(ref);
        rule.getCvTerm().add(term);
        return rule;
    }

    private static CV cv(String label, String version) {
        return new CV().label(label).fullName(label).version(version).uri(URI.create("https://example.org/" + label));
    }

    private static Parameter param(String label, String accession, String name) {
        return new Parameter().cvLabel(label).cvAccession(accession).name(name);
    }

    private static byte[] resource(String name) throws IOException {
        try (InputStream in = VocabularyResolverTest.class.getResourceAsStream(name)) {
            return in.readAllBytes();
        }
    }
}

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
    /** A term of the mini MS file, asked for under the label BTO. */
    private static final Parameter BTO_TERM = param("BTO", "MS:1000073", "electrospray ionization");
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
            assertSubstitute(resolver, V100, "SUBSTITUTE_NEWER", V150, "the local cache");
            assertTrue(only(resolver.resolved()).reRunnable());
            assertEquals(0, newer.current.versionCalls.get());

            // then the latest
            Fixture latest = new Fixture(reason, Set.of(V100, V150));
            resolver = latest.resolver(true, cv("MS", V100));
            assertFalse(resolver.ancestors(ESI).isEmpty());
            assertSubstitute(resolver, V100, "SUBSTITUTE_LATEST", V200, "the local cache");
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
    public void aDuplicateEntryWithoutAVersionIsNamedAsSuch() throws Exception {
        // final review M3
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        VocabularyResolver resolver = f.resolver(false, cv("MS", V150), cv("MS", null));

        String message = only(resolver.messages()).getMessage();
        assertTrue(message.startsWith("The controlled vocabulary \"MS\" is declared twice, in version"
            + " \"4.1.150\" and in version \"(none)\";"), message);
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

    @Test
    public void anUnlistedIsoDateIsTriedAsAReleaseThenReadAsADate() throws Exception {
        // Ruling R-Y (amended), shaped like BTO: the tag 2022-10-26 holds data-version 2021-10-26, released that day
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        f.server.file("/bto/v2022-10-26/bto.obo", miniMs);
        f.server.file("/bto/v2023-03-01/bto.obo", miniMs);
        PreferredResources listed = PreferredResources.load(f.writeResources(List.of(btoEntry(f,
            "{\"kind\": \"listed\", \"releases\": [{\"version\": \"2021-04-30\", \"date\": \"2021-04-30\"},"
            + " {\"version\": \"2022-10-26\", \"date\": \"2021-10-26\"}]}"))));

        // an unlisted real release is used as is
        VocabularyResolver real = f.resolver(listed, false, cv("BTO", "2023-03-01"));
        assertFalse(real.ancestors(BTO_TERM).isEmpty());
        ResolvedVocabulary asIs = only(real.resolved());
        assertEquals("FILE", asIs.reason());
        assertEquals("2023-03-01", asIs.versionUsed());
        assertEquals(Optional.of("2023-03-01"), asIs.declaredNormalised());
        assertEquals(List.of(), real.messages());

        // an unlisted date that is no release goes through the fallback, and says so
        VocabularyResolver date = f.resolver(listed, false, cv("BTO", "2021-10-26"));
        assertFalse(date.ancestors(BTO_TERM).isEmpty());
        ResolvedVocabulary fallback = only(date.resolved());
        assertEquals("FILE_DATE_FALLBACK", fallback.reason());
        assertEquals("2022-10-26", fallback.versionUsed());
        assertEquals("2021-10-26", fallback.declaredOriginal());
        assertEquals(Optional.of("2022-10-26"), fallback.declaredNormalised());
        assertEquals(List.of(), date.messages());
        assertFalse(Files.exists(f.root.resolve("BTO").resolve("2021-10-26")), "no folder for a release that is none");

        // a listed release that is not at its source has no fallback: strict, NO_SUCH_RELEASE
        VocabularyResolver missing = f.resolver(listed, false, cv("BTO", "2021-04-30"));
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> missing.ancestors(BTO_TERM));
        assertEquals(Optional.of(Reason.NO_SUCH_RELEASE), e.kind());
        assertEquals("2021-04-30", e.versionWanted());
    }

    @Test
    public void anIsoDateWithoutAReleaseListIsOnlyTriedAsARelease() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        f.server.file("/bto/v2022-10-26/bto.obo", miniMs);
        f.server.route("/api/repos/o/bto/tags", x -> TestServer.send(x, 500, new byte[0]));
        PreferredResources unlisted = PreferredResources.load(f.writeResources(List.of(btoEntry(f,
            "{\"kind\": \"githubTags\", \"repo\": \"o/bto\", \"tagPattern\": \"v{version}\","
            + " \"dates\": \"githubReleases\"}"))));

        VocabularyResolver real = f.resolver(unlisted, false, cv("BTO", "2022-10-26"));
        assertFalse(real.ancestors(BTO_TERM).isEmpty());
        assertEquals("FILE", only(real.resolved()).reason());

        // with no list there is no date to fall back on: the release's own failure, strict
        VocabularyResolver date = f.resolver(unlisted, false, cv("BTO", "2021-10-26"));
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> date.ancestors(BTO_TERM));
        assertEquals(Optional.of(Reason.NO_SUCH_RELEASE), e.kind());
        assertEquals("2021-10-26", e.versionWanted());
    }

    @Test
    public void aFallbackThatCannotBeHadEitherNamesBothAndForcedSubstitutesAsBefore() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        f.server.file("/bto/v2023-03-01/bto.obo", miniMs);
        PreferredResources listed = PreferredResources.load(f.writeResources(List.of(btoEntry(f,
            "{\"kind\": \"listed\", \"releases\": [{\"version\": \"2022-10-26\", \"date\": \"2021-10-26\"},"
            + " {\"version\": \"2023-03-01\", \"date\": \"2023-03-01\"}]}"))));

        VocabularyResolver strict = f.resolver(listed, false, cv("BTO", "2021-10-26"));
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> strict.ancestors(BTO_TERM));
        assertEquals(Optional.of(Reason.NO_SUCH_RELEASE), e.kind());
        assertEquals("2021-10-26", e.versionWanted());
        assertTrue(e.reason().contains("read as a date it is 2022-10-26"), e.reason());

        VocabularyResolver forced = f.resolver(listed, true, cv("BTO", "2021-10-26"));
        assertFalse(forced.ancestors(BTO_TERM).isEmpty());
        ResolvedVocabulary substitute = only(forced.resolved());
        assertEquals("SUBSTITUTE_LATEST", substitute.reason());
        assertEquals("2023-03-01", substitute.versionUsed());
        assertEquals(List.of(CrossCheckErrorType.CvVersionSubstituted), types(forced.messages()));
    }

    // ---- Rulings R-AE and R-AD: an unlisted ISO date that is no release ----

    @Test
    public void anUnlistedIsoDateBeforeTheFirstReleaseThatIsNoReleaseIsNoSuchRelease() throws Exception {
        // Ruling R-AE: it was tried as a release name and its source has none; there is no date to fall back on
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        f.server.file("/bto/v2022-10-26/bto.obo", miniMs);
        PreferredResources listed = PreferredResources.load(f.writeResources(List.of(btoEntry(f,
            "{\"kind\": \"listed\", \"releases\": [{\"version\": \"2022-10-26\", \"date\": \"2021-10-26\"}]}"))));

        VocabularyResolver strict = f.resolver(listed, false, cv("BTO", "2020-01-01"));
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> strict.ancestors(BTO_TERM));
        assertEquals(Optional.of(Reason.NO_SUCH_RELEASE), e.kind());
        assertEquals("2020-01-01", e.versionWanted());
        assertEquals(List.of(), strict.messages(), "not NOT_RECOGNISED: no Warn-3021");
        assertEquals(1, f.server.hits("/bto/v2020-01-01/bto.obo"), "it was tried as a release");

        VocabularyResolver forced = f.resolver(listed, true, cv("BTO", "2020-01-01"));
        assertFalse(forced.ancestors(BTO_TERM).isEmpty());
        ResolvedVocabulary substitute = only(forced.resolved());
        assertEquals("SUBSTITUTE_NEWER", substitute.reason());
        assertEquals("2022-10-26", substitute.versionUsed());
        assertEquals(List.of(CrossCheckErrorType.CvVersionSubstituted), types(forced.messages()));
    }

    @Test
    public void offlineAnUnlistedIsoDateThatIsNotCachedHasNoDateFallback() throws Exception {
        // Ruling R-AD: offline cannot tell a missing release from an unlisted real one
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        PreferredResources listed = PreferredResources.load(f.writeResources(List.of(btoEntry(f,
            "{\"kind\": \"listed\", \"releases\": [{\"version\": \"2021-04-30\", \"date\": \"2021-04-30\"},"
            + " {\"version\": \"2022-10-26\", \"date\": \"2021-10-26\"}]}"))));
        // the release the date fallback would give is cached
        f.prefill("BTO", "2022-10-26", "bto.obo", miniMs);

        VocabularyResolver strict = f.offline(listed, false, cv("BTO", "2021-10-26"));
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> strict.ancestors(BTO_TERM));
        assertEquals(Optional.of(Reason.OFFLINE_NOT_CACHED), e.kind());
        assertEquals("2021-10-26", e.versionWanted());
        assertEquals(List.of(), strict.resolved());

        // forced, it substitutes from the cache as usual (Ruling R-S)
        VocabularyResolver forced = f.offline(listed, true, cv("BTO", "2021-10-26"));
        assertFalse(forced.ancestors(BTO_TERM).isEmpty());
        ResolvedVocabulary substitute = only(forced.resolved());
        assertEquals("SUBSTITUTE_NEWER", substitute.reason());
        assertEquals("2022-10-26", substitute.versionUsed());
        assertEquals(0, f.server.totalHits(), "offline means no network");
    }

    @Test
    public void anUnreadableIndexNamesItsFailure() throws Exception {
        // final review M4: the reason is the I/O failure's message (VocabularyCache.message, never "null")
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        CachedRelease release = f.cache(false).get(f.resources.forLabel("MS").orElseThrow(), V150);
        // a valid header over a truncated body: the cache keeps it, opening it fails
        byte[] whole = Files.readAllBytes(release.index());
        Files.write(release.index(), java.util.Arrays.copyOf(whole, whole.length - 1));
        VocabularyResolver resolver = f.resolver(false, cv("MS", V150));

        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> resolver.ancestors(ESI));

        assertEquals(Optional.of(Reason.PARSE_FAILED), e.kind());
        assertTrue(e.reason().startsWith("its index " + release.index() + " cannot be read: " + release.index()
            + " is not a term index: truncated records"), e.reason());
    }

    // ---- Ruling R-AF: offline, a pre-filled cache names its newest release as the latest ----

    @Test
    public void offlineAPreFilledCacheAnswersAnUndeclaredOrUnrecognisedVersionWithItsNewestRelease() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        PreferredResources github = PreferredResources.load(f.writeResources(List.of(githubMsEntry(f))));
        f.prefill("MS", V100, "psi-ms.obo", miniMs);
        f.prefill("MS", V150, "psi-ms.obo", miniMs);
        // neither a newer folder that holds no release nor a name outside the scheme counts
        f.prefill("MS", "4.1.900", "notes.txt", miniMs);
        f.prefill("MS", "old", "psi-ms.obo", miniMs);

        // not declared at all, and declared with a value that names no release
        VocabularyResolver undeclared = f.offline(github, false);
        assertFalse(undeclared.ancestors(ESI).isEmpty());
        assertCachedLatest(undeclared, null, List.of(CrossCheckErrorType.CvVersionNotDeclared));
        VocabularyResolver unrecognised = f.offline(github, false, cv("MS", "2020"));
        assertFalse(unrecognised.ancestors(ESI).isEmpty());
        assertCachedLatest(unrecognised, "2020",
            List.of(CrossCheckErrorType.CvVersionNotRecognised, CrossCheckErrorType.CvVersionNotDeclared));
        assertEquals(0, f.server.totalHits(), "offline means no network");
        assertFalse(Files.exists(f.root.resolve("MS").resolve("latest.json")), "a guess from the cache is not kept");
        assertTrue(Files.exists(f.root.resolve("MS").resolve("4.1.900").resolve("notes.txt")), "nothing is removed");
    }

    @Test
    public void offlineADeclaredDateWithoutAReleaseListIsStrictAndForcedSubstitutesTheNewestCachedRelease()
        throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        PreferredResources github = PreferredResources.load(f.writeResources(List.of(githubMsEntry(f))));
        f.prefill("MS", V100, "psi-ms.obo", miniMs);
        f.prefill("MS", V150, "psi-ms.obo", miniMs);

        // a date names a release only through the release list, which offline is not in the cache
        VocabularyResolver strict = f.offline(github, false, cv("MS", "14-06-2024"));
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> strict.ancestors(ESI));
        assertEquals(Optional.of(Reason.OFFLINE_NOT_CACHED), e.kind());
        assertEquals("14-06-2024", e.versionWanted());
        assertEquals(List.of(), strict.messages());

        VocabularyResolver forced = f.offline(github, true, cv("MS", "14-06-2024"));
        assertFalse(forced.ancestors(ESI).isEmpty());
        assertSubstitute(forced, "14-06-2024", "SUBSTITUTE_LATEST", V150, "the local cache (its newest release)");
        // so does a declared release that is not cached
        VocabularyResolver missing = f.offline(github, true, cv("MS", V200));
        assertFalse(missing.ancestors(ESI).isEmpty());
        assertSubstitute(missing, V200, "SUBSTITUTE_LATEST", V150, "the local cache (its newest release)");
        assertEquals(0, f.server.totalHits(), "offline means no network");
        assertEquals(0, f.current.versionCalls.get(), "offline, the current-release source is not asked");
    }

    @Test
    public void offlineTheNewestReleaseOfAnUnlistedNewestReleaseEntryComesFromTheCacheToo() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        PreferredResources unlisted = PreferredResources.load(f.writeResources(List.of(btoEntry(f,
            "{\"kind\": \"githubTags\", \"repo\": \"o/bto\", \"tagPattern\": \"v{version}\","
            + " \"dates\": \"githubReleases\"}"))));
        f.prefill("BTO", "2021-04-30", "bto.obo", miniMs);
        f.prefill("BTO", "2022-10-26", "bto.obo", miniMs);

        VocabularyResolver resolver = f.offline(unlisted, false);
        assertFalse(resolver.ancestors(BTO_TERM).isEmpty());
        ResolvedVocabulary bto = only(resolver.resolved());
        assertEquals("LATEST", bto.reason());
        assertEquals("2022-10-26", bto.versionUsed());
        assertEquals(0, f.server.totalHits());
    }

    @Test
    public void offlineAnEmptyCacheStillCannotNameALatestRelease() throws Exception {
        Fixture f = new Fixture(Reason.NO_SUCH_RELEASE, Set.of());
        PreferredResources github = PreferredResources.load(f.writeResources(List.of(githubMsEntry(f))));

        VocabularyResolver strict = f.offline(github, false);
        VocabularyUnavailableException e = assertThrows(VocabularyUnavailableException.class,
            () -> strict.ancestors(ESI));
        assertEquals(Optional.of(Reason.OFFLINE_NOT_CACHED), e.kind());
        assertEquals("latest", e.versionWanted());
        assertTrue(e.reason().contains("nor any release of MS"), e.reason());
        // forced, there is no substitute either: the current-release source is offline
        f.current.version = Optional.empty();
        VocabularyResolver forced = f.offline(github, true);
        assertThrows(VocabularyUnavailableException.class, () -> forced.ancestors(ESI));
    }

    /** An MS entry shaped like the shipped one: GitHub tags and the latest tag, nothing listed. */
    private static String githubMsEntry(Fixture f) {
        return """
            "MS": {
              "labels": ["MS"],
              "oboPrefix": "MS",
              "location": {"format": "OBO", "urlTemplate": "%s/ms/v{version}/psi-ms.obo"},
              "scheme": "DOTTED",
              "releases": {"kind": "githubTags", "repo": "o/ms", "tagPattern": "v{version}", "dates": "githubReleases"},
              "latest": {"kind": "githubLatestTag", "repo": "o/ms", "tagPattern": "v{version}"}
            }""".formatted(f.server.base());
    }

    private static void assertCachedLatest(VocabularyResolver resolver, String declaredOriginal,
        List<MZTabErrorType> types) {
        ResolvedVocabulary ms = only(resolver.resolved());
        assertEquals("LATEST", ms.reason());
        assertEquals(V150, ms.versionUsed());
        assertEquals(declaredOriginal, ms.declaredOriginal());
        assertEquals(V150, ms.release().orElseThrow().version());
        assertEquals(types, types(resolver.messages()));
        assertEquals("No version is declared for the controlled vocabulary \"MS\"; the latest version \"4.1.150\""
            + " (the newest release in the cache, as downloads are off) was used.",
            resolver.messages().get(resolver.messages().size() - 1).getMessage());
    }

    private static String btoEntry(Fixture f, String releases) {
        return """
            "BTO": {
              "labels": ["BTO"],
              "oboPrefix": "MS",
              "location": {"format": "OBO", "urlTemplate": "%s/bto/v{version}/bto.obo"},
              "scheme": "ISO_DATE",
              "releases": %s,
              "latest": {"kind": "newestRelease"}
            }""".formatted(f.server.base(), releases);
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
            return new VocabularyCache(root, VocabularyCacheTest.purlToServer(server), offline, DAY, Clock.systemUTC(), server.api(),
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
            return resolver(source, resources, forced, reason == Reason.OFFLINE_NOT_CACHED, cvs);
        }

        VocabularyResolver offline(PreferredResources resources, boolean forced, CV... cvs) {
            return resolver(Source.LOCAL, resources, forced, true, cvs);
        }

        private VocabularyResolver resolver(Source source, PreferredResources resources, boolean forced,
            boolean offline, CV... cvs) {
            LookupPolicy policy = new LookupPolicy(source, forced, cache(offline), resources, current);
            return new VocabularyResolver(policy, List.of(cvs), Map.of());
        }

        /** Puts a release's file alone into its cache folder, as an operator pre-fills it. */
        void prefill(String key, String version, String file, byte[] content) throws IOException {
            Path folder = Files.createDirectories(root.resolve(key).resolve(version));
            Files.write(folder.resolve(file), content);
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

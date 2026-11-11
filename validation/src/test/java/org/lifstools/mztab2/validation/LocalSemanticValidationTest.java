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
package org.lifstools.mztab2.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lifstools.mztab2.test.utils.ClassPathFile.MINIMAL_EXAMPLE;

import info.psidev.cvmapping.CvMapping;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.cvmapping.CvParameterLookupService;
import org.lifstools.mztab2.cvmapping.LookupServiceAccess;
import org.lifstools.mztab2.cvmapping.OlsAncestorSource;
import org.lifstools.mztab2.cvmapping.local.CurrentReleaseSource;
import org.lifstools.mztab2.cvmapping.local.LookupPolicy;
import org.lifstools.mztab2.cvmapping.local.LookupPolicy.Source;
import org.lifstools.mztab2.cvmapping.local.PreferredResources;
import org.lifstools.mztab2.cvmapping.local.ResolvedVocabulary;
import org.lifstools.mztab2.cvmapping.local.VocabularyCache;
import org.lifstools.mztab2.io.MzTabFileParser;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.ValidationMessage;
import org.lifstools.mztab2.test.utils.ExtractClassPathFiles;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * Semantic validation with a lookup policy, against vocabularies pre-filled
 * into an offline cache: no network.
 *
 * <p>
 * The minimal example declares MS 3.54.0; the cache holds MS 4.1.200 only
 * (the mini PSI-MS file).</p>
 */
public class LocalSemanticValidationTest {

    @RegisterExtension
    static final ExtractClassPathFiles FILES = new ExtractClassPathFiles(MINIMAL_EXAMPLE);

    private static final URL DEFAULT_MAPPING = CvMappingValidator.class.getResource("/mappings/mzTab-M-mapping.xml");
    private static final String CACHED_MS = "4.1.200";
    private static final String DECLARED_MS = "3.54.0";
    private static final String POST = "the post validator ran";

    /** The vocabularies of the MappingRulesTest fixture, each read from the same fixture file. */
    private static final List<String> FIXTURE_VOCABULARIES = List.of("UO", "NCIT", "NCBITaxon", "BTO", "CL", "DOID");
    private static final String FIXTURE_MS = "4.0.18";
    private static final String FIXTURE_VERSION = "1.0.0";

    @TempDir
    Path temp;

    @Test
    public void aStrictFailureGivesOne3017AndNo3015() throws Exception {
        MzTab mzTab = minimalExample();
        Path root = cacheWithMiniMs();
        CountingCurrent current = new CountingCurrent();
        LookupPolicy policy = policy(root, false, current, entry("MS", CACHED_MS, "psi-ms.obo"));

        CvMappingValidator validator = CvMappingValidator.of(CvMappingValidator.readMapping(DEFAULT_MAPPING), mzTab,
            policy, true).withPostValidator(post());
        List<ValidationMessage> messages = validator.validate(mzTab);

        List<ValidationMessage> unavailable = withCode(messages, "3017");
        assertEquals(1, unavailable.size(), messages::toString);
        ValidationMessage error = unavailable.get(0);
        assertEquals(ValidationMessage.MessageTypeEnum.ERROR, error.getMessageType());
        assertTrue(error.getMessage().startsWith("The controlled vocabulary \"MS\" in version \"" + DECLARED_MS
            + "\" is not available: "), error.getMessage());
        assertEquals(List.of(), withCode(messages, "3015"), messages::toString);
        // the first rule looks MS up: the rules stop there, so no rule message
        assertEquals(List.of(), messages.stream().filter(m -> m.getRuleId() != null).toList());
        // the post validators still ran, after the 3017
        assertEquals(POST, messages.get(messages.size() - 1).getMessage());
        assertTrue(messages.indexOf(error) < messages.size() - 1);
        CvParameterLookupService service = validator.lookupService();
        assertEquals(List.of(), service.resolvedVocabularies());
        assertFalse(LookupServiceAccess.hasOlsClient(service), "no OLS client for a local, strict run");
        assertEquals(0, current.calls.get());
    }

    @Test
    public void aForcedRunReportsTheSubstituteAndChecksTheRules() throws Exception {
        MzTab mzTab = minimalExample();
        Path root = cacheWithMiniMs();
        CountingCurrent current = new CountingCurrent();
        LookupPolicy policy = policy(root, true, current, entry("MS", CACHED_MS, "psi-ms.obo"));

        CvMappingValidator validator = CvMappingValidator.of(CvMappingValidator.readMapping(DEFAULT_MAPPING), mzTab,
            policy, true).withPostValidator(post());
        List<ValidationMessage> messages = validator.validate(mzTab);

        assertEquals(List.of(), withCode(messages, "3017"), messages::toString);
        List<ValidationMessage> substituted = withCode(messages, "3018");
        ValidationMessage ms = substituted.stream().filter(m -> m.getMessage().contains("\"MS\"")).findFirst()
            .orElseThrow(() -> new AssertionError(messages.toString()));
        assertEquals(ValidationMessage.MessageTypeEnum.WARN, ms.getMessageType());
        assertTrue(ms.getMessage().contains(DECLARED_MS) && ms.getMessage().contains(CACHED_MS)
            && ms.getMessage().contains("local"), ms.getMessage());

        // the rules ran to the end: rules of other vocabularies after the MS ones were applied
        Set<String> ruleIds = messages.stream().map(ValidationMessage::getRuleId).filter(r -> r != null)
            .collect(Collectors.toSet());
        assertTrue(ruleIds.contains("msrun_format_may"), ruleIds::toString);
        assertTrue(ruleIds.contains("sample_species_may"), ruleIds::toString);
        // ESI is a source in the mini PSI-MS: answered from the substitute release
        assertTrue(messages.stream().noneMatch(m -> "instrument_source_may".equals(m.getRuleId())
            && m.getMessage().contains("MS:1000073")), messages::toString);

        // order: the rule messages, then the source messages, then the post validators
        int lastRule = lastIndexOf(messages, m -> m.getRuleId() != null);
        int firstSource = messages.indexOf(substituted.get(0));
        assertTrue(lastRule < firstSource, messages::toString);
        assertEquals(POST, messages.get(messages.size() - 1).getMessage());
        assertEquals(validator.lookupService().sourceMessages().size(),
            messages.size() - 1 - firstSource, messages::toString);

        List<ResolvedVocabulary> used = validator.lookupService().resolvedVocabularies();
        ResolvedVocabulary msUsed = used.stream().filter(v -> v.label().equals("MS")).findFirst().orElseThrow();
        assertEquals(CACHED_MS, msUsed.versionUsed());
        assertEquals("SUBSTITUTE_NEWER", msUsed.reason());
        assertTrue(msUsed.reRunnable());
        // a vocabulary the resources file does not know is answered by the current release source
        List<ResolvedVocabulary> others = used.stream().filter(v -> !v.label().equals("MS")).toList();
        assertFalse(others.isEmpty(), used::toString);
        assertTrue(others.stream().allMatch(v -> "OLS".equals(v.reason()) && !v.reRunnable()), used::toString);
        assertTrue(current.calls.get() > 0);
        assertFalse(LookupServiceAccess.hasOlsClient(validator.lookupService()));
    }

    @Test
    public void theLegacyFactoryStillUsesOls() throws Exception {
        CvMappingValidator validator = CvMappingValidator.of(DEFAULT_MAPPING, true);

        CvParameterLookupService service = validator.lookupService();
        assertInstanceOf(OlsAncestorSource.class, LookupServiceAccess.source(service));
        assertEquals(List.of(), service.sourceMessages());
        assertEquals(List.of(), service.resolvedVocabularies());
    }

    @Test
    public void sameMessagesAsTheStubForTheMappingRulesFixture() throws Exception {
        Path root = temp.resolve("cache");
        List<String> entries = new ArrayList<>();
        entries.add(entry("MS", FIXTURE_MS, "fixture.obo"));
        prefill(root, "MS", FIXTURE_MS, "fixture.obo", "/cv/mapping-rules.obo");
        for (String key : FIXTURE_VOCABULARIES) {
            entries.add(entry(key, FIXTURE_VERSION, "fixture.obo"));
            prefill(root, key, FIXTURE_VERSION, "fixture.obo", "/cv/mapping-rules.obo");
        }
        CountingCurrent current = new CountingCurrent();
        LookupPolicy policy = policy(root, false, current, entries.toArray(String[]::new));
        CvMapping mapping = MappingRulesTest.mapping();

        for (Parameter unit : List.of(
            param("UO", "UO:0000065", "nanomolar"),
            param("NCIT", "NCIT:C67432", "Nanomole per Liter"),
            param("NCIT", "NCIT:C48512", "Mole"),
            param("MS", "MS:1001844", "MS1 feature area"),
            param("MS", "MS:1002887", "Progenesis QI normalised abundance"),
            param("MS", "MS:1000040", "m/z"))) {
            List<ValidationMessage> stub = CvMappingValidator.of(mapping, MappingRulesTest.LOOKUP, true)
                .validate(fixture(unit));
            MzTab file = fixture(unit);
            CvMappingValidator local = CvMappingValidator.of(mapping, file, policy, true);
            List<ValidationMessage> messages = local.validate(file);

            assertTrue(stub.stream().anyMatch(m -> m.getRuleId() != null), stub::toString);
            assertEquals(stub, messages, () -> unit.getCvAccession());
            CvParameterLookupService service = local.lookupService();
            assertEquals(List.of(), service.sourceMessages());
            Set<String> labels = service.resolvedVocabularies().stream().map(ResolvedVocabulary::label)
                .collect(Collectors.toSet());
            assertTrue(labels.containsAll(List.of("MS", "NCBITaxon", "BTO", "CL", "DOID", unit.getCvLabel())),
                labels::toString);
            assertFalse(LookupServiceAccess.hasOlsClient(service));
        }
        assertEquals(0, current.calls.get());
    }

    // ---- helpers ----

    /** The MappingRulesTest fixture with the given units, declaring every vocabulary it uses. */
    private static MzTab fixture(Parameter unit) {
        MzTab mzTab = MappingRulesTest.fixture();
        mzTab.getMetadata().smallMoleculeQuantificationUnit(unit).smallMoleculeFeatureQuantificationUnit(unit);
        assertEquals(FIXTURE_MS, mzTab.getMetadata().getCv().get(0).getVersion());
        int id = mzTab.getMetadata().getCv().size();
        for (String label : FIXTURE_VOCABULARIES) {
            CV cv = new CV().label(label).fullName(label).version(FIXTURE_VERSION)
                .uri(URI.create("http://localhost.invalid/" + label));
            cv.id(++id);
            mzTab.getMetadata().addCvItem(cv);
        }
        return mzTab;
    }

    private static MzTab minimalExample() throws IOException {
        File file = new File(FILES.getBaseDir(), MINIMAL_EXAMPLE.fileName());
        MzTabFileParser parser = new MzTabFileParser(file);
        parser.parse(OutputStream.nullOutputStream(), MZTabErrorType.Level.Info, 500);
        MzTab mzTab = parser.getMZTabFile();
        assertNotNull(mzTab, () -> parser.getErrorList().toString());
        assertEquals(DECLARED_MS, mzTab.getMetadata().getCv().get(0).getVersion());
        return mzTab;
    }

    private Path cacheWithMiniMs() throws IOException {
        Path root = temp.resolve("cache");
        prefill(root, "MS", CACHED_MS, "psi-ms.obo", "/cv/mini-ms.obo");
        return root;
    }

    /** An operator's pre-fill: the vocabulary file alone in its release folder. */
    private static void prefill(Path root, String key, String version, String fileName, String resource)
        throws IOException {
        Path folder = Files.createDirectories(root.resolve(key).resolve(version));
        try (InputStream in = LocalSemanticValidationTest.class.getResourceAsStream(resource)) {
            Files.copy(in, folder.resolve(fileName));
        }
    }

    private LookupPolicy policy(Path root, boolean forced, CurrentReleaseSource current, String... entries)
        throws Exception {
        Path file = Files.createTempFile(temp, "resources", ".json");
        Files.writeString(file, "{\"vocabularies\": {" + String.join(", ", entries) + "}}", StandardCharsets.UTF_8);
        VocabularyCache cache = new VocabularyCache(root, uri -> {
            throw new IOException("no download in this test: " + uri);
        }, true, Duration.ofHours(24));
        return new LookupPolicy(Source.LOCAL, forced, cache, PreferredResources.load(file), current);
    }

    /** A vocabulary with one listed release, never downloaded. */
    private static String entry(String key, String version, String fileName) {
        return """
            "%1$s": {
              "labels": ["%1$s"],
              "oboPrefix": "%1$s",
              "location": {"format": "OBO", "urlTemplate": "http://localhost.invalid/%1$s/{version}/%2$s"},
              "scheme": "DOTTED",
              "releases": {"kind": "listed", "releases": [{"version": "%3$s"}]},
              "latest": {"kind": "newestRelease"}
            }""".formatted(key, fileName, version);
    }

    private static Validator<MzTab> post() {
        return new Validator<>() {
            @Override
            public List<ValidationMessage> validate(MzTab mzTab) {
                return List.of(new ValidationMessage().message(POST)
                    .messageType(ValidationMessage.MessageTypeEnum.INFO));
            }
        };
    }

    private static List<ValidationMessage> withCode(List<ValidationMessage> messages, String code) {
        return messages.stream().filter(m -> m.getCode() != null && m.getCode().matches("(?s)\\[\\w+-" + code + "\\].*"))
            .toList();
    }

    private static int lastIndexOf(List<ValidationMessage> messages,
        java.util.function.Predicate<ValidationMessage> p) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (p.test(messages.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static Parameter param(String cvLabel, String accession, String name) {
        return new Parameter().cvLabel(cvLabel).cvAccession(accession).name(name);
    }

    /** A current-release source that names one version and answers every term without ancestors. */
    private static final class CountingCurrent implements CurrentReleaseSource {

        final AtomicInteger calls = new AtomicInteger();

        @Override
        public String name() {
            return "fake";
        }

        @Override
        public Optional<String> currentVersion(String cvLabel) {
            calls.incrementAndGet();
            return Optional.of("2026-11-01");
        }

        @Override
        public List<Parameter> ancestors(Parameter term) {
            calls.incrementAndGet();
            return List.of();
        }
    }
}

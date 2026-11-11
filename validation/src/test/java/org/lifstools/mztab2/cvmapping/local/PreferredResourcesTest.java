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

import info.psidev.cvmapping.CvMapping;
import info.psidev.cvmapping.CvReference;
import jakarta.xml.bind.JAXBContext;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Format;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Latest;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Location;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseDates;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseIndex;

public class PreferredResourcesTest {

    private static final String USER_MS = """
        {
          "vocabularies": {
            "MS": {
              "labels": ["PSI-MS"],
              "oboPrefix": "MS",
              "location": {"format": "OBO", "urlTemplate": "https://mirror.example.org/psi-ms/{version}/psi-ms.obo"},
              "scheme": "%s",
              "releases": {"kind": "listed", "releases": [{"version": "4.1.200", "date": "2025-06-01"}]},
              "latest": {"kind": "newestRelease"}
            }
          }
        }
        """;

    @TempDir
    Path dir;

    @Test
    public void theDefaultIsValidAndCoversTheShippedMapping() throws Exception {
        CvMapping mapping = (CvMapping) JAXBContext.newInstance(CvMapping.class)
            .createUnmarshaller()
            .unmarshal(PreferredResourcesTest.class.getResource("/mappings/mzTab-M-mapping.xml"));
        PreferredResources defaults = PreferredResources.defaults();
        assertFalse(mapping.getCvReferenceList().getCvReference().isEmpty());
        for (CvReference ref : mapping.getCvReferenceList().getCvReference()) {
            Optional<VocabularyEntry> entry = defaults.forLabel(ref.getCvIdentifier());
            assertTrue(entry.isPresent(), "no entry for " + ref.getCvIdentifier());
            assertEquals(ref.getCvIdentifier(), entry.get().key());
        }
        assertEquals("OBI", defaults.forLabel("OBI").orElseThrow().key());
    }

    @Test
    public void theDefaultMsEntryUsesThePsiMsTags() {
        VocabularyEntry ms = PreferredResources.defaults().forLabel("MS").orElseThrow();
        assertEquals(VersionScheme.DOTTED, ms.scheme());
        assertEquals(new ReleaseIndex.GithubTags("HUPO-PSI/psi-ms-CV", "v{version}", ReleaseDates.GITHUB_RELEASES),
            ms.releases());
        assertEquals(new Latest.GithubLatestTag("HUPO-PSI/psi-ms-CV", "v{version}"), ms.latest());
        assertEquals(URI.create("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/v4.1.200/psi-ms.obo"),
            ms.location().url("4.1.200"));
    }

    @Test
    public void aSchemaErrorNamesTheEntry() throws Exception {
        Path file = write(USER_MS.formatted("semver"));
        InvalidResourcesException e = assertThrows(InvalidResourcesException.class,
            () -> PreferredResources.load(file));
        assertTrue(e.getMessage().contains("MS"), e.getMessage());
        assertTrue(e.getMessage().contains("/vocabularies/MS/scheme"), e.getMessage());
        assertEquals(Optional.of("MS"), e.entryKey());
        assertEquals("/vocabularies/MS/scheme", e.pointer());
    }

    @Test
    public void aFileThatIsNotJsonNamesTheFile() throws Exception {
        Path file = write("{\"vocabularies\": {");
        InvalidResourcesException e = assertThrows(InvalidResourcesException.class,
            () -> PreferredResources.load(file));
        assertTrue(e.getMessage().contains(file.toString()), e.getMessage());
        assertEquals(Optional.empty(), e.entryKey());
    }

    @Test
    public void aListedVersionOutsideTheSchemeNamesTheEntry() throws Exception {
        Path file = write(USER_MS.formatted("ISO_DATE"));
        InvalidResourcesException e = assertThrows(InvalidResourcesException.class,
            () -> PreferredResources.load(file));
        assertEquals(Optional.of("MS"), e.entryKey());
        assertEquals("/vocabularies/MS/releases/releases/0/version", e.pointer());
    }

    @Test
    public void versionDatesNeedIsoDateVersions() throws Exception {
        Path file = write("""
            {"vocabularies": {"X": {
              "labels": ["X"], "oboPrefix": "X",
              "location": {"format": "OBO", "urlTemplate": "https://example.org/{version}/x.obo"},
              "scheme": "DOTTED",
              "releases": {"kind": "githubTags", "repo": "o/x", "tagPattern": "v{version}", "dates": "version"},
              "latest": {"kind": "githubLatestTag", "repo": "o/x", "tagPattern": "v{version}"}
            }}}
            """);
        InvalidResourcesException e = assertThrows(InvalidResourcesException.class,
            () -> PreferredResources.load(file));
        assertEquals(Optional.of("X"), e.entryKey());
        assertEquals("/vocabularies/X/releases/dates", e.pointer());
    }

    private static final String URL_ENTRY = """
        {"vocabularies": {"X": {
          "labels": ["X"], "oboPrefix": "X",
          "location": {"format": "OBO", "urlTemplate": "%s"},
          "scheme": "RELEASE_NUMBER",
          "releases": {"kind": "directoryListing", "url": "%s", "entryPattern": "rel{version}/", "dates": "none"},
          "latest": %s
        }}}
        """;
    private static final String GOOD_TEMPLATE = "https://example.org/rel{version}/x.obo";
    private static final String GOOD_LISTING = "https://example.org/archive/";
    private static final String NEWEST = "{\"kind\": \"newestRelease\"}";

    @Test
    public void anInvalidUrlTemplateNamesTheEntry() throws Exception {
        assertInvalidUrl(URL_ENTRY.formatted("https://example.org/a|b/{version}/x.obo", GOOD_LISTING, NEWEST),
            "/vocabularies/X/location/urlTemplate");
        assertInvalidUrl(URL_ENTRY.formatted("https://example.org/%zz/{version}/x.obo", GOOD_LISTING, NEWEST),
            "/vocabularies/X/location/urlTemplate");
    }

    @Test
    public void anInvalidDirectoryListingUrlNamesTheEntry() throws Exception {
        assertInvalidUrl(URL_ENTRY.formatted(GOOD_TEMPLATE, "https://example.org/a|b/", NEWEST),
            "/vocabularies/X/releases/url");
    }

    @Test
    public void anInvalidHeaderUrlNamesTheEntry() throws Exception {
        assertInvalidUrl(URL_ENTRY.formatted(GOOD_TEMPLATE, GOOD_LISTING,
            "{\"kind\": \"header\", \"url\": \"https://example.org/a^b.obo\"}"),
            "/vocabularies/X/latest/url");
    }

    private void assertInvalidUrl(String json, String pointer) throws Exception {
        Path file = write(json);
        InvalidResourcesException e = assertThrows(InvalidResourcesException.class,
            () -> PreferredResources.load(file));
        assertEquals(Optional.of("X"), e.entryKey());
        assertEquals(pointer, e.pointer());
        assertTrue(e.getMessage().contains("entry X") && e.getMessage().contains(pointer), e.getMessage());
    }

    @Test
    public void aLabelClaimedByTwoEntriesNamesBoth() throws Exception {
        Path file = write("""
            {"vocabularies": {
              "A": {"labels": ["SAME"], "oboPrefix": "A",
                "location": {"format": "OBO", "oboFoundryId": "a"}, "scheme": "ISO_DATE",
                "releases": {"kind": "listed", "releases": []}, "latest": {"kind": "newestRelease"}},
              "B": {"labels": ["B"], "oboPrefix": "same",
                "location": {"format": "OBO", "oboFoundryId": "b"}, "scheme": "ISO_DATE",
                "releases": {"kind": "listed", "releases": []}, "latest": {"kind": "newestRelease"}}
            }}
            """);
        InvalidResourcesException e = assertThrows(InvalidResourcesException.class,
            () -> PreferredResources.load(file));
        assertTrue(e.getMessage().contains("A") && e.getMessage().contains("B"), e.getMessage());
        assertEquals(Optional.of("B"), e.entryKey());
    }

    @Test
    public void aUserEntryReplacesTheDefaultOne() throws Exception {
        PreferredResources user = PreferredResources.load(write(USER_MS.formatted("DOTTED")));
        PreferredResources merged = PreferredResources.defaults().overriddenBy(user);

        VocabularyEntry ms = merged.forLabel("PSI-MS").orElseThrow();
        assertEquals(URI.create("https://mirror.example.org/psi-ms/4.1.200/psi-ms.obo"),
            ms.location().url("4.1.200"));
        assertInstanceOf(ReleaseIndex.Listed.class, ms.releases());
        assertEquals(new Latest.NewestRelease(), ms.latest());
        assertTrue(ms.knownSha256().isEmpty());
        // the whole entry is replaced: the default's other label is gone with it
        assertEquals(ms, merged.forLabel("MS").orElseThrow());
        // other default entries stay
        assertEquals("CHEBI", merged.forLabel("CHEBI").orElseThrow().key());
        // the default itself is unchanged
        assertEquals(Format.OBO, PreferredResources.defaults().forLabel("MS").orElseThrow().location().format());
        assertInstanceOf(ReleaseIndex.GithubTags.class,
            PreferredResources.defaults().forLabel("MS").orElseThrow().releases());
    }

    @Test
    public void labelsMatchIgnoringCase() {
        PreferredResources defaults = PreferredResources.defaults();
        assertEquals("MS", defaults.forLabel("ms").orElseThrow().key());
        assertEquals("MS", defaults.forLabel("psi-ms").orElseThrow().key());
        assertEquals("NCBITaxon", defaults.forLabel("ncbitaxon").orElseThrow().key());
        assertEquals("NCBITaxon", defaults.forLabel("NCBITAXON").orElseThrow().key());
        assertEquals("CHEBI", defaults.forLabel("ChEBI").orElseThrow().key());
        assertEquals(Optional.empty(), defaults.forLabel("NOSUCHCV"));
        assertEquals(Optional.empty(), defaults.forLabel(null));
    }

    @Test
    public void theOboPrefixMatchesIgnoringCase() throws Exception {
        PreferredResources user = PreferredResources.load(write(USER_MS.formatted("DOTTED")));
        assertEquals("MS", user.forLabel("ms").orElseThrow().key());
        assertEquals("MS", user.forLabel("Psi-Ms").orElseThrow().key());
    }

    @Test
    public void anOboFoundryLocationUsesTheVersionedPurlOverHttps() {
        Location obo = new Location(Format.OBO, Optional.empty(), Optional.of("doid"));
        assertEquals(URI.create("https://purl.obolibrary.org/obo/doid/releases/2026-09-30/doid.obo"),
            obo.url("2026-09-30"));
        Location gz = new Location(Format.OBO_GZ, Optional.empty(), Optional.of("x"));
        assertEquals(URI.create("https://purl.obolibrary.org/obo/x/releases/1/x.obo.gz"), gz.url("1"));
        Location owl = new Location(Format.OWL_RDFXML, Optional.empty(), Optional.of("obi"));
        assertEquals(URI.create("https://purl.obolibrary.org/obo/obi/releases/2026-07-27/obi.owl"),
            owl.url("2026-07-27"));
    }

    @Test
    public void everyDefaultDownloadStartsOverHttps() {
        // Ruling R-AG: a cached release is kept and checksummed for good, so its first hop must not be plaintext
        for (VocabularyEntry e : PreferredResources.defaults().entries()) {
            URI url = e.location().url("1");
            assertEquals("https", url.getScheme(), e.key() + ": " + url);
        }
    }

    @Test
    public void aVersionThatIsNotAPathSegmentHasNoUrl() {
        Location obo = new Location(Format.OBO, Optional.empty(), Optional.of("doid"));
        assertThrows(IllegalArgumentException.class, () -> obo.url("../x"));
        assertThrows(IllegalArgumentException.class, () -> obo.url("a b"));
    }

    @Test
    public void aTagPatternMapsTagsAndVersions() {
        ReleaseIndex.GithubTags tags = new ReleaseIndex.GithubTags("HUPO-PSI/psi-ms-CV", "v{version}",
            ReleaseDates.GITHUB_RELEASES);
        assertEquals("v4.1.200", tags.tagFor("4.1.200"));
        assertEquals(Optional.of("4.1.200"), tags.versionOf("v4.1.200"));
        assertEquals(Optional.empty(), tags.versionOf("4.1.200"));
        assertEquals(Optional.empty(), tags.versionOf("v"));
        Latest.GithubLatestTag latest = new Latest.GithubLatestTag("o/r", "release-{version}-final");
        assertEquals(Optional.of("2.0"), latest.versionOf("release-2.0-final"));
        ReleaseIndex.DirectoryListing listing = new ReleaseIndex.DirectoryListing(
            URI.create("https://example.org/archive/"), "rel{version}/", ReleaseDates.NONE);
        assertEquals(Optional.of("256"), listing.versionOf("rel256/"));
    }

    @Test
    public void everyIndexKindSaysHowDatesAreFound() {
        PreferredResources defaults = PreferredResources.defaults();
        assertEquals(ReleaseDates.VERSION, defaults.forLabel("DOID").orElseThrow().releases().dates());
        assertEquals(ReleaseDates.NONE, defaults.forLabel("CHEBI").orElseThrow().releases().dates());
        assertEquals(ReleaseDates.LISTED, new ReleaseIndex.Listed(java.util.List.of()).dates());
    }

    private Path write(String json) throws Exception {
        Path file = Files.createTempFile(dir, "resources", ".json");
        Files.writeString(file, json, StandardCharsets.UTF_8);
        return file;
    }
}

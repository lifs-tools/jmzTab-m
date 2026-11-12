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
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Format;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Latest;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseIndex;

/**
 * Checks the shipped preferred-resources file against the real release
 * locations: each entry's "latest" names a concrete version, and that version's
 * file can be fetched and looks like the declared format. The lookups here are
 * a minimal stand-in for the vocabulary cache's own (Task 7).
 */
@Tag("network")
public class PreferredResourcesNetworkTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(TIMEOUT)
        .build();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern HREF = Pattern.compile("href=\"([^\"]+)\"");
    private static final Pattern NEXT_PAGE = Pattern.compile("<([^>]+)>;\\s*rel=\"next\"");

    @TestFactory
    public Stream<DynamicTest> everyDefaultEntryResolvesItsLatestRelease() {
        return PreferredResources.defaults().entries().stream()
            .map(e -> DynamicTest.dynamicTest(e.key(), () -> resolvesLatest(e)));
    }

    private static void resolvesLatest(VocabularyEntry e) throws Exception {
        String version = latest(e).orElseThrow(() -> new AssertionError(e.key() + ": no latest version"));
        assertTrue(e.scheme().accepts(version), e.key() + ": latest " + version + " is outside " + e.scheme());
        URI url = e.location().url(version);
        // the cache's own downloader: it upgrades an https-to-http redirect (R-AJ), as CHEBI's PURL needs
        byte[] head;
        URI finalUrl;
        try (InputStream in = Downloader.http(TIMEOUT).get(url)) {
            finalUrl = ((Downloader.Download) in).location();
            head = in.readNBytes(1024);
        }
        assertTrue("https".equals(finalUrl.getScheme()), e.key() + " " + version + ": " + url + " ended at " + finalUrl);
        assertTrue(looksLike(e.location().format(), head),
            e.key() + " " + version + ": " + finalUrl + " is not " + e.location().format());
    }

    private static Optional<String> latest(VocabularyEntry e) throws IOException, InterruptedException {
        if (e.latest() instanceof Latest.GithubLatestTag l) {
            return newest(e.scheme(), githubTags(l.repo()), l::versionOf);
        }
        if (e.latest() instanceof Latest.NewestRelease) {
            if (e.releases() instanceof ReleaseIndex.GithubTags t) {
                return newest(e.scheme(), githubTags(t.repo()), t::versionOf);
            }
            if (e.releases() instanceof ReleaseIndex.DirectoryListing d) {
                return newest(e.scheme(), directoryEntries(d.url()), d::versionOf);
            }
            if (e.releases() instanceof ReleaseIndex.Listed l) {
                return newest(e.scheme(), l.releases().stream().map(Release::version).toList(), Optional::of);
            }
        }
        fail(e.key() + ": no check for latest " + e.latest() + " with releases " + e.releases());
        return Optional.empty();
    }

    private static Optional<String> newest(VersionScheme scheme, List<String> names,
        Function<String, Optional<String>> versionOf) {
        return names.stream()
            .map(versionOf)
            .flatMap(Optional::stream)
            .filter(scheme::accepts)
            .max((a, b) -> scheme.compare(a, b).orElseThrow());
    }

    private static List<String> githubTags(String repo) throws IOException, InterruptedException {
        List<String> tags = new ArrayList<>();
        URI next = URI.create("https://api.github.com/repos/" + repo + "/tags?per_page=100");
        while (next != null) {
            HttpRequest.Builder request = HttpRequest.newBuilder(next)
                .timeout(TIMEOUT)
                .header("User-Agent", "jmztabm-tests")
                .header("Accept", "application/vnd.github+json");
            String token = System.getenv("GITHUB_TOKEN");
            if (token != null && !token.isBlank()) {
                request.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = HTTP.send(request.GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertTrue(response.statusCode() == 200, repo + " tags: " + response.statusCode() + " " + response.body());
            for (JsonNode tag : JSON.readTree(response.body())) {
                tags.add(tag.get("name").asText());
            }
            next = response.headers().firstValue("Link")
                .map(NEXT_PAGE::matcher)
                .filter(Matcher::find)
                .map(m -> URI.create(m.group(1)))
                .orElse(null);
        }
        return tags;
    }

    private static List<String> directoryEntries(URI url) throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(url)
            .timeout(TIMEOUT)
            .header("User-Agent", "jmztabm-tests")
            .GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertTrue(response.statusCode() == 200, url + ": " + response.statusCode());
        List<String> names = new ArrayList<>();
        Matcher m = HREF.matcher(response.body());
        while (m.find()) {
            names.add(m.group(1));
        }
        return names;
    }

    private static boolean looksLike(Format format, byte[] head) {
        String text = new String(head, StandardCharsets.UTF_8).stripLeading();
        return switch (format) {
            case OBO -> text.startsWith("format-version:");
            case OBO_GZ -> head.length > 2 && (head[0] & 0xff) == 0x1f && (head[1] & 0xff) == 0x8b;
            case OWL_RDFXML -> (text.startsWith("<?xml") || text.startsWith("<rdf:RDF"))
                && text.contains("rdf:RDF");
        };
    }
}

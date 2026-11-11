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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * One vocabulary of the preferred-resources file (design §4.3): the
 * identifiers it is known by, where its releases are downloaded, how its
 * versions compare, how its releases and their dates are listed, and how its
 * latest release is found.
 *
 * @param key the entry's key, the mapping's {@code cvIdentifier} (e.g. MS)
 * @param labels the mzTab-M {@code cv[n]-label} values it is known by
 * @param oboPrefix the prefix of its term ids (MS for MS:1000031)
 * @param location where one release is downloaded
 * @param scheme how its release names compare
 * @param releases how its releases, and their dates, are listed
 * @param latest how its latest release is found
 * @param knownSha256 known SHA-256 checksums of release files, by version
 */
public record VocabularyEntry(String key, Set<String> labels, String oboPrefix, Location location,
    VersionScheme scheme, ReleaseIndex releases, Latest latest, Map<String, String> knownSha256) {

    private static final String VERSION = "{version}";
    /** A version is put into a URL as one path segment, unescaped. */
    private static final Pattern URL_SAFE_VERSION = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");

    public VocabularyEntry {
        Objects.requireNonNull(key, "key");
        labels = Set.copyOf(labels);
        Objects.requireNonNull(oboPrefix, "oboPrefix");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(scheme, "scheme");
        Objects.requireNonNull(releases, "releases");
        Objects.requireNonNull(latest, "latest");
        knownSha256 = Map.copyOf(knownSha256);
    }

    /**
     * @return true if the label or the OBO prefix is {@code cvLabel}, ignoring case
     */
    public boolean isKnownAs(String cvLabel) {
        return cvLabel != null
            && (oboPrefix.equalsIgnoreCase(cvLabel) || labels.stream().anyMatch(cvLabel::equalsIgnoreCase));
    }

    /** The file format of a release. */
    public enum Format {
        OBO("obo"), OBO_GZ("obo.gz"), OWL_RDFXML("owl");

        private final String extension;

        Format(String extension) {
            this.extension = extension;
        }

        /** @return the file extension OBO Foundry uses for it, e.g. obo.gz */
        public String extension() {
            return extension;
        }
    }

    /**
     * Where one release is downloaded: either a URL template with
     * {@code {version}}, or an OBO Foundry id for
     * {@code http://purl.obolibrary.org/obo/{id}/releases/{version}/{id}.{ext}}.
     */
    public record Location(Format format, Optional<String> urlTemplate, Optional<String> oboFoundryId) {

        public Location {
            Objects.requireNonNull(format, "format");
            if (urlTemplate.isPresent() == oboFoundryId.isPresent()) {
                throw new IllegalArgumentException("a location has either a urlTemplate or an oboFoundryId");
            }
        }

        /**
         * @param version a release name
         * @return the URL of that release
         * @throws IllegalArgumentException if the version is not a plain path
         * segment (letters, digits, '.', '_', '-')
         */
        public URI url(String version) {
            if (version == null || !URL_SAFE_VERSION.matcher(version).matches()) {
                throw new IllegalArgumentException("not a version that fits a URL: " + version);
            }
            String template = urlTemplate.orElseGet(() -> {
                String id = oboFoundryId.orElseThrow();
                return "http://purl.obolibrary.org/obo/" + id + "/releases/" + VERSION + "/" + id + "."
                    + format.extension();
            });
            return URI.create(template.replace(VERSION, version));
        }
    }

    /** How the release dates of a release index are found. */
    public enum ReleaseDates {
        /** The published_at of the GitHub release of each tag; a tag without a release has no date. */
        GITHUB_RELEASES,
        /** Each version is its own date (ISO_DATE versions only). */
        VERSION,
        /** Each listed release gives its date, if known. */
        LISTED,
        /** No release has a known date. */
        NONE
    }

    /** How the releases of a vocabulary are listed, and how their dates are found. */
    public sealed interface ReleaseIndex {

        ReleaseDates dates();

        /**
         * The tags of a GitHub repository that match a pattern, from the tags
         * API (paged). With {@code v{version}}, tag {@code v4.1.200} is version
         * {@code 4.1.200}.
         */
        record GithubTags(String repo, String tagPattern, ReleaseDates dates) implements ReleaseIndex {

            public GithubTags {
                Objects.requireNonNull(repo, "repo");
                requireVersionPattern(tagPattern);
                if (dates == ReleaseDates.LISTED) {
                    throw new IllegalArgumentException("GitHub tags have no listed dates");
                }
            }

            /** @return the tag of a version */
            public String tagFor(String version) {
                return tagPattern.replace(VERSION, version);
            }

            /** @return the version a tag names, if it matches the pattern */
            public Optional<String> versionOf(String tag) {
                return versionIn(tagPattern, tag);
            }
        }

        /**
         * The links of an HTTP directory index (e.g. an FTP archive served over
         * HTTP) whose names match a pattern, e.g. {@code rel{version}/}.
         */
        record DirectoryListing(URI url, String entryPattern, ReleaseDates dates) implements ReleaseIndex {

            public DirectoryListing {
                Objects.requireNonNull(url, "url");
                requireVersionPattern(entryPattern);
                if (dates != ReleaseDates.VERSION && dates != ReleaseDates.NONE) {
                    throw new IllegalArgumentException("a directory listing has version dates or none");
                }
            }

            /** @return the version a link names, if it matches the pattern */
            public Optional<String> versionOf(String link) {
                return versionIn(entryPattern, link);
            }
        }

        /** The releases, written out, each with its date if known. */
        record Listed(List<Release> releases) implements ReleaseIndex {

            public Listed {
                releases = List.copyOf(releases);
            }

            @Override
            public ReleaseDates dates() {
                return ReleaseDates.LISTED;
            }
        }
    }

    /** How the latest release of a vocabulary is found. */
    public sealed interface Latest {

        /** The newest tag of a GitHub repository that matches a pattern, by the entry's scheme. */
        record GithubLatestTag(String repo, String tagPattern) implements Latest {

            public GithubLatestTag {
                Objects.requireNonNull(repo, "repo");
                requireVersionPattern(tagPattern);
            }

            /** @return the version a tag names, if it matches the pattern */
            public Optional<String> versionOf(String tag) {
                return versionIn(tagPattern, tag);
            }
        }

        /**
         * The version the unversioned file at {@code url} declares in its
         * header: {@code data-version:} (OBO) or {@code owl:versionIRI} (OWL).
         * The version is the segment of that value the entry's scheme accepts,
         * e.g. 2026-06-08 of {@code releases/2026-06-08}.
         */
        record Header(URI url) implements Latest {

            public Header {
                Objects.requireNonNull(url, "url");
            }
        }

        /** The newest release of the entry's release index, by its scheme. */
        record NewestRelease() implements Latest {
        }
    }

    private static void requireVersionPattern(String pattern) {
        Objects.requireNonNull(pattern, "pattern");
        int at = pattern.indexOf(VERSION);
        if (at < 0 || pattern.indexOf(VERSION, at + 1) >= 0) {
            throw new IllegalArgumentException("not one {version} in " + pattern);
        }
    }

    private static Optional<String> versionIn(String pattern, String name) {
        int at = pattern.indexOf(VERSION);
        String prefix = pattern.substring(0, at);
        String suffix = pattern.substring(at + VERSION.length());
        if (name == null || name.length() <= prefix.length() + suffix.length()
            || !name.startsWith(prefix) || !name.endsWith(suffix)) {
            return Optional.empty();
        }
        return Optional.of(name.substring(prefix.length(), name.length() - suffix.length()));
    }
}

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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Format;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Latest;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.Location;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseDates;
import org.lifstools.mztab2.cvmapping.local.VocabularyEntry.ReleaseIndex;

/**
 * The preferred-resources file (design §4.3): per vocabulary, where its
 * versioned releases come from. jmzTab-m ships a default; a user's file
 * replaces the default's entries by key.
 */
public final class PreferredResources {

    static final String DEFAULT_RESOURCE = "/cv/preferred-resources.json";
    static final String SCHEMA_RESOURCE = "/cv/preferred-resources.schema.json";
    private static final String VOCABULARIES = "/vocabularies/";

    private static final ObjectMapper JSON = JsonMapper.builder()
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .build();

    private final Map<String, VocabularyEntry> entries;

    private PreferredResources(Map<String, VocabularyEntry> entries) {
        this.entries = entries;
    }

    /**
     * @return the shipped default
     * @throws IllegalStateException if the shipped default is invalid
     */
    public static PreferredResources defaults() {
        return Defaults.INSTANCE;
    }

    /**
     * Reads a preferred-resources file, after checking it against the schema.
     *
     * @param userFile the file
     * @return its entries
     * @throws InvalidResourcesException if the file can't be read, isn't JSON,
     * doesn't fit the schema, or names a version outside an entry's scheme;
     * the message names the entry and the JSON pointer
     */
    public static PreferredResources load(Path userFile) throws InvalidResourcesException {
        try (InputStream in = Files.newInputStream(userFile)) {
            return read(in, userFile.toString());
        } catch (IOException e) {
            throw new InvalidResourcesException(userFile + ": cannot be read: " + e.getMessage(), null, "", e);
        }
    }

    /**
     * @param user entries that replace this file's entries with the same key
     * @return the entries of both; on a label both claim, the user's entry wins
     */
    public PreferredResources overriddenBy(PreferredResources user) {
        Map<String, VocabularyEntry> merged = new LinkedHashMap<>(user.entries);
        entries.forEach(merged::putIfAbsent);
        return new PreferredResources(Collections.unmodifiableMap(merged));
    }

    /**
     * @param cvLabel an mzTab-M cv label or an OBO prefix (the shipped entries
     * also list their mapping cvIdentifier as a label)
     * @return the first entry with that label or OBO prefix, ignoring case
     */
    public Optional<VocabularyEntry> forLabel(String cvLabel) {
        return entries.values().stream().filter(e -> e.isKnownAs(cvLabel)).findFirst();
    }

    /** @return all entries, in file order (a user's first after {@link #overriddenBy}) */
    public List<VocabularyEntry> entries() {
        return List.copyOf(entries.values());
    }

    private static PreferredResources read(InputStream in, String source) throws InvalidResourcesException {
        JsonNode root;
        try {
            root = JSON.readTree(in);
        } catch (JacksonException e) {
            throw new InvalidResourcesException(source + ": is not JSON: " + e.getOriginalMessage(), null, "", e);
        } catch (IOException e) {
            throw new InvalidResourcesException(source + ": cannot be read: " + e.getMessage(), null, "", e);
        }
        if (root == null || root.isMissingNode()) {
            throw new InvalidResourcesException(source + ": is empty", null, "", null);
        }
        List<ValidationMessage> problems = Schema.INSTANCE.validate(root).stream()
            .sorted(Comparator.comparing(m -> m.getInstanceLocation().toString()))
            .toList();
        if (!problems.isEmpty()) {
            String pointer = problems.get(0).getInstanceLocation().toString();
            String key = entryKeyOf(pointer);
            String texts = problems.stream()
                .map(m -> location(m.getInstanceLocation().toString()) + ": " + m.getError())
                .collect(Collectors.joining("; "));
            throw new InvalidResourcesException(source + ": " + (key == null ? "" : "entry " + key + ": ") + texts,
                key, pointer, null);
        }
        Map<String, VocabularyEntry> entries = new LinkedHashMap<>();
        Map<String, String> claimed = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> field : root.get("vocabularies").properties()) {
            VocabularyEntry entry = entry(source, field.getKey(), field.getValue());
            claim(source, entry, claimed);
            entries.put(entry.key(), entry);
        }
        return new PreferredResources(Collections.unmodifiableMap(entries));
    }

    private static VocabularyEntry entry(String source, String key, JsonNode n) throws InvalidResourcesException {
        String at = VOCABULARIES + escape(key);
        VersionScheme scheme = VersionScheme.valueOf(n.get("scheme").asText());
        Set<String> labels = new LinkedHashSet<>();
        n.get("labels").forEach(l -> labels.add(l.asText()));
        JsonNode loc = n.get("location");
        Location location = new Location(Format.valueOf(loc.get("format").asText()),
            text(loc, "urlTemplate"), text(loc, "oboFoundryId"));
        ReleaseIndex releases = releases(source, key, at + "/releases", n.get("releases"), scheme);
        Map<String, String> sha256 = new LinkedHashMap<>();
        if (n.has("knownSha256")) {
            for (Map.Entry<String, JsonNode> s : n.get("knownSha256").properties()) {
                requireInScheme(source, key, at + "/knownSha256/" + escape(s.getKey()), s.getKey(), scheme);
                sha256.put(s.getKey(), s.getValue().asText());
            }
        }
        return new VocabularyEntry(key, labels, n.get("oboPrefix").asText(), location, scheme, releases,
            latest(n.get("latest")), sha256);
    }

    private static ReleaseIndex releases(String source, String key, String at, JsonNode n, VersionScheme scheme)
        throws InvalidResourcesException {
        String kind = n.get("kind").asText();
        if ("listed".equals(kind)) {
            List<Release> releases = new ArrayList<>();
            int i = 0;
            for (JsonNode r : n.get("releases")) {
                String version = r.get("version").asText();
                requireInScheme(source, key, at + "/releases/" + i + "/version", version, scheme);
                Optional<LocalDate> date = Optional.empty();
                if (r.has("date")) {
                    try {
                        date = Optional.of(LocalDate.parse(r.get("date").asText()));
                    } catch (DateTimeParseException e) {
                        throw invalid(source, key, at + "/releases/" + i + "/date", "is not a calendar date");
                    }
                }
                releases.add(new Release(version, date));
                i++;
            }
            return new ReleaseIndex.Listed(releases);
        }
        ReleaseDates dates = switch (n.get("dates").asText()) {
            case "githubReleases" -> ReleaseDates.GITHUB_RELEASES;
            case "version" -> ReleaseDates.VERSION;
            default -> ReleaseDates.NONE;
        };
        if (dates == ReleaseDates.VERSION && scheme != VersionScheme.ISO_DATE) {
            throw invalid(source, key, at + "/dates", "\"version\" dates need the ISO_DATE scheme, not " + scheme);
        }
        if ("githubTags".equals(kind)) {
            return new ReleaseIndex.GithubTags(n.get("repo").asText(), n.get("tagPattern").asText(), dates);
        }
        return new ReleaseIndex.DirectoryListing(URI.create(n.get("url").asText()), n.get("entryPattern").asText(),
            dates);
    }

    private static Latest latest(JsonNode n) {
        return switch (n.get("kind").asText()) {
            case "githubLatestTag" -> new Latest.GithubLatestTag(n.get("repo").asText(), n.get("tagPattern").asText());
            case "header" -> new Latest.Header(URI.create(n.get("url").asText()));
            default -> new Latest.NewestRelease();
        };
    }

    /** One label or OBO prefix, ignoring case, belongs to one entry of a file. */
    private static void claim(String source, VocabularyEntry entry, Map<String, String> claimed)
        throws InvalidResourcesException {
        Map<String, String> names = new LinkedHashMap<>();
        entry.labels().forEach(l -> names.put(l, "/labels"));
        names.putIfAbsent(entry.oboPrefix(), "/oboPrefix");
        for (Map.Entry<String, String> name : names.entrySet()) {
            String folded = name.getKey().toUpperCase(Locale.ROOT);
            String owner = claimed.putIfAbsent(folded, entry.key());
            if (owner != null && !owner.equals(entry.key())) {
                throw invalid(source, entry.key(), VOCABULARIES + escape(entry.key()) + name.getValue(),
                    "\"" + name.getKey() + "\" is already a label or OBO prefix of entry " + owner);
            }
        }
    }

    private static void requireInScheme(String source, String key, String pointer, String version,
        VersionScheme scheme) throws InvalidResourcesException {
        if (!scheme.accepts(version)) {
            throw invalid(source, key, pointer, "\"" + version + "\" is not a version of the " + scheme + " scheme");
        }
    }

    private static InvalidResourcesException invalid(String source, String key, String pointer, String text) {
        return new InvalidResourcesException(source + ": entry " + key + ": " + pointer + ": " + text, key, pointer,
            null);
    }

    private static Optional<String> text(JsonNode n, String field) {
        return Optional.ofNullable(n.get(field)).map(JsonNode::asText);
    }

    private static String location(String pointer) {
        return pointer.isEmpty() ? "(top level)" : pointer;
    }

    /** @return the entry key a JSON pointer lies in, or null */
    private static String entryKeyOf(String pointer) {
        if (!pointer.startsWith(VOCABULARIES)) {
            return null;
        }
        String rest = pointer.substring(VOCABULARIES.length());
        int end = rest.indexOf('/');
        return unescape(end < 0 ? rest : rest.substring(0, end));
    }

    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    private static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~");
    }

    private static final class Schema {

        static final JsonSchema INSTANCE = load();

        private static JsonSchema load() {
            try (InputStream in = PreferredResources.class.getResourceAsStream(SCHEMA_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException("missing " + SCHEMA_RESOURCE);
                }
                SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                    .pathType(PathType.JSON_POINTER)
                    .locale(Locale.ENGLISH)
                    .build();
                return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(in, config);
            } catch (IOException e) {
                throw new IllegalStateException("cannot read " + SCHEMA_RESOURCE, e);
            }
        }
    }

    private static final class Defaults {

        static final PreferredResources INSTANCE = load();

        private static PreferredResources load() {
            try (InputStream in = PreferredResources.class.getResourceAsStream(DEFAULT_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException("missing " + DEFAULT_RESOURCE);
                }
                return read(in, "classpath:" + DEFAULT_RESOURCE.substring(1));
            } catch (IOException | InvalidResourcesException e) {
                throw new IllegalStateException("the shipped preferred-resources file is invalid: " + e.getMessage(),
                    e);
            }
        }
    }
}

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
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Records the OLS answers that {@link OlsParityTest} compares the local lookup
 * with (design §8: "at one fixed version"). It is run by hand, never by the
 * network job, since it rewrites the fixtures:
 *
 * <pre>
 * ./mvnw -B -nsu -pl validation -am test -Pnetwork -Dtest=OlsRecorder \
 *     -Dsurefire.failIfNoSpecifiedTests=false \
 *     -DolsRecorder.out=$PWD/validation/src/test/resources/cv/ols-parity
 * </pre>
 *
 * <p>
 * For each term it records {@code /api/ontologies/{o}/terms?obo_id=…} and the
 * closure of its {@code hierarchicalParents}, as the OLS client of
 * {@code OlsAncestorSource} walks it, as {@code <o>/<SHORTFORM>.json} in the
 * shape of the webapp's {@code ols-stub} ({@code {"term": {...}, "ancestors":
 * [...]}}). For each ontology it records {@code config.version} and
 * {@code config.versionIri} of {@code /api/ontologies/{o}}, the version
 * {@link OlsCurrentReleaseSource} derives from them ({@code olsCurrentVersion}),
 * the parity's fixed {@code version} with {@code versionFrom} (Ruling R-Z:
 * config.version where the vocabulary can resolve it, else the version the
 * versionIri names), and the recording date, in {@code versions.json}.</p>
 */
@Tag("network")
@EnabledIfSystemProperty(named = OlsRecorder.OUT, matches = ".+")
public class OlsRecorder {

    /** The system property naming the fixture folder to (re)write. */
    static final String OUT = "olsRecorder.out";

    private static final String OLS = "https://www.ebi.ac.uk/ols4/api/";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final int ATTEMPTS = 3;
    private static final int PAGE_SIZE = 500;
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(TIMEOUT)
        .build();
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * The terms of the webapp's OLS stub
     * ({@code jmzTab-m-webapp/server/src/test/resources/ols-stub/terms/}),
     * each recorded in the ontology of its prefix.
     */
    static final List<String> WEBAPP_STUB_TERMS = List.of(
        "BTO:0000759", "CL:0000182", "DOID:684", "DOID:9452", "MS:1000073", "MS:1000081", "MS:1000133",
        "MS:1000253", "MS:1000291", "MS:1000348", "MS:1000449", "MS:1000484", "MS:1000532", "MS:1000569",
        "MS:1000584", "MS:1000598", "MS:1000624", "MS:1000768", "MS:1000922", "MS:1001062", "MS:1001207",
        "MS:1001530", "MS:1001561", "MS:1001834", "MS:1001838", "MS:1001911", "MS:1002883", "MS:1002885",
        "MS:1002887", "MS:1002889", "MS:1002890", "MS:1002896", "MSIO:0000148", "NCBITaxon:12130",
        "NCBITaxon:573824", "NCBITaxon:9606", "UO:0000072");

    /**
     * One rule term of {@code mzTab-M-mapping.xml} per vocabulary of the
     * shipped preferred resources, by OLS ontology: OBI's is the MSIO rule's
     * term {@code OBI:0000094}, recorded once in MSIO and once in OBI.
     */
    static final Map<String, String> RULE_TERMS = rules();

    private static Map<String, String> rules() {
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("ms", "MS:1000031");
        rules.put("doid", "DOID:4");
        rules.put("cl", "CL:0000000");
        rules.put("bto", "BTO:0000000");
        rules.put("ncbitaxon", "NCBITaxon:1");
        rules.put("uo", "UO:0000051");
        rules.put("ncit", "NCIT:C48207");
        rules.put("pride", "PRIDE:0000307");
        rules.put("msio", "OBI:0000094");
        rules.put("chebi", "CHEBI:24433");
        rules.put("obi", "OBI:0000094");
        return rules;
    }

    /** The hierarchical parents per term IRI and ontology, asked once each. */
    private final Map<String, List<JsonNode>> parents = new HashMap<>();
    private int requests;

    @Test
    public void record() throws Exception {
        Path out = Path.of(System.getProperty(OUT));
        Files.createDirectories(out);
        try (Stream<Path> old = Files.walk(out)) {
            for (Path p : old.filter(p -> p.toString().endsWith(".json")).toList()) {
                Files.delete(p);
            }
        }
        List<String[]> terms = new ArrayList<>();
        WEBAPP_STUB_TERMS.forEach(id -> terms.add(new String[] {id.substring(0, id.indexOf(':'))
            .toLowerCase(Locale.ROOT), id}));
        RULE_TERMS.forEach((o, id) -> terms.add(new String[] {o, id}));
        Set<String> ontologies = new LinkedHashSet<>();
        for (String[] t : terms) {
            ontologies.add(t[0]);
            long start = System.nanoTime();
            int before = requests;
            Map<String, Object> fixture = fixture(t[0], t[1]);
            Path file = out.resolve(t[0]).resolve(t[1].replace(':', '_') + ".json");
            Files.createDirectories(file.getParent());
            JSON.writeValue(file.toFile(), fixture);
            System.out.printf("recorded %s/%s: %d ancestors, %d requests, %d ms%n", t[0], t[1],
                ((List<?>) fixture.get("ancestors")).size(), requests - before,
                (System.nanoTime() - start) / 1_000_000);
        }
        Map<String, Object> versions = new LinkedHashMap<>();
        versions.put("recorded", LocalDate.now(ZoneOffset.UTC).toString());
        versions.put("source", OLS);
        Map<String, Object> byOntology = new TreeMap<>();
        List<String> unversioned = new ArrayList<>();
        for (String o : ontologies) {
            JsonNode config = get(URI.create(OLS + "ontologies/" + o)).path("config");
            Optional<String> configVersion = text(config, "version");
            Optional<String> versionIri = text(config, "versionIri");
            Optional<String> iriVersion = versionIri.flatMap(VocabularyCache::purlVersion);
            // what OlsCurrentReleaseSource reports (Ruling R-T)
            Optional<String> current = configVersion.isPresent() ? configVersion : iriVersion;
            // the parity's fixed version (Ruling R-Z): config.version where the vocabulary can resolve it
            boolean configResolves = configVersion.filter(c -> resolvable(o, c)).isPresent();
            Optional<String> version = configResolves ? configVersion : iriVersion;
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("configVersion", configVersion.orElse(null));
            v.put("versionIri", versionIri.orElse(null));
            v.put("olsCurrentVersion", current.orElse(null));
            v.put("version", version.orElse(null));
            v.put("versionFrom", version.isEmpty() ? null : configResolves ? "config.version" : "versionIri");
            byOntology.put(o, v);
            if (current.isEmpty()) {
                unversioned.add(o);
            }
            System.out.printf("version %s: config.version=%s versionIri=%s -> %s, parity at %s (fileLocation %s)%n",
                o, configVersion.orElse("-"), versionIri.orElse("-"), current.orElse("NONE"),
                version.orElse("NONE"), text(config, "fileLocation").orElse("-"));
        }
        versions.put("ontologies", byOntology);
        JSON.writeValue(out.resolve("versions.json").toFile(), versions);
        System.out.println("no config.version and no versioned versionIri: " + unversioned);
        System.out.println("OLS requests: " + requests);
    }

    /**
     * Whether the vocabulary's resolver can take the version: a value of its
     * scheme, or a date (Ruling R-Y). NCIT's 26.02d, NCI's release name, is
     * neither; its versionIri names the OBO Edition's 2026-03-19.
     */
    private static boolean resolvable(String ontology, String version) {
        VocabularyEntry e = PreferredResources.defaults().forLabel(ontology).orElseThrow(
            () -> new IllegalStateException(ontology + " is no vocabulary of the shipped resources"));
        return e.scheme().accepts(version) || DeclaredVersion.isDate(version);
    }

    private Map<String, Object> fixture(String ontology, String oboId) throws IOException, InterruptedException {
        JsonNode found = get(URI.create(OLS + "ontologies/" + ontology + "/terms?obo_id="
            + URLEncoder.encode(oboId, StandardCharsets.UTF_8))).path("_embedded").path("terms");
        if (found.size() != 1) {
            throw new IllegalStateException(ontology + " has " + found.size() + " terms " + oboId);
        }
        JsonNode term = found.get(0);
        Map<String, Map<String, Object>> ancestors = new LinkedHashMap<>();
        Set<String> seen = new LinkedHashSet<>();
        seen.add(term.path("iri").asText());
        ArrayDeque<JsonNode> queue = new ArrayDeque<>();
        queue.add(term);
        while (!queue.isEmpty()) {
            for (JsonNode parent : parents(ontology, queue.poll())) {
                if (seen.add(parent.path("iri").asText())) {
                    ancestors.put(parent.path("iri").asText(), summary(parent));
                    queue.add(parent);
                }
            }
        }
        List<Map<String, Object>> sorted = new ArrayList<>(ancestors.values());
        sorted.sort(Comparator.comparing((Map<String, Object> a) -> String.valueOf(a.get("obo_id")))
            .thenComparing(a -> String.valueOf(a.get("iri"))));
        Map<String, Object> fixture = new LinkedHashMap<>();
        fixture.put("term", summary(term));
        fixture.put("ancestors", sorted);
        return fixture;
    }

    /** The term's hierarchicalParents, followed as the OLS client does: through the term's own link. */
    private List<JsonNode> parents(String ontology, JsonNode term) throws IOException, InterruptedException {
        String key = ontology + " " + term.path("iri").asText();
        List<JsonNode> known = parents.get(key);
        if (known != null) {
            return known;
        }
        List<JsonNode> found = new ArrayList<>();
        JsonNode link = term.path("_links").path("hierarchicalParents").path("href");
        String next = link.isTextual() ? withSize(https(link.asText())) : null;
        while (next != null) {
            JsonNode page = get(URI.create(next));
            page.path("_embedded").path("terms").forEach(found::add);
            JsonNode more = page.path("_links").path("next").path("href");
            next = more.isTextual() ? https(more.asText()) : null;
        }
        parents.put(key, found);
        return found;
    }

    private static Map<String, Object> summary(JsonNode term) {
        Map<String, Object> s = new LinkedHashMap<>();
        for (String field : List.of("obo_id", "label", "iri", "ontology_prefix")) {
            s.put(field, term.path(field).isNull() || term.path(field).isMissingNode() ? null
                : term.path(field).asText());
        }
        return s;
    }

    private static Optional<String> text(JsonNode node, String field) {
        JsonNode v = node.path(field);
        return v.isTextual() && !v.asText().isBlank() ? Optional.of(v.asText()) : Optional.empty();
    }

    /** OLS's links name http://, which redirects; ask https:// directly. */
    private static String https(String href) {
        return href.startsWith("http://www.ebi.ac.uk/") ? "https://" + href.substring("http://".length()) : href;
    }

    private static String withSize(String href) {
        return href + (href.contains("?") ? "&" : "?") + "size=" + PAGE_SIZE;
    }

    private JsonNode get(URI uri) throws IOException, InterruptedException {
        IOException last = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            requests++;
            HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .header("User-Agent", "jmztabm-tests")
                .header("Accept", "application/json")
                .GET().build();
            HttpResponse<byte[]> response;
            try {
                response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray());
            } catch (IOException e) {
                // a timeout or a dropped connection: ask again
                last = e;
                Thread.sleep(2_000L * attempt);
                continue;
            }
            if (response.statusCode() == 200) {
                return JSON.readTree(response.body());
            }
            last = new IOException(uri + " answered " + response.statusCode());
            if (response.statusCode() < 500) {
                throw last;
            }
            Thread.sleep(2_000L * attempt);
        }
        throw last;
    }
}

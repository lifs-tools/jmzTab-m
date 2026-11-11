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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class OwlRdfXmlReaderTest {

    private static final String MSIO = "/cv/mini-msio.owl";
    private static final String OBI = "/cv/mini-obi.owl";

    private static List<TermRecord> read(String resource, List<String> imports, List<String> versions)
        throws IOException {
        List<TermRecord> terms = new ArrayList<>();
        try (InputStream in = OwlRdfXmlReaderTest.class.getResourceAsStream(resource)) {
            new OwlRdfXmlReader().read(in, terms::add, imports::add, versions::add);
        }
        return terms;
    }

    private static Map<String, TermRecord> byId(List<TermRecord> terms) {
        Map<String, TermRecord> map = new LinkedHashMap<>();
        terms.forEach(t -> map.put(t.id(), t));
        return map;
    }

    @Test
    public void readsNamedClassesLabelsAndDeprecation() throws IOException {
        List<TermRecord> terms = read(MSIO, new ArrayList<>(), new ArrayList<>());
        Map<String, TermRecord> byId = byId(terms);

        // the English label wins over an earlier German one
        assertEquals("mass spectrometry", byId.get("MSIO:0000001").label());
        assertFalse(byId.get("MSIO:0000001").obsolete());
        assertTrue(byId.get("MSIO:0000002").obsolete());
        assertFalse(byId.get("MSIO:0000003").obsolete());
        // a class described through rdf:Description with rdf:type owl:Class
        assertEquals("described thing", byId.get("MSIO:0000004").label());
        // properties, axioms, annotations and anonymous classes are not terms
        assertEquals(List.of("MSIO:0000001", "MSIO:0000002", "MSIO:0000003", "OBI:0000094",
            "MSIO:0000004"), terms.stream().map(TermRecord::id).toList());
    }

    @Test
    public void readsSubClassOfAndPartOfRestrictions() throws IOException {
        Map<String, TermRecord> byId = byId(read(MSIO, new ArrayList<>(), new ArrayList<>()));

        // other properties and complex class expressions are ignored
        assertEquals(Map.of("is_a", List.of("MSIO:0000001"), "part_of", List.of("MSIO:0000002")),
            byId.get("MSIO:0000003").parentsByRelation());
        assertEquals(Map.of("is_a", List.of("MSIO:0000003")),
            byId.get("MSIO:0000004").parentsByRelation());
        assertEquals(Map.of(), byId.get("MSIO:0000001").parentsByRelation());
        // a stub class carries nothing
        assertEquals("", byId.get("OBI:0000094").label());
        assertEquals(Map.of(), byId.get("OBI:0000094").parentsByRelation());
    }

    @Test
    public void reportsImportsAndTheVersionIri() throws IOException {
        List<String> imports = new ArrayList<>();
        List<String> versions = new ArrayList<>();
        read(MSIO, imports, versions);

        // a relative import is resolved against xml:base
        assertEquals(List.of("http://purl.obolibrary.org/obo/mini-obi.owl"), imports);
        assertEquals(List.of("http://purl.obolibrary.org/obo/msio/releases/2023-05-01/msio.owl"), versions);
    }

    @Test
    public void anIriBecomesAnOboIdAndOthersAreKept() {
        assertEquals("MSIO:0000148", OwlRdfXmlReader.oboId("http://purl.obolibrary.org/obo/MSIO_0000148"));
        assertEquals("http://example.org/onto#Thing", OwlRdfXmlReader.oboId("http://example.org/onto#Thing"));
        assertEquals("http://purl.obolibrary.org/obo/msio.owl",
            OwlRdfXmlReader.oboId("http://purl.obolibrary.org/obo/msio.owl"));
    }

    @Test
    public void aTurtleFileIsAParseFailure() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/cv/mini-msio.ttl")) {
            IOException e = assertThrows(IOException.class,
                () -> new OwlRdfXmlReader().read(in, t -> { }, i -> { }, v -> { }));
            assertTrue(e.getMessage().contains("not OWL in RDF/XML"), e.getMessage());
        }
    }

    @Test
    public void xmlThatIsNotRdfIsAParseFailure() {
        InputStream in = new ByteArrayInputStream("<html/>".getBytes(StandardCharsets.UTF_8));
        IOException e = assertThrows(IOException.class,
            () -> new OwlRdfXmlReader().read(in, t -> { }, i -> { }, v -> { }));
        assertTrue(e.getMessage().contains("not OWL in RDF/XML"), e.getMessage());
    }

    private static final String RDF_OPEN = "<?xml version=\"1.0\"?>%s<rdf:RDF"
        + " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
        + " xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
        + " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">";

    @Test
    public void owlThingIsNoParent() throws IOException {
        // OLS lists no owl:Thing among the hierarchical parents (MSIO's merged file names it for BFO:0000001)
        String xml = String.format(RDF_OPEN, "")
            + "<owl:Class rdf:about=\"http://purl.obolibrary.org/obo/BFO_0000001\">"
            + "<rdfs:subClassOf rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/></owl:Class>"
            + "<owl:Class rdf:about=\"http://purl.obolibrary.org/obo/BFO_0000002\">"
            + "<rdfs:subClassOf rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>"
            + "<rdfs:subClassOf rdf:resource=\"http://purl.obolibrary.org/obo/BFO_0000001\"/></owl:Class>"
            + "</rdf:RDF>";
        List<TermRecord> terms = new ArrayList<>();
        new OwlRdfXmlReader().read(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
            terms::add, i -> { }, v -> { });
        Map<String, TermRecord> byId = byId(terms);
        assertEquals(Map.of(), byId.get("BFO:0000001").parentsByRelation());
        assertEquals(Map.of("is_a", List.of("BFO:0000001")), byId.get("BFO:0000002").parentsByRelation());
    }

    @Test
    public void externalEntitiesAreNotLoaded() throws IOException {
        String xml = String.format(RDF_OPEN, "<!DOCTYPE rdf:RDF [<!ENTITY x SYSTEM \"file:///etc/hosts\">]>")
            + "<owl:Class rdf:about=\"http://purl.obolibrary.org/obo/X_1\"><rdfs:label>a&x;b</rdfs:label>"
            + "</owl:Class></rdf:RDF>";
        List<TermRecord> terms = new ArrayList<>();
        new OwlRdfXmlReader().read(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
            terms::add, i -> { }, v -> { });
        // the entity resolves to nothing; the file is not read
        assertEquals("ab", terms.get(0).label());
    }

    @Test
    public void aLargeFileWithEntityPrefixesInEveryAttributeParses(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("big.owl");
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(String.format(RDF_OPEN, "<!DOCTYPE rdf:RDF [<!ENTITY obo \"http://purl.obolibrary.org/obo/\">]>"));
            for (int i = 0; i < 200_000; i++) {
                w.write("<owl:Class rdf:about=\"&obo;BIG_" + i + "\"><rdfs:subClassOf rdf:resource=\"&obo;BIG_"
                    + (i / 2) + "\"/></owl:Class>\n");
            }
            w.write("</rdf:RDF>");
        }
        int[] count = {0};
        try (InputStream in = Files.newInputStream(file)) {
            new OwlRdfXmlReader().read(in, t -> count[0]++, i -> { }, v -> { });
        }
        assertEquals(200_000, count[0]);
    }

    @Test
    public void aBillionLaughsDocumentIsRejectedByTheEntityLimits() {
        StringBuilder dtd = new StringBuilder("<!DOCTYPE rdf:RDF [<!ENTITY l0 \"lol\">");
        for (int i = 1; i <= 9; i++) {
            dtd.append("<!ENTITY l").append(i).append(" \"");
            for (int k = 0; k < 10; k++) {
                dtd.append("&l").append(i - 1).append(';');
            }
            dtd.append("\">");
        }
        dtd.append("]>");
        String xml = String.format(RDF_OPEN, dtd)
            + "<owl:Class rdf:about=\"http://purl.obolibrary.org/obo/X_1\"><rdfs:label>&l9;</rdfs:label>"
            + "</owl:Class></rdf:RDF>";
        IOException e = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> assertThrows(IOException.class,
            () -> new OwlRdfXmlReader().read(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                t -> { }, i -> { }, v -> { })));
        assertTrue(e.getMessage().startsWith("OWL file exceeds the XML entity limits"), e.getMessage());
    }

    @Test
    public void anImportedTermIsAnsweredFromTheImportingIndex(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("terms.idx");
        try (TermIndexWriter writer = TermIndexWriter.open(file)) {
            for (String resource : List.of(MSIO, OBI)) {
                try (InputStream in = getClass().getResourceAsStream(resource)) {
                    new OwlRdfXmlReader().read(in, writer, i -> { }, v -> { });
                }
            }
            writer.commit();
        }
        TermIndex index = TermIndex.open(file);
        assertEquals("material processing", index.term("OBI:0000094").orElseThrow().label());
        assertEquals(List.of("OBI:0000011"),
            index.ancestors("OBI:0000094").stream().map(TermRecord::id).toList());
    }
}

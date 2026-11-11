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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class OboReaderTest {

    static final String MINI_MS = "/cv/mini-ms.obo";

    /** Reads {@code mini-ms.obo} into a map by id, in file order. */
    static Map<String, TermRecord> readMiniMs() throws IOException {
        Map<String, TermRecord> terms = new LinkedHashMap<>();
        try (InputStream in = OboReaderTest.class.getResourceAsStream(MINI_MS)) {
            new OboReader().read(in, t -> terms.put(t.id(), t), v -> { });
        }
        return terms;
    }

    private static InputStream stream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void readsIdNameIsAAndCountedRelationships() throws IOException {
        Map<String, TermRecord> terms = readMiniMs();

        TermRecord esi = terms.get("MS:1000073");
        assertEquals("electrospray ionization", esi.label());
        assertEquals(Map.of("is_a", List.of("MS:1000008")), esi.parentsByRelation());

        assertEquals(Map.of("part_of", List.of("MS:1000458")),
            terms.get("MS:1000008").parentsByRelation());
        // a trailing {qualifier} block after the parent id is not part of it
        assertEquals(Map.of("is_a", List.of("MS:1000494")),
            terms.get("MS:1001742").parentsByRelation());
        // has_units is not a hierarchical relation, so it is not read
        assertEquals(Map.of("is_a", List.of("MS:1000455")),
            terms.get("MS:1000744").parentsByRelation());
        assertEquals(Map.of("is_a", List.of("MS:9999901", "MS:0000000")),
            terms.get("MS:9999902").parentsByRelation());
        assertEquals(Map.of(), terms.get("MS:0000000").parentsByRelation());
    }

    @Test
    public void partOfWrittenAsItsIdIsHierarchical() throws IOException {
        // ROBOT's OBO files (e.g. cl.obo) write part_of as BFO:0000050, which OLS counts
        List<TermRecord> terms = new ArrayList<>();
        new OboReader().read(stream("format-version: 1.2\n\n[Term]\nid: CL:1\nname: x\n"
            + "relationship: BFO:0000050 UBERON:2 ! part of y\nrelationship: RO:0002202 CL:3 ! develops from z\n"),
            terms::add, v -> { });
        assertEquals(Map.of("BFO:0000050", List.of("UBERON:2")), terms.get(0).parentsByRelation());
    }

    @Test
    public void skipsTypedefsAndComments() throws IOException {
        Map<String, TermRecord> terms = readMiniMs();

        assertEquals(17, terms.size());
        assertFalse(terms.containsKey("part_of"));
        assertFalse(terms.containsKey("has_units"));
        assertEquals("m/z", terms.get("MS:1000040").label());
        for (TermRecord t : terms.values()) {
            assertFalse(t.id().contains("!") || t.id().contains(" "), t.id());
            assertFalse(t.label().contains("!"), t.label());
            t.parentsByRelation().values().forEach(ps -> ps.forEach(
                p -> assertFalse(p.contains("!") || p.contains(" ") || p.contains("{"), p)));
        }
    }

    @Test
    public void marksObsoleteTerms() throws IOException {
        Map<String, TermRecord> terms = readMiniMs();

        assertTrue(terms.get("MS:1000042").obsolete());
        assertEquals("peak intensity", terms.get("MS:1000042").label());
        assertFalse(terms.get("MS:1000073").obsolete());
    }

    @Test
    public void readsTheDataVersionHeader() throws IOException {
        List<String> versions = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream(MINI_MS)) {
            new OboReader().read(in, t -> { }, versions::add);
        }
        assertEquals(List.of("4.1.200"), versions);

        List<String> none = new ArrayList<>();
        List<TermRecord> terms = new ArrayList<>();
        new OboReader().read(stream("format-version: 1.2\n\n[Term]\nid: X:1\nname: x\n"),
            terms::add, none::add);
        assertEquals(List.of(), none);
        assertEquals(List.of(new TermRecord("X:1", "x", false, Map.of())), terms);
    }

    @Test
    public void aNonOboStreamIsAParseFailure() {
        String html = "<!DOCTYPE html>\n<html><head><title>Not Found</title></head>"
            + "<body>[Term]\nid: MS:1</body></html>\n";
        String owl = "<?xml version=\"1.0\"?>\n"
            + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
            + "  <owl:Ontology rdf:about=\"http://purl.obolibrary.org/obo/ms.owl\"/>\n"
            + "</rdf:RDF>\n";
        for (String text : List.of(html, owl, "")) {
            IOException e = assertThrows(IOException.class,
                () -> new OboReader().read(stream(text), t -> { }, v -> { }));
            assertTrue(e.getMessage().contains("not an OBO file"), e.getMessage());
        }
    }

    @Test
    public void parentNamesComeFromTheCommentsOfParentLines() throws IOException {
        // minor 8 of the Task 11 review: a stub term is named as its parent line names it
        String obo = "format-version: 1.2\n\n[Term]\nid: X:1\nname: x\n"
            + "is_a: A:1 ! alpha\n"
            + "is_a: C:3\n"
            + "is_a: D:4 {source=\"x\"} ! delta  \n"
            + "is_a: E:5 !\n"
            + "relationship: part_of B:2 ! beta gamma\n"
            + "relationship: has_units U:1 ! unit\n"
            + "relationship: BFO:0000050 F:6 ! zeta \\! eta\n"
            + "\n[Typedef]\nid: part_of\nis_a: G:7 ! not a term's parent\n";
        List<String> names = new ArrayList<>();
        List<TermRecord> terms = new ArrayList<>();
        new OboReader().read(stream(obo), terms::add, v -> { }, (id, name) -> names.add(id + "=" + name));

        assertEquals(List.of("A:1=alpha", "D:4=delta", "B:2=beta gamma", "F:6=zeta ! eta"), names);
        assertEquals(Map.of("is_a", List.of("A:1", "C:3", "D:4", "E:5"), "part_of", List.of("B:2"),
            "BFO:0000050", List.of("F:6")), terms.get(0).parentsByRelation());
    }

    @Test
    public void miniMsNamesItsParentWithoutAStanza() throws IOException {
        Map<String, String> names = new LinkedHashMap<>();
        try (InputStream in = getClass().getResourceAsStream(MINI_MS)) {
            new OboReader().read(in, t -> { }, v -> { }, names::putIfAbsent);
        }
        assertEquals("unit", names.get("UO:0000000"));
        assertEquals("ionization type", names.get("MS:1000008"));
    }
}

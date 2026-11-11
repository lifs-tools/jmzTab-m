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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Reads the terms of an OWL file in RDF/XML into {@link TermRecord}s, the same
 * records {@link OboReader} produces.
 * <p>
 * Taken from the file: each named class (an {@code owl:Class}, or an
 * {@code rdf:Description} typed {@code owl:Class}, directly under
 * {@code rdf:RDF}) with its IRI turned into an obo id, its {@code rdfs:label}
 * (one without language or {@code en} is preferred, else the first),
 * {@code owl:deprecated}, its {@code rdfs:subClassOf} to a named class
 * ({@code is_a}) and its {@code rdfs:subClassOf} restriction with a
 * {@code someValuesFrom} over {@link #HIERARCHICAL_PROPERTIES}
 * ({@code part_of}). The ontology's {@code owl:imports} and
 * {@code owl:versionIRI} are reported. Everything else is ignored: anonymous
 * classes, other class expressions, axioms, properties, individuals.
 * <p>
 * Classes are emitted as they appear, without merging a stub declaration with
 * the full description of the same class; {@link TermIndexWriter} merges
 * duplicates. The reader loads no external entity and follows no import.
 */
public final class OwlRdfXmlReader {

    /** The properties whose restrictions count as {@code part_of} parents. */
    static final Set<String> HIERARCHICAL_PROPERTIES = Set.of("http://purl.obolibrary.org/obo/BFO_0000050");

    private static final String OWL = "http://www.w3.org/2002/07/owl#";
    private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    private static final String XML = "http://www.w3.org/XML/1998/namespace";
    private static final String OBO = "http://purl.obolibrary.org/obo/";
    private static final Pattern OBO_LOCAL = Pattern.compile("([^_/#]+)_([^/#]+)");
    private static final String NOT_OWL = "not OWL in RDF/XML";

    private static final XMLInputFactory FACTORY = newFactory();

    private static XMLInputFactory newFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        // internal entities (&obo;) are declared in a DOCTYPE and must expand
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, true);
        factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        // an external entity or DTD resolves to nothing
        factory.setXMLResolver((publicId, systemId, baseUri, namespace) -> new ByteArrayInputStream(new byte[0]));
        return factory;
    }

    /**
     * Turns an IRI into an obo id: {@code http://purl.obolibrary.org/obo/MSIO_0000148}
     * becomes {@code MSIO:0000148}. Any other IRI is kept as is.
     *
     * @param iri the IRI
     * @return the obo id, or the IRI itself
     */
    static String oboId(String iri) {
        if (iri.startsWith(OBO)) {
            Matcher m = OBO_LOCAL.matcher(iri.substring(OBO.length()));
            if (m.matches()) {
                return m.group(1) + ":" + m.group(2);
            }
        }
        return iri;
    }

    /**
     * Reads the classes of an OWL RDF/XML document.
     *
     * @param in the document; not closed
     * @param sink receives each named class as it ends
     * @param importIri receives each {@code owl:imports} IRI
     * @param versionIri receives each {@code owl:versionIRI}
     * @throws IOException if the stream cannot be read, or is not OWL in
     * RDF/XML (another syntax, or XML that is not {@code rdf:RDF})
     */
    public void read(InputStream in, Consumer<TermRecord> sink, Consumer<String> importIri,
        Consumer<String> versionIri) throws IOException {
        XMLStreamReader xml = null;
        try {
            xml = FACTORY.createXMLStreamReader(in);
            int depth = 0;
            String base = null;
            while (xml.hasNext()) {
                int event = xml.next();
                if (event == XMLStreamConstants.END_ELEMENT) {
                    depth--;
                } else if (event == XMLStreamConstants.START_ELEMENT) {
                    depth++;
                    if (depth == 1) {
                        if (!is(xml, RDF, "RDF")) {
                            throw new IOException(NOT_OWL + ": the root element is " + xml.getName());
                        }
                        base = xml.getAttributeValue(XML, "base");
                    } else if (depth == 2 && (is(xml, OWL, "Class") || is(xml, RDF, "Description"))) {
                        readClass(xml, base, sink);
                        depth--;
                    } else if (is(xml, OWL, "imports")) {
                        resource(xml, base, importIri);
                    } else if (is(xml, OWL, "versionIRI")) {
                        resource(xml, base, versionIri);
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new IOException(NOT_OWL + ": " + e.getMessage(), e);
        } finally {
            if (xml != null) {
                try {
                    xml.close();
                } catch (XMLStreamException ignored) {
                    // nothing to release beyond the caller's stream
                }
            }
        }
    }

    private static boolean is(XMLStreamReader xml, String namespace, String localName) {
        return namespace.equals(xml.getNamespaceURI()) && localName.equals(xml.getLocalName());
    }

    private static void resource(XMLStreamReader xml, String base, Consumer<String> target) {
        String iri = resolve(xml.getAttributeValue(RDF, "resource"), base);
        if (iri != null) {
            target.accept(iri);
        }
    }

    /** Resolves an IRI reference against {@code xml:base} if it is relative. */
    private static String resolve(String reference, String base) {
        if (reference == null) {
            return null;
        }
        reference = reference.trim();
        if (base == null || reference.contains(":")) {
            return reference;
        }
        try {
            return URI.create(base).resolve(reference).toString();
        } catch (IllegalArgumentException e) {
            return reference;
        }
    }

    /**
     * Consumes one class element, from its start tag to its end tag, and emits
     * it if it is named and a class.
     */
    private void readClass(XMLStreamReader xml, String base, Consumer<TermRecord> sink)
        throws XMLStreamException {
        boolean isClass = is(xml, OWL, "Class");
        String iri = resolve(xml.getAttributeValue(RDF, "about"), base);
        String label = null;
        boolean labelPreferred = false;
        boolean obsolete = false;
        Map<String, List<String>> parents = new LinkedHashMap<>();
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event != XMLStreamConstants.START_ELEMENT) {
                continue;
            }
            if (is(xml, RDFS, "label")) {
                String lang = xml.getAttributeValue(XML, "lang");
                boolean preferred = lang == null || lang.isEmpty() || "en".equalsIgnoreCase(lang);
                String text = xml.getElementText().trim();
                if (label == null || (preferred && !labelPreferred)) {
                    label = text;
                    labelPreferred = preferred;
                }
            } else if (is(xml, OWL, "deprecated")) {
                obsolete = "true".equals(xml.getElementText().trim());
            } else if (is(xml, RDF, "type")) {
                isClass |= (OWL + "Class").equals(resolve(xml.getAttributeValue(RDF, "resource"), base));
                skip(xml);
            } else if (is(xml, RDFS, "subClassOf")) {
                readSubClassOf(xml, base, parents);
            } else {
                skip(xml);
            }
        }
        if (isClass && iri != null) {
            sink.accept(new TermRecord(oboId(iri), label, obsolete, parents));
        }
    }

    /**
     * Reads one {@code rdfs:subClassOf}: a named class by {@code rdf:resource},
     * or a restriction child over a hierarchical property with a named
     * {@code someValuesFrom}. Consumes through the element's end tag.
     */
    private void readSubClassOf(XMLStreamReader xml, String base, Map<String, List<String>> parents)
        throws XMLStreamException {
        String named = resolve(xml.getAttributeValue(RDF, "resource"), base);
        if (named != null) {
            parents.computeIfAbsent("is_a", k -> new ArrayList<>()).add(oboId(named));
            skip(xml);
            return;
        }
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                return;
            }
            if (event != XMLStreamConstants.START_ELEMENT) {
                continue;
            }
            if (is(xml, OWL, "Restriction")) {
                String parent = partOfParent(xml, base);
                if (parent != null) {
                    parents.computeIfAbsent("part_of", k -> new ArrayList<>()).add(oboId(parent));
                }
            } else {
                skip(xml);
            }
        }
    }

    /** Reads an {@code owl:Restriction}; returns its part-of parent or null. */
    private String partOfParent(XMLStreamReader xml, String base) throws XMLStreamException {
        String property = null;
        String filler = null;
        while (xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                break;
            }
            if (event != XMLStreamConstants.START_ELEMENT) {
                continue;
            }
            if (is(xml, OWL, "onProperty")) {
                property = resolve(xml.getAttributeValue(RDF, "resource"), base);
            } else if (is(xml, OWL, "someValuesFrom")) {
                filler = resolve(xml.getAttributeValue(RDF, "resource"), base);
            }
            skip(xml);
        }
        return property != null && HIERARCHICAL_PROPERTIES.contains(property) ? filler : null;
    }

    /** Consumes the rest of the current element, children included. */
    private static void skip(XMLStreamReader xml) throws XMLStreamException {
        int depth = 1;
        while (depth > 0 && xml.hasNext()) {
            int event = xml.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }
}

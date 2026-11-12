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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Reads the terms of an OBO vocabulary release, one {@link TermRecord} per
 * {@code [Term]} stanza.
 *
 * <p>
 * Of each term it reads {@code id}, {@code name}, {@code is_obsolete},
 * {@code is_a} and those {@code relationship} lines whose relation is in
 * {@link #HIERARCHICAL}; of the header it reads {@code data-version}. Other
 * stanzas, such as {@code [Typedef]}, are skipped, as are trailing
 * {@code {...}} qualifiers after a parent id. The {@code !} comment of a
 * parent line is read only as the name of that parent, for a parent without
 * a stanza of its own; other comments are skipped.</p>
 */
public final class OboReader {

    /**
     * The relationship names counted as hierarchical parents, besides
     * {@code is_a}: the relations OLS counts as hierarchical parents. The term
     * index walks every relation it was written with, so this set alone decides
     * which relations an ancestor walk follows. {@code BFO:0000050} is
     * {@code part_of} by its id, as ROBOT writes it in OBO files such as
     * {@code cl.obo} (parity with OLS: {@code cl/CL_0000182}).
     */
    static final Set<String> HIERARCHICAL = Set.of("part_of", "BFO:0000050");

    private static final String IS_A = "is_a";
    private static final Pattern TAG_VALUE = Pattern.compile("[A-Za-z][A-Za-z0-9_-]*:.*");
    private static final int QUOTED_LINE_LENGTH = 60;

    /** Creates a reader; it holds no state between reads. */
    public OboReader() {
    }

    /**
     * Reads a release.
     *
     * @param in the OBO text, UTF-8; not closed
     * @param sink receives each term, in file order
     * @param dataVersion receives the header's {@code data-version}, if it has
     * one
     * @throws IOException if the stream cannot be read, or is not an OBO file
     * (for example an HTML page or an OWL document)
     */
    public void read(InputStream in, Consumer<TermRecord> sink, Consumer<String> dataVersion)
        throws IOException {
        read(in, sink, dataVersion, (id, name) -> {
        });
    }

    /**
     * Reads a release, and the names its parent lines give their parents.
     *
     * @param in the OBO text, UTF-8; not closed
     * @param sink receives each term, in file order
     * @param dataVersion receives the header's {@code data-version}, if it has
     * one
     * @param parentNames receives, for each {@code is_a} and hierarchical
     * {@code relationship} line of a term with a {@code ! name} comment, the
     * parent id and that name, in file order: the name a parent without a
     * stanza of its own gets as its label in the index (Ruling R-AA)
     * @throws IOException if the stream cannot be read, or is not an OBO file
     * (for example an HTML page or an OWL document)
     */
    public void read(InputStream in, Consumer<TermRecord> sink, Consumer<String> dataVersion,
        BiConsumer<String, String> parentNames) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        boolean inHeader = true;
        boolean formatVersionSeen = false;
        boolean firstLine = true;
        Term term = null;
        String line;
        while ((line = reader.readLine()) != null) {
            String text = (firstLine ? stripByteOrderMark(line) : line).strip();
            if (text.isEmpty() || text.startsWith("!")) {
                continue;
            }
            if (firstLine && !TAG_VALUE.matcher(text).matches() && !text.startsWith("[")) {
                throw new IOException("not an OBO file: it starts with \"" + quote(text) + "\"");
            }
            firstLine = false;
            if (text.startsWith("[")) {
                if (inHeader && !formatVersionSeen) {
                    throw new IOException("not an OBO file: the header has no format-version");
                }
                inHeader = false;
                emit(term, sink);
                term = text.equals("[Term]") ? new Term(parentNames) : null;
                continue;
            }
            int colon = text.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String tag = text.substring(0, colon);
            String value = text.substring(colon + 1);
            if (inHeader) {
                if (tag.equals("format-version")) {
                    formatVersionSeen = true;
                } else if (tag.equals("data-version")) {
                    dataVersion.accept(unescape(value));
                }
            } else if (term != null) {
                term.read(tag, value);
            }
        }
        if (firstLine) {
            throw new IOException("not an OBO file: it is empty");
        }
        if (inHeader && !formatVersionSeen) {
            throw new IOException("not an OBO file: the header has no format-version");
        }
        emit(term, sink);
    }

    private static void emit(Term term, Consumer<TermRecord> sink) {
        if (term != null && term.id != null && !term.id.isEmpty()) {
            sink.accept(new TermRecord(term.id, term.label, term.obsolete, term.parents));
        }
    }

    private static String stripByteOrderMark(String line) {
        return line.startsWith("﻿") ? line.substring(1) : line;
    }

    private static String quote(String text) {
        return text.length() <= QUOTED_LINE_LENGTH ? text : text.substring(0, QUOTED_LINE_LENGTH) + "...";
    }

    /** The first word of a value: an id without its comment or qualifiers. */
    private static String firstToken(String value) {
        String v = value.strip();
        int end = 0;
        while (end < v.length() && !Character.isWhitespace(v.charAt(end)) && v.charAt(end) != '!') {
            end++;
        }
        return v.substring(0, end);
    }

    /** The text after a value's unescaped {@code !}, with OBO escapes resolved; empty if there is none. */
    private static String comment(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '!') {
                return unescape(value.substring(i + 1), false);
            }
        }
        return "";
    }

    /** A value up to its unescaped {@code !} comment, with OBO escapes resolved. */
    private static String unescape(String value) {
        return unescape(value, true);
    }

    /** A value with OBO escapes resolved, up to its unescaped {@code !} if {@code toComment}. */
    private static String unescape(String value, boolean toComment) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                out.append(switch (next) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case 'W' -> ' ';
                    default -> next;
                });
            } else if (c == '!' && toComment) {
                break;
            } else {
                out.append(c);
            }
        }
        return out.toString().strip();
    }

    /** The tags of one {@code [Term]} stanza read so far. */
    private static final class Term {

        private String id;
        private String label = "";
        private boolean obsolete;
        private final Map<String, List<String>> parents = new LinkedHashMap<>();
        private final BiConsumer<String, String> parentNames;

        private Term(BiConsumer<String, String> parentNames) {
            this.parentNames = parentNames;
        }

        private void read(String tag, String value) {
            switch (tag) {
                case "id" -> id = firstToken(value);
                case "name" -> label = unescape(value);
                case "is_obsolete" -> obsolete = unescape(value).equals("true");
                case IS_A -> addParent(IS_A, firstToken(value), value);
                case "relationship" -> {
                    String[] words = unescape(value).split("\\s+");
                    if (words.length >= 2 && HIERARCHICAL.contains(words[0])) {
                        addParent(words[0], words[1], value);
                    }
                }
                default -> {
                    // other tags are not needed for the ancestor walk
                }
            }
        }

        private void addParent(String relation, String parent, String value) {
            if (!parent.isEmpty()) {
                parents.computeIfAbsent(relation, r -> new ArrayList<>()).add(parent);
                String name = comment(value);
                if (!name.isEmpty()) {
                    parentNames.accept(parent, name);
                }
            }
        }
    }
}

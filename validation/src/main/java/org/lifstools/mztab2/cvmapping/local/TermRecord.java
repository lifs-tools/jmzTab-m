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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * One term of a vocabulary release, with its hierarchical parents.
 *
 * @param id the obo id, e.g. {@code MS:1000073}
 * @param label the term's name; empty if the release gives none
 * @param obsolete whether the release marks the term obsolete
 * @param parentsByRelation the parent obo ids per relation: {@code is_a} and
 * the hierarchical relationship names the reader counts (e.g. {@code part_of});
 * relations without parents are absent
 */
public record TermRecord(String id, String label, boolean obsolete,
    Map<String, List<String>> parentsByRelation) {

    /**
     * Copies the parents into an unmodifiable map that keeps the relation order.
     * A {@code null} label is read as empty.
     */
    public TermRecord {
        Objects.requireNonNull(id, "id");
        label = label == null ? "" : label;
        Map<String, List<String>> copy = new LinkedHashMap<>();
        parentsByRelation.forEach((relation, parents) -> copy.put(relation, List.copyOf(parents)));
        parentsByRelation = Collections.unmodifiableMap(copy);
    }
}

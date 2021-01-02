package de.isas.mztab2.model;

import de.isas.mztab2.model.IndexedElement;
import java.util.Objects;

/*
 * Copyright 2020 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
/**
 * Implementation of IndexedElement where more information is required than just
 * the id. Wraps the actual object and exposes it via {@link #getPayload()}.
 *
 * @author nilshoffmann
 */
public class IndexedElementImpl implements IndexedElement {

    private final Integer id;
    private final String elementType;
    private final Object payload;

    /**
     * Create a new indexed element implementation for the provided id, element
     * type and payload.
     *
     * @param id the id.
     * @param elementType the element type string.
     * @param payload the payload, any indexed domain object.
     */
    public IndexedElementImpl(Integer id, String elementType, Object payload) {
        this.id = id;
        this.elementType = elementType;
        this.payload = payload;
    }

    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Returns the element type string.
     * This is used by MetadataElement in the <pre>jmztabm-io</pre> module.
     *
     * @return the element type string.
     */
    public String getElementType() {
        return elementType;
    }

    /**
     * Returns the wrapped object.
     *
     * @return the payload.
     */
    public Object getPayload() {
        return payload;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexedElement other = (IndexedElement) o;
        return Objects.equals(this.id, other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IndexedElement {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

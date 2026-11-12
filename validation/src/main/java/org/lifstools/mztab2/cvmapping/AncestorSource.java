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
package org.lifstools.mztab2.cvmapping;

import java.util.List;
import org.lifstools.mztab2.model.Parameter;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Where a {@link CvParameterLookupService} gets the ancestors of a term from.
 */
public interface AncestorSource {

    /**
     * Returns all transitive ancestors of a term.
     *
     * @param term the term to start from, with cvLabel and cvAccession
     * @return all ancestors, each with cvLabel, cvAccession and name
     * @throws HttpClientErrorException with status 404 if the term is unknown
     */
    List<Parameter> ancestors(Parameter term) throws HttpClientErrorException;

    /**
     * Returns all transitive descendants of a term. Optional operation.
     *
     * @param term the term to start from
     * @return all descendants
     * @throws UnsupportedOperationException if the source does not support it
     */
    default List<Parameter> descendants(Parameter term) {
        throw new UnsupportedOperationException();
    }
}

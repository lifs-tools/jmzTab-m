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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import lombok.Value;
import uk.ac.ebi.pride.jmztab2.model.MetadataElement;

/**
 * A reference in the metadata section to an element that was not defined
 * when the reference was read, e.g. an ms_run[1]-instrument_ref above the
 * lines of that instrument. It is resolved once the metadata section has been
 * read.
 *
 * @author nilshoffmann
 */
@Value
public class ForwardReference {

    /** The line of the reference. */
    int lineNumber;
    /** The referring key, e.g. ms_run[1]-instrument_ref. */
    String defineLabel;
    /** The referenced element as written, e.g. instrument[2]. */
    String valueLabel;
    /** The element type of the referring element: ms_run or assay. */
    MetadataElement referrer;
    /** The id of the referring element. */
    Integer referrerId;
    /** The element type of the referenced element: instrument, sample or ms_run. */
    MetadataElement target;
    /** The id of the referenced element. */
    Integer targetId;
}

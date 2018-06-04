/* 
 * Copyright 2018 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package uk.ac.ebi.pride.jmztab2.model;

import de.isas.mztab2.io.serialization.Serializers;
import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.IndexedElement;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.StudyVariable;

/**
 * An kind of {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn} which use CV parameter accessions in following the format:
 * opt_{OBJECT_ID}_cv_{accession}_{parameter name}. Spaces within the parameter' s name MUST be replaced by '_'.
 *
 * @author qingwei
 * @since 30/05/13
 * 
 */
public class ParameterOptionColumn extends OptionColumn {
    /** Constant <code>CV="cv_"</code> */
    public static final String CV = "cv_";

    private final Parameter param;

    /**
     * Define a {@link uk.ac.ebi.pride.jmztab2.model.OptionColumn} which use CV parameter accessions in following the format:
     * opt_{OBJECT_ID}_cv_{accession}_{parameter name}. Spaces within the parameter' s name MUST be replaced by '_'.
     *
     * @param element SHOULD not be null.
     * @param param SHOULD not be null.
     * @param columnType SHOULD not be null.
     * @param offset SHOULD be non-negative integer.
     */
    public ParameterOptionColumn(IndexedElement element, Parameter param, Class columnType, int offset) {
        super(element, CV + param.getCvAccession() + "_" + param.getName().replaceAll(" ", "_"), columnType, offset);
        this.param = param;
    }

    /**
     * get column header like: opt_{OBJECT_ID}_cv_{accession}_{parameter name}
     * Spaces within the parameter's name MUST be replaced by '_'.
     *
     * @param element {@link Assay}, {@link StudyVariable}, {@link MsRun} or "global" (if the value relates to all replicates).
     * @param param SHOULD NOT be null.
     * @return the string representation of this column's header.
     */
    public static String getHeader(IndexedElement element, Parameter param) {
        StringBuilder sb = new StringBuilder();

        sb.append(OPT).append("_").append(element == null ? GLOBAL : Serializers.getReference(element, element.getId()));
        sb.append("_").append(CV).append(param.getCvAccession()).append("_").append(param.getName().replaceAll(" ", "_"));

        return sb.toString();
    }

}

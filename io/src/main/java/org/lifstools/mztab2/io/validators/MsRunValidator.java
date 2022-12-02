/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.io.validators;

import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that the ms run section is present in metadata.
 *
 * @author nilshoffmann
 */
public class MsRunValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        SortedMap<Integer, MsRun> runMap = parserContext.getMsRunMap();
        List<MZTabError> errorList = new LinkedList<>();
        for (Integer id : runMap.keySet()) {
            if (runMap.get(id).
                    getLocation() == null) {
                errorList.add(new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.msRun + "[" + id + "]-" + MsRun.Properties.location));
            }
            List<Parameter> scanPolarity = runMap.get(id).
                    getScanPolarity();
            if (scanPolarity == null || scanPolarity.isEmpty()) {
                errorList.add(new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.msRun + "[" + id + "]-" + MsRun.Properties.scanPolarity));
            }

        }
        return errorList;
    }

}

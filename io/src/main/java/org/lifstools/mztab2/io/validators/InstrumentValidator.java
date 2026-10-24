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
package org.lifstools.mztab2.io.validators;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that every instrument defined in the metadata is referenced by
 * at least one ms_run[1-n]-instrument_ref.
 */
public class InstrumentValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        SortedMap<Integer, Instrument> instrumentMap = parserContext.getInstrumentMap();
        Set<Integer> referenced = parserContext.getMsRunMap().values().stream().
                filter(Objects::nonNull).
                map(MsRun::getInstrumentRef).
                filter(Objects::nonNull).
                map(Instrument::getId).
                collect(Collectors.toSet());
        List<MZTabError> errorList = new LinkedList<>();
        instrumentMap.forEach((id, instrument) -> {
            if (!referenced.contains(id)) {
                String element = Metadata.JSON_PROPERTY_INSTRUMENT + "[" + id + "]";
                errorList.add(new MZTabError(
                        LogicalErrorType.InstrumentNotReferenced, -1, element).
                        location(instrument != null && instrument.getName() != null
                                ? Locations.metadataKey(element + "-" + Instrument.JSON_PROPERTY_NAME)
                                : Locations.metadataGroup(Metadata.JSON_PROPERTY_INSTRUMENT)));
            }
        });
        return errorList;
    }

}

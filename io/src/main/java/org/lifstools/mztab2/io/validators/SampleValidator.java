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
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.Sample;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that every sample defined in the metadata is referenced by at
 * least one assay[1-n]-sample_ref.
 */
public class SampleValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        SortedMap<Integer, Sample> sampleMap = parserContext.getSampleMap();
        Set<Integer> referenced = parserContext.getAssayMap().values().stream().
                filter(Objects::nonNull).
                map(Assay::getSampleRef).
                filter(Objects::nonNull).
                map(Sample::getId).
                collect(Collectors.toSet());
        List<MZTabError> errorList = new LinkedList<>();
        sampleMap.forEach((id, sample) -> {
            if (!referenced.contains(id)) {
                String element = Metadata.JSON_PROPERTY_SAMPLE + "[" + id + "]";
                errorList.add(new MZTabError(
                        LogicalErrorType.SampleNotReferenced, -1, element).
                        location(sample != null && sample.getName() != null
                                ? Locations.metadataKey(element)
                                : Locations.metadataGroup(Metadata.JSON_PROPERTY_SAMPLE)));
            }
        });
        return errorList;
    }

}

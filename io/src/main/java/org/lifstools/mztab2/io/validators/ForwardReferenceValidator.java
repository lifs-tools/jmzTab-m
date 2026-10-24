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
import java.util.Map;
import org.lifstools.mztab2.model.Metadata;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.ForwardReference;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Reports the references that were read before the element they refer to
 * (Error-2068), and those whose element is not defined at all (Error-2008).
 * The parser has already resolved the former, see
 * {@link MZTabParserContext#resolveForwardReferences(Metadata)}.
 */
public class ForwardReferenceValidator implements RefiningValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        List<MZTabError> errorList = new LinkedList<>();
        for (ForwardReference reference : parserContext.getForwardReferences()) {
            if (targets(parserContext, reference).containsKey(reference.getTargetId())) {
                errorList.add(new MZTabError(
                        LogicalErrorType.ReferenceBeforeDefinition,
                        reference.getLineNumber(), reference.getDefineLabel(),
                        reference.getValueLabel()).
                        location(Locations.metadataKey(reference.getDefineLabel())));
            } else {
                errorList.add(new MZTabError(
                        LogicalErrorType.NotDefineInMetadata,
                        reference.getLineNumber(), reference.getValueLabel()).
                        location(Locations.metadataKey(reference.getDefineLabel())));
            }
        }
        return errorList;
    }

    private static Map<Integer, ?> targets(MZTabParserContext context,
            ForwardReference reference) {
        switch (reference.getTarget()) {
            case INSTRUMENT:
                return context.getInstrumentMap();
            case SAMPLE:
                return context.getSampleMap();
            case MS_RUN:
                return context.getMsRunMap();
            default:
                throw new IllegalArgumentException(
                        "Unsupported reference target: " + reference.getTarget());
        }
    }

}

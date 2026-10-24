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
package org.lifstools.mztab2.validation;

import org.lifstools.mztab2.validation.Validator;
import org.lifstools.mztab2.cvmapping.JxPathElement;
import org.lifstools.mztab2.cvmapping.SetOperations;
import org.lifstools.mztab2.io.serialization.ParameterConverter;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.ValidationMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.pride.jmztab2.utils.errors.CrossCheckErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * Validates that controlled vocabularies used by parameters are defined in the
 * metadata CV section.
 *
 * @author nilshoffmann
 */
@Slf4j
@lombok.Builder()
public class CvDefinitionValidationHandler implements Validator<MzTab> {

    @Override
    public List<ValidationMessage> validate(MzTab mzTab) {
        JXPathContext context = JXPathContext.newContext(mzTab);
        return checkCvDefinitions(mzTab, context);
    }

    private List<ValidationMessage> checkCvDefinitions(MzTab mzTabFile,
        JXPathContext context) {
        Map<String, CV> cvTerms = mzTabFile.getMetadata().
            getCv().
            stream().
            collect(Collectors.toMap((key) ->
            {
                return key.getLabel();
            }, (value) ->
            {
                return value;
            }));
        List<ValidationMessage> messages = new ArrayList<>();
        List<Pair<Pointer, Parameter>> parameters = JxPathElement.
            toList(context, "//*[cvLabel!='']",
                Parameter.class);
        log.debug("Selected {} cv parameters!", parameters.size());
        Set<String> definedCvLabels = new HashSet<>(cvTerms.keySet());
        Set<String> usedCvLabels = new HashSet<>();
        definedOnce(parameters).stream().
            forEach((t) ->
            {
                Parameter param = t.getValue();
                log.debug("Checking parameter {}", new ParameterConverter().
                    convert(param));
                if (param.getCvLabel() != null && !param.getCvLabel().
                    isEmpty()) {
                    usedCvLabels.add(param.getCvLabel());
                    if (!cvTerms.containsKey(param.getCvLabel())) {
                        log.debug(
                            "Parameter {} uses undefined controlled vocabulary: {}",
                            new ParameterConverter().
                                convert(param), param.getCvLabel());
                        MZTabError error = new MZTabError(
                            CrossCheckErrorType.CvUndefinedInMetadata, -1,
                            param.getCvLabel(),
                            new ParameterConverter().convert(param)).
                            location(ModelLocations.ofPointer(t.getKey()));
                        messages.add(error.toValidationMessage());
                    }
                }
            });
        Set<String> unusedCvLabels = SetOperations.complement(definedCvLabels,
            usedCvLabels);
        unusedCvLabels.stream().
            forEach((cvLabel) ->
            {
                log.debug(
                    "Cv with label {} is not being used by any parameters!",
                    cvLabel);
                MZTabError error = new MZTabError(
                    CrossCheckErrorType.CvUnused, -1,
                    cvLabel);
                Integer id = cvTerms.get(cvLabel).
                    getId();
                if (id != null) {
                    error.location(Locations.metadataKey(
                        "cv[" + id + "]-label"));
                }
                messages.add(error.toValidationMessage());
            });
        return messages;
    }

    /**
     * Prefers the path with the fewest references (msRunRef, instrumentRef,
     * spectraRef, assayRefs, ...), then the shortest path.
     */
    private static final Comparator<Pair<Pointer, Parameter>> DEFINITION_FIRST = Comparator.
        comparingLong((Pair<Pointer, Parameter> p) -> referenceHops(p.getKey().
            asPath())).
        thenComparingInt(p -> p.getKey().
            asPath().
            length());

    /**
     * The selection reaches a parameter once per path to it, e.g. an
     * instrument's name also through every ms_run, assay, study variable and
     * evidence row that refers to that instrument. Keeps each parameter object
     * once, at the path where it is defined, in selection order.
     */
    static List<Pair<Pointer, Parameter>> definedOnce(
        List<Pair<Pointer, Parameter>> selection) {
        List<Pair<Pointer, Parameter>> unique = new ArrayList<>();
        Map<Parameter, Integer> index = new IdentityHashMap<>();
        for (Pair<Pointer, Parameter> p : selection) {
            Integer i = index.putIfAbsent(p.getValue(), unique.size());
            if (i == null) {
                unique.add(p);
            } else if (DEFINITION_FIRST.compare(p, unique.get(i)) < 0) {
                unique.set(i, p);
            }
        }
        return unique;
    }

    private static long referenceHops(String path) {
        return Arrays.stream(path.split("/")).
            map(step -> step.replaceAll("\\[\\d+\\]$", "")).
            filter(step -> step.endsWith("Ref") || step.endsWith("Refs")).
            count();
    }
}

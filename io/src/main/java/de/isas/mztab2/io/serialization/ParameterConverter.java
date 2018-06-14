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
package de.isas.mztab2.io.serialization;

import com.fasterxml.jackson.databind.util.StdConverter;
import de.isas.mztab2.model.Parameter;
import java.util.ArrayList;
import java.util.List;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NULL;

/**
 *
 * @author nilshoffmann
 */
public class ParameterConverter extends StdConverter<Parameter, String> {

    @Override
    public String convert(Parameter in) {
//        Serializers.checkIndexedElement(in);
        return toString(in);
    }

    /**
     * In case, the name of the param contains commas, quotes MUST be added to
     * avoid problems with the parsing: [label, accession, "first part of the
     * param name , second part of the name", value].
     *
     * For example: [MOD, MOD:00648, "N,O-diacetylated L-serine",]
     */
    private static void printReserveString(String name, StringBuilder sb) {
        List<String> charList = new ArrayList<String>();

        charList.add(",");

        boolean containReserveChar = false;
        for (String c : charList) {
            if (name.contains(c)) {
                containReserveChar = true;
                break;
            }
        }

        if (containReserveChar) {
            sb.append("\"").
                append(name).
                append("\"");
        } else {
            sb.append(name);
        }
    }

    /**
     * If there exists reserved characters in value, remove them all.
     */
    private String removeReservedChars(String value) {
        if (value != null) {
            value = value.trim();

            // define a reserved character list.
            List<String> reserveCharList = new ArrayList<>();

            reserveCharList.add(",");

            for (String c : reserveCharList) {
                value = value.replaceAll(c, "");
            }
        }

        return value;
    }

    /**
     * In case, the name of the param contains commas, quotes MUST be added to
     * avoid problems with the parsing: [label, accession, "first part of the
     * param name , second part of the name", value].
     *
     * For example: [MOD, MOD:00648, "N,O-diacetylated L-serine",]
     *
     * @param param a {@link de.isas.mztab2.model.Parameter} object.
     * @return a {@link java.lang.String} object.
     */
    private String toString(Parameter param) {
        if (param == null) {
            return NULL;
        }
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        if (param.getCvLabel() != null) {
            sb.append(param.getCvLabel());
        }
        sb.append(", ");

        if (param.getCvAccession() != null) {
            sb.append(param.getCvAccession());
        }
        sb.append(", ");

        printReserveString(param.getName(), sb);
        sb.append(", ");

        if (param.getValue() != null) {
            printReserveString(param.getValue(), sb);
        }

        sb.append("]");

        return sb.toString();
    }
}

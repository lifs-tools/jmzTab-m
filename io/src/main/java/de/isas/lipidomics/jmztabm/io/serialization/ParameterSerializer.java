/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
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
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.isas.mztab1_1.model.Parameter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class ParameterSerializer extends StdSerializer<Parameter> {

    public ParameterSerializer() {
        this(null);
    }

    public ParameterSerializer(Class<Parameter> t) {
        super(t);
    }
    
    @Override
    public void serialize(Parameter p, JsonGenerator jg, SerializerProvider sp) throws IOException {
        if (p != null) {
            addLine(p, jg);
        }
    }

    public void addLine(Parameter p, JsonGenerator jg) {
        try {
            //value
            jg.writeString(toString(p));
        } catch (IOException ex) {
            Logger.getLogger(MetadataSerializer.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }
        /**
     * In case, the name of the param contains commas, quotes MUST be added to avoid problems with the parsing:
     * [label, accession, "first part of the param name , second part of the name", value].
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
            sb.append("\"").append(name).append("\"");
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
            List<String> reserveCharList = new ArrayList<String>();

            reserveCharList.add(",");

            for (String c : reserveCharList) {
                value = value.replaceAll(c, "");
            }
        }

        return value;
    }

    /**
     * In case, the name of the param contains commas, quotes MUST be added to avoid problems with the parsing:
     * [label, accession, "first part of the param name , second part of the name", value].
     *
     * For example: [MOD, MOD:00648, "N,O-diacetylated L-serine",]
     */
    public static String toString(Parameter param) {
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

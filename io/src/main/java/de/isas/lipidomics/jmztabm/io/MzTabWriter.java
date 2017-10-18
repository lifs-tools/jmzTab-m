/*
 * 
 */
package de.isas.lipidomics.jmztabm.io;

import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.ParameterList;
import de.isas.mztab1_1.model.Parameter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabWriter {
    public void write(OutputStreamWriter os, MzTab mzTab) throws IOException {
        os.write(metadataString(mzTab.getMetadata()).toString());
    }

    public StringBuilder metadataString(Metadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append(metadata.getPrefix()).append("\t").append("mzTab-version").append("\t").append(metadata.getMzTabVersion()).append("\n\r");
        sb.append(metadata.getPrefix()).append("\t").append("mzTab-ID").append("\t").append(metadata.getMzTabID()).append("\n\r");
        sb.append(metadata.getPrefix()).append("\t").append("title").append("\t").append(metadata.getTitle()).append("\n\r");
        sb.append(metadata.getPrefix()).append("\t").append("description").append("\t").append(metadata.getDescription()).append("\n\r");
        return sb;
    }
//    
//    public StringBuilder sampleProcessing(Metadata metadata) {
//        StringBuilder sampleProcessing = new StringBuilder();
//        metadata.getSampleProcessing();
//    }
//    
//    public static String parameterListToMzTabLines(String prefix, String elementName, ParameterList list) {
//        list.stream().map(parameter -> {
//            String[] param = new String[]{prefix, elementName, };
//            sb.append(prefix)
//        }).collect(Collectors.joining(", "));
//        
//    }
    
    public static String parameterToString(Parameter parameter) {
        String[] values = {parameter.getCvLabel(),parameter.getCvAccession(),parameter.getName(),parameter.getValue()};
        return Arrays.asList(values).stream().map(elem -> (elem==null)?"":elem.trim()).collect(Collectors.joining(", ","[","]"));
    }
    
    public static String mtdParameterToMzTabLine(int index, String name, Parameter parameter) {
        if(index <= 0) {
            throw new IllegalArgumentException("value for parameter index must be >= 0");
        }
        return Arrays.asList(new String[]{name+"["+index+"]", parameterToString(parameter)}).stream().map(e -> e).collect(Collectors.joining("\t","MTD\t","\n\r"));
    }
//    
//    public List<String> toStringList() {
//        
//    }
}

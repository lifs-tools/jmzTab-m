/*
 * 
 */
package de.isas.lipidomics.jmztabm.io;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.Parameter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabWriter {

    public final static String EOL = "\n\r";
    public final static String SEP = "\t";

    public void write(OutputStreamWriter os, MzTab mzTab) throws IOException {
        if (!os.getEncoding().
                equals("UTF-8")) {
            throw new IllegalArgumentException(
                    "OutputStreamWriter encoding must be UTF-8");
        }
        os.write(metadataString(mzTab.getMetadata()).
                toString());
    }

    public void write(Path path, MzTab mzTab) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName(
                "UTF-8"), StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
        writer.write(metadataString(mzTab.getMetadata()).
                toString());
    }

    public String metadataString(Metadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.
            append(toLine(metadata.getPrefix(), "mzTab-version", metadata.
                    getMzTabVersion())).
            append(toLine(metadata.getPrefix(), "mzTab-ID", metadata.
                    getMzTabID())).
            append(toLine(metadata.getPrefix(), "title", metadata.getTitle())).
            append(toLine(metadata.getPrefix(), "description", metadata.
                    getDescription()));
        return sb.toString();
    }

    public static String toLine(Enum<?> prefix, String... args) {
        return Arrays.asList(args).
                stream().
                collect(
                        Collectors.joining(SEP, prefix + "\t", EOL));
    }
    
    public static void getJsonPropertyFields(Class<?> c) {
        Field[] fields = c.getDeclaredFields();
        for(Field f:fields) {
            JsonProperty annotation = f.getAnnotation(JsonProperty.class);
            if(annotation != null) {
                String jsonPropertyFieldName = annotation.value();
                System.out.println(jsonPropertyFieldName);
            }
        }
    }
    
    public static Method getMethodForField(String fieldName) {
        return null;
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
        String[] values = {parameter.getCvLabel(), parameter.getCvAccession(),
            parameter.getName(), parameter.getValue()};
        return Arrays.asList(values).
                stream().
                map(elem ->
                        (elem == null) ? "" : elem.trim()).
                collect(Collectors.joining(
                        ", ", "[", "]"));
    }

    /*
    public static String mtdInstrumentToMzTabLine(int index, String name, Instrument instrument) {
    	
    }
     */
    public static String mtdParameterToMzTabLine(int index, String name,
            Parameter parameter) {
        if (index <= 0) {
            throw new IllegalArgumentException(
                    "value for parameter index must be >= 0");
        }
        return Arrays.asList(new String[]{name + "[" + index + "]",
            parameterToString(parameter)}).
                stream().
                map(e ->
                        e).
                collect(
                        Collectors.joining(SEP, "MTD\t", EOL));
    }
//    
//    public List<String> toStringList() {
//        
//    }
}

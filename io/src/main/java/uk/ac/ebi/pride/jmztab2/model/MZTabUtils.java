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

import de.isas.mztab2.model.IndexedElement;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.Publication;
import de.isas.mztab2.model.PublicationItem;
import de.isas.mztab2.model.SpectraRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.*;
import static uk.ac.ebi.pride.jmztab2.model.MZTabStringUtils.*;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Provide a couple of functions for translating, parsing and printing formatted strings
 * defined in the mzTab specification.
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 30/01/13
 *
 */
public class MZTabUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(
        MZTabUtils.class);

    /**
     * If ratios are included and the denominator is zero, the "INF" value MUST
     * be used. If the result leads to calculation errors (for example 0/0),
     * this MUST be reported as "not a number" ("NaN").
     *
     * @see #parseDouble(String)
     * @param value a {@link java.lang.Double} object.
     * @return a {@link java.lang.String} object.
     */
    public static String printDouble(Double value) {
        if (value == null) {
            return NULL;
        } else if (value.equals(Double.NaN)) {
            return CALCULATE_ERROR;
        } else if (value.equals(Double.POSITIVE_INFINITY)) {
            return INFINITY;
        } else {
            return value.toString();
        }
    }

    /**
     * Parse the target string, and check is obey the email format or not. If
     * not, return null.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String parseEmail(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        String regexp = REGEX_EMAIL;
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(target);
        
        return matcher.find() ? target : null;
    }

    /**
     * Parse the target string, and check it follows the mzTab Version format.
     * If not, return null.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String parseMzTabVersion(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        Pattern versionPattern = Pattern.compile(MZTabConstants.REGEX_MZTAB_M);
        Matcher m = versionPattern.matcher(target);
        if (m.matches()) {
            Integer major = Integer.parseInt(m.group("major"));
            Integer minor = Integer.parseInt(m.group("minor"));
            Integer micro = Integer.parseInt(m.group("micro"));
            if (major != 2) {
                return null;
            }
            if (!"M".equals(m.group("profile"))) {
                return null;
            }
            return target;
        }
        return null;
    }

    /**
     * Parameters are always reported as [CV label, accession, name, value]. Any
     * field that is not available MUST be left empty.
     *
     * If the name or value of param contains comma, quotes MUST be added to
     * avoid problems. Nested double quotes are not supported.
     *
     * Notice: name cell never set null.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link de.isas.mztab2.model.Parameter} object.
     */
    public static Parameter parseParam(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        try {
            target = target.substring(target.indexOf("[") + 1, target.
                lastIndexOf("]"));
            String[] tokens = target.split(REGEX_PARAM_SPLIT, -1);
            
            if (tokens.length == 4) {
                String cvLabel = tokens[0].trim();
                
                String accession = tokens[1].trim();
                
                String name = tokens[2].trim();
                if (name.contains("\"")) {  //We remove the escaping because it will be written back in the writer
                    name = removeDoubleQuotes(name);
                }
                
                if (isEmpty(name)) {
                    return null;
                }
                
                String value = tokens[3].trim();
                if (value.contains("\"")) {  //We remove the escaping because it will be written back in the writer
                    value = removeDoubleQuotes(value);
                }
                if (isEmpty(value)) {
                    value = null;
                }
                
                if (isEmpty(cvLabel) && isEmpty(accession)) {
                    return new Parameter().name(name).
                        value(value);
                } else {
                    return new Parameter().cvLabel(cvLabel).
                        cvAccession(accession).
                        name(name).
                        value(value);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        
        return null;
        
    }

    /**
     * Multiple identifiers MUST be separated by splitChar.
     *
     * @param splitChar a char.
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<String> parseStringList(char splitChar, String target) {
        List<String> list = new ArrayList<>(splitChar);
        
        target = parseString(target);
        if (target == null) {
            return list;
        }

        // regular express reserved keywords escape
        StringBuilder sb = new StringBuilder();
        switch (splitChar) {
            case '.':
            case '$':
            case '^':
            case '{':
            case '}':
            case '[':
            case ']':
            case '(':
            case ')':
            case '|':
            case '*':
            case '+':
            case '?':
            case '\\':
                sb.append("\\").
                    append(splitChar);
                break;
            default:
                sb.append(splitChar);
        }
        
        String[] items = target.split(sb.toString());
        Collections.addAll(list, items);
        
        return list.stream().
            map(value ->
                value.trim()).
            collect(Collectors.toList());
    }

    /**
     * parse the target into a {@link de.isas.mztab2.model.IndexedElement}
     * object.
     *
     * @param target a {@link java.lang.String} object.
     * @param element a {@link uk.ac.ebi.pride.jmztab2.model.MetadataElement}
     * object.
     * @return a {@link de.isas.mztab2.model.IndexedElement} object.
     */
    public static IndexedElement parseIndexedElement(String target,
        MetadataElement element) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        Pattern pattern = Pattern.compile(element + "\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(target);
        if (matcher.find()) {
            Integer id = new Integer(matcher.group(1));
            IndexedElement p = new IndexedElement().id(id);
            p.elementType(element.getName());
            return p;
        } else {
            return null;
        }
    }

    /**
     * Parse the target into a {@link de.isas.mztab2.model.IndexedElement} list.
     *
     * @param target a {@link java.lang.String} object.
     * @param element a {@link uk.ac.ebi.pride.jmztab2.model.MetadataElement}
     * object.
     * @return a {@link java.util.List} object.
     */
    public static List<IndexedElement> parseRefList(String target,
        MetadataElement element) {
        List<String> list = parseStringList(MZTabConstants.COMMA, target);
        
        List<IndexedElement> indexedElementList = new ArrayList<>();
        IndexedElement indexedElement;
        for (String item : list) {
            indexedElement = parseIndexedElement(item, element);
            if (indexedElement == null) {
                indexedElementList.clear();
                return indexedElementList;
            }
            indexedElementList.add(indexedElement);
        }
        return indexedElementList;
    }

    /**
     * A list of '|' separated parameters
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Parameter> parseParamList(String target) {
        List<String> list = parseStringList(BAR, target);
        
        Parameter param;
        SplitList<Parameter> paramList = new SplitList<>(BAR);
        for (String item : list) {
            param = parseParam(item);
            if (param == null) {
                paramList.clear();
                return paramList;
            } else {
                paramList.add(param);
            }
        }
        
        return paramList;
    }

    /**
     * A '|' delimited list of GO accessions
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<String> parseGOTermList(String target) {
        List<String> list = parseStringList(COMMA, target);
        
        List<String> goList = new SplitList<>(COMMA);
        for (String item : list) {
            item = parseString(item);
            if (item.startsWith("GO:")) {
                goList.add(item);
            } else {
                goList.clear();
                break;
            }
        }
        
        return goList;
    }

    /**
     * <p>
     * parseInteger.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    public static Integer parseInteger(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        Integer integer;
        
        try {
            integer = new Integer(target);
        } catch (NumberFormatException e) {
            integer = null;
        }
        
        return integer;
    }

    /**
     * NOTICE: If ratios are included and the denominator is zero, the "INF"
     * value MUST be used. If the result leads to calculation errors (for
     * example 0/0), this MUST be reported as "not a number" ("NaN").
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.Double} object.
     */
    public static Double parseDouble(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        Double value;
        try {
            value = new Double(target);
        } catch (NumberFormatException e) {
            switch (target) {
                case CALCULATE_ERROR:
                    value = Double.NaN;
                    break;
                case INFINITY:
                    value = Double.POSITIVE_INFINITY;
                    break;
                default:
                    value = null;
                    break;
            }
        }
        
        return value;
    }

    /**
     * <p>
     * parseLong.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.Long} object.
     */
    public static Long parseLong(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        try {
            return new Long(target);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * <p>
     * parseDoubleList.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Double> parseDoubleList(String target) {
        List<String> list = parseStringList(BAR, target);
        
        Double value;
        List<Double> valueList = new ArrayList<>(BAR);
        for (String item : list) {
            value = parseDouble(item);
            if (value == null) {
                valueList.clear();
                break;
            } else {
                valueList.add(value);
            }
        }
        
        return valueList;
    }

    /**
     * <p>
     * parseIntegerList.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Integer> parseIntegerList(String target) {
        List<String> list = parseStringList(BAR, target);
        
        Integer value;
        List<Integer> valueList = new ArrayList<>(BAR);
        for (String item : list) {
            value = parseInteger(item);
            if (value == null) {
                valueList.clear();
                break;
            } else {
                valueList.add(value);
            }
        }
        
        return valueList;
    }

    /**
     * <p>
     * parseURI.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.net.URI} object.
     */
    public static URI parseURI(String target) {
        target = parseString(target);
        if (target == null) {
            return null;
        }
        
        URI uri;
        
        try {
            uri = new URI(target);
        } catch (URISyntaxException e) {
            uri = null;
        }
        
        return uri;
    }

    /**
     * A publication on this unit. PubMed ids must be prefixed by "pubmed:",
     * DOIs by "doi:". Multiple identifiers MUST be separated by "|".
     *
     * @param publication a {@link de.isas.mztab2.model.Publication} object.
     * @param lineNumber the line number while parsing.
     * @param target a {@link java.lang.String} object.
     * @return a {@link de.isas.mztab2.model.Publication} object.
     * @throws uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException in case of
     * parsing or formatting issues of the publication string.
     */
    public static Publication parsePublicationItems(Publication publication,
        int lineNumber, String target) throws MZTabException {
        List<String> list = parseStringList(BAR, target);
        
        PublicationItem.TypeEnum type;
        String accession;
        PublicationItem item;
        for (String pub : list) {
            pub = parseString(pub).
                toLowerCase();
            if (pub == null) {
                publication.getPublicationItems().
                    clear();
                return publication;
            }
            String[] items = pub.split("" + COLON);
            if (items.length == 2) {
                type = PublicationItem.TypeEnum.fromValue(items[0]);
                if (type == null) {
                    throw new MZTabException(new MZTabError(
                        FormatErrorType.Publication, lineNumber, target, pub));
                }
                accession = items[1].trim();
                item = new PublicationItem().type(type).
                    accession(accession);
                publication.addPublicationItemsItem(item);
            } else {
                throw new MZTabException(new MZTabError(
                    FormatErrorType.Publication, lineNumber, target, pub));
            }
            
        }
        
        return publication;
    }

    /**
     * Parse a {@link de.isas.mztab2.model.SpectraRef} list.
     *
     * @param context a
     * {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param metadata a {@link de.isas.mztab2.model.Metadata} object.
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<SpectraRef> parseSpectraRefList(
        MZTabParserContext context, Metadata metadata, String target) {
        List<String> list = parseStringList(BAR, target);
        List<SpectraRef> refList = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("ms_run\\[(\\d+)\\]:(.*)");
        Matcher matcher;
        Integer ms_file_id;
        String reference;
        SpectraRef ref;
        for (String item : list) {
            matcher = pattern.matcher(item.trim());
            if (matcher.find()) {
                ms_file_id = new Integer(matcher.group(1));
                reference = matcher.group(2);
                
                MsRun msRun = context.getMsRunMap().
                    get(ms_file_id);
                if (msRun == null) {
                    ref = null;
                } else {
                    ref = new SpectraRef().msRun(msRun).
                        reference(reference);
                }
                
                if (ref == null) {
                    refList.clear();
                    break;
                } else {
                    refList.add(ref);
                }
            }
        }
        
        return refList;
    }

    /**
     * Solve the conflict about minus char between modification position and
     * CHEMMOD charge. For example: 13-CHEMMOD:-159
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateMinusToUnicode(String target) {
        Pattern pattern = Pattern.compile("(CHEMMOD:.*)(-)(.*)");
        Matcher matcher = pattern.matcher(target);
        StringBuilder sb = new StringBuilder();
        if (matcher.find()) {
            sb.append(matcher.group(1));
            sb.append("&minus;");
            sb.append(matcher.group(3));
            
        } else {
            sb.append(target);
        }
        return sb.toString();
    }

    /**
     * <p>
     * translateMinusInCVtoUnicode.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateMinusInCVtoUnicode(String target) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
        Matcher matcher = pattern.matcher(target);
        
        StringBuilder sb = new StringBuilder();
        
        int start = 0;
        int end;
        while (matcher.find()) {
            end = matcher.start(1);
            sb.append(target.substring(start, end));
            sb.append(matcher.group(1).
                replaceAll("-", "&minus;"));
            start = matcher.end(1);
        }
        sb.append(target.substring(start, target.length()));
        
        return sb.toString();
    }

    /**
     * <p>
     * translateUnicodeCVTermMinus.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateUnicodeCVTermMinus(String target) {
        return target.replaceAll("&minus;", "-");
    }

    /**
     * Solve the conflict about minus char between modification position and
     * CHEMMOD charge. For example: 13-CHEMMOD:-159
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateUnicodeToMinus(String target) {
        Pattern pattern = Pattern.compile("(.*CHEMMOD:.*)(&minus;)(.*)");
        Matcher matcher = pattern.matcher(target);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            
            sb.append(matcher.group(1));
            sb.append("-");
            sb.append(matcher.group(3));
            
            return sb.toString();
        } else {
            return target;
        }
    }

    /**
     * locate param label [label, accession, name, value], translate ',' to '\t'
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateCommaToTab(String target) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
        Matcher matcher = pattern.matcher(target);
        
        StringBuilder sb = new StringBuilder();
        
        int start = 0;
        int end;
        while (matcher.find()) {
            end = matcher.start(1);
            sb.append(target.substring(start, end));
            sb.append(matcher.group(1).
                replaceAll(",", "\t"));
            start = matcher.end(1);
        }
        sb.append(target.substring(start, target.length()));
        
        return sb.toString();
    }

    /**
     * solve the conflict about comma char which used in split modification and
     * split cv param components.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateTabToComma(String target) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
        Matcher matcher = pattern.matcher(target);
        
        StringBuilder sb = new StringBuilder();
        
        int start = 0;
        int end;
        while (matcher.find()) {
            end = matcher.start(1);
            sb.append(target.substring(start, end));
            sb.append(matcher.group(1).
                replaceAll("\t", ","));
            start = matcher.end(1);
        }
        sb.append(target.substring(start, target.length()));
        
        return sb.toString();
    }

    //Solve the problem for Neutral losses in CvTerm format
    /**
     * <p>
     * translateMinusToTab.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateMinusToTab(String target) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
        Matcher matcher = pattern.matcher(target);
        
        StringBuilder sb = new StringBuilder();
        
        int start = 0;
        int end;
        while (matcher.find()) {
            end = matcher.start(1);
            sb.append(target.substring(start, end));
            sb.append(matcher.group(1).
                replaceAll("-", "\t"));
            start = matcher.end(1);
        }
        sb.append(target.substring(start, target.length()));
        
        return sb.toString();
        
    }
    
    private static String replaceLast(String string, String toReplace,
        String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos)
                + replacement
                + string.substring(pos + toReplace.length(), string.length());
        }
        return string;
    }

    /**
     * <p>
     * translateLastToTab.</p>
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateLastToTab(String target) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
        Matcher matcher = pattern.matcher(target);
        
        StringBuilder sb = new StringBuilder();
        
        int start = 0;
        int end;
        while (matcher.find()) {
            end = matcher.start(1);
            sb.append(target.substring(start, end));
            sb.append(replaceLast(matcher.group(1), "-", "\t"));
            start = matcher.end(1);
        }
        sb.append(target.substring(start, target.length()));
        
        return sb.toString();
        
    }

    /**
     * solve the conflict about comma char which used in split modification and
     * split cv param components.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String translateTabToMinus(String target) {
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
        Matcher matcher = pattern.matcher(target);
        
        StringBuilder sb = new StringBuilder();
        
        int start = 0;
        int end;
        while (matcher.find()) {
            end = matcher.start(1);
            sb.append(target.substring(start, end));
            sb.append(matcher.group(1).
                replaceAll("\t", "-"));
            start = matcher.end(1);
        }
        sb.append(target.substring(start, target.length()));
        
        return sb.toString();
    }

    /**
     * If there exists reserved characters in value, like comma, the string need
     * to be escape. However the escaping char is not store because it will be
     * write back in the writer. Nested double quotes are not supported.
     *
     * @param value a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String removeDoubleQuotes(String value) {
        
        if (value != null) {
            int length;
            int count;
            
            value = value.trim();
            length = value.length();
            
            value = value.replace("\"", "");
            count = length - value.length();
            
            if (isEmpty(value)) {
                value = null;
            }
            
            if (count > 2) {
                LOGGER.warn(
                    "Nested double quotes in value, " + count + " occurrences have been replaced.");
            }
        }
        
        return value;
    }
    
}

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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import de.isas.mztab2.io.serialization.ParameterConverter;
import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.MZBoolean;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.SplitList;
import uk.ac.ebi.pride.jmztab2.model.MZTabUtils;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.SpectraRef;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.*;
import static uk.ac.ebi.pride.jmztab2.model.MZTabUtils.*;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;

/**
 * This class allows the validation and loading of the data into mzTab domain
 * objects.
 *
 * NOTICE: {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory} maintain a
 * couple of {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn} which have
 * internal logical position and order. In physical mzTab file, we allow user
 * not obey this logical position organized way, and provide their date with own
 * order. In order to distinguish them, we use physical position (a positive
 * integer) to record the column location in mzTab file. And use
 * {@link uk.ac.ebi.pride.jmztab2.utils.parser.PositionMapping} structure to
 * maintain the mapping between them.
 *
 * @param <T> the type of domain object the parser creates.
 * @see SMLLineParser
 * @see SMFLineParser
 * @see SMELineParser
 * @author qingwei
 * @since 14/02/13
 *
 */
@Slf4j
public abstract class MZTabDataLineParser<T> extends MZTabLineParser {

    protected MZTabColumnFactory factory;
    protected PositionMapping positionMapping;
    protected SortedMap<String, Integer> exchangeMapping; // reverse the key and value of positionMapping.

    protected SortedMap<Integer, IMZTabColumn> mapping;   // logical position --> offset
    protected Metadata metadata;

    /**
     * <p>
     * Constructor for MZTabDataLineParser.</p>
     *
     * @param context a
     * {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     */
    protected MZTabDataLineParser(MZTabParserContext context) {
        super(context);
    }

    /**
     * Generate a mzTab data line parser.
     *
     * NOTICE: {@link uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory} maintain
     * a couple of {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn} which have
     * internal logical position and order. In physical mzTab file, we allow
     * user not obey this logical position organized way, and provide their date
     * with own order. In order to distinguish them, we use physical position (a
     * positive integer) to record the column location in mzTab file. And use
     * {@link uk.ac.ebi.pride.jmztab2.utils.parser.PositionMapping} structure
     * the maintain the mapping between them.
     *
     * @param context the parser context, keeping dynamic state and lookup
     * associations.
     * @param factory SHOULD NOT be set to null
     * @param positionMapping SHOULD NOT be set to null
     * @param metadata SHOULD NOT be set to null
     * @param errorList a
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList} object.
     */
    protected MZTabDataLineParser(MZTabParserContext context,
        MZTabColumnFactory factory, PositionMapping positionMapping,
        Metadata metadata, MZTabErrorList errorList) {
        this(context);
        if (factory == null) {
            throw new NullPointerException(
                "Column header factory should be created first.");
        }
        this.factory = factory;

        this.positionMapping = positionMapping;
        this.exchangeMapping = positionMapping.reverse();
        this.mapping = factory.getOffsetColumnsMap();

        if (metadata == null) {
            throw new NullPointerException("Metadata should be parsed first.");
        }
        this.metadata = metadata;
        this.errorList = errorList == null ? new MZTabErrorList() : errorList;
    }

    /**
     * {@inheritDoc}
     *
     * Validate and parse the data line, if there exist errors, add them into
     * {@link MZTabErrorList}.
     */
    @Override
    public void parse(int lineNumber, String line, MZTabErrorList errorList) throws MZTabException {
        super.parse(lineNumber, line, errorList);
        checkCount();

        int offset = checkData();
        if (offset != items.length) {
            log.error(
                "Number of expected items after parsing header is: {} but data line has: {} items!",
                offset,
                items.length);
            log.error("Current mapping is: {}", mapping);
            log.error("Items given: {} expected: {}", Arrays.toString(items),
                Arrays.toString(line.split("\\t")));
            this.errorList.add(new MZTabError(FormatErrorType.CountMatch,
                lineNumber, "" + offset, "" + items.length));
        }
    }

    /**
     * Check header line items size equals data line items size. The number of
     * Data line items does not match with the number of Header line items.
     * Normally, the user has not used the Unicode Horizontal Tab character
     * (Unicode codepoint 0009) as the column delimiter, there is a file
     * encoding error, or the user has not provided the definition of optional
     * columns in the header line.
     */
    private void checkCount() {
        int headerCount = mapping.size();
        int dataCount = items.length - 1;

        if (headerCount != dataCount) {
            log.error(
                "Number of expected items after parsing header is: {} but data line has: {} items!",
                headerCount,
                dataCount);
            log.error("Current mapping is: {}", mapping);
            log.error("Items given: {} expected: {}", Arrays.toString(items),
                Arrays.toString(line.split("\\t")));
            this.errorList.add(new MZTabError(FormatErrorType.CountMatch,
                lineNumber, "" + dataCount, "" + headerCount));
        }
    }

    /**
     * Retrieve the data line to a type mzTab domain object.
     *
     * @return a typed mzTab domain object.
     */
    public abstract T getRecord();

    /**
     * Check and translate the columns into mzTab elements.
     *
     * @return a int.
     */
    protected abstract int checkData();

    /**
     * load best_search_engine_score[id], read id value.
     *
     * @param bestSearchEngineScoreLabel a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer loadBestSearchEngineScoreId(
        String bestSearchEngineScoreLabel) {
        Pattern pattern = Pattern.compile(
            "search_engine_score\\[(\\d+)\\](\\w+)?");
        Matcher matcher = pattern.matcher(bestSearchEngineScoreLabel);

        if (matcher.find()) {
            return new Integer(matcher.group(1));
        }

        return null;
    }

    /**
     * load search_engine_score[id]_ms_run[..], read id value.
     *
     * @param searchEngineLabel a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer loadSearchEngineScoreId(String searchEngineLabel) {
        Pattern pattern = Pattern.compile("search_engine_score\\[(\\d+)\\]\\w*");
        Matcher matcher = pattern.matcher(searchEngineLabel);

        if (matcher.find()) {
            return new Integer(matcher.group(1));
        }

        return null;
    }

    /**
     * In the table-based sections (protein, peptide, and small molecule) there
     * MUST NOT be any empty cells. Some field not allow "null" value, for
     * example unit_id, accession and so on. In "Complete" file, in general
     * "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @param allowNull a boolean.
     * @return a {@link java.lang.String} object.
     */
    protected String checkData(IMZTabColumn column, String target,
        boolean allowNull) {
        if (target == null && allowNull) {
            return null;
        }
        if (target == null) {
            this.errorList.add(new MZTabError(LogicalErrorType.NULL, lineNumber,
                column.getHeader()));
            return null;
        }

        target = target.trim();
        if (target.isEmpty()) {
            this.errorList.add(new MZTabError(LogicalErrorType.NULL, lineNumber,
                column.getHeader()));
            return null;
        }
        if (MZTabConstants.NULL.equals(target) && !allowNull) {
            this.errorList.add(new MZTabError(LogicalErrorType.NULL, lineNumber,
                column.getHeader()));
            return null;
        }

        return target;
    }

    /**
     * In the table-based sections (protein, peptide, and small molecule) there
     * MUST NOT be any empty cells. Some field not allow "null" value, for
     * example unit_id, accession and so on. In "Complete" file, in general
     * "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkString(IMZTabColumn column, String target) {
        return checkData(column, target, true);
    }

    /**
     * In the table-based sections (protein, peptide, and small molecule) there
     * MUST NOT be any empty cells. Some field not allow "null" value, for
     * example unit_id, accession and so on. In "Complete" file, in general
     * "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @param allowNull if true, null target values will pass the check, if
     * false, the check will raise an error in the error list.
     * @return a {@link java.lang.String} object.
     */
    protected String checkString(IMZTabColumn column, String target,
        boolean allowNull) {
        return checkData(column, target, allowNull);
    }

    /**
     * Check and translate target string into Integer. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkInteger(IMZTabColumn column, String target) {
        return checkInteger(column, target, true);
    }

    /**
     * Check and translate target string into Integer. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @param allowNull if true, null target values will pass the check, if
     * false, the check will raise an error in the error list.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkInteger(IMZTabColumn column, String target,
        boolean allowNull) {
        String result = checkData(column, target, allowNull);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return null;
        }

        Integer value = parseInteger(result);
        if (value == null) {
            this.errorList.add(new MZTabError(FormatErrorType.Integer,
                lineNumber, column.getHeader(), target));
        }

        return value;
    }

    /**
     * Check and translate target string into Double. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Double}
     * error.
     *
     * NOTICE: If ratios are included and the denominator is zero, the "INF"
     * value MUST be used. If the result leads to calculation errors (for
     * example 0/0), this MUST be reported as "not a number" ("NaN").
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkDouble(IMZTabColumn column, String target) {
        return checkDouble(column, target, true);
    }

    /**
     * Check and translate target string into Double. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Double}
     * error.
     *
     * NOTICE: If ratios are included and the denominator is zero, the "INF"
     * value MUST be used. If the result leads to calculation errors (for
     * example 0/0), this MUST be reported as "not a number" ("NaN").
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @param allowNull if true, null target values will pass the check, if
     * false, the check will raise an error in the error list.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkDouble(IMZTabColumn column, String target,
        boolean allowNull) {
        String result = checkData(column, target, allowNull);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return null;
        }

        Double value = parseDouble(result);
        if (value == null) {
            this.errorList.add(
                new MZTabError(FormatErrorType.Double, lineNumber, column.
                    getHeader(), target));
            return null;
        }
        if (value.equals(Double.NaN) || value.equals(Double.POSITIVE_INFINITY)) {
            return value;
        }

        return value;
    }

    /**
     * Check and translate target string into parameter list which split by '|'
     * character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#ParamList}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<Parameter> checkParamList(IMZTabColumn column, String target) {
        String result = checkData(column, target, true);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return new ArrayList<>(BAR);
        }

        List<Parameter> paramList = parseParamList(result);
        if (paramList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.ParamList,
                lineNumber, "Column " + column.getHeader(), target));
        }
        for (Parameter param : paramList) {
            if (param != null && param.getCvAccession() != null && !param.
                getCvAccession().
                isEmpty()) {
                if (!param.getCvAccession().
                    contains(":")) {
                    this.errorList.add(new MZTabError(
                        FormatErrorType.ParamAccessionNotNamespaced, lineNumber,
                        column.getHeader(), param.getCvAccession(),
                        new ParameterConverter().convert(param)));
                }
            }
        }

        return paramList;
    }

    /**
     * <p>
     * checkParameter.</p>
     *
     * @param column a {@link uk.ac.ebi.pride.jmztab2.model.IMZTabColumn}
     * object.
     * @param target a {@link java.lang.String} object.
     * @param allowNull a boolean.
     * @return a {@link de.isas.mztab2.model.Parameter} object.
     */
    protected Parameter checkParameter(IMZTabColumn column, String target,
        boolean allowNull) {
        String result = checkData(column, target, true);
        if (result == null || (result.equalsIgnoreCase(NULL) && !allowNull)) {
            this.errorList.add(new MZTabError(FormatErrorType.Param, lineNumber,
                "Column " + column.getHeader(), target));
        }
        Parameter param = MZTabUtils.parseParam(target);
        if (param != null && param.getCvAccession() != null && !param.
            getCvAccession().
            isEmpty()) {
            if (!param.getCvAccession().
                contains(":")) {
                this.errorList.add(new MZTabError(
                    FormatErrorType.ParamAccessionNotNamespaced, lineNumber,
                    column.getHeader(), param.getCvAccession(),
                    new ParameterConverter().convert(param)));
            }
        }
        return param;
    }

    /**
     * Check and translate target string into parameter list which split by
     * splitChar character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @param splitChar a char.
     * @return a {@link java.util.List} object.
     */
    protected List<String> checkStringList(IMZTabColumn column, String target,
        char splitChar) {
        String result = checkData(column, target, true);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return new ArrayList<>(splitChar);
        }

        List<String> stringList = parseStringList(splitChar, result);
        if (stringList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.StringList,
                lineNumber, column.getHeader(), result, "" + splitChar));
        }

        return stringList;
    }

    /**
     * Check and translate target string into integer list which split by
     * splitChar character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @param splitChar a char.
     * @return a {@link java.util.List} object.
     */
    protected List<Integer> checkIntegerList(IMZTabColumn column, String target,
        char splitChar) {
        return checkIntegerList(column, target, splitChar, true);
    }

    /**
     * Check and translate target string into integer list which split by
     * splitChar character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @param splitChar a char.
     * @param allowNull if true, null will be treated as a valid element of the
     * list. Otherwise, an error will be added to the error list.
     * @return a {@link java.util.List} object.
     */
    protected List<Integer> checkIntegerList(IMZTabColumn column, String target,
        char splitChar, boolean allowNull) {
        String result = checkData(column, target, allowNull);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return new ArrayList<>(splitChar);
        }

        List<Integer> stringList = parseIntegerList(result);
        if (stringList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.IntegerList,
                lineNumber, column.getHeader(), result, "" + splitChar));
        }

        return stringList;
    }

    /**
     * Check and translate target string into parameter list which split by
     * splitChar character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<Double> checkDoubleList(IMZTabColumn column, String target) {
        String result = checkData(column, target, true);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return new ArrayList<>(MZTabConstants.BAR);
        }

        List<Double> doubleList = parseDoubleList(target);
        if (doubleList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.DoubleList,
                lineNumber, column.getHeader(), result, "" + MZTabConstants.BAR));
        }

        return doubleList;
    }

    /**
     * Check and translate target to
     * {@link uk.ac.ebi.pride.jmztab2.model.MZBoolean}. Only "0" and "1" allow
     * used in express Boolean (0/1). If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#MZBoolean}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param target SHOULD NOT be empty.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.MZBoolean} object.
     */
    protected MZBoolean checkMZBoolean(IMZTabColumn column, String target) {
        String result = checkData(column, target, true);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return null;
        }

        MZBoolean value = MZBoolean.findBoolean(result);
        if (value == null) {
            this.errorList.add(new MZTabError(FormatErrorType.MZBoolean,
                lineNumber, column.getHeader(), result));
        }

        return value;
    }

    /**
     * Check target string. Normally, description can set "null". But in
     * "Complete" file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param description SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkDescription(IMZTabColumn column, String description) {
        return checkData(column, description, true);
    }

    /**
     * Check and translate taxid string into Integer. If exists error during
     * parse, raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error. Normally, taxid may be set to "null"; in general
     * "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param taxid SHOULD NOT be empty.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkTaxid(IMZTabColumn column, String taxid) {
        return checkInteger(column, taxid);
    }

    /**
     * Check target string. Normally, species can set "null". But in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param species SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkSpecies(IMZTabColumn column, String species) {
        return checkData(column, species, true);
    }

    /**
     * Check target string. Normally, database can set "null". But in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param database SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkDatabase(IMZTabColumn column, String database) {
        return checkData(column, database, true);
    }

    /**
     * Check target string. Normally, databaseVersion can set "null". But in
     * "Complete" file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param databaseVersion SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkDatabaseVersion(IMZTabColumn column,
        String databaseVersion) {
        return checkData(column, databaseVersion, true);
    }

    /**
     * Check and translate searchEngine string into parameter list which split
     * by '|' character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#ParamList}
     * error. Normally, searchEngine may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param searchEngine SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<Parameter> checkSearchEngine(IMZTabColumn column,
        String searchEngine) {
        return checkParamList(column, searchEngine);
    }

    /**
     * The best search engine score (for this type of score) for the given
     * peptide across all replicates reported. The type of score MUST be defined
     * in the metadata section. If the peptide was not identified by the
     * specified search engine, “null” MUST be reported.
     *
     * @param column SHOULD NOT be set to null
     * @param bestSearchEngineScore SHOULD NOT be empty.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkBestSearchEngineScore(IMZTabColumn column,
        String bestSearchEngineScore) {
        return checkDouble(column, bestSearchEngineScore);
    }

    /**
     * The search engine score for the given peptide in the defined ms run. The
     * type of score MUST be defined in the metadata section. If the peptide was
     * not identified by the specified search engine “null” must be reported.
     *
     * @param column SHOULD NOT be set to null
     * @param searchEngineScore SHOULD NOT be empty.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkSearchEngineScore(IMZTabColumn column,
        String searchEngineScore) {
        return checkDouble(column, searchEngineScore);
    }

    /**
     * Check and translate numPSMs string into Integer. If exists error during
     * parse, raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error. Normally, numPSMs may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param numPSMs SHOULD NOT be empty.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkNumPSMs(IMZTabColumn column, String numPSMs) {
        return checkInteger(column, numPSMs);
    }

    /**
     * Check and translate numPeptidesDistinct string into Integer. If exists
     * error during parse, raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error. Normally, numPeptidesDistinct can set "null", but in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param numPeptidesDistinct SHOULD NOT be empty.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkNumPeptidesDistinct(IMZTabColumn column,
        String numPeptidesDistinct) {
        return checkInteger(column, numPeptidesDistinct);
    }

    /**
     * Check and translate numPeptidesUnique string into Integer. If exists
     * error during parse, raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error. Normally, numPeptidesUnique can set "null", but in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param numPeptidesUnique SHOULD NOT be empty.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkNumPeptidesUnique(IMZTabColumn column,
        String numPeptidesUnique) {
        return checkInteger(column, numPeptidesUnique);
    }

    /**
     * Check and translate target string into parameter list which split by ','
     * character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error. Normally, ambiguityMembers may be set to "null";
     * in general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param ambiguityMembers SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<String> checkAmbiguityMembers(IMZTabColumn column,
        String ambiguityMembers) {
        return checkStringList(column, ambiguityMembers, COMMA);
    }

    /**
     * Checks the provided URI string.
     *
     * @param column SHOULD NOT be set to null
     * @param uri a {@link java.lang.String} object, conforming to URI format.
     * @return the uri as an ASCII encoded string.
     */
    protected String checkURI(IMZTabColumn column, String uri) {
        String result_uri = checkData(column, uri, true);

        if (result_uri == null || result_uri.equalsIgnoreCase(NULL)) {
            return null;
        }

        java.net.URI result = parseURI(result_uri);
        if (result == null) {
            this.errorList.add(new MZTabError(FormatErrorType.URI, lineNumber,
                "Column " + column.getHeader(), result_uri));
            return null;
        } else {
            return result.toASCIIString();
        }
    }

    /**
     * Check and translate spectraRef string into
     * {@link de.isas.mztab2.model.SpectraRef} list. If parse incorrect, or
     * ms_run not defined in metadata raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#SpectraRef}
     * error. Normally, spectraRef may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param spectraRef SHOULD NOT be empty.
     * @param context a
     * {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @return a {@link java.util.List} object.
     */
    protected List<SpectraRef> checkSpectraRef(MZTabParserContext context,
        IMZTabColumn column, String spectraRef) {
        return checkSpectraRef(context, column, spectraRef, false);
    }

    /**
     * Check and translate spectraRef string into
     * {@link de.isas.mztab2.model.SpectraRef} list. If parse incorrect, or
     * ms_run not defined in metadata raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#SpectraRef}
     * error. Normally, spectraRef may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param spectraRef SHOULD NOT be empty.
     * @param context a
     * {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
     * @param allowNull if true, allow null for value. Otherwise, an error will
     * be added to the error list.
     * @return a {@link java.util.List} object.
     */
    protected List<SpectraRef> checkSpectraRef(MZTabParserContext context,
        IMZTabColumn column, String spectraRef, boolean allowNull) {
        String result_spectraRef = checkData(column, spectraRef, allowNull);

        if (result_spectraRef == null || result_spectraRef.
            equalsIgnoreCase(NULL)) {
            return new SplitList<>(BAR);
        }

        List<SpectraRef> refList = parseSpectraRefList(context, metadata,
            result_spectraRef);
        if (refList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.SpectraRef,
                lineNumber, column.getHeader(), result_spectraRef));
        } else {
            for (SpectraRef ref : refList) {
                MsRun run = ref.getMsRun();
                if (run.getLocation() == null) {
                    //As the location can be null and the field is mandatory, this is not an error, it is a warning
                    this.errorList.add(new MZTabError(
                        LogicalErrorType.SpectraRef, lineNumber, column.
                            getHeader(), result_spectraRef, "ms_run[" + run.
                            getId() + "]-location"));
                }
            }
        }

        return refList;
    }

    /**
     * Check target string. Normally, pre can set "null". "null" values should only be given, if no value is available and where the specification allows for "null" explicitly."
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param pre SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkPre(IMZTabColumn column, String pre) {
        return checkData(column, pre, true);
    }

    /**
     * Check target string. Normally, post can set "null". But in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param post SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkPost(IMZTabColumn column, String post) {
        return checkData(column, post, true);
    }

    /**
     * Check target string. Normally, start can set "null". But in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param start SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkStart(IMZTabColumn column, String start) {
        return checkData(column, start, true);
    }

    /**
     * Check target string. Normally, end can set "null". But in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param end SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkEnd(IMZTabColumn column, String end) {
        return checkData(column, end, true);
    }

    /**
     * Check and translate target string into string list which split by ','
     * character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error. Besides, each item in list should be start with "GO:", otherwise
     * system raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#GOTermList}
     * error. Normally, go_terms may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param go_terms SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<String> checkGOTerms(IMZTabColumn column, String go_terms) {
        String result_go_terms = checkData(column, go_terms, true);

        if (result_go_terms == null || result_go_terms.equalsIgnoreCase(NULL)) {
            return new ArrayList<>(COMMA);
        }

        List<String> stringList = parseGOTermList(result_go_terms);
        if (stringList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.GOTermList,
                lineNumber, column.getHeader(), result_go_terms));
        }

        return stringList;
    }

    /**
     * Check and translate protein_coverage string into Double. If parse
     * is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Double}
     * error. protein_coverage range should be in the [0, 1), otherwise raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType#ProteinCoverage}
     * error.
     *
     * NOTICE: If ratios are included and the denominator is zero, the "INF"
     * value MUST be used. If the result leads to calculation errors (for
     * example 0/0), this MUST be reported as "not a number" ("NaN").
     *
     * @param column SHOULD NOT be set to null
     * @param protein_coverage SHOULD NOT be empty.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkProteinCoverage(IMZTabColumn column,
        String protein_coverage) {
        Double result = checkDouble(column, protein_coverage);

        if (result == null) {
            return null;
        }

        if (result < 0 || result > 1) {
            this.errorList.add(new MZTabError(LogicalErrorType.ProteinCoverage,
                lineNumber, column.getHeader(), printDouble(result)));
            return null;
        }

        return result;
    }

    /**
     * Check and translate peptide sequence. 'O' and 'U' are encoded by codons
     * that are usually interpreted as stop codons, which can not displayed in
     * the sequence. So, if find it, system raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Sequence}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param sequence SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkSequence(IMZTabColumn column, String sequence) {
        String result = checkData(column, sequence, true);

        if (result == null) {
            return null;
        }

        result = result.toUpperCase();

        Pattern pattern = Pattern.compile("[OU]");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find()) {
            this.errorList.add(new MZTabError(FormatErrorType.Sequence,
                lineNumber, column.getHeader(), sequence));
        }

        return result;
    }

    /**
     * Check and translate psm_id string into Integer. If exists error during
     * parse, raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error. Normally, psm_id may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param psm_id SHOULD NOT be empty.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkPSMID(IMZTabColumn column, String psm_id) {
        return checkInteger(column, psm_id);
    }

    /**
     * Check and translate unique to
     * {@link uk.ac.ebi.pride.jmztab2.model.MZBoolean}. Only "0" and "1" allow
     * used in express Boolean (0/1). If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#MZBoolean}
     * error.
     *
     * @param column SHOULD NOT be set to null
     * @param unique SHOULD NOT be empty.
     * @return a {@link uk.ac.ebi.pride.jmztab2.model.MZBoolean} object.
     */
    protected MZBoolean checkUnique(IMZTabColumn column, String unique) {
        return checkMZBoolean(column, unique);
    }

    /**
     * Check and translate charge string into Integer. If exists error during
     * parse, raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Integer}
     * error. Normally, charge may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param charge SHOULD NOT be empty.
     * @return a {@link java.lang.Integer} object.
     */
    protected Integer checkCharge(IMZTabColumn column, String charge) {
        return checkInteger(column, charge);
    }

    /**
     * Check and translate mass_to_charge string into Double. If parse
     * is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Double}
     * error.
     *
     * NOTICE: If ratios are included and the denominator is zero, the "INF"
     * value MUST be used. If the result leads to calculation errors (for
     * example 0/0), this MUST be reported as "not a number" ("NaN").
     *
     * @param column SHOULD NOT be set to null
     * @param mass_to_charge SHOULD NOT be empty.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkMassToCharge(IMZTabColumn column,
        String mass_to_charge) {
        return checkDouble(column, mass_to_charge);
    }

    /**
     * Check and translate exp_mass_to_charge string into Double. If parse
     * is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Double}
     * error.
     *
     * NOTICE: If ratios are included and the denominator is zero, the "INF"
     * value MUST be used. If the result leads to calculation errors (for
     * example 0/0), this MUST be reported as "not a number" ("NaN").
     *
     * @param column SHOULD NOT be set to null
     * @param exp_mass_to_charge SHOULD NOT be empty.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkExpMassToCharge(IMZTabColumn column,
        String exp_mass_to_charge) {
        return checkDouble(column, exp_mass_to_charge);
    }

    /**
     * Check and translate calc_mass_to_charge string into Double. If parse
     * is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#Double}
     * error.
     *
     * NOTICE: If ratios are included and the denominator is zero, the "INF"
     * value MUST be used. If the result leads to calculation errors (for
     * example 0/0), this MUST be reported as "not a number" ("NaN").
     *
     * @param column SHOULD NOT be set to null
     * @param calc_mass_to_charge SHOULD NOT be empty.
     * @return a {@link java.lang.Double} object.
     */
    protected Double checkCalcMassToCharge(IMZTabColumn column,
        String calc_mass_to_charge) {
        return checkDouble(column, calc_mass_to_charge);
    }

    /**
     * Check and translate identifier string into string list which split by '|'
     * character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error. Normally, identifier may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param identifier SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<String> checkIdentifier(IMZTabColumn column,
        String identifier) {
        return checkStringList(column, identifier, BAR);
    }

    /**
     * Check chemical_formula string. Normally, chemical_formula can set "null".
     * But in "Complete" file, in general "null" values SHOULD not be given.
     *
     * @see #checkData(IMZTabColumn, String, boolean)
     * @param column SHOULD NOT be set to null
     * @param chemical_formula SHOULD NOT be empty.
     * @return a {@link java.lang.String} object.
     */
    protected String checkChemicalFormula(IMZTabColumn column,
        String chemical_formula) {
        return checkData(column, chemical_formula, true);
    }

    /**
     * Check and translate smiles string into parameter list which split by '|'
     * character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error. Normally, smiles may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param smiles SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<String> checkSmiles(IMZTabColumn column, String smiles) {
        return checkStringList(column, smiles, BAR);
    }

    /**
     * Check and translate inchi_key string into parameter list which split by
     * '|' character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#StringList}
     * error. Normally, inchi_key may be set to "null"; in
     * general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param inchi_key SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<String> checkInchiKey(IMZTabColumn column, String inchi_key) {
        return checkStringList(column, inchi_key, BAR);
    }

    /**
     * Check and translate retention_time string into Double list which split by
     * '|' character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#DoubleList}
     * error. Normally, retention_time may be set to "null";
     * in general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param retention_time SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<Double> checkRetentionTime(IMZTabColumn column,
        String retention_time) {
        String result = checkData(column, retention_time, true);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return new SplitList<>(BAR);
        }

        List<Double> valueList = parseDoubleList(result);
        if (valueList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.DoubleList,
                lineNumber, column.getHeader(), result, "" + BAR));
        }

        return valueList;
    }

    /**
     * Check and translate retention_time_window string into Double list which
     * split by '|' character.. If parse is incorrect, throws
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType#DoubleList}
     * error. Normally, retention_time_window can set "null", but in "Complete"
     * file, in general "null" values SHOULD not be given.
     *
     * @param column SHOULD NOT be set to null
     * @param retention_time_window SHOULD NOT be empty.
     * @return a {@link java.util.List} object.
     */
    protected List<Double> checkRetentionTimeWindow(IMZTabColumn column,
        String retention_time_window) {
        String result = checkData(column, retention_time_window, true);

        if (result == null || result.equalsIgnoreCase(NULL)) {
            return new SplitList<>(BAR);
        }

        List<Double> valueList = parseDoubleList(result);
        if (valueList.isEmpty()) {
            this.errorList.add(new MZTabError(FormatErrorType.DoubleList,
                lineNumber, column.getHeader(), result, "" + BAR));
        }

        return valueList;
    }
}

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
package org.lifstools.mztab2.io;

import org.lifstools.mztab2.model.ColumnParameterMapping;
import org.lifstools.mztab2.model.Comment;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.MzTabAccess;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;
import org.lifstools.mztab2.io.validators.MzTabProfileValidator;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.REGEX_DEFAULT_RELIABILITY;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB;
import uk.ac.ebi.pride.jmztab2.model.MZTabStringUtils;
import uk.ac.ebi.pride.jmztab2.model.Section;
import static uk.ac.ebi.pride.jmztab2.utils.MZTabProperties.*;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.Locations;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab2.utils.parser.COMLineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.MTDLineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;
import uk.ac.ebi.pride.jmztab2.utils.parser.PositionMapping;
import uk.ac.ebi.pride.jmztab2.utils.parser.SEHLineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.SFHLineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.SMELineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.SMFLineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.SMHLineParser;
import uk.ac.ebi.pride.jmztab2.utils.parser.SMLLineParser;

/**
 *
 * MZTabFileParser provides reading functionality of the mzTab file. During the
 * parsing process, minimal integrity checks are preformed.
 *
 * @author qingwei
 * @author nilshoffmann
 * 
 * @since 21/02/13
 *
 */
public class MzTabFileParser {

    private MzTab mzTabFile;
    private URI tabFile;

    private MZTabErrorList errorList;

    /**
     * MS run identifier (MRI) as defined by the HUPO-PSI USI specification:
     * mzspec:&lt;collection&gt;:&lt;msRun&gt;, where msRun may contain a path and
     * a format suffix.
     */
    private static final Pattern MS_RUN_IDENTIFIER = Pattern.compile("^mzspec:[^:]+:.+$");
    private MZTabParserContext context;
    // where each parsed line belongs, for the locations of its errors
    private LineIndex lineIndex;

    /**
     * Create a new {@code MZTabFileParser} for the given file.
     *
     * @param tabFile the MZTab file. The file SHOULD not be null and MUST exist
     * @throws java.lang.IllegalArgumentException if the provided argument in
     * invalid.
     */
    public MzTabFileParser(File tabFile) throws IllegalArgumentException {
        this(tabFile.toURI());
    }

    /**
     * Create a new {@code MZTabFileParser} for the given file URI.
     *
     * @param tabFileUri the MZTab file URI. The file SHOULD not be null and
     * MUST exist {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList}
     * return by
     * {@link org.lifstools.mztab2.io.MzTabFileParser#getErrorList()}
     * @throws java.lang.IllegalArgumentException if the provided argument in
     * invalid.
     */
    public MzTabFileParser(URI tabFileUri) throws IllegalArgumentException {
        if (tabFileUri == null) {
            throw new IllegalArgumentException(
                "MZTab file uri must not be null!");
        }
        if (("file".equals(tabFileUri.getScheme()) && !new File(tabFileUri).
            exists())) {
            throw new IllegalArgumentException("MZTab File URI " + tabFileUri.
                toASCIIString() + " does not exist!");
        }

        this.tabFile = tabFileUri;
    }

    /**
     * Create a new {@code MZTabParserContext} and {@code MZTabErrorList} for
     * the given file URI. Parsing output and errors are written to the provided
     * {@link java.io.OutputStream}.
     *
     * @param out the output stream for parsing messages
     * @param level the minimum error level to report errors for
     * @param maxErrorCount the maximum number of errors to report in the
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList} return by
     * {@link org.lifstools.mztab2.io.MzTabFileParser#getErrorList()}
     * @return the error list
     * @throws java.io.IOException if any io related errors occur.
     */
    public MZTabErrorList parse(OutputStream out, MZTabErrorType.Level level,
        int maxErrorCount) throws IOException {
        try {
            context = new MZTabParserContext();
            errorList = new MZTabErrorList(level, maxErrorCount);
            lineIndex = new LineIndex();
            check();
            refine();
        } catch (MZTabException e) {
            out.write(e.getMessage().getBytes());
            try (PrintStream ps = new PrintStream(out)) {
                e.printStackTrace(ps);
            }
            errorList.add(e.getError());
        } catch (MZTabErrorOverflowException e) {
            try (PrintStream ps = new PrintStream(out)) {
                e.printStackTrace(ps);
            }
            if(e.getMessage() != null) {
                out.write(e.getMessage().getBytes());
            } else {
                out.write(("Error Overflow exception did not contain a message!" + NEW_LINE).getBytes());
            }
        } finally {
            attachLineLocations();
        }

        errorList.print(out);
        if (mzTabFile != null && errorList.isEmpty()) {
            out.write(
                ("No structural or logical errors in " + tabFile + " file!" + NEW_LINE).
                    getBytes());
        }
        return errorList;
    }

    /**
     * Create a new {@code MZTabParserContext} and {@code MZTabErrorList} for
     * the given file URI. Parsing output and errors are written to the provided
     * {@link java.io.OutputStream}. Reports up to
     * {@link uk.ac.ebi.pride.jmztab2.utils.MZTabProperties#MAX_ERROR_COUNT}
     * errors.
     *
     * @param out the output stream for parsing messages
     * @param level the minimum error level to report errors for
     * @return the error list
     * @throws java.io.IOException if any io related errors occur.
     */
    public MZTabErrorList parse(OutputStream out, MZTabErrorType.Level level) throws IOException {
        return parse(out, level, MAX_ERROR_COUNT);
    }

    /**
     * Create a new {@code MZTabParserContext} and {@code MZTabErrorList} for
     * the given file URI. Parsing output and errors are written to the provided
     * {@link java.io.OutputStream}. Reports up to
     * {@link uk.ac.ebi.pride.jmztab2.utils.MZTabProperties#MAX_ERROR_COUNT}
     * errors on level
     * {@link uk.ac.ebi.pride.jmztab2.utils.MZTabProperties#LEVEL}.
     *
     * @param out the output stream for parsing messages
     * @return the error list
     * @throws java.io.IOException if any io related errors occur.
     */
    public MZTabErrorList parse(OutputStream out) throws IOException {
        return parse(out, LEVEL, MAX_ERROR_COUNT);
    }

    /**
     * <p>
     * Getter for the field <code>errorList</code>.</p>
     *
     * @return a {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList}
     * object.
     */
    public MZTabErrorList getErrorList() {
        return errorList;
    }

    private Section getSection(String line) {
        String[] items = line.split("\\s*" + TAB + "\\s*");
        String section = items[0].trim();
        return Section.findSection(section);
    }

    /**
     * Records the location of a line: the section, plus the metadata key for
     * MTD lines and the 0-based row index within its table for SML, SMF and
     * SME lines. Lines outside the mzTab-M sections get section FILE.
     *
     * @param section the line's section, may be null
     */
    private void recordLine(int lineNumber, Section section, String line) {
        SectionEnum sectionEnum = section == null ? null : Locations.sectionOf(
            section.getPrefix());
        if (sectionEnum == null) {
            lineIndex.record(lineNumber, SectionEnum.FILE);
            return;
        }
        switch (sectionEnum) {
            case MTD:
                String[] items = line.split("\\s*" + TAB + "\\s*");
                if (items.length > 1 && !items[1].trim().isEmpty()) {
                    lineIndex.recordMetadataKey(lineNumber, items[1].trim());
                } else {
                    lineIndex.record(lineNumber, SectionEnum.MTD);
                }
                break;
            case SML:
            case SMF:
            case SME:
                lineIndex.recordRow(lineNumber, sectionEnum);
                break;
            default:
                lineIndex.record(lineNumber, sectionEnum);
        }
    }

    /**
     * Gives each error that names a parsed line and has no location yet a new
     * location for that line, with the error's column where the line is a
     * table header or data row.
     */
    private void attachLineLocations() {
        if (errorList == null || lineIndex == null) {
            return;
        }
        for (MZTabError error : errorList.getErrorList()) {
            if (error.getLocation() != null || error.getLineNumber() <= 0) {
                continue;
            }
            ValidationLocation location = lineIndex.locationOf(error.
                getLineNumber(), error.getColumn());
            if (location != null) {
                error.location(location);
            }
        }
    }

    /**
     * Compact per-line record of where each parsed line belongs: its section
     * and, for table rows, the row index, in arrays indexed by line number; the
     * key only for MTD lines. Locations are built on demand, only for lines
     * that have errors, so large tables do not cost an object per line.
     */
    private static final class LineIndex {

        private static final SectionEnum[] SECTIONS = SectionEnum.values();
        private static final int INITIAL_CAPACITY = 1024;

        // SectionEnum ordinal + 1 per line; 0 = line not recorded
        private byte[] sections = new byte[INITIAL_CAPACITY];
        // 0-based row index per SML/SMF/SME line
        private int[] rowIndices = new int[INITIAL_CAPACITY];
        private final Map<Integer, String> metadataKeys = new HashMap<>();
        private final int[] rowCounts = new int[SECTIONS.length];

        void record(int lineNumber, SectionEnum section) {
            ensureCapacity(lineNumber);
            sections[lineNumber] = (byte) (section.ordinal() + 1);
        }

        void recordRow(int lineNumber, SectionEnum section) {
            record(lineNumber, section);
            rowIndices[lineNumber] = rowCounts[section.ordinal()]++;
        }

        void recordMetadataKey(int lineNumber, String key) {
            record(lineNumber, SectionEnum.MTD);
            metadataKeys.put(lineNumber, key);
        }

        /**
         * @return a new location for the line, or null if the line was not
         * recorded
         */
        ValidationLocation locationOf(int lineNumber, String column) {
            if (lineNumber >= sections.length || sections[lineNumber] == 0) {
                return null;
            }
            SectionEnum section = SECTIONS[sections[lineNumber] - 1];
            switch (section) {
                case SMH:
                case SFH:
                case SEH:
                    return column == null ? Locations.section(section) : Locations.
                        headerColumn(section, column);
                case SML:
                case SMF:
                case SME:
                    return Locations.tableCell(section, rowIndices[lineNumber],
                        column);
                case MTD:
                    String key = metadataKeys.get(lineNumber);
                    return key == null ? Locations.section(section) : Locations.
                        metadataKey(key);
                default:
                    return Locations.section(section);
            }
        }

        private void ensureCapacity(int lineNumber) {
            if (lineNumber >= sections.length) {
                int capacity = Math.max(sections.length * 2, lineNumber + 1);
                sections = Arrays.copyOf(sections, capacity);
                rowIndices = Arrays.copyOf(rowIndices, capacity);
            }
        }
    }

    private BufferedReader readFile(URI tabFile) throws IOException {
        BufferedReader reader;

        InputStream is;
        File file = new File(tabFile);
        if (file.isFile()) {
            is = new FileInputStream(file);
        } else {
            URL tabFileUrl = tabFile.toURL();
            is = tabFileUrl.openStream();
        }
        if (tabFile.getPath().
            endsWith(".gz")) {
            reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(is), ENCODE));
        } else {
            reader = new BufferedReader(new InputStreamReader(
                is, ENCODE));
        }

        return reader;
    }

    private String subString(String source) {
        int length = 20;

        if (length >= source.length()) {
            return source;
        } else {
            return source.substring(0, length - 1) + "...";
        }
    }

    /**
     * refine all MZTabFile consistency correct.
     */
    private void refine() throws MZTabException, MZTabErrorOverflowException {
        if (mzTabFile == null) {
            return;
        }

        Metadata metadata = mzTabFile.getMetadata();

        //If ms_run[1-n]-hash is present,  ms_run[1-n]-hash_method SHOULD also be present
        for (MsRun msRun : metadata.getMsRun()) {
            if (msRun.getHash() != null && msRun.getHashMethod() == null) {
                throw new MZTabException(new MZTabError(
                    LogicalErrorType.MsRunHashMethodNotDefined, -1, msRun.
                        getId().
                        toString()));
            }
            //ms_run[1-n]-usi_identifier SHOULD be an MS run identifier: mzspec:<collection>:<msRun>
            if (msRun.getUsiIdentifier() != null && !MS_RUN_IDENTIFIER.matcher(
                msRun.getUsiIdentifier()).matches()) {
                errorList.add(new MZTabError(
                    LogicalErrorType.MsRunUsiIdentifierInvalid, -1, msRun.
                        getId().
                        toString(), msRun.getUsiIdentifier()).location(
                        Locations.metadataKey("ms_run[" + msRun.getId()
                            + "]-usi_identifier")));
            }
        }
    }

    /**
     * Query {@link MZTabErrorList} to check exist errors or not.
     *
     * @throws java.io.IOException
     * @throws uk.ac.ebi.pride.jmztab.utils.errors.MZTabException during parsing
     * of metadata,
     * protein/peptide/small_molecule/small_molecule_feature/small_molecule_evidence
     * header lines, if there exist any errors.
     * @throws uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorOverflowException
     * when too many errors are detected, as defined by the MzTabAccess.Properties
     * file mztab.max_error_count parameter.
     */
    private void check() throws IOException, MZTabException, MZTabErrorOverflowException {
        COMLineParser comParser = new COMLineParser(context);
        MTDLineParser mtdParser = new MTDLineParser(context);
        SMHLineParser smhParser = null;
        SMLLineParser smlParser = null;
        SFHLineParser sfhParser = null;
        SMFLineParser smfParser = null;
        SEHLineParser sehParser = null;
        SMELineParser smeParser = null;

        SortedMap<Integer, Comment> commentMap = new TreeMap<>();
        SortedMap<Integer, SmallMoleculeSummary> smallMoleculeSummaryMap = new TreeMap<>();
        SortedMap<Integer, SmallMoleculeFeature> smallMoleculeFeatureMap = new TreeMap<>();
        SortedMap<Integer, SmallMoleculeEvidence> smallMoleculeEvidenceMap = new TreeMap<>();

        PositionMapping smlPositionMapping = null;
        PositionMapping smfPositionMapping = null;
        PositionMapping smePositionMapping = null;

        String line;
        int highWaterMark = 1;
        int lineNumber = 0;
        Section section;
        try (BufferedReader reader = readFile(tabFile)) {
            while ((line = reader.readLine()) != null) {
                try {
                    lineNumber++;

                    if (MZTabStringUtils.isEmpty(line)) {
                        continue;
                    }

                    if (line.startsWith(Section.Comment.getPrefix())) {
                        lineIndex.record(lineNumber, SectionEnum.COM);
                        comParser.parse(lineNumber, line, errorList);
                        commentMap.put(lineNumber, comParser.getComment());
                        continue;
                    }

                    section = getSection(line);
                    // record before any check or line parser can report on this line
                    recordLine(lineNumber, section, line);
                    if (section == null) {
                        MZTabError sectionNullError = new MZTabError(
                            FormatErrorType.LinePrefix, lineNumber,
                            subString(line));
                        throw new MZTabException(sectionNullError);
                    }
                    if (section.getLevel() < highWaterMark) {
                        Section currentSection = Section.findSection(
                            highWaterMark);
                        MZTabError sectionLineOrderError = new MZTabError(
                            LogicalErrorType.LineOrder, lineNumber,
                            currentSection.getName(), section.getName());
                        throw new MZTabException(sectionLineOrderError);
                    }

                    highWaterMark = section.getLevel();

                    switch (highWaterMark) {
                        case 1:
                            // metadata section.
                            mtdParser.parse(lineNumber, line, errorList);
                            break;
                        case 8:
                            if (smhParser != null) {
                                MZTabError error = new MZTabError(
                                    LogicalErrorType.HeaderLine,
                                    lineNumber, subString(line));
                                // header line only display once!
                                throw new MZTabException(error);
                            }

                            // small molecule header section
                            smhParser = new SMHLineParser(context, mtdParser.
                                getMetadata());
                            smhParser.parse(lineNumber, line, errorList);
                            smlPositionMapping = new PositionMapping(smhParser.
                                getFactory(), line);

                            // tell system to continue check small molecule data line.
                            highWaterMark = 9;
                            break;
                        case 9:
                            if (smhParser == null) {
                                // header line should be check first.
                                throw new MZTabException(new MZTabError(
                                    LogicalErrorType.NoHeaderLine,
                                    lineNumber, subString(line)));
                            }

                            if (smlParser == null) {
                                smlParser = new SMLLineParser(context,
                                    smhParser.
                                        getFactory(),
                                    smlPositionMapping, mtdParser.getMetadata(),
                                    errorList);
                            }
                            smlParser.parse(lineNumber, line, errorList);
                            smallMoleculeSummaryMap.put(lineNumber, smlParser.
                                getRecord());

                            break;
                        case 10:
                            if (sfhParser != null) {
                                // header line only display once!
                                throw new MZTabException(new MZTabError(
                                    LogicalErrorType.HeaderLine,
                                    lineNumber, subString(line)));
                            }

                            // small molecule header section
                            sfhParser = new SFHLineParser(context, mtdParser.
                                getMetadata());
                            sfhParser.parse(lineNumber, line, errorList);
                            smfPositionMapping = new PositionMapping(sfhParser.
                                getFactory(), line);

                            // tell system to continue check small molecule data line.
                            highWaterMark = 11;
                            break;
                        case 11:
                            if (sfhParser == null) {
                                // header line should be check first.
                                throw new MZTabException(new MZTabError(
                                    LogicalErrorType.NoHeaderLine,
                                    lineNumber, subString(line)));
                            }

                            if (smfParser == null) {
                                smfParser = new SMFLineParser(context,
                                    sfhParser.
                                        getFactory(),
                                    smfPositionMapping, mtdParser.getMetadata(),
                                    errorList);
                            }
                            smfParser.parse(lineNumber, line, errorList);
                            smallMoleculeFeatureMap.put(lineNumber, smfParser.
                                getRecord());

                            break;
                        case 12:
                            if (sehParser != null) {
                                // header line only display once!
                                throw new MZTabException(new MZTabError(
                                    LogicalErrorType.HeaderLine,
                                    lineNumber, subString(line)));
                            }

                            // small molecule header section
                            sehParser = new SEHLineParser(context, mtdParser.
                                getMetadata());
                            sehParser.parse(lineNumber, line, errorList);
                            smePositionMapping = new PositionMapping(sehParser.
                                getFactory(), line);

                            // tell system to continue check small molecule data line.
                            highWaterMark = 13;
                            break;
                        case 13:
                            if (sehParser == null) {
                                // header line should be check first.
                                throw new MZTabException(new MZTabError(
                                    LogicalErrorType.NoHeaderLine,
                                    lineNumber, subString(line)));
                            }

                            if (smeParser == null) {
                                smeParser = new SMELineParser(context,
                                    sehParser.
                                        getFactory(),
                                    smePositionMapping, mtdParser.getMetadata(),
                                    errorList);
                            }
                            smeParser.parse(lineNumber, line, errorList);
                            smallMoleculeEvidenceMap.put(lineNumber, smeParser.
                                getRecord());

                            break;
                        default:
                            throw new IllegalArgumentException(
                                "Unknown section level " + highWaterMark);
                    }
                } catch (NullPointerException npe) {
                    throw new MZTabException(new MZTabError(
                        LogicalErrorType.NULL,
                        lineNumber, subString(line)), npe);
                }
            }

        }

        mtdParser.refineNormalMetadata();

        if (errorList.isEmpty()) {
            mzTabFile = new MzTab();
            mzTabFile.metadata(mtdParser.getMetadata());
            for (Integer id : commentMap.keySet()) {
                mzTabFile.addCommentItem(commentMap.get(id));
            }

            if (smlParser != null) {

                for (Integer id : smallMoleculeSummaryMap.keySet()) {
                    mzTabFile.addSmallMoleculeSummaryItem(
                        smallMoleculeSummaryMap.get(
                            id));
                }
                //check that reliability values are correct
                if (mzTabFile.getMetadata().
                    getSmallMoleculeIdentificationReliability() == null) {
                    Pattern p = Pattern.compile(REGEX_DEFAULT_RELIABILITY);
                    for (SmallMoleculeSummary smi : mzTabFile.
                        getSmallMoleculeSummary()) {
                        String reliability = smi.getReliability();
                        Matcher matcher = p.matcher(reliability);
                        if (!matcher.matches()) {
                            errorList.add(new MZTabError(
                                FormatErrorType.RegexMismatch, -1,
                                SmallMoleculeSummary.JSON_PROPERTY_RELIABILITY, reliability,
                                MzTabAccess.Properties.smallMoleculeSummary.
                                    getPropertyName(), "" + smi.getSmlId(),
                                REGEX_DEFAULT_RELIABILITY));
                        }
                    }
                }
                checkColunitMapping(smhParser.getFactory(), Optional.ofNullable(
                    mzTabFile.
                        getMetadata().
                        getColunitSmallMolecule()),
                    Metadata.JSON_PROPERTY_COLUNIT_SMALL_MOLECULE,
                    MzTabAccess.Properties.smallMoleculeSummary);
            }

            if (smfParser != null) {
                for (Integer id : smallMoleculeFeatureMap.keySet()) {
                    SmallMoleculeFeature smf
                        = smallMoleculeFeatureMap.get(
                            id);
                    mzTabFile.addSmallMoleculeFeatureItem(smf);
                }
                checkColunitMapping(sfhParser.getFactory(), Optional.ofNullable(
                    mzTabFile.
                        getMetadata().
                        getColunitSmallMoleculeFeature()),
                    Metadata.JSON_PROPERTY_COLUNIT_SMALL_MOLECULE_FEATURE,
                    MzTabAccess.Properties.smallMoleculeFeature);
            }
            if (smeParser != null) {
                for (Integer id : smallMoleculeEvidenceMap.keySet()) {
                    mzTabFile.addSmallMoleculeEvidenceItem(
                        smallMoleculeEvidenceMap.get(
                            id));
                }
                checkColunitMapping(sehParser.getFactory(), Optional.ofNullable(
                    mzTabFile.
                        getMetadata().
                        getColunitSmallMoleculeEvidence()),
                    Metadata.JSON_PROPERTY_COLUNIT_SMALL_MOLECULE_EVIDENCE,
                    MzTabAccess.Properties.smallMoleculeEvidence
                );
            }
            //check ID refs, starting at SML level
            if (smlParser != null && smfParser != null) {
                for (Integer id : smallMoleculeSummaryMap.keySet()) {
                    SmallMoleculeSummary sms = smallMoleculeSummaryMap.get(id);
                    Set<Integer> smfIdRefs = new HashSet<>(sms.getSmfIdRefs());
                    Set<Integer> definedIds = smallMoleculeFeatureMap.values().
                        stream().
                        map((t) ->
                        {
                            return t.getSmfId();
                        }).
                        collect(Collectors.toSet());
                    smfIdRefs.removeAll(definedIds);
                    if (!smfIdRefs.isEmpty()) {
                        for (Integer smfRefId : smfIdRefs) {
                            //raise a warning about unmatched SMF id
                            //Reference id "{0}" for column "{1}" from element "{2}" in section "{3}" to section "{4}" must have a matching element defined.
                            errorList.add(new MZTabError(
                                LogicalErrorType.UnknownRefId, -1, "" + smfRefId,
                                SmallMoleculeSummary.JSON_PROPERTY_SMF_ID_REFS, "" + sms.getSmlId(),
                                MzTabAccess.Properties.smallMoleculeSummary.
                                    getPropertyName(),
                                MzTabAccess.Properties.smallMoleculeFeature.
                                    getPropertyName()));
                        }
                    }
                }
                if (smeParser != null) {
                    for (Integer id : smallMoleculeFeatureMap.keySet()) {
                        SmallMoleculeFeature smf = smallMoleculeFeatureMap.get(
                            id);
                        Set<Integer> smeIdRefs = new HashSet<>(smf.
                            getSmeIdRefs());
                        Set<Integer> definedIds = smallMoleculeEvidenceMap.
                            values().
                            stream().
                            map((t) ->
                            {
                                return t.getSmeId();
                            }).
                            collect(Collectors.toSet());
                        smeIdRefs.removeAll(definedIds);
                        if (!smeIdRefs.isEmpty()) {
                            for (Integer smeRefId : smeIdRefs) {
                                //raise a warning about unmatched SMF id
                                //Reference id "{0}" for column "{1}" from element "{2}" in section "{3}" to section "{4}" must have a matching element defined.
                                errorList.add(new MZTabError(
                                    LogicalErrorType.UnknownRefId, -1,
                                    "" + smeRefId,
                                    SmallMoleculeFeature.JSON_PROPERTY_SME_ID_REFS, "" + smf.getSmfId(),
                                    MzTabAccess.Properties.smallMoleculeFeature.
                                        getPropertyName(),
                                    MzTabAccess.Properties.smallMoleculeEvidence.
                                        getPropertyName()));
                            }
                        }
                    }
                }
            }
            // Determine/verify the mzTab-profile and enforce the profile-driven
            // table presence, cross-reference and conditional-field rules.
            for (MZTabError profileError : new MzTabProfileValidator().
                validateProfile(mzTabFile.getMetadata(),
                    smallMoleculeSummaryMap, smallMoleculeFeatureMap,
                    smallMoleculeEvidenceMap)) {
                errorList.add(profileError);
            }
        }

    }

    protected void checkColunitMapping(MZTabColumnFactory columnFactory,
        Optional<Collection<ColumnParameterMapping>> columnParameterMapping,
        String colUnitProperty, MzTabAccess.Properties mzTabSection) {
        columnParameterMapping.orElse(Collections.emptyList()).
            forEach((colUnit) ->
            {
                String columnName = colUnit.getColumnName();
                IMZTabColumn column = columnFactory.findColumnByHeader(
                    columnName);
                if (column == null) {
                    errorList.add(new MZTabError(
                        FormatErrorType.ColUnit, -1,
                        colUnitProperty, columnName,
                        mzTabSection.
                            getPropertyName()));
                }
            });
    }

    /**
     * <p>
     * getMZTabFile.</p>
     *
     * @return a {@link org.lifstools.mztab2.model.MzTab} object.
     */
    public MzTab getMZTabFile() {
        return mzTabFile;
    }
}

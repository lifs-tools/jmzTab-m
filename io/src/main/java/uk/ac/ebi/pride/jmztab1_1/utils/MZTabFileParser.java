package uk.ac.ebi.pride.jmztab1_1.utils;

import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorOverflowException;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.SFHLineParser;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.PositionMapping;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.SMFLineParser;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.MZTabParserContext;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.SEHLineParser;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.MTDLineParser;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.SMLLineParser;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.SMELineParser;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.SMHLineParser;
import uk.ac.ebi.pride.jmztab1_1.utils.parser.COMLineParser;
import de.isas.mztab1_1.model.Comment;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
import de.isas.mztab1_1.model.SmallMoleculeSummary;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.TAB;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabUtils;
import uk.ac.ebi.pride.jmztab1_1.model.Section;
import static uk.ac.ebi.pride.jmztab1_1.utils.MZTabProperties.*;

/**
 *
 * MZTabFileParser provides reading functionality of the mzTab file. During the
 * parsing process, minimal integrity checks are preformed.
 *
 * @author qingwei
 * @since 21/02/13
 */
public class MZTabFileParser {

    private MzTab mzTabFile;
    private URI tabFile;

    private MZTabErrorList errorList;
    private MZTabParserContext context;

    /**
     * Create a new {@code MZTabFileParser} for the given file. 
     *
     * @param tabFile the MZTab file. The file SHOULD not be null and MUST exist
     * @throws java.lang.IllegalArgumentException
     */
    public MZTabFileParser(File tabFile) throws IllegalArgumentException {
        this(tabFile.toURI());
    }

    /**
     * Create a new {@code MZTabFileParser} for the given file URI.
     *
     * @param tabFileUri the MZTab file URI. The file SHOULD not be null and
     * MUST exist
     * {@link MZTabErrorList} return by {@link MZTabFileParser#getErrorList()}
     * @throws java.lang.IllegalArgumentException
     */
    public MZTabFileParser(URI tabFileUri) throws IllegalArgumentException {
        if (tabFileUri == null) {
            throw new IllegalArgumentException("MZTab file uri must not be null!");
        }
        if(("file".equals(tabFileUri.getScheme()) && !new File(tabFileUri).exists())) {
            throw new IllegalArgumentException("MZTab File URI "+tabFileUri.toASCIIString()+" does not exist!");
        }

        this.tabFile = tabFileUri;
    }

    /**
     * Create a new {@code MZTabParserContext} and {@code MZTabErrorList} for the given file URI.
     * Parsing output and errors are written to the provided
     * {@link java.io.OutputStream}.
     *
     * @param out the output stream for parsing messages
     * @param level the minimum error level to report errors for
     * @param maxErrorCount the maximum number of errors to report in the
     * {@link MZTabErrorList} return by {@link MZTabFileParser#getErrorList()}
     * @throws java.io.IOException
     * @return the error list
     */
    public MZTabErrorList parse(OutputStream out, MZTabErrorType.Level level, int maxErrorCount) throws IOException {
        try {
            context = new MZTabParserContext();
            errorList = new MZTabErrorList(level, maxErrorCount);
            check();
            refine();
        } catch (MZTabException e) {
            out.write(MZTabExceptionMessage.getBytes());
            errorList.add(e.getError());
        } catch (MZTabErrorOverflowException e) {
            out.write(MZTabErrorOverflowExceptionMessage.getBytes());
        }

        errorList.print(out);
        if (errorList.isEmpty()) {
            out.write(("No errors in " + tabFile + " file!" + NEW_LINE).
                getBytes());
        }
        return errorList;
    }
    
    /**
     * Create a new {@code MZTabParserContext} and {@code MZTabErrorList} for the given file URI.
     * Parsing output and errors are written to the provided
     * {@link java.io.OutputStream}. Reports up to {@link MZTabFileParser#MAX_ERROR_COUNT} errors.
     *
     * @param out the output stream for parsing messages
     * @param level the minimum error level to report errors for
     * @throws java.io.IOException
     * @return the error list
     */
    public MZTabErrorList parse(OutputStream out, MZTabErrorType.Level level) throws IOException {
        return parse(out, level, MAX_ERROR_COUNT);
    }

    /**
     * Create a new {@code MZTabParserContext} and {@code MZTabErrorList} for the given file URI.
     * Parsing output and errors are written to the provided
     * {@link java.io.OutputStream}. Reports up to {@link MZTabFileParser#MAX_ERROR_COUNT} errors
     * on level {@link MZTabFileParser#LEVEL}.
     *
     * @param out the output stream for parsing messages
     * @throws java.io.IOException
     * @return the error list
     */    
    public MZTabErrorList parse(OutputStream out) throws IOException {
        return parse(out, LEVEL, MAX_ERROR_COUNT);
    }

    public MZTabErrorList getErrorList() {
        return errorList;
    }

    private Section getSection(String line) {
        String[] items = line.split("\\s*" + TAB + "\\s*");
        String section = items[0].trim();
        return Section.findSection(section);
    }

    private BufferedReader readFile(URI tabFile) throws IOException {
        BufferedReader reader;

        InputStream is = null;
        File mzTabFile = new File(tabFile);
        if (mzTabFile.isFile()) {
            is = new FileInputStream(mzTabFile);
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
        for (MsRun msRun : metadata.getMsrun()) {
            if (msRun.getHash() != null && msRun.getHashMethod() == null) {
                throw new MZTabException(new MZTabError(
                    LogicalErrorType.MsRunHashMethodNotDefined, -1, msRun.
                        getId().
                        toString()));
            }
        }

        // If mzTab-type is "Quantification", then at least one section with {protein|peptide|small_molecule}_abundance* columns MUST be present
        // FIXME commented during migration to mzTab 1.1
//        boolean hasAbundance = false;
//        if (metadata.getMZTabType() == MZTabDescription.Type.Quantification) {
//            if (proteinFactory != null && ! proteinFactory.getAbundanceColumnMapping().isEmpty()) {
//                hasAbundance = true;
//            }
//            if (peptideFactory != null && ! peptideFactory.getAbundanceColumnMapping().isEmpty()) {
//                hasAbundance = true;
//            }
//            if (smlFactory != null && ! smlFactory.getAbundanceColumnMapping().isEmpty()) {
//                hasAbundance = true;
//            }
//            if (! hasAbundance) {
//                throw new MZTabException(new MZTabError(LogicalErrorType.QuantificationAbundance, -1));
//            }
//        }
    }

    /**
     * Query {@link MZTabErrorList} to check exist errors or not.
     *
     * @throws java.io.IOException
     * @throws uk.ac.ebi.pride.jmztab.utils.errors.MZTabException during parse
     * metadata, protein/peptide/small_molecule header line, exists error.
     * @throws uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorOverflowException
     * reference mztab.properties file mztab.max_error_count parameter.
     */
    private void check() throws IOException, MZTabException, MZTabErrorOverflowException {
        BufferedReader reader = readFile(tabFile);

        COMLineParser comParser = new COMLineParser(context);
        MTDLineParser mtdParser = new MTDLineParser(context);
//        PRHLineParser prhParser = null;
//        PRTLineParser prtParser = null;
//        PEHLineParser pehParser = null;
//        PEPLineParser pepParser = null;
//        PSHLineParser pshParser = null;
//        PSMLineParser psmParser = null;
        SMHLineParser smhParser = null;
        SMLLineParser smlParser = null;
        SFHLineParser sfhParser = null;
        SMFLineParser smfParser = null;
        SEHLineParser sehParser = null;
        SMELineParser smeParser = null;

        SortedMap<Integer, Comment> commentMap = new TreeMap<Integer, Comment>();
//        SortedMap<Integer, Protein> proteinMap = new TreeMap<Integer, Protein>();
//        SortedMap<Integer, Peptide> peptideMap = new TreeMap<Integer, Peptide>();
//        SortedMap<Integer, PSM> psmMap = new TreeMap<Integer, PSM>();
        SortedMap<Integer, SmallMoleculeSummary> smallMoleculeSummaryMap = new TreeMap<>();
        SortedMap<Integer, SmallMoleculeFeature> smallMoleculeFeatureMap = new TreeMap<>();
        SortedMap<Integer, SmallMoleculeEvidence> smallMoleculeEvidenceMap = new TreeMap<>();

//        PositionMapping prtPositionMapping = null;
//        PositionMapping pepPositionMapping = null;
//        PositionMapping psmPositionMapping = null;
        PositionMapping smlPositionMapping = null;
        PositionMapping smfPositionMapping = null;
        PositionMapping smePositionMapping = null;

        String line;
        int highWaterMark = 1;
        int lineNumber = 0;
        Section section;
        while ((line = reader.readLine()) != null) {
            try {
                lineNumber++;

                if (MZTabUtils.isEmpty(line)) {
                    continue;
                }

                if (line.startsWith(Section.Comment.getPrefix())) {
                    comParser.parse(lineNumber, line, errorList);
                    commentMap.put(lineNumber, comParser.getComment());
                    continue;
                }

                section = getSection(line);
                if (section == null) {
                    MZTabError sectionNullError = new MZTabError(
                        FormatErrorType.LinePrefix, lineNumber,
                        subString(line));
                    throw new MZTabException(sectionNullError);
                }
                if (section.getLevel() < highWaterMark) {
                    Section currentSection = Section.findSection(highWaterMark);
                    MZTabError sectionLineOrderError = new MZTabError(
                        LogicalErrorType.LineOrder, lineNumber,
                        currentSection.getName(), section.getName());
                    throw new MZTabException(sectionLineOrderError);
                }

                highWaterMark = section.getLevel();
                // There exists errors during checking metadata section.
                // However it stop with warnings too, so we should try to continue parsing as much as
                // possible to provide several errors/warnings not only the first one in metadata
                //            if (highWaterMark == 1 && ! errorList.isEmpty()) {
                //                break;
                //            }

                switch (highWaterMark) {
                    case 1:
                        // metadata section.
                        mtdParser.parse(lineNumber, line, errorList);
                        break;
                    //                case 2:
                    //                    if (prhParser != null) {
                    //                        // header line only display once!
                    //                        error = new MZTabError(LogicalErrorType.HeaderLine, lineNumber, subString(line));
                    //                        throw new MZTabException(error);
                    //                    }
                    //
                    //                    // protein header section
                    //                    prhParser = new PRHLineParser(mtdParser.getMetadata());
                    //                    prhParser.parse(lineNumber, line, errorList);
                    //                    prtPositionMapping = new PositionMapping(prhParser.getFactory(), line);
                    //
                    //                    // tell system to continue check protein data line.
                    //                    highWaterMark = 3;
                    //                    break;
                    //                case 3:
                    //                    if (prhParser == null) {
                    //                        // header line should be check first.
                    //                        error = new MZTabError(LogicalErrorType.NoHeaderLine, lineNumber, subString(line));
                    //                        throw new MZTabException(error);
                    //                    }
                    //
                    //                    if (prtParser == null) {
                    //                        prtParser = new PRTLineParser(prhParser.getFactory(), prtPositionMapping, mtdParser.getMetadata(), errorList);
                    //                    }
                    //                    prtParser.parse(lineNumber, line, errorList);
                    //                    proteinMap.put(lineNumber, prtParser.getRecord());
                    //
                    //                    break;
                    //                case 4:
                    //                    if (pehParser != null) {
                    //                        // header line only display once!
                    //                        error = new MZTabError(LogicalErrorType.HeaderLine, lineNumber, subString(line));
                    //                        throw new MZTabException(error);
                    //                    }
                    //
                    //                    if (mtdParser.getMetadata().getMZTabType() == MZTabDescription.Type.Identification) {
                    //                        errorList.add(new MZTabError(LogicalErrorType.PeptideSection, lineNumber, subString(line)));
                    //                    }
                    //
                    //                    // peptide header section
                    //                    pehParser = new PEHLineParser(mtdParser.getMetadata());
                    //                    pehParser.parse(lineNumber, line, errorList);
                    //                    pepPositionMapping = new PositionMapping(pehParser.getFactory(), line);
                    //
                    //                    // tell system to continue check peptide data line.
                    //                    highWaterMark = 5;
                    //                    break;
                    //                case 5:
                    //                    if (pehParser == null) {
                    //                        // header line should be check first.
                    //                        error = new MZTabError(LogicalErrorType.NoHeaderLine, lineNumber, subString(line));
                    //                        throw new MZTabException(error);
                    //                    }
                    //
                    //                    if (pepParser == null) {
                    //                        pepParser = new PEPLineParser(pehParser.getFactory(), pepPositionMapping, mtdParser.getMetadata(), errorList);
                    //                    }
                    //                    pepParser.parse(lineNumber, line, errorList);
                    //                    peptideMap.put(lineNumber, pepParser.getRecord());
                    //
                    //                    break;
                    //                case 6:
                    //                    if (pshParser != null) {
                    //                        // header line only display once!
                    //                        error = new MZTabError(LogicalErrorType.HeaderLine, lineNumber, subString(line));
                    //                        throw new MZTabException(error);
                    //                    }
                    //
                    //                    // psm header section
                    //                    pshParser = new PSHLineParser(mtdParser.getMetadata());
                    //                    pshParser.parse(lineNumber, line, errorList);
                    //                    psmPositionMapping = new PositionMapping(pshParser.getFactory(), line);
                    //
                    //                    // tell system to continue check peptide data line.
                    //                    highWaterMark = 7;
                    //                    break;
                    //                case 7:
                    //                    if (pshParser == null) {
                    //                        // header line should be check first.
                    //                        error = new MZTabError(LogicalErrorType.NoHeaderLine, lineNumber, subString(line));
                    //                        throw new MZTabException(error);
                    //                    }
                    //
                    //                    if (psmParser == null) {
                    //                        psmParser = new PSMLineParser(pshParser.getFactory(), psmPositionMapping, mtdParser.getMetadata(), errorList);
                    //                    }
                    //                    psmParser.parse(lineNumber, line, errorList);
                    //                    psmMap.put(lineNumber, psmParser.getRecord());
                    //
                    //                    break;
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
                            smlParser = new SMLLineParser(context, smhParser.
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
                            smfParser = new SMFLineParser(context, sfhParser.
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
                            smeParser = new SMELineParser(context, sehParser.
                                getFactory(),
                                smePositionMapping, mtdParser.getMetadata(),
                                errorList);
                        }
                        smeParser.parse(lineNumber, line, errorList);
                        smallMoleculeEvidenceMap.put(lineNumber, smeParser.
                            getRecord());

                        break;
                }
            } catch (NullPointerException npe) {
                throw new MZTabException(new MZTabError(LogicalErrorType.NULL,
                    lineNumber, subString(line)), npe);
            }
        }

        if (reader != null) {
            reader.close();
        }

        if (errorList.isEmpty()) {
            mzTabFile = new MzTab();
            mzTabFile.metadata(mtdParser.getMetadata());
            for (Integer id : commentMap.keySet()) {
                mzTabFile.addCommentItem(commentMap.get(id));
            }

//            if (prhParser != null) {
//                MZTabColumnFactory proteinColumnFactory = prhParser.getFactory();
//                mzTabFile.setProteinColumnFactory(proteinColumnFactory);
//                for (Integer id : proteinMap.keySet()) {
//                    mzTabFile.addProtein(id, proteinMap.get(id));
//                }
//            }
//
//            if (pehParser != null) {
//                MZTabColumnFactory peptideColumnFactory = pehParser.getFactory();
//                mzTabFile.setPeptideColumnFactory(peptideColumnFactory);
//                for (Integer id : peptideMap.keySet()) {
//                    mzTabFile.addPeptide(id, peptideMap.get(id));
//                }
//            }
//
//            if (pshParser != null) {
//                MZTabColumnFactory psmColumnFactory = pshParser.getFactory();
//                mzTabFile.setPSMColumnFactory(psmColumnFactory);
//                for (Integer id : psmMap.keySet()) {
//                    mzTabFile.addPSM(id, psmMap.get(id));
//                }
//            }
            if (smallMoleculeSummaryMap.isEmpty()) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NoSmallMoleculeSummarySection, -1,
                    "NoSmallMoleculeSummarySection"));
            }
            if (smhParser != null) {

                for (Integer id : smallMoleculeSummaryMap.keySet()) {
                    mzTabFile.addSmallMoleculeSummaryItem(
                        smallMoleculeSummaryMap.get(
                            id));
                }
            }

            if (smallMoleculeFeatureMap.isEmpty() && !smallMoleculeSummaryMap.
                isEmpty()) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NoSmallMoleculeFeatureSection, -1,
                    "NoSmallMoleculeFeatureSection"));
            }
            if (smfParser != null) {
                for (Integer id : smallMoleculeFeatureMap.keySet()) {
                    mzTabFile.addSmallMoleculeFeatureItem(
                        smallMoleculeFeatureMap.get(
                            id));
                }
            }
            if (smallMoleculeEvidenceMap.isEmpty() && !smallMoleculeSummaryMap.
                isEmpty()) {
                errorList.add(new MZTabError(
                    LogicalErrorType.NoSmallMoleculeEvidenceSection, -1,
                    "NoSmallMoleculeFeatureSection"));
            }
            if (smeParser != null) {
                for (Integer id : smallMoleculeEvidenceMap.keySet()) {
                    mzTabFile.addSmallMoleculeEvidenceItem(
                        smallMoleculeEvidenceMap.get(
                            id));
                }
            }
        }

    }

    public MzTab getMZTabFile() {
        return mzTabFile;
    }
}

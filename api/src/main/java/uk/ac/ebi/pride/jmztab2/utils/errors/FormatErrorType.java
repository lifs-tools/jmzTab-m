package uk.ac.ebi.pride.jmztab2.utils.errors;

import static uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Category.Format;

/**
 * Diagnose different types of format-related (reporting format problems).
 * Reference: http://code.google.com/p/mztab/wiki/jmzTab_message for details.
 *
 * @author qingwei
 * @since 29/01/13
 *
 */
public final class FormatErrorType extends MZTabErrorType {

    private FormatErrorType() {

    }

    /**
     * Constant <code>LinePrefix</code>
     */
    public static final MZTabErrorType LinePrefix = createError(Format, "LinePrefix");
    /**
     * Constant <code>CountMatch</code>
     */
    public static final MZTabErrorType CountMatch = createError(Format, "CountMatch");

    /**
     * Constant <code>IndexedElement</code>
     */
    public static final MZTabErrorType IndexedElement = createError(Format,
        "IndexedElement");
    /**
     * Constant <code>AbundanceColumn</code>
     */
    public static final MZTabErrorType AbundanceColumn = createError(Format,
        "AbundanceColumn");
    /**
     * Constant <code>MsRunOptionalColumn</code>
     */
    public static final MZTabErrorType MsRunOptionalColumn = createError(Format,
        "MsRunOptionalColumn");
    /**
     * Constant <code>OptionalCVParamColumn</code>
     */
    public static final MZTabErrorType OptionalCVParamColumn = createError(Format,
        "OptionalCVParamColumn");
    /**
     * Constant <code>StableColumn</code>
     */
    public static final MZTabErrorType StableColumn = createError(Format,
        "StableColumn");

    /**
     * Constant <code>MTDLine</code>
     */
    public static final MZTabErrorType MTDLine = createError(Format, "MTDLine");
    /**
     * Constant <code>MTDDefineLabel</code>
     */
    public static final MZTabErrorType MTDDefineLabel = createError(Format,
        "MTDDefineLabel");
    /**
     * Constant <code>MZTabMode</code>
     */
    public static final MZTabErrorType MZTabMode = createError(Format, "MZTabMode");
    /**
     * Constant <code>MZTabType</code>
     */
    public static final MZTabErrorType MZTabType = createError(Format, "MZTabType");
    /**
     * Constant <code>MZTabType</code>
     */
    public static final MZTabErrorType MZTabId = createError(Format, "MZTabId");
    /**
     * Constant <code>MZTabVersion</code>
     */
    public static final MZTabErrorType MZTabVersion = createError(Format,
        "MZTabVersion");
    /**
     * Constant <code>Param</code>
     */
    public static final MZTabErrorType Param = createError(Format, "Param");
    /**
     * Constant <code>ParamList</code>
     */
    public static final MZTabErrorType ParamList = createError(Format, "ParamList");
    /**
     * Constant <code>Publication</code>
     */
    public static final MZTabErrorType Publication = createError(Format, "Publication");
    /**
     * Constant <code>URI</code>
     */
    public static final MZTabErrorType URI = createError(Format, "URI");
    /**
     * Constant <code>URL</code>
     */
    public static final MZTabErrorType URL = createError(Format, "URL");
    /**
     * Constant <code>Email</code>
     */
    public static final MZTabErrorType Email = createError(Format, "Email");

    /**
     * Constant <code>Integer</code>
     */
    public static final MZTabErrorType Integer = createError(Format, "Integer");
    /**
     * Constant <code>Double</code>
     */
    public static final MZTabErrorType Double = createError(Format, "Double");
    /**
     * Constant <code>Reliability</code>
     */
    public static final MZTabErrorType Reliability = createError(Format, "Reliability");
    /**
     * Constant <code>StringList</code>
     */
    public static final MZTabErrorType StringList = createError(Format, "StringList");
    /**
     * Constant <code>DoubleList</code>
     */
    public static final MZTabErrorType DoubleList = createError(Format, "DoubleList");
    /**
     * Constant <code>ModificationList</code>
     */
    public static final MZTabErrorType ModificationList = createError(Format,
        "ModificationList");
    /**
     * Constant <code>GOTermList</code>
     */
    public static final MZTabErrorType GOTermList = createError(Format, "GOTermList");
    /**
     * Constant <code>MZBoolean</code>
     */
    public static final MZTabErrorType MZBoolean = createError(Format, "MZBoolean");
    /**
     * Constant <code>SpectraRef</code>
     */
    public static final MZTabErrorType SpectraRef = createError(Format, "SpectraRef");
    /**
     * Constant <code>CHEMMODSAccession</code>
     */
    public static final MZTabErrorType CHEMMODSAccession = createError(Format,
        "CHEMMODSAccession");
    /**
     * Constant <code>SearchEngineScore</code>
     */
    public static final MZTabErrorType SearchEngineScore = createWarn(Format,
        "SearchEngineScore");
    /**
     * Constant <code>Sequence</code>
     */
    public static final MZTabErrorType Sequence = createWarn(Format,
        "SearchEngineScore");

    /**
     * Constant <code>ColUnit</code>
     */
    public static final MZTabErrorType ColUnit = createError(Format, "ColUnit");

    /**
     * Constant <code>IntegerList</code>
     */
    public static final MZTabErrorType IntegerList = createError(Format, "IntegerList");

    /**
     * Constant <code>RegexMismatch</code>
     */
    public static final MZTabErrorType RegexMismatch = createError(Format, "RegexMismatch");
    
    /**
     * Constant <code>ParamAccessionNotNamespaced</code>
     */
    public static final MZTabErrorType ParamAccessionNotNamespaced = createWarn(Format, "ParamAccessionNotNamespaced");
    
    /**
     * Constant <code>InvalidColunitFormat</code>
     */
    public static final MZTabErrorType InvalidColunitFormat = createError(Format, "InvalidColunitFormat");
    
    public static final MZTabErrorType[] VALUES = {
        LinePrefix, CountMatch, IndexedElement, AbundanceColumn, MsRunOptionalColumn, OptionalCVParamColumn, StableColumn, MTDLine, MTDDefineLabel, MZTabMode,
        MZTabType, MZTabId, MZTabVersion, Param, ParamList, Publication, URI, URL, Email, Integer, Double, Reliability, StringList, DoubleList, ModificationList, GOTermList,
        MZBoolean, SpectraRef, CHEMMODSAccession, SearchEngineScore, Sequence, ColUnit, IntegerList, RegexMismatch, ParamAccessionNotNamespaced, InvalidColunitFormat
    };
}

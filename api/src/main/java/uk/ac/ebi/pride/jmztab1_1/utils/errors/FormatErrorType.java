package uk.ac.ebi.pride.jmztab1_1.utils.errors;

import static uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorType.Category.Format;

/**
 * Diagnose different types of format-related (reporting format problems).
 * Reference: http://code.google.com/p/mztab/wiki/jmzTab_message for details.
 *
 * @author qingwei
 * @since 29/01/13
 * 
 */
public class FormatErrorType extends MZTabErrorType {
    /** Constant <code>LinePrefix</code> */
    public static MZTabErrorType LinePrefix = createError(Format, "LinePrefix");
    /** Constant <code>CountMatch</code> */
    public static MZTabErrorType CountMatch = createError(Format, "CountMatch");

    /** Constant <code>IndexedElement</code> */
    public static MZTabErrorType IndexedElement = createError(Format, "IndexedElement");
    /** Constant <code>AbundanceColumn</code> */
    public static MZTabErrorType AbundanceColumn = createError(Format, "AbundanceColumn");
    /** Constant <code>MsRunOptionalColumn</code> */
    public static MZTabErrorType MsRunOptionalColumn = createError(Format, "MsRunOptionalColumn");
    /** Constant <code>OptionalCVParamColumn</code> */
    public static MZTabErrorType OptionalCVParamColumn = createError(Format, "OptionalCVParamColumn");
    /** Constant <code>StableColumn</code> */
    public static MZTabErrorType StableColumn = createError(Format, "StableColumn");

    /** Constant <code>MTDLine</code> */
    public static MZTabErrorType MTDLine = createError(Format, "MTDLine");
    /** Constant <code>MTDDefineLabel</code> */
    public static MZTabErrorType MTDDefineLabel = createError(Format, "MTDDefineLabel");
    /** Constant <code>MZTabMode</code> */
    public static MZTabErrorType MZTabMode = createError(Format, "MZTabMode");
    /** Constant <code>MZTabType</code> */
    public static MZTabErrorType MZTabType = createError(Format, "MZTabType");
    /** Constant <code>MZTabVersion</code> */
    public static MZTabErrorType MZTabVersion = createError(Format, "MZTabVersion");
    /** Constant <code>Param</code> */
    public static MZTabErrorType Param = createError(Format, "Param");
    /** Constant <code>ParamList</code> */
    public static MZTabErrorType ParamList = createError(Format, "ParamList");
    /** Constant <code>Publication</code> */
    public static MZTabErrorType Publication = createError(Format, "Publication");
    /** Constant <code>URI</code> */
    public static MZTabErrorType URI = createError(Format, "URI");
    /** Constant <code>URL</code> */
    public static MZTabErrorType URL = createError(Format, "URL");
    /** Constant <code>Email</code> */
    public static MZTabErrorType Email = createError(Format, "Email");

    /** Constant <code>Integer</code> */
    public static MZTabErrorType Integer = createError(Format, "Integer");
    /** Constant <code>Double</code> */
    public static MZTabErrorType Double = createError(Format, "Double");
    /** Constant <code>Reliability</code> */
    public static MZTabErrorType Reliability = createError(Format, "Reliability");
    /** Constant <code>StringList</code> */
    public static MZTabErrorType StringList = createError(Format, "StringList");
    /** Constant <code>DoubleList</code> */
    public static MZTabErrorType DoubleList = createError(Format, "DoubleList");
    /** Constant <code>ModificationList</code> */
    public static MZTabErrorType ModificationList = createError(Format, "ModificationList");
    /** Constant <code>GOTermList</code> */
    public static MZTabErrorType GOTermList = createError(Format, "GOTermList");
    /** Constant <code>MZBoolean</code> */
    public static MZTabErrorType MZBoolean = createError(Format, "MZBoolean");
    /** Constant <code>SpectraRef</code> */
    public static MZTabErrorType SpectraRef = createError(Format, "SpectraRef");
    /** Constant <code>CHEMMODSAccession</code> */
    public static MZTabErrorType CHEMMODSAccession = createError(Format, "CHEMMODSAccession");
    /** Constant <code>SearchEngineScore</code> */
    public static MZTabErrorType SearchEngineScore = createWarn(Format, "SearchEngineScore");
    /** Constant <code>Sequence</code> */
    public static MZTabErrorType Sequence = createWarn(Format, "SearchEngineScore");

    /** Constant <code>ColUnit</code> */
    public static MZTabErrorType ColUnit = createError(Format, "ColUnit");
}

package uk.ac.ebi.pride.jmztab1_1.utils.errors;

/**
 * Reporting errors related to the logical relationships among the different sections in a file.
 * Reference: http://code.google.com/p/mztab/wiki/jmzTab_message for details.
 *
 * @author qingwei
 * @since 29/01/13
 * 
 */
public final class LogicalErrorType extends MZTabErrorType {
    
    private LogicalErrorType() {
        
    }
    
    /** Constant <code>NULL</code> */
    public static MZTabErrorType NULL = createError(Category.Logical, "NULL");
    /** Constant <code>NotNULL</code> */
    public static MZTabErrorType NotNULL = createWarn(Category.Logical, "NotNULL");

    /** Constant <code>LineOrder</code> */
    public static MZTabErrorType LineOrder = createError(Category.Logical, "LineOrder");
    /** Constant <code>HeaderLine</code> */
    public static MZTabErrorType HeaderLine = createError(Category.Logical, "HeaderLine");
    /** Constant <code>NoHeaderLine</code> */
    public static MZTabErrorType NoHeaderLine = createError(Category.Logical, "NoHeaderLine");

    // not defined in metadata.
    /** Constant <code>MsRunNotDefined</code> */
    public static MZTabErrorType MsRunNotDefined = createError(Category.Logical, "MsRunNotDefined");
    /** Constant <code>AssayNotDefined</code> */
    public static MZTabErrorType AssayNotDefined = createError(Category.Logical, "AssayNotDefined");
    /** Constant <code>StudyVariableNotDefined</code> */
    public static MZTabErrorType StudyVariableNotDefined = createError(Category.Logical, "StudyVariableNotDefined");
    /** Constant <code>ProteinSearchEngineScoreNotDefined</code> */
    public static MZTabErrorType ProteinSearchEngineScoreNotDefined = createWarn(Category.Logical, "ProteinSearchEngineScoreNotDefined");
    /** Constant <code>PeptideSearchEngineScoreNotDefined</code> */
    public static MZTabErrorType PeptideSearchEngineScoreNotDefined = createWarn(Category.Logical, "PeptideSearchEngineScoreNotDefined");
    /** Constant <code>PSMSearchEngineScoreNotDefined</code> */
    public static MZTabErrorType PSMSearchEngineScoreNotDefined = createWarn(Category.Logical, "PSMSearchEngineScoreNotDefined");
    /** Constant <code>SmallMoleculeSearchEngineScoreNotDefined</code> */
    public static MZTabErrorType SmallMoleculeSearchEngineScoreNotDefined = createWarn(Category.Logical, "SmallMoleculeSearchEngineScoreNotDefined");

    /** Constant <code>MsRunHashMethodNotDefined</code> */
    public static MZTabErrorType MsRunHashMethodNotDefined = createError(Category.Logical, "MsRunHashMethodNotDefined");

    /** Constant <code>NotDefineInMetadata</code> */
    public static MZTabErrorType NotDefineInMetadata = createError(Category.Logical, "NotDefineInMetadata");
    /** Constant <code>NotDefineInHeader</code> */
    public static MZTabErrorType NotDefineInHeader = createError(Category.Logical, "NotDefineInHeader");
    /** Constant <code>DuplicationDefine</code> */
    public static MZTabErrorType DuplicationDefine = createError(Category.Logical, "DuplicationDefine");
    /** Constant <code>DuplicationAccession</code> */
    public static MZTabErrorType DuplicationAccession = createError(Category.Logical, "DuplicationAccession");
    /** Constant <code>AssayRefs</code> */
    public static MZTabErrorType AssayRefs = createError(Category.Logical, "AssayRefs");

    /** Constant <code>ProteinCoverage</code> */
    public static MZTabErrorType ProteinCoverage = createError(Category.Logical, "ProteinCoverage");
    /** Constant <code>IdNumber</code> */
    public static MZTabErrorType IdNumber = createError(Category.Logical, "IdNumber");
    /** Constant <code>ModificationPosition</code> */
    public static MZTabErrorType ModificationPosition = createError(Category.Logical, "ModificationPosition");
    /** Constant <code>CHEMMODS</code> */
    public static MZTabErrorType CHEMMODS = createWarn(Category.Logical, "CHEMMODS");
    /** Constant <code>SubstituteIdentifier</code> */
    public static MZTabErrorType SubstituteIdentifier = createError(Category.Logical, "SubstituteIdentifier");
    /** Constant <code>SoftwareVersion</code> */
    public static MZTabErrorType SoftwareVersion = createWarn(Category.Logical, "SoftwareVersion");

    /** Constant <code>AbundanceColumnTogether</code> */
    public static MZTabErrorType AbundanceColumnTogether = createError(Category.Logical, "AbundanceColumnTogether");
    /** Constant <code>AbundanceColumnSameId</code> */
    public static MZTabErrorType AbundanceColumnSameId = createError(Category.Logical, "AbundanceColumnSameId");

    /** Constant <code>SpectraRef</code> */
    public static MZTabErrorType SpectraRef = createWarn(Category.Logical, "SpectraRef");
    /** Constant <code>AmbiguityMod</code> */
    public static MZTabErrorType AmbiguityMod = createWarn(Category.Logical, "AmbiguityMod");
    /** Constant <code>MsRunLocation</code> */
    public static MZTabErrorType MsRunLocation = createWarn(Category.Logical, "MsRunLocation");

    /** Constant <code>FixedMod</code> */
    public static MZTabErrorType FixedMod = createError(Category.Logical, "FixedMod");
    /** Constant <code>VariableMod</code> */
    public static MZTabErrorType VariableMod = createError(Category.Logical, "VariableMod");

    /** Constant <code>PeptideSection</code> */
    public static MZTabErrorType PeptideSection = createWarn(Category.Logical, "PeptideSection");

    /** Constant <code>QuantificationAbundance</code> */
    public static MZTabErrorType QuantificationAbundance = createError(Category.Logical, "QuantificationAbundance");
    /** Constant <code>DuplicationID</code> */
    public static MZTabErrorType DuplicationID = createError(Category.Logical, "DuplicationID");

    /** Constant <code>ColumnNotValid</code> */
    public static MZTabErrorType ColumnNotValid = createError(Category.Logical, "ColumnNotValid");
    /** Constant <code>HeaderNotValid</code> */
    public static MZTabErrorType HeaderNotValid = createError(Category.Logical, "HeaderNotValid");
    
    /** Constant <code>NoSmallMoleculeEvidenceSection</code> */
    public static MZTabErrorType NoSmallMoleculeEvidenceSection = createInfo(Category.Logical, "NoSmallMoleculeEvidenceSection");
    /** Constant <code>NoSmallMoleculeFeatureSection</code> */
    public static MZTabErrorType NoSmallMoleculeFeatureSection = createInfo(Category.Logical, "NoSmallMoleculeFeatureSection");
    /** Constant <code>NoSmallMoleculeSummarySection</code> */
    public static MZTabErrorType NoSmallMoleculeSummarySection = createWarn(Category.Logical, "NoSmallMoleculeSummarySection");
    
    /** Constant <code>NoSmallMoleculeQuantificationUnit</code> */
    public static MZTabErrorType NoSmallMoleculeQuantificationUnit = createError(Category.Logical, "NoSmallMoleculeQuantificationUnit");
    /** Constant <code>NoSmallMoleculeFeatureQuantificationUnit</code> */
    public static MZTabErrorType NoSmallMoleculeFeatureQuantificationUnit = createError(Category.Logical, "NoSmallMoleculeFeatureQuantificationUnit");
    /** Constant <code>NoSmallMoleculeIdentificationReliability</code> */
    public static MZTabErrorType NoSmallMoleculeIdentificationReliability = createInfo(Category.Logical, "NoSmallMoleculeIdentificationReliability");

    /** Constant <code>ExternalStudyIdFormatNotDefined</code> */
    public static MZTabErrorType ExternalStudyIdFormatNotDefined = createError(Category.Logical, "ExternalStudyIdFormatNotDefined");
    /** Constant <code>ExternalStudyFormatNotDefined</code> */
    public static MZTabErrorType ExternalStudyFormatNotDefined = createError(Category.Logical, "ExternalStudyFormatNotDefined");

}

package uk.ac.ebi.pride.jmztab2.utils.errors;

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
    public static final MZTabErrorType NULL = createError(Category.Logical, "NULL");
    /** Constant <code>NotNULL</code> */
    public static final MZTabErrorType NotNULL = createWarn(Category.Logical, "NotNULL");

    /** Constant <code>LineOrder</code> */
    public static final MZTabErrorType LineOrder = createError(Category.Logical, "LineOrder");
    /** Constant <code>HeaderLine</code> */
    public static final MZTabErrorType HeaderLine = createError(Category.Logical, "HeaderLine");
    /** Constant <code>NoHeaderLine</code> */
    public static final MZTabErrorType NoHeaderLine = createError(Category.Logical, "NoHeaderLine");

    // not defined in metadata.
    /** Constant <code>MsRunNotDefined</code> */
    public static final MZTabErrorType MsRunNotDefined = createError(Category.Logical, "MsRunNotDefined");
    /** Constant <code>AssayNotDefined</code> */
    public static final MZTabErrorType AssayNotDefined = createError(Category.Logical, "AssayNotDefined");
    /** Constant <code>StudyVariableNotDefined</code> */
    public static final MZTabErrorType StudyVariableNotDefined = createError(Category.Logical, "StudyVariableNotDefined");
    /** Constant <code>ProteinSearchEngineScoreNotDefined</code> */
    public static final MZTabErrorType ProteinSearchEngineScoreNotDefined = createWarn(Category.Logical, "ProteinSearchEngineScoreNotDefined");
    /** Constant <code>PeptideSearchEngineScoreNotDefined</code> */
    public static final MZTabErrorType PeptideSearchEngineScoreNotDefined = createWarn(Category.Logical, "PeptideSearchEngineScoreNotDefined");
    /** Constant <code>PSMSearchEngineScoreNotDefined</code> */
    public static final MZTabErrorType PSMSearchEngineScoreNotDefined = createWarn(Category.Logical, "PSMSearchEngineScoreNotDefined");
    /** Constant <code>SmallMoleculeSearchEngineScoreNotDefined</code> */
    public static final MZTabErrorType SmallMoleculeSearchEngineScoreNotDefined = createWarn(Category.Logical, "SmallMoleculeSearchEngineScoreNotDefined");

    /** Constant <code>MsRunHashMethodNotDefined</code> */
    public static final MZTabErrorType MsRunHashMethodNotDefined = createError(Category.Logical, "MsRunHashMethodNotDefined");

    /** Constant <code>NotDefineInMetadata</code> */
    public static final MZTabErrorType NotDefineInMetadata = createError(Category.Logical, "NotDefineInMetadata");
    /** Constant <code>NotDefineInHeader</code> */
    public static final MZTabErrorType NotDefineInHeader = createError(Category.Logical, "NotDefineInHeader");
    /** Constant <code>DuplicationDefine</code> */
    public static final MZTabErrorType DuplicationDefine = createError(Category.Logical, "DuplicationDefine");
    /** Constant <code>DuplicationAccession</code> */
    public static final MZTabErrorType DuplicationAccession = createError(Category.Logical, "DuplicationAccession");
    /** Constant <code>AssayRefs</code> */
    public static final MZTabErrorType AssayRefs = createError(Category.Logical, "AssayRefs");

    /** Constant <code>ProteinCoverage</code> */
    public static final MZTabErrorType ProteinCoverage = createError(Category.Logical, "ProteinCoverage");
    /** Constant <code>IdNumber</code> */
    public static final MZTabErrorType IdNumber = createError(Category.Logical, "IdNumber");
    /** Constant <code>ModificationPosition</code> */
    public static final MZTabErrorType ModificationPosition = createError(Category.Logical, "ModificationPosition");
    /** Constant <code>CHEMMODS</code> */
    public static final MZTabErrorType CHEMMODS = createWarn(Category.Logical, "CHEMMODS");
    /** Constant <code>SubstituteIdentifier</code> */
    public static final MZTabErrorType SubstituteIdentifier = createError(Category.Logical, "SubstituteIdentifier");
    /** Constant <code>SoftwareVersion</code> */
    public static final MZTabErrorType SoftwareVersion = createWarn(Category.Logical, "SoftwareVersion");

    /** Constant <code>AbundanceColumnTogether</code> */
    public static final MZTabErrorType AbundanceColumnTogether = createError(Category.Logical, "AbundanceColumnTogether");
    /** Constant <code>AbundanceColumnSameId</code> */
    public static final MZTabErrorType AbundanceColumnSameId = createError(Category.Logical, "AbundanceColumnSameId");

    /** Constant <code>SpectraRef</code> */
    public static final MZTabErrorType SpectraRef = createWarn(Category.Logical, "SpectraRef");
    /** Constant <code>AmbiguityMod</code> */
    public static final MZTabErrorType AmbiguityMod = createWarn(Category.Logical, "AmbiguityMod");
    /** Constant <code>MsRunLocation</code> */
    public static final MZTabErrorType MsRunLocation = createWarn(Category.Logical, "MsRunLocation");

    /** Constant <code>FixedMod</code> */
    public static final MZTabErrorType FixedMod = createError(Category.Logical, "FixedMod");
    /** Constant <code>VariableMod</code> */
    public static final MZTabErrorType VariableMod = createError(Category.Logical, "VariableMod");

    /** Constant <code>PeptideSection</code> */
    public static final MZTabErrorType PeptideSection = createWarn(Category.Logical, "PeptideSection");

    /** Constant <code>QuantificationAbundance</code> */
    public static final MZTabErrorType QuantificationAbundance = createError(Category.Logical, "QuantificationAbundance");
    /** Constant <code>DuplicationID</code> */
    public static final MZTabErrorType DuplicationID = createError(Category.Logical, "DuplicationID");

    /** Constant <code>ColumnNotValid</code> */
    public static final MZTabErrorType ColumnNotValid = createError(Category.Logical, "ColumnNotValid");
    /** Constant <code>HeaderNotValid</code> */
    public static final MZTabErrorType HeaderNotValid = createError(Category.Logical, "HeaderNotValid");
    
    /** Constant <code>NoMetadataSection</code> */
    public static final MZTabErrorType NoMetadataSection = createError(Category.Logical, "NoMetadataSection");
    /** Constant <code>NoSmallMoleculeEvidenceSection</code> */
    public static final MZTabErrorType NoSmallMoleculeEvidenceSection = createInfo(Category.Logical, "NoSmallMoleculeEvidenceSection");
    /** Constant <code>NoSmallMoleculeFeatureSection</code> */
    public static final MZTabErrorType NoSmallMoleculeFeatureSection = createInfo(Category.Logical, "NoSmallMoleculeFeatureSection");
    /** Constant <code>NoSmallMoleculeSummarySection</code> */
    public static final MZTabErrorType NoSmallMoleculeSummarySection = createError(Category.Logical, "NoSmallMoleculeSummarySection");
    
    /** Constant <code>NoSmallMoleculeQuantificationUnit</code> */
    public static final MZTabErrorType NoSmallMoleculeQuantificationUnit = createError(Category.Logical, "NoSmallMoleculeQuantificationUnit");
    /** Constant <code>NoSmallMoleculeFeatureQuantificationUnit</code> */
    public static final MZTabErrorType NoSmallMoleculeFeatureQuantificationUnit = createError(Category.Logical, "NoSmallMoleculeFeatureQuantificationUnit");
    /** Constant <code>NoSmallMoleculeIdentificationReliability</code> */
    public static final MZTabErrorType NoSmallMoleculeIdentificationReliability = createInfo(Category.Logical, "NoSmallMoleculeIdentificationReliability");

    /** Constant <code>ExternalStudyIdFormatNotDefined</code> */
    public static final MZTabErrorType ExternalStudyIdFormatNotDefined = createError(Category.Logical, "ExternalStudyIdFormatNotDefined");
    /** Constant <code>ExternalStudyFormatNotDefined</code> */
    public static final MZTabErrorType ExternalStudyFormatNotDefined = createError(Category.Logical, "ExternalStudyFormatNotDefined");
    
    /** Constant <code>NoDatabaseMustHaveNullPrefix</code> */
    public static final MZTabErrorType NoDatabaseMustHaveNullPrefix = createError(Category.Logical, "NoDatabaseMustHaveNullPrefix");
    
    /** Constant <code>ItemNumberMismatch</code> */
    public static final MZTabErrorType ItemNumberMismatch = createError(Category.Logical, "ItemNumberMismatch");

    /** Constant <code>UnknownRefId</code> */
    public static final MZTabErrorType UnknownRefId = createError(Category.Logical, "UnknownRefId");

}

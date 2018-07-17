package uk.ac.ebi.pride.jmztab2.utils.errors;

/**
 * Provide crosscheck service, that is parse the consistent between current mztab file and
 * some other resource (eg, database, xml file and so on).
 *
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 29/01/13
 * 
 */
public final class CrossCheckErrorType extends MZTabErrorType {
    
    private CrossCheckErrorType() {
        
    }
    
    /** Constant <code>Species</code> */
    public static MZTabErrorType Species = createWarn(Category.CrossCheck, "Species");

    /** Constant <code>CvTermNotAllowed</code> */
    public static MZTabErrorType CvTermNotAllowed = createWarn(Category.CrossCheck, "CvTermNotAllowed");
    
    /** Constant <code>CvTermRequired</code> */
    public static MZTabErrorType CvTermRequired = createError(Category.CrossCheck, "CvTermRequired");
    
    /** Constant <code>CvTermRecommended</code> */
    public static MZTabErrorType CvTermRecommended = createWarn(Category.CrossCheck, "CvTermRecommended");
    
    /** Constant <code>CvTermOptional</code> */
    public static MZTabErrorType CvTermOptional = createInfo(Category.CrossCheck, "CvTermOptional");
    
    /** Constant <code>CvTermNotInRule</code> */
    public static MZTabErrorType CvTermNotInRule = createWarn(Category.CrossCheck, "CvTermNotInRule");
    
    /** *  Constant <code>RulePointerObjectNullRequired</code> */
    public static MZTabErrorType RulePointerObjectNullRequired = createError(Category.CrossCheck, "RulePointerObjectNull");
    
    /** *  Constant <code>RulePointerObjectNullRecommended</code> */
    public static MZTabErrorType RulePointerObjectNullRecommended = createWarn(Category.CrossCheck, "RulePointerObjectNull");
    
    /** *  Constant <code>RulePointerObjectNullOptional</code> */
    public static MZTabErrorType RulePointerObjectNullOptional = createInfo(Category.CrossCheck, "RulePointerObjectNull");

    /** Constant <code>CvTermXor</code> */
    public static MZTabErrorType CvTermXor = createError(Category.CrossCheck, "CvTermXor");
    
    /** Constant <code>CvTermMalformed</code> */
    public static MZTabErrorType CvTermMalformed = createError(Category.CrossCheck, "CvTermMalformed");

    /** Constant <code>CvUndefinedInMetadata</code> */
    public static MZTabErrorType CvUndefinedInMetadata = createError(Category.CrossCheck, "CvUndefinedInMetadata");
    
    /** Constant <code>CvUnused</code> */
    public static MZTabErrorType CvUnused = createWarn(Category.CrossCheck, "CvUnused");
    
    /** Constant <code>SemanticValidationException</code> */
    public static MZTabErrorType SemanticValidationException = createError(Category.CrossCheck, "SemanticValidationException");

}

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
    
    /** Constant <code>RulePointerObjectNull</code> */
    public static MZTabErrorType RulePointerObjectNull = createError(Category.CrossCheck, "RulePointerObjectNull");

}

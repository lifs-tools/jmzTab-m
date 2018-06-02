package uk.ac.ebi.pride.jmztab1_1.utils.errors;

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
    public static MZTabErrorType CvTermNotAllowed = createWarn(Category.Logical, "CvTermNotAllowed");
    
    /** Constant <code>CvTermRequired</code> */
    public static MZTabErrorType CvTermRequired = createError(Category.Logical, "CvTermRequired");
    
    /** Constant <code>CvTermRecommended</code> */
    public static MZTabErrorType CvTermRecommended = createWarn(Category.Logical, "CvTermRecommended");
    
    /** Constant <code>CvTermOptional</code> */
    public static MZTabErrorType CvTermOptional = createInfo(Category.Logical, "CvTermOptional");

}

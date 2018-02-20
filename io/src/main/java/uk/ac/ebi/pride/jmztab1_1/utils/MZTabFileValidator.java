package uk.ac.ebi.pride.jmztab1_1.utils;

import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;

/**
 * Provide semantic validation for mzTab file.
 *
 * @author nilshoffmann
 * 
 */
public class MZTabFileValidator {
    private MZTabErrorList errorList;

    /**
     * <p>Constructor for MZTabFileValidator.</p>
     *
     * @param errorList a {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList} object.
     */
    public MZTabFileValidator(MZTabErrorList errorList) {
        this.errorList = errorList == null ? new MZTabErrorList() : errorList;
    }


}

package uk.ac.ebi.pride.jmztab1_1.utils.errors;

/**
 * Wrap a {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError}. This exception mainly used in parse the metadata section of mzTab file.
 * Once raise exception, system will stop validate and output the error messages.
 *
 * NOTICE: In some special situation, the consistency constraints SHOULD be maintain in metadata section,
 * for example: the assay[n]-sample_ref, study_variable[1-n]-assay_refs and so on. We suggest user raise
 * this exception very carefully, because of this break the continuous validate principle. During process
 * the value format, system will add the {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError} into {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList}, instead of raise
 * the exception directly. And all errors will output after validate the whole mzTab file.
 *
 * @author qingwei
 * @since 29/01/13
 * 
 */
public class MZTabException extends Exception {
    private MZTabError error;

    /**
     * <p>Constructor for MZTabException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public MZTabException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for MZTabException.</p>
     *
     * @param error a {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError} object.
     */
    public MZTabException(MZTabError error) {
        super(error.toString());
        this.error = error;
    }

    /**
     * <p>Constructor for MZTabException.</p>
     *
     * @param error a {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public MZTabException(MZTabError error, Throwable cause) {
        super(error.toString(), cause);
        this.error = error;
    }

    /**
     * <p>Getter for the field <code>error</code>.</p>
     *
     * @return a {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError} object.
     */
    public MZTabError getError() {
        return error;
    }
}

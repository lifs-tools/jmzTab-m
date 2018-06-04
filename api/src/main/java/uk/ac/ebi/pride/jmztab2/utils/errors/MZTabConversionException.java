package uk.ac.ebi.pride.jmztab2.utils.errors;

/**
 * Runtime exception for mzTab conversion
 *
 * @author Rui Wang
 */
public class MZTabConversionException extends RuntimeException {

    /** Constant <code>ERROR_AMBIGUITY="mzTab do not support one protein in mor"{trunked}</code> */
    public static String ERROR_AMBIGUITY = "mzTab do not support one protein in more than one ambiguity groups.";

    /**
     * <p>Constructor for MZTabConversionException.</p>
     */
    public MZTabConversionException() {
    }

    /**
     * <p>Constructor for MZTabConversionException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public MZTabConversionException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for MZTabConversionException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause a {@link java.lang.Throwable} object.
     */
    public MZTabConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for MZTabConversionException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public MZTabConversionException(Throwable cause) {
        super(cause);
    }
}

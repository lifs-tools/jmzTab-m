package uk.ac.ebi.pride.jmztab2.utils.errors;

import org.lifstools.mztab2.model.ValidationMessage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import static uk.ac.ebi.pride.jmztab2.utils.MZTabProperties.MAX_ERROR_COUNT;

/**
 * A limit max capacity list, if contains a couple of
 * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError} objects. If overflow,
 * system will raise
 * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException}.
 * Besides this, during add a new
 * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError} object, it's
 * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType#level} SHOULD
 * equal or great than its level setting.
 *
 * @author qingwei
 * @since 29/01/13
 *
 */
public class MZTabErrorList {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        MZTabErrorList.class);

    private int maxErrorCount;
    private List<MZTabError> errorList;
    private MZTabErrorType.Level level;

    /**
     * Generate a error list, which max size is
     * {@link uk.ac.ebi.pride.jmztab2.utils.MZTabProperties#MAX_ERROR_COUNT},
     * and only allow
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Level#Error}
     * or greater level errors to be added into list.
     */
    public MZTabErrorList() {
        this(MZTabErrorType.Level.Error);
    }

    /**
     * Generate a error list, which max size is
     * {@link uk.ac.ebi.pride.jmztab2.utils.MZTabProperties#MAX_ERROR_COUNT}
     *
     * @param level if null, default level is
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Level#Error}
     */
    public MZTabErrorList(MZTabErrorType.Level level) {
        this(level, MAX_ERROR_COUNT);
    }

    /**
     * Generate a error list, with given error level and maximum error count.
     *
     * @param level if null, default level is
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType.Level#Error}
     * @param maxErrorCount the maximum number of errors recorded by this list
     * before an
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException}
     * is thrown
     */
    public MZTabErrorList(MZTabErrorType.Level level, int maxErrorCount) {
        this.level = level == null ? MZTabErrorType.Level.Error : level;
        this.maxErrorCount = maxErrorCount >= 0 ? maxErrorCount : 0;
        this.errorList = new ArrayList<MZTabError>(this.maxErrorCount);
    }

    /**
     * Unmodifiable list of errors
     *
     * @return error list
     */
    public List<MZTabError> getErrorList() {
        return Collections.unmodifiableList(errorList);
    }

    /**
     * A limit max capacity list, if contains a couple of
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError} objects. If
     * overflow, system will raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException}.
     * Besides this, during add a new
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError} object, it's
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType#level} SHOULD
     * equal or greater than its level setting.
     *
     * @param error SHOULD NOT set null
     * @return a boolean, false if the element could not be added, true on success
     * @throws uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException
     * if any.
     */
    public boolean add(MZTabError error) throws MZTabErrorOverflowException {
        if (error == null) {
            throw new NullPointerException("Can not add a null error into list.");
        }
        
        switch(error.getType().getLevel()) {
            case Info -> {
                if(level==MZTabErrorType.Level.Warn || level==MZTabErrorType.Level.Error) {
                    return false;
                }
            }
            case Warn -> {
                if(level==MZTabErrorType.Level.Error) {
                    return false;
                }
                //ERROR is always being reported
            }
            //ERROR is always being reported
        }

        if (errorList.size() >= maxErrorCount) {
            LOGGER.error("Max error count of {} reached!", maxErrorCount);
            LOGGER.error("{}", this.toString());
            throw new MZTabErrorOverflowException();
        }

        return errorList.add(error);
    }
    
    /**
     * A limit max capacity list, if contains a couple of
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError} objects. If
     * overflow, system will raise
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException}.
     * Besides this, during add a new
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError} object, it's
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType#level} SHOULD
     * equal or greater than its level setting.
     *
     * @param errors the list of MZTabError objects, must not be null
     * @return a boolean, false no elements were added, true otherwise.
     * @throws uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException
     * if any.
     */
    public boolean addAll(List<MZTabError> errors) throws MZTabErrorOverflowException {
        if (errors == null) {
            throw new NullPointerException("Can not add a null list of errors.");
        }
        return errors.stream().map((t) -> {
            return errorList.add(t);
        }).collect(Collectors.reducing((Boolean t, Boolean u) -> t || u)).orElse(Boolean.FALSE);
    }

    /**
     * <p>
     * Getter for the field <code>maxErrorCount</code>.</p>
     *
     * @return The maximum number of errors that are going to be reported before
     * the parser stops with an
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException}
     */
    public int getMaxErrorCount() {
        return maxErrorCount;
    }

    /**
     * Define the maximum number of errors recorded by this list before an
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorOverflowException}
     * is thrown
     *
     * @param maxErrorCount needs to be a positive number or zero will be set
     */
    public void setMaxErrorCount(int maxErrorCount) {
        this.maxErrorCount = maxErrorCount >= 0 ? maxErrorCount : 0;
    }

    /**
     * <p>
     * Getter for the field <code>level</code>.</p>
     *
     * @return level of errors reported
     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType#level}
     */
    public MZTabErrorType.Level getLevel() {
        return level;
    }

//    /**
//     * Define the level of the errors that are going to be store in the list.
//     * The incoming errors with an equal or highest level will be stored.
//     *
//     * @param level
//     * {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType#level}
//     */
//    public void setLevel(MZTabErrorType.Level level) {
//        this.level = level;
//    }

    /**
     * Clear all errors stored in the error list.
     */
    public void clear() {
        errorList.clear();
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return a int.
     */
    public int size() {
        return errorList.size();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return a {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError} object.
     */
    public MZTabError getError(int index) {
        return errorList.get(index);
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return a boolean.
     */
    public boolean isEmpty() {
        return errorList.isEmpty();
    }

    /**
     * Print error list to output stream.
     *
     * @param out SHOULD NOT set null.
     * @throws java.io.IOException if any.
     */
    public void print(OutputStream out) throws IOException {
        if (out == null) {
            throw new NullPointerException("Output stream should be set first.");
        }

        for (MZTabError e : errorList) {
            out.write(e.toString().
                getBytes());
        }
    }

    /**
     * Print error list to string.
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (MZTabError error : errorList) {
            sb.append(error).
                append(MZTabConstants.NEW_LINE);
        }

        return sb.toString();
    }

    /**
     * Converts the provided error list to a list of validation messages.
     *
     * @param errorList the error list to convert.
     * @return a list of validation messages.
     * @throws IllegalStateException for unhandled mappings of
     * {@link ValidationMessage.MessageTypeEnum}.
     */
    public static List<ValidationMessage> convertToValidationMessages(
        MZTabErrorList errorList) throws IllegalStateException {
        List<ValidationMessage> validationResults = new ArrayList<>(
            errorList.size());
        for (MZTabError error : errorList.getErrorList()) {
            ValidationMessage vr = error.toValidationMessage();
            LOGGER.debug(vr.toString());
            validationResults.add(vr);
        }
        return validationResults;
    }

    /**
     * Converts this error list to a list of validation messages.
     *
     * @return a list of validation messages.
     * @throws IllegalStateException for unhandled mappings of
     * {@link ValidationMessage.MessageTypeEnum}.
     */
    public List<ValidationMessage> convertToValidationMessages() throws IllegalStateException {
        return convertToValidationMessages(this);
    }
}

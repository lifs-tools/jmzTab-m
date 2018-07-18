package uk.ac.ebi.pride.jmztab2.utils.errors;

import de.isas.mztab2.model.ValidationMessage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;

/**
 * mzTab files can be validated to ensure that they comply with the latest
 * version of the format specification. The process includes two steps: first of
 * all the basic model architecture is created, including the metadata section
 * and the generation of the table column headers. The second step is the
 * validation of the column rows, which take most of the processing time. The
 * class MZTabFileParser is used to parse and validate the mzTab files. If the
 * validation is successful, an MZTabFile model will be then generated. A series
 * of messages are then reported, which can help to diagnose different types of
 * format-related (reporting format problems) and/or logical (reporting errors
 * related to the logical relationships among the different sections in a file)
 * errors. At the moment of writing, there are about sixty types of error
 * messages (http://mztab.googlecode.com/wiki/jmzTab_message). The validation
 * messages have a unique identifier and are classified in three levels: Info,
 * Warn and Error, according to the requirements included in the specification
 * document.
 *
 * @author qingwei
 * @since 06/02/13
 * 
 */
public class MZTabError implements Serializable {

    private int lineNumber;
    private MZTabErrorType type;
    private String message;

    /**
     * System will fill a couple of values one by one, and generate a concrete
     * error message during parse {@link #lineNumber} line in mzTab file.
     *
     * @param type SHOULD NOT null.
     * @param lineNumber SHOULD be positive integer. Except "-1", which means
     * the line number unknown.
     * @param values May be null, if no variable in error's original pattern.
     */
    public MZTabError(MZTabErrorType type, int lineNumber, String... values) {
        if (type == null) {
            throw new NullPointerException("MZTabErrorType should not set null");
        }
        this.type = type;

        this.lineNumber = lineNumber;

        List<String> valueList = new ArrayList<String>();
        for (String value : values) {
            valueList.add(value == null ? "" : value);
        }

        this.message = fill(0, valueList, type.getOriginal());
    }

    /**
     * fill "{id}" parameter list one by one.
     */
    private String fill(int count, List<String> values, String message) {
        String regexp = "\\{\\w\\}";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(message);

        String value;
        if (matcher.find()) {
            if (count >= values.size()) {
                throw new ArrayIndexOutOfBoundsException(
                    "Tried to replace placeholder " + (count + 1) + " but only " + values.
                        size() + " values are available for " + getClass().
                        getSimpleName() + " " + type.toString());
            }
            value = values.get(count);
            message = matcher.replaceFirst(Matcher.quoteReplacement(value));
            return fill(count + 1, values, message);
        } else {
            return message;
        }
    }

    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType}
     * @see FormatErrorType
     * @see LogicalErrorType
     */
    public MZTabErrorType getType() {
        return type;
    }

    /**
     * <p>Getter for the field <code>message</code>.</p>
     *
     * @return a concrete error/warn message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * <p>Getter for the field <code>lineNumber</code>.</p>
     *
     * @return the line number.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * {@inheritDoc}
     *
     * Code: Unique number for error/warn Category: Currently, there are three
     * types of messages: Format, Logical Original: Message expression pattern.
     * "{?}" is a couple of parameters which can be filled during validate
     * processing. Cause: A readable text to describe the reason why raise this
     * error/warn. Currently, these cause message coming from mztab
     * specification mainly.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[").
            append(type.getLevel()).
            append("-").
            append(type.getCode()).
            append("] ");
        sb.append("line ").
            append(lineNumber).
            append(": ");
        sb.append(message).
            append(NEW_LINE);

        return sb.toString();
    }
    
    public ValidationMessage toValidationMessage() throws IllegalStateException {
        ValidationMessage.MessageTypeEnum level = ValidationMessage.MessageTypeEnum.INFO;
        switch (getType().
            getLevel()) {
            case Error:
                level = ValidationMessage.MessageTypeEnum.ERROR;
                break;
            case Info:
                level = ValidationMessage.MessageTypeEnum.INFO;
                break;
            case Warn:
                level = ValidationMessage.MessageTypeEnum.WARN;
                break;
            default:
                throw new IllegalStateException("State " + 
                    getType().
                    getLevel() + " is not handled in switch/case statement!");
        }
        ValidationMessage.CategoryEnum category = ValidationMessage.CategoryEnum.FORMAT;
        switch(getType().getCategory()) {
            case Format:
                category = ValidationMessage.CategoryEnum.FORMAT;
                break;
            case Logical:
                category = ValidationMessage.CategoryEnum.LOGICAL;
                break;
            case CrossCheck:
                category = ValidationMessage.CategoryEnum.CROSS_CHECK;
                break;
            default:
                throw new IllegalStateException("Category " + 
                    getType().
                    getCategory()+ " is not handled in switch/case statement!");
        }
        ValidationMessage vr = new ValidationMessage().lineNumber(
            Long.valueOf(getLineNumber())).
            category(category).
            messageType(level).
            message(getMessage()).
            code(toString());
        return vr;
    }
}

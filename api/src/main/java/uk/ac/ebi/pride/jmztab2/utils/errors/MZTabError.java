package uk.ac.ebi.pride.jmztab2.utils.errors;

import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationMessage;
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
    private final List<String> values;
    // ValidationLocation is not Serializable; the location is not carried across serialization.
    private transient ValidationLocation location;
    private String column;
    private String ruleId;
    private String fixElement;
    private String fixAllowed;

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

        this.values = valueList;
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

    private static final Pattern FIX_PLACEHOLDER = Pattern.compile("\\{(\\d|element|allowed)\\}");

    /**
     * Fill a fix template by index: {0}..{9} are replaced with
     * <code>values.get(n)</code> (empty string if out of range), {element} with
     * <code>element</code> (default "the element") and {allowed} with
     * <code>allowed</code> (default "the terms listed in the validation profile").
     *
     * @param template the fix template.
     * @param values the original's parameters.
     * @param element the element the fix refers to, may be null.
     * @param allowed a description of the allowed values, may be null.
     * @return the filled text.
     */
    public static String fillFix(String template, List<String> values, String element, String allowed) {
        Matcher matcher = FIX_PLACEHOLDER.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String replacement;
            if ("element".equals(name)) {
                replacement = element != null ? element : "the element";
            } else if ("allowed".equals(name)) {
                replacement = allowed != null ? allowed : "the terms listed in the validation profile";
            } else {
                int n = Integer.parseInt(name);
                replacement = n < values.size() ? values.get(n) : "";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public MZTabError location(ValidationLocation location) {
        this.location = location;
        return this;
    }

    public ValidationLocation getLocation() {
        return location;
    }

    public MZTabError column(String column) {
        this.column = column;
        return this;
    }

    public String getColumn() {
        return column;
    }

    public MZTabError ruleId(String ruleId) {
        this.ruleId = ruleId;
        return this;
    }

    public String getRuleId() {
        return ruleId;
    }

    /**
     * Set the named extras used by the fix template's {element} and {allowed}.
     */
    public MZTabError fixContext(String element, String allowed) {
        this.fixElement = element;
        this.fixAllowed = allowed;
        return this;
    }

    /**
     * @return the filled fix text, or null if the type has no fix template.
     */
    public String getSuggestion() {
        String template = type.getFix();
        return template == null ? null : fillFix(template, values, fixElement, fixAllowed);
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
            case Error -> level = ValidationMessage.MessageTypeEnum.ERROR;
            case Info -> level = ValidationMessage.MessageTypeEnum.INFO;
            case Warn -> level = ValidationMessage.MessageTypeEnum.WARN;
            default -> throw new IllegalStateException("State " + 
                    getType().
                    getLevel() + " is not handled in switch/case statement!");
        }
        ValidationMessage.CategoryEnum category = ValidationMessage.CategoryEnum.FORMAT;
        switch(getType().getCategory()) {
            case Format -> category = ValidationMessage.CategoryEnum.FORMAT;
            case Logical -> category = ValidationMessage.CategoryEnum.LOGICAL;
            case CrossCheck -> category = ValidationMessage.CategoryEnum.CROSS_CHECK;
            default -> throw new IllegalStateException("Category " + 
                    getType().
                    getCategory()+ " is not handled in switch/case statement!");
        }
        ValidationMessage vr = new ValidationMessage().lineNumber(
            Long.valueOf(getLineNumber())).
            category(category).
            messageType(level).
            message(getMessage()).
            code(toString()).
            suggestion(getSuggestion()).
            ruleId(getRuleId()).
            location(getLocation());
        return vr;
    }
}

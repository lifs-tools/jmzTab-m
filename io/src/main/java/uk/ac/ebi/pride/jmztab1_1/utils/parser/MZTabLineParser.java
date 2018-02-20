package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import uk.ac.ebi.pride.jmztab1_1.model.Section;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;

import static uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants.TAB;

/**
 * Common tab split line parser. If there exists format or logical errors during the parse process,
 * system will add them into {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList}, or break validate and throw {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException}
 * directly.
 *
 * @see MZTabHeaderLineParser
 * @see MZTabDataLineParser
 * @see MTDLineParser
 * @author qingwei
 * @since 10/02/13
 * 
 */
public class MZTabLineParser {

    protected int lineNumber;
    protected Section section;
    protected String line;

    /**
     * based on TAB char to split raw line into String array.
     */
    protected String[] items;

    protected final MZTabParserContext context;
    protected MZTabErrorList errorList;
    
    /**
     * <p>Constructor for MZTabLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab1_1.utils.parser.MZTabParserContext} object.
     */
    protected MZTabLineParser(MZTabParserContext context) {
        if (context == null) {
            throw new NullPointerException("Parser context should be created first!");
        }
        this.context = context;
    }

    /**
     * We assume that user before call this method, have parse the raw line
     * is not empty line and start with section prefix.
     *
     * @param lineNumber a int.
     * @param line a {@link java.lang.String} object.
     * @param errorList a {@link uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList} object.
     * @throws uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException if any.
     */
    protected void parse(int lineNumber, String line, MZTabErrorList errorList) throws MZTabException {
        this.lineNumber = lineNumber;
        this.line = line;
        this.errorList = errorList == null ? new MZTabErrorList() : errorList;

        this.items = line.split("\\s*" + TAB + "\\s*");
        items[0] = items[0].trim();
        items[items.length - 1] = items[items.length - 1].trim();

        section = Section.findSection(items[0]);

        if (section == null) {
            MZTabError error = new MZTabError(FormatErrorType.LinePrefix, lineNumber, items[0]);
            this.errorList.add(error);
        }
    }
}

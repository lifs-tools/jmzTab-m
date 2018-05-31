package uk.ac.ebi.pride.jmztab1_1.utils.parser;

import de.isas.mztab1_1.model.Comment;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab1_1.utils.errors.MZTabException;

/**
 * Comment line parser.
 * Comment lines can be placed anywhere in an mzTab file. These lines must start with the three-letter
 * code COM and are ignored by most parsers. Empty lines can also occur anywhere in an mzTab file and are ignored.
 *
 * @see MZTabLineParser
 * @author qingwei
 * @since 10/02/13
 * 
 */
public class COMLineParser extends MZTabLineParser {

    /**
     * <p>Constructor for COMLineParser.</p>
     *
     * @param context a {@link uk.ac.ebi.pride.jmztab1_1.utils.parser.MZTabParserContext} object.
     */
    public COMLineParser(MZTabParserContext context) {
        super(context);
    }

    /** {@inheritDoc} */
    @Override
    public void parse(int lineNumber, String line, MZTabErrorList errorList) throws MZTabException {
        super.parse(lineNumber, line, errorList);
    }

    /**
     * <p>getComment.</p>
     *
     * @return a {@link de.isas.mztab1_1.model.Comment} object.
     */
    public Comment getComment() {
        String msg = items.length == 1 ? "" : items[1];
        return new Comment().msg(msg).lineNumber(lineNumber);
    }
}

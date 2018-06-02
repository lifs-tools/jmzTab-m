/* 
 * Copyright 2018 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

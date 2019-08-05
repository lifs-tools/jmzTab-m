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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB;
import uk.ac.ebi.pride.jmztab2.model.Section;
import uk.ac.ebi.pride.jmztab2.utils.errors.FormatErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Common tab split line parser. If there exists format or logical errors during the parse process,
 * system will add them into {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList}, or break validate and throw {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException}
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
     * @param context a {@link uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext} object.
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
     * @param errorList a {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList} object.
     * @throws uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException if any.
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

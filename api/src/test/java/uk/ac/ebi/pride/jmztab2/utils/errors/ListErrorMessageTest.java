/*
 * Copyright 2026 LIFS Tools Consortium.
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
package uk.ac.ebi.pride.jmztab2.utils.errors;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The list format errors name the value and the split character, as the
 * parser passes them: column, value, split character (#243). MZTabError
 * fills the placeholders of a message in order, but the templates are also
 * read by index, as the fix texts are, so they must use the right index.
 */
public class ListErrorMessageTest {

    @ParameterizedTest
    @ValueSource(strings = {"StringList", "DoubleList", "IntegerList"})
    void messageNamesTheSplitChar(String name) throws Exception {
        MZTabErrorType type = (MZTabErrorType) FormatErrorType.class.getField(name).get(null);
        List<String> values = List.of("theoretical_neutral_mass", "1.0,2.0", "|");
        String byIndex = MZTabError.fillFix(type.getOriginal(), values, null, null);
        assertTrue(byIndex.startsWith("Column \"theoretical_neutral_mass\" value \"1.0,2.0\""), byIndex);
        assertTrue(byIndex.endsWith("The split char is \"|\""), byIndex);
        assertEquals(byIndex, new MZTabError(type, 7, values.toArray(new String[0])).getMessage());
        assertFalse(type.getCause().startsWith("="), type.getCause());
    }
}

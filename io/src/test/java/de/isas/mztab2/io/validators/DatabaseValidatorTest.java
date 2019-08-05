/*
 * Copyright 2019 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package de.isas.mztab2.io.validators;

import de.isas.mztab2.model.Database;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.Parameter;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 *
 * @author nilshoffmann
 */
public class DatabaseValidatorTest {

    /**
     * Test of validateRefine method for null database, of class DatabaseValidator.
     */
    @Test
    public void testValidateRefineNullDatabase() {
        Metadata metadata = new Metadata();
        MZTabParserContext parserContext = new MZTabParserContext();
        DatabaseValidator instance = new DatabaseValidator();
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.Properties.database + ""));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
    }

    /**
     * Test of validateRefine method for explicit no database, of class DatabaseValidator.
     */
    @Test
    public void testValidateRefineNoDatabase() {
        Metadata metadata = new Metadata();
        metadata.addDatabaseItem(new Database().id(1).uri("file:///my/illegal/db/uri").param(new Parameter().name("no database")).prefix("my illegal prefix"));
        MZTabParserContext parserContext = new MZTabParserContext();
        DatabaseValidator instance = new DatabaseValidator();
        List<MZTabError> expResult = Arrays.asList(new MZTabError(
                LogicalErrorType.NoDatabaseMustHaveNullPrefix,
                -1,
                1 + "", "my illegal prefix"), new MZTabError(
                LogicalErrorType.NotDefineInMetadata, -1,
                Metadata.Properties.database + "[" + 1 + "]-" + Database.Properties.uri),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.database + "[" + 1 + "]-" + Database.Properties.version));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
        assertEquals(expResult.get(1).toString(), result.get(1).toString());
        assertEquals(expResult.get(2).toString(), result.get(2).toString());
    }

    /**
     * Test of validateRefine method for a custom database, of class DatabaseValidator.
     */
    @Test
    public void testValidateRefineCustomDatabase() {
        Metadata metadata = new Metadata();
        metadata.addDatabaseItem(new Database().id(1).param(new Parameter().name("custom")).prefix("CUSTOM"));
        MZTabParserContext parserContext = new MZTabParserContext();
        DatabaseValidator instance = new DatabaseValidator();
        List<MZTabError> expResult = Arrays.asList(
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.database + "[" + 1 + "]-" + Database.Properties.uri),
                new MZTabError(
                        LogicalErrorType.NotDefineInMetadata, -1,
                        Metadata.Properties.database + "[" + 1 + "]-" + Database.Properties.version));
        List<MZTabError> result = instance.validateRefine(metadata, parserContext);
        assertEquals(expResult.size(), result.size());
        assertEquals(expResult.get(0).toString(), result.get(0).toString());
        assertEquals(expResult.get(1).toString(), result.get(1).toString());
    }

}

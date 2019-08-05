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

import de.isas.mztab2.model.CV;
import de.isas.mztab2.model.Database;
import de.isas.mztab2.model.Metadata;
import java.util.LinkedList;
import java.util.List;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.parser.MZTabParserContext;

/**
 * Validates that the database section is present and correctly populated in
 * metadata.
 *
 * @author nilshoffmann
 */
public class DatabaseValidator implements MetadataValidator<Metadata> {

    @Override
    public List<MZTabError> validateRefine(Metadata metadata, MZTabParserContext parserContext) {
        List<MZTabError> errorList = new LinkedList<>();
        if (metadata.getDatabase() == null || metadata.getDatabase().
                isEmpty()) {
            errorList.add(new MZTabError(
                    LogicalErrorType.NotDefineInMetadata, -1,
                    Metadata.Properties.database + ""));
        } else {
            for (Database db : metadata.getDatabase()) {
                if (db.getParam().
                        getName().
                        equals("no database")) {
                    if (db.getPrefix() != null && !db.getPrefix().
                            equals("null")) {
                        errorList.add(new MZTabError(
                                LogicalErrorType.NoDatabaseMustHaveNullPrefix,
                                -1,
                                db.getId() + "", db.getPrefix()));
                    }
                    if (db.getUri() != null && !db.getUri().
                            equals("null")) {
                        errorList.add(new MZTabError(
                                LogicalErrorType.NotDefineInMetadata, -1,
                                Metadata.Properties.database + "[" + db.getId() + "]-" + Database.Properties.uri));
                    }
                } else {
                    if (db.getUri() == null) {
                        errorList.add(new MZTabError(
                                LogicalErrorType.NotDefineInMetadata, -1,
                                Metadata.Properties.database + "[" + db.getId() + "]-" + Database.Properties.uri));
                    }
                }
                if (db.getVersion() == null) {
                    errorList.add(new MZTabError(
                            LogicalErrorType.NotDefineInMetadata, -1,
                            Metadata.Properties.database + "[" + db.getId() + "]-" + Database.Properties.version));
                }
            }
        }
        return errorList;
    }

}

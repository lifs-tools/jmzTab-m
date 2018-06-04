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
package de.isas.mztab2.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithNullProperty;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithProperty;
import de.isas.mztab2.model.Database;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>DatabaseSerializer class.</p>
 *
 * @author nilshoffmann
 * 
 */
public class DatabaseSerializer extends StdSerializer<Database> {

    /**
     * <p>Constructor for DatabaseSerializer.</p>
     */
    public DatabaseSerializer() {
        this(null);
    }

    /**
     * <p>Constructor for DatabaseSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public DatabaseSerializer(Class<Database> t) {
        super(t);
    }

    /** {@inheritDoc} */
    @Override
    public void serialize(Database database, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (database != null) {
            Serializers.addLineWithPropertyParameters(jg, Section.Metadata.getPrefix(),
                null, database, Arrays.asList(database.getParam()));
            if(database.getParam()!= null && database.getParam().getName().equals("no database")) {
                addLineWithNullProperty(jg, Section.Metadata.getPrefix(),
                    Database.Properties.prefix.getPropertyName(), database);
            } else {
                addLineWithProperty(jg, Section.Metadata.getPrefix(),
                    Database.Properties.prefix.getPropertyName(), database,
                    database.
                        getPrefix());
            }
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                Database.Properties.url.getPropertyName(), database,
                database.getUrl());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                Database.Properties.version.getPropertyName(), database,
                database.getVersion());
        } else {
            Logger.getLogger(DatabaseSerializer.class.getName()).
                log(Level.FINE, Database.class.getSimpleName()+" is null!");
        }
    }

}

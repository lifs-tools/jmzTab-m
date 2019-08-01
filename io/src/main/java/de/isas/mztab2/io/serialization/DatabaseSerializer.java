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
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithNullProperty;
import static de.isas.mztab2.io.serialization.Serializers.addLineWithProperty;
import de.isas.mztab2.model.Database;
import de.isas.mztab2.model.Parameter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.jmztab2.model.Section;

/**
 * <p>DatabaseSerializer implementation for {@link de.isas.mztab2.model.Database}.</p>
 *
 * @author nilshoffmann
 *
 */
@Slf4j
public class DatabaseSerializer extends StdSerializer<Database> {

    /**
     * <p>
     * Constructor for DatabaseSerializer.</p>
     */
    public DatabaseSerializer() {
        this(null);
    }

    /**
     * <p>
     * Constructor for DatabaseSerializer.</p>
     *
     * @param t a {@link java.lang.Class} object.
     */
    public DatabaseSerializer(Class<Database> t) {
        super(t);
    }

    @Override
    public void serializeWithType(Database value, JsonGenerator gen,
        SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Database database, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (database != null) {
            Serializers.checkIndexedElement(database);
            Serializers.addLineWithPropertyParameters(jg, Section.Metadata.
                getPrefix(),
                null, database, Arrays.asList(database.getParam()));
            Optional<Parameter> dbParam = Optional.ofNullable(database.getParam());
            if (dbParam.isPresent() && dbParam.get().
                getName().
                equals("no database")) {
                addLineWithNullProperty(jg, Section.Metadata.getPrefix(),
                    Database.Properties.prefix.getPropertyName(), database);
                addLineWithProperty(jg, Section.Metadata.getPrefix(),
                    Database.Properties.uri.getPropertyName(), database,
                    "null");
            } else {
                addLineWithProperty(jg, Section.Metadata.getPrefix(),
                    Database.Properties.prefix.getPropertyName(), database,
                    database.
                        getPrefix());
                addLineWithProperty(jg, Section.Metadata.getPrefix(),
                    Database.Properties.uri.getPropertyName(), database,
                    database.getUri());
            }
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                Database.Properties.version.getPropertyName(), database,
                Optional.ofNullable(database.getVersion()).orElse("Unknown"));

        } else {
           log.debug(Database.class.getSimpleName() + " is null!");
        }
    }

}

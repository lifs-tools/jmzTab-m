/*
 * 
 */
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addLineWithProperty;
import static de.isas.lipidomics.jmztabm.io.serialization.Serializers.addSubElementParameter;
import de.isas.mztab1_1.model.Database;
import java.io.IOException;
import uk.ac.ebi.pride.jmztab1_1.model.Section;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class DatabaseSerializer extends StdSerializer<Database> {

    public DatabaseSerializer() {
        this(null);
    }

    public DatabaseSerializer(Class<Database> t) {
        super(t);
    }

    @Override
    public void serialize(Database database, JsonGenerator jg,
        SerializerProvider sp) throws IOException {
        if (database != null) {
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "prefix", database,
                database.
                    getPrefix());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "url", database,
                database.getUrl());
            addLineWithProperty(jg, Section.Metadata.getPrefix(),
                "version", database,
                database.getVersion());
            addSubElementParameter(jg, Section.Metadata.getPrefix(),
                database, "parameter", database.getParam());

        } else {
            System.err.println("Database is null!");
        }
    }

}

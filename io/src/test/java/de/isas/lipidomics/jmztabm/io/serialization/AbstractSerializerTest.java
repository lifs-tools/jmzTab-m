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
package de.isas.lipidomics.jmztabm.io.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import de.isas.lipidomics.jmztabm.io.formats.AssayFormat;
import de.isas.lipidomics.jmztabm.io.formats.ContactFormat;
import de.isas.lipidomics.jmztabm.io.formats.CvFormat;
import de.isas.lipidomics.jmztabm.io.formats.DatabaseFormat;
import de.isas.lipidomics.jmztabm.io.formats.InstrumentFormat;
import de.isas.lipidomics.jmztabm.io.formats.MetadataFormat;
import de.isas.lipidomics.jmztabm.io.formats.MsRunFormat;
import de.isas.lipidomics.jmztabm.io.formats.ParameterFormat;
import de.isas.lipidomics.jmztabm.io.formats.PublicationFormat;
import de.isas.lipidomics.jmztabm.io.formats.SampleFormat;
import de.isas.lipidomics.jmztabm.io.formats.SampleProcessingFormat;
import de.isas.lipidomics.jmztabm.io.formats.SoftwareFormat;
import de.isas.lipidomics.jmztabm.io.formats.StudyVariableFormat;
import de.isas.lipidomics.jmztabm.io.formats.UriFormat;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.SampleProcessing;
import de.isas.mztab1_1.model.Software;
import de.isas.mztab1_1.model.StudyVariable;
import de.isas.mztab1_1.model.Uri;
import java.io.IOException;
import java.io.StringWriter;
import lombok.experimental.Builder;
import org.junit.Assert;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants;

/**
 *
 * @author Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V.
 */
public abstract class AbstractSerializerTest {
    
    public CsvMapper mapper() {
        CsvFactory factory = new CsvFactory();
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        return new CsvMapper(factory);
    }

    public CsvMapper metadataMapper() {
        CsvMapper mapper = mapper();
        mapper.addMixIn(Metadata.class, MetadataFormat.class);
        mapper.addMixIn(Assay.class, AssayFormat.class);
        mapper.addMixIn(Contact.class, ContactFormat.class);
        mapper.addMixIn(Publication.class, PublicationFormat.class);
        mapper.addMixIn(Instrument.class, InstrumentFormat.class);
        mapper.addMixIn(Sample.class, SampleFormat.class);
        mapper.addMixIn(SampleProcessing.class, SampleProcessingFormat.class);
        mapper.addMixIn(Software.class, SoftwareFormat.class);
        mapper.addMixIn(StudyVariable.class, StudyVariableFormat.class);
        mapper.addMixIn(MsRun.class, MsRunFormat.class);
        mapper.addMixIn(Database.class, DatabaseFormat.class);
        mapper.addMixIn(Parameter.class, ParameterFormat.class);
        mapper.addMixIn(CV.class, CvFormat.class);
        mapper.addMixIn(Uri.class, UriFormat.class);
        return mapper;
    }

    public CsvSchema metaDataSchema(CsvMapper mapper) {
        CsvSchema.Builder builder = mapper.schema().
            builder();
        return builder.addColumn("PREFIX",
            CsvSchema.ColumnType.STRING).
            addColumn("KEY",
            CsvSchema.ColumnType.STRING).
            addArrayColumn("VALUES", MZTabConstants.BAR_S).
            build().
            withAllowComments(true).
            withArrayElementSeparator(MZTabConstants.BAR_S).
            withNullValue(MZTabConstants.NULL).
            withUseHeader(false).
            withoutQuoteChar().
            withoutEscapeChar().
            withLineSeparator(MZTabConstants.NEW_LINE).
            withColumnSeparator(MZTabConstants.TAB);
    }

    public ObjectWriter metaDataWriter() {
        CsvMapper metaDataMapper = metadataMapper();
        CsvSchema metaDataSchema = metaDataSchema(metaDataMapper);
        return writer(metaDataMapper, metaDataSchema);
    }

    public String serialize(ObjectWriter writer, Object object) throws IOException {
        StringWriter sw = new StringWriter();
        writer.writeValue(sw, object);
        return sw.toString();
    }

    public ObjectWriter writer(CsvMapper mapper, CsvSchema schema) {
        ObjectWriter writer = mapper.writer(schema);
        return writer;
    }
    
    public void assertEqSentry(String a, String b) {
        Assert.assertEquals("'"+a+"'","'"+b+"'");
    }

}

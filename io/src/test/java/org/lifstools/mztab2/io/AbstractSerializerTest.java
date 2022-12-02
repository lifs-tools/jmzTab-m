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
package org.lifstools.mztab2.io;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.lifstools.mztab2.model.MzTab;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import org.junit.Assert;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 *
 * @author nilshoffmann
 */
public abstract class AbstractSerializerTest {

    private final MzTabWriterDefaults writerDefaults = new MzTabWriterDefaults();
    
    public ObjectWriter smallMoleculeSummaryWriter(MzTab mzTabFile) throws MZTabException {
        CsvMapper smallMoleculeSummaryMapper = writerDefaults.smallMoleculeSummaryMapper();
        CsvSchema smallMoleculeSummarySchema = writerDefaults.smallMoleculeSummarySchema(smallMoleculeSummaryMapper, mzTabFile);
        return writer(smallMoleculeSummaryMapper, smallMoleculeSummarySchema);
    }
    
    public ObjectWriter smallMoleculeFeatureWriter(MzTab mzTabFile) throws MZTabException  {
        CsvMapper smallMoleculeFeatureMapper = writerDefaults.smallMoleculeFeatureMapper();
        CsvSchema smallMoleculeFeatureSchema = writerDefaults.smallMoleculeFeatureSchema(smallMoleculeFeatureMapper, mzTabFile);
        return writer(smallMoleculeFeatureMapper, smallMoleculeFeatureSchema);
    }
    
    public ObjectWriter smallMoleculeEvidenceWriter(MzTab mzTabFile) throws MZTabException  {
        CsvMapper smallMoleculeEvidenceMapper = writerDefaults.smallMoleculeEvidenceMapper();
        CsvSchema smallMoleculeEvidenceSchema = writerDefaults.smallMoleculeEvidenceSchema(smallMoleculeEvidenceMapper, mzTabFile);
        return writer(smallMoleculeEvidenceMapper, smallMoleculeEvidenceSchema);
    }

    public String serializeSequence(ObjectWriter writer, Collection<?> elements) throws IOException {
        StringWriter sw = new StringWriter();
        SequenceWriter sequenceWriter = writer.writeValues(sw);
        sequenceWriter.writeAll(elements);
        return sw.toString();
    }
    
    public String serializeSingle(ObjectWriter writer, Object object) throws IOException {
        StringWriter sw = new StringWriter();
        writer.writeValue(sw, object);
        return sw.toString();
    }

    public ObjectWriter metaDataWriter() {
        CsvMapper metaDataMapper = writerDefaults.metadataMapper();
        CsvSchema metaDataSchema = writerDefaults.metaDataSchema(metaDataMapper);
        return writer(metaDataMapper, metaDataSchema);
    }
    
    public ObjectWriter writer(CsvMapper mapper, CsvSchema schema) {
        ObjectWriter writer = mapper.writer(schema);
        return writer;
    }
    
    public void assertEqSentry(String a, String b) {
        Assert.assertEquals("'"+a+"'","'"+b+"'");
    }

}

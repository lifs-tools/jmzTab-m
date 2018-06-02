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
package de.isas.lipidomics.jmztabm.io;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import de.isas.mztab1_1.model.MzTab;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.Assert;

/**
 *
 * @author nilshoffmann
 */
public abstract class AbstractSerializerTest {

    private final MzTabWriterDefaults writerDefaults = new MzTabWriterDefaults();
    
    public ObjectWriter smallMoleculeSummaryWriter(MzTab mzTabFile) {
        CsvMapper smallMoleculeSummaryMapper = writerDefaults.smallMoleculeSummaryMapper();
        CsvSchema smallMoleculeSummarySchema = writerDefaults.smallMoleculeSummarySchema(smallMoleculeSummaryMapper, mzTabFile);
        return writer(smallMoleculeSummaryMapper, smallMoleculeSummarySchema);
    }
    
    public ObjectWriter smallMoleculeFeatureWriter(MzTab mzTabFile) {
        CsvMapper smallMoleculeFeatureMapper = writerDefaults.smallMoleculeFeatureMapper();
        CsvSchema smallMoleculeFeatureSchema = writerDefaults.smallMoleculeFeatureSchema(smallMoleculeFeatureMapper, mzTabFile);
        return writer(smallMoleculeFeatureMapper, smallMoleculeFeatureSchema);
    }
    
    public ObjectWriter smallMoleculeEvidenceWriter(MzTab mzTabFile) {
        CsvMapper smallMoleculeEvidenceMapper = writerDefaults.smallMoleculeEvidenceMapper();
        CsvSchema smallMoleculeEvidenceSchema = writerDefaults.smallMoleculeEvidenceSchema(smallMoleculeEvidenceMapper, mzTabFile);
        return writer(smallMoleculeEvidenceMapper, smallMoleculeEvidenceSchema);
    }

    public String serialize(ObjectWriter writer, Object object) throws IOException {
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

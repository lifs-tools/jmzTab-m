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
package de.isas.mztab2.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import de.isas.mztab2.model.MzTab;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;

/**
 * <p>
 * MzTabWriter class.</p>
 *
 * @author nilshoffmann
 *
 */
public class MzTabNonValidatingWriter implements MzTabWriter<Void> {

    private final MzTabWriterDefaults writerDefaults;
    
    public MzTabNonValidatingWriter() {
        this.writerDefaults = new MzTabWriterDefaults();
    }
    
    public MzTabNonValidatingWriter(MzTabWriterDefaults writerDefaults) {
        this.writerDefaults = writerDefaults;
    }

    /**
     * <p>
     * Write the mzTab object to the provided output stream writer.</p>
     *
     * This method does not close the output stream but will issue a
     * <code>flush</code> on the provided output stream writer!
     *
     * @param writer a {@link java.io.OutputStreamWriter} object.
     * @param mzTab a {@link de.isas.mztab2.model.MzTab} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public Optional<Void> write(OutputStreamWriter writer, MzTab mzTab) throws IOException {
        if (!writer.getEncoding().
            equals("UTF8")) {
            throw new IllegalArgumentException(
                "OutputStreamWriter encoding must be UTF8 but is " + writer.
                    getEncoding());
        }
        writeMzTab(mzTab, writer);
        return Optional.empty();
    }

    /**
     * <p>
     * Write the mzTab object to the provided path / file.</p>
     *
     *
     * @param path a {@link java.nio.file.Path} object.
     * @param mzTab a {@link de.isas.mztab2.model.MzTab} object.
     * @return the validation messages.
     * @throws java.io.IOException if any.
     */
    @Override
    public Optional<Void> write(Path path, MzTab mzTab) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.
            forName(
                "UTF-8"), StandardOpenOption.CREATE,
            StandardOpenOption.WRITE)) {
            writeMzTab(mzTab, writer);
        }
        return Optional.empty();
    }

    void writeMzTab(MzTab mzTab, final Writer writer) throws IOException {
        writeMetadataWithJackson(mzTab, writer);
        writer.write("\n");
        writeSmallMoleculeSummaryWithJackson(mzTab, writer);
        writer.write("\n");
        writeSmallMoleculeFeaturesWithJackson(mzTab, writer);
        writer.write("\n");
        writeSmallMoleculeEvidenceWithJackson(mzTab, writer);
        writer.flush();
    }

    void writeMetadataWithJackson(MzTab mztabfile, Writer writer) throws IOException {
        CsvMapper mapper = writerDefaults.metadataMapper();
        CsvSchema schema = writerDefaults.metaDataSchema(mapper);
        if(mztabfile.getMetadata().getMzTabVersion()==null) {
            //set default version if not set
            mztabfile.getMetadata().mzTabVersion(MZTabConstants.VERSION_MZTAB_M);
        }
        try {
            mapper.writer(schema).
                writeValue(writer, mztabfile.getMetadata());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabNonValidatingWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    void writeSmallMoleculeSummaryWithJackson(MzTab mztabfile, Writer writer) throws IOException {
        CsvMapper mapper = writerDefaults.smallMoleculeSummaryMapper();
        CsvSchema schema = writerDefaults.smallMoleculeSummarySchema(mapper, mztabfile);
        try {
            mapper.writer(schema).
                writeValue(writer, mztabfile.getSmallMoleculeSummary());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabNonValidatingWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    void writeSmallMoleculeFeaturesWithJackson(MzTab mztabfile, Writer writer) throws IOException {
        CsvMapper mapper = writerDefaults.smallMoleculeFeatureMapper();
        CsvSchema schema = writerDefaults.smallMoleculeFeatureSchema(mapper, mztabfile);
        try {
            mapper.writer(schema).
                writeValue(writer, mztabfile.getSmallMoleculeFeature());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabNonValidatingWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

    void writeSmallMoleculeEvidenceWithJackson(MzTab mztabfile, Writer writer) throws IOException {
        CsvMapper mapper = writerDefaults.smallMoleculeEvidenceMapper();
        CsvSchema schema = writerDefaults.smallMoleculeEvidenceSchema(mapper, mztabfile);
        try {
            mapper.writer(schema).
                writeValue(writer, mztabfile.getSmallMoleculeEvidence());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabNonValidatingWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
    }

}

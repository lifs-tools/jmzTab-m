/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import de.isas.lipidomics.jmztabm.io.formats.AssayFormat;
import de.isas.lipidomics.jmztabm.io.formats.ContactFormat;
import de.isas.lipidomics.jmztabm.io.formats.CvFormat;
import de.isas.lipidomics.jmztabm.io.formats.DatabaseFormat;
import de.isas.lipidomics.jmztabm.io.formats.ExternalStudyFormat;
import de.isas.lipidomics.jmztabm.io.formats.InstrumentFormat;
import de.isas.lipidomics.jmztabm.io.formats.MetadataFormat;
import de.isas.lipidomics.jmztabm.io.formats.MsRunFormat;
import de.isas.lipidomics.jmztabm.io.formats.ParameterFormat;
import de.isas.lipidomics.jmztabm.io.formats.PublicationFormat;
import de.isas.lipidomics.jmztabm.io.formats.SampleFormat;
import de.isas.lipidomics.jmztabm.io.formats.SampleProcessingFormat;
import de.isas.lipidomics.jmztabm.io.formats.SmallMoleculeEvidenceFormat;
import de.isas.lipidomics.jmztabm.io.formats.SmallMoleculeFeatureFormat;
import de.isas.lipidomics.jmztabm.io.formats.SmallMoleculeSummaryFormat;
import de.isas.lipidomics.jmztabm.io.formats.SoftwareFormat;
import de.isas.lipidomics.jmztabm.io.formats.StudyVariableFormat;
import de.isas.lipidomics.jmztabm.io.serialization.Serializers;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.ExternalStudy;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.OptColumnMapping;
import de.isas.mztab1_1.model.Parameter;
import de.isas.mztab1_1.model.Publication;
import de.isas.mztab1_1.model.Sample;
import de.isas.mztab1_1.model.SampleProcessing;
import de.isas.mztab1_1.model.SmallMoleculeEvidence;
import de.isas.mztab1_1.model.SmallMoleculeFeature;
import de.isas.mztab1_1.model.SmallMoleculeSummary;
import de.isas.mztab1_1.model.Software;
import de.isas.mztab1_1.model.StudyVariable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
public class MzTabWriter {

    public final static String EOL = "\n\r";
    public final static String SEP = "\t";

    public void write(OutputStreamWriter os, MzTab mzTab) throws IOException {
        if (!os.getEncoding().
            equals("UTF8")) {
            throw new IllegalArgumentException(
                "OutputStreamWriter encoding must be UTF8 but is " + os.
                    getEncoding());
        }
        try {
            os.write(writeMetadataWithJackson(mzTab));
        } catch (NullPointerException npe) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, npe);
        }
        try {
            os.write(writeSmallMoleculeSummaryWithJackson(mzTab));
        } catch (NullPointerException npe) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, npe);
        }
        try {
            os.write(writeSmallMoleculeFeaturesWithJackson(mzTab));
        } catch (NullPointerException npe) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, npe);
        }
        try {
            os.write(writeSmallMoleculeEvidenceWithJackson(mzTab));
        } catch (NullPointerException npe) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, npe);
        }
    }

    public void write(Path path, MzTab mzTab) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName(
            "UTF-8"), StandardOpenOption.CREATE,
            StandardOpenOption.WRITE)) {
            writer.write(writeMetadataWithJackson(mzTab));
            writer.write(writeSmallMoleculeSummaryWithJackson(mzTab));
            writer.write(writeSmallMoleculeFeaturesWithJackson(mzTab));
            writer.write(writeSmallMoleculeEvidenceWithJackson(mzTab));
            writer.flush();
            writer.close();
        }
    }

    String writeMetadataWithJackson(MzTab mztabfile) {
        CsvMapper mapper = new CsvMapper();
        mapper.addMixIn(Metadata.class, MetadataFormat.class);
        mapper.addMixIn(Assay.class, AssayFormat.class);
        mapper.addMixIn(Contact.class, ContactFormat.class);
        mapper.addMixIn(Publication.class, PublicationFormat.class);
        mapper.addMixIn(ExternalStudy.class, ExternalStudyFormat.class);
        mapper.addMixIn(Instrument.class, InstrumentFormat.class);
        mapper.addMixIn(Sample.class, SampleFormat.class);
        mapper.addMixIn(SampleProcessing.class, SampleProcessingFormat.class);
        mapper.addMixIn(Software.class, SoftwareFormat.class);
        mapper.addMixIn(StudyVariable.class, StudyVariableFormat.class);
        mapper.addMixIn(MsRun.class, MsRunFormat.class);
        mapper.addMixIn(Database.class, DatabaseFormat.class);
        mapper.addMixIn(Parameter.class, ParameterFormat.class);
        mapper.addMixIn(CV.class, CvFormat.class);

        CsvSchema schema = mapper.schema().
            builder().
            addColumn("PREFIX", CsvSchema.ColumnType.STRING).
            addColumn("KEY", CsvSchema.ColumnType.STRING).
            addArrayColumn("VALUES", "|").
            build().
            withAllowComments(true).
            withArrayElementSeparator("|").
            withNullValue("null").
            withUseHeader(false).
            withoutQuoteChar().
            withoutEscapeChar().
            withColumnSeparator('\t');
        try {
            return mapper.writer(schema).
                writeValueAsString(mztabfile.getMetadata());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        return "";
    }

    String writeSmallMoleculeSummaryWithJackson(MzTab mztabfile) {
        CsvMapper mapper = new CsvMapper();
        mapper.addMixIn(SmallMoleculeSummary.class,
            SmallMoleculeSummaryFormat.class);
        Builder builder = mapper.schema().
            builder();
        builder.addColumn("SMH", CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.SML_ID.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.SMF_ID_REFS.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.DATABASE_IDENTIFIER.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.CHEMICAL_FORMULA.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.SMILES.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.INCHI.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.CHEMICAL_NAME.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.URI.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.THEOR_NEUTRAL_MASS.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.EXP_MASS_TO_CHARGE.getHeader(),
                CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeColumn.Stable.RETENTION_TIME.getHeader(),
                CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeColumn.Stable.ADDUCT_IONS.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.RELIABILITY.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_MEASURE.
                getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_VALUE.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING);
        mztabfile.getMetadata().
            getAssay().
            forEach((assay) ->
            {
                builder.addColumn("abundance_assay[" + assay.getId() + "]",
                    CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        mztabfile.getMetadata().
            getStudyVariable().
            forEach((studyVariable) ->
            {
                builder.addColumn("abundance_study_variable[" + studyVariable.
                    getId() + "]", CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        mztabfile.getMetadata().
            getStudyVariable().
            forEach((studyVariable) ->
            {
                builder.addColumn(
                    "abundance_coeffvar_study_variable[" + studyVariable.getId() + "]",
                    CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        for (SmallMoleculeSummary sms : mztabfile.getSmallMoleculeSummary()) {
            for (OptColumnMapping ocm : Optional.ofNullable(sms.getOpt()).orElse(Collections.emptyList())) {
                optColumns.putIfAbsent(Serializers.printOptColumnMapping(ocm),
                    ocm);
            }
        }
        for (String key : optColumns.keySet()) {
            builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
        }
        CsvSchema schema = defaultSchemaForBuilder(builder);

        try {
            return mapper.writer(schema).
                writeValueAsString(mztabfile.getSmallMoleculeSummary());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        return "";
    }

    static CsvSchema defaultSchemaForBuilder(Builder builder) {
        return builder.
            build().
            withAllowComments(true).
            withArrayElementSeparator("|").
            withNullValue("null").
            withUseHeader(true).
            withoutQuoteChar().
            withoutEscapeChar().
            withColumnSeparator('\t');
    }

    String writeSmallMoleculeFeaturesWithJackson(MzTab mztabfile) {
        CsvMapper mapper = new CsvMapper();
        mapper.addMixIn(SmallMoleculeFeature.class,
            SmallMoleculeFeatureFormat.class);

        Builder builder = mapper.schema().
            builder();
        builder.addColumn("SFH", CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.SMF_ID.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.SME_ID_REFS.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(
                SmallMoleculeFeatureColumn.Stable.SME_ID_REF_AMBIGUITY_CODE.
                    getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.ADDUCT_ION.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.ISOTOPOMER.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.EXP_MASS_TO_CHARGE.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.CHARGE.getHeader(),
                CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_START.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_END.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING);

        mztabfile.getMetadata().
            getAssay().
            forEach((assay) ->
            {
                builder.addColumn("abundance_assay[" + assay.getId() + "]",
                    CsvSchema.ColumnType.NUMBER_OR_STRING);
            });

        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        for (SmallMoleculeFeature smf : mztabfile.getSmallMoleculeFeature()) {
            for (OptColumnMapping ocm : Optional.ofNullable(smf.getOpt()).orElse(Collections.emptyList())) {
                optColumns.putIfAbsent(Serializers.printOptColumnMapping(ocm),
                    ocm);
            }
        }
        for (String key : optColumns.keySet()) {
            builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
        }
        CsvSchema schema = defaultSchemaForBuilder(builder);
        try {
            return mapper.writer(schema).
                writeValueAsString(mztabfile.getSmallMoleculeFeature());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        return "";
    }

    String writeSmallMoleculeEvidenceWithJackson(MzTab mztabfile) {
        CsvMapper mapper = new CsvMapper();
        mapper.addMixIn(SmallMoleculeEvidence.class,
            SmallMoleculeEvidenceFormat.class);
        Builder builder = mapper.schema().
            builder();
        builder.addColumn("SEH", CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.SME_ID.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.EVIDENCE_UNIQUE_ID.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.DATABASE_IDENTIFIER.
                getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.CHEMICAL_FORMULA.
                getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.SMILES.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.INCHI.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.CHEMICAL_NAME.
                getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.URI.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.DERIVATIZED_FORM.
                getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.ADDUCT_ION.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.EXP_MASS_TO_CHARGE.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.CHARGE.getHeader(),
                CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(
                SmallMoleculeEvidenceColumn.Stable.THEORETICAL_MASS_TO_CHARGE.
                    getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.SPECTRA_REF.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.IDENTIFICATION_METHOD.
                getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.MS_LEVEL.getHeader(),
                CsvSchema.ColumnType.STRING);
        mztabfile.getMetadata().
            getIdConfidenceMeasure().
            forEach((param) ->
            {
                builder.
                    addColumn("id_confidence_measure[" + param.getId() + "]",
                        CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        builder.addColumn(SmallMoleculeEvidenceColumn.Stable.RANK.getHeader(),
            CsvSchema.ColumnType.NUMBER_OR_STRING);
        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        for (SmallMoleculeEvidence sme : mztabfile.getSmallMoleculeEvidence()) {
            for (OptColumnMapping ocm : Optional.ofNullable(sme.getOpt()).orElse(Collections.emptyList())) {
                optColumns.putIfAbsent(Serializers.printOptColumnMapping(ocm),
                    ocm);
            }
        }
        for (String key : optColumns.keySet()) {
            builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
        }
        CsvSchema schema = defaultSchemaForBuilder(builder);
        try {
            return mapper.writer(schema).
                writeValueAsString(mztabfile.getSmallMoleculeEvidence());
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MzTabWriter.class.getName()).
                log(Level.SEVERE, null, ex);
        }
        return "";
    }

}

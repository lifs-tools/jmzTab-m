/*
 * 
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
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.ExternalStudy;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.MzTab;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
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
import java.util.List;
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
                "OutputStreamWriter encoding must be UTF8 but is "+os.getEncoding());
        }
        os.write(writeMetadataWithJackson(mzTab));
        os.write(writeSmallMoleculeSummaryWithJackson(mzTab));
        os.write(writeSmallMoleculeFeaturesWithJackson(mzTab));
        os.write(writeSmallMoleculeEvidenceWithJackson(mzTab));
    }

    public void write(Path path, MzTab mzTab) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName(
            "UTF-8"), StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE);
        writer.write(writeMetadataWithJackson(mzTab));
        writer.write(writeSmallMoleculeSummaryWithJackson(mzTab));
        writer.write(writeSmallMoleculeFeaturesWithJackson(mzTab));
        writer.write(writeSmallMoleculeEvidenceWithJackson(mzTab));
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
        List<SmallMoleculeSummary> sm = mztabfile.getSmallMoleculeSummary();
        Builder builder = mapper.schema().
            builder();
        builder.addColumn("SMH", CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.SML_ID.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.SMF_ID_REFS.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.DATABASE_IDENTIFIER.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.CHEMICAL_FORMULA.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.SMILES.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.INCHI.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.CHEMICAL_NAME.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.URI.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.THEOR_NEUTRAL_MASS.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.EXP_MASS_TO_CHARGE.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeColumn.Stable.RETENTION_TIME.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeColumn.Stable.ADDUCT_IONS.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeColumn.Stable.RELIABILITY.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_MEASURE.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_VALUE.getHeader(), CsvSchema.ColumnType.NUMBER);
        mztabfile.getMetadata().
            getAssay().
            forEach((assay) ->
            {
                builder.addColumn("abundance_assay[" + assay.getId() + "]",
                    CsvSchema.ColumnType.NUMBER);
            });
        mztabfile.getMetadata().
            getStudyVariable().
            forEach((studyVariable) ->
            {
                builder.addColumn("abundance_study_variable[" + studyVariable.
                    getId() + "]", CsvSchema.ColumnType.NUMBER);
            });
        mztabfile.getMetadata().
            getStudyVariable().
            forEach((studyVariable) ->
            {
                builder.addColumn(
                    "abundance_coeffvar_study_variable[" + studyVariable.getId() + "]",
                    CsvSchema.ColumnType.NUMBER);
            });
        //TODO add optional columns
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
            addColumn(SmallMoleculeFeatureColumn.Stable.SMF_ID.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.SME_ID_REFS.getHeader(), CsvSchema.ColumnType.ARRAY).
            addColumn(SmallMoleculeFeatureColumn.Stable.SME_ID_REF_AMBIGUITY_CODE.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeFeatureColumn.Stable.ADDUCT_ION.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.ISOTOPOMER.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeFeatureColumn.Stable.EXP_MASS_TO_CHARGE.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeFeatureColumn.Stable.CHARGE.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_START.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_END.getHeader(), CsvSchema.ColumnType.NUMBER);

        mztabfile.getMetadata().
            getAssay().
            forEach((assay) ->
            {
                builder.addColumn("abundance_assay[" + assay.getId() + "]",
                    CsvSchema.ColumnType.NUMBER);
            });

        //TODO add optional columns
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
            addColumn(SmallMoleculeEvidenceColumn.Stable.SME_ID.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.EVIDENCE_UNIQUE_ID.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeEvidenceColumn.Stable.DATABASE_IDENTIFIER.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.CHEMICAL_FORMULA.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.SMILES.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.INCHI.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.CHEMICAL_NAME.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.URI.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.DERIVATIZED_FORM.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.ADDUCT_ION.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.EXP_MASS_TO_CHARGE.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeEvidenceColumn.Stable.CHARGE.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeEvidenceColumn.Stable.THEORETICAL_MASS_TO_CHARGE.getHeader(), CsvSchema.ColumnType.NUMBER).
            addColumn(SmallMoleculeEvidenceColumn.Stable.SPECTRA_REF.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.IDENTIFICATION_METHOD.getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.MS_LEVEL.getHeader(), CsvSchema.ColumnType.STRING);
        mztabfile.getMetadata().
            getIdConfidenceMeasure().
            forEach((param) ->
            {
                builder.
                    addColumn("id_confidence_measure[" + param.getId() + "]",
                        CsvSchema.ColumnType.NUMBER);
            });
        builder.addColumn(SmallMoleculeEvidenceColumn.Stable.RANK.getHeader(), CsvSchema.ColumnType.NUMBER);
        //TODO add optional columns
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

}

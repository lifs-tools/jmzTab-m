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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.lifstools.mztab2.io.formats.AssayFormat;
import org.lifstools.mztab2.io.formats.ContactFormat;
import org.lifstools.mztab2.io.formats.CvFormat;
import org.lifstools.mztab2.io.formats.DatabaseFormat;
import org.lifstools.mztab2.io.formats.InstrumentFormat;
import org.lifstools.mztab2.io.formats.MetadataFormat;
import org.lifstools.mztab2.io.formats.MsRunFormat;
import org.lifstools.mztab2.io.formats.ParameterFormat;
import org.lifstools.mztab2.io.formats.PublicationFormat;
import org.lifstools.mztab2.io.formats.SampleFormat;
import org.lifstools.mztab2.io.formats.SampleProcessingFormat;
import org.lifstools.mztab2.io.formats.SmallMoleculeEvidenceFormat;
import org.lifstools.mztab2.io.formats.SmallMoleculeFeatureFormat;
import org.lifstools.mztab2.io.formats.SmallMoleculeSummaryFormat;
import org.lifstools.mztab2.io.formats.SoftwareFormat;
import org.lifstools.mztab2.io.formats.StudyVariableFormat;
import org.lifstools.mztab2.io.formats.UriFormat;
import org.lifstools.mztab2.io.serialization.Serializers;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Contact;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.Instrument;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Publication;
import org.lifstools.mztab2.model.Sample;
import org.lifstools.mztab2.model.SampleProcessing;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import org.lifstools.mztab2.model.Software;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.Uri;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab2.model.SmallMoleculeFeatureColumn;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * Default mapper and schema definitions for writing of mzTab files using the
 * Jackson CSV mapper.
 *
 * @author nilshoffmann
 */
public class MzTabWriterDefaults {

    /**
     * Create a default csv mapper instance.
     *
     * @return the csv mapper
     */
    public CsvMapper defaultMapper() {
        CsvFactory factory = new CsvFactory();
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        factory.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
        CsvMapper mapper = new CsvMapper(factory);
        mapper.configure(Feature.IGNORE_UNKNOWN, true);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /**
     * Create a metadata section csv mapper. This registers mixins for
     * serialization of all objects that are part of the metadata section.
     *
     * @return the metadata section csv mapper.
     */
    public CsvMapper metadataMapper() {
        CsvMapper mapper = defaultMapper();
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

    /**
     * Creates the csv schema for the metadata section (column names, value
     * separators, array element separators, etc.).
     *
     * @param mapper the configured csv mapper
     * @return the metadata csv schema
     */
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

    /**
     * Create a small molecule summary section csv mapper. This registers mixins
     * for serialization of all objects that are part of the small molecule
     * summary section.
     *
     * @return the small molecule summary section csv mapper.
     */
    public CsvMapper smallMoleculeSummaryMapper() {
        CsvMapper mapper = metadataMapper();
        mapper.addMixIn(SmallMoleculeSummary.class,
                SmallMoleculeSummaryFormat.class);
        return mapper;
    }

    /**
     * Create a small molecule feature section csv mapper. This registers mixins
     * for serialization of all objects that are part of the small molecule
     * feature section.
     *
     * @return the small molecule feature section csv mapper.
     */
    public CsvMapper smallMoleculeFeatureMapper() {
        CsvMapper mapper = metadataMapper();
        mapper.addMixIn(SmallMoleculeFeature.class,
                SmallMoleculeFeatureFormat.class);
        return mapper;
    }

    /**
     * Create a small molecule evidence section csv mapper. This registers
     * mixins for serialization of all objects that are part of the small
     * molecule evidence section.
     *
     * @return the small molecule evidence section csv mapper.
     */
    public CsvMapper smallMoleculeEvidenceMapper() {
        CsvMapper mapper = metadataMapper();
        mapper.addMixIn(SmallMoleculeEvidence.class,
                SmallMoleculeEvidenceFormat.class);
        return mapper;
    }

    /**
     * Apply the default csv schema to the provided builder.
     *
     * @param builder the builder to use for schema configuration
     * @return the configured csv schema
     */
    public CsvSchema defaultSchemaForBuilder(CsvSchema.Builder builder) {
        return builder.
                build().
                withAllowComments(true).
                withArrayElementSeparator(MZTabConstants.BAR_S).
                withNullValue(MZTabConstants.NULL).
                withUseHeader(true).
                withoutQuoteChar().
                withoutEscapeChar().
                withLineSeparator(MZTabConstants.NEW_LINE).
                withColumnSeparator(MZTabConstants.TAB);
    }

    /**
     * Creates the csv schema (column names and types) for the small molecule
     * summary section.
     *
     * @param mapper the csv mapper
     * @param mzTabFile the mztab object
     * @return the configured csv schema for the small molecule summary section
     * @throws MZTabException
     */
    public CsvSchema smallMoleculeSummarySchema(CsvMapper mapper,
            MzTab mzTabFile) throws MZTabException {
        CsvSchema.Builder builder = mapper.schema().
                builder();
        builder.addColumn(SmallMoleculeSummary.HeaderPrefixEnum.SMH.getValue(),
                CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.SML_ID).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.SMF_ID_REFS).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.DATABASE_IDENTIFIER).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.CHEMICAL_FORMULA).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.SMILES).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.INCHI).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.CHEMICAL_NAME).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.URI).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.THEOR_NEUTRAL_MASS).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.ADDUCT_IONS).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.RELIABILITY).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_MEASURE).
                        getHeader(), CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeColumn.Stable.columnFor(
                        SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_VALUE).
                        getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING);

        Metadata metadata = Optional.ofNullable(mzTabFile.getMetadata()).orElseThrow(() -> new MZTabException(new MZTabError(
                LogicalErrorType.NoMetadataSection, -1)));

        List<SmallMoleculeSummary> smsList = Optional.ofNullable(mzTabFile.getSmallMoleculeSummary()).orElse(Collections.emptyList());
        //orElseThrow(() -> new MZTabException(new MZTabError(
        //            LogicalErrorType.NoSmallMoleculeSummarySection, -1)));

        metadata.
                getAssay().
                forEach((assay)
                        -> {
                    builder.addColumn(
                            SmallMoleculeSummary.JSON_PROPERTY_ABUNDANCE_ASSAY + "[" + assay.
                                    getId() + "]",
                            CsvSchema.ColumnType.NUMBER_OR_STRING);
                });
        metadata.
                getStudyVariable().
                forEach((studyVariable)
                        -> {
                    builder.addColumn(
                            SmallMoleculeSummary.JSON_PROPERTY_ABUNDANCE_STUDY_VARIABLE + "[" + studyVariable.
                                    getId() + "]", CsvSchema.ColumnType.NUMBER_OR_STRING);
                });
        metadata.
                getStudyVariable().
                forEach((studyVariable)
                        -> {
                    builder.addColumn(
                            SmallMoleculeSummary.JSON_PROPERTY_ABUNDANCE_VARIATION_STUDY_VARIABLE + "[" + studyVariable.
                                    getId() + "]",
                            CsvSchema.ColumnType.NUMBER_OR_STRING);
                });
        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        smsList.
                forEach((SmallMoleculeSummary sms)
                        -> {
                    Optional.ofNullable(sms.getOpt()).
                            orElse(Collections.emptyList()).
                            forEach((ocm)
                                    -> {
                                optColumns.putIfAbsent(Serializers.
                                        printOptColumnMapping(ocm),
                                        ocm);
                            });
                });
        optColumns.keySet().
                forEach((key)
                        -> {
                    builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
                });
        return defaultSchemaForBuilder(builder);
    }

    /**
     * Creates the csv schema (column names and types) for the small molecule
     * feature section.
     *
     * @param mapper the csv mapper
     * @param mzTabFile the mztab object
     * @return the configured csv schema for the small molecule feature section
     * @throws MZTabException
     */
    public CsvSchema smallMoleculeFeatureSchema(CsvMapper mapper,
            MzTab mzTabFile) throws MZTabException {
        CsvSchema.Builder builder = mapper.schema().
                builder();
        builder.addColumn(SmallMoleculeFeature.HeaderPrefixEnum.SFH.getValue(),
                CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeFeatureColumn.Stable.columnFor(
                        SmallMoleculeFeatureColumn.Stable.SMF_ID).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeFeatureColumn.Stable.columnFor(
                        SmallMoleculeFeatureColumn.Stable.SME_ID_REFS).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(
                        SmallMoleculeFeatureColumn.Stable.columnFor(
                                SmallMoleculeFeatureColumn.Stable.SME_ID_REF_AMBIGUITY_CODE).
                                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(SmallMoleculeFeatureColumn.Stable.columnFor(
                        SmallMoleculeFeatureColumn.Stable.ADDUCT_ION).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeFeatureColumn.Stable.columnFor(
                        SmallMoleculeFeatureColumn.Stable.ISOTOPOMER).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeFeatureColumn.Stable.columnFor(
                        SmallMoleculeFeatureColumn.Stable.EXP_MASS_TO_CHARGE).
                        getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(SmallMoleculeFeatureColumn.Stable.columnFor(
                        SmallMoleculeFeatureColumn.Stable.CHARGE).
                        getHeader(),
                        CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(
                        SmallMoleculeFeatureColumn.Stable.columnFor(
                                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS).
                                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(
                        SmallMoleculeFeatureColumn.Stable.columnFor(
                                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_START).
                                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(
                        SmallMoleculeFeatureColumn.Stable.columnFor(
                                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_END).
                                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING);
        Metadata metadata = Optional.ofNullable(mzTabFile.getMetadata()).orElseThrow(
                () -> new MZTabException(new MZTabError(
                        LogicalErrorType.NoMetadataSection, -1)));
        Optional.ofNullable(metadata.
                getAssay()).
                ifPresent((assayList)
                        -> assayList.forEach((assay)
                        -> {
                    builder.addColumn(
                            SmallMoleculeFeature.JSON_PROPERTY_ABUNDANCE_ASSAY + "[" + assay.
                                    getId() + "]",
                            CsvSchema.ColumnType.NUMBER_OR_STRING);
                })
                );

        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        mzTabFile.getSmallMoleculeFeature().
                forEach((SmallMoleculeFeature smf)
                        -> {
                    Optional.ofNullable(smf.getOpt()).
                            orElse(Collections.emptyList()).
                            forEach((ocm)
                                    -> {
                                optColumns.putIfAbsent(Serializers.
                                        printOptColumnMapping(ocm),
                                        ocm);
                            });
                });
        optColumns.keySet().
                forEach((key)
                        -> {
                    builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
                });
        return defaultSchemaForBuilder(builder);
    }

    /**
     * Creates the csv schema (column names and types) for the small molecule
     * feature section.
     *
     * @param mapper the csv mapper
     * @param mzTabFile the mztab object
     * @return the configured csv schema for the small molecule feature section
     * @throws MZTabException
     */
    public CsvSchema smallMoleculeEvidenceSchema(CsvMapper mapper,
            MzTab mzTabFile) throws MZTabException {
        CsvSchema.Builder builder = mapper.schema().
                builder();
        builder.addColumn(SmallMoleculeEvidence.HeaderPrefixEnum.SEH.getValue(),
                CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.SME_ID).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.EVIDENCE_INPUT_ID).
                        getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.DATABASE_IDENTIFIER).
                        getHeader(), CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.CHEMICAL_FORMULA).
                        getHeader(), CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.SMILES).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.INCHI).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.CHEMICAL_NAME).
                        getHeader(), CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.URI).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.DERIVATIZED_FORM).
                        getHeader(), CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.ADDUCT_ION).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.EXP_MASS_TO_CHARGE).
                        getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.CHARGE).
                        getHeader(),
                        CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(
                        SmallMoleculeEvidenceColumn.Stable.columnFor(
                                SmallMoleculeEvidenceColumn.Stable.THEORETICAL_MASS_TO_CHARGE).
                                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.SPECTRA_REF).
                        getHeader(),
                        CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.IDENTIFICATION_METHOD).
                        getHeader(), CsvSchema.ColumnType.STRING).
                addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                        SmallMoleculeEvidenceColumn.Stable.MS_LEVEL).
                        getHeader(),
                        CsvSchema.ColumnType.STRING);
        Metadata metadata = Optional.ofNullable(mzTabFile.getMetadata()).orElseThrow(()
                -> new MZTabException(new MZTabError(
                        LogicalErrorType.NoMetadataSection, -1)));
        Optional.ofNullable(metadata.
                getIdConfidenceMeasure()).
                ifPresent((parameterList)
                        -> {
                    IntStream.range(0, parameterList.
                            size()).
                            forEachOrdered(i
                                    -> {
                                builder.
                                        addColumn(
                                                SmallMoleculeEvidence.JSON_PROPERTY_ID_CONFIDENCE_MEASURE + "[" + (i + 1) + "]",
                                                CsvSchema.ColumnType.NUMBER_OR_STRING);
                            });

                });
        builder.addColumn(SmallMoleculeEvidenceColumn.Stable.columnFor(
                SmallMoleculeEvidenceColumn.Stable.RANK).
                getHeader(),
                CsvSchema.ColumnType.NUMBER_OR_STRING);
        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        mzTabFile.getSmallMoleculeEvidence().
                forEach((SmallMoleculeEvidence sme)
                        -> {
                    Optional.ofNullable(sme.getOpt()).
                            orElse(Collections.emptyList()).
                            forEach((ocm)
                                    -> {
                                optColumns.putIfAbsent(Serializers.
                                        printOptColumnMapping(ocm),
                                        ocm);
                            });
                });
        optColumns.keySet().
                forEach((key)
                        -> {
                    builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
                });
        return defaultSchemaForBuilder(builder);
    }
}

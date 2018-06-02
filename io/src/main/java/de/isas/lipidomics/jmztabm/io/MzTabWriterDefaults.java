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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
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
import de.isas.lipidomics.jmztabm.io.formats.SmallMoleculeEvidenceFormat;
import de.isas.lipidomics.jmztabm.io.formats.SmallMoleculeFeatureFormat;
import de.isas.lipidomics.jmztabm.io.formats.SmallMoleculeSummaryFormat;
import de.isas.lipidomics.jmztabm.io.formats.SoftwareFormat;
import de.isas.lipidomics.jmztabm.io.formats.StudyVariableFormat;
import de.isas.lipidomics.jmztabm.io.formats.UriFormat;
import de.isas.lipidomics.jmztabm.io.serialization.Serializers;
import de.isas.mztab1_1.model.Assay;
import de.isas.mztab1_1.model.CV;
import de.isas.mztab1_1.model.Contact;
import de.isas.mztab1_1.model.Database;
import de.isas.mztab1_1.model.Instrument;
import de.isas.mztab1_1.model.Metadata;
import de.isas.mztab1_1.model.MsRun;
import de.isas.mztab1_1.model.MzTab;
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
import de.isas.mztab1_1.model.Uri;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import uk.ac.ebi.pride.jmztab1_1.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeEvidenceColumn;
import uk.ac.ebi.pride.jmztab1_1.model.SmallMoleculeFeatureColumn;

/**
 *
 * @author nilshoffmann
 */
public class MzTabWriterDefaults {
        
    public CsvMapper defaultMapper() {
        CsvFactory factory = new CsvFactory();
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        CsvMapper mapper = new CsvMapper(factory);
        mapper.configure(Feature.IGNORE_UNKNOWN, true);
        return mapper;
    }

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
    
    public CsvMapper smallMoleculeSummaryMapper() {
        CsvMapper mapper = metadataMapper();
        mapper.addMixIn(SmallMoleculeSummary.class,
            SmallMoleculeSummaryFormat.class);
        return mapper;
    }
    
    public CsvMapper smallMoleculeFeatureMapper() {
        CsvMapper mapper = metadataMapper();
        mapper.addMixIn(SmallMoleculeFeature.class,
            SmallMoleculeFeatureFormat.class);
        return mapper;
    }
    
    public CsvMapper smallMoleculeEvidenceMapper() {
        CsvMapper mapper = metadataMapper();
        mapper.addMixIn(SmallMoleculeEvidence.class,
            SmallMoleculeEvidenceFormat.class);
        return mapper;
    }
    
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
    
    public CsvSchema smallMoleculeSummarySchema(CsvMapper mapper, MzTab mzTabFile) {
        CsvSchema.Builder builder = mapper.schema().
            builder();
        builder.addColumn(SmallMoleculeSummary.HeaderPrefixEnum.SMH.getValue(), CsvSchema.ColumnType.STRING).
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
            addColumn(SmallMoleculeColumn.Stable.ADDUCT_IONS.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.RELIABILITY.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_MEASURE.
                getHeader(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeColumn.Stable.BEST_ID_CONFIDENCE_VALUE.
                getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING);
        mzTabFile.getMetadata().
            getAssay().
            forEach((assay) ->
            {
                builder.addColumn(SmallMoleculeSummary.Properties.abundanceAssay+"[" + assay.getId() + "]",
                    CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        mzTabFile.getMetadata().
            getStudyVariable().
            forEach((studyVariable) ->
            {
                builder.addColumn(SmallMoleculeSummary.Properties.abundanceStudyVariable+"[" + studyVariable.
                    getId() + "]", CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        mzTabFile.getMetadata().
            getStudyVariable().
            forEach((studyVariable) ->
            {
                builder.addColumn(
                    SmallMoleculeSummary.Properties.abundanceVariationStudyVariable+"[" + studyVariable.getId() + "]",
                    CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        mzTabFile.getSmallMoleculeSummary().
            forEach((SmallMoleculeSummary sms) ->
            {
                Optional.ofNullable(sms.getOpt()).
                    orElse(Collections.emptyList()).
                    forEach((ocm) ->
                    {
                        optColumns.putIfAbsent(Serializers.
                            printOptColumnMapping(ocm),
                            ocm);
                    });
            });
        optColumns.keySet().
            forEach((key) ->
            {
                builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        return defaultSchemaForBuilder(builder);
    }
    
    public CsvSchema smallMoleculeFeatureSchema(CsvMapper mapper, MzTab mzTabFile) {
        CsvSchema.Builder builder = mapper.schema().
            builder();
        builder.addColumn(SmallMoleculeFeature.HeaderPrefixEnum.SFH.getValue(), CsvSchema.ColumnType.STRING).
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
            addColumn(
                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS.
                    getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(
                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_START.
                    getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING).
            addColumn(
                SmallMoleculeFeatureColumn.Stable.RETENTION_TIME_IN_SECONDS_END.
                    getHeader(), CsvSchema.ColumnType.NUMBER_OR_STRING);

        Optional.ofNullable(mzTabFile.getMetadata().
            getAssay()).
            ifPresent((assayList) ->
                assayList.forEach((assay) ->
                {
                    builder.addColumn(SmallMoleculeFeature.Properties.abundanceAssay+"[" + assay.getId() + "]",
                        CsvSchema.ColumnType.NUMBER_OR_STRING);
                })
            );

        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        mzTabFile.getSmallMoleculeFeature().
            forEach((SmallMoleculeFeature smf) ->
            {
                Optional.ofNullable(smf.getOpt()).
                    orElse(Collections.emptyList()).
                    forEach((ocm) ->
                    {
                        optColumns.putIfAbsent(Serializers.
                            printOptColumnMapping(ocm),
                            ocm);
                    });
            });
        optColumns.keySet().
            forEach((key) ->
            {
                builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        return defaultSchemaForBuilder(builder);
    }
    
    public CsvSchema smallMoleculeEvidenceSchema(CsvMapper mapper, MzTab mzTabFile) {
        CsvSchema.Builder builder = mapper.schema().
            builder();
        builder.addColumn(SmallMoleculeEvidence.HeaderPrefixEnum.SEH.getValue(), CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.SME_ID.getHeader(),
                CsvSchema.ColumnType.STRING).
            addColumn(SmallMoleculeEvidenceColumn.Stable.EVIDENCE_INPUT_ID.
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
        Optional.ofNullable(mzTabFile.getMetadata().
            getIdConfidenceMeasure()).
            ifPresent((parameterList) ->
            {
                parameterList.forEach((param) ->
                {
                    builder.
                        addColumn(SmallMoleculeEvidence.Properties.idConfidenceMeasure+"[" + param.getId() + "]",
                            CsvSchema.ColumnType.NUMBER_OR_STRING);
                });
            });
        builder.addColumn(SmallMoleculeEvidenceColumn.Stable.RANK.getHeader(),
            CsvSchema.ColumnType.NUMBER_OR_STRING);
        Map<String, OptColumnMapping> optColumns = new LinkedHashMap<>();
        mzTabFile.getSmallMoleculeEvidence().
            forEach((SmallMoleculeEvidence sme) ->
            {
                Optional.ofNullable(sme.getOpt()).
                    orElse(Collections.emptyList()).
                    forEach((ocm) ->
                    {
                        optColumns.putIfAbsent(Serializers.
                            printOptColumnMapping(ocm),
                            ocm);
                    });
            });
        optColumns.keySet().
            forEach((key) ->
            {
                builder.addColumn(key, CsvSchema.ColumnType.NUMBER_OR_STRING);
            });
        return defaultSchemaForBuilder(builder);
    }
}

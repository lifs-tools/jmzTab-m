/*
 * Copyright 2024 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package org.lifstools.mztab2.io.validators;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MzTabProfile;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabError;

/**
 * Validates the mzTab-M profile of a parsed file. The profile declares which of
 * the four tables (metadata, small molecule summary, small molecule feature,
 * small molecule evidence) are present. This validator:
 * <ul>
 * <li>reads the declared {@code mzTab-profile} or, if absent, infers it from the
 * sections present (emitting an info message);</li>
 * <li>checks that the tables present match the declared/inferred profile and
 * flags prohibited (M+E, M+S+E) and discouraged (M+S+F) combinations;</li>
 * <li>enforces the profile-gated cross-reference rules on SMF_ID_REFS,
 * SME_ID_REFS and SME_ID_REF_ambiguity_code;</li>
 * <li>checks the conditional metadata fields that are only required when their
 * related table is present, tolerating (with an info mismatch message) fields
 * that are present although the related table is absent.</li>
 * </ul>
 *
 * @author nilshoffmann
 */
public class MzTabProfileValidator {

    private static final String SML_LABEL = "small molecule summary (SML)";
    private static final String SMF_LABEL = "small molecule feature (SMF)";
    private static final String SME_LABEL = "small molecule evidence (SME)";
    private static final String IDENTIFICATION_LABEL = "small molecule evidence (SME)";

    /**
     * Validate the profile of the given metadata against the sections actually
     * present in the file.
     *
     * @param metadata the parsed metadata
     * @param smallMoleculeSummaryMap the parsed SML rows (may be empty)
     * @param smallMoleculeFeatureMap the parsed SMF rows (may be empty)
     * @param smallMoleculeEvidenceMap the parsed SME rows (may be empty)
     * @return the list of profile validation errors, may be empty
     */
    public List<MZTabError> validateProfile(Metadata metadata,
        Map<Integer, SmallMoleculeSummary> smallMoleculeSummaryMap,
        Map<Integer, SmallMoleculeFeature> smallMoleculeFeatureMap,
        Map<Integer, SmallMoleculeEvidence> smallMoleculeEvidenceMap) {

        List<MZTabError> errors = new LinkedList<>();

        boolean hasSml = smallMoleculeSummaryMap != null && !smallMoleculeSummaryMap.isEmpty();
        boolean hasSmf = smallMoleculeFeatureMap != null && !smallMoleculeFeatureMap.isEmpty();
        boolean hasSme = smallMoleculeEvidenceMap != null && !smallMoleculeEvidenceMap.isEmpty();

        boolean identificationPerformed = hasSme || carriesIdentifiers(smallMoleculeSummaryMap);

        MzTabProfile declared = metadata.getMzTabProfile();
        String effectiveLabel;

        if (declared != null) {
            effectiveLabel = declared.getValue();
            checkTablesAgainstDeclaredProfile(declared, hasSml, hasSmf, hasSme, errors);
        } else {
            effectiveLabel = inferAndReport(hasSml, hasSmf, hasSme, errors);
        }

        // Conditional metadata fields: required only when their related table is
        // present, tolerated (info mismatch) when present but their table is absent.
        checkConditionalFields(metadata, hasSml, hasSmf, hasSme,
            identificationPerformed, effectiveLabel, errors);

        // Cross-reference rules gated on the sections actually present.
        checkCrossReferences(hasSml, hasSmf, hasSme,
            smallMoleculeSummaryMap, smallMoleculeFeatureMap, errors);

        return errors;
    }

    private boolean carriesIdentifiers(Map<Integer, SmallMoleculeSummary> smlMap) {
        if (smlMap == null) {
            return false;
        }
        for (SmallMoleculeSummary sml : smlMap.values()) {
            List<String> ids = sml.getDatabaseIdentifier();
            if (ids != null) {
                for (String id : ids) {
                    if (id != null && !id.trim().isEmpty() && !"null".equalsIgnoreCase(id.trim())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Infer the profile from the sections present, report prohibited and
     * discouraged combinations and emit an inferred-profile info message.
     *
     * @return the effective profile label for use in downstream messages
     */
    private String inferAndReport(boolean hasSml, boolean hasSmf, boolean hasSme,
        List<MZTabError> errors) {
        int code = (hasSml ? 4 : 0) | (hasSmf ? 2 : 0) | (hasSme ? 1 : 0);
        switch (code) {
            case 0: // M
                errors.add(new MZTabError(LogicalErrorType.ProfileInferred, -1, MzTabProfile.M.getValue()));
                return MzTabProfile.M.getValue();
            case 4: // SML -> M+S
                errors.add(new MZTabError(LogicalErrorType.ProfileInferred, -1, MzTabProfile.M_S.getValue()));
                return MzTabProfile.M_S.getValue();
            case 2: // SMF -> M+F
                errors.add(new MZTabError(LogicalErrorType.ProfileInferred, -1, MzTabProfile.M_F.getValue()));
                return MzTabProfile.M_F.getValue();
            case 3: // SMF+SME -> M+F+E
                errors.add(new MZTabError(LogicalErrorType.ProfileInferred, -1, MzTabProfile.M_F_E.getValue()));
                return MzTabProfile.M_F_E.getValue();
            case 7: // SML+SMF+SME -> M+S+F+E
                errors.add(new MZTabError(LogicalErrorType.ProfileInferred, -1, MzTabProfile.M_S_F_E.getValue()));
                return MzTabProfile.M_S_F_E.getValue();
            case 6: // SML+SMF, no SME -> M+S+F (tolerated but discouraged)
                errors.add(new MZTabError(LogicalErrorType.ProfileMSFDiscouraged, -1));
                return "M+S+F";
            case 1: // SME only -> M+E (prohibited)
                errors.add(new MZTabError(LogicalErrorType.ProfileUnsupportedCombination, -1, "M+E"));
                return "M+E";
            case 5: // SML+SME, no SMF -> M+S+E (prohibited)
            default:
                errors.add(new MZTabError(LogicalErrorType.ProfileUnsupportedCombination, -1, "M+S+E"));
                return "M+S+E";
        }
    }

    private void checkTablesAgainstDeclaredProfile(MzTabProfile declared,
        boolean hasSml, boolean hasSmf, boolean hasSme, List<MZTabError> errors) {
        boolean expectSml;
        boolean expectSmf;
        boolean expectSme;
        switch (declared) {
            case M:
                expectSml = false; expectSmf = false; expectSme = false;
                break;
            case M_S:
                expectSml = true; expectSmf = false; expectSme = false;
                break;
            case M_F:
                expectSml = false; expectSmf = true; expectSme = false;
                break;
            case M_F_E:
                expectSml = false; expectSmf = true; expectSme = true;
                break;
            case M_S_F_E:
            default:
                expectSml = true; expectSmf = true; expectSme = true;
                break;
        }
        checkTable(declared, expectSml, hasSml, SML_LABEL, errors);
        checkTable(declared, expectSmf, hasSmf, SMF_LABEL, errors);
        checkTable(declared, expectSme, hasSme, SME_LABEL, errors);
    }

    private void checkTable(MzTabProfile declared, boolean expected, boolean present,
        String label, List<MZTabError> errors) {
        if (expected && !present) {
            errors.add(new MZTabError(LogicalErrorType.ProfileTableMissing, -1,
                declared.getValue(), label));
        } else if (!expected && present) {
            errors.add(new MZTabError(LogicalErrorType.ProfileTableForbidden, -1,
                declared.getValue(), label));
        }
    }

    private void checkConditionalFields(Metadata metadata, boolean hasSml,
        boolean hasSmf, boolean hasSme, boolean identificationPerformed,
        String effectiveLabel, List<MZTabError> errors) {

        // quantification_method: required when SML or SMF present.
        conditionalField(metadata.getQuantificationMethod() != null,
            hasSml || hasSmf, Metadata.JSON_PROPERTY_QUANTIFICATION_METHOD,
            SML_LABEL + "/" + SMF_LABEL, effectiveLabel, errors);

        // small_molecule-quantification_unit: required when SML present.
        conditionalField(metadata.getSmallMoleculeQuantificationUnit() != null,
            hasSml, Metadata.JSON_PROPERTY_SMALL_MOLECULE_QUANTIFICATION_UNIT,
            SML_LABEL, effectiveLabel, errors);

        // small_molecule_feature-quantification_unit: required when SMF present.
        conditionalField(metadata.getSmallMoleculeFeatureQuantificationUnit() != null,
            hasSmf, Metadata.JSON_PROPERTY_SMALL_MOLECULE_FEATURE_QUANTIFICATION_UNIT,
            SMF_LABEL, effectiveLabel, errors);

        // id_confidence_measure[1-n]: required when SME present.
        conditionalField(metadata.getIdConfidenceMeasure() != null
            && !metadata.getIdConfidenceMeasure().isEmpty(),
            hasSme, Metadata.JSON_PROPERTY_ID_CONFIDENCE_MEASURE,
            SME_LABEL, effectiveLabel, errors);

        // database[1-n]: required when identification was performed.
        conditionalField(metadata.getDatabase() != null
            && !metadata.getDatabase().isEmpty(),
            identificationPerformed, Metadata.JSON_PROPERTY_DATABASE,
            IDENTIFICATION_LABEL, effectiveLabel, errors);
    }

    private void conditionalField(boolean present, boolean required,
        String fieldName, String sectionLabel, String effectiveLabel,
        List<MZTabError> errors) {
        if (required && !present) {
            errors.add(new MZTabError(LogicalErrorType.NotDefineInMetadata, -1, fieldName));
        } else if (!required && present) {
            errors.add(new MZTabError(LogicalErrorType.ProfileFieldMismatch, -1,
                fieldName, effectiveLabel, sectionLabel));
        }
    }

    private void checkCrossReferences(boolean hasSml, boolean hasSmf, boolean hasSme,
        Map<Integer, SmallMoleculeSummary> smlMap,
        Map<Integer, SmallMoleculeFeature> smfMap, List<MZTabError> errors) {

        if (hasSml) {
            for (SmallMoleculeSummary sml : smlMap.values()) {
                boolean hasRefs = sml.getSmfIdRefs() != null && !sml.getSmfIdRefs().isEmpty();
                if (!hasSmf && hasRefs) {
                    // M+S: no feature section, so SMF_ID_REFS MUST be null.
                    errors.add(new MZTabError(LogicalErrorType.ProfileSmfIdRefsForbidden,
                        -1, String.valueOf(sml.getSmlId())));
                } else if (hasSmf && hasSme && !hasRefs) {
                    // M+S+F+E: each summary MUST reference at least one feature.
                    errors.add(new MZTabError(LogicalErrorType.ProfileSmfIdRefsRequired,
                        -1, String.valueOf(sml.getSmlId())));
                }
            }
        }

        if (hasSmf) {
            for (SmallMoleculeFeature smf : smfMap.values()) {
                boolean hasRefs = smf.getSmeIdRefs() != null && !smf.getSmeIdRefs().isEmpty();
                if (!hasSme && hasRefs) {
                    // M+F: no evidence section, so SME_ID_REFS MUST be null.
                    errors.add(new MZTabError(LogicalErrorType.ProfileSmeIdRefsForbidden,
                        -1, String.valueOf(smf.getSmfId())));
                }
                // A feature without SME_ID_REFS is an unidentified feature and is
                // legitimate even when an evidence section is present, so there is
                // no per-feature "must reference evidence" check here.

                // Ambiguity code MUST be null when SME_ID_REFS is null.
                if (!hasRefs && smf.getSmeIdRefAmbiguityCode() != null) {
                    errors.add(new MZTabError(LogicalErrorType.ProfileAmbiguityCodeMustBeNull,
                        -1, String.valueOf(smf.getSmfId())));
                }
            }
        }
    }
}

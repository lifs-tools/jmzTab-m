/*
 * Copyright 2020 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package de.isas.mztab2.model;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.REGEX_ABUNDANCE_ASSAY_COLUMN_NAME;

/**
 * Utility class for easier access to linked information within an mztab-M data
 * object.
 *
 * @author nilshoffmann
 */
public class MzTabAccess {

    private final MzTab mzTab;

    /**
     * Create an MzTabAccess object for the provided mzTab object.
     *
     * @param mzTab
     */
    public MzTabAccess(MzTab mzTab) {
        this.mzTab = mzTab;
    }

    /**
     * Retrieve all small molecule features in order of definition for the
     * provided small molecule summary.
     *
     * @param sms the small molecule summary.
     * @return the list of small molecule features.
     */
    public List<SmallMoleculeFeature> getFeatures(SmallMoleculeSummary sms) {
        List<Integer> smfIds = sms.getSmfIdRefs();
        return mzTab.getSmallMoleculeFeature().stream().filter((t) -> {
            return smfIds.contains(t.getSmfId());
        }).collect(Collectors.toList());
    }

    /**
     * Retrieve all small molecule evidences in order of definition for the
     * provided small molecule feature.
     *
     * @param smf the small molecule feature.
     * @return the list of small molecule evidences.
     */
    public List<SmallMoleculeEvidence> getEvidences(SmallMoleculeFeature smf) {
        List<Integer> smeIds = smf.getSmeIdRefs();
        return mzTab.getSmallMoleculeEvidence().stream().filter((t) -> {
            return smeIds.contains(t.getSmeId());
        }).collect(Collectors.toList());
    }

    /**
     * Retrieve all small molecule evidences for the provided evidence input id.
     *
     * @param evidenceInputId the evidence input it.
     * @return the list of small molecule evidences.
     */
    public List<SmallMoleculeEvidence> getEvidencesByEvidenceInputId(String evidenceInputId) {
        return mzTab.getSmallMoleculeEvidence().stream().filter((t) -> {
            return t.getEvidenceInputId().equals(evidenceInputId);
        }).collect(Collectors.toList());
    }

    /**
     * Retrieves the abundance value for the provided assay and small molecule
     * feature object.
     *
     * @param assay the assay.
     * @param smf the small molecule feature object.
     * @return the feature abundance. May be null.
     */
    public Double getAbundanceFor(Assay assay, SmallMoleculeFeature smf) {
        return smf.getAbundanceAssay().get(assay.getId() - 1);
    }

    /**
     * Retrieves the abundance value for the provided study variable and small
     * molecule summary object.
     *
     * @param studyVariable the study variable.
     * @param sms the small molecule summary object.
     * @return the study variable abundance. May be null.
     */
    public Double getAbundanceFor(StudyVariable studyVariable, SmallMoleculeSummary sms) {
        return sms.getAbundanceStudyVariable().get(studyVariable.getId() - 1);
    }

    /**
     * Retrieves the abundance variation value for the provided study variable
     * and small molecule summary object.
     *
     * @param studyVariable the study variable.
     * @param sms the small molecule summary object.
     * @return the study variable abundance variation. May be null.
     */
    public Double getAbundanceVariationFor(StudyVariable studyVariable, SmallMoleculeSummary sms) {
        return sms.getAbundanceVariationStudyVariable().get(studyVariable.getId() - 1);
    }

    /**
     * Retrieves the abundance value for the provided assay and small molecule
     * summary object.
     *
     * @param assay the assay.
     * @param sms the small molecule summary object.
     * @return the assay abundance. May be null.
     */
    public Double getAbundanceFor(Assay assay, SmallMoleculeSummary sms) {
        return sms.getAbundanceAssay().get(assay.getId() - 1);
    }

    /**
     * Tries to locate the assay referenced by id from the metadata section.
     *
     * @param id the assay id.
     * @param metadata the metadata used to locate the assay.
     * @return an optional with the assay object, or an empty optiona
     */
    public Optional<Assay> getAssayFor(Integer id, Metadata metadata) {
        return metadata.getAssay().stream().filter((t) -> {
            return t.getId().equals(id);
        }).findFirst();
    }

    /**
     * Tries to locate the study variable referenced by id from the metadata
     * section.
     *
     * @param id the study variable id.
     * @param metadata the metadata used to locate the study variable.
     * @return an optional with the study variable object, or an empty optiona
     */
    public Optional<StudyVariable> getStudyVariableFor(Integer id, Metadata metadata) {
        return metadata.getStudyVariable().stream().filter((t) -> {
            return t.getId().equals(id);
        }).findFirst();
    }

    /**
     * Tries to locate the ms run referenced by id from the metadata section.
     *
     * @param id the ms run id.
     * @param metadata the metadata used to locate the ms run.
     * @return an optional with the ms run object, or an empty optional.
     */
    public Optional<MsRun> getMsRunFor(Integer id, Metadata metadata) {
        return metadata.getMsRun().stream().filter((t) -> {
            return t.getId().equals(id);
        }).findFirst();
    }

    /**
     * Tries to locate the database referenced by id from the metadata section.
     *
     * @param id the database id.
     * @param metadata the metadata used to locate the database.
     * @return an optional with the database object, or an empty optional.
     */
    public Optional<Database> getDatabaseFor(Integer id, Metadata metadata) {
        return metadata.getDatabase().stream().filter((t) -> {
            return t.getId().equals(id);
        }).findFirst();
    }

    /**
     * Tries to locate the assay referenced by id from the columnMapping
     * identifier. Returns an empty optional if either the identifier was null
     * or not an assay, or no matching assay was found.
     *
     * @param columnMapping the column mapping.
     * @param metadata the metadata used to locate the assay.
     * @return an optional with the assay object, or an empty optional.
     */
    public Optional<Assay> getAssayFor(OptColumnMapping columnMapping, Metadata metadata) {
        String identifier = columnMapping.getIdentifier();
        if (identifier != null) {
            Pattern p = Pattern.compile(REGEX_ABUNDANCE_ASSAY_COLUMN_NAME);
            Matcher m = p.matcher(identifier);
            if (m.find()) {
                Integer assayId = Integer.parseInt(m.group(1));
                return getAssayFor(assayId, metadata);
            }
        }
        return Optional.empty();
    }
}

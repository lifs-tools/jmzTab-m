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
import java.util.stream.Collectors;

/**
 *
 * @author nilshoffmann
 */
public class MzTabAccess {

    private final MzTab mzTab;

    public MzTabAccess(MzTab mzTab) {
        this.mzTab = mzTab;
    }

    public List<SmallMoleculeFeature> getFeatures(SmallMoleculeSummary sms) {
        List<Integer> smfIds = sms.getSmfIdRefs();
        return mzTab.getSmallMoleculeFeature().stream().filter((t) -> {
            return smfIds.contains(t.getSmfId());
        }).collect(Collectors.toList());
    }

    public List<SmallMoleculeEvidence> getEvidences(SmallMoleculeFeature smf) {
        List<Integer> smeIds = smf.getSmeIdRefs();
        return mzTab.getSmallMoleculeEvidence().stream().filter((t) -> {
            return smeIds.contains(t.getSmeId());
        }).collect(Collectors.toList());
    }
    
    public List<SmallMoleculeEvidence> getEvidencesByEvidenceInputId(String evidenceInputId) {
        return mzTab.getSmallMoleculeEvidence().stream().filter((t) -> {
            return t.getEvidenceInputId().equals(evidenceInputId);
        }).collect(Collectors.toList());
    }

    public Double getAbundanceFor(Assay assay, SmallMoleculeFeature smf) {
        return smf.getAbundanceAssay().get(assay.getId() - 1);
    }

    public Double getAbundanceFor(StudyVariable studyVariable, SmallMoleculeSummary sms) {
        return sms.getAbundanceStudyVariable().get(studyVariable.getId() - 1);
    }

    public Double getAbundanceVariationFor(StudyVariable studyVariable, SmallMoleculeSummary sms) {
        return sms.getAbundanceVariationStudyVariable().get(studyVariable.getId() - 1);
    }

    public Double getAbundanceFor(Assay assay, SmallMoleculeSummary sms) {
        return sms.getAbundanceAssay().get(assay.getId() - 1);
    }

}

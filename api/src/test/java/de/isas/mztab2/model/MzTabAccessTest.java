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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author nilshoffmann
 */
public class MzTabAccessTest {

    public MzTabAccessTest() {
    }

    @Test
    public void testGetFeatures() {
        SmallMoleculeFeature smf1 = new SmallMoleculeFeature().smfId(1).charge(1);
        SmallMoleculeFeature smf2 = new SmallMoleculeFeature().smfId(2).charge(3);
        SmallMoleculeSummary sms = new SmallMoleculeSummary().smlId(1).addSmfIdRefsItem(smf1.getSmfId()).addSmfIdRefsItem(smf2.getSmfId());
        MzTab mzTab = new MzTab().addSmallMoleculeSummaryItem(sms).addSmallMoleculeFeatureItem(smf1).addSmallMoleculeFeatureItem(smf2);
        MzTabAccess mzTabAccess = new MzTabAccess(mzTab);
        List<SmallMoleculeFeature> features = mzTabAccess.getFeatures(sms);
        assertEquals(2, features.size());
        assertEquals(Integer.valueOf(1), features.get(0).getSmfId());
        assertEquals(Integer.valueOf(1), features.get(0).getCharge());
        assertEquals(Integer.valueOf(2), features.get(1).getSmfId());
        assertEquals(Integer.valueOf(3), features.get(1).getCharge());
    }

    @Test
    public void testGetEvidences() {
        SmallMoleculeEvidence sme1 = new SmallMoleculeEvidence().smeId(1).charge(1);
        SmallMoleculeEvidence sme2 = new SmallMoleculeEvidence().smeId(2).charge(3);
        SmallMoleculeFeature smf = new SmallMoleculeFeature().smfId(1).addSmeIdRefsItem(sme1.getSmeId()).addSmeIdRefsItem(sme2.getSmeId());
        MzTab mzTab = new MzTab().addSmallMoleculeFeatureItem(smf).addSmallMoleculeEvidenceItem(sme1).addSmallMoleculeEvidenceItem(sme2);
        MzTabAccess mzTabAccess = new MzTabAccess(mzTab);
        List<SmallMoleculeEvidence> features = mzTabAccess.getEvidences(smf);
        assertEquals(2, features.size());
        assertEquals(Integer.valueOf(1), features.get(0).getSmeId());
        assertEquals(Integer.valueOf(1), features.get(0).getCharge());
        assertEquals(Integer.valueOf(2), features.get(1).getSmeId());
        assertEquals(Integer.valueOf(3), features.get(1).getCharge());
    }

    @Test
    public void testGetEvidencesByEvidenceInputId() {
        
    }

    @Test
    public void testGetAbundanceFor_Assay_SmallMoleculeFeature() {
    }

    @Test
    public void testGetAbundanceFor_StudyVariable_SmallMoleculeSummary() {
    }

    @Test
    public void testGetAbundanceVariationFor() {
    }

    @Test
    public void testGetAbundanceFor_Assay_SmallMoleculeSummary() {
    }

    @Test
    public void testGetAssayFor_Integer_Metadata() {
    }

    @Test
    public void testGetStudyVariableFor() {
    }

    @Test
    public void testGetMsRunFor() {
    }

    @Test
    public void testGetDatabaseFor() {
    }

    @Test
    public void testGetAssayFor_OptColumnMapping_Metadata() {
        Assay assay = new Assay().id(1).name("first assay");
        Metadata metadata = new Metadata();
        metadata.addAssayItem(assay);
        OptColumnMapping mapping = new OptColumnMapping().identifier("assay[1]").param(new Parameter().cvAccession("MS").cvLabel("MS:123456").value("just for testing"));
        MzTab mzTab = new MzTab().metadata(metadata);
        MzTabAccess access = new MzTabAccess(mzTab);
        Optional<Assay> foundAssay = access.getAssayFor(mapping, metadata);
        assertTrue(foundAssay.isPresent());
        assertEquals(assay.getId(), foundAssay.get().getId());
    }

}

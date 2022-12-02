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
package org.lifstools.mztab2.model;

import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.SmallMoleculeEvidence;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import org.lifstools.mztab2.model.StudyVariable;
import org.lifstools.mztab2.model.MzTabAccess;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for MzTabAccess.
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
        SmallMoleculeEvidence sme1 = new SmallMoleculeEvidence().smeId(1).charge(1).evidenceInputId("inputId1");
        SmallMoleculeEvidence sme2 = new SmallMoleculeEvidence().smeId(2).charge(3).evidenceInputId("inputId1");
        SmallMoleculeFeature smf = new SmallMoleculeFeature().smfId(1).addSmeIdRefsItem(sme1.getSmeId()).addSmeIdRefsItem(sme2.getSmeId());
        MzTab mzTab = new MzTab().addSmallMoleculeFeatureItem(smf).addSmallMoleculeEvidenceItem(sme1).addSmallMoleculeEvidenceItem(sme2);
        MzTabAccess mzTabAccess = new MzTabAccess(mzTab);
        List<SmallMoleculeEvidence> features = mzTabAccess.getEvidencesByEvidenceInputId("inputId1");
        assertEquals(2, features.size());
        assertEquals(Integer.valueOf(1), features.get(0).getSmeId());
        assertEquals(Integer.valueOf(1), features.get(0).getCharge());
        assertEquals(Integer.valueOf(2), features.get(1).getSmeId());
        assertEquals(Integer.valueOf(3), features.get(1).getCharge());
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
        Assay assay = new Assay().id(1).name("123");
        Metadata metadata = new Metadata();
        metadata.addAssayItem(assay);
        MzTab mzTab = new MzTab().metadata(metadata);
        MzTabAccess access = new MzTabAccess(mzTab);
        Optional<Assay> foundAssay = access.getAssayFor(1, metadata);
        assertTrue(foundAssay.isPresent());
        assertEquals(assay.getId(), foundAssay.get().getId());
    }

    @Test
    public void testGetStudyVariableFor() {
        StudyVariable studyVariable = new StudyVariable().id(1).name("123");
        Metadata metadata = new Metadata();
        metadata.addStudyVariableItem(studyVariable);
        MzTab mzTab = new MzTab().metadata(metadata);
        MzTabAccess access = new MzTabAccess(mzTab);
        Optional<StudyVariable> foundStudyVariable = access.getStudyVariableFor(1, metadata);
        assertTrue(foundStudyVariable.isPresent());
        assertEquals(studyVariable.getId(), foundStudyVariable.get().getId());
    }

    @Test
    public void testGetMsRunFor() {
        MsRun msRun = new MsRun().id(1).location("123");
        Metadata metadata = new Metadata();
        metadata.addMsRunItem(msRun);
        MzTab mzTab = new MzTab().metadata(metadata);
        MzTabAccess access = new MzTabAccess(mzTab);
        Optional<MsRun> foundMsRun = access.getMsRunFor(1, metadata);
        assertTrue(foundMsRun.isPresent());
        assertEquals(msRun.getId(), foundMsRun.get().getId());
    }

    @Test
    public void testGetDatabaseFor() {
        Database db = new Database().id(1).version("123");
        Metadata metadata = new Metadata();
        metadata.addDatabaseItem(db);
        MzTab mzTab = new MzTab().metadata(metadata);
        MzTabAccess access = new MzTabAccess(mzTab);
        Optional<Database> foundDb = access.getDatabaseFor(1, metadata);
        assertTrue(foundDb.isPresent());
        assertEquals(db.getId(), foundDb.get().getId());
    }

    @Test
    public void testGetAssayFor_OptColumnMapping_Metadata() {
        Assay assay = new Assay().id(1).name("first assay");
        Metadata metadata = new Metadata();
        metadata.addAssayItem(assay);
        OptColumnMapping mapping = new OptColumnMapping().identifier("opt_assay[1]_cv_MS:128712_made_up_for_testing").param(new Parameter().cvAccession("MS").cvLabel("MS:123456").value("just for testing"));
        MzTab mzTab = new MzTab().metadata(metadata);
        MzTabAccess access = new MzTabAccess(mzTab);
        Optional<Assay> foundAssay = access.getAssayFor(mapping, metadata);
        assertTrue(foundAssay.isPresent());
        assertEquals(assay.getId(), foundAssay.get().getId());
    }

}

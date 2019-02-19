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
package de.isas.mztab2.io.serialization;

import com.fasterxml.jackson.databind.ObjectWriter;
import de.isas.mztab2.io.AbstractSerializerTest;
import static de.isas.mztab2.io.MzTabTestData.create2_0TestFile;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.SmallMoleculeEvidence;
import de.isas.mztab2.model.SpectraRef;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab2.model.IOptColumnMappingBuilder;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilder;

/**
 *
 * @author nilshoffmann
 */
public class SmallMoleculeEvidenceSerializerTest extends AbstractSerializerTest {

    public SmallMoleculeEvidenceSerializerTest() {
    }

    /**
     * Test of serializeSingle method, of class SmallMoleculeEvidenceSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        MzTab mzTabFile = create2_0TestFile();
        IOptColumnMappingBuilder qualifierRef = OptColumnMappingBuilder.
            forGlobal().
            withName("qualifiers_evidence_grouping_ID_REFS");
        SmallMoleculeEvidence sme1 = new SmallMoleculeEvidence().smeId(
            1).
            evidenceInputId("" + 1).
            databaseIdentifier("LM:LMSP0501AB02").
            chemicalFormula("C42H83NO3").
            smiles(
                "CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC").
            inchi(
                "InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1").
            chemicalName("LacCer d18:1/12:0").
            uri("http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012").
            adductIon("[M+H]1+").
            expMassToCharge(650.6432).
            charge(1).
            theoreticalMassToCharge(650.6446).
            spectraRef(Arrays.asList(new SpectraRef().msRun(new MsRun().id(1)).
                reference("index=731"))).
            identificationMethod(new Parameter().name(
                "qualifier ions exact mass")).
            msLevel(new Parameter().cvAccession("MS:100511").
                cvLabel("MS").
                name("ms level").
                value("1")).
            addIdConfidenceMeasureItem(0.958).
            rank(1).
            addOptItem(qualifierRef.build("" + 2));
        SmallMoleculeEvidence sme2 = new SmallMoleculeEvidence().smeId(2).
            evidenceInputId("" + 2).
            chemicalFormula("C17H33N").
            chemicalName("Cer d18:1/24:0 W' - CHO").
            expMassToCharge(252.2677).
            charge(1).
            theoreticalMassToCharge(252.2686).
            addSpectraRefItem(new SpectraRef().msRun(new MsRun().id(1)).
                reference("index=732")).
            identificationMethod(new Parameter().name("exact mass")).
            msLevel(new Parameter().cvAccession("MS:100511").
                cvLabel("MS").
                name("ms level").
                value("2")).
            addIdConfidenceMeasureItem(0.9780).
            rank(1).
            addOptItem(qualifierRef.
                build(null));
        mzTabFile.addSmallMoleculeEvidenceItem(sme1).
            addSmallMoleculeEvidenceItem(sme2);
        ObjectWriter writer = smallMoleculeEvidenceWriter(mzTabFile);
        String serializedString = serializeSequence(writer, mzTabFile.
            getSmallMoleculeEvidence());
        Assert.assertFalse(serializedString.isEmpty());
        System.out.println(serializedString);
        //check for exactly one header line + 2 entry lines
        Assert.assertEquals(3,
            serializedString.split(MZTabConstants.NEW_LINE).length);
        String expected = "SEH	SME_ID	evidence_input_id	database_identifier	chemical_formula	smiles	inchi	chemical_name	uri	derivatized_form	adduct_ion	exp_mass_to_charge	charge	theoretical_mass_to_charge	spectra_ref	identification_method	ms_level	id_confidence_measure[1]	rank	opt_global_qualifiers_evidence_grouping_ID_REFS" + MZTabConstants.NEW_LINE
            + "SME	1	1	LM:LMSP0501AB02	C42H83NO3	CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC	InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1	LacCer d18:1/12:0	http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012	null	[M+H]1+	650.6432	1	650.6446	ms_run[1]:index=731	[, , qualifier ions exact mass, ]	[MS, MS:100511, ms level, 1]	0.958	1	2" + MZTabConstants.NEW_LINE
            + "SME	2	2	null	C17H33N	null	null	Cer d18:1/24:0 W' - CHO	null	null	null	252.2677	1	252.2686	ms_run[1]:index=732	[, , exact mass, ]	[MS, MS:100511, ms level, 2]	0.978	1	null" + MZTabConstants.NEW_LINE;
        assertEqSentry(expected, serializedString);

    }

}

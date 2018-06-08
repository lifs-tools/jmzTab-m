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
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.SmallMoleculeEvidence;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author nilshoffmann
 */
public class SmallMoleculeEvidenceSerializerTest extends AbstractSerializerTest {

    public SmallMoleculeEvidenceSerializerTest() {
    }

    /**
     * Test of serialize method, of class SmallMoleculeEvidenceSerializer.
     */
    @Ignore
    @Test
    public void testSerialize() throws Exception {
        MzTab mzTabFile = create2_0TestFile();
        SmallMoleculeEvidence sme1 = new SmallMoleculeEvidence();
        /*
        COM	Evidence rows for parent / fragment ions.																				
COM	Primary use case: report single hits from spectral library or accurate mass searches without quantification. -> Qualification																				
SEH	SME_ID	evidence_input_id	database_identifier	chemical_formula	smiles	inchi	chemical_name	uri	derivatized_form	adduct_ion	exp_mass_to_charge	charge	theoretical_mass_to_charge	opt_global_mass_error	spectra_ref	identification_method	ms_level	id_confidence_measure[1]	rank	opt_global_qualifiers_evidence_grouping_ID_REFS	opt_global_scan_polarity
SME	1	1	LM:LMSP0501AB02	C42H83NO3	CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC	InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1	LacCer d18:1/12:0 | C12 lactosyl ceramide | N-(dodecanoyl)-1-b-lactosyl-sphing-4-enine	http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012	null	[M+H]1+	650.6432	1	650.6446	-2.1517	ms_run[1]:index=731	[,, qualifier ions exact mass,]	[MS,MS:100511, ms level, 1]	0.958	1	2	[MS, MS:1000130, positive scan, ]
SME	2	2	null	C17H33N	null	null	Cer d18:1/24:0 W' - CHO	null	null	null	252.2677	1	252.2686	-3.5676	ms_run[1]:index=732	[,, exact mass, ]	[MS,MS:100511, ms level, 2]	0.9780	1	null	[MS, MS:1000130, positive scan, ]
SME	3	2	null	C18H33N	null	null	Cer d18:1/24:0 W''	null	null	null	264.2689	1	264.2686	-1.1352	ms_run[1]:index=732	[,, exact mass, ]	[MS,MS:100511, ms level, 2]	0.7500	1	null	[MS, MS:1000130, positive scan, ]
SME	4	2	null	C18H35NO	null	null	Cer d18:1/24:0 W'	null	null	null	282.2788	1	282.2791	-1.0628	ms_run[1]:index=732	[,, exact mass, ]	[MS,MS:100511, ms level, 2]	0.8760	1	null	[MS, MS:1000130, positive scan, ]

         */
        mzTabFile.addSmallMoleculeEvidenceItem(sme1);
        ObjectWriter writer = smallMoleculeEvidenceWriter(mzTabFile);
        serialize(writer, mzTabFile);
    }

}

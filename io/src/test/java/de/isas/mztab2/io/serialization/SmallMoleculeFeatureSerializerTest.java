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
import de.isas.mztab2.model.SmallMoleculeFeature;
import org.junit.Test;
import org.junit.Ignore;

/**
 * 
 * @author nilshoffmann
 */
public class SmallMoleculeFeatureSerializerTest extends AbstractSerializerTest {
    
    public SmallMoleculeFeatureSerializerTest() {
    }

    /**
     * Test of serialize method, of class SmallMoleculeFeatureSerializer.
     */
    @Ignore
    @Test
    public void testSerialize() throws Exception {
        MzTab mzTabFile = create2_0TestFile();
        SmallMoleculeFeature smf1 = new SmallMoleculeFeature();
    /*
        COM	MS feature rows , used to report m/z and individual abundance information for quantification																				
SFH	SMF_ID	SME_ID_REFS	SME_ID_REF_ambiguity_code	adduct_ion	isotopomer	exp_mass_to_charge	charge	retention_time_in_seconds	retention_time_in_seconds_start	retention_time_in_seconds_end	abundance_assay[1]	opt_global_quantifiers_SMF_ID_REFS									
SMF	1	1	null	[M+H]1+	null	650.6432	1	821.2341	756.0000	954.0000	4.448784E-05	3									
SMF	2	2	null	null	null	252.2677	1	821.2341	756.0000	954.0000	6.673176E-06	null									
SMF	3	3	null	null	null	264.2689	1	821.2341	756.0000	954.0000	1.3346352E-05	null									
SMF	4	4	null	null	null	282.2788	1	821.2341	756.0000	954.0000	9.831813E-06	null									
        */
    
        mzTabFile.addSmallMoleculeFeatureItem(smf1);
        ObjectWriter writer = smallMoleculeFeatureWriter(mzTabFile);
        serialize(writer, mzTabFile);
    }
    
}

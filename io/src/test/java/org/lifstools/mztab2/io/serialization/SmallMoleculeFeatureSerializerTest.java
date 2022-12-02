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
package org.lifstools.mztab2.io.serialization;

import com.fasterxml.jackson.databind.ObjectWriter;
import org.lifstools.mztab2.io.AbstractSerializerTest;
import static org.lifstools.mztab2.io.MzTabTestData.create2_0TestFile;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.OptColumnMapping;
import org.lifstools.mztab2.model.SmallMoleculeFeature;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;

/**
 *
 * @author nilshoffmann
 */
public class SmallMoleculeFeatureSerializerTest extends AbstractSerializerTest {

    public SmallMoleculeFeatureSerializerTest() {
    }

    /**
     * Test of serializeSingle method, of class SmallMoleculeFeatureSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        MzTab mzTabFile = create2_0TestFile();
        String optColumnQualifiers = "opt_global_quantifiers_SMF_ID_REFS";
        mzTabFile.addSmallMoleculeFeatureItem(new SmallMoleculeFeature().
            smfId(1).
            smeIdRefs(Arrays.asList(1)).
            adductIon("[M+H]1+").
            expMassToCharge(650.6432).
            charge(1).
            retentionTimeInSeconds(821.2341).
            retentionTimeInSecondsStart(756.0000).
            retentionTimeInSecondsEnd(954.0000).
            abundanceAssay(Arrays.asList(4.448784E-05)).
            opt(Arrays.asList(new OptColumnMapping().identifier(
                optColumnQualifiers).
                value("3"))));
        mzTabFile.addSmallMoleculeFeatureItem(new SmallMoleculeFeature().
            smfId(2).
            smeIdRefs(Arrays.asList(2)).
            expMassToCharge(252.2677).
            charge(1).
            retentionTimeInSeconds(821.2341).
            retentionTimeInSecondsStart(756.0000).
            retentionTimeInSecondsEnd(954.0000).
            abundanceAssay(Arrays.asList(6.673176E-06)).
            opt(Arrays.asList(new OptColumnMapping().identifier(
                optColumnQualifiers))));
        mzTabFile.addSmallMoleculeFeatureItem(new SmallMoleculeFeature().
            smfId(3).
            smeIdRefs(Arrays.asList(3)).
            expMassToCharge(264.2689).
            charge(1).
            retentionTimeInSeconds(821.2341).
            retentionTimeInSecondsStart(756.0000).
            retentionTimeInSecondsEnd(954.0000).
            abundanceAssay(Arrays.asList(1.3346352E-05)).
            opt(Arrays.asList(new OptColumnMapping().identifier(
                optColumnQualifiers))));
        mzTabFile.addSmallMoleculeFeatureItem(new SmallMoleculeFeature().
            smfId(4).
            smeIdRefs(Arrays.asList(3)).
            expMassToCharge(282.2788).
            charge(1).
            retentionTimeInSeconds(821.2341).
            retentionTimeInSecondsStart(756.0000).
            retentionTimeInSecondsEnd(954.0000).
            abundanceAssay(Arrays.asList(9.831813E-06)).
            opt(Arrays.asList(new OptColumnMapping().identifier(
                optColumnQualifiers))));

        ObjectWriter writer = smallMoleculeFeatureWriter(mzTabFile);
        String serializedString = serializeSequence(writer, mzTabFile.
            getSmallMoleculeFeature());
        Assert.assertFalse(serializedString.isEmpty());
        System.out.println(serializedString);
        //check for exactly one header line + 4 entry lines
        Assert.assertEquals(5,
            serializedString.split(MZTabConstants.NEW_LINE).length);

        String reference = "SFH	SMF_ID	SME_ID_REFS	SME_ID_REF_ambiguity_code	adduct_ion	isotopomer	exp_mass_to_charge	charge	retention_time_in_seconds	retention_time_in_seconds_start	retention_time_in_seconds_end	abundance_assay[1]	abundance_assay[2]	opt_global_quantifiers_SMF_ID_REFS"+MZTabConstants.NEW_LINE
            + "SMF	1	1	null	[M+H]1+	null	650.6432	1	821.2341	756.0	954.0	4.448784E-5		3"+MZTabConstants.NEW_LINE
            + "SMF	2	2	null	null	null	252.2677	1	821.2341	756.0	954.0	6.673176E-6		null"+MZTabConstants.NEW_LINE
            + "SMF	3	3	null	null	null	264.2689	1	821.2341	756.0	954.0	1.3346352E-5		null"+MZTabConstants.NEW_LINE
            + "SMF	4	3	null	null	null	282.2788	1	821.2341	756.0	954.0	9.831813E-6		null"+MZTabConstants.NEW_LINE;
        assertEqSentry(reference, serializedString);
    }

}

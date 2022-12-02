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
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab2.model.IOptColumnMappingBuilder;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
import uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilder;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

/**
 * @author nilshoffmann
 */
public class SmallMoleculeSummarySerializerTest extends AbstractSerializerTest {

    public SmallMoleculeSummarySerializerTest() {
    }

    @Test
    public void testSerialize() throws IOException, MZTabException {
        MzTab mzTabFile = create2_0TestFile();
        Assay a1 = mzTabFile.getMetadata().getAssay().get(0);
        Assay a2 = mzTabFile.getMetadata().getAssay().get(1);
        IOptColumnMappingBuilder assay1OptPeakMz = OptColumnMappingBuilder.forIndexedElement(a1).withName("peak_mz");
        IOptColumnMappingBuilder assay2OptPeakMz = OptColumnMappingBuilder.forIndexedElement(a2).withName("peak_mz");
        //just for testing
        Parameter cvp = new Parameter().cvLabel("MS").cvAccession("MS:121317").name("just for testing");
        IOptColumnMappingBuilder assay1CvTerm = OptColumnMappingBuilder.forIndexedElement(a1).withParameter(cvp);
        IOptColumnMappingBuilder assay2CvTerm = OptColumnMappingBuilder.forIndexedElement(a2).withParameter(cvp);
        SmallMoleculeSummary smsi = new SmallMoleculeSummary();
        smsi.smlId(1).
            smfIdRefs(Arrays.asList(1, 2, null, 4, 5)). // null serialization is possible
            chemicalName(Arrays.asList("Cer(d18:1/24:0)",
                "N-(tetracosanoyl)-sphing-4-enine", "C24 Cer")).
            addOptItem(OptColumnMappingBuilder.forGlobal().withParameter(
                new Parameter().cvLabel("LM").
                    cvAccession("LM:SP").
                    name("Category")).
                    build("Sphingolipids")).
            addOptItem(OptColumnMappingBuilder.forGlobal().withParameter(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Species")).
                    build("Cer 42:1")).
            addOptItem(OptColumnMappingBuilder.forGlobal().withParameter(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Sub Species")).
                    build("Cer d18:1/24:0")).
            addDatabaseIdentifierItem("LM:LMSP02010012").
            addChemicalFormulaItem("C42H83NO3").
            addSmilesItem(
                "CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC").
            addInchiItem(
                "InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1").
            addUriItem(
                "http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012").
            addTheoreticalNeutralMassItem(649.6373).
            addAdductIonsItem("[M+H]1+").
            reliability("1").
            bestIdConfidenceMeasure(new Parameter().name(
                "qualifier ions exact mass")).
            bestIdConfidenceValue(0.958).
            addAbundanceAssayItem(4.448784E-05).
            addAbundanceAssayItem(5.448784E-05).
            addAbundanceStudyVariableItem(4.448784E-05).
            addAbundanceStudyVariableItem(5.448784E-05).
            addAbundanceVariationStudyVariableItem(0.0d).
            addAbundanceVariationStudyVariableItem(0.00001d).
            addOptItem(assay1OptPeakMz.build("782.1240")).
            addOptItem(assay2OptPeakMz.build("782.1239")).
            addOptItem(assay1CvTerm.build("1")).
            addOptItem(assay2CvTerm.build("2"));
        mzTabFile.addSmallMoleculeSummaryItem(smsi);
        ObjectWriter writer = smallMoleculeSummaryWriter(mzTabFile);

        String serializedString = serializeSequence(writer, mzTabFile.
            getSmallMoleculeSummary());
        Assert.assertFalse(serializedString.isEmpty());
        System.out.println(serializedString);
        //check for exactly one header line + 1 entry lines
        Assert.assertEquals(2,
            serializedString.split(MZTabConstants.NEW_LINE).length);
        String expected = 
            "SMH	SML_ID	SMF_ID_REFS	database_identifier	chemical_formula	smiles	inchi	chemical_name	uri	theoretical_neutral_mass	adduct_ions	reliability	best_id_confidence_measure	best_id_confidence_value	abundance_assay[1]	abundance_assay[2]	abundance_study_variable[1]	abundance_study_variable[2]	abundance_variation_study_variable[1]	abundance_variation_study_variable[2]	opt_global_cv_LM:SP_Category	opt_global_cv_LH:XXXXX_Species	opt_global_cv_LH:XXXXX_Sub_Species	opt_assay[1]_peak_mz	opt_assay[2]_peak_mz	opt_assay[1]_cv_MS:121317_just_for_testing	opt_assay[2]_cv_MS:121317_just_for_testing" + MZTabConstants.NEW_LINE
           +"SML	1	1|2|null|4|5	LM:LMSP02010012	C42H83NO3	CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC	InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1	Cer(d18:1/24:0)|N-(tetracosanoyl)-sphing-4-enine|C24 Cer	http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012	649.6373	[M+H]1+	1	[, , qualifier ions exact mass, ]	0.958	4.448784E-5	5.448784E-5	4.448784E-5	5.448784E-5	0.0	1.0E-5	Sphingolipids	Cer 42:1	Cer d18:1/24:0	782.1240	782.1239	1	2" + MZTabConstants.NEW_LINE;
        assertEqSentry(expected, serializedString);
    }

}

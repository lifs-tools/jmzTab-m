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

import de.isas.mztab2.io.AbstractSerializerTest;
import com.fasterxml.jackson.databind.ObjectWriter;
import static de.isas.mztab2.model.Metadata.PrefixEnum.MTD;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.OptColumnMapping;
import de.isas.mztab2.model.Parameter;
import de.isas.mztab2.model.SmallMoleculeSummary;
import static de.isas.mztab2.model.SmallMoleculeSummary.HeaderPrefixEnum.SMH;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;
import uk.ac.ebi.pride.jmztab2.model.MetadataElement;
import static de.isas.mztab2.io.MzTabTestData.create2_0TestFile;
import org.junit.Assert;
import org.junit.Ignore;
import uk.ac.ebi.pride.jmztab2.model.MZTabConstants;
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
        SmallMoleculeSummary smsi = new SmallMoleculeSummary();
        smsi.smlId(1).
            smfIdRefs(Arrays.asList(1, 2, null, 4, 5)). // null serialization is possible
            chemicalName(Arrays.asList("Cer(d18:1/24:0)",
                "N-(tetracosanoyl)-sphing-4-enine", "C24 Cer")).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_category").
                param(new Parameter().cvLabel("LM").
                    cvAccession("LM:SP").
                    name("Category").
                    value("Sphingolipids"))).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_species").
                param(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Species").
                    value("Cer 42:1"))).
            addOptItem(new OptColumnMapping().identifier("global").
                value("lipid_best_id_level").
                param(new Parameter().cvLabel("LH").
                    cvAccession("LH:XXXXX").
                    name("Sub Species").
                    value("Cer d18:1/24:0"))).
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
            addAbundanceVariationStudyVariableItem(0.00001d);
        mzTabFile.addSmallMoleculeSummaryItem(smsi);
        ObjectWriter writer = smallMoleculeSummaryWriter(mzTabFile);

        String serializedString = serializeSequence(writer, mzTabFile.
            getSmallMoleculeSummary());
        Assert.assertFalse(serializedString.isEmpty());
        System.out.println(serializedString);
        //check for exactly one header line + 1 entry lines
        Assert.assertEquals(2,
            serializedString.split(MZTabConstants.NEW_LINE).length);
        String expected = "SMH	SML_ID	SMF_ID_REFS	database_identifier	chemical_formula	smiles	inchi	chemical_name	uri	theoretical_neutral_mass	adduct_ions	reliability	best_id_confidence_measure	best_id_confidence_value	abundance_assay[1]	abundance_assay[2]	abundance_study_variable[1]	abundance_study_variable[2]	abundance_variation_study_variable[1]	abundance_variation_study_variable[2]	opt_global_cv_LM:SP_Category	opt_global_cv_LH:XXXXX_Species	opt_global_cv_LH:XXXXX_Sub_Species" + MZTabConstants.NEW_LINE
            + "SML	1	1|2|null|4|5	LM:LMSP02010012	C42H83NO3	CCCCCCCCCCCCCCCCCCCCCCCC(=O)N[C@@H](CO)[C@H](O)/C=C/CCCCCCCCCCCCC	InChI=1S/C42H83NO3/c1-3-5-7-9-11-13-15-17-18-19-20-21-22-23-24-26-28-30-32-34-36-38-42(46)43-40(39-44)41(45)37-35-33-31-29-27-25-16-14-12-10-8-6-4-2/h35,37,40-41,44-45H,3-34,36,38-39H2,1-2H3,(H,43,46)/b37-35+/t40-,41+/m0/s1	Cer(d18:1/24:0)|N-(tetracosanoyl)-sphing-4-enine|C24 Cer	http://www.lipidmaps.org/data/LMSDRecord.php?LM_ID=LMSP02010012	649.6373	[M+H]1+	1	[, , qualifier ions exact mass, ]	0.958	4.448784E-5	5.448784E-5	4.448784E-5	5.448784E-5	0.0	1.0E-5	[LM, LM:SP, Category, Sphingolipids]	[LH, LH:XXXXX, Species, Cer 42:1]	[LH, LH:XXXXX, Sub Species, Cer d18:1/24:0]" + MZTabConstants.NEW_LINE;
        assertEqSentry(expected, serializedString);
    }

}

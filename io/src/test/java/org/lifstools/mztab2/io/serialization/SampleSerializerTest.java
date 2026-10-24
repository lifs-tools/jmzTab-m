/* 
 * Copyright 2018 LIFS Tools Consortium.
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
import org.lifstools.mztab2.io.TestResources;
import org.lifstools.mztab2.io.serialization.ParameterConverter;
import org.lifstools.mztab2.model.Metadata;
import static org.lifstools.mztab2.model.Metadata.PrefixEnum.MTD;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.Sample;
import org.junit.jupiter.api.Test;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.TAB_STRING;

/**
 * @author nilshoffmann
 */
public class SampleSerializerTest extends AbstractSerializerTest {

    /**
     * Test of serializeSingle method, of class SampleSerializer.
     */
    @Test
    public void testSerialize() throws Exception {
        Metadata mtd = new Metadata();
        Sample sample1 = new Sample().id(1).
            name("Sample 1").
            description("Hepatocellular carcinoma samples.").
            addSpeciesItem(new Parameter().cvLabel("NEWT").
                cvAccession("9606").
                name(
                    "Homo sapiens (Human)")).
            addTissueItem(new Parameter().cvLabel("BTO").
                cvAccession("BTO:0000759").
                name("liver")).
            addCellTypeItem(new Parameter().cvLabel("CL").
                cvAccession("CL:0000182").
                name("hepatocyte")).
            addDiseaseItem(new Parameter().cvLabel("DOID").
                cvAccession("DOID:684").
                name("hepatocellular carcinoma")).
            addDiseaseItem(new Parameter().cvLabel("DOID").
                cvAccession("DOID:9451").
                name("alcoholic fatty liver")).
            addCustomItem(new Parameter().name("Extraction date").
                value("2011-12-21")).
            addCustomItem(new Parameter().name("Extraction reason").
                value("liver biopsy"));
        mtd.addSampleItem(sample1);
        Sample sample2 = new Sample().id(2).
            name("Sample 2").
            description("Healthy control samples.").
            addSpeciesItem(new Parameter().cvLabel("NEWT").
                cvAccession("9606").
                name(
                    "Homo sapiens (Human)")).
            addSpeciesItem(new Parameter().cvLabel("NEWT").
                cvAccession("12130").
                name("Human rhinovirus 2")).
            addTissueItem(new Parameter().cvLabel("BTO").
                cvAccession("BTO:0000759").
                name("liver")).
            addCellTypeItem(new Parameter().cvLabel("CL").
                cvAccession("CL:0000182").
                name("hepatocyte")).
            addCustomItem(new Parameter().name("Extraction date").
                value("2011-12-19")).
            addCustomItem(new Parameter().name("Extraction reason").
                value("liver biopsy"));
        mtd.addSampleItem(sample2);

        ObjectWriter writer = metaDataWriter();
        assertEqSentry(TestResources.MZTAB_VERSION_HEADER
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]" + TAB_STRING + sample1.
                getName()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_DESCRIPTION + TAB_STRING + sample1.
                getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_SPECIES + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample1.getSpecies().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_CELL_TYPE + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample1.getCellType().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_DISEASE + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample1.getDisease().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_DISEASE + "[2]" + TAB_STRING + new ParameterConverter().
                convert(sample1.getDisease().
                    get(1))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_TISSUE + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample1.getTissue().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_CUSTOM + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample1.getCustom().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[1]-" + Sample.JSON_PROPERTY_CUSTOM + "[2]" + TAB_STRING + new ParameterConverter().
                convert(sample1.getCustom().
                    get(1))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]" + TAB_STRING + sample2.getName()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]-" + Sample.JSON_PROPERTY_DESCRIPTION + TAB_STRING + sample2.
                getDescription()
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]-" + Sample.JSON_PROPERTY_SPECIES + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample2.getSpecies().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]-" + Sample.JSON_PROPERTY_SPECIES + "[2]" + TAB_STRING + new ParameterConverter().
                convert(sample2.getSpecies().
                    get(1))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]-" + Sample.JSON_PROPERTY_CELL_TYPE + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample2.getCellType().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]-" + Sample.JSON_PROPERTY_TISSUE + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample2.getTissue().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]-" + Sample.JSON_PROPERTY_CUSTOM + "[1]" + TAB_STRING + new ParameterConverter().
                convert(sample2.getCustom().
                    get(0))
            + NEW_LINE
            + MTD + TAB_STRING + Metadata.JSON_PROPERTY_SAMPLE + "[2]-" + Sample.JSON_PROPERTY_CUSTOM + "[2]" + TAB_STRING + new ParameterConverter().
                convert(sample2.getCustom().
                    get(1))
            + NEW_LINE,
             serializeSingle(writer, mtd));
    }

}

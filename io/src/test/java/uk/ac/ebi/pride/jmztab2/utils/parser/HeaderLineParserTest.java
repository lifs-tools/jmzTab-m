/*
 * Copyright 2026 Leibniz-Institut für Analytische Wissenschaften – ISAS – e.V..
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
package uk.ac.ebi.pride.jmztab2.utils.parser;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.Parameter;
import uk.ac.ebi.pride.jmztab2.model.IMZTabColumn;
import uk.ac.ebi.pride.jmztab2.model.MZTabColumnFactory;
import uk.ac.ebi.pride.jmztab2.model.OptionColumn;
import uk.ac.ebi.pride.jmztab2.model.Section;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorList;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabException;

public class HeaderLineParserTest {

    private static final List<String> SMH_STABLE = List.of("SML_ID", "SMF_ID_REFS",
        "database_identifier", "chemical_formula", "smiles", "inchi", "chemical_name",
        "uri", "theoretical_neutral_mass", "adduct_ions", "reliability",
        "best_id_confidence_measure", "best_id_confidence_value");

    private static final List<String> SEH_STABLE = List.of("SME_ID", "evidence_input_id",
        "database_identifier", "chemical_formula", "smiles", "inchi", "chemical_name",
        "uri", "derivatized_form", "adduct_ion", "exp_mass_to_charge", "charge",
        "theoretical_mass_to_charge", "spectra_ref", "identification_method", "ms_level");

    private static final List<String> SFH_STABLE = List.of("SMF_ID", "SME_ID_REFS",
        "SME_ID_REF_ambiguity_code", "adduct_ion", "isotopomer", "exp_mass_to_charge",
        "charge", "retention_time_in_seconds", "retention_time_in_seconds_start",
        "retention_time_in_seconds_end");

    private static String header(String prefix, List<String> stable, String... extra) {
        List<String> cells = new ArrayList<>();
        cells.add(prefix);
        cells.addAll(stable);
        cells.addAll(List.of(extra));
        return String.join("\t", cells);
    }

    private static MZTabException parseSmh(MZTabParserContext context, Metadata metadata, String line) {
        SMHLineParser parser = new SMHLineParser(context, metadata);
        return assertThrows(MZTabException.class, () -> parser.parse(1, line, new MZTabErrorList()));
    }

    @Test
    public void duplicateOptHeaderIsReported() {
        MZTabException e = parseSmh(new MZTabParserContext(), new Metadata(),
            header("SMH", SMH_STABLE, "opt_global_x", "opt_global_x"));
        assertEquals(LogicalErrorType.DuplicateColumnHeader, e.getError().getType());
        assertTrue(e.getError().getMessage().contains("opt_global_x"), e.getError().getMessage());
    }

    @Test
    public void duplicateHeaderDifferingOnlyInCaseIsReported() {
        MZTabException e = parseSmh(new MZTabParserContext(), new Metadata(),
            header("SMH", SMH_STABLE, "opt_global_X", "opt_global_x"));
        assertEquals(LogicalErrorType.DuplicateColumnHeader, e.getError().getType());
    }

    @Test
    public void duplicateStableHeaderIsReported() {
        MZTabException e = parseSmh(new MZTabParserContext(), new Metadata(),
            header("SMH", SMH_STABLE, "SML_ID"));
        assertEquals(LogicalErrorType.DuplicateColumnHeader, e.getError().getType());
    }

    @Test
    public void undefinedAssayIsStillReported() {
        MZTabParserContext context = new MZTabParserContext();
        Metadata metadata = new Metadata();
        context.addAssay(metadata, new Assay().id(1).name("assay 1"));
        MZTabException e = parseSmh(context, metadata,
            header("SMH", SMH_STABLE, "abundance_assay[1]", "abundance_assay[5]"));
        assertEquals(LogicalErrorType.AssayNotDefined, e.getError().getType());
    }

    @Test
    public void idConfidenceMeasureBeyondMetadataIsReported() {
        Metadata metadata = new Metadata();
        metadata.addIdConfidenceMeasureItem(new Parameter().name("only measure"));
        SEHLineParser parser = new SEHLineParser(new MZTabParserContext(), metadata);
        MZTabException e = assertThrows(MZTabException.class, () -> parser.parse(1,
            header("SEH", SEH_STABLE, "id_confidence_measure[1]", "id_confidence_measure[2]", "rank"),
            new MZTabErrorList()));
        assertEquals(LogicalErrorType.NotDefineInMetadata, e.getError().getType());
        assertTrue(e.getError().getMessage().contains("id_confidence_measure[2]"), e.getError().getMessage());
    }

    @Test
    public void factoryClashIsWrappedAsHeaderNotValid() {
        MZTabHeaderLineParser parser = new MZTabHeaderLineParser(new MZTabParserContext(),
            MZTabColumnFactory.getInstance(Section.Small_Molecule_Header), new Metadata()) {
            @Override
            protected int parseColumns() {
                throw new IllegalArgumentException("Position 5.0.0 for column 'opt_global_b' is already assigned to 'opt_global_a'");
            }

            @Override
            protected void refine() {
            }
        };
        MZTabException e = assertThrows(MZTabException.class, () ->
            parser.parse(1, header("SMH", SMH_STABLE), new MZTabErrorList()));
        assertEquals(LogicalErrorType.HeaderNotValid, e.getError().getType());
        assertTrue(e.getError().getMessage().contains("opt_global_b"), e.getError().getMessage());
    }

    @Test
    public void optColumnHeaderContainingAbundanceIsParsedAsOptionColumn() throws MZTabException {
        Metadata metadata = new Metadata();
        metadata.setSmallMoleculeFeatureQuantificationUnit(new Parameter().name("qty"));
        SFHLineParser parser = new SFHLineParser(new MZTabParserContext(), metadata);

        parser.parse(1, header("SFH", SFH_STABLE, "opt_global_abundance_raw"), new MZTabErrorList());

        IMZTabColumn column = parser.getFactory().findColumnByHeader("opt_global_abundance_raw");
        assertNotNull(column, "opt_global_abundance_raw should have been registered as a column");
        assertInstanceOf(OptionColumn.class, column,
            () -> "Expected opt_global_abundance_raw to be parsed as an OptionColumn, but was " + column.getClass());
    }
}

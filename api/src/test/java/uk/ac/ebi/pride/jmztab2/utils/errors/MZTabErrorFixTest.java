package uk.ac.ebi.pride.jmztab2.utils.errors;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationMessage;
import static org.junit.jupiter.api.Assertions.*;

public class MZTabErrorFixTest {

    @Test
    void fillFixUsesIndicesAndNamedExtras() {
        assertEquals("Keep only one of the terms b on el. Allowed: al.",
            MZTabError.fillFix("Keep only one of the terms {1} on {element}. Allowed: {allowed}.", List.of("a", "b"), "el", "al"));
        assertEquals("Change the name of p from \"a\" to \"b\".",
            MZTabError.fillFix("Change the {0} of {1} from \"{2}\" to \"{3}\".", List.of("name", "p", "a", "b"), null, null));
    }

    @Test
    void fillFixOutOfRangeAndMissingExtras() {
        assertEquals("x  y", MZTabError.fillFix("x {1} y", List.of("a"), null, null));
        assertEquals("Add a term to the element. Allowed: the terms listed in the validation profile.",
            MZTabError.fillFix("Add a term to {element}. Allowed: {allowed}.", List.of(), null, null));
    }

    @Test
    void toValidationMessageCarriesSuggestionRuleAndLocation() {
        ValidationLocation where = Locations.metadataKey("instrument[1]-name");
        ValidationMessage vm = new MZTabError(CrossCheckErrorType.CvTermNotAllowed, -1,
                "[MS, MS:1000040, m/z, ]", "/metadata/instrument/@name", "rule text")
            .ruleId("instrument_name_may").location(where)
            .fixContext("instrument[1]-name", "MS:1000031 (instrument model) or one of its child terms")
            .toValidationMessage();
        assertEquals("instrument_name_may", vm.getRuleId());
        assertSame(where, vm.getLocation());
        assertEquals("Replace the term [MS, MS:1000040, m/z, ] on instrument[1]-name with a term the validation profile allows: MS:1000031 (instrument model) or one of its child terms.",
            vm.getSuggestion());
        assertTrue(vm.getMessage().startsWith("The cv term \"[MS, MS:1000040, m/z, ]\" is not allowed"));
    }

    @Test
    void unknownRefIdFixNamesRowAndTargetSection() {
        // arguments as passed by MzTabFileParser: ref id, column, row id, from and to section
        String s = new MZTabError(LogicalErrorType.UnknownRefId, -1, "3",
            "smf_id_refs", "5", "smallMoleculeSummary", "smallMoleculeFeature").getSuggestion();
        assertEquals("Change the value \"3\" in column \"smf_id_refs\" of the smallMoleculeSummary row with id 5"
            + " so that it refers to an existing smallMoleculeFeature entry, or add the missing entry.", s);
    }

    @Test
    void countMatchFixGivesNoNumbers() {
        String s = new MZTabError(FormatErrorType.CountMatch, 7, "12", "10").getSuggestion();
        assertEquals("Make the row have exactly as many tab-separated values as the header line has columns.", s);
    }

    @Test
    void errorWithoutExtrasStillHasNoPlaceholders() {
        String s = new MZTabError(CrossCheckErrorType.CvUnused, -1, "UO").getSuggestion();
        assertFalse(s.contains("{"));
    }
}

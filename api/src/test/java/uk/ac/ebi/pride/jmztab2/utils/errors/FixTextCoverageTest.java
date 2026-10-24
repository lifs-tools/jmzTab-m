package uk.ac.ebi.pride.jmztab2.utils.errors;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every format, logical and cross-check message type must carry a suggested
 * fix that is plain ASCII and only uses placeholders its original provides.
 */
class FixTextCoverageTest {

    static Stream<MZTabErrorType> allTypes() {
        return Stream.of(FormatErrorType.getValues(), LogicalErrorType.getValues(),
            CrossCheckErrorType.getValues()).flatMap(Arrays::stream);
    }

    @Test
    void allTypesAreCovered() {
        assertEquals(114, allTypes().count());
    }

    @ParameterizedTest
    @MethodSource("allTypes")
    void everyTypeHasAnAsciiFixThatOnlyUsesKnownPlaceholders(MZTabErrorType t) {
        String fix = t.getFix();
        assertNotNull(fix, "no _fix_ for code " + t.getCode());
        assertFalse(fix.isBlank());
        assertTrue(StandardCharsets.US_ASCII.newEncoder().canEncode(fix),
            "non-ASCII fix for " + t.getCode());
        long params = Pattern.compile("\\{\\w\\}").matcher(t.getOriginal()).results().count();
        Pattern.compile("\\{([^}]*)\\}").matcher(fix).results().map(r -> r.group(1)).forEach(p -> assertTrue(
            p.equals("element") || p.equals("allowed") || (p.matches("\\d") && Integer.parseInt(p) < params),
            "placeholder {" + p + "} not available for code " + t.getCode()));
    }
}

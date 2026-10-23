package org.lifstools.mztab2.model;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;
import org.lifstools.mztab2.model.ValidationMessage.CategoryEnum;
import org.lifstools.mztab2.model.ValidationMessage.MessageTypeEnum;

public class ValidationMessageJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void messageWithoutNewFieldsSerialisesAsBefore() throws Exception {
        ValidationMessage m = new ValidationMessage().code("[Error-2009] line 5: x").category(CategoryEnum.LOGICAL)
                .messageType(MessageTypeEnum.ERROR).message("x").lineNumber(5L);
        String json = mapper.writeValueAsString(m);
        assertFalse(json.contains("suggestion"));
        assertFalse(json.contains("rule_id"));
        assertFalse(json.contains("location"));
    }

    @Test
    void oldJsonWithoutNewFieldsDeserialises() throws Exception {
        ValidationMessage m = mapper.readValue("{\"code\":\"c\",\"category\":\"format\",\"message_type\":\"warn\",\"message\":\"m\",\"line_number\":3}", ValidationMessage.class);
        assertNull(m.getSuggestion());
        assertNull(m.getRuleId());
        assertNull(m.getLocation());
    }

    @Test
    void newFieldsRoundTripInSnakeCase() throws Exception {
        ValidationMessage m = new ValidationMessage().code("c").category(CategoryEnum.CROSS_CHECK).message("m")
                .suggestion("Add a term.").ruleId("sample_species_may")
                .location(new ValidationLocation().section(SectionEnum.SML).rowIndex(1).column("best_id_confidence_measure")
                        .elementPath("/smallMoleculeSummary[2]/@bestIdConfidenceMeasure"));
        String json = mapper.writeValueAsString(m);
        assertTrue(json.contains("\"rule_id\":\"sample_species_may\""));
        assertTrue(json.contains("\"section\":\"sml\""));
        assertTrue(json.contains("\"row_index\":1"));
        assertTrue(json.contains("\"element_path\":"));
        assertFalse(json.contains("metadata_key"));
        assertEquals(m, mapper.readValue(json, ValidationMessage.class));
    }
}

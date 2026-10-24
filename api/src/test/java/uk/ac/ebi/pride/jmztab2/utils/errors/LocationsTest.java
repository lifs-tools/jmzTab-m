package uk.ac.ebi.pride.jmztab2.utils.errors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;

public class LocationsTest {

    @Test
    void groupOfKey() {
        assertEquals("sample", Locations.groupOf("sample[3]-species[1]"));
        assertEquals("ms_run", Locations.groupOf("ms_run[1]-format"));
        assertEquals("general", Locations.groupOf("small_molecule-quantification_unit"));
        assertEquals("general", Locations.groupOf("mzTab-version"));
    }

    @Test
    void metadataKeyLocation() {
        ValidationLocation l = Locations.metadataKey("instrument[1]-name");
        assertEquals(SectionEnum.MTD, l.getSection());
        assertEquals("instrument[1]-name", l.getMetadataKey());
        assertEquals("instrument", l.getMetadataGroup());
        assertNull(l.getRowIndex());
    }

    @Test
    void metadataGroupLocation() {
        ValidationLocation l = Locations.metadataGroup("sample");
        assertEquals(SectionEnum.MTD, l.getSection());
        assertEquals("sample", l.getMetadataGroup());
        assertNull(l.getMetadataKey());
    }

    @Test
    void tableCellAndHeaderColumn() {
        ValidationLocation c = Locations.tableCell(SectionEnum.SML, 2, "chemical_name");
        assertEquals(SectionEnum.SML, c.getSection());
        assertEquals(2, c.getRowIndex());
        assertEquals("chemical_name", c.getColumn());
        assertNull(Locations.tableCell(SectionEnum.SMF, 0, null).getColumn());
        ValidationLocation h = Locations.headerColumn(SectionEnum.SMH, "x");
        assertEquals(SectionEnum.SMH, h.getSection());
        assertEquals("x", h.getColumn());
        assertNull(h.getRowIndex());
        assertEquals(SectionEnum.COM, Locations.section(SectionEnum.COM).getSection());
    }

    @Test
    void sectionOfPrefix() {
        assertEquals(SectionEnum.SMF, Locations.sectionOf("SMF"));
        assertEquals(SectionEnum.MTD, Locations.sectionOf("mtd"));
        assertNull(Locations.sectionOf("XYZ"));
        assertNull(Locations.sectionOf(null));
    }
}

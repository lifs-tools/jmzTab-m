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
package uk.ac.ebi.pride.jmztab2.model;

import org.lifstools.mztab2.test.utils.LogMethodName;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import static uk.ac.ebi.pride.jmztab2.model.Section.Comment;
import static uk.ac.ebi.pride.jmztab2.model.Section.Metadata;
import static uk.ac.ebi.pride.jmztab2.model.Section.PSM_Header;
import static uk.ac.ebi.pride.jmztab2.model.Section.Peptide_Header;
import static uk.ac.ebi.pride.jmztab2.model.Section.Protein_Header;
import static uk.ac.ebi.pride.jmztab2.model.Section.Small_Molecule_Evidence_Header;
import static uk.ac.ebi.pride.jmztab2.model.Section.Small_Molecule_Feature_Header;
import static uk.ac.ebi.pride.jmztab2.model.Section.Small_Molecule_Header;

/**
 *
 * @author nilshoffmann
 */
public class SectionTest {

    @RegisterExtension
    public LogMethodName methodNameLogger = new LogMethodName();

    /**
     * Test of values method, of class Section.
     */
    @Test
    public void testValues() {
        assertEquals(14, Section.values().length);
    }

    /**
     * Test of getPrefix method, of class Section.
     */
    @Test
    public void testGetPrefix() {
        assertEquals("COM", Section.Comment.getPrefix());
        assertEquals("MTD", Section.Metadata.getPrefix());

        assertEquals("PRH", Section.Protein_Header.getPrefix());
        assertEquals("PRT", Section.Protein.getPrefix());

        assertEquals("PEH", Section.Peptide_Header.getPrefix());
        assertEquals("PEP", Section.Peptide.getPrefix());

        assertEquals("PSH", Section.PSM_Header.getPrefix());
        assertEquals("PSM", Section.PSM.getPrefix());

        assertEquals("SMH", Section.Small_Molecule_Header.getPrefix());
        assertEquals("SML", Section.Small_Molecule.getPrefix());

        assertEquals("SFH", Section.Small_Molecule_Feature_Header.getPrefix());
        assertEquals("SMF", Section.Small_Molecule_Feature.getPrefix());

        assertEquals("SEH", Section.Small_Molecule_Evidence_Header.getPrefix());
        assertEquals("SME", Section.Small_Molecule_Evidence.getPrefix());
    }

    /**
     * Test of getLevel method, of class Section.
     */
    @Test
    public void testGetLevel() {
        assertEquals(0, Section.Comment.getLevel());
        assertEquals(1, Section.Metadata.getLevel());

        assertEquals(2, Section.Protein_Header.getLevel());
        assertEquals(3, Section.Protein.getLevel());

        assertEquals(4, Section.Peptide_Header.getLevel());
        assertEquals(5, Section.Peptide.getLevel());

        assertEquals(6, Section.PSM_Header.getLevel());
        assertEquals(7, Section.PSM.getLevel());

        assertEquals(8, Section.Small_Molecule_Header.getLevel());
        assertEquals(9, Section.Small_Molecule.getLevel());

        assertEquals(10, Section.Small_Molecule_Feature_Header.getLevel());
        assertEquals(11, Section.Small_Molecule_Feature.getLevel());

        assertEquals(12, Section.Small_Molecule_Evidence_Header.getLevel());
        assertEquals(13, Section.Small_Molecule_Evidence.getLevel());
    }

    /**
     * Test of findSection method, of class Section.
     */
    @Test
    public void testFindSection_int() {
        for (Section s : Section.values()) {
            Assertions.assertEquals(s, Section.findSection(s.getLevel()));
        }
    }

    /**
     * Test of isComment method, of class Section.
     */
    @Test
    public void testIsComment() {
        for (Section s : Section.values()) {
            switch (s) {
                case Comment:
                    Assertions.assertTrue(s.isComment());
                    break;
                default:
                    Assertions.assertFalse(s.isComment());
            }
        }
    }

    /**
     * Test of isMetadata method, of class Section.
     */
    @Test
    public void testIsMetadata() {
        for (Section s : Section.values()) {
            switch (s) {
                case Metadata:
                    Assertions.assertTrue(s.isMetadata());
                    break;
                default:
                    Assertions.assertFalse(s.isMetadata());
            }
        }
    }

    /**
     * Test of isHeader method, of class Section.
     */
    @Test
    public void testIsHeader() {
        for (Section s : Section.values()) {
            switch (s) {
                case PSM_Header:
                case Peptide_Header:
                case Protein_Header:
                case Small_Molecule_Evidence_Header:
                case Small_Molecule_Feature_Header:
                case Small_Molecule_Header:
                    Assertions.assertTrue(s.isHeader());
                    break;
                default:
                    Assertions.assertFalse(s.isHeader());
            }
        }
    }

    /**
     * Test of isData method, of class Section.
     */
    @Test
    public void testIsData() {
        for (Section s : Section.values()) {
            switch (s) {
                case PSM_Header:
                case Peptide_Header:
                case Protein_Header:
                case Small_Molecule_Evidence_Header:
                case Small_Molecule_Feature_Header:
                case Small_Molecule_Header:
                case Comment:
                case Metadata:
                    Assertions.assertFalse(s.isData());
                    break;
                default:
                    Assertions.assertTrue(s.isData());
            }
        }
    }

    /**
     * Test of toHeaderSection method, of class Section.
     */
    @Test
    public void testToHeaderSection() {
        Assertions.assertEquals(Section.Peptide_Header, Section.toHeaderSection(
            Section.Peptide));
        Assertions.assertEquals(Section.Peptide_Header, Section.toHeaderSection(
            Section.Peptide_Header));

        Assertions.assertEquals(Section.Protein_Header, Section.toHeaderSection(
            Section.Protein));
        Assertions.assertEquals(Section.Protein_Header, Section.toHeaderSection(
            Section.Protein_Header));

        Assertions.assertEquals(Section.PSM_Header, Section.toHeaderSection(
            Section.PSM));
        Assertions.assertEquals(Section.PSM_Header, Section.toHeaderSection(
            Section.PSM_Header));

        Assertions.assertEquals(Section.Small_Molecule_Header, Section.
            toHeaderSection(
                Section.Small_Molecule));
        Assertions.assertEquals(Section.Small_Molecule_Header, Section.
            toHeaderSection(
                Section.Small_Molecule_Header));

        Assertions.assertEquals(Section.Small_Molecule_Evidence_Header, Section.
            toHeaderSection(
                Section.Small_Molecule_Evidence));
        Assertions.assertEquals(Section.Small_Molecule_Evidence_Header, Section.
            toHeaderSection(
                Section.Small_Molecule_Evidence_Header));

        Assertions.assertEquals(Section.Small_Molecule_Feature_Header, Section.
            toHeaderSection(
                Section.Small_Molecule_Feature));
        Assertions.assertEquals(Section.Small_Molecule_Feature_Header, Section.
            toHeaderSection(
                Section.Small_Molecule_Feature_Header));

        Assertions.assertNull(Section.toHeaderSection(Section.Comment));
    }

    /**
     * Test of toDataSection method, of class Section.
     */
    @Test
    public void testToDataSection() {
        Assertions.assertEquals(Section.Peptide, Section.toDataSection(
            Section.Peptide));
        Assertions.assertEquals(Section.Peptide, Section.toDataSection(
            Section.Peptide_Header));

        Assertions.assertEquals(Section.Protein, Section.toDataSection(
            Section.Protein));
        Assertions.assertEquals(Section.Protein, Section.toDataSection(
            Section.Protein_Header));

        Assertions.assertEquals(Section.PSM, Section.toDataSection(
            Section.PSM));
        Assertions.assertEquals(Section.PSM, Section.toDataSection(
            Section.PSM_Header));

        Assertions.assertEquals(Section.Small_Molecule, Section.toDataSection(
            Section.Small_Molecule));
        Assertions.assertEquals(Section.Small_Molecule, Section.toDataSection(
            Section.Small_Molecule_Header));

        Assertions.assertEquals(Section.Small_Molecule_Evidence, Section.
            toDataSection(
                Section.Small_Molecule_Evidence));
        Assertions.assertEquals(Section.Small_Molecule_Evidence, Section.
            toDataSection(
                Section.Small_Molecule_Evidence_Header));

        Assertions.assertEquals(Section.Small_Molecule_Feature, Section.
            toDataSection(
                Section.Small_Molecule_Feature));
        Assertions.assertEquals(Section.Small_Molecule_Feature, Section.
            toDataSection(
                Section.Small_Molecule_Feature_Header));

        Assertions.assertNull(Section.toDataSection(Section.Comment));
    }

    /**
     * Test of findSection method, of class Section.
     */
    @Test
    public void testFindSection_String() {
        Assertions.assertNull(Section.findSection(null));

        Assertions.assertEquals(Section.Comment, Section.findSection("comment"));
        Assertions.assertEquals(Section.Comment, Section.findSection("COM"));

        Assertions.assertEquals(Section.Metadata, Section.findSection("metadata"));
        Assertions.assertEquals(Section.Metadata, Section.findSection("MTD"));

        Assertions.assertEquals(Section.PSM, Section.findSection("psm"));
        Assertions.assertEquals(Section.PSM, Section.findSection("PSM"));
        Assertions.assertEquals(Section.PSM_Header, Section.
            findSection("psm_header"));
        Assertions.assertEquals(Section.PSM_Header, Section.findSection("PSH"));

        Assertions.assertEquals(Section.Peptide, Section.findSection("peptide"));
        Assertions.assertEquals(Section.Peptide, Section.findSection("PEP"));
        Assertions.assertEquals(Section.Peptide_Header, Section.findSection(
            "peptide_header"));
        Assertions.assertEquals(Section.Peptide_Header, Section.findSection("PEH"));

        Assertions.assertEquals(Section.Protein, Section.findSection("protein"));
        Assertions.assertEquals(Section.Protein, Section.findSection("PRT"));
        Assertions.assertEquals(Section.Protein_Header, Section.findSection(
            "protein_header"));
        Assertions.assertEquals(Section.Protein_Header, Section.findSection("PRH"));

        Assertions.assertEquals(Section.Small_Molecule, Section.findSection(
            "small_molecule"));
        Assertions.assertEquals(Section.Small_Molecule, Section.findSection("SML"));
        Assertions.assertEquals(Section.Small_Molecule_Header, Section.findSection(
            "small_molecule_header"));
        Assertions.assertEquals(Section.Small_Molecule_Header, Section.findSection(
            "SMH"));

        Assertions.assertEquals(Section.Small_Molecule_Feature, Section.findSection(
            "small_molecule_feature"));
        Assertions.assertEquals(Section.Small_Molecule_Feature, Section.findSection(
            "SMF"));
        Assertions.assertEquals(Section.Small_Molecule_Feature_Header, Section.
            findSection("small_molecule_feature_header"));
        Assertions.assertEquals(Section.Small_Molecule_Feature_Header, Section.
            findSection("SFH"));

        Assertions.assertEquals(Section.Small_Molecule_Evidence, Section.
            findSection("small_molecule_evidence"));
        Assertions.assertEquals(Section.Small_Molecule_Evidence, Section.
            findSection("SME"));
        Assertions.assertEquals(Section.Small_Molecule_Evidence_Header, Section.
            findSection("small_molecule_evidence_header"));
        Assertions.assertEquals(Section.Small_Molecule_Evidence_Header, Section.
            findSection("SEH"));
    }

}

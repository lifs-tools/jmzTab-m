package uk.ac.ebi.pride.jmztab2.model;

import org.lifstools.mztab2.io.ColumnStructureAssertions;
import org.lifstools.mztab2.io.MzTabNonValidatingWriter;
import org.lifstools.mztab2.model.Assay;
import org.lifstools.mztab2.model.CV;
import org.lifstools.mztab2.model.Database;
import org.lifstools.mztab2.model.Metadata;
import org.lifstools.mztab2.model.MsRun;
import org.lifstools.mztab2.model.MzTab;
import org.lifstools.mztab2.model.Parameter;
import org.lifstools.mztab2.model.SmallMoleculeSummary;
import org.lifstools.mztab2.model.Software;
import org.lifstools.mztab2.model.StudyVariable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ebi.pride.jmztab2.model.OptColumnMappingBuilder.IndexedElementOptColumnMappingBuilder;

/**
 * @author qingwei
 * @since 29/05/13
 */
public class MZTabColumnFactoryTest {

    /**
     * https://github.com/PRIDE-Utilities/jmzTab/issues/11
     */
    @Test
    public void testOptionalColumnsAndManyRows(@TempDir Path tempDir) throws IOException {
        int files = 250;
        int molecules = 500;
        Metadata mtd = new Metadata();
        mtd.setMzTabVersion(MZTabConstants.VERSION_MZTAB_M);
        mtd.setMzTabID("testId1234");
        mtd.addSoftwareItem(new Software().id(1).parameter(new Parameter().cvLabel("MS")
            .cvAccession("MS:1001582").name("XCMS").value("3.1.1")));
        mtd.quantificationMethod(new Parameter().cvLabel("MS").cvAccession("MS:1001834")
            .name("LC-MS label-free quantitation analysis"));
        mtd.addCvItem(new CV().id(1).label("MS").fullName("PSI-MS controlled vocabulary")
            .version("4.1.138").uri(URI.create("https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo")));
        mtd.addDatabaseItem(new Database().id(1).param(new Parameter().name("PubChem"))
            .prefix("PUBCHEM").version("2024").uri(URI.create("https://pubchem.ncbi.nlm.nih.gov")));
        mtd.setSmallMoleculeQuantificationUnit(new Parameter().cvLabel("MS")
            .cvAccession("MS:1002887").name("Progenesis QI normalised abundance"));
        // The writer always emits an SFH header line, including all abundance_assay
        // columns for every assay in the metadata, even though this test writes no
        // SmallMoleculeFeature rows. SFHLineParser.refine() therefore requires this
        // quantification unit to be present, just like the SMH section requires
        // smallMoleculeQuantificationUnit above.
        mtd.setSmallMoleculeFeatureQuantificationUnit(new Parameter().cvLabel("MS")
            .cvAccession("MS:1002887").name("Progenesis QI normalised abundance"));
        mtd.smallMoleculeIdentificationReliability(new Parameter().cvLabel("MS")
            .cvAccession("MS:1002896").name("compound identification confidence level"));

        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_mz_opt = new LinkedHashMap<>();
        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_rt_opt = new LinkedHashMap<>();
        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_height_opt = new LinkedHashMap<>();
        MzTab mzTab = new MzTab();
        mzTab.metadata(mtd);
        mtd.addStudyVariableItem(new StudyVariable().id(1).name("first study variable")
            .description("first study variable"));
        mtd.addStudyVariableItem(new StudyVariable().id(2).name("second study variable")
            .description("second study variable"));
        for (int fileCounter = 1; fileCounter <= files; fileCounter++) {

            MsRun msRun = new MsRun().id(fileCounter)
                .location(URI.create("file:///data/run" + fileCounter + ".mzML"));
            // location and scan_polarity are mandatory per ms_run; see MsRunValidator.
            msRun.addScanPolarityItem(new Parameter().cvLabel("MS")
                .cvAccession("MS:1000130").name("positive scan"));
            mtd.addMsRunItem(msRun);
            Assay assay = new Assay().id(fileCounter).name("assay "+fileCounter);
            assay.addMsRunRefItem(msRun);
            mtd.addAssayItem(assay);
            if(fileCounter<files/2) {
                mtd.getStudyVariable().get(0).addAssayRefsItem(assay);
            } else {
                mtd.getStudyVariable().get(1).addAssayRefsItem(assay);
            }

            peak_mz_opt.put(assay, OptColumnMappingBuilder.forIndexedElement(assay).withName("peak_mz"));
            peak_rt_opt.put(assay, OptColumnMappingBuilder.forIndexedElement(assay).withName("peak_rt"));
            peak_height_opt.put(assay, OptColumnMappingBuilder.forIndexedElement(assay).withName("peak_height"));

        }
        for (int i = 1; i<=molecules; i++) {
            // reliability is mandatory (non-null) per row; see SMLLineParser.checkData().
            SmallMoleculeSummary sms = new SmallMoleculeSummary().smlId(i).reliability("2");
            double sumAbundanceSv1 = 0;
            double sumAbundanceSv2 = 0;
            for(int fileCounter=1;fileCounter<=files; fileCounter++) {
                double abundanceAssay = Math.random();
                sms.addAbundanceAssayItem(abundanceAssay);
                if(fileCounter<files/2) {
                    sumAbundanceSv1+=abundanceAssay;
                } else {
                    sumAbundanceSv2+=abundanceAssay;
                }
                sms.addOptItem(peak_mz_opt.get(mtd.getAssay().get(fileCounter-1)).build(""+1000*Math.random()));
                sms.addOptItem(peak_rt_opt.get(mtd.getAssay().get(fileCounter-1)).build(""+8000*Math.random()));
                sms.addOptItem(peak_height_opt.get(mtd.getAssay().get(fileCounter-1)).build(""+1.0e7*Math.random()));
            }
            double sv1Mean = sumAbundanceSv1/(double)files/2.0d;
            double sv2Mean = sumAbundanceSv2/(double)files/2.0d;
            sms.addAbundanceStudyVariableItem(sv1Mean);
            sms.addAbundanceStudyVariableItem(sv2Mean);
            double sv1stddev = 0;
            double sv2stddev = 0;
            for(int fileCounter=1;fileCounter<files; fileCounter++) {
                if(fileCounter<files/2) {
                    sv1stddev += Math.pow(sms.getAbundanceAssay().get(fileCounter-1)-sv1Mean,2);
                } else {
                    sv2stddev = Math.pow(sms.getAbundanceAssay().get(fileCounter-1)-sv1Mean,2);
                }
            }
            sv1stddev = Math.sqrt(sv1stddev/(files-1.0d));
            sv2stddev = Math.sqrt(sv2stddev/(files-1.0d));
            sms.addAbundanceVariationStudyVariableItem(sv1stddev);
            sms.addAbundanceVariationStudyVariableItem(sv2stddev);
            mzTab.addSmallMoleculeSummaryItem(sms);
        }
        assertEquals(molecules, mzTab.getSmallMoleculeSummary().size());
        assertEquals(files*3, mzTab.getSmallMoleculeSummary().get(0).getOpt().size());
        Path file = tempDir.resolve("many-columns.mztab");
        new MzTabNonValidatingWriter().write(file, mzTab);
        MzTab parsed = ColumnStructureAssertions.parseWithoutColumnErrors(file);
        assertEquals(molecules, parsed.getSmallMoleculeSummary().size());
        for (int row : new int[]{0, molecules - 1}) {
            SmallMoleculeSummary expected = mzTab.getSmallMoleculeSummary().get(row);
            SmallMoleculeSummary actual = parsed.getSmallMoleculeSummary().stream()
                .filter(s -> s.getSmlId().equals(expected.getSmlId())).findFirst().orElseThrow();
            assertEquals(files, actual.getAbundanceAssay().size());
            for (int a = 0; a < files; a++) {
                assertEquals(expected.getAbundanceAssay().get(a), actual.getAbundanceAssay().get(a), 1e-9);
            }
            assertEquals(files * 3, actual.getOpt().size());
            for (int o = 0; o < files * 3; o++) {
                // OptColumnMappingBuilder.build() sets the identifier to the full
                // header (including the "opt_" prefix), while parsing strips that
                // prefix (see SMLLineParser.checkData()); normalize before comparing.
                String expectedIdentifier = expected.getOpt().get(o).getIdentifier();
                if (expectedIdentifier.startsWith(MZTabConstants.OPT_PREFIX)) {
                    expectedIdentifier = expectedIdentifier.substring(MZTabConstants.OPT_PREFIX.length());
                }
                assertEquals(expectedIdentifier, actual.getOpt().get(o).getIdentifier());
                assertEquals(expected.getOpt().get(o).getValue(), actual.getOpt().get(o).getValue());
            }
        }
    }

    @Test
    public void positionsStayUniqueAndNumericBeyond99Columns() {
        MZTabColumnFactory factory = MZTabColumnFactory.getInstance(Section.Small_Molecule_Header);
        for (ISmallMoleculeColumn column : SmallMoleculeColumn.Stable.columns()) {
            factory.addStableColumn(column, column.getOrder());
        }
        int order = 13;
        List<Assay> assays = new ArrayList<>();
        for (int i = 1; i <= 150; i++) {
            Assay assay = new Assay().id(i).name("assay " + i);
            assays.add(assay);
            factory.addAbundanceOptionalColumn(assay, ++order);
        }
        List<StudyVariable> studyVariables = new ArrayList<>();
        for (int j = 1; j <= 3; j++) {
            StudyVariable sv = new StudyVariable().id(j).name("sv " + j);
            studyVariables.add(sv);
            factory.addAbundanceOptionalColumn(sv, "study_variable[" + j + "]", ++order);
            factory.addAbundanceOptionalColumn(sv, "variation_study_variable[" + j + "]", ++order);
        }
        for (int k = 1; k <= 50; k++) {
            factory.addOptionalColumn("feature_" + k, String.class);
            factory.addOptionalColumn(assays.get(k - 1), "peak_mz", String.class);
            factory.addOptionalColumn(studyVariables.get(k % 3), "x" + k, String.class);
            factory.addOptionalColumn(new Parameter().cvLabel("MS")
                .cvAccession("MS:" + (1000000 + k)).name("param " + k), String.class);
        }

        assertEquals(13 + 150 + 6 + 200, factory.getColumnMapping().size());
        int previousOrder = 0;
        Set<String> headers = new HashSet<>();
        for (IMZTabColumn column : factory.getColumnMapping().values()) {
            assertTrue(column.getPosition().order() > previousOrder,
                "order not increasing at " + column.getHeader());
            previousOrder = column.getPosition().order();
            assertTrue(headers.add(column.getHeader().toLowerCase()), "duplicate header " + column.getHeader());
        }
        assertEquals(369, previousOrder);
        assertEquals("abundance_assay[1]", factory.getOffsetColumnsMap().get(14).getHeader());
        assertEquals("abundance_assay[150]", factory.getOffsetColumnsMap().get(163).getHeader());
        assertEquals("opt_global_feature_1", factory.getOffsetColumnsMap().get(170).getHeader());
        assertEquals(150 + 6, factory.getAbundanceColumnMapping().size());
        assertEquals("opt_global_feature_50", factory.findColumnByHeader("OPT_GLOBAL_FEATURE_50").getHeader());
    }

    @Test
    public void registeringATakenPositionThrows() {
        MZTabColumnFactory factory = MZTabColumnFactory.getInstance(Section.Small_Molecule_Header);
        factory.addStableColumn(SmallMoleculeColumn.Stable.columnFor(SmallMoleculeColumn.Stable.SML_ID), 1);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            factory.addStableColumn(SmallMoleculeColumn.Stable.columnFor(SmallMoleculeColumn.Stable.SMF_ID_REFS), 1));
        assertEquals("Position 1.0.0 for column 'SMF_ID_REFS' is already assigned to 'SML_ID'", e.getMessage());
        assertEquals(1, factory.getColumnMapping().size());
    }

    @Test
    public void registeringAHeaderTwiceThrows() {
        MZTabColumnFactory factory = MZTabColumnFactory.getInstance(Section.Small_Molecule_Feature_Header);
        factory.addOptionalColumn("mzmin", String.class, 20);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            factory.addOptionalColumn("MZMIN", String.class, 21));
        assertEquals("Column header 'opt_global_MZMIN' is already defined", e.getMessage());
    }

    @Test
    public void mixingExplicitAndConvenienceOrdersReportsClash() {
        MZTabColumnFactory factory = MZTabColumnFactory.getInstance(Section.Small_Molecule_Feature_Header);
        factory.addOptionalColumn("a", String.class, 5);
        assertEquals(6, factory.addOptionalColumn("b", String.class).order());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
            factory.addOptionalColumn("c", String.class, 6));
        assertEquals("Position 6.0.0 for column 'opt_global_c' is already assigned to 'opt_global_b'", e.getMessage());
    }

    @Test
    public void columnMapsAreUnmodifiable() {
        MZTabColumnFactory factory = MZTabColumnFactory.getInstance(Section.Small_Molecule_Header);
        factory.addOptionalColumn("a", String.class);
        assertThrows(UnsupportedOperationException.class, () -> factory.getColumnMapping().clear());
        assertThrows(UnsupportedOperationException.class, () -> factory.getOptionalColumnMapping().clear());
        assertThrows(UnsupportedOperationException.class, () -> factory.getStableColumnMapping().clear());
        assertThrows(UnsupportedOperationException.class, () -> factory.getAbundanceColumnMapping().clear());
    }

    @Test
    public void idConfidenceMeasureColumnsBeyond99() {
        MZTabColumnFactory factory = MZTabColumnFactory.getInstance(Section.Small_Molecule_Evidence_Header);
        for (int k = 1; k <= 110; k++) {
            factory.addIdConfidenceMeasureColumn(new Parameter().name("measure " + k), k, Double.class);
        }
        assertEquals(110, factory.getColumnMapping().size());
        IMZTabColumn last = factory.getColumnMapping().get(factory.getColumnMapping().lastKey());
        assertEquals("id_confidence_measure[110]", last.getHeader());
        assertEquals(ColumnPosition.of(110, 110, 0), last.getPosition());
    }
}

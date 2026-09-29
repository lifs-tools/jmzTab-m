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
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("lifs-tools/jmzTab-m#187: enabled by the ColumnPosition refactoring")
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
        mtd.smallMoleculeIdentificationReliability(new Parameter().cvLabel("MS")
            .cvAccession("MS:1002896").name("compound identification confidence level"));

        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_mz_opt = new LinkedHashMap<>();
        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_rt_opt = new LinkedHashMap<>();
        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_height_opt = new LinkedHashMap<>();
        MzTab mzTab = new MzTab();
        mzTab.metadata(mtd);
        mtd.addStudyVariableItem(new StudyVariable().id(1).name("first study variable"));
        mtd.addStudyVariableItem(new StudyVariable().id(2).name("second study variable"));
        for (int fileCounter = 1; fileCounter <= files; fileCounter++) {

            MsRun msRun = new MsRun().id(fileCounter)
                .location(URI.create("file:///data/run" + fileCounter + ".mzML"));
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
            SmallMoleculeSummary sms = new SmallMoleculeSummary().smlId(i);
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
                assertEquals(expected.getOpt().get(o).getIdentifier(), actual.getOpt().get(o).getIdentifier());
                assertEquals(expected.getOpt().get(o).getValue(), actual.getOpt().get(o).getValue());
            }
        }
    }
}

package uk.ac.ebi.pride.jmztab2.model;

import de.isas.mztab2.io.MzTabNonValidatingWriter;
import de.isas.mztab2.model.Assay;
import de.isas.mztab2.model.IndexedElementAdapter;
import de.isas.mztab2.model.Metadata;
import de.isas.mztab2.model.MsRun;
import de.isas.mztab2.model.MzTab;
import de.isas.mztab2.model.SmallMoleculeSummary;
import de.isas.mztab2.model.StudyVariable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
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
    public void testOptionalColumnsAndManyRows() throws IOException {
        int files = 250;
        int molecules = 500;
        Metadata mtd = new Metadata();
        mtd.setMzTabVersion(MZTabConstants.VERSION_MZTAB_M);
        mtd.setMzTabID("testId1234");

        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_mz_opt = new LinkedHashMap<>();
        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_rt_opt = new LinkedHashMap<>();
        Map<Assay, IndexedElementOptColumnMappingBuilder> peak_height_opt = new LinkedHashMap<>();
        MzTab mzTab = new MzTab();
        mzTab.metadata(mtd);
        mtd.addStudyVariableItem(new StudyVariable().id(1).name("first study variable"));
        mtd.addStudyVariableItem(new StudyVariable().id(2).name("second study variable"));
        for (int fileCounter = 1; fileCounter <= files; fileCounter++) {

            MsRun msRun = new MsRun().id(fileCounter).name("ms run "+fileCounter);
            mtd.addMsRunItem(msRun);
            Assay assay = new Assay().id(fileCounter).name("assay "+fileCounter);
            assay.addMsRunRefItem(msRun);
            mtd.addAssayItem(assay);
            if(fileCounter<files/2) {
                mtd.getStudyVariable().get(0).addAssayRefsItem(assay);
            } else {
                mtd.getStudyVariable().get(1).addAssayRefsItem(assay);
            }

            peak_mz_opt.put(assay, OptColumnMappingBuilder.forIndexedElement(new IndexedElementAdapter(assay)).withName("peak_mz"));
            peak_rt_opt.put(assay, OptColumnMappingBuilder.forIndexedElement(new IndexedElementAdapter(assay)).withName("peak_rt"));
            peak_height_opt.put(assay, OptColumnMappingBuilder.forIndexedElement(new IndexedElementAdapter(assay)).withName("peak_height"));

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
        MzTabNonValidatingWriter writer = new MzTabNonValidatingWriter();
        try (OutputStreamWriter osw = new OutputStreamWriter(System.out,
             StandardCharsets.UTF_8)) {
            writer.write(osw, mzTab);
        }
    }
}

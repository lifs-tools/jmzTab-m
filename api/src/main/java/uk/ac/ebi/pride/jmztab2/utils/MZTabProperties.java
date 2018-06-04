package uk.ac.ebi.pride.jmztab2.utils;

import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;

import java.io.IOException;
import java.util.Properties;

import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * This Class will load the properties used by the mzTab library
 *
 * @author qingwei
 * @since 29/01/13
 * 
 */
public final class MZTabProperties {
//    private static Logger logger = LoggerFactory.getLogger(MZTabProperties.class);
    
    private MZTabProperties()  {
        
    }

    private static final Properties properties = new Properties();
    static {
        final String mzTabProperties = "/conf1_1/mztab.properties";
        final String formatProperties = "/conf1_1/mztab_format_error.properties";
        final String logicalProperties = "/conf1_1/mztab_logical_error.properties";
        final String crosscheckProperties = "/conf1_1/mztab_crosscheck_error.properties";
        try {
            properties.load(MZTabProperties.class.getResourceAsStream(mzTabProperties));
            properties.load(MZTabProperties.class.getResourceAsStream(formatProperties));
            properties.load(MZTabProperties.class.getResourceAsStream(logicalProperties));
            properties.load(MZTabProperties.class.getResourceAsStream(crosscheckProperties));
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }
    }

    /**
     * <p>getProperty.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /** Constant <code>MZTabExceptionMessage="There exist errors in the metadata sect"{trunked}</code> */
    public final static String MZTabExceptionMessage = "There exist errors in the metadata section or " +
            "protein/peptide/small_molecule/small_molecule_feature/small_molecule_evidence header section! Validation will stop, and ignore data table check!" + NEW_LINE;
    /** Constant <code>MZTabErrorOverflowExceptionMessage="System error queue overflow! + NEW_LINE"</code> */
    public final static String MZTabErrorOverflowExceptionMessage = "System error queue overflow!" + NEW_LINE;

    /** Constant <code>ENCODE="getProperty(mztab.encode)"</code> */
    public final static String ENCODE = getProperty("mztab.encode");
    /** Constant <code>MAX_ERROR_COUNT=Integer.parseInt(getProperty("mztab.max_error_count"))</code> */
    public final static int MAX_ERROR_COUNT = Integer.parseInt(getProperty("mztab.max_error_count"));
    /** Constant <code>LEVEL</code> */
    public final static LogicalErrorType.Level LEVEL = MZTabErrorType.findLevel(getProperty("mztab.level"));

}

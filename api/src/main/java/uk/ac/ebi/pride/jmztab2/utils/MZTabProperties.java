package uk.ac.ebi.pride.jmztab2.utils;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.NEW_LINE;
import uk.ac.ebi.pride.jmztab2.utils.errors.LogicalErrorType;
import uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType;

/**
 * This class will load the properties used by the mzTab-M library for message formatting during validation.
 *
 * @author qingwei
 * @author nilshoffmann
 * @since 29/01/13
 *
 */
public final class MZTabProperties {

    private static Logger LOGGER = LoggerFactory.
        getLogger(MZTabProperties.class);

    private MZTabProperties() {

    }

    private static final Properties properties = new Properties();

    static {
        loadProperties("/conf1_1/mztab.properties");
        loadProperties("/conf1_1/mztab_format_error.properties");
        loadProperties("/conf1_1/mztab_logical_error.properties");
        loadProperties("/conf1_1/mztab_crosscheck_error.properties");
    }

    private static void loadProperties(String path) {
        try {
            properties.load(MZTabProperties.class.getResourceAsStream(path));
        } catch (IOException e) {
            LOGGER.error(
                "Could not load properties from classpath location: " + path, e.
                    getMessage());
        }
    }

    /**
     * <p>
     * getProperty.</p>
     *
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Constant
     * <code>MZTAB_EXCEPTION_MESSAGE="There exist errors in the metadata sect"{trunked}</code>
     */
    public static final String MZTAB_EXCEPTION_MESSAGE = "There exist errors in the metadata section or "
        + "protein/peptide/small_molecule/small_molecule_feature/small_molecule_evidence header section! Validation will stop, and ignore data table check!" + NEW_LINE;
    /**
     * Constant
     * <code>MZTAB_ERROR_OVERFLOW_EXCEPTION_MESSAGE="System error queue overflow! + NEW_LINE"</code>
     */
    public static final String MZTAB_ERROR_OVERFLOW_EXCEPTION_MESSAGE = "System error queue overflow!" + NEW_LINE;

    /**
     * Constant <code>ENCODE="getProperty(mztab.encode)"</code>
     */
    public static final String ENCODE = getProperty("mztab.encode");
    /**
     * Constant
     * <code>MAX_ERROR_COUNT=Integer.parseInt(getProperty("mztab.max_error_count"))</code>
     */
    public static final int MAX_ERROR_COUNT = Integer.parseInt(getProperty(
        "mztab.max_error_count"));
    /**
     * Constant <code>LEVEL</code>
     */
    public static final LogicalErrorType.Level LEVEL = MZTabErrorType.findLevel(
        getProperty("mztab.level"));

}

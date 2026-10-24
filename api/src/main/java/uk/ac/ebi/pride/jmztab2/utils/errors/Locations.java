package uk.ac.ebi.pride.jmztab2.utils.errors;

import org.lifstools.mztab2.model.ValidationLocation;
import org.lifstools.mztab2.model.ValidationLocation.SectionEnum;

/**
 * Factory methods for {@link ValidationLocation} instances.
 */
public final class Locations {

    private static final String GENERAL_GROUP = "general";

    private Locations() {
    }

    /**
     * Location of a metadata key, e.g. {@code sample[1]-species[1]}.
     *
     * @param key the full MTD key
     * @return a location in section MTD with key and group set
     */
    public static ValidationLocation metadataKey(String key) {
        return new ValidationLocation().section(SectionEnum.MTD).metadataKey(key).metadataGroup(groupOf(key));
    }

    /**
     * Location of a whole metadata group, e.g. {@code sample}.
     *
     * @param group the metadata group
     * @return a location in section MTD with only the group set
     */
    public static ValidationLocation metadataGroup(String group) {
        return new ValidationLocation().section(SectionEnum.MTD).metadataGroup(group);
    }

    /**
     * Location of a table cell.
     *
     * @param section  the table section
     * @param rowIndex 0-based index in the model list of the table
     * @param column   the column name, or null if the row as a whole is meant
     * @return the location
     */
    public static ValidationLocation tableCell(SectionEnum section, int rowIndex, String column) {
        return new ValidationLocation().section(section).rowIndex(rowIndex).column(column);
    }

    /**
     * Location of a column in a table header.
     *
     * @param headerSection the header section (SMH, SFH or SEH)
     * @param column        the column name
     * @return the location
     */
    public static ValidationLocation headerColumn(SectionEnum headerSection, String column) {
        return new ValidationLocation().section(headerSection).column(column);
    }

    /**
     * Location that names only a section.
     *
     * @param section the section
     * @return the location
     */
    public static ValidationLocation section(SectionEnum section) {
        return new ValidationLocation().section(section);
    }

    /**
     * The metadata group of a key: its prefix before the first '[', or
     * "general" if the key has no '['.
     *
     * @param key the full MTD key
     * @return the group
     */
    public static String groupOf(String key) {
        int bracket = key.indexOf('[');
        return bracket < 0 ? GENERAL_GROUP : key.substring(0, bracket);
    }

    /**
     * Maps a line prefix to its section.
     *
     * @param linePrefix one of MTD, SMH, SML, SFH, SMF, SEH, SME, COM (case-insensitive)
     * @return the section, or null for any other prefix
     */
    public static SectionEnum sectionOf(String linePrefix) {
        if (linePrefix == null) {
            return null;
        }
        switch (linePrefix.toUpperCase(java.util.Locale.ROOT)) {
            case "MTD":
                return SectionEnum.MTD;
            case "SMH":
                return SectionEnum.SMH;
            case "SML":
                return SectionEnum.SML;
            case "SFH":
                return SectionEnum.SFH;
            case "SMF":
                return SectionEnum.SMF;
            case "SEH":
                return SectionEnum.SEH;
            case "SME":
                return SectionEnum.SME;
            case "COM":
                return SectionEnum.COM;
            default:
                return null;
        }
    }
}

package uk.ac.ebi.pride.jmztab2.utils.errors;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * This list class used to storage all types of MZTabError, and provide query service based on
 * the error/warn code. This class used to generate mzTab help document.
 *
 * @author qingwei
 * @since 27/02/13
 * 
 */
public final class MZTabErrorTypeMap {
    private final Map<Integer, MZTabErrorType> typeMap = new TreeMap<Integer, MZTabErrorType>();

    private void add(MZTabErrorType type) {
        typeMap.put(type.getCode(), type);
    }

    /**
     * <p>Constructor for MZTabErrorTypeMap.</p>
     */
    public MZTabErrorTypeMap() {
        Arrays.stream(FormatErrorType.VALUES).forEach((error) -> {
            add(error);
        });
        Arrays.stream(LogicalErrorType.VALUES).forEach((error) -> {
            add(error);
        });
        Arrays.stream(CrossCheckErrorType.VALUES).forEach((error) -> {
            add(error);
        });
    }

    /**
     * <p>getType.</p>
     *
     * @param code a int.
     * @return a {@link uk.ac.ebi.pride.jmztab2.utils.errors.MZTabErrorType} object.
     */
    public MZTabErrorType getType(int code) {
        return typeMap.get(code);
    }

    /**
     * <p>Getter for the field <code>typeMap</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<Integer, MZTabErrorType> getTypeMap() {
        return typeMap;
    }
}

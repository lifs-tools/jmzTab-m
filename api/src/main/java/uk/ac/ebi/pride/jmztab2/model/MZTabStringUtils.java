package uk.ac.ebi.pride.jmztab2.model;

import static uk.ac.ebi.pride.jmztab2.model.MZTabConstants.*;

/**
 * Provide a couple of functions for translate, parse and print formatted string defined in the mzTab specification.
 *
 * @author qingwei
 * @since 30/01/13
 * 
 */
public final class MZTabStringUtils {

    /**
     * Private constructor.
     */
    private MZTabStringUtils() {}
    
    /**
     * Check the string is null or blank.
     *
     * @param s a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    /**
     * Translate the string to the first char is upper case, others are lower case.
     *
     * @param s a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String toCapital(String s) {
        if (isEmpty(s)) {
            return s;
        }

        if (s.length() == 1) {
            return s.toUpperCase();
        }

        String firstChar = s.substring(0, 1);
        String leftString = s.substring(1);
        return firstChar.toUpperCase().concat(leftString.toLowerCase());
    }

    /**
     * Pre-process the String object. If object is null, return null; otherwise
     * remove heading and tailing white space.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String parseString(String target) {
        if (target == null || target.isEmpty() || target.trim().equalsIgnoreCase(NULL)) {
            return null;
        } else {
            return target.trim();
        }
    }

}

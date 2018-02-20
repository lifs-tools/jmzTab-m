package uk.ac.ebi.pride.jmztab1_1.model;

import java.util.ArrayList;

/**
 * This is list which each item split by a split char.
 *
 * @author qingwei
 * @since 31/01/13
 * 
 */
public class SplitList<E> extends ArrayList<E> {
    private char splitChar;

    /**
     * <p>Constructor for SplitList.</p>
     *
     * @param splitChar a char.
     */
    public SplitList(char splitChar) {
        this.splitChar = splitChar;
    }

    /**
     * <p>Getter for the field <code>splitChar</code>.</p>
     *
     * @return a char.
     */
    public char getSplitChar() {
        return splitChar;
    }

    /**
     * <p>Setter for the field <code>splitChar</code>.</p>
     *
     * @param splitChar a char.
     */
    public void setSplitChar(char splitChar) {
        this.splitChar = splitChar;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(get(0));

        for (int i = 1; i < size(); i++) {
            sb.append(splitChar).append(get(i));
        }

        return sb.toString();
    }
}

package uk.ac.ebi.pride.jmztab1_1.model;

/**
 * Define a couple constants used in the jmzTab API.
 *
 * @author qingwei
 * @since 08/03/13
 * 
 */
public final class MZTabConstants {
    /** Constant <code>NEW_LINE="System.getProperty(line.separator)"</code> */
    public final static String NEW_LINE = "\r\n";//System.getProperty("line.separator");
    /** Constant <code>NULL="null"</code> */
    public final static String NULL = "null";
    /** Constant <code>INFINITY="INF"</code> */
    public final static String INFINITY = "INF";
    /** Constant <code>CALCULATE_ERROR="NaN"</code> */
    public final static String CALCULATE_ERROR = "NaN";

    /** Constant <code>TAB='\u0009'</code> */
    public final static char TAB = '\u0009';
    /** Constant <code>TABs=""+'\u0009'</code> */
    public final static String TAB_STRING = ""+TAB;
    /** Constant <code>MINUS='-'</code> */
    public final static char MINUS = '-';
    /** Constant <code>BAR='\u007c'</code> */
    public final static char BAR = '\u007c';
    /** Constant <code>BAR='\u007c'</code> */
    public final static String BAR_S = ""+BAR;
    /** Constant <code>COLON=':'</code> */
    public final static char COLON = ':';
    /** Constant <code>COMMA=','</code> */
    public final static char COMMA = ',';
    /** Constant <code>VERSION="2.0.0-M"</code> */
    public final static String VERSION_MZTAB_M = "2.0.0-M";
    /** Constant <code>OPT_PREFIX="opt_"</code> */
    public final static String OPT_PREFIX = "opt_";
}

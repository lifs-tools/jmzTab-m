package uk.ac.ebi.pride.jmztab2.model;

/**
 * Define a couple of constants used in the jmzTab API.
 *
 * @author qingwei
 * @author Nils Hoffmann
 * @since 08/03/13
 * 
 */
public final class MZTabConstants {
    /** Constant <code>NEW_LINE="System.getProperty(line.separator)"</code> */
    public static final String NEW_LINE = "\r\n";//System.getProperty("line.separator");
    /** Constant <code>NULL="null"</code> */
    public static final String NULL = "null";
    /** Constant <code>INFINITY="INF"</code> */
    public static final String INFINITY = "INF";
    /** Constant <code>CALCULATE_ERROR="NaN"</code> */
    public static final String CALCULATE_ERROR = "NaN";

    /** Constant <code>TAB='\u0009'</code> */
    public static final char TAB = '\u0009';
    /** Constant <code>TABs=""+'\u0009'</code> */
    public static final String TAB_STRING = ""+TAB;
    /** Constant <code>MINUS='-'</code> */
    public static final char MINUS = '-';
    /** Constant <code>BAR='\u007c'</code> */
    public static final char BAR = '\u007c';
    /** Constant <code>BAR='\u007c'</code> */
    public static final String BAR_S = ""+BAR;
    /** Constant <code>COLON=':'</code> */
    public static final char COLON = ':';
    /** Constant <code>COMMA=','</code> */
    public static final char COMMA = ',';
    /** Constant <code>VERSION="2.0.0-M"</code> */
    public static final String VERSION_MZTAB_M = "2.0.0-M";
    /** Constant <code>OPT_PREFIX="opt_"</code> */
    public static final String OPT_PREFIX = "opt_";
    /** Constant <code>ABUNDANCE_PREFIX="abundance_"</code> */
    public static final String ABUNDANCE_PREFIX = "abundance_";
    /** Constant <code>GLOBAL="global"</code> */
    public static final String GLOBAL = "global";
    /** Constant <code>CV_PREFIX="cv_"</code> */
    public static final String CV_PREFIX = "cv_";
    
    /*
     Regular expressions that are not directly applicable to domain objects.
    */
    /** Constant <code>REGEX_INDEXED_VALUE="\\[(\\d+)\\]"</code> */
    public static final String REGEX_INDEXED_VALUE = "\\[(\\d+)\\]";
    /** Constant <code>REGEX_CV_PARAM_OPT_COLUMN_NAME="cv(_([A-Za-z0-9\\-\\[\\]:\\.]+))?(_([A-Za-z0-9_\\-\\[\\]:\\.]+)*)"</code> */
    public static final String REGEX_CV_PARAM_OPT_COLUMN_NAME = "cv(_([A-Za-z0-9\\-\\[\\]:\\.]+))?(_([A-Za-z0-9_\\-\\[\\]:\\.]+)*)";
    /** Constant <code>REGEX_OPT_COLUMN_NAME="opt_((assay|study_variable|ms_run)\\[(\\w+)\\]|global)_([A-Za-z0-9_\\-\\[\\]:\\.]+)"</code> */
    public static final String REGEX_OPT_COLUMN_NAME = "opt_((assay|study_variable|ms_run)\\[(\\w+)\\]|global)_([A-Za-z0-9_\\-\\[\\]:\\.]+)";
    /** Constant <code>REGEX_ABUNDANCE_COLUMN_NAME="abundance_(.+)"</code> */
    public static final String REGEX_ABUNDANCE_COLUMN_NAME = "abundance_(.+)";
    /** Constant <code>REGEX_ABUNDANCE_ASSAY_COLUMN_NAME="assay\\[(\\d+)\\]"</code> */
    public static final String REGEX_ABUNDANCE_ASSAY_COLUMN_NAME = "assay"+REGEX_INDEXED_VALUE;
    /** Constant <code>REGEX_STUDY_VARIABLE_COLUMN_NAME="study_variable\\[(\\d+)\\]"</code> */
    public static final String REGEX_STUDY_VARIABLE_COLUMN_NAME = "study_variable"+REGEX_INDEXED_VALUE;
    /** Constant <code>REGEX_NORMAL_METADATA="(\\w+)(\\[(\\w+)\\])?(-(\\w+)(\\[(\\w+)\\])?)?(-(\\w+))?"</code> */
    public static final String REGEX_NORMAL_METADATA = "(\\w+)(\\[(\\w+)\\])?(-(\\w+)(\\[(\\w+)\\])?)?(-(\\w+))?";
    /** Constant <code>REGEX_EMAIL="[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-']+)*@[A-Za-z0-9]+(?:[-.][A-Za-z0-9]+)*(\\.[A-Za-z]{2,})"</code> */
    public static final String REGEX_EMAIL = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-']+)*@[A-Za-z0-9]+(?:[-.][A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";
    /** Constant {@code REGEX_MZTAB_M="(?<major>[2]{1})\\.(?<minor>\\d{1})\\.(?<micro>\\d{1})-(?<profile>[M]{1})"} */
    public static final String REGEX_MZTAB_M = "(?<major>[2]{1})\\.(?<minor>\\d{1})\\.(?<micro>\\d{1})-(?<profile>[M]{1})";
    /** Constant <code>REGEX_PARAM_SPLIT=",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"</code> */
    public static final String REGEX_PARAM_SPLIT = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    /** Constant <code>REGEX_DEFAULT_RELIABILITY="[1234]{1}"</code> */
    public static final String REGEX_DEFAULT_RELIABILITY = "[1234]{1}";
    /** More complex for isotopes: \[\d*M([\w\d]+)*([+-][\w\d]+)*\]\d*[+-] **/
    /** Constant <code>REGEX_ADDUCT="^\[\d*M([+-][\w\d]+)*\]\d*[+-]$"</code> */
    public static final String REGEX_ADDUCT = "^\\[\\d*M([+-][\\w\\d]+)*\\]\\d*[+-]$";
    
}

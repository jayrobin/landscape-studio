/**
 * Constants holder for the program
 * 
 * Currently only used for:
 * -frame titles
 * -file extensions
 * -file prefixes
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class Constants
{
    public static final String NEW_NAME = "New";
    public static final String LOAD_NAME = "Load";
    public static final String SAVE_NAME = "Save";
    public static final String SAVEAS_NAME = "Save As";
    public static final String VIEW2DFRAME_NAME = "2D View";
    public static final String VIEW3DFRAME_NAME = "3D View";
    public static final String ALGORITHMFRAME_NAME = "Algorithm";
    public static final String PAINTFRAME_NAME = "Paint";
    public static final String COMBINEFRAME_NAME = "Combine";
    public static final String ADVANCEDFRAME_NAME = "Advanced";
    public static final String FILTERSFRAME_NAME = "Filters";
    
    public static final String CLOSE_NAME = "x";
    
    public static final String CLASS_EXTENSION = ".class";
    public static final String ALGORITHM_PREFIX = "ALG";
    public static final String IMPORTER_PREFIX = "IMP";
    public static final String EXPORTER_PREFIX = "EXP";
    public static final String FILTER_PREFIX = "FLT";
    
    public static final int SIZE_DEFAULT = 257;
    public static final int VIEW2DZOOM_DEFAULT = 257;
    public static final int VIEW2DZOOM_MAX = 2049;
    public static final int VIEW3DQUALITY_DEFAULT = 128;
    public static final int VIEW3DQUALITY_MIN = 32;
    public static final int VIEW3DQUALITY_MAX = 512;
    
    public int view2DZoom;
    public int view3DQuality;
}

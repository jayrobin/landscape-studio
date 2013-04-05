import java.io.FilenameFilter;
import java.io.File;

/**
 * Customisable filter
 * This is used to filter algorithms, exporters, importers
 * and filters
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class MultiFilter implements FilenameFilter {
    
    private String type;
    
    /**
     * Constructor for class MultiFilter
     */
    public MultiFilter(String type)
    {
        this.type = type;
    }
    
    /**
     * Determines whether a given file should be accepted
     * 
     * @param dir       The location of the file
     * @param name      The file name
     */
    public boolean accept(File dir, String name) 
    {
        if(type.equals(Constants.ALGORITHM_PREFIX)) {
            return (name.startsWith(Constants.ALGORITHM_PREFIX) 
                && name.endsWith(Constants.CLASS_EXTENSION));
        }
        else if(type.equals(Constants.IMPORTER_PREFIX)) {
            return (name.startsWith(Constants.IMPORTER_PREFIX) 
                && name.endsWith(Constants.CLASS_EXTENSION));
        }
        else if(type.equals(Constants.EXPORTER_PREFIX)) {
            return (name.startsWith(Constants.EXPORTER_PREFIX) 
                && name.endsWith(Constants.CLASS_EXTENSION));
        }
        else if(type.equals(Constants.FILTER_PREFIX)) {
            return (name.startsWith(Constants.FILTER_PREFIX) 
                && name.endsWith(Constants.CLASS_EXTENSION));
        }
        
        return false;
    }
}
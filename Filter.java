import java.io.File;

/**
 * Filters heightmap files using the following rule:
 * -Ends with .extension
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class Filter extends javax.swing.filechooser.FileFilter {
    
    private String extension;
    
    /**
     * Constructor for class MultiFilter
     */
    public Filter(String extension)
    {
        this.extension = extension;
    }
    
    /**
     * Determines whether a given file should be accepted
     * 
     * @param file      The file to be checked
     */
    public boolean accept(File file) 
    {
        // Automatically return true if it is a directory
        if (file.isDirectory()) 
            return true;
            
        return (file.getName().endsWith("." + extension));
    }
    
    /**
     * Returns a string representing the files accepted by this
     * filter
     * 
     * @return String The string representing the extension accepted
     */
    public String getDescription() 
    {
        return "*." + extension;
    }
}
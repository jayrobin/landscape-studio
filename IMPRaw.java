import java.io.*;
import javax.swing.*;
import Interfaces.*;

/**
 * RAW importer
 * Allows the loading of RAW heightmap files
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class IMPRaw implements IImporter
{   
    private ICanvasAlg parent;
    
    /**
     * Sets the parent of this class to the specified instance of
     * ICanvasAlg
     * 
     * @param parent    The new parent to be set
     */
    public void setParent(ICanvasAlg parent)
    {
        this.parent = parent;
    }
    
    /**
     * Returns a string representing this class
     * 
     * @return String   The string representing this class
     */
    public String toString()
    {
        return "raw";
    }
    
    /**
     * Loads the specified file and converts it into a two dimensional
     * integer array
     * 
     * @param file      The file to be loaded as specified by the user
     * 
     * @return int[][]  A two dimensional integer array representing a heightmap
     */
    public int[][] loadFile(File file)
    {
        try {            
            InputStream is = new FileInputStream(file);
    
            // Get the size of the file
            long length = file.length();
            
            // compare heightmap data size to width
            if((Math.sqrt(length) % 1) != 0) {
                JOptionPane.showMessageDialog(null, "Not a valid heightmap file");
                is.close();
                return null;
            }
            
            // Create the byte array to hold the data
            byte[] bytes = new byte[(int)length];
            
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
            
            // Close the input stream
            is.close();            
            
            int[] intBytes = new int[(int)length];
        
            parent.setProgressBar(bytes.length);
                        
            for(int i = 0; i < bytes.length; i++) {
                if(bytes[i] < 0) {
                    intBytes[i] = bytes[i] + 256;
                }
                else {
                    intBytes[i] = bytes[i];
                }
                
                parent.increaseProgressBar();
            }
            
            int width = (int) Math.sqrt(length);
            
            int[][] heightmap = new int[width][width];
            
            // write the data from the file into the heightmap
            int i = 0;
            int j = 0;
            
            for(i = 0; i < heightmap.length; i++) {
                for(j = 0; j < heightmap[i].length; j++) {
                    heightmap[j][i] = (int) intBytes[(i * width) + j];
                }
            }
            
            parent.amendLog("File successfully loaded: " + file.getName());
            
            return heightmap;
        } 
        catch (Exception e) { 
            parent.amendLog("Error loading file: " + file.getName()); 
            parent.displayError(e.toString(), "Could not load file", "Please check file is not corrupt by loading it in some other application");
        }
        
        return null;
    } 
}

import java.io.*;
import javax.swing.*;
import Interfaces.*;

/**
 * TER importer
 * Allows the loading of TER files
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class IMPTer implements IImporter
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
        return "ter";
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
            
            // Create the byte array to hold the data
            byte[] bytes = new byte[(int)length];
            
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            
            is.read(bytes);
            
            int size = (new Byte(bytes[29]).intValue() * 256) + new Byte(bytes[28]).intValue();
            for(int i = 30; i < bytes.length; i++) {
                if(new Byte(bytes[i]).intValue() == 65 &&
                       new Byte(bytes[i + 1]).intValue() == 76 &&
                       new Byte(bytes[i + 2]).intValue() == 84 &&
                       new Byte(bytes[i + 3]).intValue() == 87) {
                   offset = i + 8;
                   i = bytes.length;
                }
            }
            
            int[][] heightmap = new int[size][size];
            //int x = 0;
            //int y = 0;
            int j = offset;
            
            for(int x = size - 1; x > -1; x--) {
                for(int y = 0; y < size; y++) {
                    heightmap[y][x] = (new Byte(bytes[j + 1]).intValue() * 256) + new Byte(bytes[j]).intValue();
                    j += 2;
                }
            }
            
            // Close the input stream
            is.close();
            
            
            parent.amendLog("File successfully loaded: " + file.getName());
            
            return heightmap;
        } 
        catch (Exception e) { 
            parent.displayError(e.toString(), "Could not load file", "Please check file is not corrupt by loading it in some other application");
            parent.amendLog("Error loading file: " + file.getName()); 
        }
        
        return null;
    } 
}

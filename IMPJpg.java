import java.io.*;
import javax.swing.*;
import Interfaces.*;
import java.awt.image. BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * BMP importer
 * Allows the loading of BMP heightmap files
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class IMPJpg implements IImporter
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
        return "jpg"; 
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
            BufferedImage image = ImageIO.read(file);
            int[][] heightmap = new int[image.getHeight()][image.getWidth()];
            
            for(int i = 0; i < image.getWidth(); i++) {
                for(int j = 0; j < image.getHeight(); j++) {
                    heightmap[i][j] = image.getRGB(i, j);
                }
            }
            
            return heightmap;
        } 
        catch (Exception e) { 
            parent.amendLog("Error loading file: " + file.getName()); 
            parent.displayError(e.toString(), "Could not load file", "Please check file is not corrupt by loading it in some other application");
        }
        
        return null;
    }
}

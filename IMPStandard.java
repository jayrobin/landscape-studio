import java.io.*;
import javax.swing.*;
import Interfaces.*;

/**
 * HMP importer
 * Allows the loading of HMP heightmap files
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class IMPStandard implements IImporter
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
        return "hmp"; 
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
            BufferedReader in = new BufferedReader(new FileReader(file));
            
            // read the initial width value to initialise the heightmap
            String hmString = in.readLine();
            String[] heightData = hmString.split(":");
            
            // check the validity of the file
            if(heightData.length != 2) {
                JOptionPane.showMessageDialog(null, "Not a valid heightmap file");
                in.close();
                return null;
            }
            
            hmString = heightData[1];
            int width = Integer.parseInt(heightData[0]);
            int[][] heightmap = new int[width][width];
            
            heightData = hmString.split("_");
            
            // compare heightmap data size to width
            if(width != ((int)(Math.sqrt(heightData.length))) || (Math.sqrt(heightData.length) % 1) != 0) {
                JOptionPane.showMessageDialog(null, "Not a valid heightmap file");
                in.close();
                return null;
            }
            
            parent.setProgressBar(heightData.length);
            
            // write the data from the file into the heightmap
            int i = 0;
            int j = 0;
            int val = 0;
            for(int k = 0; k < heightData.length; k++) {
                try {
                    val = Integer.parseInt(heightData[k]);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Not a valid heightmap file");
                    in.close();
                    return null;
                }
                
                heightmap[i][j] = val;
                
                j++;
                if(j == heightmap.length) {
                    i++;
                    j = 0;
                }
                
                parent.increaseProgressBar();
            }
            
            parent.amendLog("File successfully loaded: " + file.getName());
            
            in.close();
            parent.resetProgressBar();
            return heightmap;
        } 
        catch (Exception e) { 
            parent.amendLog("Error loading file: " + file.getName()); 
            parent.displayError(e.toString(), "Could not load file", "Please check file is not corrupt by loading it in some other application");
        }
        
        return null;
    }
}

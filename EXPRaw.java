import java.io.*;
import Interfaces.*;

/**
 * RAW exporter
 * Allows the saving of heightmap files to HMP format
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class EXPRaw implements IExporter
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
     * Converts the heightmap and outputs it to the specified destination
     * 
     * @param file      The destination of the file to be saved
     * @param heightmap The heightmap to be saved
     */
    public void saveFile(File file, int[][] heightmap)
    {
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            
            heightmap = parent.normaliseMap(heightmap, 255);
            
            parent.setProgressBar(heightmap.length);
            
            byte[] col = new byte[heightmap.length];
            
            for(int i = 0; i < heightmap.length; i++) {
                String hmString = "";
                
                for(int j = 0; j < heightmap[i].length; j++) {
                    col[j] = (byte) heightmap[j][i];
                }
                
                out.write(col);
                
                parent.increaseProgressBar();
            }
            
            out.close();
            parent.resetProgressBar();
            parent.amendLog("File successfully saved as " + file.getName());
            
        } 
        catch (Exception e) { 
            parent.amendLog("Error saving file: " + file.getName()); 
            parent.displayError(e.toString(), "Could not save file", "Please check file is not write protected");
        }
    }
}

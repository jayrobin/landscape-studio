import java.io.*;
import Interfaces.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.*;

/**
 * HMP exporter
 * Allows the saving of heightmap files to HMP format
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class EXPJpg implements IExporter
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
     * Converts the heightmap and outputs it to the specified destination
     * 
     * @param file      The destination of the file to be saved
     * @param heightmap The heightmap to be saved
     */
    public void saveFile(File file, int[][] heightmap)
    {
        try {
            parent.setProgressBar(heightmap.length);
            
            heightmap = parent.normaliseMap(heightmap, 255);
            
            BufferedImage image = new BufferedImage(heightmap.length, heightmap.length, BufferedImage.TYPE_INT_RGB);
            // Create a graphics contents on the buffered image
            Graphics2D g2d = image.createGraphics();
            
            for(int i = 0; i < heightmap.length; i++) {
                for(int j = 0; j < heightmap[i].length; j++) {
                    g2d.setColor(new Color(heightmap[i][j], heightmap[i][j], heightmap[i][j]));
                    g2d.drawLine(i, j, i, j);
                }
                parent.increaseProgressBar();
            }
            
            ImageIO.write(image, "jpg", file);
            
            parent.resetProgressBar();
            
            parent.amendLog("File successfully saved as " + file.getName());
        } 
        catch (Exception e) { 
            parent.amendLog("Error saving file: " + file.getName()); 
            parent.displayError(e.toString(), "Could not save file", "Please check file is not write protected");
        }
    }
}

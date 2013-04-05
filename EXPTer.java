import java.io.*;
import Interfaces.*;

/**
 * TER exporter
 * Allows the saving of heightmap files to TER format
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class EXPTer implements IExporter
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
     * Converts the heightmap and outputs it to the specified destination
     * 
     * @param file      The destination of the file to be saved
     * @param heightmap The heightmap to be saved
     */
    public void saveFile(File file, int[][] heightmap)
    {
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            
            heightmap = parent.normaliseMap(heightmap, 30000);
            
            parent.setProgressBar(heightmap.length);
            
            int[][] nMap = new int[heightmap.length][heightmap[0].length];
            
            byte[] bStream;
            out.write("TERRAGENTERRAIN SIZE".getBytes());       // header
            
            out.write(intToByteArray2(heightmap.length - 1));   // heightmap size
            out.write((byte) 0);                                // padding
            out.write((byte) 0);                                // padding
            
            out.write("XPTS".getBytes());                       // text
            out.write(intToByteArray2(heightmap.length));       // x-points
            out.write((byte) 0);                                // padding
            out.write((byte) 0);                                // padding
            
            out.write("YPTS".getBytes());                       // text
            out.write(intToByteArray2(heightmap[0].length));    // y-points
            out.write((byte) 0);                                // padding
            out.write((byte) 0);                                // padding
            
            out.write("ALTW".getBytes());                       // text
            out.write(intToByteArray(8960));                    // height scale
            out.write(intToByteArray(3072));                     // base height
            
            for(int i = heightmap.length - 1; i >= 0; i--) {
                for(int j = 0; j < heightmap[i].length ; j++) {
                    nMap[i][j] = heightmap[j][i];
                    out.write(intToByteArray2(nMap[i][j]));      // elevation
                }
                
                parent.increaseProgressBar();
            }
            
            out.write("EOF ".getBytes());                       // text
            
            out.close();
            
            parent.resetProgressBar();
            
            parent.amendLog("File successfully saved as " + file.getName());
        } 
        catch (Exception e) { 
            parent.amendLog("Error saving file: " + file.getName()); 
            parent.displayError(e.toString(), "Could not save file", "Please check file is not write protected");
        }
    }
    
    private byte[] intToByteArray(int i) 
    {
		byte[] dword = new byte[2];
		dword[1] = (byte) (i & 0x00FF);
		dword[0] = (byte) ((i >> 8) & 0x000000FF);
		return dword;
	}
	
	private byte[] intToByteArray2(int i) 
    {
		byte[] dword = new byte[2];
		dword[0] = (byte) (i & 0x00FF);
		dword[1] = (byte) ((i >> 8) & 0x000000FF);
		return dword;
	}
}

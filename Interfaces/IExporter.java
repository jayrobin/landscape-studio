package Interfaces;

import java.io.*;

/**
 * Provides an interface for exporters. 
 * Includes methods for setting the parent, saving a file
 * and toString
 * 
 * @author James Robinson 
 * @version 1.0
 */
public interface IExporter
{
    public void setParent(ICanvasAlg parent);
    public void saveFile(File file, int[][] heightmap);
    public String toString();
}

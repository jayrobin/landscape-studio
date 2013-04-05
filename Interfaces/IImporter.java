package Interfaces;

import java.io.*;

/**
 * Provides an interface for importers. 
 * Includes methods for setting the parent, loading a file
 * and toString
 * 
 * @author James Robinson 
 * @version 1.0
 */
public interface IImporter
{
    public void setParent(ICanvasAlg parent);
    public int[][] loadFile(File file);
    public String toString();
}

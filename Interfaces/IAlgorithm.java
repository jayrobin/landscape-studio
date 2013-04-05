package Interfaces;

import javax.swing.*;

/**
 * Provides an interface for terrain generation algorithms. 
 * Includes methods for setting the container panel and parent
 * 
 * @author James Robinson 
 * @version 1.0
 */
public interface IAlgorithm
{
    public void setPanel(JPanel panel);
    public void setParent(ICanvasAlg parent);
    public String toString();
    public void randomise();
    public void setPreview(boolean preview);
}

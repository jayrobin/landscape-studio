package Interfaces;

import javax.swing.*;

/**
 * Provides an interface for filters. 
 * Includes methods for setting the container panel, 
 * parent and toString
 * 
 * @author James Robinson 
 * @version 1.0
 */
public interface IFilter
{
    public void setParent(ICanvasAlg parent);
    public void setPanel(JPanel panel);
    public String toString();
    public void setPreview(boolean preview);
}

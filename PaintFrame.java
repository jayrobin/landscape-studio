import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * JInternalFrame containing a top down view of the current terrain map with 
 * controls for adapting terrain using a ‘paint brush’ style interface 
 * (i.e. left click to increase height, right click to decrease height)
 * 
 * NOT YET IMPLEMENTED
 * 
 * @author James Robinson 
 * @version 0.1
 */
public class PaintFrame extends JInternalFrame
{
    private TabbedCanvas parent;

    /**
     * Constructor for objects of class PaintFrame
     */
    public PaintFrame(TabbedCanvas parent)
    {
        super(Constants.PAINTFRAME_NAME, false, true, false, false);
        this.setSize(new Dimension(300, 300));
        this.parent = parent;
        this.getContentPane().setLayout(new BorderLayout());
        this.setVisible(true);
    }
}

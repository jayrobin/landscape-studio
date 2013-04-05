import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import Interfaces.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Slide-Erode filter
 * Filter class, inverts the current heightmap
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class FLTErode implements IFilter, ActionListener, ChangeListener
{
    private ICanvasAlg parent;
    private JPanel panel;
    private JButton btnApply;
    private JSlider sldIterations;
    private JTextField txtIterations;
    private boolean preview = true;
    
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
    
    public void setPreview(boolean preview)
    {
        this.preview = preview;
    }
    
    /**
     * Sets the panel in which the GUI should be implemented
     * Any standard swing components can be used
     * 
     * @param panel The GUI panel
     */
    public void setPanel(JPanel panel)
    {
        this.panel = panel;
        
        panel.setLayout(new BorderLayout());
        
        JLabel label = new JLabel("Applies basic erosion", (int) JLabel.CENTER_ALIGNMENT);
        panel.add(label, BorderLayout.PAGE_START);
        
        JPanel iterations = new JPanel();
        iterations.setLayout(new FlowLayout());
        
        label = new JLabel("Iterations ");
        iterations.add(label);
        sldIterations = new JSlider(1, 100, 1);
        sldIterations.addChangeListener(this);
        iterations.add(sldIterations);
        txtIterations = new JTextField(Integer.toString((int)sldIterations.getValue()));
        txtIterations.setEditable(false);
        txtIterations.setPreferredSize(new Dimension(35, 25));
        iterations.add(txtIterations);
        
        panel.add(iterations, BorderLayout.CENTER);
        
        btnApply = new JButton("Apply Basic Erosion");
        btnApply.addActionListener(this);
        panel.add(btnApply, BorderLayout.PAGE_END);
        
        if(preview)
            parent.refreshMiniView(apply(preview));
    }
    
    /*
     * Returns a two dimensional integer array representing the heightmap
     * with the filter applied
     * 
     * @return int[][] The filter-applied heightmap
     */
    private int[][] applyErode(int[][] heightmap)
    {
        float filter = 0.4f;
        int[][] tempBuffer = heightmap;
        int size = tempBuffer.length;
        
        //erode left to right
        float v;

        for (int i = 0; i < size; i++) {
            v = tempBuffer[i][0];
            for (int j = 1; j < size; j++) {
                tempBuffer[i][j] = (int) (filter * v + (1 - filter) * tempBuffer[i][j]);
                v = tempBuffer[i][j];
            }
        }

        //erode right to left
        for (int i = size - 1; i >= 0; i--) {
            v = tempBuffer[i][0];
            for (int j = 0; j < size; j++) {
                tempBuffer[i][j] = (int) (filter * v + (1 - filter) * tempBuffer[i][j]);
                v = tempBuffer[i][j];
                //erodeBand(tempBuffer[size * i + size - 1], -1);
            }
        }

        //erode top to bottom
        for (int i = 0; i < size; i++) {
            v = tempBuffer[0][i];
            for (int j = 0; j < size; j++) {
                tempBuffer[j][i] = (int) (filter * v + (1 - filter) * tempBuffer[j][i]);
                v = tempBuffer[j][i];
            }
        }

        //erode from bottom to top
        for (int i = size - 1; i >= 0; i--) {
            v = tempBuffer[0][i];
            for (int j = 0; j < size; j++) {
                tempBuffer[j][i] = (int) (filter * v + (1 - filter) * tempBuffer[j][i]);
                v = tempBuffer[j][i];
            }
        }

        
        return tempBuffer;
    }
    
    /**
     * Returns a string representing this class, this is displayed in
     * the filters combo box
     * 
     * @return String The string representing this class
     */
    public String toString()
    {
        return "Basic Erosion";
    }
    
    private int[][] apply(boolean preview)
    {
        int[][] heightmap = new int[128][128];
        int step = 1;
        
        if(!preview) {
            parent.setProgressBar(sldIterations.getValue());
            heightmap = parent.cloneArray(parent.getHeightMap());
        }
        else {
            heightmap = parent.getPreviewMap();
            step = (int) Math.ceil((double)parent.getHeightMap().length / (double)parent.getPreviewMap().length);
        }
        
        for(int i = 0; i < sldIterations.getValue() / step; i++) {
            heightmap = applyErode(heightmap);
            
            if(!preview)
                parent.increaseProgressBar();
        }   
        
        if(!preview)
            parent.resetProgressBar();
        
        return heightmap;
    }
    
    /**
     * Called when an action event occurs, in this case the 'Invert Terrain' button
     * is the only component which generates an action event
     * 
     * @param e     The action event generated
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(btnApply)) {
            parent.amendLog("Applying filter: " + toString());
            parent.setHeightMap(apply(false));
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sldIterations)) {
            txtIterations.setText(Integer.toString((int)sldIterations.getValue()));
            if(preview) {
                parent.setPreviewMap(parent.getHeightMap(), 128);
                parent.refreshMiniView(apply(preview));
            }
        }
    }
}

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
public class FLTThermal implements IFilter, ActionListener, ChangeListener
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
        
        JLabel label = new JLabel("Produces an effect similar to thermal erosion", (int) JLabel.CENTER_ALIGNMENT);
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
        
        btnApply = new JButton("Apply Thermal Erosion");
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
    private int[][] applyThermal(int[][] heightmap)
    {
        for(int i = 0; i < heightmap.length; i++) {
            for(int j = 0; j < heightmap[i].length; j++) {
                for(int i2 = -1; i2 < 2; i2+=2) {
                    for(int j2 = -1; j2 < 2; j2+=2) {
                        if((i + i2 > 0) && (i + i2 < heightmap.length) && (j+j2 > 0) && (j + j2 < heightmap[i].length) && heightmap[i+i2][j+j2] > 0) {
                            int count = 0;
                            while(((double) (heightmap[i][j] - heightmap[i+i2][j+j2])) > 2 && heightmap[i+i2][j+j2] > 0) {
                                heightmap[i][j]--;
                                heightmap[i+i2][j+j2]++;
                            }
                        }
                    }
                }
            }
        }
        
        return heightmap;
    }
    
    /**
     * Returns a string representing this class, this is displayed in
     * the filters combo box
     * 
     * @return String The string representing this class
     */
    public String toString()
    {
        return "Thermal Erosion";
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
            heightmap = applyThermal(heightmap);
            
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

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import Interfaces.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Blur filter
 * Filter class, uses standard spatial averaging methods
 * to blur a heightmap
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class FLTSmooth implements IFilter, ActionListener, ChangeListener
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
        
        JLabel label = new JLabel("Smoothes the terrain using spatial averaging", (int) JLabel.CENTER_ALIGNMENT);
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
        
        btnApply = new JButton("Apply Smooth");
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
    private int[][] applySmooth(int[][] heightmap)
    {
        int[][] blurredmap = heightmap;
        
        for(int i = 0; i < heightmap.length; i++) {
            for(int j = 0; j < heightmap[i].length; j++) {
                int count = 0;
                int val = 0;
                for(int p = -1; p < 2; p++) {
                    for(int q = -1; q < 2; q++) {
                        if((i+p >= 0) && (i+p < heightmap.length) && (j+q >= 0) && (j+q < heightmap[i].length)) {
                            val += heightmap[i + p][j + q];
                            count++;
                        }
                    }
                }
                blurredmap[i][j] = (int) ((float)val / (float)count);
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
        return "Smooth";
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
            heightmap = applySmooth(heightmap);
            
            if(!preview)
                parent.increaseProgressBar();
        }   
        
        if(!preview)
            parent.resetProgressBar();
        
        return heightmap;
    }
    
    /**
     * Called when an action event occurs, in this case the 'Apply Blur' button
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

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import Interfaces.*;

/**
 * Invert filter
 * Filter class, inverts the current heightmap
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class FLTGradient implements IFilter, ActionListener
{
    private ICanvasAlg parent;
    private JPanel panel;
    private JButton btnApply;
    private JComboBox cmbOrientation;
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
        
        JLabel label = new JLabel("Applies a high-low gradient slope to the terrain", (int) JLabel.CENTER_ALIGNMENT);
        panel.add(label, BorderLayout.PAGE_START);
        
        cmbOrientation = new JComboBox();
        cmbOrientation.addActionListener(this);
        cmbOrientation.addItem("Top-Bottom");
        cmbOrientation.addItem("Bottom-Top");
        cmbOrientation.addItem("Left-Right");
        cmbOrientation.addItem("Right-Left");
        
        JPanel subPanel = new JPanel();
        subPanel.setLayout(new FlowLayout());
        subPanel.add(new JLabel("Orientation"));
        subPanel.add(cmbOrientation);
        panel.add(subPanel, BorderLayout.CENTER);
        
        btnApply = new JButton("Apply Gradient");
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
    private int[][] applyGradient()
    {
        int[][] heightmap = parent.cloneArray(parent.getHeightMap());
        
        for(int i = 0; i < heightmap.length; i++) {
            for(int j = 0; j < heightmap[i].length; j++) {
                if(cmbOrientation.getSelectedItem().equals("Top-Bottom")) {
                    heightmap[i][j] = (int) (((double) heightmap[i][j]) * ((double) (1 - ((double) j / (double) heightmap.length))));
                }
                else if(cmbOrientation.getSelectedItem().equals("Bottom-Top")) {
                    heightmap[i][j] = (int) (((double) heightmap[i][j]) * ((double) (((double) j / (double) heightmap.length))));
                }
                else if(cmbOrientation.getSelectedItem().equals("Left-Right")) {
                    heightmap[i][j] = (int) (((double) heightmap[i][j]) * ((double) (1 - ((double) i / (double) heightmap.length))));
                }
                else if(cmbOrientation.getSelectedItem().equals("Right-Left")) {
                    heightmap[i][j] = (int) (((double) heightmap[i][j]) * ((double) (((double) i / (double) heightmap.length))));
                }
            }
        }
        
        return heightmap;
    }
    
    private int[][] normaliseMap(int[][] map, int max)
    {
        int minAlg = map[0][0];
        int maxAlg = map[0][0];
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
               if(map[i][j] < minAlg) minAlg = map[i][j];
               if(map[i][j] > maxAlg) maxAlg = map[i][j];
            }
        }
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
                map[i][j] -= minAlg;
                map[i][j] = (int) (((double) map[i][j] / (maxAlg - minAlg)) * max);
            }
        }
        
        return map;
    }
    
    /**
     * Returns a string representing this class, this is displayed in
     * the filters combo box
     * 
     * @return String The string representing this class
     */
    public String toString()
    {
        return "Gradient";
    }
    
    private int[][] apply(boolean preview)
    {        
        return applyGradient();
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
        else if(e.getSource().equals(cmbOrientation)) {
            if(preview) {
                parent.setPreviewMap(parent.getHeightMap(), 128);
                parent.refreshMiniView(apply(preview));
            }
        }
    }
}

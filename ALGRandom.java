import java.awt.event.*;
import javax.swing.*;
import Interfaces.*;

/**
 * Pure random heightmap generation
 * (Generally used for debugging/combination/TAM purposes)
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class ALGRandom implements IAlgorithm, ActionListener
{
    private ICanvasAlg parent;
    private JPanel panel;
    private JButton btnGenerate;
    private int width;
    private JComboBox cmbSize;
    private boolean preview = true;
    
    /**
     * Sets the parent of this class to the specified instance of
     * ICanvasAlg
     * 
     * @param parent The new parent to be set
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
        
        cmbSize = new JComboBox();
        for(int i = 7; i < 13; i++) {
            cmbSize.addItem(Integer.toString((int) Math.pow(2, i) + 1));
        }
        cmbSize.setSelectedIndex(1); // Select 257 as width by default
        panel.add(cmbSize);
        
        btnGenerate = new JButton("Generate Random");
        btnGenerate.addActionListener(this);
        panel.add(btnGenerate);
        
        parent.refreshMiniView(generateTerrain(true));
    }
    
    public void randomise() 
    {
        if(preview)
            parent.refreshMiniView(generateTerrain(true));
    }
    
    /*
     * Returns a two dimensional integer array representing the heightmap
     * 
     * @return int[][] The random heightmap
     */
    private int[][] generateTerrain(boolean preview)
    {
        parent.amendLog("Generating terrain using " + toString());
        
        if(preview) {
            width = 128;
        }
        else {
            width = Integer.parseInt((String) cmbSize.getSelectedItem());
        }
        
        parent.setProgressBar(width);
        
        int[][] heightmap = new int[width][width];
        
        for(int i = 0; i < heightmap.length; i++) {
            for(int j = 0; j < heightmap[i].length; j++) {
                heightmap[i][j] = (int) (Math.random() * 256);
            }
            
            if(!preview)
                parent.increaseProgressBar();
        }
        
        parent.resetProgressBar();
        
        return heightmap;
    }
    
    /**
     * Returns a string representing this class, this is displayed in
     * the fractal method combo box
     * 
     * @return String The string representing this class
     */
    public String toString()
    {
        return "Random Heightmap";
    }
    
    /**
     * Called when an action event occurs, in this case the Generate button
     * is the only component which generates an action event
     * 
     * @param e     The action event generated
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(btnGenerate)) {
            parent.setHeightMap(generateTerrain(false));
        }
    }
}

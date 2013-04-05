import java.util.Random;
import java.awt.event.*;
import javax.swing.*;
import Interfaces.*;

/**
 * Offset-square algorithm.
 * Original code by James McNeill, adapted by Carl Burke,
 * adapted for this project by James Robinson
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class ALGOffset implements IAlgorithm, ActionListener
{
    private int width;
    private int[][] heightmap;
    private Random rgen;
    private boolean started;
    private ICanvasAlg parent;
    private JPanel panel;
    private JButton btnGenerate;
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
    
    public void setPreview(boolean preview) {}
    
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
        for(int i = 6; i < 12; i++) {
            cmbSize.addItem(Integer.toString((int) Math.pow(2, i)));
        }
        cmbSize.setSelectedIndex(2); // Select 256 as width by default
        panel.add(cmbSize);
        
        btnGenerate = new JButton("Generate OS Fractal");
        btnGenerate.addActionListener(this);
        panel.add(btnGenerate);
        
        rgen = new Random();
        
        parent.refreshMiniView(generateTerrain(true));
    }
    
    /*
     * rand1
     * 
     * @author C. Burke
     */
    private int rand1(int base, int delta)
    { 
        int i;
        i = (int) (base + (rgen.nextInt() % delta) - (delta / 2));
        return i;
    }
    
    /*
     * avgyvals
     * 
     * @author C. Burke
     */
    private int avgyvals(int i, int j, int strut, int dim)
    {
        if (i == 0)
            return ((heightmap[i][(j - strut) & (width - 1)] +
                     heightmap[i][(j + strut) & (width - 1)] +
                     heightmap[(i + strut) & (width - 1)][j]) / 3);
        else if (i == dim - 1)
            return ((heightmap[i][(j - strut) & (width - 1)] +
                     heightmap[i][(j + strut) & (width - 1)] +
                     heightmap[(i - strut) & (width - 1)][j]) / 3);
        else if (j == 0)
            return ((heightmap[(i - strut) & (width - 1)][j] +
                     heightmap[(i + strut) & (width - 1)][j] +
                     heightmap[i][(j + strut) & (width - 1)]) / 3);
        else if (j == dim - 1)
            return ((heightmap[(i - strut) & (width - 1)][j] +
                     heightmap[(i + strut) & (width - 1)][j] +
                     heightmap[i][(j - strut) & (width - 1)]) / 3);
        else
            return ((heightmap[(i - strut) & (width - 1)][j] +
                     heightmap[(i + strut) & (width - 1)][j] +
                     heightmap[i][(j - strut) & (width - 1)] +
                     heightmap[i][(j + strut) & (width - 1)]) / 4);
    }
    
    /*
     * avgyvals2
     * 
     * @author C. Burke
     */
    private int avgyvals2(int i, int j, int strut, int dim)
    {
        int tstrut = strut / 2;
        
        return ((heightmap[(i - tstrut) & (width - 1)][(j - tstrut) & (width - 1)] +
                 heightmap[(i - tstrut) & (width - 1)][(j + tstrut) & (width - 1)] +
                 heightmap[(i + tstrut) & (width - 1)][(j - tstrut) & (width - 1)] +
                 heightmap[(i + tstrut) & (width - 1)][(j + tstrut) & (width - 1)]) / 4);
    }
    
    /*
     * Returns a two dimensional integer array representing the heightmap
     * 
     * @return int[][] The plasma heightmap
     */
    private int[][] generateTerrain(boolean preview)
    {
        parent.amendLog("Generating terrain using " + toString());
        
        int row_offset = 0;  // start at zero for first row
        
        if(preview) {
            width = 128;
        }
        else {
            width = Integer.parseInt((String) cmbSize.getSelectedItem());
        }
        
        parent.setProgressBar(width);
        
        // initialise the heightmap
        heightmap = new int[width][width];
        
        // set the base values
        for(int i = 0; i < width; i++) 
            for(int j = 0;j < width; j++)
                heightmap[i][j] = 0;
                
        for (int square_size = width; square_size > 1; square_size /= 2)
        {
            int random_range = square_size;
            
            for (int x1 = row_offset; x1 < width; x1 += square_size)
            {
                for (int y1 = row_offset; y1 < width; y1 += square_size)
                {
                    // Get the four corner points.
                    int x2 = (x1 + square_size) % width;
                    int y2 = (y1 + square_size) % width;
                    
                    int i1 = heightmap[x1][y1];
                    int i2 = heightmap[x2][y1];
                    int i3 = heightmap[x1][y2];
                    int i4 = heightmap[x2][y2];
                    
                    // Obtain new points by averaging the corner points.
                    int p1 = ((i1 * 9) + (i2 * 3) + (i3 * 3) + (i4)) / 16;
                    int p2 = ((i1 * 3) + (i2 * 9) + (i3) + (i4 * 3)) / 16;
                    int p3 = ((i1 * 3) + (i2) + (i3 * 9) + (i4 * 3)) / 16;
                    int p4 = ((i1) + (i2 * 3) + (i3 * 3) + (i4 * 9)) / 16;
                    
                    // Add a random offset to each new point.
                    p1 = rand1(p1, random_range);
                    p2 = rand1(p2, random_range);
                    p3 = rand1(p3, random_range);
                    p4 = rand1(p4, random_range);
                    
                    // Write out the generated points.
                    int x3 = (x1 + square_size / 4) % width;
                    int y3 = (y1 + square_size / 4) % width;
                    x2 = (x3 + square_size / 2) % width;
                    y2 = (y3 + square_size / 2) % width;
                    
                    heightmap [x3][y3]   = p1;
                    heightmap [x2][y3]   = p2;
                    heightmap [x3][y2]   = p3;
                    heightmap [x2][y2]   = p4;
                }
                
                if(!preview)
                    parent.increaseProgressBar();
            }
            row_offset = square_size / 4;
        }
        
        parent.resetProgressBar();
        
        return heightmap;
    }
    
    public void randomise() 
    { 
        if(preview)
            parent.refreshMiniView(generateTerrain(true)); 
    }
    
    /**
     * Returns a string representing this class, this is displayed in
     * the fractal method combo box
     * 
     * @return String The string representing this class
     */
    public String toString()
    {
        return "Offset-Square Plasma Fractal";
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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import Interfaces.*;

/**
 * Prodedural Fractional Brownian Motion algorithm
 * Original code by Carl Burke, adapted by James Robinson
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class ALGFault implements IAlgorithm, ActionListener, ChangeListener
{
    private ICanvasAlg parent;
    private JPanel panel;
    private JButton btnGenerate;
    private boolean first_fBm = true;
    private double exponent_array[];
    private static final int B = 0x100;
    private static final int BM = 0xff;
    private static final int N = 0x1000;
    
    private int p[];
    private double g3[][];
    private Random rgen;
    private JSlider sldMinDelta, sldIterations, sldMaxDelta;
    private JTextField txtIterations, txtMinDelta, txtMaxDelta;
    private int size;
    private JComboBox cmbSize;
    private boolean preview = true;
    private Random randomizer;
    
    private int randomX1, randomZ1, randomX2, randomZ2;
    
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
    
    /**
     * Sets the panel in which the GUI should be implemented
     * Any standard swing components can be used
     * 
     * @param panel The GUI panel
     */
    public void setPanel(JPanel panel)
    {
        randomizer = new Random();
        
        this.panel = panel;
        panel.setLayout(new BorderLayout());
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblIterations = new JLabel("Iterations: ");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 15;
        controlPanel.add(lblIterations, c);
        
        sldIterations = new JSlider(1, 2000, 250);
        sldIterations.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        controlPanel.add(sldIterations, c);
        
        txtIterations = new JTextField(Integer.toString((int)sldIterations.getValue()));
        txtIterations.setEditable(false);
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 25;
        controlPanel.add(txtIterations, c);
        
        JLabel lblMinDelta = new JLabel("Min Delta: ");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(lblMinDelta, c);
        
        sldMinDelta = new JSlider(0, 100, 0);   // No divide
        sldMinDelta.setMajorTickSpacing(1);
        sldMinDelta.setSnapToTicks(true);
        sldMinDelta.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(sldMinDelta, c);
        
        txtMinDelta = new JTextField(Integer.toString(sldMinDelta.getValue()));
        txtMinDelta.setEditable(false);
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(txtMinDelta, c);
        
        JLabel lblMaxDelta = new JLabel("Max Delta: ");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(lblMaxDelta, c);
        
        sldMaxDelta = new JSlider(0, 100, 100);
        sldMaxDelta.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(sldMaxDelta, c);
        
        txtMaxDelta = new JTextField(Integer.toString(sldMaxDelta.getValue()));
        txtMaxDelta.setEditable(false);
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(txtMaxDelta, c);
        
        panel.add(controlPanel, BorderLayout.CENTER);
        
        JPanel sizePanel = new JPanel();
        sizePanel.setLayout(new FlowLayout());

        JLabel lblSize = new JLabel("Terrain Size: ");
        sizePanel.add(lblSize);
        
        cmbSize = new JComboBox();
        for(int i = 7; i < 12; i++) {
            cmbSize.addItem(Integer.toString((int) Math.pow(2, i) + 1));
        }
        cmbSize.setSelectedIndex(1); // Select 257 as width by default
        sizePanel.add(cmbSize);
        
        panel.add(sizePanel, BorderLayout.PAGE_START);
        
        btnGenerate = new JButton("Generate Fault Fractal");
        btnGenerate.addActionListener(this);
        panel.add(btnGenerate, BorderLayout.PAGE_END);
        
        randomise();
    }    
    
    public void randomise()
    {
        parent.refreshMiniView(generateTerrain(true));
    }
    
    /*
     * Returns a two dimensional integer array representing the heightmap
     * 
     * @return int[][] The fBm heightmap
     */
    private int[][] generateTerrain(boolean preview)
    {
        parent.amendLog("Generating terrain using " + toString());
        
        if(preview) {
            size = 128;
        }
        else {
            size = Integer.parseInt((String) cmbSize.getSelectedItem());
        }
    
        //amount to raise a slice of terrain.
        float heightVarience;
        //random points for the fault line
        
        //line directions
        int directionX1, directionZ1;
        int directionX2, directionZ2;

        //allocate new arrays
        int[] heightData = new int[size*size];
        int[][] heightmap = new int[size][size];
        
        // FUEAIODFJKAIO
        int iterations = (int)sldIterations.getValue();
        int maxDelta = (int) sldMaxDelta.getValue();
        int minDelta = (int) sldMinDelta.getValue();
        
        parent.setProgressBar(iterations);
        
        try{
        //generate faults for the number of iterations given.
        for (int i = 0; i < iterations; i++) {
            heightVarience = maxDelta - ((maxDelta - minDelta) * i) / iterations;

            //find two different random points.
            randomX1 = (int) (random() * size);
            randomZ1 = (int) (random() * size);
            
            do {
               randomX2 = (int) (random() * size);
               randomZ2 = (int) (random() * size);
            } while (randomX1 == randomX2 && randomZ1 == randomZ2);

            //calculate the direction of the line the two points create.
            directionX1 = randomX2 - randomX1;
            directionZ1 = randomZ2 - randomZ1;

            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    //calculate the direction of the line from the first
                    //point to where we currently are.
                    directionX2 = x - randomX1;
                    directionZ2 = z - randomZ1;

                    //If the direction between the two directions is positive,
                    //the current point is above the line and should be
                    //increased by the height varient.
                    if ((directionX2 * directionZ1 - directionX1 * directionZ2) > 0) {
                        heightmap[x][z] += heightVarience;
                    }
                }
            }
            
            if(!preview)
                parent.increaseProgressBar();
        }
        
        parent.resetProgressBar();
        
    }catch(Exception e){}
        
        return heightmap;
    }
    private double random() {
        return randomizer.nextDouble();
    }
    
    /**
     * Returns a string representing this class, this is displayed in
     * the fractal method combo box
     * 
     * @return String The string representing this class
     */
    public String toString()
    {
        return "Fault Fractal";
    }
    
    public void setPreview(boolean preview)
    {
        this.preview = preview;
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
    
    /**
     * Called when an change event occurs, such as the slider components
     * 
     * @param e     The change event generated
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sldIterations)) {
            txtIterations.setText(Integer.toString((int)sldIterations.getValue()));
            if(preview) {
                parent.refreshMiniView(generateTerrain(true));
            }
        }
        else if (e.getSource().equals(sldMinDelta)) {
            txtMinDelta.setText(Integer.toString(sldMinDelta.getValue()));
            if(preview) {
                parent.refreshMiniView(generateTerrain(true));
            }
        }
        else if (e.getSource().equals(sldMaxDelta)) {
            txtMaxDelta.setText(Integer.toString(sldMaxDelta.getValue()));
            if(preview) {
                parent.refreshMiniView(generateTerrain(true));
            }
        }
    }
}

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;
import Interfaces.*;

/**
 * Pure random heightmap generation
 * (Generally used for debugging/combination/TAM purposes)
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class ALGGauss implements IAlgorithm, ActionListener, ChangeListener
{
    private ICanvasAlg parent;
    private JPanel panel;
    private JButton btnGenerate;
    private int width;
    private JComboBox cmbSize;
    private boolean preview = true;
    private JSlider sldC;
    private JTextField txtC;
    
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
        panel.setLayout(new BorderLayout());
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblC = new JLabel("C value: ");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 15;
        controlPanel.add(lblC, c);
        
        sldC = new JSlider(0, 500, 250);
        sldC.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        controlPanel.add(sldC, c);
        
        txtC = new JTextField(Integer.toString((int)sldC.getValue()));
        txtC.setEditable(false);
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 25;
        controlPanel.add(txtC, c);
        
        panel.add(controlPanel, BorderLayout.CENTER);
        
        JPanel sizePanel = new JPanel();
        sizePanel.setLayout(new FlowLayout());

        JLabel lblSize = new JLabel("Terrain Size: ");
        sizePanel.add(lblSize);
        
        cmbSize = new JComboBox();
        for(int i = 7; i < 14; i++) {
            cmbSize.addItem(Integer.toString((int) Math.pow(2, i) + 1));
        }
        cmbSize.setSelectedIndex(1); // Select 257 as width by default
        sizePanel.add(cmbSize);
        
        panel.add(sizePanel, BorderLayout.PAGE_START);
        
        btnGenerate = new JButton("Generate Gaussian");
        btnGenerate.addActionListener(this);
        panel.add(btnGenerate, BorderLayout.PAGE_END);
        
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
        int mult = (int)sldC.getValue();
        if(preview)
            mult = (int) ((float)mult * (128 / Float.parseFloat((String) cmbSize.getSelectedItem())));
        
        for(int i = 0; i < heightmap.length; i++) {
            for(int j = 0; j < heightmap[i].length; j++) {
                int x = i - (heightmap.length / 2);
                int y = j - (heightmap.length / 2);
                    //mult *= 128 / Integer.parseInt((String) cmbSize.getSelectedItem());
                double z = Math.pow(Math.E, -((double)Math.sqrt((x*x + y*y)))/(2*mult));
                heightmap[i][j] = (int) (z * 1000000);
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
        return "Gaussian Heightmap";
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
        if (e.getSource().equals(sldC)) {
            txtC.setText(Double.toString((double)sldC.getValue()));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
    }
}

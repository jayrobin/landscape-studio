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
public class ALGDeposition implements IAlgorithm, ActionListener, ChangeListener
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
    private JSlider sldPeakWalk, sldJumps, sldCaldera, sldMinParticles, sldMaxParticles;
    private JTextField txtJumps, txtPeakWalk, txtCaldera, txtMinParticles, txtMaxParticles;
    private int size;
    private JComboBox cmbSize;
    private int iterations;
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
        this.preview  = preview;
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
        
        JLabel lblJumps = new JLabel("Jumps: ");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 15;
        controlPanel.add(lblJumps, c);
        
        sldJumps = new JSlider(0, 3000, 10);   // Divide by 100
        sldJumps.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        controlPanel.add(sldJumps, c);
        
        txtJumps = new JTextField(Integer.toString(sldJumps.getValue()));
        txtJumps.setEditable(false);
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 25;
        controlPanel.add(txtJumps, c);
        
        JLabel lblPeakWalk = new JLabel("Peak Walk: ");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(lblPeakWalk, c);
        
        sldPeakWalk = new JSlider(0, 1000, 10);   // No divide
        sldPeakWalk.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(sldPeakWalk, c);
        
        txtPeakWalk = new JTextField(Double.toString(sldPeakWalk.getValue()));
        txtPeakWalk.setEditable(false);
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(txtPeakWalk, c);
        
        JLabel lblCaldera = new JLabel("Caldera: ");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(lblCaldera, c);
        
        sldCaldera = new JSlider(0, 1000, 10);   // Divide by 10
        sldCaldera.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(sldCaldera, c);
        
        txtCaldera = new JTextField(Double.toString((double)sldCaldera.getValue() / 1000));
        txtCaldera.setEditable(false);
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(txtCaldera, c);
        
        JLabel lblMinParticles = new JLabel("Min Particles: ");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        controlPanel.add(lblMinParticles, c);
        
        sldMinParticles = new JSlider(0, 1000, 100);   // Divide by 10
        sldMinParticles.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        controlPanel.add(sldMinParticles, c);
        
        txtMinParticles = new JTextField(Integer.toString(sldMinParticles.getValue()));
        txtMinParticles.setEditable(false);
        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        controlPanel.add(txtMinParticles, c);
        
        JLabel lblMaxParticles = new JLabel("Max Particles: ");
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        controlPanel.add(lblMaxParticles, c);
        
        sldMaxParticles = new JSlider(0, 1000, 1000);
        sldMaxParticles.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        controlPanel.add(sldMaxParticles, c);
        
        txtMaxParticles = new JTextField(Integer.toString(sldMaxParticles.getValue()));
        txtMaxParticles.setEditable(false);
        c.gridx = 2;
        c.gridy = 4;
        c.gridwidth = 1;
        controlPanel.add(txtMaxParticles, c);
        
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
        
        btnGenerate = new JButton("Generate Deposition Fractal");
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
        
        size = Integer.parseInt((String) cmbSize.getSelectedItem());

        //allocate new arrays
        int[] heightData = new int[size*size];
        float[][] tempBuffer = new float[size][size];
        int[][] heightmap = new int[size][size];
        
        int jumps = (int) sldJumps.getValue();
        int peakWalk = (int) sldPeakWalk.getValue();
        float caldera = (float) sldCaldera.getValue() / 1000;
        int maxParticles = (int) sldMinParticles.getValue();
        int minParticles = (int) sldMaxParticles.getValue();
        
        int x, y;
        int calderaX, calderaY;
        int sx, sy;
        int tx, ty;
        int m;
        float calderaStartPoint;
        float cutoff;
        int dx[] = { 0, 1, 0, size - 1, 1, 1, size - 1, size - 1 };
        int dy[] = { 1, 0, size - 1, 0, size - 1, 1, size - 1, 1 };
        //map 0 unmarked, unvisited, 1 marked, unvisited, 2 marked visited.
        int[][] calderaMap = new int[size][size];
        boolean done;

        int minx, maxx;
        int miny, maxy;
        
        parent.setProgressBar(jumps);
        
        try{
        //create peaks.
        for (int i = 0; i < jumps; i++) {

            //pick a random point.
            x = (int) (Math.rint(Math.random() * (size - 1)));
            y = (int) (Math.rint(Math.random() * (size - 1)));

            //set the caldera point.
            calderaX = x;
            calderaY = y;

            int numberParticles =
                (int) (Math
                    .rint(
                        (Math.random() * (maxParticles - minParticles))
                            + minParticles));
            //drop particles.
            for (int j = 0; j < numberParticles; j++) {
                //check to see if we should aggitate the drop point.
                if (peakWalk != 0 && j % peakWalk == 0) {
                    m = (int) (Math.rint(Math.random() * 7));
                    x = (x + dx[m] + size) % size;
                    y = (y + dy[m] + size) % size;
                }

                //add the particle to the piont.
                tempBuffer[x][y] += 1;

                sx = x;
                sy = y;
                done = false;

                //cause the particle to "slide" down the slope and settle at
                //a low point.
                while (!done) {
                    done = true;

                    //check neighbors to see if we are higher.
                    m = (int) (Math.rint((Math.random() * 8)));
                    for (int jj = 0; jj < 8; jj++) {
                        tx = (sx + dx[(jj + m) % 8]) % (size);
                        ty = (sy + dy[(jj + m) % 8]) % (size);

                        //move to the neighbor.
                        if (tempBuffer[tx][ty] + 1.0f < tempBuffer[sx][sy]) {
                            tempBuffer[tx][ty] += 1.0f;
                            tempBuffer[sx][sy] -= 1.0f;
                            sx = tx;
                            sy = ty;
                            done = false;
                            break;
                        }
                    }
                }

                //This point is higher than the current caldera point,
                //so move the caldera here.
                if (tempBuffer[sx][sy] > tempBuffer[calderaX][calderaY]) {
                    calderaX = sx;
                    calderaY = sy;
                }
            }

            //apply the caldera.
            calderaStartPoint = tempBuffer[calderaX][calderaY];
            cutoff = calderaStartPoint * (1.0f - caldera);
            minx = calderaX;
            maxx = calderaX;
            miny = calderaY;
            maxy = calderaY;

            calderaMap[calderaX][calderaY] = 1;

            done = false;
            while (!done) {
                done = true;
                sx = minx;
                sy = miny;
                tx = maxx;
                ty = maxy;

                for (x = sx; x <= tx; x++) {
                    for (y = sy; y <= ty; y++) {

                        calderaX = (x + size) % size;
                        calderaY = (y + size) % size;

                        if (calderaMap[calderaX][calderaY] == 1) {
                            calderaMap[calderaX][calderaY] = 2;

                            if (tempBuffer[calderaX][calderaY] > cutoff
                                && tempBuffer[calderaX][calderaY]
                                    <= calderaStartPoint) {

                                done = false;
                                tempBuffer[calderaX][calderaY] =
                                    2 * cutoff - tempBuffer[calderaX][calderaY];

                                //check the left and right neighbors
                                calderaX = (calderaX + 1) % size;
                                if (calderaMap[calderaX][calderaY] == 0) {
                                    if (x + 1 > maxx) {
                                        maxx = x + 1;
                                    }
                                    calderaMap[calderaX][calderaY] = 1;
                                }

                                calderaX = (calderaX + size - 2) % size;
                                if (calderaMap[calderaX][calderaY] == 0) {
                                    if (x - 1 < minx)
                                        minx = x - 1;
                                    calderaMap[calderaX][calderaY] = 1;
                                }

                                //check the upper and lower neighbors.
                                calderaX = (x + size) % size;
                                calderaY = (calderaY + 1) % size;
                                if (calderaMap[calderaX][calderaY] == 0) {
                                    if (y + 1 > maxy)
                                        maxy = y + 1;
                                    calderaMap[calderaX][calderaY] = 1;
                                }
                                calderaY = (calderaY + size - 2) % size;
                                if (calderaMap[calderaX][calderaY] == 0) {
                                    if (y - 1 < miny)
                                        miny = y - 1;
                                    calderaMap[calderaX][calderaY] = 1;
                                }
                            }
                        }
                    }
                }
            }
            
            if(!preview)
                parent.increaseProgressBar();
        }
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                heightmap[i][j] = (int) tempBuffer[i][j];
            }
        }
        
        parent.resetProgressBar();
        
    }catch(Exception e){System.out.println(e);}
        
        return heightmap;
    }
    
    private Random randomizer;

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
        return "Deposition Fractal";
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
        if (e.getSource().equals(sldJumps)) {
            txtJumps.setText(Integer.toString((int)sldJumps.getValue()));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
        else if (e.getSource().equals(sldPeakWalk)) {
            txtPeakWalk.setText(Integer.toString((int)sldPeakWalk.getValue()));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
        else if (e.getSource().equals(sldCaldera)) {
            txtCaldera.setText(Double.toString((double)sldCaldera.getValue() / 1000));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
        else if (e.getSource().equals(sldMinParticles)) {
            txtMinParticles.setText(Integer.toString((int)sldMinParticles.getValue()));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
        else if (e.getSource().equals(sldMaxParticles)) {
            txtMaxParticles.setText(Integer.toString((int)sldMaxParticles.getValue()));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
    }
}

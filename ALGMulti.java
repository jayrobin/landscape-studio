import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import Interfaces.*;

/**
 * Prodedural Multi-Fractal algorithm
 * Original code by Carl Burke, adapted by James Robinson
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class ALGMulti implements IAlgorithm, ActionListener, ChangeListener
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
    private JSlider sldOctaves, sldZ, sldLacunarity, sldOffset;
    private JTextField txtZ, txtOctaves, txtLacunarity, txtOffset;
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
        panel.setLayout(new BorderLayout());
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblZ = new JLabel("Z value: ");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 15;
        controlPanel.add(lblZ, c);
        
        sldZ = new JSlider(0, 1000, 50);   // Divide by 100
        sldZ.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        controlPanel.add(sldZ, c);
        
        txtZ = new JTextField(Double.toString((double)sldZ.getValue() / 10));
        txtZ.setEditable(false);
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 25;
        controlPanel.add(txtZ, c);
        
        JLabel lblOctaves = new JLabel("Octaves: ");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(lblOctaves, c);
        
        sldOctaves = new JSlider(1, 8, 2);   // No divide
        sldOctaves.setMajorTickSpacing(1);
        sldOctaves.setSnapToTicks(true);
        sldOctaves.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(sldOctaves, c);
        
        txtOctaves = new JTextField(Double.toString(sldOctaves.getValue()));
        txtOctaves.setEditable(false);
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        controlPanel.add(txtOctaves, c);
        
        JLabel lblLacunarity = new JLabel("Lacunarity: ");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(lblLacunarity, c);
        
        sldLacunarity = new JSlider(0, 50, 20);   // Divide by 10
        sldLacunarity.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(sldLacunarity, c);
        
        txtLacunarity = new JTextField(Double.toString((double)sldLacunarity.getValue() / 10));
        txtLacunarity.setEditable(false);
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        controlPanel.add(txtLacunarity, c);
        
        JLabel lblOffset = new JLabel("Offset: ");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        controlPanel.add(lblOffset, c);
        
        sldOffset = new JSlider(0, 100, 50);   // Divide by 100
        sldOffset.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        controlPanel.add(sldOffset, c);
        
        txtOffset = new JTextField(Double.toString((double)sldOffset.getValue() / 100));
        txtOffset.setEditable(false);
        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        controlPanel.add(txtOffset, c);
        
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
        
        btnGenerate = new JButton("Generate Multifractal");
        btnGenerate.addActionListener(this);
        panel.add(btnGenerate, BorderLayout.PAGE_END);
        
        randomise();
        
        parent.refreshMiniView(generateTerrain(true));
    }
    
    public void randomise()
    {
        rgen = new Random();
        init_noise();
        parent.refreshMiniView(generateTerrain(true));
    }
    
    /*
     * Initialises the noise
     * 
     * @author C. Burke
     */
    private void init_noise()
    {
        int i, j, k;
    
        p = new int[B + B + 2];
        g3 = new double[B + B + 2][3];
    
        for (i = 0 ; i < B ; i++)
        {
            p[i] = i;
    
            for (j = 0 ; j < 3 ; j++)
            g3[i][j] = rgen.nextDouble() * 2.0 - 1.0;  // -1.0 to 1.0
            normalize3(g3[i]);
        }
    
        while ((--i) > 0)
        {
            j = (int)(rgen.nextDouble() * B);
            k = p[i];
            p[i] = p[j];
            p[j] = k;
        }
    
        for (i = 0 ; i < B + 2 ; i++)
        {
            p[B + i] = p[i];
            for (j = 0 ; j < 3 ; j++)
            g3[B + i][j] = g3[i][j];
        }
    }
    
    /*
     * Normalises a two dimensional point
     * 
     * @author C. Burke
     */
    private void normalize2(double v[]) // v.length == 2
    {
        double s;
    
        s = Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        v[0] = v[0] / s;
        v[1] = v[1] / s;
    }
    
    /*
     * Normalises a three dimensional point
     * 
     * @author C. Burke
     */
    private void normalize3(double v[]) // v.length == 3
    {
        double s;
    
        s = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] = v[0] / s;
        v[1] = v[1] / s;
        v[2] = v[2] / s;
    }
    
    /*
     * Normalises a two dimensional double array to a specified
     * value
     */
    private double[][] normaliseMap(double[][] map, int max)
    {
        double minAlg = map[0][0];
        double maxAlg = map[0][0];
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
               if(map[i][j] < minAlg) minAlg = map[i][j];
               if(map[i][j] > maxAlg) maxAlg = map[i][j];
            }
        }
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
                map[i][j] -= minAlg;
                map[i][j] = (((double) map[i][j] / (maxAlg - minAlg)) * max);
            }
        }
        
        return map;
    }
    
    /*
     * Returns a two dimensional integer array representing the heightmap
     * 
     * @return int[][] The RMF heightmap
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
        
        double[][] heightmap = new double[width][width];
        double point[] = new double[3];
        double z = (double)sldZ.getValue() / 10;
        double h = (double)0.5;
        double lacunarity = (double)sldLacunarity.getValue() / 10;
        double octaves = (int) sldOctaves.getValue();
        double offset = (double) sldOffset.getValue() / 100;
        
        for (int i = 0; i < heightmap.length; i++) {
            for (int j = 0; j < heightmap[i].length; j++) {
                point[0] = ((double)i) / ((double)width) + 1.0;
                point[1] = ((double)j) / ((double)width) + 1.0;
                point[2] = z;
                heightmap[i][j] = (multifractal(point, h, lacunarity, octaves, offset));
            }
            
            if(!preview)
                parent.increaseProgressBar();
        }
        
        heightmap = normaliseMap(heightmap, 1000);
        
        int[][] map = new int[heightmap.length][heightmap.length];
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                map[i][j] = (int) heightmap[i][j];
            }
        }
        
        parent.resetProgressBar();
        
        return map;
    }
    
    /*
     * Returns the height value of a specified point calculated using specified
     * parameters
     * 
     * @author C. Burke
     * 
     * @param point         The point to calculate
     * @param H             The H value
     * @param lacunarity    The lacunarity value
     * @param octaves       The number of octaves
     * @param offset        The offset value
     * 
     * @return double       The height value of the specified point
     */
    private double multifractal(double point[], double H, double lacunarity, double octaves, double offset )
    {
        double            value, frequency, remainder;
        int               i;
    
        /* precompute and store spectral weights */
        if ( first_fBm )
        {
            /* seize required memory for exponent_array */
            exponent_array = new double[(int)octaves+1];
            frequency = 1.0;
            for (i=0; i <= octaves; i++)
            {
              /* compute weight for each frequency */
              exponent_array[i] = Math.pow( frequency, -H );
              frequency *= lacunarity;
            }
            first_fBm = false;
        }
        
        value = 1.0;            /* initialize vars to proper values */
        frequency = 1.0;
        
        /* inner loop of multifractal construction */
        for (i=0; i < octaves; i++)
        {
            value *= offset * frequency * noise3( point );
            point[0] *= lacunarity;
            point[1] *= lacunarity;
            point[2] *= lacunarity;
        }
        
        remainder = octaves - (int)octaves;
        if ( remainder != 0.0 )
        {
            /* add in ``octaves''  remainder */
            /* ``i''  and spatial freq. are preset in loop above */
            value += remainder * noise3( point ) * exponent_array[i];
        }
        
        return value;
    }
    
    /*
     * 3D noise
     * 
     * @author C. Burke
     */
    private double noise3(double vec[])  // vec.length == 3
    {
        int bx0, bx1, by0, by1, bz0, bz1, b00, b10, b01, b11;
        double rx0, rx1, ry0, ry1, rz0, rz1, q[], sy, sz, a, b, c, d, t, u, v;
        int i, j;
    
        /* setup(0, bx0,bx1, rx0,rx1) */
        t = vec[0] + N;
        bx0 = ((int)t) & BM;
        bx1 = (bx0+1) & BM;
        rx0 = t - (int)t;
        rx1 = rx0 - 1.;
        /***/
        /* setup(1, by0,by1, ry0,ry1) */
        t = vec[1] + N;
        by0 = ((int)t) & BM;
        by1 = (by0+1) & BM;
        ry0 = t - (int)t;
        ry1 = ry0 - 1.;
        /***/
        /* setup(2, bz0,bz1, rz0,rz1) */
        t = vec[2] + N;
        bz0 = ((int)t) & BM;
        bz1 = (bz0+1) & BM;
        rz0 = t - (int)t;
        rz1 = rz0 - 1.;
        /***/
    
        i = p[ bx0 ];
        j = p[ bx1 ];
    
        b00 = p[ i + by0 ];
        b10 = p[ j + by0 ];
        b01 = p[ i + by1 ];
        b11 = p[ j + by1 ];
    
        t  = s_curve(rx0);
        sy = s_curve(ry0);
        sz = s_curve(rz0);
        
        q = g3[ b00 + bz0 ] ; u = ( rx0 * q[0] + ry0 * q[1] + rz0 * q[2] );
        q = g3[ b10 + bz0 ] ; v = ( rx1 * q[0] + ry0 * q[1] + rz0 * q[2] );
        a = lerp(t, u, v);
    
        q = g3[ b01 + bz0 ] ; u = ( rx0 * q[0] + ry1 * q[1] + rz0 * q[2] );
        q = g3[ b11 + bz0 ] ; v = ( rx1 * q[0] + ry1 * q[1] + rz0 * q[2] );
        b = lerp(t, u, v);
    
        c = lerp(sy, a, b);
    
        q = g3[ b00 + bz1 ] ; u = ( rx0 * q[0] + ry0 * q[1] + rz1 * q[2] );
        q = g3[ b10 + bz1 ] ; v = ( rx1 * q[0] + ry0 * q[1] + rz1 * q[2] );
        a = lerp(t, u, v);
    
        q = g3[ b01 + bz1 ] ; u = ( rx0 * q[0] + ry1 * q[1] + rz1 * q[2] );
        q = g3[ b11 + bz1 ] ; v = ( rx1 * q[0] + ry1 * q[1] + rz1 * q[2] );
        b = lerp(t, u, v);
    
        d = lerp(sy, a, b);
    
        return lerp(sz, c, d);
    }
    
    /*
     * S_curve
     * 
     * @author C. Burke
     */
    private double s_curve(double t)
    {
        return t * t * (3. - 2. * t);
    }
    
    /*
     * Lerp
     * 
     * @author C. Burke
     */
    private double lerp(double t, double a, double b)
    {
        return a + t * (b - a);
    }
    
    /**
     * Returns a string representing this class, this is displayed in
     * the fractal method combo box
     * 
     * @return String The string representing this class
     */
    public String toString()
    {
        return "Procedural Multi-Fractal";
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
        if (e.getSource().equals(sldZ)) {
            txtZ.setText(Double.toString((double)sldZ.getValue() / 100));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
        else if (e.getSource().equals(sldOctaves)) {
            txtOctaves.setText(Double.toString((double)sldOctaves.getValue()));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
        else if (e.getSource().equals(sldLacunarity)) {
            txtLacunarity.setText(Double.toString((double)sldLacunarity.getValue() / 10));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
        else if (e.getSource().equals(sldOffset)) {
            txtOffset.setText(Double.toString((double)sldOffset.getValue() / 100));
            if(preview)
                parent.refreshMiniView(generateTerrain(true));
        }
    }

}

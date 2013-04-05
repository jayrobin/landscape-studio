import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.io.*;
import Interfaces.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * JInternalFrame containing controls allowing the combination
 * of heightmaps using various methods including add, subtract,
 * multiply, min, max, and, or and xor
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class CombineFrame extends JInternalFrame implements ChangeListener
{
    private TabbedCanvas parent;
    private CombineListener cListener;
    private JComboBox cmbMethod;
    private JButton btnCombine, btnTMOpen;
    private Vector importers;
    private JTextField txtTerrainMap;
    private JSlider sldStrength;
    private ViewPanel view;
    private JCheckBox chkPreview;
    private Image bufferedImage, scalableImage;

    /**
     * Constructor for objects of class CombineFrame
     */
    public CombineFrame(TabbedCanvas parent, Vector importers)
    {
        super(Constants.COMBINEFRAME_NAME, false, true, false, false);
        //this.setSize(new Dimension(400, 250));
        this.parent = parent;
        this.getContentPane().setLayout(new BorderLayout());
        this.setVisible(true);
        this.importers = importers;
        init();
    }
    
    public void reset()
    {
        bufferedImage = null;
        scalableImage = null;
    }
    
    /*
     * Initialisations for FiltersFrame, for example setting up container
     * panels and placing components
     */
    private void init()
    {
        cListener = new CombineListener();
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel label = new JLabel("Terrain Map: ");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        panel.add(label, c);
        
        txtTerrainMap = new JTextField();
        txtTerrainMap.setEditable(false);
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 150;
        c.gridwidth = 2;
        panel.add(txtTerrainMap, c);
        
        btnTMOpen = new JButton("...");
        btnTMOpen.addActionListener(cListener);
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        c.ipadx = 0;
        panel.add(btnTMOpen, c);
        
        label = new JLabel("Method: ");
        c.gridx = 0;
        c.gridy = 1;
        panel.add(label, c);
        
        cmbMethod = new JComboBox();
        cmbMethod.addItem("Add");
        cmbMethod.addItem("Subtract");
        cmbMethod.addItem("Multiply");
        cmbMethod.addItem("Max");
        cmbMethod.addItem("Min");
        cmbMethod.addItem("And");
        cmbMethod.addItem("Or");
        cmbMethod.addItem("Xor");
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 3;
        panel.add(cmbMethod, c);
        
        JLabel lblStrength = new JLabel("Strength: ");
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(lblStrength, c);
        
        sldStrength = new JSlider(0, 200, 100);
        sldStrength.setMajorTickSpacing(50);
        sldStrength.setMinorTickSpacing(10);
        sldStrength.setPaintTicks(true);
        sldStrength.setPaintLabels(true);
        sldStrength.addChangeListener(this);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 3;
        panel.add(sldStrength, c);
        
        btnCombine = new JButton("Combine");
        btnCombine.addActionListener(cListener);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        panel.add(btnCombine, c);
        
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        view = new ViewPanel();
        view.setPreferredSize(new Dimension(128, 128));
        chkPreview = new JCheckBox("Preview");
        chkPreview.setSelected(true);
        viewPanel.add(view, BorderLayout.CENTER);
        viewPanel.add(chkPreview, BorderLayout.PAGE_END);
        
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(viewPanel, BorderLayout.LINE_START);
        this.setSize(new Dimension(this.getWidth() + 10, this.getHeight() + 10));
        parent.setCombineMap(parent.getHeightMap());
        this.revalidate();
        this.pack();
        
        parent.setTerrainMap(null);
    }
    
    /*
     * Performs file loading for the purpose of loading height maps
     * 
     * @return int[][]  A two dimensional integer array, the loaded heightmap
     */
    private int[][] loadFile()
    {
        int[][] heightmap = null;
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir") + "\\");
        
        for(int i = 0; i < importers.size(); i++) {
            IImporter imp = (IImporter) importers.get(i);
            fc.addChoosableFileFilter(new Filter(imp.toString()));
        }
        
        File file = null;
        int returnVal = fc.showOpenDialog(this);
        
        if (returnVal == JFileChooser.CANCEL_OPTION) {
          return null;
        }
        else if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            
            if(file == null) {
                System.out.println("NULL");
                return null;
            }
            
            String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
            
            for(int j = 0; j < importers.size(); j++) {
                IImporter imp = (IImporter) importers.get(j);
                if(imp.toString().equals(extension)) {
                    heightmap = imp.loadFile(file);
                    
                    // check for null heightmap
                    if(heightmap == null)
                        return null;
                        
                    j = importers.size();
                }
            }
            
            txtTerrainMap.setText(file.getName());
            
            return heightmap;
            
        }
        
        return null;
    }
    
    /**
     * Internal 2D view panel, displays the top down view of the heightmap
     */
    class ViewPanel extends JPanel
    {
        /**
         * Paints the 2D view
         * 
         * @param g     Graphics object for rendering
         */
        public void paint(Graphics g)
        {
            if(chkPreview.isSelected()) {
                if(bufferedImage == null) {
                    bufferImageGrey();
                    revalidate();
                    //pack();
                }
                
                if(scalableImage == null) {
                    scalableImage = bufferedImage.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
                }
                
                g.drawImage(scalableImage, 0, 0, this);
            }
        }
        
        /*
         * Buffer the image using the greyscale renderer
         */
        private void bufferImageGrey()
        {
            int col = 0;
            int[][] heightmap = parent.normaliseMap(parent.getPreviewMap(), 255);
            
            bufferedImage = createImage(heightmap.length, heightmap[0].length);
            Graphics bg = bufferedImage.getGraphics();
            for(int i = 0; i < heightmap.length; i ++) {
                for(int j = 0; j < heightmap[i].length - 1; j++) {
                    col = heightmap[i][j];
                    if(col > 255)
                        col = 255;
                    bg.setColor(new Color(col, col, col));
                    bg.drawLine(i, j, i, j);
                }
            }
        }
    }
    
    /**
     * ActionListener for various components of CombineListener
     */
    class CombineListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(btnTMOpen)) {
                int[][] heightmap = loadFile();
                
                if(heightmap != null)
                    parent.setTerrainMap(heightmap);
            }
            else if(e.getSource().equals(btnCombine)) {
                if(parent.getTerrainMap() != null) {
                    int[][] heightmap = parent.combineMaps((String) cmbMethod.getSelectedItem(), 
                        (int) sldStrength.getValue(), parent.getHeightMap(), parent.getTerrainMap(), false);
                    
                    if(heightmap == null) {
                        JOptionPane.showMessageDialog (null, "The selected heightmap has different dimensions, combination could not be performed");
                    }
                    else {
                        parent.setHeightMap(heightmap);
                    }
                }
            }
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(sldStrength)) {
            if(chkPreview.isSelected() && parent.getTerrainMap() != null) {
                parent.setPreviewMap(parent.getHeightMap(), 128);
                parent.refreshMiniView(parent.combineMaps((String) cmbMethod.getSelectedItem(), 
                                                                (int) sldStrength.getValue(), 
                                                                      parent.getPreviewMap(), 
                                                                      parent.getTerrainMap(), true));
            }
        }
    }
}

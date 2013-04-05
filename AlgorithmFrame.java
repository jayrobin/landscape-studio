import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.io.*;
import Interfaces.*;

/**
 * Algorithms are first loaded into this frame (in a drop down box), allowing 
 * the user to choose a suitable generation algorithm. Options are also 
 * available for including terrain and application maps
 * 
 * Features:
 * -Algorithm selector
 * -Terrain Application Mapping
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class AlgorithmFrame extends JInternalFrame
{
    private TabbedCanvas parent;
    private Vector algorithms;
    private Vector importers;
    private AlgorithmListener aListener;
    private IAlgorithm algorithm;
    private JPanel algPanel, 
                   tamPanel;
    private JButton btnTMOpen, btnAMOpen, btnRandomise;
    private JComboBox cmbAlgorithms;
    private JTextField txtTerrainMap, txtApplicationMap;
    private JCheckBox chkTAM, chkPreview;
    private ViewPanel view;
    private Image bufferedImage, scalableImage;

    /**
     * Constructor for objects of class AlgorithmPanel
     */
    public AlgorithmFrame(TabbedCanvas parent, Vector algorithms, Vector importers)
    {
        super(Constants.ALGORITHMFRAME_NAME, false, true, false, false);
        this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.parent = parent;
        this.algorithms = algorithms;
        this.importers = importers;
        this.getContentPane().setLayout(new BorderLayout());
        this.setVisible(true);        
        init();
    }
    
    public void resetImages()
    {
        bufferedImage = null;
        scalableImage = null;
    }
    
    /*
     * Initialisations for AlgorithmPanel, for example setting up container
     * panels and placing components
     */
    private void init()
    {
        // reset terrain and application maps
        parent.setTerrainMap(null);
        parent.setApplicationMap(null);
        
        chkPreview = new JCheckBox("Preview");
        chkPreview.setSelected(true);
        
        aListener = new AlgorithmListener();
        
        cmbAlgorithms = new JComboBox();
        cmbAlgorithms.addActionListener(aListener);
        
        tamPanel = new JPanel();
        tamPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        chkTAM = new JCheckBox("Use Terrain-Application mapping");
        chkTAM.setSelected(false);
        chkTAM.addActionListener(aListener);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        tamPanel.add(chkTAM, c);
        
        JLabel label = new JLabel("Terrain Map: ");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        tamPanel.add(label, c);
        
        txtTerrainMap = new JTextField();
        txtTerrainMap.setEditable(false);
        txtTerrainMap.setEnabled(false);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        tamPanel.add(txtTerrainMap, c);
        
        btnTMOpen = new JButton("...");
        btnTMOpen.setEnabled(false);
        btnTMOpen.addActionListener(aListener);
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        tamPanel.add(btnTMOpen, c);
        
        label = new JLabel("Application Map: ");
        c.gridx = 0;
        c.gridy = 2;
        tamPanel.add(label, c);
        
        txtApplicationMap = new JTextField();
        txtApplicationMap.setEditable(false);
        txtApplicationMap.setEnabled(false);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        tamPanel.add(txtApplicationMap, c);
        
        btnAMOpen = new JButton("...");
        btnAMOpen.setEnabled(false);
        btnAMOpen.addActionListener(aListener);
        c.gridx = 3;
        c.gridy = 2;
        c.gridwidth = 1;
        tamPanel.add(btnAMOpen, c);
        
        for(int i = 0; i < algorithms.size(); i++) {
            cmbAlgorithms.addItem(algorithms.get(i).toString());
        }
        
        JPanel mainPanel = new JPanel(); // Central panel holding size panel and TAM panel
        JPanel subPanel = new JPanel();
        JPanel viewPanel = new JPanel();
        subPanel.setLayout(new BorderLayout());
        mainPanel.setLayout(new FlowLayout());
        viewPanel.setLayout(new BorderLayout());
        
        view = new ViewPanel();
        view.setPreferredSize(new Dimension(128, 128));
        
        chkPreview.setSelected(true);
        chkPreview.addActionListener(aListener);
        
        subPanel.add(cmbAlgorithms, BorderLayout.PAGE_START);
        subPanel.add(tamPanel, BorderLayout.CENTER);
        subPanel.add(chkPreview, BorderLayout.PAGE_END);
        
        btnRandomise = new JButton("Randomise");
        btnRandomise.addActionListener(aListener);
        
        viewPanel.add(view, BorderLayout.CENTER);
        viewPanel.add(btnRandomise, BorderLayout.PAGE_END);
        
        mainPanel.add(viewPanel);
        mainPanel.add(subPanel);
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(algPanel, BorderLayout.PAGE_END);
        
        super.pack();
        this.setSize(new Dimension(this.getWidth() + 10, this.getHeight() + 10));
        
        parent.setTerrainMap(null);
        parent.setApplicationMap(null);
    }
    
    /*
     * Performs file loading for the purpose of loading terrain and
     * application maps
     * 
     * @param mapType   The type of map being loaded (Terrain/Application)
     * 
     * @return int[][]  A two dimensional integer array, the loaded heightmap
     */
    private int[][] loadFile(String mapType)
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
            
            if(mapType.equals("terrain")) {
                txtTerrainMap.setText(file.getName());
            }
            else if(mapType.equals("application")) {
                txtApplicationMap.setText(file.getName());
            }
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
     * ActionListener for various components of AlgorithmPanel
     */
    class AlgorithmListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(cmbAlgorithms)) {
                int index = cmbAlgorithms.getSelectedIndex();
                algorithm = (IAlgorithm) algorithms.get(index);
                algorithm.setPreview(chkPreview.isSelected());
                
                if(algPanel != null)
                    remove(algPanel);
                    
                algPanel = new JPanel();
                
                algorithm.setPanel(algPanel);
                getContentPane().add(algPanel, BorderLayout.PAGE_END);
                algPanel.setBorder(BorderFactory.createLineBorder(Color.black));
                revalidate();
                repaint();
                pack();
                setSize(new Dimension(getWidth() + 10, getHeight() + 10));
            }
            else if(e.getSource().equals(chkTAM)) {
                if(chkTAM.isSelected()) {
                    txtTerrainMap.setEnabled(true);
                    btnTMOpen.setEnabled(true);
                    txtApplicationMap.setEnabled(true);
                    btnAMOpen.setEnabled(true);
                }
                else {
                    // reset terrain and application maps
                    parent.setTerrainMap(null);
                    parent.setApplicationMap(null);
                    
                    txtTerrainMap.setEnabled(false);
                    txtTerrainMap.setText("");
                    btnTMOpen.setEnabled(false);
                    txtApplicationMap.setEnabled(false);
                    txtApplicationMap.setText("");
                    btnAMOpen.setEnabled(false);
                }
            }
            else if(e.getSource().equals(btnTMOpen)) {
                int[][] heightmap = loadFile("terrain");
                
                if(heightmap != null)
                    parent.setTerrainMap(heightmap);
            }
            else if(e.getSource().equals(btnAMOpen)) {
                int[][] heightmap = loadFile("application");
                
                if(heightmap != null)
                    parent.setApplicationMap(heightmap);
            }
            else if(e.getSource().equals(btnRandomise)) {
                int index = cmbAlgorithms.getSelectedIndex();
                algorithm = (IAlgorithm) algorithms.get(index);
                algorithm.randomise();
            }
            else if(e.getSource().equals(chkPreview)) {
                algorithm.setPreview(chkPreview.isSelected());
                repaint();
            }
        }
    }
}
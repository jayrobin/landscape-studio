import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * JInternalFrame containing a top down view of the current height map
 * Includes several redering modes
 * 
 * Features:
 * -Colour renderer
 * -Greyscale renderer
 * -Contour renderer
 * -Real renderer
 * 
 * @author James Robinson 
 * @version 0.1
 */
public class View2DFrame extends JInternalFrame
{
    private TabbedCanvas parent;
    private ViewPanel viewPanel;
    private Image bufferedImage, scalableImage;
    private int[][] heightmap;
    private JButton btnRefresh, btnZIn, btnZOut;
    private ViewListener listener;
    private JComboBox cmbView;
    private String viewMethod = "Greyscale";
    private int width, zoom, x, y, dragX, dragY;
    private boolean draggable;
    private boolean repaint = true;
    

    /**
     * Constructor for objects of class View2DFrame
     */
    public View2DFrame(TabbedCanvas parent)
    {
        super(Constants.VIEW2DFRAME_NAME, false, true, false, false);
        this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        this.parent = parent;
        this.getContentPane().setLayout(new BorderLayout());
        this.setVisible(true);
        init();
    }
    
    /*
     * Resets the view
     */
    public void reset()
    {
        repaint = true;
        bufferedImage = null;
        scalableImage = null;
        zoom = parent.constants.view2DZoom;
        x = y = 0;
        this.setSize(new Dimension(zoom + 10, zoom + 70));
    }
    
    /*
     * Initialisations for View2DPanel
     */
    private void init()
    {        
        draggable = false;
        zoom = Constants.VIEW2DZOOM_DEFAULT;
        x = y = 0;
        this.setSize(new Dimension(zoom + 10, zoom + 70));
        
        viewPanel = new ViewPanel();
        viewPanel.setPreferredSize(new Dimension(zoom, zoom));
        this.getContentPane().add(viewPanel, BorderLayout.CENTER);
        
        listener = new ViewListener();
        
        JPanel bottomPanel = new JPanel();

        btnZIn = new JButton("+");
        btnZIn.addActionListener(listener);
        bottomPanel.add(btnZIn);
        
        btnZOut = new JButton("-");
        btnZOut.addActionListener(listener);
        bottomPanel.add(btnZOut);
        
        cmbView = new JComboBox();
        cmbView.addActionListener(listener);
        cmbView.addItem("Greyscale");
        cmbView.addItem("RGB");
        cmbView.addItem("Contour");
        cmbView.addItem("Real");
        bottomPanel.add(cmbView);
        
        this.getContentPane().add(bottomPanel, BorderLayout.PAGE_START);
    }
    
    /*
     * Sets the size of this frame to a specified dimension
     * 
     * @param d     The dimension to be used as the size of this frame
     */
    private void setPanelSize(Dimension d)
    {
        this.setSize(d);
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
            if(repaint == true) {
                if(viewMethod.equals("RGB")) {
                    bufferImageColor();
                }
                else if(viewMethod.equals("Greyscale")) {
                    bufferImageGrey();
                }
                else if(viewMethod.equals("Contour")) {
                    bufferImageContour();
                }
                else if(viewMethod.equals("Real")) {
                    bufferImageReal();
                }
                parent.amendLog("2D renderer switched to " + viewMethod);
            }
            
            if(scalableImage == null) {
                scalableImage = bufferedImage.getScaledInstance(zoom, zoom, Image.SCALE_SMOOTH);
                bufferedImage = null;
                repaint = false;
                System.gc();
            }
            
            parent.resetProgressBar();
            
            g.drawImage(scalableImage, x, y, this);
        }
        
        /*
         * Buffer the image using the greyscale renderer
         */
        private void bufferImageGrey()
        {
            int col = 0;
            heightmap = parent.normaliseMap(parent.getHeightMap(), 255);
            
            parent.setProgressBar(heightmap.length);
            
            bufferedImage = createImage(heightmap.length, heightmap[0].length);
            Graphics bg = bufferedImage.getGraphics();
            for(int i = 0; i < heightmap.length; i++) {
                for(int j = 0; j < heightmap[i].length - 1; j++) {
                    col = heightmap[i][j];
                    if(col > 255)
                        col = 255;
                    bg.setColor(new Color(col, col, col));
                    bg.drawLine(i, j, i, j);
                }
                
                parent.increaseProgressBar();
            }
        }
        
        /*
         * Buffer the image using the contour renderer
         */
        private void bufferImageContour()
        {
            int col = 0;
            heightmap = parent.normaliseMap(parent.getHeightMap(), 505);
            
            parent.setProgressBar(heightmap.length);
            
            bufferedImage = createImage(heightmap.length, heightmap[0].length);
            Graphics bg = bufferedImage.getGraphics();
            for(int i = 0; i < heightmap.length; i++) {
                for(int j = 0; j < heightmap[i].length; j++) {
                    if(heightmap[i][j] > 0 && heightmap[i][j] < 5) {
                        bg.setColor(new Color(25, 25, 25));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 50 && heightmap[i][j] < 55) {
                        bg.setColor(new Color(50, 50, 50));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 100 && heightmap[i][j] < 105) {
                        bg.setColor(new Color(75, 75, 75));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 150 && heightmap[i][j] < 155) {
                        bg.setColor(new Color(100, 100, 100));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 200 && heightmap[i][j] < 205) {
                        bg.setColor(new Color(125, 125, 125));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 250 && heightmap[i][j] < 255) {
                        bg.setColor(new Color(150, 150, 150));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 350 && heightmap[i][j] < 355) {
                        bg.setColor(new Color(175, 175, 175));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 400 && heightmap[i][j] < 405) {
                        bg.setColor(new Color(200, 200, 200));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 450 && heightmap[i][j] < 455) {
                        bg.setColor(new Color(225, 225, 225));
                        bg.drawLine(i, j, i, j);
                    }
                    else if(heightmap[i][j] > 500 && heightmap[i][j] < 505) {
                        bg.setColor(new Color(255, 255, 255));
                        bg.drawLine(i, j, i, j);
                    }
                    else {
                        bg.setColor(new Color(0, 0, 0));
                        bg.drawLine(i, j, i, j);
                    }
                }
                
                parent.increaseProgressBar();
            }
        }
        
        /*
         * Buffer the image using the colour renderer
         */
        private void bufferImageColor()
        {
            int colR = 0;
            int colG = 0;
            int colB = 0;
            heightmap = parent.normaliseMap(parent.getHeightMap(), 765);
            
            parent.setProgressBar(heightmap.length);
            
            bufferedImage = createImage(heightmap.length, heightmap[0].length);
            Graphics bg = bufferedImage.getGraphics();
            for(int i = 0; i < heightmap.length; i++) {
                for(int j = 0; j < heightmap[i].length; j++) {
                    colR = 0;
                    colG = 0;
                    colB = 0;
                    colR = heightmap[i][j];
                    if(colR > 255) {
                        colG = colR - 255;
                        colR = 255;
                        if(colG > 255) {
                            colB = colG - 255;
                            colG = 255;
                            if(colB > 255) colB = 255;
                        }
                    }
                    bg.setColor(new Color(colR, colG, colB));
                    bg.drawLine(i, j, i, j);
                }
                
                parent.increaseProgressBar();
            }
        }
        
        /*
         * Buffer the image using the Real renderer
         */
        private void bufferImageReal()
        {
            int colR = 0;
            int colG = 0;
            int colB = 0;
            heightmap = parent.normaliseMap(parent.getHeightMap(), 255);
            
            parent.setProgressBar(heightmap.length);
            
            bufferedImage = createImage(heightmap.length, heightmap[0].length);
            Graphics bg = bufferedImage.getGraphics();
            for(int i = 0; i < heightmap.length; i++) {
                for(int j = 0; j < heightmap[i].length; j++) {
                    if(heightmap[i][j] >= 0 && heightmap[i][j] < 100) {
                        colR = (int) (heightmap[i][j] / 2);
                        colG = 125 - heightmap[i][j];
                        colB = heightmap[i][j] / 8;
                    }
                    else if(heightmap[i][j] >= 100 && heightmap[i][j] < 200) {
                        colR = heightmap[i][j] - 50;
                        colG = heightmap[i][j] - 75;
                        colB = heightmap[i][j] / 4;
                    }
                    else if(heightmap[i][j] >= 200 && heightmap[i][j] <= 255) {
                        colR = heightmap[i][j];
                        colG = heightmap[i][j];
                        colB = heightmap[i][j];
                    }
                    bg.setColor(new Color(colR, colG, colB));
                    bg.drawLine(i, j, i, j);
                }
                
                parent.increaseProgressBar();
            }
        }
    }
        
    /**
     * ActionListener for components of View2DPanel
     */
    class ViewListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(btnZIn)) {                
                zoom = ((zoom - 1) * 2) + 1;
                
                if(zoom < Constants.VIEW2DZOOM_MAX) {
                    setPanelSize(new Dimension(zoom + 10, zoom + 70));
                }
                else {
                    zoom = Constants.VIEW2DZOOM_MAX;
                }
                
                parent.constants.view2DZoom = zoom;
                repaint = true;
                scalableImage = null;
                repaint();
            }
            else if(e.getSource().equals(btnZOut)) {                
                if(zoom > Constants.VIEW2DZOOM_DEFAULT)
                    zoom = ((zoom - 1) / 2) + 1;
                
                parent.constants.view2DZoom = zoom;
                setPanelSize(new Dimension(zoom + 10, zoom + 70));
                repaint = true;
                scalableImage = null;
                repaint();
            }
            else if(e.getSource().equals(cmbView)) {
                viewMethod = cmbView.getSelectedItem().toString();
                repaint = true;
                bufferedImage = null;
                scalableImage = null;
                repaint();
            }
        }
    }
}

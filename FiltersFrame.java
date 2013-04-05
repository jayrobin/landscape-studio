import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import Interfaces.*;

/**
 * Filters are first loaded into this frame (in a drop down box), allowing 
 * the user to choose a suitable filter. 
 * Upon choosing a filter the relevant panel is displayed containing customisable
 * parameters for that filter (If applicable)
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class FiltersFrame extends JInternalFrame
{
    private TabbedCanvas parent;
    private Vector filters;
    private FilterListener fListener;
    private JComboBox cmbFilters;
    private JPanel fltPanel, panel;
    private IFilter filter;
    private ViewPanel view;
    private Image bufferedImage, scalableImage;
    private JCheckBox chkPreview;

    /**
     * Constructor for objects of class FiltersFrame
     */
    public FiltersFrame(TabbedCanvas parent, Vector filters)
    {
        super(Constants.FILTERSFRAME_NAME, false, true, false, false);
        this.parent = parent;
        this.getContentPane().setLayout(new BorderLayout());
        this.setVisible(true);
        this.filters = filters;
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
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        chkPreview = new JCheckBox("Preview");
        chkPreview.setSelected(true);
        
        fListener = new FilterListener();
        
        cmbFilters = new JComboBox();
        cmbFilters.addActionListener(fListener);
        
        for(int i = 0; i < filters.size(); i++) {
            cmbFilters.addItem(filters.get(i).toString());
        }
        
        panel.add(cmbFilters, BorderLayout.PAGE_START);
        panel.add(fltPanel, BorderLayout.PAGE_END);
        
        JPanel viewPanel = new JPanel();
        viewPanel.setLayout(new BorderLayout());
        view = new ViewPanel();
        view.setPreferredSize(new Dimension(128, 128));
        
        chkPreview.addActionListener(fListener);
        viewPanel.add(view, BorderLayout.CENTER);
        viewPanel.add(chkPreview, BorderLayout.PAGE_END);
        
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.getContentPane().add(viewPanel, BorderLayout.LINE_START);
        this.setSize(new Dimension(this.getWidth() + 10, this.getHeight() + 10));
        parent.setCombineMap(parent.getHeightMap());
        this.revalidate();
        this.pack();
    }
    
    
    /**
     * ActionListener for various components of FilterListener
     */
    class FilterListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(cmbFilters)) {
                int index = cmbFilters.getSelectedIndex();
                filter = (IFilter) filters.get(index);
                filter.setPreview(chkPreview.isSelected());
                
                if(fltPanel != null)
                    panel.remove(fltPanel);
                    
                fltPanel = new JPanel();
                
                filter.setPanel(fltPanel);
                panel.add(fltPanel, BorderLayout.PAGE_END);
                fltPanel.setBorder(BorderFactory.createLineBorder(Color.black));
                revalidate();
                repaint();
                pack();
                setSize(new Dimension(getWidth() + 10, getHeight() + 10));
            }
            else if(e.getSource().equals(chkPreview)) {
                filter.setPreview(chkPreview.isSelected());
                repaint();
            }
        }
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
}

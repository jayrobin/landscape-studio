import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

/**
 * JInternalFrame containing information on the current 
 * heightmap including height range and map size. 
 * It also displays the status log
 * 
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class AdvancedFrame extends JInternalFrame
{
    private TabbedCanvas parent;
    private AdvancedListener aListener;
    private JButton btnClear;
    private Vector log;

    /**
     * Constructor for objects of class AdvancedFrame
     */
    public AdvancedFrame(TabbedCanvas parent, Vector log)
    {
        super(Constants.ADVANCEDFRAME_NAME, false, true, false, false);
        this.parent = parent;
        this.getContentPane().setLayout(new BorderLayout());
        this.setVisible(true);
        this.log = log;
        init();
    }
    
    /*
     * Initialisations for View2DPanel
     */
    private void init()
    {
        aListener = new AdvancedListener();
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblHeight = new JLabel("Height: ");
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        panel.add(lblHeight, c);
        
        JTextField txtHeight = new JTextField(Integer.toString(parent.getHeightMap().length));
        txtHeight.setEditable(false);
        c.ipadx = 25;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 3;
        panel.add(txtHeight, c);
        
        JLabel lblWidth = new JLabel("Width: ");
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        panel.add(lblWidth, c);
        
        JTextField txtWidth = new JTextField(Integer.toString(parent.getHeightMap().length));
        txtWidth.setEditable(false);
        c.ipadx = 25;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 3;
        panel.add(txtWidth, c);
        
        JLabel lblRange = new JLabel("Value range: ");
        c.ipadx = 0;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(lblRange, c);
        
        JTextField txtMin = new JTextField(Integer.toString(parent.getMinValue(parent.getHeightMap())));
        txtMin.setEditable(false);
        c.ipadx = 25;
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(txtMin, c);
        
        JLabel lblRange2 = new JLabel(" to ");
        lblRange2.setHorizontalAlignment(JLabel.CENTER);
        c.ipadx = 0;
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(lblRange2, c);
        
        JTextField txtMax = new JTextField(Integer.toString(parent.getMaxValue(parent.getHeightMap())));
        txtMax.setEditable(false);
        c.ipadx = 25;
        c.gridx = 3;
        c.gridy = 2;
        c.gridwidth = 1;
        panel.add(txtMax, c);
        
        btnClear = new JButton("Clear Terrain");
        btnClear.addActionListener(aListener);
        c.ipadx = 0;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 2;
        panel.add(btnClear, c);
        
        JTextArea txtLog = new JTextArea(10, 1);
        txtLog.setEditable(false);
        for(int i = 0; i < log.size(); i++) {
            String s = (String) log.get(i);
            txtLog.append(s);
        }
        
        JScrollPane scrollPane = 
                    new JScrollPane(txtLog,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        this.getContentPane().add(scrollPane, BorderLayout.PAGE_END);
        
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.pack();
        this.setSize(new Dimension(600, this.getHeight() + 10));
    }
    
    /**
     * ActionListener for components of View2DPanel
     */
    class AdvancedListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(btnClear)) {
                parent.setHeightMap(new int[parent.getHeightMap().length][parent.getHeightMap().length]);
                parent.amendLog("Heightmap reset");
            }
        }
    }
}

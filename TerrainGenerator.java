import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Initialisation class and main frame, holds one or more instances of 
 * TabbedCanvas in a JTabbedPane
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class TerrainGenerator extends JFrame
{
    private JTabbedPane tabbedPane;
    private JButton btnNew, btnLoad, btnSave, btn2D, btn3D, btnAlgorithm, btnPaint, btnCombine, btnAdvanced, btnFilters, btnClose;
    private int progress, progressMax, progressStep;
    private Rectangle progressBounds;
    private JMenuBar menuBar;
    private JMenu mnuFile, mnuEdit, mnuView, mnuWindow, mnuHelp, mnuPaste, mnuQuality3D;
    private JMenuItem mniNew, mniLoad, mniSave, mniSaveAs, mniExit,
                      mniAlgorithm, mniPaint, mniCombine, mniFilters, mniView2D, mniView3D,
                      mniCloseAll, mniAbout, mniCopy, mniUndo,
                      mniPAdd, mniPSub, mniPMult, mniPMax, mniPMin, mniPAnd, mniPOr, mniPXor,
                      mni3DVLow, mni3DLow, mni3DMed, mni3DHigh, mni3DVHigh;
    private ControlPanelListener cpListener;
    private SidePanelListener spListener;
    private MenuListener mListener;
    private int[][] copyBuffer;
    private static TerrainGenerator mainFrame;
    private File dirInfo; // Last save location
    private JPanel progressPanel;
    private JProgressBar progressBar;
    
    /**
     * Constructor for class TerrainGenerator
     */
    public TerrainGenerator()
    {
        try
        {
            init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /*
     * Initialise frame, add buttons etc
     */
    private void init()
    {
        // Setup the frame
        this.setTitle("Landscape Studio");
        this.setResizable(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        this.setLocation((screenSize.width - frameSize.width) / 2,
                    (screenSize.height - frameSize.height) / 2);
                    
        progress = 0;
        progressMax = 100;
        progressStep = 10;
        
        // Add the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel);
        
        // Add the tabbed pane and control panel to the main panel
        mainPanel.add(createTabbedPane(), BorderLayout.CENTER);
        mainPanel.add(createControlPanel(), BorderLayout.LINE_START);
        mainPanel.add(createSidePanel(), BorderLayout.LINE_END);
        mainPanel.add(createProgressPanel(), BorderLayout.PAGE_END);
        this.setJMenuBar(createMenu());
    }
    
    /*
     * Create and return the side panel (For closing TabbedCanvas instances)
     */
    private JPanel createSidePanel()
    {
        JPanel sidePanel = new JPanel();
        spListener = new SidePanelListener();
        
        btnClose = new JButton(Constants.CLOSE_NAME);
        btnClose.setPreferredSize(new Dimension(40, 40));
        btnClose.addActionListener(spListener);
        
        sidePanel.add(btnClose);
        
        return sidePanel;
    }
    
    private JPanel createProgressPanel()
    {
        progressPanel = new JPanel();
        
        Dimension size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        progressBounds = new Rectangle(size.width - 50, 25);
        progressPanel.setPreferredSize(new Dimension(size.width - 50, 25));
        progressBar = new JProgressBar();
        progressPanel.add(progressBar);
        
        return progressPanel;
    }
    
    /*
     * Create and return the main menu
     */
    private JMenuBar createMenu()
    {
        mListener = new MenuListener();
        
        menuBar = new JMenuBar();
        
        mnuFile = new JMenu("File");
        menuBar.add(mnuFile);
        mnuEdit = new JMenu("Edit");
        menuBar.add(mnuEdit);
        mnuView = new JMenu("View");
        menuBar.add(mnuView);
        mnuWindow = new JMenu("Window");
        menuBar.add(mnuWindow);
        mnuHelp = new JMenu("Help");
        menuBar.add(mnuHelp);
        
        mniNew = new JMenuItem(Constants.NEW_NAME);
        mniNew.addActionListener(mListener);
        mnuFile.add(mniNew);
        mniLoad = new JMenuItem(Constants.LOAD_NAME);
        mniLoad.addActionListener(mListener);
        mnuFile.add(mniLoad);
        mniSave = new JMenuItem(Constants.SAVE_NAME);
        mniSave.addActionListener(mListener);
        mnuFile.add(mniSave);
        mniSaveAs = new JMenuItem(Constants.SAVEAS_NAME);
        mniSaveAs.addActionListener(mListener);
        mnuFile.add(mniSaveAs);
        mnuFile.addSeparator();
        mniExit = new JMenuItem("Exit");
        mniExit.addActionListener(mListener);
        mnuFile.add(mniExit);
        
        mniUndo = new JMenuItem("Undo");
        mniUndo.setEnabled(false);
        mniUndo.addActionListener(mListener);
        mnuEdit.add(mniUndo);
        mnuEdit.addSeparator();
        mniCopy = new JMenuItem("Copy");
        mniCopy.addActionListener(mListener);
        mnuEdit.add(mniCopy);
        mnuPaste = new JMenu("Paste");
        mnuEdit.add(mnuPaste);
        mniPAdd = new JMenuItem("Add");
        mniPAdd.addActionListener(mListener);
        mnuPaste.add(mniPAdd);
        mniPSub = new JMenuItem("Subtract");
        mniPSub.addActionListener(mListener);
        mnuPaste.add(mniPSub);
        mniPMult = new JMenuItem("Multiply");
        mniPMult.addActionListener(mListener);
        mnuPaste.add(mniPMult);
        mniPMin = new JMenuItem("Min");
        mniPMin.addActionListener(mListener);
        mnuPaste.add(mniPMin);
        mniPMax = new JMenuItem("Max");
        mniPMax.addActionListener(mListener);
        mnuPaste.add(mniPMax);
        mniPAnd = new JMenuItem("And");
        mniPAnd.addActionListener(mListener);
        mnuPaste.add(mniPAnd);
        mniPOr = new JMenuItem("Or");
        mniPOr.addActionListener(mListener);
        mnuPaste.add(mniPOr);
        mniPXor = new JMenuItem("Xor");
        mniPXor.addActionListener(mListener);
        mnuPaste.add(mniPXor);
        mnuEdit.addSeparator();
        mniAlgorithm = new JMenuItem(Constants.ALGORITHMFRAME_NAME);
        mniAlgorithm.addActionListener(mListener);
        mnuEdit.add(mniAlgorithm);
        mniPaint = new JMenuItem(Constants.PAINTFRAME_NAME);
        mniPaint.setEnabled(false);
        mniPaint.addActionListener(mListener);
        mnuEdit.add(mniPaint);
        mniCombine = new JMenuItem(Constants.COMBINEFRAME_NAME);
        mniCombine.addActionListener(mListener);
        mnuEdit.add(mniCombine);
        
        mniFilters = new JMenuItem(Constants.FILTERSFRAME_NAME);
        mniFilters.addActionListener(mListener);
        mnuEdit.add(mniFilters);
        
        mniView2D = new JMenuItem(Constants.VIEW2DFRAME_NAME);
        mniView2D.addActionListener(mListener);
        mnuView.add(mniView2D);
        mniView3D = new JMenuItem(Constants.VIEW3DFRAME_NAME);
        mniView3D.addActionListener(mListener);
        mnuView.add(mniView3D);
        mnuView.addSeparator();
        mnuQuality3D = new JMenu("Quality");
        mnuView.add(mnuQuality3D);
        
        mni3DVLow = new JMenuItem("Very Low");
        mni3DVLow.addActionListener(mListener);
        mnuQuality3D.add(mni3DVLow);
        mni3DLow = new JMenuItem("Low");
        mni3DLow.addActionListener(mListener);
        mnuQuality3D.add(mni3DLow);
        mni3DMed = new JMenuItem("Med");
        mni3DMed.addActionListener(mListener);
        mnuQuality3D.add(mni3DMed);
        mni3DHigh = new JMenuItem("High");
        mni3DHigh.addActionListener(mListener);
        mnuQuality3D.add(mni3DHigh);
        mni3DVHigh = new JMenuItem("Very High");
        mni3DVHigh.addActionListener(mListener);
        mnuQuality3D.add(mni3DVHigh);
        
        mniCloseAll = new JMenuItem("Close All");
        mniCloseAll.addActionListener(mListener);
        mnuWindow.add(mniCloseAll);
        
        mniAbout = new JMenuItem("About");
        mniAbout.addActionListener(mListener);
        mnuHelp.add(mniAbout);
        
        return menuBar;
    }
    
    /*
     * Create and return the tabbed pane
     */
    private JTabbedPane createTabbedPane()
    {
        // Create a tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("new terrain", new TabbedCanvas(this));
        
        tabbedPane.addChangeListener(
            new ChangeListener() {
                public void stateChanged(ChangeEvent evt) {
                    JTabbedPane pane = (JTabbedPane)evt.getSource();

                    TabbedCanvas tab = (TabbedCanvas) pane.getSelectedComponent();
                    if(tab.getUndoMap() == null) {
                        mniUndo.setEnabled(false);
                    }
                    else {
                        mniUndo.setEnabled(true);
                    }
                }
            }
        );
        
        return tabbedPane;
    }
    
    /*
     * Create and return the control panel
     */
    private JPanel createControlPanel()
    {
        // Create the control panel (Along with test buttons)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 1));
        
        cpListener = new ControlPanelListener();
        
        btnNew = new JButton(Constants.NEW_NAME);
        btnNew.addActionListener(cpListener);
        controlPanel.add(btnNew);
        
        btnLoad = new JButton(Constants.LOAD_NAME);
        btnLoad.addActionListener(cpListener);
        controlPanel.add(btnLoad);
        
        btnSave = new JButton(Constants.SAVE_NAME);
        btnSave.addActionListener(cpListener);
        controlPanel.add(btnSave);
        
        controlPanel.add(new JSeparator());
        
        btn2D = new JButton(Constants.VIEW2DFRAME_NAME);
        controlPanel.add(btn2D);
        btn2D.addActionListener(cpListener);
        
        btn3D = new JButton(Constants.VIEW3DFRAME_NAME);
        controlPanel.add(btn3D);
        btn3D.addActionListener(cpListener);
        
        controlPanel.add(new JSeparator());
        
        btnAlgorithm = new JButton(Constants.ALGORITHMFRAME_NAME);
        controlPanel.add(btnAlgorithm);
        btnAlgorithm.addActionListener(cpListener);
        
        btnPaint = new JButton(Constants.PAINTFRAME_NAME);
        btnPaint.setEnabled(false);
        controlPanel.add(btnPaint);
        btnPaint.addActionListener(cpListener);
        
        btnCombine = new JButton(Constants.COMBINEFRAME_NAME);
        controlPanel.add(btnCombine);
        btnCombine.addActionListener(cpListener);
        
        btnFilters = new JButton(Constants.FILTERSFRAME_NAME);
        controlPanel.add(btnFilters);
        btnFilters.addActionListener(cpListener);
        
        controlPanel.add(new JSeparator());
        
        btnAdvanced = new JButton(Constants.ADVANCEDFRAME_NAME);
        controlPanel.add(btnAdvanced);
        btnAdvanced.addActionListener(cpListener);
        
        controlPanel.add(new JSeparator());
        
        return controlPanel;
    }
    
    public File getDirectoryInfo()
    {
        return dirInfo;
    }
    
    public void setDirectoryInfo(File dirInfo)
    {
        this.dirInfo = dirInfo;
    }
    
    public void resetProgressBar()
    {
        progress = 0;
        progressBar.setValue(0);
    }
    
    public void setProgressBar(int max)
    {
        progressMax = max;
        progressBar.setMaximum(max);
        progressStep = (int) (progressMax / 10);
    }
    
    public void increaseProgressBar()
    {
        progress++;
        
        if(progressStep != 0) {
            if(progress % progressStep == 0) {
                progressBar.setValue(progress);
                
                Rectangle progressRect = progressBar.getBounds();
                progressRect.x = 0;
                progressRect.y = 0;
                progressBar.paintImmediately( progressRect );
                repaint();
            }
        }
    }
        
    
    /**
     * Sets the title of a given tab, returns true if the tab was found
     * and the title was set, otherwise returns false
     * 
     * @param tab The tab to set the title of
     * @param title The new title for the tab
     * 
     * @return boolean Return true if succesful, false otherwise
     */
    public boolean setTabTitle(TabbedCanvas tab, String title)
    {
        for(int i = 0; i < tabbedPane.getTabCount(); i++) {
            TabbedCanvas curTab = (TabbedCanvas) tabbedPane.getComponentAt(i);
            if(curTab == tab) {
                tabbedPane.setTitleAt(i, title);
                return true;
            }
        }
        return false;
    }
    
    /*
     * Returns true if the heightmap is blank (All zeros)
     */
    private boolean heightmapIsBlank(int[][] heightmap)
    {
        for(int i = 0; i < heightmap.length; i++) {
            for(int j = 0; j < heightmap[i].length; j++) {
                if(heightmap[i][j] != 0) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void setUndoEnabled(boolean enabled)
    {
        mniUndo.setEnabled(enabled);
    }
    
    /*
     * Setup a tabbed canvas for loading (Load button pressed)
     */
    private void load()
    {
        if(tabbedPane.getTabCount() == 0) {
            TabbedCanvas pane = new TabbedCanvas(this);
            tabbedPane.addTab("new terrain", pane);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            String tabName = pane.loadFile();
            if(tabName != null) {
                tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabName);
            }
            else if (heightmapIsBlank(pane.getHeightMap())) {
                tabbedPane.remove(tabbedPane.getTabCount() - 1);
            }
        }
        else {
            TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
            if(!heightmapIsBlank(pane.getHeightMap())) {
                int response = JOptionPane.showConfirmDialog (null,
                    "Save file before loading?","Confirm",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    if(pane.getFileInfo() == null) {
                        pane.saveFile(true);
                    }
                    else {
                        pane.saveFile(false);
                    }
                    String tabName = pane.loadFile();
                    if(tabName != null) {
                        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabName);
                    }
                }
                else if (response == JOptionPane.NO_OPTION) {
                    String tabName = pane.loadFile();
                    if(tabName != null) {
                        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabName);
                    }
                }
            }
            else {
                String tabName = pane.loadFile();
                if(tabName != null) {
                    tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), tabName);
                }
            }
        }
    }
    
    /*
     * Setup a tabbed canvas for file saving (Save button pressed)
     */
    private void save(boolean saveAs)
    {
        if(tabbedPane.getTabCount() > 0) {
            TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
            pane.saveFile(saveAs);
        }
    }
    
    /**
     * Main method for class Terra
     * 
     * @param args Args for class TerrainGenerator
     */
    public static void main( String args[] )
    {
        try {
         UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { JOptionPane.showMessageDialog (null, "System look and feel not found"); }
        
        // Create an instance of the test application
        mainFrame  = new TerrainGenerator();
        Dimension size = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setBounds(0, 0, size.width, size.height - 25);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JInternalFrame.EXIT_ON_CLOSE);
    }
    
    /**
     * ActionListener for the control panel
     */
    class ControlPanelListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals(Constants.NEW_NAME)) {
                tabbedPane.addTab("new terrain", new TabbedCanvas(mainFrame));
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            }
            else if(e.getActionCommand().equals(Constants.SAVE_NAME)) {
                save(false);
            }
            else if(e.getActionCommand().equals(Constants.LOAD_NAME)) {
                load();
            }
            else if((e.getActionCommand().equals(Constants.VIEW2DFRAME_NAME)) ||
                    (e.getActionCommand().equals(Constants.VIEW3DFRAME_NAME)) ||
                    (e.getActionCommand().equals(Constants.ALGORITHMFRAME_NAME)) ||
                    (e.getActionCommand().equals(Constants.ALGORITHMFRAME_NAME)) ||
                    (e.getActionCommand().equals(Constants.PAINTFRAME_NAME)) ||
                    (e.getActionCommand().equals(Constants.COMBINEFRAME_NAME)) ||
                    (e.getActionCommand().equals(Constants.FILTERSFRAME_NAME)) ||
                    (e.getActionCommand().equals(Constants.ADVANCEDFRAME_NAME))) {  
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    pane.openFrame(e.getActionCommand());
                }
            }
        }
    }
    
    /**
     * ActionListener for the side panel
     */
    class SidePanelListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals(Constants.CLOSE_NAME)) {
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    if(!heightmapIsBlank(pane.getHeightMap())) {
                        int response = JOptionPane.showConfirmDialog (null,
                            "Save file before closing?","Confirm",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                        if (response == JOptionPane.YES_OPTION) {
                            if(pane.getFileInfo() == null) {
                                pane.saveFile(true);
                            }
                            else {
                                pane.saveFile(false);
                            }
                            tabbedPane.remove(tabbedPane.getSelectedComponent());
                        }
                        else if (response == JOptionPane.NO_OPTION) {
                            tabbedPane.remove(tabbedPane.getSelectedComponent());
                        }
                    }
                    else {
                        tabbedPane.remove(tabbedPane.getSelectedComponent());
                    }
                }
            }
        }
    }
    
    /**
     * ActionListener for the menu
     */
    class MenuListener implements ActionListener 
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(mniNew)) {
                tabbedPane.addTab("new terrain", new TabbedCanvas(mainFrame));
                tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            }
            else if(e.getSource().equals(mniSave)) {
                save(false);
            }
            else if(e.getSource().equals(mniSaveAs)) {
                save(true);
            }
            else if(e.getSource().equals(mniLoad)) {
                load();
            }
            else if(e.getSource().equals(mniExit)) {
                System.exit(0);
            }
            else if(e.getSource().equals(mniAlgorithm)) {
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    pane.openFrame(Constants.ALGORITHMFRAME_NAME);
                }
            }
            else if(e.getSource().equals(mniPaint)) {
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    pane.openFrame(Constants.PAINTFRAME_NAME);
                }
            }
            else if(e.getSource().equals(mniCombine)) {
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    pane.openFrame(Constants.COMBINEFRAME_NAME);
                }
            }
            else if(e.getSource().equals(mniFilters)) {
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    pane.openFrame(Constants.FILTERSFRAME_NAME);
                }
            }
            else if(e.getSource().equals(mniView2D)) {
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    pane.openFrame(Constants.VIEW2DFRAME_NAME);
                }
            }
            else if(e.getSource().equals(mniView3D)) {
                if(tabbedPane.getTabCount() > 0) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    pane.openFrame(Constants.VIEW3DFRAME_NAME);
                }
            }
            else if(e.getSource().equals(mniCloseAll)) {
                tabbedPane.removeAll();
            }
            else if(e.getSource().equals(mniUndo)) {
                TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                pane.undo();
            }
            else if(e.getSource().equals(mniCopy)) {
                TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                copyBuffer = pane.getHeightMap();
            }
            else if(e.getSource().equals(mniPAdd) || 
                    e.getSource().equals(mniPSub) ||
                    e.getSource().equals(mniPMult)||
                    e.getSource().equals(mniPMax) ||
                    e.getSource().equals(mniPMin) ||
                    e.getSource().equals(mniPAnd) ||
                    e.getSource().equals(mniPOr)  ||
                    e.getSource().equals(mniPXor)) {
                if(!heightmapIsBlank(copyBuffer)) {
                    TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                    int[][] heightmap = pane.combineMaps(((JMenuItem)e.getSource()).getText(), 100, pane.getHeightMap(), copyBuffer, true);
                    pane.setHeightMap(heightmap);
                    pane.refreshMainView();
                }
            }
            else if(e.getSource().equals(mni3DVLow)) {
                TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                pane.constants.view3DQuality = 32;
            }
            else if(e.getSource().equals(mni3DLow)) {
                TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                pane.constants.view3DQuality = 64;
            }
            else if(e.getSource().equals(mni3DMed)) {
                TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                pane.constants.view3DQuality = 128;
            }
            else if(e.getSource().equals(mni3DHigh)) {
                TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                pane.constants.view3DQuality = 256;
            }
            else if(e.getSource().equals(mni3DVHigh)) {
                TabbedCanvas pane = (TabbedCanvas) tabbedPane.getSelectedComponent();
                pane.constants.view3DQuality = 512;
            }
        }
    }
}
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.util.Vector;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import Interfaces.*;
import javax.swing.filechooser.FileFilter;

/**
 * Holds a terrain map (2-dimensional integer array), holds instances of 
 * various GUI windows as JInternalFrames
 * 
 * @author James Robinson 
 * @version 1.0
 */
public class TabbedCanvas extends JDesktopPane implements ICanvasAlg
{
    private JButton btnClose;
    private int[][] heightmap, terrainMap, applicationMap, previewMap, combineMap, undoMap;
    private Vector algorithms, importers, exporters, filters;
    private int width;
    private Vector log;
    private boolean preview;
    private TerrainGenerator parent;
    private File fileInfo;
    public Constants constants;
    
    /**
     * Constructor for objects of class TabbedCanvas
     */
    public TabbedCanvas(TerrainGenerator parent)
    {
        this.parent = parent;
        init();
    }
    
    /*
     * Initialisation routines for TabbedCanvas
     */
    private void init()
    {
        // The background colour must be set to 'Windows grey'
        this.setBackground(new Color(200, 200, 200));
        width = Constants.SIZE_DEFAULT;
        
        // Initialise the vectors
        log = new Vector();
        algorithms = new Vector();
        importers = new Vector();
        exporters = new Vector();
        filters = new Vector();
        
        // Initialise the heightmap
        heightmap = new int[width][width];
        for(int i = 0; i < heightmap.length; i++) {
            for(int j = 0; j < heightmap[i].length; j++) {
                heightmap[i][j] = 0;
            }
        }
        previewMap = new int[128][128];
        for(int i = 0; i < previewMap.length; i++) {
            for(int j = 0; j < previewMap[i].length; j++) {
                previewMap[i][j] = 0;
            }
        }
        combineMap = new int[128][128];
        for(int i = 0; i < combineMap.length; i++) {
            for(int j = 0; j < combineMap[i].length; j++) {
                combineMap[i][j] = 0;
            }
        }
        amendLog("Heightmaps initialised");
        
        // initialise constants
        constants = new Constants();
        constants.view2DZoom = Constants.VIEW2DZOOM_DEFAULT;
        constants.view3DQuality = Constants.VIEW3DQUALITY_DEFAULT;
        
        // load algorithms
        try {
            MultiFilter filter = new MultiFilter(Constants.ALGORITHM_PREFIX);
            String dir = System.getProperty("user.dir") + System.getProperty("file.separator");
            File f = new File(dir);
            String[] fl = f.list(filter);
            for(int i = 0; i < fl.length; i++) {
                Class IA = Class.forName(fl[i].substring(0, fl[i].lastIndexOf(".")));
                IAlgorithm IAlg = (IAlgorithm) IA.newInstance();
                IAlg.setParent(this);
                algorithms.add(IAlg);
                amendLog("Algorithm " + IAlg + " loaded");
            }
        }
        catch (Exception e) { displayError(e.toString(), "Error loading algorithm file", "Please redownload and reinstall Landscape Studio and any 3rd party algorithm files");}
        
        
        // load importers
        try {
            MultiFilter filter = new MultiFilter(Constants.IMPORTER_PREFIX);
            String dir = System.getProperty("user.dir") + System.getProperty("file.separator");
            File f = new File(dir);
            String[] fl = f.list(filter);
            for(int i = 0; i < fl.length; i++) {
                Class II = Class.forName(fl[i].substring(0, fl[i].lastIndexOf(".")));
                IImporter IImp = (IImporter) II.newInstance();
                IImp.setParent(this);
                importers.add(IImp);
                amendLog("Importer " + IImp + " loaded");
            }
        }
        catch (Exception e) { amendLog("Error loading importer"); }
        
        // load exporters
        try {
            MultiFilter filter = new MultiFilter(Constants.EXPORTER_PREFIX);
            String dir = System.getProperty("user.dir") + System.getProperty("file.separator");
            File f = new File(dir);
            String[] fl = f.list(filter);
            for(int i = 0; i < fl.length; i++) {
                Class IE = Class.forName(fl[i].substring(0, fl[i].lastIndexOf(".")));
                IExporter IExp = (IExporter) IE.newInstance();
                IExp.setParent(this);
                exporters.add(IExp);
                amendLog("Exporter " + IExp + " loaded");
            }
        }
        catch (Exception e) { amendLog("Error loading importer"); }
        
        // load filters
        try {
            MultiFilter filter = new MultiFilter(Constants.FILTER_PREFIX);
            String dir = System.getProperty("user.dir") + System.getProperty("file.separator");
            File f = new File(dir);
            String[] fl = f.list(filter);
            for(int i = 0; i < fl.length; i++) {
                Class IF = Class.forName(fl[i].substring(0, fl[i].lastIndexOf(".")));
                IFilter IFil = (IFilter) IF.newInstance();
                IFil.setParent(this);
                filters.add(IFil);
                amendLog("Filter " + IFil + " loaded");
            }
        }
        catch (Exception e) { amendLog("Error loading filter"); }
    }
    
    public File getFileInfo()
    {
        return fileInfo;
    }
    
    public void resetProgressBar()
    {
        parent.resetProgressBar();
    }
    
    public void setProgressBar(int max)
    {
        parent.setProgressBar(max);
    }
    
    public void increaseProgressBar()
    {
        parent.increaseProgressBar();
    }
    
    /**
     * Normalise the map to a given value
     * 
     * @param map The map to be normalised
     * @param max The normalisation value
     * 
     * @return int[][] The normalised map
     */
    public int[][] normaliseMap(int[][] map, int max)
    {
        int minAlg = map[0][0];
        int maxAlg = map[0][0];
        
        int[][] newMap = new int[map.length][map.length];
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
               if(map[i][j] < minAlg) minAlg = map[i][j];
               if(map[i][j] > maxAlg) maxAlg = map[i][j];
            }
        }
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
                newMap[i][j] = map[i][j];
                newMap[i][j] -= minAlg;
                newMap[i][j] = (int) (((double) newMap[i][j] / (maxAlg - minAlg)) * max);
            }
        }
        
        amendLog("Map normalised to " + max);
        
        return newMap;
    }
    
    /**
     * Returns the minimum value of a given heightmap
     * 
     * @param map The map to be searched
     * 
     * @return int The minimum integer value within the map
     */
    public int getMinValue(int[][] map)
    {
        int min = map[0][0];
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
               if(map[i][j] < min) min = map[i][j];
            }
        }
        
        return min;
    }
    
    /**
     * Returns the maximum value of a given heightmap
     * 
     * @param map The map to be searched
     * 
     * @return int The maximum integer value within the map
     */
    public int getMaxValue(int[][] map)
    {
        int max = map[0][0];
        
        for (int i=0; i < map.length; i++) { 
            for (int j=0; j < map[i].length; j++) {
               if(map[i][j] > max) max = map[i][j];
            }
        }
        
        return max;
    }
    
    /**
     * Combines two maps using a given method and strength
     * 
     * @param method    The combination method to be used
     * @param strength  The relative strength which to combine the maps (hm1:hm2)
     * @param hm1       The main heightmap
     * @param hm1       The secondary heightmap
     * 
     * @return int[][]  The result of the combination
     */
    public int[][] combineMaps(String method, int strength, int[][] hm1, int[][] hm2, boolean preview)
    {
//         if(((hm1.length != hm2.length) || (hm1[0].length != hm2[0].length)) && !preview) {
//             return null;
//         }
        
        hm1 = normaliseMap(hm1, 1000);
        hm2 = normaliseMap(hm2, 1000);
        
        setUndoMap(this.heightmap);
        parent.setUndoEnabled(true);
          
        double step = (double)hm2.length / (double)hm1.length;
        double multiplier = (double) strength / 100;
        
        parent.setProgressBar(hm1.length);
           
        for(int i = 0; i < hm1.length; i++) {
            for(int j = 0; j < hm1[i].length; j++) {
                
                if(method.equals("Add")) {
                    hm1[i][j] += (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier);
                }
                else if(method.equals("Subtract")) {
                    hm1[i][j] -= (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier);
                }
                else if(method.equals("Multiply")) {
                    hm1[i][j] *= (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier);
                }
                else if(method.equals("Max")) {
                    hm1[i][j] = Math.max(hm1[i][j], (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier));
                }
                else if(method.equals("Min")) {
                    hm1[i][j] = Math.min(hm1[i][j], (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier));
                }
                else if(method.equals("And")) {
                    hm1[i][j] = hm1[i][j] & (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier);
                }
                else if(method.equals("Or")) {
                    hm1[i][j] = hm1[i][j] | (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier);
                }
                else if(method.equals("Xor")) {
                    hm1[i][j] = hm1[i][j] ^ (int) ((double) hm2[(int)(i * step)][(int) (j * step)] * multiplier);
                }
            }
            
            if(!preview)
                parent.increaseProgressBar();
        }
        parent.resetProgressBar();
        
        amendLog("Operation " + method + " performed");
        
        return hm1;
    }
    
    /**
     * Display a given frame if it is not already open
     * 
     * @param name The title of the frame to display
     */
    public void openFrame(String name)
    {
        if(name.equals(Constants.VIEW2DFRAME_NAME)) {
            if(!isOpen(Constants.VIEW2DFRAME_NAME)) {
                View2DFrame frame = new View2DFrame(this);
                this.add(frame);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
            else {
                JInternalFrame frame = (JInternalFrame) getFrame(Constants.VIEW2DFRAME_NAME);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
        }
        else if(name.equals(Constants.VIEW3DFRAME_NAME)) {
            if(!isOpen(Constants.VIEW3DFRAME_NAME)) {
                try {
                    View3DFrame frame = new View3DFrame(this);
                    this.add(frame);
                    frame.setSelected(true);
                    frame.requestFocus();
                } catch(Exception e) {JOptionPane.showMessageDialog (null, e.toString()); amendLog(e.toString());}
                catch(UnsatisfiedLinkError e) {displayError(e.toString(), "Could not find the Java3D libraries", "Please download Java3D from http://java.sun.com/products/java-media/3D/download.html");}
            }
            else {
                JInternalFrame frame = (JInternalFrame) getFrame(Constants.VIEW3DFRAME_NAME);
                try {
                    frame.setSelected(true);
                    frame.requestFocus();
                }
                catch(UnsatisfiedLinkError e) {displayError(e.toString(), "Could not find the Java3D libraries", "Please download Java3D from http://java.sun.com/products/java-media/3D/download.html");}
                catch(Exception e) {JOptionPane.showMessageDialog (null, e.toString()); amendLog(e.toString());}
            }
        }
        else if(name.equals(Constants.ALGORITHMFRAME_NAME)) {
            if(!isOpen(Constants.ALGORITHMFRAME_NAME)) {
                if(isOpen(Constants.COMBINEFRAME_NAME)) {
                    closeFrame(Constants.COMBINEFRAME_NAME);
                }
                if(isOpen(Constants.FILTERSFRAME_NAME)) {
                    closeFrame(Constants.FILTERSFRAME_NAME);
                }
                AlgorithmFrame frame = new AlgorithmFrame(this, algorithms, importers);
                setTerrainMap(null);
                setApplicationMap(null);
                this.add(frame);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
            else {
                JInternalFrame frame = (JInternalFrame) getFrame(Constants.ALGORITHMFRAME_NAME);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
        }
        else if(name.equals(Constants.PAINTFRAME_NAME)) {
            if(!isOpen(Constants.PAINTFRAME_NAME)) {
                PaintFrame frame = new PaintFrame(this);
                this.add(frame);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
            else {
                JInternalFrame frame = (JInternalFrame) getFrame(Constants.PAINTFRAME_NAME);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
        }
        else if(name.equals(Constants.COMBINEFRAME_NAME)) {
            if(!isOpen(Constants.COMBINEFRAME_NAME)) {
                if(isOpen(Constants.ALGORITHMFRAME_NAME)) {
                    closeFrame(Constants.ALGORITHMFRAME_NAME);
                }
                if(isOpen(Constants.FILTERSFRAME_NAME)) {
                    closeFrame(Constants.FILTERSFRAME_NAME);
                }
                CombineFrame frame = new CombineFrame(this, importers);
                this.add(frame);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
            else {
                JInternalFrame frame = (JInternalFrame) getFrame(Constants.COMBINEFRAME_NAME);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
        }
        else if(name.equals(Constants.FILTERSFRAME_NAME)) {
            if(!isOpen(Constants.FILTERSFRAME_NAME)) {
                if(isOpen(Constants.ALGORITHMFRAME_NAME)) {
                    closeFrame(Constants.ALGORITHMFRAME_NAME);
                }
                if(isOpen(Constants.COMBINEFRAME_NAME)) {
                    closeFrame(Constants.COMBINEFRAME_NAME);
                }
                FiltersFrame frame = new FiltersFrame(this, filters);
                this.add(frame);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
            else {
                JInternalFrame frame = (JInternalFrame) getFrame(Constants.FILTERSFRAME_NAME);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
        }
        else if(name.equals(Constants.ADVANCEDFRAME_NAME)) {
            if(!isOpen(Constants.ADVANCEDFRAME_NAME)) {
                AdvancedFrame frame = new AdvancedFrame(this, log);
                this.add(frame);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
            else {
                JInternalFrame frame = (JInternalFrame) getFrame(Constants.ADVANCEDFRAME_NAME);
                try {
                    frame.setSelected(true);
                } catch(Exception e) {}
                frame.requestFocus();
            }
        }
        repaint();
    }
    
    public void reset3D()
    {
        closeFrame(Constants.VIEW3DFRAME_NAME);
        openFrame(Constants.VIEW3DFRAME_NAME);
    }
    
    /*
     * Returns true if a given frame is currently open
     * 
     * @param name  The title of the frame to check
     */
    private boolean isOpen(String name)
    {
        Component[] components = getComponents();
        for(int i = 0; i < components.length; i++) {
            JInternalFrame frame = (JInternalFrame) components[i];
            if(frame.getTitle().equals(name))
                return true;
        }
        
        return false;
    }
    
    private JInternalFrame getFrame(String frameTitle)
    {
        Component[] components = getComponents();
        for(int i = 0; i < components.length; i++) {
            JInternalFrame frame = (JInternalFrame) components[i];
            if(frame.getTitle().equals(frameTitle))
                return frame;
        }
        
        return null;
    }

    /**
     * Close all currently displayed panels
     */
    public void removeAllFrames()
    {
        removeAll();
        this.revalidate();
        this.repaint();
    }
    
    private void closeFrame(String frameTitle)
    {
        Component[] components = getComponents();
        for(int i = 0; i < components.length; i++) {
            JInternalFrame frame = (JInternalFrame) components[i];
            if(frame.getTitle().equals(frameTitle)) {
                remove(frame);
            }
        }
    }
    
    /**
     * Returns the heightmap
     * 
     * @returns int[][] The heightmap
     */
    public int[][] getHeightMap()
    {
        return heightmap;
    }
    
    /**
     * Returns the terrain map
     * 
     * @returns int[][] The terrain map
     */
    public int[][] getTerrainMap()
    {
        return terrainMap;
    }
    
    /**
     * Returns the application map
     * 
     * @returns int[][] The application map
     */
    public int[][] getApplicationMap()
    {
        return applicationMap;
    }
    
    /**
     * Returns the preview map
     * 
     * @returns int[][] The preview map
     */
    public int[][] getPreviewMap()
    {
        return previewMap;
    }
    
    public int[][] getCombineMap()
    {
        return combineMap;
    }
    
    public int[][] getUndoMap()
    {
        return undoMap;
    }
    
    /**
     * Set the heightmap (Includes TAM and normalisation operations)
     * 
     * @param heightmap The heightmap
     */
    public void setHeightMap(int[][] heightmap)
    {
        try {
            if(terrainMap == null || applicationMap == null) {
                setUndoMap(this.heightmap);
                parent.setUndoEnabled(true);
                this.heightmap = normaliseMap(heightmap, 1000);
                amendLog("Heightmap set");
            }
            else {
                // check that the maps are of the same size
//                 if(heightmap.length != terrainMap.length || heightmap.length != applicationMap.length) {
//                     JOptionPane.showMessageDialog (null, "The terrain and application maps must be of the same size as the heightmap");
//                     amendLog("Error during TAM: incorrect map sizes");
//                     return;
//                 }
                
                setUndoMap(this.heightmap);
                parent.setUndoEnabled(true);
                
                // normalise all maps to 1000
                terrainMap = normaliseMap(terrainMap, 1000);
                applicationMap = normaliseMap(applicationMap, 1000);
                heightmap = normaliseMap(heightmap, 1000);
                this.heightmap = new int[heightmap.length][heightmap.length];
                
                double stepT = (double)terrainMap.length / (double)heightmap.length;
                double stepA = (double)applicationMap.length / (double)heightmap.length;
                
                for(int i = 0; i < heightmap.length; i++) {
                    for(int j = 0; j < heightmap[i].length; j++) {
                        if(((int) (i * stepT) < terrainMap.length) && ((int) (j * stepT) < terrainMap.length) && ((int) (i * stepA) < applicationMap.length) && ((int) (j * stepA) < applicationMap.length))
                            this.heightmap[i][j] = terrainMap[(int) (i * stepT)][(int) (j * stepT)] + ((int) (heightmap[i][j] * ((double)applicationMap[(int) (i * stepA)][(int) (j * stepA)] / 100)));
                    }
                }
                amendLog("Heightmap set, TAM operation performed");
            }
            
            heightmap = normaliseMap(heightmap, 1000);
            
            refreshMainView();
            refreshMiniView(this.heightmap);
            
            if(isOpen(Constants.VIEW3DFRAME_NAME))
                reset3D();
        }
        catch(Exception e) { amendLog("Error setting heightmap"); System.out.println(e); e.printStackTrace(); }
        System.gc();
    }
    
    public void undo()
    {
        heightmap = undoMap;
        
//         for(int i = 0; i < undoMap.length; i++) {
//             for(int j = 0; j < undoMap[i].length; j++) {
//                 this.heightmap[i][j] = this.undoMap[i][j];
//             }
//         }
        
        parent.setUndoEnabled(false);
        undoMap = null;
        refreshMainView();
        refreshMiniView(heightmap);
    }
    
    public void refreshMainView()
    {
        if(isOpen(Constants.VIEW2DFRAME_NAME) || isOpen(Constants.VIEW3DFRAME_NAME)) {
            Component[] components = getComponents();
            for(int i = 0; i < components.length; i++) {
                JInternalFrame frame = (JInternalFrame) components[i];
                if(frame.getTitle().equals(Constants.VIEW2DFRAME_NAME)) {
                    View2DFrame vFrame = (View2DFrame) components[i];
                    vFrame.reset();
                    vFrame.repaint();
                }
                if(frame.getTitle().equals(Constants.ALGORITHMFRAME_NAME)) {
                    AlgorithmFrame aFrame = (AlgorithmFrame) components[i];
                    aFrame.repaint();
                }
                if(frame.getTitle().equals(Constants.VIEW3DFRAME_NAME)) {
                    View3DFrame vFrame = (View3DFrame) components[i];
                    //vFrame.reset();
                }
            }
        }
        else {
            openFrame(Constants.VIEW2DFRAME_NAME);
        }
    }
    
    public void refreshMiniView(int[][] pmap)
    {
        if(isOpen(Constants.ALGORITHMFRAME_NAME)) {
            if(pmap == null) {
                setPreviewMap(new int[128][128], 128);
            }
            else {
                setPreviewMap(pmap, 128);
            }
            Component[] components = getComponents();
            for(int i = 0; i < components.length; i++) {
                JInternalFrame frame = (JInternalFrame) components[i];
                if(frame.getTitle().equals(Constants.ALGORITHMFRAME_NAME)) {
                    AlgorithmFrame aFrame = (AlgorithmFrame) components[i];
                    aFrame.resetImages();
                    aFrame.repaint();
                }
            }
        }
        if(isOpen(Constants.FILTERSFRAME_NAME)) {
            if(pmap == null) {
                setPreviewMap(new int[128][128], 128);
            }
            else {
                setPreviewMap(pmap, 128);
            }
            Component[] components = getComponents();
            for(int i = 0; i < components.length; i++) {
                JInternalFrame frame = (JInternalFrame) components[i];
                if(frame.getTitle().equals(Constants.FILTERSFRAME_NAME)) {
                    FiltersFrame fFrame = (FiltersFrame) components[i];
                    fFrame.reset();
                    fFrame.repaint();
                }
            }
        }
        if(isOpen(Constants.COMBINEFRAME_NAME)) {
            if(pmap == null) {
                setPreviewMap(new int[128][128], 128);
            }
            else {
                setPreviewMap(pmap, 128);
            }
            Component[] components = getComponents();
            for(int i = 0; i < components.length; i++) {
                JInternalFrame frame = (JInternalFrame) components[i];
                if(frame.getTitle().equals(Constants.COMBINEFRAME_NAME)) {
                    CombineFrame cFrame = (CombineFrame) components[i];
                    cFrame.reset();
                    cFrame.repaint();
                }
            }
        }
    }
    
//     public void refreshCombineView(int[][] cmap)
//     {
//         if(isOpen(Constants.COMBINEFRAME_NAME)) {
//             if(pmap == null) {
//                 setPreviewMap(new int[128][128]);
//             }
//             else {
//                 setPreviewMap(cmap);
//             }
//             Component[] components = getComponents();
//             for(int i = 0; i < components.length; i++) {
//                 JInternalFrame frame = (JInternalFrame) components[i];
//                 if(frame.getTitle().equals(Constants.COMBINEFRAME_NAME)) {
//                     CombineFrame cFrame = (CombineFrame) components[i];
//                     cFrame.reset();
//                     cFrame.repaint();
//                 }
//             }
//         }
//     }
    
    /**
     * Set the terrain map
     * 
     * @param terrainMap The terrain map
     */
    public void setTerrainMap(int[][] terrainMap)
    {
//         this.terrainMap = new int[terrainMap.length][terrainMap.length];
//         for(int i = 0; i < terrainMap.length; i++) {
//             for(int j = 0; j < terrainMap[i].length; j++) {
//                 this.terrainMap[i][j] = terrainMap[i][j];
//             }
//         }
        this.terrainMap = terrainMap;
        amendLog("Terrain map set");
    }
    
    /**
     * Set the application map
     * 
     * @param The application map
     */
    public void setApplicationMap(int[][] applicationMap)
    {
//         this.applicationMap = new int[applicationMap.length][applicationMap.length];
//         for(int i = 0; i < applicationMap.length; i++) {
//             for(int j = 0; j < applicationMap[i].length; j++) {
//                 this.applicationMap[i][j] = applicationMap[i][j];
//             }
//         }
        this.applicationMap = applicationMap;
        amendLog("Application map set");
    }
    
    public void setUndoMap(int[][] undoMap)
    {
        this.undoMap = new int[undoMap.length][undoMap.length];
        for(int i = 0; i < undoMap.length; i++) {
            for(int j = 0; j < undoMap[i].length; j++) {
                this.undoMap[i][j] = undoMap[i][j];
            }
        }
        amendLog("Undo map set");
    }
    
    /**
     * Set the preview map
     * 
     * @param The preview map
     */
    public void setPreviewMap(int[][] previewMap, int width)
    {
        if(terrainMap != null && applicationMap != null) {
            this.previewMap = new int[width][width];
            double step = (double)previewMap.length / (double)this.previewMap.length;
            
            terrainMap = normaliseMap(terrainMap, 1000);
            applicationMap = normaliseMap(applicationMap, 1000);
            
            for(int i = 0; i < previewMap.length; i++) {
                for(int j = 0; j < previewMap[i].length; j++) {
                    this.previewMap[i][j] = terrainMap[(int) (i*step)][(int) (j*step)] + ((int) (previewMap[i][j] * ((double)applicationMap[(int) (i*step)][(int) (j*step)] / 100)));
                }
            }
            
            amendLog("Preview map set - TAM");
        }
        else {
            this.previewMap = new int[width][width];
            double step = (double)previewMap.length / (double)this.previewMap.length;
            
            for(int i = 0; i < this.previewMap.length; i++) {
                for(int j = 0; j < this.previewMap[i].length; j++) {
                    this.previewMap[i][j] = previewMap[(int) (i*step)][(int) (j*step)];
                }
            }
            amendLog("Preview map set");
        }
    }
    
    public void setCombineMap(int[][] combineMap)
    {
        if(combineMap == null) {
            this.combineMap = new int[128][128];
        }
        else {
            this.combineMap = new int[combineMap.length][combineMap[0].length];
        }
        for(int i = 0; i < combineMap.length; i++) {
            for(int j = 0; j < combineMap[i].length; j++) {
                this.combineMap[i][j] = combineMap[i][j];
            }
        }
        amendLog("Combine map set");
        
    }
    
    /*
     * Return a given column number of the heightmap as a string
     * 
     * @param col       The heightmap column to obtain
     * 
     * @return String   The string representing the column
     */
    private String getHeightMapCol(int col)
    {
        String hmString = "";
        
        for(int i = 0; i < heightmap[col].length; i++) {
            hmString += heightmap[col][i] + "_";
        }
        
        return hmString;
    }
    
    /**
     * Performs heightmap save routines, including displaying a JFileChooser 
     * and writing the file
     */
    public void saveFile(boolean saveAs)
    {
        if(saveAs || fileInfo == null) {
            JFileChooser fc;
            
            File file = parent.getDirectoryInfo();
            
            if(file == null) {
                fc = new JFileChooser(System.getProperty("user.dir") + "\\");
            }
            else {
                fc = new JFileChooser(file.getPath().substring(0, file.getPath().lastIndexOf("\\") + 1));
            }
            
            fc.setAcceptAllFileFilterUsed(false);
            
            for(int i = 0; i < exporters.size(); i++) {
                IExporter exp = (IExporter) exporters.get(i);
                fc.addChoosableFileFilter(new Filter(exp.toString()));
            }
            
            FileFilter[] flts = fc.getChoosableFileFilters();
            fc.setFileFilter(flts[0]);
            
            file = null;
            int returnVal = fc.showSaveDialog(this);
            
            // User wishes to save to a file
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                if(file == null) {
                    return;
                }
                
                // File already exists, determine overwrite
                if (file.exists()) {
                    int response = JOptionPane.showConfirmDialog (null,
                    "Overwrite existing file?","Confirm Overwrite",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.CANCEL_OPTION) return;
                }
                
                String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
                String filter = fc.getFileFilter().getDescription();
                filter = filter.substring(filter.lastIndexOf(".") + 1, filter.length());
                
                if(!extension.equals(filter)) {
                    file = new File(file.getPath().substring(0, file.getPath().lastIndexOf("\\") + 1)  + file.getName() + "." + filter);
                }
                
                for(int j = 0; j < exporters.size(); j++) {
                    IExporter exp = (IExporter) exporters.get(j);
                    if(exp.toString().equals(filter)) {
                        exp.saveFile(file, heightmap);
                        parent.setDirectoryInfo(file);
                        j = exporters.size();
                    }
                }
                
                fileInfo = file;
                parent.setTabTitle(this, fileInfo.getName());
            }
        }
        else {
            String extension = fileInfo.getName().substring(fileInfo.getName().lastIndexOf(".") + 1, fileInfo.getName().length());
                
            for(int j = 0; j < exporters.size(); j++) {
                IExporter exp = (IExporter) exporters.get(j);
                if(exp.toString().equals(extension)) {
                    heightmap = normaliseMap(heightmap, 255);
                    exp.saveFile(fileInfo, heightmap);
                    parent.setDirectoryInfo(fileInfo);
                    j = exporters.size();
                }
            }
        }
    }
    
    /**
     * Performs heightmap load routines, including displaying a JFileChooser 
     * and reading the file
     * 
     * @return String   The file name of the loaded file to be displayed in the relevant tab
     */
    public String loadFile()
    {
        File file = parent.getDirectoryInfo();
        JFileChooser fc;
        
        if(file == null) {
            fc = new JFileChooser(System.getProperty("user.dir") + "\\");
        }
        else {
            fc = new JFileChooser(file.getPath().substring(0, file.getPath().lastIndexOf("\\") + 1));
        }
        
        for(int i = 0; i < importers.size(); i++) {
            IImporter imp = (IImporter) importers.get(i);
            fc.addChoosableFileFilter(new Filter(imp.toString()));
        }
        
        file = null;
        int returnVal = fc.showOpenDialog(this);
        
        if (returnVal == JFileChooser.CANCEL_OPTION) {
          return null;
        }
        else if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            
            if(file == null) {
                return null;
            }
            
            String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());

            for(int j = 0; j < importers.size(); j++) {
                IImporter imp = (IImporter) importers.get(j);
                if(imp.toString().equals(extension)) {
                    
                    // Reset terrain and application maps
                    applicationMap = null;
                    terrainMap = null;
                    
                    int[][] heightmap = imp.loadFile(file);
                    
                    // check for null heightmap
                    if(heightmap == null)
                        return null;
                        
                    setHeightMap(heightmap);
                    j = importers.size();
                }
            }
            
            // by default close all panels and open the 2D view
            removeAllFrames();
            View2DFrame frame = new View2DFrame(this);
            this.add(frame);
            
            return file.getName();
        }
        
        return null;
    }
    
    public int[][] cloneArray(int[][] source) 
    {
        int[][] destination = new int[source.length][source.length];
        
        for(int i = 0; i < source.length; i++) {
            for(int j = 0; j < source[i].length; j++) {
                destination[i][j] = source[i][j];
            }
        }
        
        return destination;
    }
    
    /**
     * Adds a new event to the log, including the date and time of the event
     * 
     * @param s     A string representing the event
     */
    public void amendLog(String s)
    {
        String result = "";
        
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        String format = "HH:mm:ss aa";
        SimpleDateFormat simpleFormat = new SimpleDateFormat(format);
        simpleFormat.setTimeZone(TimeZone.getDefault());
        result = simpleFormat.format(c.getTime()) + "\t" + s + "\n";

        log.add(result);
    }
    
    /**
     * Displays a JOptionPane message dialog, consisting of an error, problem and a solution
     * 
     * @param error     A string representing the error (exception/error)
     * @param problem   A string representing the problem (explanation of error)
     * @param solution  A string representing the solution (solution to error)
     */
    public void displayError(String error, String problem, String solution)
    {
        JOptionPane.showMessageDialog (null, "Error: " + error + "\nProblem: " + problem + "\nSolution: " + solution);
    }
    
    /**
     * Resets the log
     */
    public void resetLog()
    {
        log = new Vector();
    }
}

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Geometry;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;

/**
 * JInternalFrame containing a 3-dimensional view of the current 
 * terrain map, including controls for rotating, zooming and panning the view
 * 
 * NOT YET IMPLEMENTED
 * 
 * @author James Robinson 
 * @version 0.1
 */
public class View3DFrame extends JInternalFrame
{
    private TabbedCanvas parent;
    private SimpleUniverse univ = null;
    private BranchGroup scene, root;
    private javax.swing.JPanel drawingPanel;
    private JButton btnReset;
    private MouseRotate mRot;
    //private Rotator mRot;
    private MouseZoom mZoom;
    private MouseTranslate mTran;
    private BranchGroup objRoot;
    private TransformGroup objTrans;
    private BoundingSphere bounds;
    private SimpleUniverse simpleU;
    private TransformGroup vpTrans;
    private KeyNavigatorBehavior keyNavBeh;
    private int divs;

    /**
     * Constructor for objects of class View3DFrame
     */
    public View3DFrame(TabbedCanvas parent)
    {
        super(Constants.VIEW3DFRAME_NAME, false, true, false, false);
        try {
            this.setSize(new Dimension(300, 300));
            this.parent = parent;
            this.getContentPane().setLayout(new BorderLayout());
            this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
            
            divs = parent.constants.view3DQuality;
            
            GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

            Canvas3D canvas3D = new Canvas3D(config);
            
            btnReset = new JButton("Reset");
            btnReset.addActionListener(new ViewListener());
            this.getContentPane().add(btnReset, BorderLayout.PAGE_START);
            
            this.getContentPane().add(canvas3D, BorderLayout.CENTER);
            simpleU = new SimpleUniverse(canvas3D);
            // Position the view
            TransformGroup viewingPlatformGroup = simpleU.getViewingPlatform().getViewPlatformTransform();
            simpleU.getCanvas().getView().setBackClipDistance(3000.0d);
            Transform3D t3d = new Transform3D();
            t3d.rotX(-Math.PI / 4.0d);
            t3d.setTranslation(new Vector3f(divs / 2, divs, divs / 2));
            viewingPlatformGroup.setTransform(t3d);
            vpTrans = simpleU.getViewingPlatform().getViewPlatformTransform();
            root = createSceneGraph(simpleU);
            scene = new BranchGroup();
            scene.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            scene.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            scene.addChild(root);
            simpleU.addBranchGraph(scene);
            this.setVisible(true);
            
            root = null;
            objRoot = null;
            System.gc();
        }
        catch(Exception e) {parent.amendLog(e.toString());}
    }

    public BranchGroup createSceneGraph(SimpleUniverse su) 
    {
        // The secret to using the triangle strip array is 
        // how the vertices need to be specified.  This is 
        // not documented at all in the Sun documentation 
        // and I was able to figure it out based on a single
        // e-mail in the Java3D mailing list archives.  The 
        // Java3D tutorial has just a few hints on page 2-27 
        // but they didn't really jump out at me.  
        float[][] hf = getHeightField();
        
        parent.setProgressBar(hf.length * 3);
        
        // The height field
        objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_DETACH);
        
        GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_STRIP_ARRAY);
        // The number of vertices (or coordinates) is based on the 
        // number of rows (5) times the number of columns (5)
        // times 3 (one each for x, y, and z values).
        float[] coordinates = new float[hf.length * hf.length * 3]; // No more Point3f!
        
        // Convert the height field to x, y, z values
        for (int row = 0; row < hf.length; row++) {
            for (int col = 0; col < hf[row].length; col++) {
                // coordinate index is the column plus
                // the row times the width of a row times
                // the 3 (one for each x, y, and z).
                int ci = (col + row * hf.length) * 3;
                coordinates[ci + 0] = col; // x
                coordinates[ci + 1] = hf[row][col]; // y
                coordinates[ci + 2] = -row; // z
            }
            parent.increaseProgressBar();
        }
        
        // The number of indices is based on the 
        // number of horizontal strips (height - 1) times the 
        // number of vertices per strip (width * 2).
        int[] indices = new int[(hf.length - 1) * (hf.length * 2)];
        // The secret is that the strip vertices must be ordered 
        // like this: NW, SW, NE, SE for each set of four corners 
        // of a quad.  A convenient way to accomplish this is to 
        // organize the landscape in horizontal strips and iterate 
        // across the columns calculating two vertices at a time. 
        int pi = 0; // points index
        for (int row = 0; row < hf.length - 1; row++) {
            int width = row * hf.length;
            for (int col = 0; col < hf.length; col++) {
                int coordinateIndex = width + col; // coordinate index
                indices[pi + 0] = coordinateIndex + hf.length; //NW
                indices[pi + 1] = coordinateIndex; //SW
                pi = pi + 2;
            }
            parent.increaseProgressBar();
        }
        
        int[] stripCounts = new int[hf.length - 1];
        for (int strip = 0; strip < hf.length - 1; strip++) {
            stripCounts[strip] = hf.length * 2;
            parent.increaseProgressBar();
        }
        
        gi.setStripCounts(stripCounts);
        gi.setCoordinates(coordinates);
        gi.setCoordinateIndices(indices);
        float[] colors = getElevationColors();
        gi.setColors3(colors);
        int[] colorIndices = getElevationColorIndices(hf);
        gi.setColorIndices(colorIndices);
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(gi);
        
        Geometry geometry = gi.getIndexedGeometryArray();
        Shape3D shape = new Shape3D(geometry);
        shape.setAppearance(getAppearance());

        // Add ambient light  
        bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000.0);
        bounds.setRadius(1000);
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new Color3f(1f, 1f, 1f));
        ambient.setInfluencingBounds(bounds);
        //objRoot.addChild(ambient);

        // Add a directional light
        DirectionalLight directional = new DirectionalLight();
        directional.setDirection(0.5f, -0.4f, -1f);
        directional.setColor(new Color3f(1f, 0.7f, 0.7f));
        directional.setInfluencingBounds(bounds);
        //objRoot.addChild(directional);
        
        // Add a directional light2
        DirectionalLight directional2 = new DirectionalLight();
        directional2.setDirection(-0.5f, -0.3f, -0.5f);
        directional2.setColor(new Color3f(1f, 0.7f, 0.7f));
        directional2.setInfluencingBounds(bounds);
        //objRoot.addChild(directional2);
        
        objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        
        BranchGroup b = new BranchGroup();
        b.addChild(shape);
        b.addChild(ambient);
        b.addChild(directional);
        b.addChild(directional2);
        
        objRoot.addChild(objTrans);
        objTrans.addChild(b);
        
        mRot = new MouseRotate();
        //mRot = new Rotator();
        mRot.setTransformGroup(objTrans);
        mRot.setSchedulingBounds(bounds);
        mRot.setFactor(0.005, 0.005);
        objTrans.addChild(mRot);
        
        mTran = new MouseTranslate();
        mTran.setTransformGroup(objTrans);
        mTran.setSchedulingBounds(bounds);
        mTran.setFactor(0.05, 0.05);
        objTrans.addChild(mTran);
        
        mZoom = new MouseZoom();
        mZoom.setTransformGroup(objTrans);
        mZoom.setSchedulingBounds(bounds);
        mZoom.setFactor(0.05);
        objTrans.addChild(mZoom);
        
		keyNavBeh = new KeyNavigatorBehavior(vpTrans);
		keyNavBeh.setSchedulingBounds(bounds);
		keyNavBeh.setCapability(BranchGroup.ALLOW_DETACH);
		objTrans.addChild(keyNavBeh);
        
        // Optimize the scene graph
        objRoot.compile();
        
        parent.resetProgressBar();
        
        hf = null;
        
        return objRoot;
    }
    
    private float[][] getHeightField() {
        // Create a simple 5 by 5 grid representing a height field.
        // Each entry represents an altitude value.
        
        int[][] heightmap = parent.normaliseMap(parent.getHeightMap(), 270);
        
        float[][] hf = new float[divs + 1][divs + 1];
        double step = (double)heightmap.length / (double)hf.length;
        double mult = (128 / (divs * 2));
        mult = Math.max(1, mult);
        
        for(int i = 0; i < hf.length; i++) {
            for(int j = 0; j < hf.length; j++) {
                hf[i][j] = (float) ((float) heightmap[(int)(i * step)][(int)(j * step)] / (10 * mult));
            }
        }
        
        return hf;
    }
    
    private int[] getElevationColorIndices(float[][] hf) {
        int[] indices = new int[divs * (divs + 1) * 2];
        int i = 0;
        for (int row = 0; row < divs; row++) {
            for (int col = 0; col < (divs + 1); col++) {
                // Normalize the height value to a
                // color index between 0 and NUMBER_OF_COLORS - 1
                int nw =
                    Math.round(
                        (27 - 1)
                            * ((100f - (hf[row + 1][col] * 3)) / 100f));
                indices[i] = nw;
                int sw =
                    Math.round(
                        (27 - 1)
                            * ((100f - (hf[row][col] * 3)) / 100f));
                indices[i + 1] = sw;
                i = i + 2;
            }
        }
        return indices;
    }

    private float[] getElevationColors() {
        // These colors were arrived at through experimentation.
        // A color utility I found very useful is called
        // 'La boite a couleurs' by Benjamin Chartier
        //
        float[] colors = new float[3 * 27];
        int i = 0;
        //      
        colors[i++] = 0.72f;
        colors[i++] = 0.59f;
        colors[i++] = 0.44f;
        //      
        colors[i++] = 0.64f;
        colors[i++] = 0.49f;
        colors[i++] = 0.32f;
        //      
        colors[i++] = 0.51f;
        colors[i++] = 0.39f;
        colors[i++] = 0.25f;

        colors[i++] = 0.43f;
        colors[i++] = 0.33f;
        colors[i++] = 0.21f;
        //      
        colors[i++] = 0.38f;
        colors[i++] = 0.29f;
        colors[i++] = 0.18f;
        //      
        colors[i++] = 0.31f;
        colors[i++] = 0.25f;
        colors[i++] = 0.15f;

        colors[i++] = 0.27f;
        colors[i++] = 0.21f;
        colors[i++] = 0.13f;

        colors[i++] = 0.23f;
        colors[i++] = 0.28f;
        colors[i++] = 0.14f;

        //      
        colors[i++] = 0.28f;
        colors[i++] = 0.36f;
        colors[i++] = 0.14f;
        //      
        colors[i++] = 0.23f;
        colors[i++] = 0.35f;
        colors[i++] = 0.11f;
        //      
        colors[i++] = 0.28f;
        colors[i++] = 0.43f;
        colors[i++] = 0.13f;

        colors[i++] = 0.30f;
        colors[i++] = 0.46f;
        colors[i++] = 0.14f;
        //       
        colors[i++] = 0.33f;
        colors[i++] = 0.50f;
        colors[i++] = 0.16f;
        //  
        colors[i++] = 0.35f;
        colors[i++] = 0.53f;
        colors[i++] = 0.17f;

        colors[i++] = 0.38f;
        colors[i++] = 0.58f;
        colors[i++] = 0.18f;

        colors[i++] = 0.43f;
        colors[i++] = 0.66f;
        colors[i++] = 0.20f;
        //      sandy
        colors[i++] = 0.62f;
        colors[i++] = 0.58f;
        colors[i++] = 0.38f;
        //      
        colors[i++] = 0.66f;
        colors[i++] = 0.62f;
        colors[i++] = 0.44f;
        //      
        colors[i++] = 0.70f;
        colors[i++] = 0.67f;
        colors[i++] = 0.50f;
        //      
        colors[i++] = 0.74f;
        colors[i++] = 0.71f;
        colors[i++] = 0.56f;
        //      
        colors[i++] = 0.77f;
        colors[i++] = 0.75f;
        colors[i++] = 0.63f;
        //      blue
        colors[i++] = 0.0f;
        colors[i++] = 0.56f;
        colors[i++] = 0.57f;
        //      
        colors[i++] = 0.0f;
        colors[i++] = 0.38f;
        colors[i++] = 0.54f;
        //      
        colors[i++] = 0.0f;
        colors[i++] = 0.24f;
        colors[i++] = 0.35f;
        //      
        colors[i++] = 0.0f;
        colors[i++] = 0.14f;
        colors[i++] = 0.20f;
        //      
        colors[i++] = 0.0f;
        colors[i++] = 0.07f;
        colors[i++] = 0.10f;
        //      
        colors[i++] = 0.0f;
        colors[i++] = 0.03f;
        colors[i++] = 0.04f;

        return colors;
    }

    private Appearance getAppearance() {
        Appearance appearance = new Appearance();
        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
        polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        // Setting back face normal flip to true allows backfacing
        // polygons to be lit (normal is facing wrong way) but only
        // if the normal is flipped.
        polyAttrib.setBackFaceNormalFlip(true);
        appearance.setPolygonAttributes(polyAttrib);
        Material material = new Material();
        material.setAmbientColor(0f, 0f, 0f);
        // Changing the specular color reduces the shine
        // that occurs with the default setting.
        material.setSpecularColor(0.1f, 0.1f, 0.1f);
        appearance.setMaterial(material);
        return appearance;
    }
    
    public void reset()
    {
        parent.reset3D();
    }
    
    /**
     * ActionListener for components of View2DPanel
     */
    class ViewListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) {
            if(e.getSource().equals(btnReset)) { 
                reset();
            }
        }
    }
}

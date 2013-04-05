import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.behaviors.mouse.*;

/**
 * MouseRotate is a Java3D behavior object that lets users control the 
 * rotation of an object via a mouse.
 * <p>
 * To use this utility, first create a transform group that this 
 * rotate behavior will operate on. Then,
 *<blockquote><pre>
 * 
 *   MouseRotate behavior = new MouseRotate();
 *   behavior.setTransformGroup(objTrans);
 *   objTrans.addChild(behavior);
 *   behavior.setSchedulingBounds(bounds);
 *
 *</pre></blockquote>
 * The above code will add the rotate behavior to the transform
 * group. The user can rotate any object attached to the objTrans.
 */

public class Rotator extends MouseBehavior {
    double x_angle, y_angle;
    double x_factor = .03;
    double y_factor = .03;

    private MouseBehaviorCallback callback = null;

    /**
     * Creates a rotate behavior given the transform group.
     * @param transformGroup The transformGroup to operate on.
     */
    public Rotator(TransformGroup transformGroup) {
	super(transformGroup);
    }

    /**
     * Creates a default mouse rotate behavior.
     **/
    public Rotator() {
	super(0);
    }

    /**
     * Creates a rotate behavior.
     * Note that this behavior still needs a transform
     * group to work on (use setTransformGroup(tg)) and
     * the transform group must add this behavior.
     * @param flags interesting flags (wakeup conditions).
     */
    public Rotator(int flags) {
	super(flags);
    }

    /**
     * Creates a rotate behavior that uses AWT listeners and behavior
     * posts rather than WakeupOnAWTEvent.  The behavior is added to the
     * specified Component. A null component can be passed to specify
     * the behavior should use listeners.  Components can then be added
     * to the behavior with the addListener(Component c) method.
     * @param c The Component to add the MouseListener
     * and MouseMotionListener to.
     * @since Java 3D 1.2.1
     */
    public Rotator(Component c) {
	super(c, 0);
    }

    /**
     * Creates a rotate behavior that uses AWT listeners and behavior
     * posts rather than WakeupOnAWTEvent.  The behaviors is added to
     * the specified Component and works on the given TransformGroup.
     * A null component can be passed to specify the behavior should use
     * listeners.  Components can then be added to the behavior with the
     * addListener(Component c) method.
     * @param c The Component to add the MouseListener and
     * MouseMotionListener to.
     * @param transformGroup The TransformGroup to operate on.
     * @since Java 3D 1.2.1
     */
    public Rotator(Component c, TransformGroup transformGroup) {
	super(c, transformGroup);
    }

    /**
     * Creates a rotate behavior that uses AWT listeners and behavior
     * posts rather than WakeupOnAWTEvent.  The behavior is added to the
     * specified Component.  A null component can be passed to specify
     * the behavior should use listeners.  Components can then be added to
     * the behavior with the addListener(Component c) method.
     * Note that this behavior still needs a transform
     * group to work on (use setTransformGroup(tg)) and the transform
     * group must add this behavior.
     * @param flags interesting flags (wakeup conditions).
     * @since Java 3D 1.2.1
     */
    public Rotator(Component c, int flags) {
	super(c, flags);
    }
    
    public void initialize() {
	super.initialize();
	x_angle = 0;
	y_angle = 0;
	if ((flags & INVERT_INPUT) == INVERT_INPUT) {
	    invert = true;
	    x_factor *= -1;
	    y_factor *= -1;
	}
    }
    
    /**
     * Return the x-axis movement multipler.
     **/
    public double getXFactor() {
	return x_factor;
    }
  
    /**
     * Return the y-axis movement multipler.
     **/
    public double getYFactor() {
	return y_factor;
    }
  

    /**
     * Set the x-axis amd y-axis movement multipler with factor.
     **/
    public void setFactor( double factor) {
	x_factor = y_factor = factor;
    }

    /**
     * Set the x-axis amd y-axis movement multipler with xFactor and yFactor
     * respectively.
     **/
    public void setFactor( double xFactor, double yFactor) {
	x_factor = xFactor;
	y_factor = yFactor;    
    }

    public void processStimulus (Enumeration criteria) {
	WakeupCriterion wakeup;
	AWTEvent[] events;
 	MouseEvent evt;
// 	int id;
// 	int dx, dy;
	
	while (criteria.hasMoreElements()) {
	    wakeup = (WakeupCriterion) criteria.nextElement();
	    if (wakeup instanceof WakeupOnAWTEvent) {
		events = ((WakeupOnAWTEvent)wakeup).getAWTEvent();
		if (events.length > 0) {
		    evt = (MouseEvent) events[events.length-1];
		    doProcess(evt);
		}
	    }

	    else if (wakeup instanceof WakeupOnBehaviorPost) {
		while (true) {
		    // access to the queue must be synchronized
		    synchronized (mouseq) {
			if (mouseq.isEmpty()) break;
			evt = (MouseEvent)mouseq.remove(0);
			// consolidate MOUSE_DRAG events
			while ((evt.getID() == MouseEvent.MOUSE_DRAGGED) &&
			       !mouseq.isEmpty() &&
			       (((MouseEvent)mouseq.get(0)).getID() ==
				MouseEvent.MOUSE_DRAGGED)) {
			    evt = (MouseEvent)mouseq.remove(0);
			}
		    }
		    doProcess(evt);
		}
	    }

	}
	wakeupOn (mouseCriterion);
    }

    void doProcess(MouseEvent evt) {
	int id;
	int dx, dy;

	processMouseEvent(evt);
	if (((buttonPress)&&((flags & MANUAL_WAKEUP) == 0)) ||
	    ((wakeUp)&&((flags & MANUAL_WAKEUP) != 0))) {
	    id = evt.getID();
	    if ((id == MouseEvent.MOUSE_DRAGGED) && 
		!evt.isMetaDown() && ! evt.isAltDown()){
		x = evt.getX();
		y = evt.getY();
		
		dx = x - x_last;
		dy = y - y_last;
		
		if (!reset){	    
		    x_angle = dy * y_factor;
		    y_angle = dx * x_factor;
		    
		    transformX.rotX(x_angle);
		    transformY.rotY(y_angle);
		    
		    transformGroup.getTransform(currXform);
		    
		    Matrix4d mat = new Matrix4d();
		    // Remember old matrix
		    currXform.get(mat);
		    
		    // Translate to origin
		    currXform.setTranslation(new Vector3d(0.0,0.0,0.0));
		    if (invert) {
			currXform.mul(currXform, transformX);
			currXform.mul(currXform, transformY);
		    } else {
			currXform.mul(transformX, currXform);
			currXform.mul(transformY, currXform);
		    }
		    
		    // Set old translation back
		    Vector3d translation = new 
			Vector3d(mat.m03, mat.m13, mat.m23);
		    currXform.setTranslation(translation);
		    
		    // Update xform
		    transformGroup.setTransform(currXform);
		    
		    transformChanged( currXform );
		    
		    if (callback!=null)
			callback.transformChanged( MouseBehaviorCallback.ROTATE,
						   currXform );
		}
		else {
		    reset = false;
		}
		
		x_last = x;
		y_last = y;
	    }
	    else if (id == MouseEvent.MOUSE_PRESSED) {
		x_last = evt.getX();
		y_last = evt.getY();
	    }
	}
    }
    
    /**
     * Users can overload this method  which is called every time
     * the Behavior updates the transform
     *
     * Default implementation does nothing
     */
    public void transformChanged( Transform3D transform ) {
    }


    /**
     * The transformChanged method in the callback class will
     * be called every time the transform is updated
     */
    public void setupCallback( MouseBehaviorCallback callback ) {
	this.callback = callback;
    }
}
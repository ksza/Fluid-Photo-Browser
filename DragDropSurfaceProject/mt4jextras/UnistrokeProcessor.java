package mt4jextras;


import org.mt4j.input.inputData.InputCursor;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputProcessors.IInputProcessor;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.Direction;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.UnistrokeGesture;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.Recognizer;
import org.mt4j.util.math.Vector3D;

import processing.core.PApplet;

public class UnistrokeProcessor extends AbstractCursorProcessor {
	private PApplet pa;
	/** The plane normal. */
	private Vector3D planeNormal;
	/** The point in plane. */
	private Vector3D pointInPlane;
	
	
	private UnistrokeContext context;
	private Recognizer recognizer;
	private UnistrokeUtils du;


	public UnistrokeProcessor(PApplet pa) { 
		super();
		this.pa = pa;
		planeNormal = new Vector3D(0, 0, 1);
		pointInPlane = new Vector3D(0, 0, 0);
		
		this.setLockPriority(1);
		
		du = new UnistrokeUtils();
		recognizer = du.getRecognizer();
	}
	
	public UnistrokeUtils getUnistrokeUtils() {
		return du;
	}
	
	public void addTemplate(UnistrokeGesture gesture, Direction direction){
		recognizer.addTemplate(gesture, direction);
	}
	
	@Override
	public void cursorStarted(InputCursor inputCursor, MTFingerInputEvt currentEvent) {
		if (this.canLock(inputCursor)) {
			context = new UnistrokeContext(pa, planeNormal, pointInPlane, inputCursor, recognizer, du, inputCursor.getTarget());
			if (!context.gestureAborted) {
				this.getLock(inputCursor);
				context.update(inputCursor);
				
				//FIXME ?? 3 times? REMOVE?
				context.update(inputCursor);
				context.update(inputCursor);
				context.update(inputCursor);
				
				this.fireGestureEvent(new UnistrokeEvent(this, MTGestureEvent.GESTURE_DETECTED, inputCursor.getCurrentTarget(), context.getVisualizer(), UnistrokeGesture.NOGESTURE));
			}
		}

	}

	
	
	@Override
	public void cursorUpdated(InputCursor inputCursor, MTFingerInputEvt currentEvent) {
		if (getLockedCursors().contains(inputCursor) && context != null) {
			if (!context.gestureAborted) {
				context.update(inputCursor);
				this.fireGestureEvent(new UnistrokeEvent(this, MTGestureEvent.GESTURE_UPDATED, inputCursor.getCurrentTarget(), context.getVisualizer(), UnistrokeGesture.NOGESTURE));
			}
		}

	}

	
	/* (non-Javadoc)
	 * @see org.mt4j.input.inputProcessors.componentProcessors.AbstractCursorProcessor#cursorEnded(org.mt4j.input.inputData.InputCursor, org.mt4j.input.inputData.MTFingerInputEvt)
	 */
	@Override
	public void cursorEnded(InputCursor inputCursor, MTFingerInputEvt currentEvent) {
		if (getLockedCursors().contains(inputCursor) && context != null) {
			this.fireGestureEvent(new UnistrokeEvent(this, MTGestureEvent.GESTURE_ENDED, inputCursor.getCurrentTarget(), context.getVisualizer(), context.recognizeGesture()));
			
			context.getVisualizer().destroy();
			this.unLock(inputCursor);
			context = null;
		}
	}

	
	
	@Override
	public void cursorLocked(InputCursor cursor, IInputProcessor lockingprocessor) {
		if (getLockedCursors().contains(cursor) && context != null) {
			this.fireGestureEvent(new UnistrokeEvent(this, MTGestureEvent.GESTURE_ENDED, cursor.getCurrentTarget(), context.getVisualizer(), UnistrokeGesture.NOGESTURE));
		}

	}

	@Override
	public void cursorUnlocked(InputCursor cursor) {

	}

	
	@Override
	public String getName() {
		return "MTDollarGesture Processor";
	}

	
}

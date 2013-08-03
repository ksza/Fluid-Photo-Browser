package itu.spvc.template;

import itu.spvc.vect.SerializableVector3D;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.shapes.MTEllipse;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.shapes.MTRoundRectangle;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.IMTInputEventListener;
import org.mt4j.input.inputData.MTFingerInputEvt;
import org.mt4j.input.inputData.MTInputEvent;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.input.inputProcessors.globalProcessors.RawFingerProcessor;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;

public class CreateTemplateScene extends AbstractScene {
	MTApplication mtApp=null;

	//ArrayList for holding the touch points that make up a custom gesture
	private ArrayList<SerializableVector3D> points;

	private final MTRoundRectangle gestureRecordingArea;
	
	private final MTRectangle startStopButton;
	boolean started = false; // if true, the user started to register the gesture; if false, the user finished registering the gesture
	private final MTRectangle clearButton;
	private final MTRectangle saveButton;

	final RawFingerProcessor rfp = new RawFingerProcessor();
	
	public CreateTemplateScene (MTApplication mtApplication, String name) {
		super(mtApplication, name);
		mtApp = mtApplication;
		
		gestureRecordingArea = new MTRoundRectangle(0, 0, 0, 300, 300, 10, 10, mtApp);
		startStopButton = new MTRectangle(200, 60, mtApp);
		clearButton = new MTRectangle(200, 60, mtApp);
		saveButton = new MTRectangle(200, 60, mtApp);
	}

	/**
	 * Draw a scene to record custom gestures
	 */
	public void initDefineGesture() {
		//Area within to define gestures
		gestureRecordingArea.setNoFill(true);
		gestureRecordingArea.removeAllGestureEventListeners();
		this.getCanvas().addChild(gestureRecordingArea);
		
		MTColor white = new MTColor(255,255,255);
		//Show touches
		this.registerGlobalInputProcessor(new CursorTracer(mtApp, this));
		//Add text to the drag-drop area
		IFont fontArial = FontManager.getInstance().createFont(mtApp, "arial.ttf", 
				20, 	//Font size
				white,  //Font fill color
				white);	//Font outline color
		
		/* create the start/stop button */
		final MTTextArea startText = new MTTextArea(mtApp, fontArial);
		startText.setText("Start recording");
		startText.setNoStroke(true);
		startText.setNoFill(true);
		startText.setPositionRelativeToOther(startStopButton, startStopButton.getCenterPointGlobal());
		startText.removeAllGestureEventListeners();
		startStopButton.addChild(startText);
		startStopButton.setFillColor(new MTColor(146, 150, 188, 255));
		
		Vector3D startButtonPosition = gestureRecordingArea.getCenterPointGlobal();
		startButtonPosition.x = 100;
		startButtonPosition.y += 200;
		startStopButton.setPositionRelativeToOther(gestureRecordingArea, startButtonPosition);
		this.getCanvas().addChild(startStopButton);		
		
		/* create the clear button */
		final MTTextArea clearText = new MTTextArea(mtApp, fontArial);
		clearText.setText("Clear");
		clearText.setNoStroke(true);
		clearText.setNoFill(true);
		clearText.setPositionRelativeToOther(clearButton, clearButton.getCenterPointGlobal());
		clearText.removeAllGestureEventListeners();
		clearButton.addChild(clearText);
		clearButton.setFillColor(new MTColor(146, 150, 188, 255));
		
		Vector3D clearButtonPosition = gestureRecordingArea.getCenterPointGlobal();
		clearButtonPosition.x = 400;
		clearButtonPosition.y += 200;
		clearButton.setPositionRelativeToOther(gestureRecordingArea, clearButtonPosition);
		this.getCanvas().addChild(clearButton);	
		
		/* create the save button */
		final MTTextArea saveText = new MTTextArea(mtApp, fontArial);
		saveText.setText("Save");
		saveText.setNoStroke(true);
		saveText.setNoFill(true);
		saveText.setPositionRelativeToOther(saveButton, saveButton.getCenterPointGlobal());
		saveText.removeAllGestureEventListeners();
		saveButton.addChild(saveText);
		saveButton.setFillColor(new MTColor(146, 150, 188, 255));
		
		Vector3D saveButtonPosition = gestureRecordingArea.getCenterPointGlobal();
		saveButtonPosition.x = 700;
		saveButtonPosition.y += 200;
		saveButton.setPositionRelativeToOther(gestureRecordingArea, saveButtonPosition);
		this.getCanvas().addChild(saveButton);	
		
		/* add tap functionality to the start/stop button */
		startStopButton.registerInputProcessor(new TapProcessor(mtApp));
		startStopButton.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				if(ge.getId() == MTGestureEvent.GESTURE_DETECTED) {
					startStopButton.setFillColor(MTColor.AQUA);
				} else if(ge.getId() == MTGestureEvent.GESTURE_ENDED) {
					started = ! started;
					
					startStopButton.setFillColor(new MTColor(146, 150, 188, 255));
					
					if(started) {
						startText.setText("Stop Recording");
						addRawFingerProcessor(gestureRecordingArea, rfp);
					} else {
						startText.setText("Start Recording");
						removeRawFingerProcessor(gestureRecordingArea, rfp);
						// also clear recorded points
						points = new ArrayList<SerializableVector3D>();
						gestureRecordingArea.removeAllChildren();
					}
				}
				
				return true;
			}
		});
		
		/* add tap functionality to the clear button */
		clearButton.registerInputProcessor(new TapProcessor(mtApp));
		clearButton.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				if(ge.getId() == MTGestureEvent.GESTURE_DETECTED) {
					clearButton.setFillColor(MTColor.AQUA);
				} else if(ge.getId() == MTGestureEvent.GESTURE_ENDED) {
					clearButton.setFillColor(new MTColor(146, 150, 188, 255));
					points = new ArrayList<SerializableVector3D>();
					gestureRecordingArea.removeAllChildren();
				}
				
				return true;
			}
		});
		
		/* add tap functionality to the save button */
		saveButton.registerInputProcessor(new TapProcessor(mtApp));
		saveButton.addGestureListener(TapProcessor.class, new IGestureEventListener() {
			
			@Override
			public boolean processGestureEvent(MTGestureEvent ge) {
				if(ge.getId() == MTGestureEvent.GESTURE_DETECTED) {
					saveButton.setFillColor(MTColor.AQUA);
				} else if(ge.getId() == MTGestureEvent.GESTURE_ENDED) {
					saveButton.setFillColor(new MTColor(146, 150, 188, 255));
					
					try {
						ObjectOutputStream objectOut = new ObjectOutputStream(new FileOutputStream("templates/custom.ges"));
						objectOut.writeObject(points);
						objectOut.flush();
						objectOut.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				return true;
			}
		});
	}

	/**
	 * Add a raw finger processor to get raw data for user-defined gestures
	 * 
	 * <note>The RawFingerProcessor cannot be added to an MTComponent, so we add it
	 * to the scene and check if touches are within a specified component before saving
	 * the touch coordinates</note>
	 * 
	 * @param rectComp, the component that gestures should be defined within
	 * @param rfp, a raw finger processor
	 */
	private void addRawFingerProcessor(MTRoundRectangle comp, RawFingerProcessor rfp) {
		final MTRoundRectangle rectComp = comp;
		points = new ArrayList<SerializableVector3D>();
		
		rfp.addProcessorListener(new IMTInputEventListener() {
			
			@Override
			public boolean processInputEvent(MTInputEvent inEvt) {
				//TODO save the points 
				
				if(inEvt instanceof MTFingerInputEvt) {
					MTFingerInputEvt fingerEvt = (MTFingerInputEvt)inEvt;
					
					if(inEvt.hasTarget() && inEvt.getTarget().equals(gestureRecordingArea)) {
						points.add(new SerializableVector3D(fingerEvt.getPosition()));
						System.out.println(fingerEvt.getPosition());
						
						MTEllipse pointRepresentation = new MTEllipse(mtApp, fingerEvt.getPosition(), 5, 5);
						pointRepresentation.setFillColor(MTColor.YELLOW);
						rectComp.addChild(pointRepresentation);
					}
					
				}

				return true;
			}
		});
		this.registerGlobalInputProcessor(rfp);
	}

	/**
	 * Remove raw finger processor
	 * @param comp, the component to remove the processor from
	 * @param rfp, the processor to remove
	 */
	private void removeRawFingerProcessor(MTRoundRectangle comp, RawFingerProcessor rfp) {
		this.unregisterGlobalInputProcessor(rfp);
	}

	@Override
	public void init() {
		initDefineGesture();		
	}
	@Override
	public void shutDown() {
		// TODO Auto-generated method stub

	}
}

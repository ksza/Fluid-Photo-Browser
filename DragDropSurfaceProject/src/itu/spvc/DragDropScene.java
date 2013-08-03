package itu.spvc;

import java.awt.Image;
import java.awt.MediaTracker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mt4j.MTApplication;
import org.mt4j.components.visibleComponents.font.FontManager;
import org.mt4j.components.visibleComponents.font.IFont;
import org.mt4j.components.visibleComponents.shapes.MTRectangle;
import org.mt4j.components.visibleComponents.widgets.MTImage;
import org.mt4j.components.visibleComponents.widgets.MTTextArea;
import org.mt4j.input.gestureAction.DefaultDragAction;
import org.mt4j.input.gestureAction.DefaultRotateAction;
import org.mt4j.input.gestureAction.DefaultScaleAction;
import org.mt4j.input.inputProcessors.IGestureEventListener;
import org.mt4j.input.inputProcessors.MTGestureEvent;
import org.mt4j.input.inputProcessors.componentProcessors.AbstractComponentProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.rotateProcessor.RotateProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.scaleProcessor.ScaleProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.tapAndHoldProcessor.TapAndHoldProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeEvent;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeProcessor;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.Direction;
import org.mt4j.input.inputProcessors.componentProcessors.unistrokeProcessor.UnistrokeUtils.UnistrokeGesture;
import org.mt4j.input.inputProcessors.globalProcessors.CursorTracer;
import org.mt4j.sceneManagement.AbstractScene;
import org.mt4j.sceneManagement.AddNodeActionThreadSafe;
import org.mt4j.util.MTColor;
import org.mt4j.util.math.Vector3D;

import processing.core.PImage;

public class DragDropScene extends AbstractScene {
	//The multitouch application 
	private MTApplication mtApp;

	//Foldername where images are located 
	private String imageFolder = "images/";

	//For dispatching unistroke events
	private UnistrokeProcessor usp;

	//List of displayed images
	private ArrayList<MTImage> images;

	private Set<MTImageWithPath> imagesInDragDropZone = Collections.synchronizedSet(new HashSet<MTImageWithPath>());

	//Store the last image added
	private MTImage lastImageAdded;

	private MTRectangle dragDropZone;

	private Server server;

	private MTTextArea modeTextArea;

	/* if true -> we are in imageHandlingMode (move, rotate, scale); is false -> we are in img. sending mode */
	private boolean imageHandlingMode = true;

	/**
	 * Constructor
	 * @param mtApplication, mt application 
	 * @param name of application
	 */
	public DragDropScene(MTApplication mtApplication, String name) {
		super(mtApplication, name);
		this.mtApp = mtApplication;
		images = new ArrayList<MTImage>();

		//Set scene background color
		this.setClearColor(new MTColor(146, 150, 188, 255));

		//Add mouse input
		this.registerGlobalInputProcessor(new CursorTracer(mtApp, this));

		//Start new server
		this.server = new Server(this);
	}


	/**
	 * Draw a scene with images, drag and drop area and relevant processors and listeners
	 */
	private void initImages() {
		//Remove previous components
		this.getCanvas().removeAllChildren();

		dragDropZone = new MTRectangle(mtApp.getWidth() * 0.5f, mtApp.getHeight(), mtApp);
		dragDropZone.setNoFill(true);		
		this.getCanvas().addChild(dragDropZone);
		
		//Add images
		File imagesDir= new File(imageFolder);
		if(imagesDir.isDirectory()) {
			System.out.println("Images dir found !");

			for(final File imgFile: imagesDir.listFiles()) {
				if(imgFile.isFile() && ! imgFile.getName().equals("Thumbs.db")) {
					PImage loadImage = mtApp.loadImage(imgFile.getPath());
					final MTImageWithPath imageComponent = new MTImageWithPath(loadImage, mtApp, imgFile.getPath());
					Vector3D relativePosition = dragDropZone.getCenterPointGlobal();
					relativePosition.x += 700;
					imageComponent.setPositionRelativeToOther(dragDropZone, relativePosition);
					this.getCanvas().addChild(imageComponent);

					imageComponent.addGestureListener(DragProcessor.class, new IGestureEventListener() {

						@Override
						public boolean processGestureEvent(MTGestureEvent ge) {
							if(ge.getId() == MTGestureEvent.GESTURE_ENDED ) {

								if(dragDropZone.containsPointGlobal(imageComponent.getCenterPointGlobal())) {
									imagesInDragDropZone.add(imageComponent);
								} else {
									imagesInDragDropZone.remove(imageComponent);
								}

							}

							return false;
						}
					});
					//					addUnistrokeProcessor(imageComponent, imgFile.getPath());

					images.add(imageComponent);
					lastImageAdded = imageComponent;
				}
			}
		}

		MTColor white = new MTColor(255,255,255);
		this.setClearColor(new MTColor(146, 150, 188, 255));
		//Show touches
		this.registerGlobalInputProcessor(new CursorTracer(mtApp, this));
		//Add text to the drag-drop area
		IFont fontArial = FontManager.getInstance().createFont(mtApp, "arial.ttf", 
				50, 	//Font size
				white,  //Font fill color
				white);	//Font outline color
		MTTextArea text = new MTTextArea(mtApp, fontArial);
		text.setText("Drag and Drop");
		text.setNoStroke(true);
		text.setNoFill(true);
		text.setPositionRelativeToOther(dragDropZone, dragDropZone.getCenterPointGlobal());
		text.removeAllGestureEventListeners();
		dragDropZone.addChild(text);

		modeTextArea = new MTTextArea(mtApp, fontArial);
		modeTextArea.setText("ImageHandling Mode");
		modeTextArea.setNoStroke(true);
		modeTextArea.setNoFill(true);
		modeTextArea.removeAllGestureEventListeners();
		dragDropZone.addChild(modeTextArea);

		dragDropZone.removeAllGestureEventListeners();

		TapAndHoldProcessor tahp = new TapAndHoldProcessor(mtApp);
		tahp.setMaxFingerUpDist(1000);
		dragDropZone.registerInputProcessor(tahp);
		dragDropZone.addGestureListener(TapAndHoldProcessor.class, new IGestureEventListener() {
			@Override
			/**
			 * Check if a finger is down in the drag and drop zone
			 */
			public boolean processGestureEvent(MTGestureEvent ge) {
				// use the id of the gesture event to identify 
				// if the finger was just pressed (MTGestureEvent.GESTURE_DETECTED)
				// or if the finger was just released (MTGestureEvent.GESTURE_ENDED)
				if(ge.getId() == MTGestureEvent.GESTURE_DETECTED) {
					changeMode();
				}

				return false;
			}
		});
	}


	/**
	 * init the gesture processors
	 */
	public void initGestureProcessors(ArrayList<Vector3D> points) {

		//Define unistroke processor
		usp = new UnistrokeProcessor(mtApp);

		//Add templates to process by the unistroke processor
		//		usp.addTemplate(UnistrokeGesture.V, Direction.CLOCKWISE);


		//If we have defined a gesture we add it
		if(points != null && points.size() > 0) {
			//TODO: update MT4J code before going further
			System.out.println("$$ Registered custom gesture!");
			usp.getUnistrokeUtils().getRecognizer().addTemplate(UnistrokeGesture.CUSTOMGESTURE, points, Direction.CLOCKWISE);
		}
	}


	/**
	 * Register a unistroke processor and listen for gesture events. The templates to match gestures against
	 * are the ones we specified the 'initImages()'method
	 * @param mti, the image in the drag and drop zone
	 */
	private void addUnistrokeProcessor(final MTImage mti, final String imagePath) {
		System.out.println("$$ added unis");

		mti.registerInputProcessor(usp);
		mti.addGestureListener(UnistrokeProcessor.class, new IGestureEventListener() {
			@Override
			/**
			 * If the gesture is being recognized and a finger is pressed in the send the image
			 */
			public boolean processGestureEvent(MTGestureEvent ge) {
				UnistrokeEvent ust = (UnistrokeEvent)ge;
				if(ust.hasTarget() && ust.getId() == MTGestureEvent.GESTURE_ENDED && dragDropZone.containsPointGlobal(mti.getCenterPointGlobal()) && ust.getGesture().equals(UnistrokeGesture.CUSTOMGESTURE)) {
					//TODO: Send the image using the server

					try {
						System.out.println("1 Sending the image ..." + imagePath);

						if(imagePath != null) {
							server.sendImage(imagePath);
							System.out.println("2 Sent the image ...");
						} 
					} catch (IOException e) {
						e.printStackTrace();
					}

					//					System.out.println("$$ GESTURE RECOGNIZED !!");

				}
				return true;
			}
		});
	}

	/**
	 * Remove the unistroke processor and listener when the image leaves the drag and drop area
	 * @param mti, the image leaving the drag and drop zone
	 */
	private void removeUnistrokeProcessor(MTImage mti) {
		System.out.println("@@ removed");

		mti.removeAllGestureEventListeners(UnistrokeProcessor.class);
		mti.removeInputListener(usp);
		mti.unregisterInputProcessor(usp);
	}

	public void changeMode() {
		imageHandlingMode = ! imageHandlingMode;

		if(imageHandlingMode) {
			modeTextArea.setText("ImageHandling Mode");
			setFingerUp();
		} else {
			modeTextArea.setText("ImageSending Mode");
			setFingerDown();
		}
	}

	/**
	 * Called when a touch is registered in the drag and drop area. A unistroke processor is added to 
	 * and move/scale and resize processors are removed from all images in the area so unistroke gestures 
	 * can be detected without the images moving.
	 */
	private void setFingerDown() {
		for(MTImageWithPath image: imagesInDragDropZone) {
			image.removeAllGestureEventListeners(DragProcessor.class);
			image.removeAllGestureEventListeners(RotateProcessor.class);
			image.removeAllGestureEventListeners(ScaleProcessor.class);

			boolean procAlreadyRegistered = false;
			for(AbstractComponentProcessor proc: image.getInputProcessors()) {
				if(proc.getClass().equals(UnistrokeProcessor.class)) {
					procAlreadyRegistered = true;
					break;
				}
			}

			if(! procAlreadyRegistered) {
				addUnistrokeProcessor(image, image.getWrappedImagePath());
			}
		}
	}

	/**
	 * When no finger is down in the drag and drop zone we remove the unistroke processor and
	 * add default processors to images again
	 */
	private void setFingerUp() {
		for(MTImageWithPath image: imagesInDragDropZone) {
			image.addGestureListener(RotateProcessor.class, new DefaultRotateAction());
			image.addGestureListener(ScaleProcessor.class, new DefaultScaleAction());
			image.addGestureListener(DragProcessor.class, new DefaultDragAction());

			removeUnistrokeProcessor(image);
		}

	}

	public void addImage(Image awtImage) {
		System.out.println("Adding image ....");

		PImage img = loadImageMT(awtImage);
		MTImage mti1 = new MTImage(img, mtApp);
		mti1.setFillColor(randomMTColor());

		// sets the last image
		lastImageAdded = mti1;

		//Add the downloaded image 
		this.registerPreDrawAction(new AddNodeActionThreadSafe(mti1, this.getCanvas()));

		dragDropZone.addChild(mti1);
	}

	private MTColor randomMTColor() {
		return MTColor.randomColor();
	}


	public void moveImage(float x, float y) {	
		//the the w/h of the window
		int w = mtApp.getWidth();
		int h = mtApp.getHeight();

		//tx and ty identifies where the image should be translated
		float tx,ty;

		tx = x*w;
		ty = y*h;
		System.out.println("translate " + lastImageAdded + " to " + tx + ", " + ty);

		//get the center of the image
		Vector3D imgPos = lastImageAdded.getCenterPointGlobal();

		//translate the new coords in the image space
		tx -= imgPos.x;
		ty -= imgPos.y;

		//get the length of the vector, i.e. how much the image should move

		System.out.println("translate " + lastImageAdded + " to local coords: " + tx + ", " + ty);

		lastImageAdded.translate(new Vector3D(tx,ty));
		mtApp.repaint();
	}

	/**
	 * Transform a basic java image into a Processing image
	 * uses the MediaTracker to load it. 
	 * @param awtImage basic java image
	 * @return the same image as a Processing image object
	 */
	private PImage loadImageMT(Image awtImage) {
		MediaTracker tracker = new MediaTracker(mtApp);
		tracker.addImage(awtImage, 0);
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			//e.printStackTrace();  // non-fatal, right?
		}

		PImage image = new PImage(awtImage);
		return image;
	}


	/**
	 * Start drawing the scene
	 */
	@Override
	public void init(){
		initImages();
	};

	@Override
	public void shutDown() {}
}

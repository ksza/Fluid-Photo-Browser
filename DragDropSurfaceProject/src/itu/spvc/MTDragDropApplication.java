package itu.spvc;

import itu.spvc.vect.SerializableVector3D;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.mt4j.MTApplication;
import org.mt4j.input.inputSources.Win7NativeTouchSource;
import org.mt4j.util.MT4jSettings;

public class MTDragDropApplication extends MTApplication{

	private static final long serialVersionUID = 1L;

	public MTDragDropApplication() {
		
	}
	
	public static void main(String[] args) {
		MT4jSettings.getInstance().fullscreen = true;
		initialize();
	}
	
	@Override
	public void startUp() {
		// uncomment the following line if you have multitouch apple trackpad
		//getInputManager().registerInputSource(new MacTrackpadSource(this));
		
		getInputManager().registerInputSource(new Win7NativeTouchSource(this));
		
		// Create the basic Drag and drop scene
		addDragDropScene();
	}

	@SuppressWarnings("unchecked")
	public void addDragDropScene() {
		DragDropScene ddScene = new DragDropScene(this, "Multitouch drag & drop");
		try {
			ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream("templates/custom.ges"));
			ArrayList<SerializableVector3D> v = (ArrayList<SerializableVector3D>)objectIn.readObject();
			ddScene.initGestureProcessors(SerializableVector3D.fromSerializableArray(v));
			objectIn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// We add the new Drag and Drop scene to the application
		addScene(ddScene);
	}
}

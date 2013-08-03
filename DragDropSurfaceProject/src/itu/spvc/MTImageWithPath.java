package itu.spvc;

import org.mt4j.components.visibleComponents.widgets.MTImage;

import processing.core.PApplet;
import processing.core.PImage;

public class MTImageWithPath extends MTImage {

	private final String wrappedImagePath;

	public MTImageWithPath(PImage texture, PApplet pApplet, String wrappedImagePath) {
		super(texture, pApplet);
		
		this.wrappedImagePath = wrappedImagePath;
	}
	
	public String getWrappedImagePath() {
		return wrappedImagePath;
	}

}

package itu.spvc.template;

import org.mt4j.MTApplication;
import org.mt4j.input.inputSources.Win7NativeTouchSource;

public class MTTemplateCreatorApplication extends MTApplication {

	private static final long serialVersionUID = 1L;
	private CreateTemplateScene templateCreatorScene = null; 

	public static void main(String[] args) {
		initialize();
	}

	@Override
	public void startUp() {
		getInputManager().registerInputSource(new Win7NativeTouchSource(this));

		// Create the template creator scene
		templateCreatorScene = new CreateTemplateScene(this, "Template creator");
		addScene(templateCreatorScene);
	}

}

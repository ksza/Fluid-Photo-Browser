package dk.itu.android.dragdrop;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Gallery;
import dk.itu.android.dragdrop.AndroidClient.OnCoords;
import dk.itu.android.dragdrop.AndroidClient.OnImage;
import dk.itu.android.dragdrop.AndroidClient.OnResult;

/**
 * Entry point of the application.
 * This activity displays a frame containing the selected image and a gallery with the available images.
 * @author frza
 *
 */
public class DragDropActivity extends Activity {
	static final String TAG = "DRAGDROP_ACTIVITY";

	/**
	 * IP of the server. Obtained from the SharedPreferences of this application
	 */
	String serverIp;
	/**
	 * port of the server. Obtained from the SharedPreferences of this application
	 */
	int serverPort;

	/**
	 * The network client. Used to send images and coordinates
	 */
	AndroidClient client;

	/**
	 * The gallery with the available images
	 */
	Gallery gallery;
	/**
	 * The frame layout where to put the selected image
	 */
	FrameLayout frame;
	/**
	 * Custom view that visualize the image and
	 * handle basic gestures
	 */
	PlayAreaView dragged = null;
	/**
	 * Menu items associated with the activity to modify preferences and to exit (hide, really) the application
	 */
	MenuItem preferences,exit;

	NewImageDialog imagesDialog = null;

	boolean playViewFull = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//we don't want title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
	}

	/**
	 * onStart is called every time the activity is shown (so, the first time and after returning from a "pause")
	 */
	@Override
	protected void onStart() {
		super.onStart();

		//get the shared preferences object. The name is defined in PreferencesActivity.PREFS_NAME
		SharedPreferences prefs = getSharedPreferences(PreferencesActivity.PREFS_NAME, MODE_PRIVATE);

		//if the preferences are not setted, start the PreferencesActivity activity
		if (! prefs.contains("server_ip")) {
			startActivity(new Intent(this, PreferencesActivity.class));			
		} else {
			//otherwise, get the server ip and port, and initialize this activity
			serverIp = prefs.getString("server_ip", "localhost");
			serverPort = prefs.getInt("server_port", 0);
			initDragDrop();
		}
	}

	/**
	 * Called when the activity needs to create its own menu.
	 * Create here the "preferences" and "exit" menu items,
	 * by calling menu.add( <some string> ).
	 * Return true to display the menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		preferences = menu.add(R.string.preferences);
		exit = menu.add(R.string.exit);

		return true;
	}

	/**
	 * Called when a menu item is selected by the user.
	 * Handle here the "preferences" and "exit" menu item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item == preferences) {
			startActivity(new Intent(this, PreferencesActivity.class));
		} else {
			this.finish();
		}
		return true;
	}

	/**
	 * Called when an activity is requested to stop.
	 * Close the network client
	 */
	@Override
	protected void onStop() {
		super.onStop();
		try{ 
			client.close(); 
		} catch(Exception ignored) { }
	}

	/**
	 * Fill the frame and gallery fields.
	 * Set the gallery adapter (use the LolImageAdapter for the exercises).
	 * Set up a gesture listener, a GalleryImageThrownDetector, to call 
	 * "setDragged" when an image is "thrown" by the user.
	 * Create the network client
	 */
	private void initDragDrop() {
		frame = (FrameLayout)findViewById(R.id.Frame);
		gallery = (Gallery)findViewById(R.id.Gallery);

		gallery.setAdapter(new LolImageAdapter(this));

		new GalleryImageThrownDetector(this);
	}

	/**
	 * Called when an image is "thrown" by the user.
	 * Since the server accepts one image at a time,
	 * if the "dragged" field is already filled, ignore the call.
	 * Otherwise, send the image to the server using the network client,
	 * and create a PlayAreaView that displays the image itself.
	 * For the exercises, the image is static and the resource id can be
	 * found in the "imageView.resourceId" field.
	 * @param imageView
	 */
	public void setDragged( final FileAwareImageView imageView ) {
		if(dragged == null) {
			try {
				dragged = new PlayAreaView(this, imageView.resourceId, 100f, 100f);
				if(client == null) {
					client = new AndroidClient(serverIp, serverPort,
							new OnImage() {

						@Override
						public void onImage(final byte[] imageBytes) {
							Log.i("IMAGE_RECEIVER","recevied image of length: " + imageBytes.length);

							DragDropActivity.this.runOnUiThread(new Runnable(){
								@Override
								public void run() {
									if(imagesDialog == null || (imagesDialog != null && ! imagesDialog.isShowing())) {
										imagesDialog = new NewImageDialog(DragDropActivity.this);
										imagesDialog.show();
										Log.i("IMAGE_RECEIVER","before add 1");
										imagesDialog.addImage(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
									} else {
										Log.i("IMAGE_RECEIVER","before add 2");
										imagesDialog.addImage(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
									}
								}
							});
							
							Log.i("IMAGE_RECEIVER","OREVOIRE");
						}
					},   new OnCoords() {

						@Override
						public void onCoords(float x, float y) {
							Log.i("EMPTY_COORDSCALLBACK","received coords: " + x + ", " + y);									
						}
					}
					);
				}
				client.sendImage(imageView.getImageInputStream(), new OnResult<Boolean>() {
					@Override
					public void onResult(Boolean res) {
						Log.d(TAG, "image uploaded? " + res);
						if(res) {
							playViewFull = true;
							frame.addView(dragged);
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				dragged = null;
			}
		}
	}

	/**
	 * Called when the "dragged" image is moved in the screen.
	 * Send the coordinates to the network client
	 * @param x
	 * @param y
	 */
	public void imageMoved(float x, float y) {
		client.sendCoordinates(x, y); //well, here we don't really care too much about when the coords are received
	}

	/**
	 * Called when the user performs the gesture to "clean" the application.
	 * If the "dragged" field is not null, remove all the views from the "frame" field,
	 * and set "dragged" to null. Close also the network client
	 */
	public void resetDragDrop() {
		playViewFull = false;

		if(dragged != null) {
			frame.removeAllViews();
			dragged = null;
		}
		try {
			client.close();
		} catch(Exception ignored){} finally { client = null; }
	}
}
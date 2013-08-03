package dk.itu.android.dragdrop;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/** Class Must extends with Dialog */
/** Implement onClickListener to dismiss dialog when OK Button is pressed */
public class NewImageDialog extends Dialog implements OnClickListener {
	Button okButton;

	public NewImageDialog(Context context) {
		super(context);

		setTitle("New images received");
		/** Design the dialog in main.xml file */
		setContentView(R.layout.dialog);

		Log.i("DIALOG_CUSTOM", "CREATING");
	}

	public void addImage(final Bitmap bmp) {
		Log.i("DIALOG_CUSTOM", "ADDING NEW IMAGE ...");
		ImageView image = new ImageView(this.getContext());
		image.setImageBitmap(bmp);
		((LinearLayout) findViewById(R.id.dialogContainer)).addView(image);
	}
	
	@Override
	public void onClick(View v) {
		dismiss();
	}
}


package com.cisa.pp.callback;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.cisa.app.utils.Utils;

public class MyPictureCallback implements PictureCallback {
	private Activity context;
	private String imageFileName;
	private String imagePath;
	private File pictureFileDir = getDir();
	public MyPictureCallback(Activity context) {
		this.context = context;
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
			Log.d(Utils.TAG_DIRECTORY, "Not Enough permissions to create a Directory");
			Toast.makeText(context, "Unable to create a Directory",Toast.LENGTH_LONG).show();
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymddhhmmss");
			String date = dateFormat.format(new Date());
			imageFileName = "coia_" + date + ".png";
			imagePath = pictureFileDir.getPath() + File.separator + imageFileName;
			File pictureFile = new File(imagePath);
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				camera.stopPreview();
				//Toast.makeText(context, "New Image Saved: " + imageFileName, Toast.LENGTH_LONG).show();
				Utils.setImage(imagePath);
			//	ImageBean bean = ImageBean.getImageBean();
			//	bean.setImagePath(imagePath);
			} catch (Exception e) {
				Toast.makeText(context, "Failed to save image", Toast.LENGTH_LONG).show();
			}
		}
	}
	protected File getDir() {
		File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, Utils.IMAGE_DIRECTORY_NAME);
	}
}
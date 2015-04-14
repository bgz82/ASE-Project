package com.cisa.app;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cisa.app.R;
import com.cisa.app.bean.Analysis;
import com.cisa.app.bean.Boundary;
import com.cisa.app.bean.Region;
import com.cisa.app.bean.Scene;
import com.cisa.app.services.GeoLocationService;
import com.cisa.app.services.HttpAsyncService;
import com.cisa.app.utils.Utils;
import com.cisa.app.views.Preview;
import com.cisa.pp.callback.MyPictureCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class ObserverActivity extends Activity {

	private Preview mPreview;
	private Camera mCamera;
	private EditText e_comment = null;
	private GeoLocationService geoLocService;
	private TextView latTv;
	private TextView lonTv;
	private TextView capture_status;
	private Button image_capture;
	private ImageView imageLoadedView;
	private GoogleMap mMap = null;
	private LatLng location = null;
	private Intent intent = null;
	private Scene scene = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.observer);
		scene = new Scene();

		/* Initializing all elements */
		e_comment = (EditText) findViewById(R.id.comment);
		latTv = (TextView) findViewById(R.id.lat);
		lonTv = (TextView) findViewById(R.id.lon);
		capture_status = (TextView) findViewById(R.id.capture_status);
		image_capture = (Button) findViewById(R.id.image_capture);

		/* Adding default current Location */
		addGeoLocation();

		/* Getting camera instance */
		mCamera = getCameraInstance();
		mPreview = new Preview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

		/* Intent that recieves data from other Application like gallery */
		intent = getIntent();
		imageLoadedView = (ImageView) findViewById(R.id.imageLoaded);
		Uri streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

		if (streamUri != null) {
			imageLoadedView.setVisibility(View.VISIBLE);
			image_capture.setVisibility(View.INVISIBLE);
			ContentResolver cr = getContentResolver();
			InputStream is;
			try {
				is = cr.openInputStream(streamUri);
				System.out.println("path: " + streamUri);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				Toast.makeText(getApplicationContext(),
						getRealPathFromURI(getApplicationContext(), streamUri),
						Toast.LENGTH_LONG).show();
				Utils.setImage(getRealPathFromURI(getApplicationContext(),
						streamUri));
				imageLoadedView.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if (preview != null) {
					preview.addView(mPreview);
				} else
					throw new Exception();
			} catch (Exception e) {
				Log.i("TAG", "Preview is null");
			}
		}

	}

	public void addGeoLocation() {
		geoLocService = new GeoLocationService(ObserverActivity.this);
		// check if GPS enabled
		if (geoLocService.canGetLocation()) {
			double latitude = geoLocService.getLatitude();
			double longitude = geoLocService.getLongitude();
			latTv.setText("Lat: " + latitude);
			lonTv.setText("Lon: " + longitude);
			double latlon[] = { latitude, longitude };
			scene.setLocation(latlon);
		} else {
			geoLocService.showSettingsAlert();
		}
	}

	private Camera getCameraInstance() {
		Camera c = null;
		if (ObserverActivity.this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_ANY)) {
			Log.i("Camera", "There is no Camera on this device");
		} else {
			Log.i("Camera", "There is no Camera on this ");
		}
		try {
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			int cameraCont = Camera.getNumberOfCameras();
			for (int camIndex = 0; camIndex < cameraCont; camIndex++) {
				Camera.getCameraInfo(camIndex, cameraInfo);
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					try {
						c = Camera.open(camIndex);
					} catch (RuntimeException e) {
						Log.e("Camera",
								"failed to open camera: "
										+ e.getLocalizedMessage());
					}
				}
			}
		} catch (Exception e) {
			Log.i("camera", "Unable to open camera");
		}
		return c;
	}

	public void onClickCapture(View v) {
		capture_status.setText("scene captured");
		capture_status.setVisibility(View.VISIBLE);
		capture_status.setAlpha(0.6f);
		image_capture.setText("Capture Again");
		mCamera.takePicture(null, null, new MyPictureCallback(
				ObserverActivity.this));
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void onCickOpenDialogMap(View view) {
		final Dialog dialog = new Dialog(ObserverActivity.this);
		dialog.setContentView(R.layout.map_dialog);
		dialog.setTitle("Mark the Location");

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng position) {
				mMap.clear();
				location = mMap.addMarker(
						new MarkerOptions().position(position).draggable(true))
						.getPosition();
			}
		});

		Button dialogOk = (Button) dialog.findViewById(R.id.dialogOk);
		dialogOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (location != null) {
					latTv.setText("Lat: " + location.latitude);
					lonTv.setText("Lon: " + location.longitude);
					double[] latlon = { location.latitude, location.longitude };
					scene.setLocation(latlon);
				}
				dialog.dismiss();
			}
		});
		Button dialogCancel = (Button) dialog.findViewById(R.id.dialogCancel);
		dialogCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void OnClickReset(View v) {
		mCamera.release();
		startActivity(new Intent(this, ObserverActivity.class));
		finish();
	}

	public void uploadData(View v) {
		if(Utils.image != null)
		{
		if (scene != null ) {
			scene.setSceneId("-1");
			scene.setImageData(Utils.getImage());
			scene.setDescription(e_comment.getText().toString());
			System.out.print(scene.getDescription());
			System.out.println(scene);
			
			double latlon[] = { 56.67, 23.223 };

			
			/* Test API */
			
			Boundary b = new Boundary(10, 20, 30, 34);
			Region reg = new Region(9, b, 8);
			ArrayList<Region> al = new ArrayList<Region>();
			al.add(reg);
			al.add(new Region(2, b, 3));
/*			Analysis analysis = new Analysis("Android", al, latlon);
			
			ArrayList<Analysis> analys = new ArrayList<Analysis>();
			analys.add(analysis);
*/			
			Gson gson = new Gson();
			gson.toJson(scene);
			
			System.out.println("GSON SCENE ARRAY " + gson.toJson(scene));
			AsyncTask<String, Void, String> a = new HttpAsyncService(ObserverActivity.this).execute(String.valueOf(Utils.UploadInfo),
					Utils.UPLOAD_URL+"?event=generic", gson.toJson(scene));
			try {
				Toast.makeText(getApplicationContext(), a.get().toString(), Toast.LENGTH_SHORT).show();
				Utils.image = null;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	}
	
}

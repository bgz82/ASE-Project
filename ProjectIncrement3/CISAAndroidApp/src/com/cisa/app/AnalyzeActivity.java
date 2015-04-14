package com.cisa.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.cisa.app.R;
import com.cisa.app.bean.Analysis;
import com.cisa.app.bean.Boundary;
import com.cisa.app.bean.Region;
import com.cisa.app.bean.Scene;
import com.cisa.app.services.HttpAsyncService;
import com.cisa.app.utils.Base64;
import com.cisa.app.utils.Utils;
import com.cisa.app.views.CropView;
import com.cisa.app.views.GestureView;
import com.cisa.app.views.SelectionPreview;
import com.google.gson.Gson;

public class AnalyzeActivity extends Activity {
	
	private CropView cropView;
	private ImageView imageView;
	private static Button mark = null;
	private Scene scene;
	private String deviceID;
	private ArrayList<Analysis> analysisList = new ArrayList<Analysis>();
	private ArrayList<Region> selectedRegions = new ArrayList<Region>();
	public static String event="";
    private SelectionPreview selectionPreview;
	Dialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.analyze_layout);
		selectionPreview = (SelectionPreview) findViewById(R.id.selectionPreview);
		cropView = (CropView)findViewById(R.id.cropView);
		Intent intent = getIntent();
		event = intent.getStringExtra("event").toString();
		dialog = new Dialog(AnalyzeActivity.this);
		deviceID = getDeviceID();
		initializeViewElements();
	}
	
	private String getDeviceID() {
		WifiManager m_wm = (WifiManager)getSystemService(Context.WIFI_SERVICE); 
		String m_wlanMacAdd = m_wm.getConnectionInfo().getMacAddress();
		System.out.println(m_wlanMacAdd);
		return m_wlanMacAdd;
	}

	public void addRegion(View view) {
		Region region = new Region(1, new Boundary(2, 10, 0, 20), showDamageLevelDialog());
		//selectedRegions.add(region);
	}

	
	private int showDamageLevelDialog() {
		dialog.setContentView(R.layout.seekbar_layout);
		dialog.setTitle("Select Damage Level");
	
		final SeekBar sb = (SeekBar) dialog.findViewById(R.id.seekBar);
		sb.incrementProgressBy(1);
		sb.setMax(4);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(final SeekBar seekBar,
                    final int progress, final boolean fromUser) {
                update(seekBar);
            }
        });
        update(sb);
		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		return 100;
	}

	private void update(final SeekBar sb) {
        final LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.layout);

        final int colorStart = Color.parseColor("#BD4141");
        final int colorEnd = Color.parseColor("#2aff12");

        layout.setBackgroundColor(interpolateColor(colorStart, colorEnd,
                sb.getProgress() / 5f)); // assuming SeekBar max is 100
    }

	private float interpolate(final float a, final float b,
            final float proportion) {
        return (a + ((b - a) * proportion));
    }

    private int interpolateColor(final int a, final int b,
            final float proportion) {
        final float[] hsva = new float[3];
        final float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }
	
	
	private void initializeViewElements() {
		Bitmap bitmap = null;
		imageView = (ImageView) findViewById(R.id.imageView);
		mark = (Button) findViewById(R.id.mark);
		scene = (Scene) getIntent().getSerializableExtra("scene");
		
		byte[] encodeByte;
		try {
			encodeByte = Base64.decode(scene.getImageData().getData());
			bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
					encodeByte.length);
			imageView.setImageBitmap(bitmap);

		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	public void uploadDetails(View v) {

		Analysis analysis = new Analysis(deviceID, new Region(0,  new Boundary(2, 5, 5, 8), 10), null);
		String sceneId = scene.getSceneId();
		
		AsyncTask<String, Void, String> response = new HttpAsyncService(AnalyzeActivity.this).execute(String.valueOf(Utils.UploadAnalysis), Utils.UPLOAD_ANALYSIS, new Gson().toJson(analysis), sceneId, event);
		
		try {
			Toast.makeText(getApplicationContext(), response.get().toString(), Toast.LENGTH_SHORT).show();
			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		
	}

	public void viewInfo(View view) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        String text = getIntent().getExtras().get("iText").toString();
        double[] loc = getIntent().getExtras().getDoubleArray("iLoc");
        String dispText = "Description : " + text + "\n" + "Lattitude: " + loc[0] + "  Longitude: " + loc[1];
        dlgAlert.setMessage(dispText);
        dlgAlert.setTitle("Scene Information");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

	public void OnClickReset(View v) {
		Intent intent = new Intent(getApplicationContext(), AnalyzeActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("scene", scene);
		intent.putExtras(bundle);
		intent.putExtra("event", event);
		startActivity(intent);
	}
	 public void onRadioClick(View v) {
	        switch (v.getId()) {

	            case R.id.radioBuilding:
	                cropView.setMode(CropView.MODE_RECT);
	                break;
	            case R.id.radioBridge:
	                cropView.setMode(CropView.MODE_CIRCLE);
	                break;
	            case R.id.radioRoad:
	                cropView.setMode(CropView.MODE_CIRCLE);
	                break;
	            case R.id.radioVehicle:
	                cropView.setMode(CropView.MODE_RECT);
	                break;
	            case R.id.radioVegetation:
	                cropView.setMode(CropView.MODE_RECT);
	                break;
	            default:
	                break;
	        }
	        selectionPreview.clearSelection();
	        cropView.showCropView();

	    }
}

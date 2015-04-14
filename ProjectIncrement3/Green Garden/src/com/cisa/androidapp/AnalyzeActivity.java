package com.cisa.androidapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
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

import com.cisa.androidapp.bean.Analysis;
import com.cisa.androidapp.bean.Boundary;
import com.cisa.androidapp.bean.Region;
import com.cisa.androidapp.bean.Scene;
import com.cisa.androidapp.services.HttpAsyncService;
import com.cisa.androidapp.utils.Base64;
import com.cisa.androidapp.utils.Utils;
import com.cisa.androidapp.views.GestureView;
import com.google.gson.Gson;

public class AnalyzeActivity extends Activity {
	
	private GestureView gestureView;
	private ImageView imageView;
	private static Button mark = null;
	private Scene scene;
	private String deviceID;
	private ArrayList<Analysis> analysisList = new ArrayList<Analysis>();
	private ArrayList<Region> selectedRegions = new ArrayList<Region>();
	
	Dialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.analyze_layout);	
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
		
		AsyncTask<String, Void, String> response = new HttpAsyncService(AnalyzeActivity.this).execute(String.valueOf(Utils.UploadAnalysis), Utils.UPLOAD_ANALYSIS, new Gson().toJson(analysis), sceneId);
		
		try {
			Toast.makeText(getApplicationContext(), response.get().toString(), Toast.LENGTH_SHORT).show();
			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void OnClickReset(View v) {
		Intent intent = new Intent(getApplicationContext(), AnalyzeActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("scene", scene);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}

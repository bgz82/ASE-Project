package edu.umkc.dscrowd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.gson.Gson;

import edu.umkc.dscrowd.bean.Analysis;
import edu.umkc.dscrowd.bean.Boundary;
import edu.umkc.dscrowd.bean.Region;
import edu.umkc.dscrowd.bean.Scene;
import edu.umkc.dscrowd.services.HttpAsyncService;
import edu.umkc.dscrowd.utils.Base64;
import edu.umkc.dscrowd.utils.Utils;
import edu.umkc.dscrowd.views.CropView;
import edu.umkc.dscrowd.views.SelectionPreview;

public class AnalyzeActivity extends Activity {
	
	private ImageView imageView;
	private Scene scene;
	private String deviceID;
	private ArrayList<Region> selectedRegions = new ArrayList<Region>();
	private CropView cropView;
	private SelectionPreview selectionPreview;
	List<String> li;
	ListView list;
	RadioGroup radioGroup;
	Map<Integer, RectF> map = new HashMap<>();
	String eventType="";	
	int progress = 0;
	
	Dialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.analyze_layout);	
		cropView = (CropView)findViewById(R.id.cropView);
		selectionPreview = (SelectionPreview) findViewById(R.id.selectionPreview);
		li = new ArrayList<String>();
		Intent intent = getIntent();
		eventType = intent.getStringExtra("event");
		list = (ListView) findViewById(R.id.regionResult);
		radioGroup = (RadioGroup) findViewById(R.id.radioObject);
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
		int i = showDamageLevelDialog();
	}

	
	private int showDamageLevelDialog() {
		
		
		
		dialog.setContentView(R.layout.seekbar_layout);
		dialog.setTitle("Select Damage Level");
		cropView.clearCropView();
		
		
		
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
				progress = sb.getProgress();
				
				int type = 0;
				String typeText = null;
				RectF rect = cropView.getRect();
				RadioButton rb = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
				rb.setSelected(false);
				switch (radioGroup.getCheckedRadioButtonId()) {
				case R.id.radioBuilding:
					type = Utils.REGION_BUILDING;
					typeText = "Building";
					break;
				case R.id.radioBridge:
					type = Utils.REGION_BRIDGE;
					typeText = "Bridge";
					break;
				case R.id.radioRoad:
					type = Utils.REGION_ROAD;
					typeText = "Road";
					break;
				case R.id.radioHuman:
					type = Utils.REGION_HUMAN;
					typeText = "Human";
					break;
				case R.id.radioVehicle:
					type = Utils.REGION_VEHICLE;
					typeText = "Vehicle";
					break;
				case R.id.radioVegetation:
					typeText = "Vegetation";
					type = Utils.REGION_VEGETATION;
					break;
				default:
					break;
				}
				final Region region = new Region(type, new Boundary(rect.left, rect.top, rect.right, rect.bottom), progress);
				selectedRegions.add(region);
				
				li.add(0, typeText + ": " + region.getDamageLevel());
				map.put(map.size(), new RectF(region.getBoundry().getLeft(), region.getBoundry().getTop(), region.getBoundry().getRight(), region.getBoundry().getBottom()));
				
				ArrayAdapter<String> adp = new ArrayAdapter<String>(
						getApplicationContext(), R.layout.list, li);
				list.setAdapter(adp);
				list.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						String str =(String) list.getItemAtPosition(position);
						String values[] = str.split(":");
						Utils.getShape(values[0]);
						
						selectionPreview.showSelection(map.get(map.size()-position-1), Utils.getShape(values[0]), false);
					}
				});
				
				dialog.dismiss();
			}
		});
		dialog.show();
		return sb.getProgress();
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
		scene = (Scene) getIntent().getSerializableExtra("scene");
		
		byte[] encodeByte;
		try {
			encodeByte = Base64.decode(scene.getImageData().getData());
			bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
					encodeByte.length);
			imageView.setImageBitmap(bitmap);
			selectionPreview.clearSelection();
			cropView.setMode(CropView.MODE_RECT);
			cropView.showCropView();
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	public void uploadDetails(View v) throws JSONException {
		Analysis analysis = new Analysis(deviceID, new Region(0,  new Boundary(2, 5, 5, 8), 10), null);
		JSONArray region = new JSONArray();
		Gson gson = new Gson();
		JSONObject jobj = new JSONObject();
		jobj.accumulate("deviceId", deviceID);
		for(int i = 0; i < selectedRegions.size(); i++) {
			String gJson = gson.toJson(selectedRegions.get(i));
			try {
				
				region.put(new JSONObject(gJson));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		jobj.accumulate("region", region);
		
		/*JsonObject j = new JsonObject();
		j.addProperty("analysis", gson.toJson(region));
		System.out.println("s" + region);
		*/
		String sceneId = scene.getSceneId();
		String value = "Yes";
		
		AsyncTask<String, Void, String> response = new HttpAsyncService(AnalyzeActivity.this).execute(String.valueOf(Utils.UploadAnalysis), Utils.UPLOAD_ANALYSIS, jobj.toString(), sceneId, eventType);
		
		Intent intent = new Intent(getApplicationContext(),
				AnalyzeStatusActivity.class);
		intent.putExtra("event", eventType);
		startActivity(intent);
	}
	
	public void OnClickReset(View v) {
		Intent intent = new Intent(getApplicationContext(), AnalyzeActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("scene", scene);
		intent.putExtras(bundle);
		intent.putExtra("event", eventType);
		startActivity(intent);
	}
	
	public void onRadioClick(View v) {
		switch (v.getId()) {
		
		case R.id.radioBuilding:
			cropView.setMode(CropView.MODE_RECT);
			break;
		case R.id.radioBridge:
			cropView.setMode(CropView.MODE_RECT);
			break;
		case R.id.radioRoad:
			cropView.setMode(CropView.MODE_RECT);
			break;
		case R.id.radioHuman:
			cropView.setMode(CropView.MODE_CIRCLE);
			break;
		case R.id.radioVehicle:
			cropView.setMode(CropView.MODE_CIRCLE);
			break;
		case R.id.radioVegetation:
			cropView.setMode(CropView.MODE_CIRCLE);
			break;
		default:
			break;
		}
		selectionPreview.clearSelection();
		cropView.showCropView();
	}
	
	
	public void onInfoClick(View view) {
		Intent intent = new Intent(AnalyzeActivity.this,
				AnalysisDetails.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("scene", scene);
		intent.putExtras(bundle);
		intent.putExtra("event", eventType);
		startActivity(intent);
		AnalyzeActivity.this.overridePendingTransition(
				R.anim.right_left_in, R.anim.right_left_out);
	}
}

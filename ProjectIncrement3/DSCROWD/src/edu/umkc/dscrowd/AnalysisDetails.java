package edu.umkc.dscrowd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import edu.umkc.dscrowd.bean.Scene;
import edu.umkc.dscrowd.services.HttpAsyncService;
import edu.umkc.dscrowd.utils.Base64;
import edu.umkc.dscrowd.utils.Utils;
import edu.umkc.dscrowd.views.SelectionPreview;

public class AnalysisDetails extends Activity {

	Scene scene = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis_details);
		scene = (Scene) getIntent().getExtras().get("scene");
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(scene)).commit();
		}
	}

	public class PlaceholderFragment extends Fragment {
		Scene scene;
		private ImageView img;
		TextView location;
		TextView tv;
		TextView al;
		private SelectionPreview selectionPreview;

		public PlaceholderFragment(Scene scene) {
			this.scene = scene;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_analysis_details, container, false);
			
			
			img = (ImageView) rootView
					.findViewById(R.id.imageView);
			location = (TextView) rootView
					.findViewById(R.id.location);
			tv = (TextView) rootView
					.findViewById(R.id.description);
			al = (TextView) rootView.findViewById(R.id.analyzed);
			selectionPreview = (SelectionPreview) rootView.findViewById(R.id.selectionPreview);

			loadAsyncData();
			
			return rootView;
		}
		
		private void loadAsyncData() {
			final AsyncTask<String, Void, String> response = new HttpAsyncService(
					AnalysisDetails.this).execute(String
					.valueOf(Utils.GetAnalysis), Utils.UPLOAD_ANALYSIS,scene.getSceneId());
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {					
								byte[] encodeByte;
								Bitmap bitmap;
								try {
									encodeByte = Base64.decode(scene
											.getImageData().getData());
									bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
											encodeByte.length);
									img.setImageBitmap(bitmap);
									location.setText("[ "
											+ scene.getLocation()[0]
											+ ", "
											+ scene.getLocation()[1]
											+ " ]");
									tv.setText("Description: "
											+ scene.getDescription());

									try {
										String result = response.get();
									ArrayList<RectF> rectf = new ArrayList<>();
									ArrayList<Integer> shape = new ArrayList<>();
									ArrayList<Integer> damage = new ArrayList<>();
									
										JSONArray jsonList = new JSONArray(result);
										
										for(int i=0; i< jsonList.length(); i++) {
											JSONObject regionObj = jsonList.getJSONObject(i);
										//	System.out.println(regionObj);									
											String regStr = regionObj.getString("region");
										//	System.out.println("reg array " + regStr);
											JSONArray bArray = new JSONArray(regStr);
										//	System.out.println("reg array " + regArray);
										
											for (int j = 0; j < bArray.length(); j++) {
												JSONObject area = new JSONObject(bArray.getString(j));
												
												JSONObject boundary = (JSONObject) area.get("boundry");
												System.out.println("Boundry: " + boundary);
												
												RectF rect = new RectF(Float.parseFloat(boundary.getString("left")), Float.parseFloat(boundary.getString("top")), Float.parseFloat(boundary.getString("right")),Float.parseFloat(boundary.getString("bottom")));
												rectf.add(rect);
												shape.add(Utils.getShape(area.getInt("categroy")));
												damage.add(area.getInt("damageLevel"));	
											}
										}
										selectionPreview.drawAll(rectf, shape, damage);
									}catch (JSONException e) {
										e.printStackTrace();
									}
								} catch (IOException e) {
									e.printStackTrace();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (Exception e) {
								
								}


							}
						});
					} finally {
					}
				}
			}).start();
		}
	}
	
	public void goBack(View view) {
		Intent intent = new Intent(AnalysisDetails.this, AnalyzeList.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("scene", scene);
		intent.putExtras(bundle);
		startActivity(intent);
		AnalysisDetails.this.overridePendingTransition(
				R.anim.right_left_in, R.anim.right_left_out);
		
	}
	
	public void onExit(View view) {
		Intent homeIntent= new Intent(Intent.ACTION_MAIN);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(homeIntent);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.gc();
	}
}

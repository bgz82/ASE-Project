package edu.umkc.dscrowd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import edu.umkc.dscrowd.bean.Image;
import edu.umkc.dscrowd.bean.Scene;
import edu.umkc.dscrowd.utils.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class CoordinatorActivity extends Activity {

	GoogleMap mMap;
	LatLng location;
	ArrayList<Scene> sceneList;
	ArrayList<Bitmap> bitmapList;
	SceneListAdapter dataAdapter = null;
	int start = 0;
	int limit = 10;
	
	String eventType = "";
	boolean loadingMore = false;
	ListView listView = null;
	Activity context = null;
	Map<Marker, Scene> mMarkerMap = new HashMap<>();
	boolean loadmore = true;
	boolean dialogShow = true;
	Menu menu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coordinator_layout);
		context = CoordinatorActivity.this;
		sceneList = new ArrayList<Scene>();
		Intent intent = getIntent();
		eventType = intent.getStringExtra("event").toString();
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	 	
		loadDataFromCloud();
		
		changeCameraPosition(new LatLng(35.316122,-97.548005));	
	}
	private void loadDataFromCloud() {
		new HTTPCloudService().execute();
	}

	void changeCameraPosition(LatLng location) {
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 11));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.analyze_list_map, menu);
		MenuItem list = menu.findItem(R.id.action_list);
		MenuItem map = menu.findItem(R.id.action_map);
		list.setVisible(true);
		map.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (item.getItemId()) {
		case R.id.action_list:
			invalidateOptionsMenu();
			startActivity(new Intent(context, AnalyzeList.class));
			break;
		default:
			break;
		}
		return true;
	}
	
	class HTTPCloudService extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = new ProgressDialog(context);

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Getting your data... Please wait...");
			if(dialogShow)
				dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			ByteArrayOutputStream out = null;
			HttpClient client = new DefaultHttpClient();
			String url = Utils.HOST + Utils.RES_LIST_ITEMS + "?start="+ sceneList.size() + "&limit=10&event=" + eventType;
			HttpGet get = new HttpGet(url);
			HttpResponse response;
			try {
				response = client.execute(get);
				out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				get.abort();
			}
			return out.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			dialogShow = false;
			if(populateSceneData(result)){
				populateMarkersToMap();
			}

			dialog.dismiss();
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
	}
	
	
		
	private boolean populateSceneData(String data) {
		if (data == null) {
			//listView.removeFooterView(loadMoreView);
			return false;
		}
		JSONArray jsonList;
		try {
			jsonList = new JSONArray(data);
			if (jsonList.length() == 0) {
				loadmore = false;
				//listView.removeFooterView(loadMoreView);
			} else {
				if (jsonList.length() < 10) {
					//listView.removeFooterView(loadMoreView);
					loadmore = false;
				}
				for (int i = 0; i < jsonList.length(); i++) {
					JSONObject sceneJSON = jsonList.getJSONObject(i);
					// Image Mapping
					Image image = new Gson().fromJson(sceneJSON.getString("imageData"), Image.class);
					// Location Mapping
					String sceneId = sceneJSON.getString("_id");
					String locationString = sceneJSON.getString("location");
					double location[] = Utils.extractLocationFromString(locationString);
					// Description Mapping
					String description = sceneJSON.getString("description");
					Scene scene = new Scene(sceneId, image, description, location);
					sceneList.add(scene);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}
	

	private void populateMarkersToMap() {

		for (int i = 0; i < sceneList.size(); i++) {
			Scene scene = sceneList.get(i);
			Marker m = mMap.addMarker(new MarkerOptions().position(
					new LatLng(scene.getLocation()[0], scene.getLocation()[1]))
					.title("[" + scene.getLocation()[0] + ", "
							+ scene.getLocation()[1] + "]"));

			mMarkerMap.put(m, scene);
			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

				@Override
				public boolean onMarkerClick(Marker marker) {

					Intent intent = new Intent(context, AnalysisDetails.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("scene", mMarkerMap.get(marker));
					intent.putExtras(bundle);
					startActivity(intent);
					context.overridePendingTransition(
							R.anim.right_left_in, R.anim.right_left_out);
					return true;
				}
			});

		}
		if(loadmore)
		loadDataFromCloud();
	}
}

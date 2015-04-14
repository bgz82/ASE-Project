package com.cisa.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cisa.app.R;
import com.cisa.app.bean.Image;
import com.cisa.app.bean.Scene;
import com.cisa.app.services.HttpAsyncService;
import com.cisa.app.utils.Base64;
import com.cisa.app.utils.Utils;
import com.google.gson.Gson;



public class AnalysisItemListActivity extends Activity {

	View loadMoreView;
	ArrayList<Scene> sceneList = new ArrayList<Scene>();
	ArrayAdapter<Scene> adapter = null;
	ListView list;
	boolean loadingMore = false;
	private boolean loadmore = true;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.analysis_list);

		populateList();
		loadMore();
		registerClickCallback();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflator = getMenuInflater();
		menuInflator.inflate(R.menu.menu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_map:
				Toast.makeText(getApplicationContext(), "Map", Toast.LENGTH_SHORT).show();
			break;
		}
		return true;
	}
	
	
	private void populateList() {
		adapter = new SceneListAdapter(this, R.layout.item_info, sceneList);
		list = (ListView) findViewById(R.id.sceneList);
		loadMoreView = ((LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.loadmore, null, false);
		
		loadMoreView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadMore();
				
			}
		});
		list.addFooterView(loadMoreView);
		list.setAdapter(adapter);
		list.setTextFilterEnabled(true);
	}

	private void populateSceneData(String data) {

		if(data == null){
			list.removeFooterView(loadMoreView);
			return;
		}
		
		JSONArray jsonList;
		try {
			jsonList = new JSONArray(data);

			if (jsonList.length() == 0) {
				list.removeFooterView(loadMoreView);
			} else {

				for (int i = 0; i < jsonList.length(); i++) {
					JSONObject sceneJSON = jsonList.getJSONObject(i);
					// Image Mapping
					Image image = new Gson().fromJson(
							sceneJSON.getString("imageData"), Image.class);

					// Location Mapping
					String sceneId = sceneJSON.getString("_id");
					String locationString = sceneJSON.getString("location");
					double location[] = extractLocation(locationString);

					// Description Mapping
					String description = sceneJSON.getString("description");

					Scene scene = new Scene(sceneId, image, description, location);

					sceneList.add(scene);
					adapter.add(scene);
				}
				adapter.notifyDataSetChanged();
				loadmore = false;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private double[] extractLocation(String locationString) {
		System.out.println(locationString);
		locationString = locationString.substring(1, locationString.length()-1);
		String values[] = locationString.split(",");
		double latitude = Double.parseDouble(values[0]);
		double longitude = Double.parseDouble(values[1]);
		double[] location = {latitude, longitude};
		return location;
	}
	private void registerClickCallback() {		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				Scene scene = (Scene) parent.getItemAtPosition(position);
				Toast.makeText(getApplicationContext(), scene.getDescription(),
						Toast.LENGTH_SHORT).show();

				Intent intent = new Intent(getApplicationContext(),
						AnalyzeActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("scene", scene);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	private void loadMore(){
		System.out.println("sdf");
		AsyncTask<String, Void, String> a = new HttpAsyncService(
				AnalysisItemListActivity.this).execute(
				String.valueOf(Utils.ItemsList), Utils.HOST
						+ Utils.RES_LIST_ITEMS + "?start="+sceneList.size()
						+ "&limit=10");
		try {
			populateSceneData(a.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	
	}

	private class ViewHolder {
		ImageView image;
		TextView locality;
		TextView description;
	}

	private class SceneListAdapter extends ArrayAdapter<Scene> {

		Bitmap bitmap;
		private ArrayList<Scene> sceneList;
		

		public SceneListAdapter(Context context, int itemInfo, ArrayList<Scene> sceneList) {
			super(context, itemInfo, sceneList);
			this.sceneList = new ArrayList<Scene>();
			this.sceneList.addAll(sceneList);
		}

		@Override
		public void add(Scene scene) {
			// TODO Auto-generated method stub
				this.sceneList.add(scene);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				//LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				//convertView = vi.inflate(R.layout.item_info, null);
				convertView = getLayoutInflater().inflate(R.layout.item_info, parent, false);
			}

			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.imageicon);
			holder.locality = (TextView) convertView.findViewById(R.id.locality);
			//holder.location = (TextView) convertView.findViewById(R.id.location);
		//	holder.description = (TextView) convertView.findViewById(R.id.description);
			//holder.count = (TextView) convertView.findViewById(R.id.count);

			Scene currentScene = sceneList.get(position);
			byte[] encodeByte;
			try {
				encodeByte = Base64.decode(currentScene.getImageData().getData());
				bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
						encodeByte.length);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			holder.image.setImageBitmap(bitmap);
			//holder..setText("ID: " + iBeanInfo.getImageId());
			double lat = currentScene.getLocation()[0];
			double lon = currentScene.getLocation()[1];
			
			Geocoder mGeocoder = new Geocoder(AnalysisItemListActivity.this);
			List<Address> addresses = null;
			try {
				addresses = mGeocoder.getFromLocation(lat, lon, 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String addressText = null;
			
			if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                //return addressText;
            } else {
                //return "No address found";
            }
			holder.locality.setText(addressText);
			//holder.location.setText("Loc: " + lat + ", " + lon);
			holder.description.setText("Description: " + currentScene.getDescription());
			
			//holder.count.setText("0");
			return convertView;
		}
	}

	private void registerScrollCallback() {
		list.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				System.out.println("sdf");
				int lastInScreen = firstVisibleItem + visibleItemCount;
				if ((lastInScreen == totalItemCount) && !(loadingMore)) {
					AsyncTask<String, Void, String> a = new HttpAsyncService(
							AnalysisItemListActivity.this).execute(
							String.valueOf(Utils.ItemsList), Utils.HOST
									+ Utils.RES_LIST_ITEMS + "?start=" + sceneList.size()
									+ "&limit=10");
					try {
						populateSceneData(a.get().toString());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				AsyncTask<String, Void, String> a = new HttpAsyncService(
						AnalysisItemListActivity.this).execute(
						String.valueOf(Utils.ItemsList), Utils.HOST
								+ Utils.RES_LIST_ITEMS + "?start=0"
								+ "&limit=10");
				try {
					populateSceneData(a.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		});
	}

}

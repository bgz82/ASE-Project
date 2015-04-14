package com.cisa.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;


import com.cisa.app.R;
import com.cisa.app.bean.Image;
import com.cisa.app.bean.Scene;
import com.cisa.app.services.HttpAsyncService;
import com.cisa.app.utils.Base64;
import com.cisa.app.utils.Utils;



public class AnalyzeListMap extends Activity {

	Menu menu;
	boolean a = false;
	static String event="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_list_map);
        Intent intent = getIntent();
        event = intent.getStringExtra("event").toString();
		if (savedInstanceState == null) {
			if (getIntent().getExtras().getBoolean("list")) {

				getFragmentManager().beginTransaction()
						.add(R.id.container, new ListHolderFragment()).commit();
			}
			else {
                //setContentView(R.layout.coordinator_layout);
				getFragmentManager().beginTransaction().add(R.id.container, new MapHolderFragment()).commit();
			}
		}

	}
	@Override
	public void onResume() {
		super.onResume();
	}
 
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
 
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.analyze_list_map, menu);
		MenuItem list = menu.findItem(R.id.action_list);
		MenuItem map = menu.findItem(R.id.action_map);
		list.setVisible(a);
		map.setVisible(!a);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (item.getItemId()) {
		case R.id.action_list:
			a = false;
			invalidateOptionsMenu();
			getFragmentManager().beginTransaction()
					.replace(R.id.container, new ListHolderFragment()).commit();
			Toast.makeText(getApplicationContext(), "List", Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.action_map:
			a = true;
			invalidateOptionsMenu();
			getFragmentManager().beginTransaction()
					.replace(R.id.container, new MapHolderFragment()).commit();
			Toast.makeText(getApplicationContext(), "Map", Toast.LENGTH_SHORT)
					.show();
			break;
		default:
			break;
		}
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class ListHolderFragment extends Fragment {

		View rootView;
		View loadMoreView;
		ArrayList<Scene> sceneList = new ArrayList<Scene>();
		ArrayAdapter<Scene> adapter = null;
		ListView list;
		boolean loadingMore = false;
		private boolean loadmore = true;

		public ListHolderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_analyze_list,
					container, false);

			populateList();
			loadMore();
			registerClickCallback();
			return rootView;
		}

		private void populateList() {
			list = (ListView) rootView.findViewById(R.id.sceneList);
			adapter = new SceneListAdapter(this.getActivity(),
					R.layout.item_info, sceneList);
			loadMoreView = ((LayoutInflater) this.getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.loadmore, null, false);

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

			if (data == null) {
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
						Scene scene = new Scene(sceneId, image, description,
								location);

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
			locationString = locationString.substring(1,
					locationString.length() - 1);
			String values[] = locationString.split(",");
			double latitude = Double.parseDouble(values[0]);
			double longitude = Double.parseDouble(values[1]);
			double[] location = { latitude, longitude };
			return location;
		}

		private void registerClickCallback() {
			list.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// When clicked, show a toast with the TextView text
					Scene scene = (Scene) parent.getItemAtPosition(position);
					//Toast.makeText(getActivity().getApplicationContext(),
						//	scene.getDescription(), Toast.LENGTH_SHORT).show();
                   System.out.println("BALU1");
					Intent intent = new Intent(getActivity()
							.getApplicationContext(), AnalyzeActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("scene", scene);
					System.out.println(scene.getDescription());
					System.out.println(scene.getLocation());
                    intent.putExtra("iText", scene.getDescription());
                    intent.putExtra("iLoc", scene.getLocation());
					intent.putExtras(bundle);
					intent.putExtra("event", event);
					if(scene != null){
					startActivity(intent);}
					System.out.println("BALU2");
				}
			});
		}

		private void loadMore() {
			System.out.println("sdf");
			AsyncTask<String, Void, String> a = new HttpAsyncService(
					this.getActivity()).execute(
					String.valueOf(Utils.ItemsList),
					Utils.HOST + Utils.RES_LIST_ITEMS + "?start="
							+ sceneList.size() + "&limit=10&event=" + event);
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
		}

		private class SceneListAdapter extends ArrayAdapter<Scene> {

			Bitmap bitmap;
			private ArrayList<Scene> sceneList;

			public SceneListAdapter(Context listHolderFragment, int itemInfo,
					ArrayList<Scene> sceneList) {
				super(listHolderFragment, itemInfo, sceneList);
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
					convertView = getActivity().getLayoutInflater().inflate(
							R.layout.item_info, parent, false);
				}

				holder = new ViewHolder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.imageicon);
				holder.locality = (TextView) convertView
						.findViewById(R.id.locality);
				Scene currentScene = sceneList.get(position);
				byte[] encodeByte;
				try {
					encodeByte = Base64.decode(currentScene.getImageData()
							.getData());
					bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
							encodeByte.length);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//ThumbnailUtils thumb;
				Bitmap thumbNail = ThumbnailUtils.extractThumbnail(bitmap, 64, 64);
				holder.image.setImageBitmap(thumbNail);
				double lat = currentScene.getLocation()[0];
				double lon = currentScene.getLocation()[1];

				Geocoder mGeocoder = new Geocoder(this.getContext());
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
					 * Format the first line of address (if available), city,
					 * and country name.
					 */
					addressText = String.format(
							"%s, %s, %s",
							// If there's a street address, add it
							address.getMaxAddressLineIndex() > 0 ? address
									.getAddressLine(0) : "",
							// Locality is usually a city
							address.getLocality(),
							// The country of the address
							address.getCountryName());
					// Return the text
					// return addressText;
				} else {
					// return "No address found";
				}
				holder.locality.setText(addressText);
				return convertView;
			}
		}

	}

	public class MapHolderFragment extends Fragment{
		
		GoogleMap mMap;
		MapView mapView;
		LatLng location;
		ArrayList<Scene> sceneList;
		ArrayList<Bitmap> bitmapList;
		int start = 0;
		int limit = 10;
		boolean loadingMore = false;
		ListView listView = null;
		Activity context = null;
		Map<Marker, Scene> mMarkerMap = new HashMap<>();
		boolean loadmore = true;
		boolean dialogShow = true;
		Menu menu;
		
		public MapHolderFragment() {
		}
		private void loadDataFromCloud() {
			new HTTPCloudService(this).execute();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            //this.setContentView(R.layout.coordinator_layout); 
			View rootView = inflater.inflate(R.layout.coordinator_layout,
					container, false);
			mapView = (MapView) rootView.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            System.out.println("mapview: " + mapView.toString());
            context = AnalyzeListMap.this;
    		sceneList = new ArrayList<Scene>();
    		//mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    		mMap = mapView.getMap();
    		loadDataFromCloud();
    		return rootView;
//    		changeCameraPosition(new LatLng(35.316122,-97.548005));
			
		}
		
		void changeCameraPosition(LatLng location) {
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 11));
		}
		
		@Override
		public void onResume() {
            if (mapView != null) {
            	super.onResume();
                mapView.onResume();
            }
            else
            {
            	System.out.println("onResume: mapview is null");
            }
			//mapView.onResume();

		}
	 
		@Override
		public void onDestroy() {
			super.onDestroy();
			mapView.onDestroy();
		}
	 
		@Override
		public void onLowMemory() {
			super.onLowMemory();
			mapView.onLowMemory();
		}
		
		
		class HTTPCloudService extends AsyncTask<String, Void, String> {
			private ProgressDialog dialog = new ProgressDialog(context);
            MapHolderFragment obj;
			public HTTPCloudService(MapHolderFragment object)
			{
				obj=object;
			}
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
				String url = Utils.HOST + Utils.RES_LIST_ITEMS + "?start="+ sceneList.size() + "&limit=20&event=" + event;
				System.out.println(" Url: " + url);
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
				System.out.println("Data from server: " + result);
				if(populateSceneData(result)){
					populateMarkersToMap();
					
					MapsInitializer.initialize(obj.getActivity());
					
					
					// Updates the location and zoom of the MapView
					System.out.println("Before CAMMAP");
					CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(sceneList.get(0).getLocation()[0], sceneList.get(0).getLocation()[1]), 15);
					mMap.animateCamera(cameraUpdate);
			 
					//return obj.rootView;
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
						double location[] = extractLocation(locationString);
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
		
		private double[] extractLocation(String locationString) {
			System.out.println(locationString);
			locationString = locationString.substring(1,
					locationString.length() - 1);
			String values[] = locationString.split(",");
			double latitude = Double.parseDouble(values[0]);
			double longitude = Double.parseDouble(values[1]);
			double[] location = { latitude, longitude };
			return location;
		}

		private void populateMarkersToMap() {

			for (int i = 0; i < sceneList.size(); i++) {
				Scene scene = sceneList.get(i);
				Image image = scene.getImageData();
				String encoded = image.getData();
				System.out.println("adding marker " + i +" with image: " + encoded);
				Bitmap icon = null, thumb = null;
				try {
					byte[] byteImage = com.cisa.app.utils.Base64.decode(encoded);
					//System.out.println("byte image: " + byteImage.length);
					icon = BitmapFactory.decodeByteArray(byteImage, 0,
							byteImage.length);
					thumb = ThumbnailUtils.extractThumbnail(icon, 64, 64);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//System.out.println("Exception");
					e.printStackTrace();
				}
				//System.out.println("Thumb created: " + thumb != null ? thumb.toString():"thumb null");
				Marker m = mMap.addMarker(new MarkerOptions().position(
						new LatLng(scene.getLocation()[0], scene.getLocation()[1]))
						.title(scene.getDescription()));

				mMarkerMap.put(m, scene);
				/*mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

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
				});*/

			}
		/*	if(loadmore)
				loadDataFromCloud();*/
		}
	}
	
}





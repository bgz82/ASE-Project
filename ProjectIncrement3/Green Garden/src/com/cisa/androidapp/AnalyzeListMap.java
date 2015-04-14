package com.cisa.androidapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
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

import com.cisa.androidapp.bean.Image;
import com.cisa.androidapp.bean.Scene;
import com.cisa.androidapp.services.HttpAsyncService;
import com.cisa.androidapp.utils.Base64;
import com.cisa.androidapp.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

public class AnalyzeListMap extends Activity {

	Menu menu;
	boolean a = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analyze_list_map);
		if (savedInstanceState == null) {
			if (getIntent().getExtras().getBoolean("list")) {
				getFragmentManager().beginTransaction()
						.add(R.id.container, new ListHolderFragment()).commit();
			}
			else {
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
					Toast.makeText(getActivity().getApplicationContext(),
							scene.getDescription(), Toast.LENGTH_SHORT).show();

					Intent intent = new Intent(getActivity()
							.getApplicationContext(), AnalyzeActivity.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("scene", scene);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
		}

		private void loadMore() {
			System.out.println("sdf");
			AsyncTask<String, Void, String> a = new HttpAsyncService(
					this.getActivity()).execute(
					String.valueOf(Utils.ItemsList),
					Utils.HOST + Utils.RES_LIST_ITEMS + "?start="
							+ sceneList.size() + "&limit=10");
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
			TextView location;
			TextView count;
			TextView description;
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
					// LayoutInflater vi = (LayoutInflater)
					// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					// convertView = vi.inflate(R.layout.item_info, null);
					convertView = getActivity().getLayoutInflater().inflate(
							R.layout.item_info, parent, false);
				}

				holder = new ViewHolder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.imageicon);
				holder.locality = (TextView) convertView
						.findViewById(R.id.locality);
				holder.location = (TextView) convertView
						.findViewById(R.id.location);
				holder.description = (TextView) convertView
						.findViewById(R.id.description);
				holder.count = (TextView) convertView.findViewById(R.id.count);

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
				holder.image.setImageBitmap(bitmap);
				// holder..setText("ID: " + iBeanInfo.getImageId());
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
				holder.location.setText("Loc: " + lat + ", " + lon);
				holder.description.setText("Description: "
						+ currentScene.getDescription());

				holder.count.setText("0");
				return convertView;
			}
		}

	}

	public static class MapHolderFragment extends MapFragment{

		GoogleMap mMap, map;
		MapView mapView;
		
		public MapHolderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.coordinator_layout,
					container, false);
			
			mapView = (MapView) rootView.findViewById(R.id.map);
			mapView.onCreate(savedInstanceState);
			
			// Gets to GoogleMap from the MapView and does initialization stuff
			map = mapView.getMap();
			map.getUiSettings().setMyLocationButtonEnabled(false);
			map.setMyLocationEnabled(true);
			
			MapsInitializer.initialize(this.getActivity());
			
			
			// Updates the location and zoom of the MapView
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
			map.animateCamera(cameraUpdate);
	 
			return rootView;
		}
		
		@Override
		public void onResume() {
			mapView.onResume();
			super.onResume();
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
	}

}

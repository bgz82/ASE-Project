package edu.umkc.dscrowd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;

import edu.umkc.dscrowd.bean.Image;
import edu.umkc.dscrowd.bean.Scene;
import edu.umkc.dscrowd.utils.Base64;
import edu.umkc.dscrowd.utils.Utils;

public class AnalyzeList extends Activity {

	ArrayList<Scene> sceneList;
	ArrayList<Bitmap> bitmapList;
	ArrayList<String> Analysis;
	SceneListAdapter dataAdapter = null;
	int start = 0;
	int limit = 10;
	boolean loadingMore = false;
	View loadMoreView;
	ListView listView = null;
	Activity context = null;
	Menu menu;
	String eventType="";

	long ms;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analyze_list);
		context = AnalyzeList.this;
        Intent intent = getIntent();
        eventType = intent.getStringExtra("event").toString();
        ms = intent.getLongExtra("msec", 0); 
        listView = (ListView) findViewById(R.id.sceneList);

		loadMoreView = ((LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.loadmore, listView, false);
		listView.addFooterView(loadMoreView);

		sceneList = new ArrayList<Scene>();
		bitmapList = new ArrayList<Bitmap>();
		Analysis = new ArrayList<String>();
		dataAdapter = new SceneListAdapter(this, R.layout.item_info_white,
				sceneList, bitmapList, Analysis);
		listView.setAdapter(dataAdapter);

		listView.setTextFilterEnabled(true);
		setListenersToList();
		loadDataFromCloud();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.analyze_list_map, menu);
		MenuItem list = menu.findItem(R.id.action_list);
		MenuItem map = menu.findItem(R.id.action_map);
		list.setVisible(false);
		map.setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (item.getItemId()) {
		case R.id.action_map:
			invalidateOptionsMenu();
			Intent intent = new Intent(context, CoordinatorActivity.class);
			intent.putExtra("event", eventType);
			startActivity(intent);
			break;
		default:
			break;
		}
		return true;
	}

	
	
	
	public void onLoadMoreClick(View view) {
		loadDataFromCloud();
	}
	
	private void setListenersToList() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Scene scene = (Scene) parent.getItemAtPosition(position);
				Intent intent = new Intent(context, AnalyzeActivity.class);
				intent.putExtra("event", eventType);
				Bundle bundle = new Bundle();
				bundle.putSerializable("scene", scene);
				intent.putExtras(bundle);
				startActivity(intent);
				context.overridePendingTransition(R.anim.right_left_in, R.anim.right_left_out);
			}
		});
	}

	private void loadDataFromCloud() {
		new HTTPCloudService().execute();
	}

	private void populateSceneData(String data) {
		if (data == null) {
			listView.removeFooterView(loadMoreView);
			return;
		}
		JSONArray jsonList;
		try {
			jsonList = new JSONArray(data);

			if (jsonList.length() == 0) {
				listView.removeFooterView(loadMoreView);
			} else {
				if (jsonList.length() < 10) {
					listView.removeFooterView(loadMoreView);
				}
				for (int i = 0; i < jsonList.length(); i++) {
					JSONObject sceneJSON = jsonList.getJSONObject(i);
					// Image Mapping
					Image image = new Gson().fromJson(
							sceneJSON.getString("imageData"), Image.class);
					// Location Mapping
					String sceneId = sceneJSON.getString("_id");
					String locationString = sceneJSON.getString("location");
					double location[] = Utils
							.extractLocationFromString(locationString);
					// Description Mapping
					String description = sceneJSON.getString("description");
					String check;
					if(sceneJSON.has("results") || sceneJSON.has("Analyzed")){
					check = "Yes";
					}
					else
					{
						check = "No";
					}
					Scene scene = new Scene(sceneId, image, description,
							location);
					
					Bitmap bitmap = null;
					
					try {
						byte[] encodeByte = Base64.decode(scene.getImageData().getData());
						
						BitmapFactory.Options options= new BitmapFactory.Options();
						options.inPurgeable = true;
						options.inDither = true;
						options.inPreferredConfig = Bitmap.Config.RGB_565;
						options.inSampleSize = 2;
						bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
								encodeByte.length, options);
						encodeByte = null;
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					bitmapList.add(bitmap);
					sceneList.add(scene);
					Analysis.add(check);
					dataAdapter.add(scene, bitmap, check);
				}
				dataAdapter.notifyDataSetChanged();
			}
			Date eDate = new Date();
			long ms1 = eDate.getTime();
			long diff = ms1 - ms;
			ms = ms1;
			float mins = (float)diff/(1000);
			String tst = mins + " seconds";
			Toast.makeText(getApplicationContext(), tst.toString(), Toast.LENGTH_SHORT).show();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class HTTPCloudService extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog = new ProgressDialog(context);

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Loading Data ...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			ByteArrayOutputStream out = null;
			HttpClient client = new DefaultHttpClient();
			String url = Utils.HOST + Utils.RES_LIST_ITEMS + "?start="
					+ sceneList.size() + "&limit=10" + "&event=" + eventType;
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
			populateSceneData(result);

			dialog.dismiss();
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
	}
	
	 @Override
	    protected void onDestroy() {
	    super.onDestroy();
	    System.gc();
	    }
}

class SceneListAdapter extends ArrayAdapter<Scene> {

	private class ViewHolder {
		ImageView image;
		TextView location;
		TextView description;
		TextView isAnalyzed;
	}

	Bitmap bitmap;
	private ArrayList<Scene> sceneList;
	private ArrayList<Bitmap> bitmapList;
	private ArrayList<String> analyst;
 
	public SceneListAdapter(Context listHolderFragment, int itemInfo,
			ArrayList<Scene> sceneList, ArrayList<Bitmap> bitmapList, ArrayList<String> temp) {
		super(listHolderFragment, itemInfo, sceneList);
		this.sceneList = new ArrayList<Scene>();
		this.bitmapList = new ArrayList<Bitmap>();
		this.analyst = new ArrayList<String>();
		this.sceneList.addAll(sceneList);
		this.bitmapList.addAll(bitmapList);
		this.analyst.addAll(temp);
	}

	public void add(Scene scene, Bitmap bitmap, String temp) {
		this.sceneList.add(scene);
		this.bitmapList.add(bitmap);
		this.analyst.add(temp);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View view;
		LayoutInflater vi = (LayoutInflater) this.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			view = vi.inflate(R.layout.item_info_white, parent, false);
		} else
			view = convertView;
			holder = new ViewHolder();
			holder.image = (ImageView) view.findViewById(R.id.imageicon);
			holder.location = (TextView) view.findViewById(R.id.location);
			holder.description = (TextView) view.findViewById(R.id.description);
			holder.isAnalyzed = (TextView) view.findViewById(R.id.analyzed);

			Scene currentScene = sceneList.get(position);
			double lat = currentScene.getLocation()[0];
			double lon = currentScene.getLocation()[1];
			holder.image.setImageBitmap(bitmapList.get(position));			
			holder.location.setText("Loc: " + lat + ", " + lon);
			holder.description.setText("Description: "
					+ currentScene.getDescription());
			holder.isAnalyzed.setText("Analyzed : " + analyst.get(position));
	
		return view;
	}
}
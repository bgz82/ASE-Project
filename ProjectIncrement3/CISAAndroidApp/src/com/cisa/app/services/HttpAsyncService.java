package com.cisa.app.services;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;




import com.cisa.app.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.Gravity;
import android.widget.Toast;

public class HttpAsyncService extends AsyncTask<String, Void, String>{

	private Activity context;
	
	public HttpAsyncService(Activity context) {
		this.context = context;
	}
	
	public Activity getContext() {
		return context;
	}

	public void setContext(Activity context) {
		this.context = context;
	}



	private ProgressDialog dialog;
	boolean error = true;

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(getContext());
		dialog.setMessage("Processing... Please wait...");
		dialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		int key = Integer.parseInt(params[0]);
		String url = params[1];
		HttpClient client = new DefaultHttpClient();
		HttpPost post;
		HttpGet get;
		HttpResponse response = null;
		String content = null;
		error = false;
		try {
			switch (key) {
			case Utils.UploadInfo:
				post = new HttpPost(url);
				post.setHeader("Accept", "application/json");
				post.setHeader("Content-Type", "application/json");
				
				StringEntity se = new StringEntity(params[2]);
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				post.setEntity(se);
				response = client.execute(post);
				
				break;
				
			case Utils.ItemsList:
				// Loading List items
				get = new HttpGet(url);
				response = client.execute(get);
				break;
				
			case Utils.UploadAnalysis:
				post = new HttpPost(url + "?sceneId="+params[3]+"&event"+params[4]);
				post.setHeader("Accept", "application/json");
				post.setHeader("Content-Type", "application/json");
				
				se = new StringEntity(params[2]);
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				post.setEntity(se);
				response = client.execute(post);
				break;
			default:
				break;
			}
			
			if(inspectResponse(response)) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				content = out.toString();
				return content;
			} else {
				error = true;
				cancel(true);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content;
	}
	
	protected void onPostExecute(String content) {
		dialog.dismiss();
		Toast toast;
		if (error) {
			toast = Toast.makeText(getContext(), "Unable to process request", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 25, 400);
			toast.show();
		} else {
			toast = Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 25, 400);
			toast.show();
		}
	}
	
	private boolean inspectResponse(HttpResponse response) {
		StatusLine statusLine = response.getStatusLine();
		if(statusLine.getStatusCode() == HttpStatus.SC_OK) {
			return true;
		}
		return false;
	}
}

package edu.umkc.dscrowd.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;
import edu.umkc.dscrowd.bean.Boundary;
import edu.umkc.dscrowd.bean.Image;

public class Utils {
	private static Image image = null;

	public static final String TAG_DIRECTORY = "DIRECTORY";
	public static String IMAGE_DIRECTORY_NAME = "coia";

	public static String HOST = "http://lasir.umkc.edu:8080/cisaserviceengine/";
	public static String RES_UPLOAD = "webresources/cisa/observerdata";
	public static String RES_DOWNLOAD = "webresources/cisa/observerdata";
	public static String RES_LIST_ITEMS = "webresources/cisa/itemslist";
	public static String RES_ANALYSIS = "webresources/cisa/analysis";
	
	public static String UPLOAD_URL = HOST + RES_UPLOAD;
	public static String DOWNLOAD_URL = HOST + RES_DOWNLOAD;
	public static String UPLOAD_ANALYSIS = HOST + RES_ANALYSIS;
	
	public static String RES_ANALYSIS_UPLOAD = "/data/updateanalysis";

	public static final int REGION_BUILDING = 1;
	public static final int REGION_BRIDGE = 2;
	public static final int REGION_ROAD = 3;
	public static final int REGION_HUMAN = 4;
	public static final int REGION_VEHICLE = 5;
	public static final int REGION_VEGETATION = 6;
	
	
	public static final int CLEAR = -1;

	public static final int UploadInfo = 0;
	public static final int ItemsList = 1;
	public static final int UploadAnalysis = 2;
	public static final int GetAnalysis = 3;

	public static final int RECT = 0;
	public static final int OVAL = 1;

	public static void setImage(String url) {

		File file;
		String encoded;
		if (image == null) {
			file = new File(url);
			byte[] bFile = new byte[(int) file.length()];

			try {
				encoded = Base64.encodeFromFile(url);
				System.out.println(encoded);

				image = new Image(encoded, getFileExtension(url));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
		}
	}

	public static Image getImage() {
		if (image != null)
			return image;
		else
			return new Image();
	}

	private static String getFileExtension(String url) {
		File file = new File(url);
		String fileName = file.getName();
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		else
			return "";
	}
	
	public static double[] extractLocationFromString(String locationString) {
		System.out.println(locationString);
		locationString = locationString.substring(1,
				locationString.length() - 1);
		String values[] = locationString.split(",");
		double latitude = Double.parseDouble(values[0]);
		double longitude = Double.parseDouble(values[1]);
		double[] location = { latitude, longitude };
		return location;
	}
	
	// Convert a view to bitmap
		public static Bitmap createDrawableFromView(Context context, View view) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
			view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
			view.buildDrawingCache();
			Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
	 
			Canvas canvas = new Canvas(bitmap);
			view.draw(canvas);
	 
			return bitmap;
		}

		public static int getShape(String typeText) {
			if(typeText.equalsIgnoreCase("Building")) {
				return 0;
			} else if(typeText.equalsIgnoreCase("Bridge")) {
				return 0;
			} else if(typeText.equalsIgnoreCase("Human")) {
				return 1;
			} else if(typeText.equalsIgnoreCase("Road")) {
				return 0;
			} else if(typeText.equalsIgnoreCase("Vegetation")) {
				return 1;
			} else if(typeText.equalsIgnoreCase("Vehicle")) {
				return 1;
			}
			return 0;
		}
		
		public static int getShape(int category) {
			int shape = 0;
			switch (category) {
			case Utils.REGION_BUILDING:
			case Utils.REGION_BRIDGE:
			case Utils.REGION_ROAD:
				shape = Utils.RECT;
				break;
			
			case Utils.REGION_HUMAN:
			case Utils.REGION_VEHICLE:
			case Utils.REGION_VEGETATION:
				shape = Utils.OVAL;
				break;

			default:
				break;
			}

			return shape;
		}

		public static JSONArray getBoundaries(String result) {
			System.out.println(result);
			
			 
			
			JSONArray jsonList;
			try {
				jsonList = new JSONArray(result);
				
				for(int i=0; i< jsonList.length(); i++) {
					JSONObject regionObj = jsonList.getJSONObject(i);
					System.out.println(regionObj);
					
					String regStr = regionObj.getString("region");
					System.out.println("reg array " + regStr);
					JSONArray regArray = new JSONArray(regStr);
					System.out.println("reg array " + regArray);
					
					return regArray;
					/*for(int j=0; j < regArray.length(); j++) {
						JSONObject area = new JSONObject(regArray.getString(j));
						JSONObject boundary = (JSONObject) area.get("boundry");
						System.out.println("Boundry: " + boundary);
						
					}*/
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}

			return null;
		}

}

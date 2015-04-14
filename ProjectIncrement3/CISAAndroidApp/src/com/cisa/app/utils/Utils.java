package com.cisa.app.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.cisa.app.bean.Image;

public class Utils {
	public static Image image = null;

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

	public static final int REGION_DAMAGE = 0;
	public static final int REGION_BUILDING = 1;
	public static final int REGION_PERSON = 2;
	public static final int REGION_VEHICLE = 3;
	public static final int CLEAR = -1;


    public static final int RECT = 0;
    public static final int OVAL = 1;
	public static final int UploadInfo = 0;
	public static final int ItemsList = 1;
	public static final int UploadAnalysis = 2;

/*	public static void setImage(String url) {

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
	}*/
	
	public static void setImage(String url) {

    	System.out.println("IN NOTOB");
		File file;
		String encoded;
		byte [] encodeByte,b;
		if (image == null) {
			file = new File(url);
			//byte[] bFile = new byte[(int) file.length()];

			try {
				      encoded = com.cisa.app.utils.Base64.encodeFromFile(url);
				     // System.out.println(encoded);
				      encodeByte=com.cisa.app.utils.Base64.decode(encoded);
				      Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
				      Bitmap insertImage = ThumbnailUtils.extractThumbnail(bitmap, 600, 600);
				      ByteArrayOutputStream baos = new ByteArrayOutputStream();
				      insertImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				      b = baos.toByteArray();
			          encoded = com.cisa.app.utils.Base64.encodeBytes(b);
			          image = new Image(encoded, getFileExtension(url));
			          b = null;
			          bitmap = null;
			          insertImage = null;
				} 
				
			 catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
		{
			
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
}

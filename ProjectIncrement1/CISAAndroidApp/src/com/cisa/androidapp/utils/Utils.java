package com.cisa.androidapp.utils;

import java.io.File;
import java.io.IOException;

import com.cisa.androidapp.bean.Image;

public class Utils {
	private static Image image = null;

	public static final String TAG_DIRECTORY = "DIRECTORY";
	public static String IMAGE_DIRECTORY_NAME = "coia";

	public static String HOST = "http://lasir.umkc.edu:8080/greengardenservice/";
	public static String EVE_GET_URL = HOST + "webresources/cisa/events";
	public static String RES_UPLOAD = "webresources/ggarden/observerdata";
	public static String RES_DOWNLOAD = "webresources/ggarden/observerdata";
	public static String RES_LIST_ITEMS = "webresources/ggarden/itemslist";
	public static String RES_ANALYSIS = "webresources/ggarden/analysis";
	
	public static String UPLOAD_URL = HOST + RES_UPLOAD;
	public static String DOWNLOAD_URL = HOST + RES_DOWNLOAD;
	public static String UPLOAD_ANALYSIS = HOST + RES_ANALYSIS;
	
	public static String RES_ANALYSIS_UPLOAD = "/data/updateanalysis";

    public static final int REGION_PLANT = 1;
    public static final int REGION_WATER = 2;
    public static final int REGION_DEBRIS = 3;
    public static final int REGION_DRYSOIL = 4;
    public static final int REGION_WETSOIL = 5;
    public static final int REGION_VEGETATION = 6;
	public static final int CLEAR = -1;

	public static final int UploadInfo = 0;
	public static final int ItemsList = 1;
	public static final int UploadAnalysis = 2;

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
}

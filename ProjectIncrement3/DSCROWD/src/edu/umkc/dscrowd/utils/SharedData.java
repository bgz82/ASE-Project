package edu.umkc.dscrowd.utils;

import java.util.ArrayList;

import edu.umkc.dscrowd.bean.Scene;
import android.app.Application;

public class SharedData extends Application {

	private ArrayList<Scene> sceneList = new ArrayList<Scene>();
	public SharedData() {
		
	}
	public ArrayList<Scene> getSceneList() {
		return sceneList;
	}
	public void setSceneList(ArrayList<Scene> sceneList) {
		this.sceneList = sceneList;
	}
	
	

}

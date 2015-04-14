package com.cisa.app.bean;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;

public class Scene implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8391958543836725081L;
	
	private String sceneId;
	private Image imageData;
	private String description;
	private double location[];
	private Date date;

	public Scene() {
		
	}
	public Scene(String sceneId, Image data, String desc, double[] loc) {
		this.sceneId = sceneId;
		this.imageData = data;
		this.description = desc;
		this.location = loc;
	}

	
	public void setSceneId(String sceneId) {
		this.sceneId = sceneId;
	}
	public String getSceneId() {
		return sceneId;
	}
	public Image getImageData() {
		return imageData;
	}

	public void setImageData(Image imageData) {
		this.imageData = imageData;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	@Override
	public String toString() {
		return "[" + getImageData() + " ," + getLocation()[1] + " ,"
				+ getDescription() + "}";
	}
}

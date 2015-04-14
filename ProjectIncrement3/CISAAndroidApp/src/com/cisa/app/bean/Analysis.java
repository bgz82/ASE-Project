package com.cisa.app.bean;

import java.util.ArrayList;

public class Analysis {

	private String deviceId;
	private Region region;
	private double[] enhanced_location;

	
	public Analysis(String deviceId, Region region, double[] enhanced_location) {
		super();
		this.deviceId = deviceId;
		this.region = region;
		this.enhanced_location = enhanced_location;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public double[] getEnhanced_location() {
		return enhanced_location;
	}

	public void setEnhanced_location(double[] enhanced_location) {
		this.enhanced_location = enhanced_location;
	}
}
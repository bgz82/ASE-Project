package com.cisa.app.bean;

import java.io.Serializable;

public class Image implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4000846187936782703L;
	private String type;
	private String data;
	
	public Image() {
	}
	public Image(String encoded, String fileExtension) {
		data = encoded;
		type = fileExtension;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}

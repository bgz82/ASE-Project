package com.cisa.androidapp.bean;

public class Region {
	int categroy;
	Boundary boundry;

	public Region(int categroy, Boundary boundry) {
		super();
		this.categroy = categroy;
		this.boundry = boundry;
	}

	public int getCategroy() {
		return categroy;
	}

	public void setCategroy(int categroy) {
		this.categroy = categroy;
	}

	public Boundary getBoundry() {
		return boundry;
	}

	public void setBoundry(Boundary boundry) {
		this.boundry = boundry;
	}

}

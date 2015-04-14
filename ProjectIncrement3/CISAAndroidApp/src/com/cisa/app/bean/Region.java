package com.cisa.app.bean;

public class Region {
	int categroy;
	Boundary boundry;
	int damageLevel;

	public Region(int categroy, Boundary boundry, int damageLevel) {
		super();
		this.categroy = categroy;
		this.boundry = boundry;
		this.damageLevel = damageLevel;
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

	public int getDamageLevel() {
		return damageLevel;
	}

	public void setDamageLevel(int damageLevel) {
		this.damageLevel = damageLevel;
	}
}

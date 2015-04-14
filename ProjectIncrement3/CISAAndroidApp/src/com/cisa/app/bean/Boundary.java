package com.cisa.app.bean;

public class Boundary {
	float left;
	float right;
	float top;
	float bottom;

	
	public Boundary(float left, float right, float top, float bottom) {
		super();
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	public float getLeft() {
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	public float getRight() {
		return right;
	}

	public void setRight(float right) {
		this.right = right;
	}

	public float getTop() {
		return top;
	}

	public void setTop(float top) {
		this.top = top;
	}

	public float getBottom() {
		return bottom;
	}

	public void setBottom(float bottom) {
		this.bottom = bottom;
	}
}
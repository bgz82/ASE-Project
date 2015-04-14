package com.cisa.app.views;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Camera mCamera;
	
	@SuppressWarnings("deprecation")
	public Preview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if(mCamera != null) {
				Log.i("Surface", "Camera is NOT NULL");
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} else {
				Log.i("Surface", "Camera is NULL");
			}
		} catch (IOException e) {
			Log.d("CAMERA", "ERROR setting camera Preview");
			e.printStackTrace();
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if(mHolder.getSurface() == null) {
			return;
		}
		try {
			mCamera.stopPreview();
		}catch(Exception e) {
		}
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setDisplayOrientation(90);
			mCamera.startPreview();
		} catch(Exception e) {
			Log.d("Camera", "Error starting camera preview");
		}		
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if(mCamera != null)
			mCamera.release();
	}
}
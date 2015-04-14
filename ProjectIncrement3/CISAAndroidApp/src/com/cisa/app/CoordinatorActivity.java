package com.cisa.app;

import android.app.Activity;
import android.os.Bundle;

import com.cisa.app.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class CoordinatorActivity extends Activity {

	private GoogleMap mMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.coordinator_layout);

		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		
	}
}

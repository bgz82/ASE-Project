package com.cisa.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
		
	}
	

	/* onClick Handler for Observer Image Button */
	public void observerOnClick(View view) {
		intent = new Intent(this, ObserverActivity.class);
		startActivity(intent);

	}

	/* onClick Handler for Analyst Image Button */
	public void analystOnClick(View view) {
		
		System.out.println("Button Clicked");
		intent = new Intent(this, AnalyzeListMap.class);
		intent.putExtra("list", true);
		startActivity(intent);
	}

	/* onClick Handler for Coordinate Image Button */
	public void coordinateOnClick(View view) {
		intent = new Intent(this, AnalyzeListMap.class);
		intent.putExtra("list", false);
		startActivity(intent);
	}
}

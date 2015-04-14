package edu.umkc.dscrowd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AnalyzeStatusActivity extends Activity {

	public String eventType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analyze_status);
		Intent intent = getIntent();
		eventType = intent.getStringExtra("event");
	}
	public void onAnalyzeMoreClick(View v) {
		Intent intent = new Intent(getBaseContext(), AnalyzeList.class);
		intent.putExtra("list", true);
		intent.putExtra("event", eventType);
		startActivity(intent);
	}
	
	public void onMenuClick(View v) {
		startActivity(new Intent(getBaseContext(), MainActivity.class));
	}
	
	public void onExitApplication(View v) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Exit Application?");
		alertDialogBuilder
				.setMessage("Click yes to Exit")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent homeIntent= new Intent(Intent.ACTION_MAIN);
						homeIntent.addCategory(Intent.CATEGORY_HOME);
						homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(homeIntent);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
}

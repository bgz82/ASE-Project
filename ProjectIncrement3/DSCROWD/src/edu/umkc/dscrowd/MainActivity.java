package edu.umkc.dscrowd;


import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

public class MainActivity extends Activity {

	Intent intent;
	String spintext;
	Context context=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	
	/* onClick Handler for Analyst Image Button */
	public void analystOnClick(View view) {
		
		Spinner event = (Spinner) findViewById(R.id.spinner1);
		spintext = event.getSelectedItem().toString();
		intent = new Intent(this, AnalyzeList.class);
		intent.putExtra("list", true);
		intent.putExtra("event", spintext);
		Date cDate = new Date();
		long time = cDate.getTime();
		intent.putExtra("msec", time);
		startActivity(intent);
	}

	/* onClick Handler for Coordinate Image Button */
	public void coordinateOnClick(View view) {
		Spinner event = (Spinner) findViewById(R.id.spinner1);
		spintext = event.getSelectedItem().toString();
		intent = new Intent(this, CoordinatorActivity.class);
		intent.putExtra("list", false);
		intent.putExtra("event", spintext);
		startActivity(intent);
	}
}

package com.cisa.androidapp.test;

import java.util.ArrayList;
import java.util.List;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cisa.androidapp.*;
public class Test1 extends ActivityInstrumentationTestCase2<MainActivity> {
    
	private MainActivity mActivity; 
	RadioButton rb; 
	private String resourceString; 
	
	public Test1(Class<MainActivity> activityClass) {
		super(activityClass);
		// TODO Auto-generated constructor stub
	}
	@Override protected void setUp() throws Exception {
		super.setUp(); 
		mActivity = this.getActivity(); 
		List<String> names = new ArrayList<String>();
		rb = (RadioButton) mActivity.findViewById(com.cisa.androidapp.R.id.plant);
		rb.setChecked(true);
		
		}
	public void testPreconditions() {
		assertNotNull(rb); }
	public void testText() {
		assertEquals(1,rb.isChecked()); 
		} 

}

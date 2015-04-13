package com.cisa.androidapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.app.AlertDialog;
import android.widget.EditText;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.cisa.androidapp.bean.Analysis;
import com.cisa.androidapp.bean.Boundary;
import com.cisa.androidapp.bean.Region;
import com.cisa.androidapp.bean.Scene;
import com.cisa.androidapp.services.HttpAsyncService;
import com.cisa.androidapp.utils.Base64;
import com.cisa.androidapp.utils.Utils;
import com.cisa.androidapp.views.CropView;
import com.cisa.androidapp.views.SelectionPreview;
import com.google.gson.Gson;

public class AnalyzeActivity extends Activity {

    private CropView cropView;
    private ImageView imageView;
    private static Button mark = null;
    private Scene scene;
    private String deviceID;
    private ArrayList<Analysis> analysisList = new ArrayList<Analysis>();
    private ArrayList<Region> selectedRegions = new ArrayList<Region>();
    private SelectionPreview selectionPreview;

    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyze_layout);
        System.out.println("BALU");
        cropView = (CropView)findViewById(R.id.cropView);
        selectionPreview = (SelectionPreview) findViewById(R.id.selectionPreview);
        dialog = new Dialog(AnalyzeActivity.this);
        deviceID = getDeviceID();
        initializeViewElements();
        Button addRegion = (Button) findViewById(R.id.saveRegion);
        System.out.println("BALU1");
        addRegion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                RectF rect = cropView.getRect();
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioObject);
                int id = radioGroup.getCheckedRadioButtonId();
                int type = 1;
                String typeText;
                switch (id) {
                    case R.id.radioPlant:
                        type = Utils.REGION_PLANT;
                        typeText = "Plant";
                        break;
                    case R.id.radioWater:
                        type = Utils.REGION_WATER;
                        typeText = "Water";
                        break;
                    case R.id.radioDebris:
                        type = Utils.REGION_DEBRIS;
                        typeText = "Debris";
                        break;
                    case R.id.radioDry:
                        type = Utils.REGION_DRYSOIL;
                        typeText = "Dry Soil";
                        break;
                    case R.id.radioWet:
                        type = Utils.REGION_WETSOIL;
                        typeText = "Wet Soil";
                        break;
                    case R.id.radioVegetation:
                        typeText = "Vegetation";
                        type = Utils.REGION_VEGETATION;
                        break;
                    default:
                        break;
                }
                final Region region = new Region(type, new Boundary(rect.left, rect.top, rect.right, rect.bottom));
                selectedRegions.add(region);
            }
        });

    }


    private String getDeviceID() {
        WifiManager m_wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String m_wlanMacAdd = m_wm.getConnectionInfo().getMacAddress();
        System.out.println(m_wlanMacAdd);
        return m_wlanMacAdd;
    }

    public void addRegion(View view) {
        Region region = new Region(1, new Boundary(2, 10, 0, 20));
        //selectedRegions.add(region);
    }


    private int showDamageLevelDialog() {
        dialog.setContentView(R.layout.seekbar_layout);
        dialog.setTitle("Select Damage Level");

        final SeekBar sb = (SeekBar) dialog.findViewById(R.id.seekBar);
        sb.incrementProgressBy(1);
        sb.setMax(4);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(final SeekBar seekBar,
                                          final int progress, final boolean fromUser) {
                update(seekBar);
            }
        });
        update(sb);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        return 100;
    }

    private void update(final SeekBar sb) {
        final LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.layout);

        final int colorStart = Color.parseColor("#BD4141");
        final int colorEnd = Color.parseColor("#2aff12");

        layout.setBackgroundColor(interpolateColor(colorStart, colorEnd,
                sb.getProgress() / 5f)); // assuming SeekBar max is 100
    }

    private float interpolate(final float a, final float b,
                              final float proportion) {
        return (a + ((b - a) * proportion));
    }

    private int interpolateColor(final int a, final int b,
                                 final float proportion) {
        final float[] hsva = new float[3];
        final float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }


    private void initializeViewElements() {
        Bitmap bitmap = null;
        imageView = (ImageView) findViewById(R.id.imageView);
        //mark = (Button) findViewById(R.id.mark);
        scene = (Scene) getIntent().getSerializableExtra("scene");

        byte[] encodeByte;
        try {
            encodeByte = Base64.decode(scene.getImageData().getData());
            bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            imageView.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void uploadDetails(View v) {

        Analysis analysis = new Analysis(deviceID, new Region(0, new Boundary(2, 5, 5, 8)), null);
        String sceneId = scene.getSceneId();

        AsyncTask<String, Void, String> response = new HttpAsyncService(AnalyzeActivity.this).execute(String.valueOf(Utils.UploadAnalysis), Utils.UPLOAD_ANALYSIS, new Gson().toJson(analysis), sceneId);

        try {
            Toast.makeText(getApplicationContext(), response.get().toString(), Toast.LENGTH_SHORT).show();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    public void viewInfo(View view) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        String text = getIntent().getExtras().get("iText").toString();
        double[] loc = getIntent().getExtras().getDoubleArray("iLoc");
        String dispText = "Description : " + text + "\n" + "Lattitude: " + loc[0] + "  Longitude: " + loc[1];
        dlgAlert.setMessage(dispText);
        dlgAlert.setTitle("Scene Information");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void OnClickReset(View v) {

        int i=0;
        while(i < selectedRegions.size())
        {
            selectedRegions.remove(i);
            i++;
        }
        RadioGroup rg = (RadioGroup)findViewById(R.id.radioObject);
        rg.clearCheck();
        rg.check(R.id.radioPlant);
        cropView.setMode(CropView.MODE_RECT);
        return;
    }

    public void onRadioClick(View v) {
        switch (v.getId()) {

            case R.id.radioPlant:
                cropView.setMode(CropView.MODE_RECT);
                break;
            case R.id.radioWater:
                cropView.setMode(CropView.MODE_RECT);
                break;
            case R.id.radioDebris:
                cropView.setMode(CropView.MODE_RECT);
                break;
            case R.id.radioDry:
                cropView.setMode(CropView.MODE_CIRCLE);
                break;
            case R.id.radioWet:
                cropView.setMode(CropView.MODE_CIRCLE);
                break;
            case R.id.radioVegetation:
                cropView.setMode(CropView.MODE_CIRCLE);
                break;
            default:
                break;
        }
        selectionPreview.clearSelection();
        cropView.showCropView();

    }
}
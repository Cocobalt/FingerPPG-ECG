package com.felhr.serialportexample;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;
import butterknife.ButterKnife;
import top.defaults.view.TextButton;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.serialportexample.Globals.GlobalBluetoothDevice;
import com.felhr.serialportexample.R;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.BodyDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import java.util.Calendar;
import java.util.Set;

public class ExpActivity extends AppCompatActivity implements View.OnClickListener {

    private View prepareToRecord;
    private RadioButton Playground;
    private RadioButton NaturalSta;
    private RadioButton LEDSta;
    private RadioButton IncandescentSta;
    private RadioButton NaturalHeadRandomly;
    private RadioButton NaturalHeadLR;
    private RadioButton NaturalHeadUD;
    private RadioButton NaturalTalking;
    private RadioButton NaturalWalking;
    private RadioButton NaturalRunning;
    private RadioButton NaturalLongPPG;

    public static ExpActivity expactivity = null;
    public Camera2Photographer camera2Photographer = null;
    public String nameagegender;
    public String nameagegenderexp;
    public String Name;
    public String age;
    public String sex;
    public String mode;

    public static TgStreamReader tgStreamReader;
    // TODO connection sdk
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice mBluetoothDevice;
    public static int currentState = 0;
    private static boolean ifConnected = false;

    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;
    private static final int MSG_CONNECT = 1003;

    public static String deviceName = null;
    public static int badPacketCount = 0;
    public static int GlobalFlag=0;
    public static long preSysTime=0;
    public static String rri = "0";
    public static String bpm="0";
    public int flag = 0;

    @Override
    public void onClick(View v) {
        // default method for handling onClick Events..

        RxPermissions rxPermissions = new RxPermissions(this);
        Playground = findViewById(R.id.radioPG);
        NaturalSta = findViewById(R.id.radioNS);
        LEDSta = findViewById(R.id.radioLED);
        IncandescentSta = findViewById(R.id.radioIncandescent);
        NaturalHeadRandomly = findViewById(R.id.radioRandom);
        NaturalHeadLR = findViewById(R.id.radioLR);
        NaturalTalking = findViewById(R.id.radioTalking);
        NaturalRunning = findViewById(R.id.radioRunning);
        NaturalLongPPG = findViewById(R.id.LongPPG);

        if (v.getId() == R.id.open_camera_RGB) {

            prepareToRecord = findViewById(R.id.open_camera_RGB);
            setPrepareToRecord(rxPermissions, "1");
        }
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            expactivity = this;
            super.onCreate(savedInstanceState);
            camera2Photographer = new Camera2Photographer();
            ExpActivity.this.setTitle("Experiment Mode");
            setContentView(R.layout.activity_experiment);
            ButterKnife.bind(this);
            Intent it = getIntent();
            nameagegender = it.getStringExtra("extra");
            Name = it.getStringExtra("name");
            age = it.getStringExtra("age");
            sex = it.getStringExtra("sex");
            mode = it.getStringExtra("mode");
            if(SearchDevice()){
                ConnectToBlueTooth();
                Toast.makeText(this, "Find Device BMD101!",Toast.LENGTH_LONG);
//            text_connect.setText("Find Device BMD101!");
//            btn_next.setVisibility(View.VISIBLE);
            }
        TextButton rgb = findViewById(R.id.open_camera_RGB);
        rgb.setOnClickListener(this);


    }

    private void startVideoRecordActivity(boolean islong) {
        Intent intent = new Intent(this, PhotographerActivity.class);
        Log.d("MianActivity", "exp former current working!");
        intent.putExtra("extra", nameagegenderexp);
        intent.putExtra("extra2",nameagegender);
        intent.putExtra("name", Name);
        intent.putExtra("age", age);
        intent.putExtra("isLong", String.valueOf(islong));
        intent.putExtra("sex", sex);
        intent.putExtra("mode", mode);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public boolean SearchDevice() {

        try {
            // TODO
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                System.out.println("Please enable your Bluetooth and re-run this program !");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i("AutoConnect", "error:" + e.getMessage());
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device: pairedDevices){
            if(device.getName().equals("BT04-A")){
                mBluetoothDevice=device;
                deviceName = "BT04-A";
                ifConnected = true;
                Toast.makeText(this, "successful!",Toast.LENGTH_LONG).show();
                return true;

            }
        }
        return false;
    }

    public void ConnectToBlueTooth() {

        BluetoothDevice bd = mBluetoothAdapter.getRemoteDevice(mBluetoothDevice.getAddress());
        GlobalBluetoothDevice d = (GlobalBluetoothDevice) getApplication();
        d.setBluetoothDevice(bd);
    }

    @SuppressLint("CheckResult")
    public void setPrepareToRecord(RxPermissions rxPermissions, String cameraID) {

        RxView.clicks(prepareToRecord)
                .compose(rxPermissions.ensure(Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(granted -> {
                    if (granted) {
                        String experiment = "";
                        boolean isLong = false;

                        if (Playground.isChecked()) {
                            experiment = "Playground";
                        } else if (NaturalSta.isChecked()) {
                            experiment = "Natural_Stationary";
                        } else if (LEDSta.isChecked()) {
                            experiment = "LED_stationary";
                        } else if (IncandescentSta.isChecked()) {
                            experiment = "Incandescent_stationary";
                        } else if (NaturalHeadRandomly.isChecked()) {
                            experiment = "Natural_Head_Randomly";
                        } else if (NaturalHeadLR.isChecked()) {
                            experiment = "Natural_Head_LeftRight";
                        } else if (NaturalTalking.isChecked()) {
                            experiment = "Natural_Talking";
                        } else if (NaturalRunning.isChecked()) {
                            experiment = "Natural_Running";
                        }else if (NaturalLongPPG.isChecked()){
                            experiment = "PPG";
                            isLong = true;
                        }

//                        MainActivity.nameAgeGenderExp = experiment;
//                        Log.d("exp", MainActivity.nameAgeGenderExp);

                        nameagegenderexp = experiment;
                        String cameraType;
                        if (MainActivity.isInfraredSurvey)
                            cameraType = "Infrared";
                        else
                            cameraType = "Front";



                        Camera2Photographer.setFilePath(Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/Record/" + nameagegender + "/" + cameraType + "/"+ experiment + "/");
                        Camera2Photographer.setFileName(experiment+"_Infrared");
                        Camera2Photographer.setFileName2(experiment+"_Front");
                        Camera2Photographer.setCameraID("5");
                        Camera2Photographer.setCameraID2("1");

                        startVideoRecordActivity(isLong);

                    } else {
                        Snackbar.make(prepareToRecord, getString(R.string.no_enough_permission), Snackbar.LENGTH_SHORT).setAction("Confirm", null).show();
                    }
                });
    }
}

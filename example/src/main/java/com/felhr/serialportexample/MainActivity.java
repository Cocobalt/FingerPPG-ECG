package com.felhr.serialportexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.Manifest;
import android.annotation.SuppressLint;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.data.Entry;
import com.google.android.material.snackbar.Snackbar;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import top.defaults.view.TextButton;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //Camera part
    private View prepareToExp;
    private EditText nameText;
    private EditText ageText;
    private RadioButton male;
    private RadioButton female;
    private RadioButton infraRed;
    private RadioButton frontCamera;
    private RadioButton ecg;
    public String nameAgeGender = "";
    public String nameAgeGenderExp = "";
    public static String Name = "";
    public static String age = "";
    public static String sex = "";
    public static String mode = "";


    public static boolean isInfraredSurvey = false;
    public static AppCompatActivity fa;

    private List<Entry> mEntries = new ArrayList<>();
    private List<String> bpmList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> heartBeatList = new ArrayList<>();
    private List<String> rawTimeList = new ArrayList<>();
    private List<String> rawDataList = new ArrayList<>();
    private Thread mThread;
    private Handler mHandler;
    private Random mRandom;
    private Integer dataNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fa= this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        MainActivity.this.setTitle("User Information");
        ButterKnife.bind(this);

        TextButton nextButton = findViewById(R.id.next);
        nextButton.setOnClickListener(this);

        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);

        male = findViewById(R.id.radioM);
        female = findViewById(R.id.radioF);

        infraRed = findViewById(R.id.radioI);
        frontCamera = findViewById(R.id.radioFront);
        ecg = findViewById(R.id.ecg);

        male.setOnClickListener(this);

        Intent it = getIntent();
        if (it != null){
            nameText.setText(it.getStringExtra("name"));
            ageText.setText(it.getStringExtra("age"));
            if (it.getStringExtra("sex") != null && it.getStringExtra("sex").equals("female")){
                female.setChecked(true);
            }
            else{
                male.setChecked(true);
            }

            if (it.getStringExtra("mode") != null && it.getStringExtra("mode").equals("infrared")){
                infraRed.setChecked(true);
            }
            else{
                frontCamera.setChecked(true);
            }
        }
    }

    @Override
    protected void onStop() {
        Log.d("Main", "stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("Main", "destroy");
        super.onDestroy();
    }

    private void startPhotographActivity() {
        Intent intent = new Intent(this, ExpActivity.class);
        intent.putExtra("extra", nameAgeGender);
        intent.putExtra("name", Name);
        intent.putExtra("age", age);
        intent.putExtra("sex", sex);
        intent.putExtra("mode", mode);
        startActivity(intent);

    }

    private void startEcgActivity() {
        Intent intent = new Intent(this, ECG_bluetooth_connect.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        // default method for handling onClick Events..
        RxPermissions rxPermissions = new RxPermissions(this);

        if (v.getId() == R.id.next) {
            prepareToExp = findViewById(R.id.next);
            setPrepareToExp(rxPermissions);
        }
    }

    @SuppressLint("CheckResult")
    public void setPrepareToExp(RxPermissions rxPermissions) {

        RxView.clicks(prepareToExp)
                .compose(rxPermissions.ensure(Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(granted -> {
                    if (granted) {
                        Name = nameText.getText().toString();
                        age = ageText.getText().toString();
                        String gender = "";

                        if (male.isChecked()) {
                            gender = "male";
                            sex = "male";

                        } else if (female.isChecked()) {
                            gender = "female";
                            sex = "female";
                        }

                        nameAgeGender = Name + "_" + age + "_" + gender;

                        if (infraRed.isChecked()) {
                            isInfraredSurvey = true;
                            mode = "infrared";
                            startPhotographActivity();
                        }
                        else if (frontCamera.isChecked()) {
                            isInfraredSurvey = false;
                            mode = "front";
                            startPhotographActivity();
                        }
                       else if(ecg.isChecked()) {
                           mode = "ecg";
                           startEcgActivity();
                        }


                    } else {
                        Snackbar.make(prepareToExp, getString(R.string.no_enough_permission), Snackbar.LENGTH_SHORT).setAction("Confirm", null).show();
                    }
                });
    }

}
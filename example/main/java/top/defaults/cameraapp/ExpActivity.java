package top.defaults.cameraapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.RadioButton;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.ButterKnife;
import top.defaults.camera.Camera2Photographer;
import top.defaults.view.TextButton;
import android.support.v7.app.AppCompatActivity;

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
    public static String nameAgeGenderExp = "";

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
        NaturalHeadUD = findViewById(R.id.radioUD);
        NaturalTalking = findViewById(R.id.radioTalking);
        NaturalWalking = findViewById(R.id.radioWalking);
        NaturalRunning = findViewById(R.id.radioRunning);

        if (v.getId() == R.id.open_camera_RGB) {
            prepareToRecord = findViewById(R.id.open_camera_RGB);
            setPrepareToRecord(rxPermissions, "1");
        }
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExpActivity.this.setTitle("Experiment Mode");
        setContentView(R.layout.activity_experiment);
        ButterKnife.bind(this);

        TextButton rgb = findViewById(R.id.open_camera_RGB);
        rgb.setOnClickListener(this);
    }

    private void startVideoRecordActivity() {
        Intent intent = new Intent(this, PhotographerActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        } else if (NaturalHeadUD.isChecked()) {
                            experiment = "Natural_Head_UpDown";
                        } else if (NaturalTalking.isChecked()) {
                            experiment = "Natural_Talking";
                        } else if (NaturalWalking.isChecked()) {
                            experiment = "Natural_Walking";
                        } else if (NaturalRunning.isChecked()) {
                            experiment = "Natural_Running";
                        }

                        nameAgeGenderExp = MainActivity.nameAgeGender + "_" + experiment;
                        String cameraType;
                        if (MainActivity.isInfraredSurvey)
                            cameraType = "Infrared";
                        else
                            cameraType = "Front";

                        Camera2Photographer camera2Photographer = new Camera2Photographer();
                        Camera2Photographer.setFilePath(Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/Record/" + MainActivity.nameAgeGender + "/" + cameraType + "/");
                        Camera2Photographer.setFileName(nameAgeGenderExp + "_Infrared");
                        Camera2Photographer.setFileName2(nameAgeGenderExp + "_Front");
                        Camera2Photographer.setCameraID("5");
                        Camera2Photographer.setCameraID2("1");
                        startVideoRecordActivity();

                    } else {
                        Snackbar.make(prepareToRecord, getString(R.string.no_enough_permission), Snackbar.LENGTH_SHORT).setAction("Confirm", null).show();
                    }
                });
    }
}

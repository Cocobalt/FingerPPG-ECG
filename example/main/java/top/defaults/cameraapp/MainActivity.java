package top.defaults.cameraapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import com.jakewharton.rxbinding2.view.RxView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.ButterKnife;
import top.defaults.view.TextButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private View prepareToExp;
    private EditText nameText;
    private EditText ageText;
    private RadioButton male;
    private RadioButton female;
    private RadioButton infraRed;
    private RadioButton frontCamera;
    public static String nameAgeGender = "";
    public static boolean isInfraredSurvey = false;

    @Override
    public void onClick(View v) {
        // default method for handling onClick Events..
        RxPermissions rxPermissions = new RxPermissions(this);

        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);

        male = findViewById(R.id.radioM);
        female = findViewById(R.id.radioF);

        infraRed = findViewById(R.id.radioI);
        frontCamera = findViewById(R.id.radioFront);

        if (v.getId() == R.id.next) {
            prepareToExp = findViewById(R.id.next);
            setPrepareToExp(rxPermissions);
        }
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.this.setTitle("User Information");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        TextButton nextButton = findViewById(R.id.next);
        nextButton.setOnClickListener(this);
    }

    private void startExpActivity() {
        Intent intent = new Intent(this, ExpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("CheckResult")
    public void setPrepareToExp(RxPermissions rxPermissions) {

        RxView.clicks(prepareToExp)
                .compose(rxPermissions.ensure(Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(granted -> {
                    if (granted) {
                        String name = nameText.getText().toString();
                        String age = ageText.getText().toString();
                        String gender = "";

                        if (male.isChecked()) {
                            gender = "male";
                        } else if (female.isChecked()) {
                            gender = "female";
                        }

                        nameAgeGender = name + "_" + age + "_" + gender;

                        if (infraRed.isChecked())
                            isInfraredSurvey = true;
                        else if (frontCamera.isChecked())
                            isInfraredSurvey = false;

                        startExpActivity();

                    } else {
                        Snackbar.make(prepareToExp, getString(R.string.no_enough_permission), Snackbar.LENGTH_SHORT).setAction("Confirm", null).show();
                    }
        });
    }
}
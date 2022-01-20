package com.felhr.serialportexample;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.RggbChannelVector;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.felhr.serialportexample.CameraPPGutils.HRCompute;


import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.felhr.serialportexample.Globals.GlobalBluetoothDevice;
import com.github.mikephil.charting.data.Entry;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.BodyDataType;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import top.defaults.cameraapp.options.Commons;

public class PhotographerActivity extends AppCompatActivity implements SensorEventListener {
    static String serialstr;
    Photographer photographer;
    PhotographerHelper photographerHelper;
    private static boolean isRecordingVideo;
    private static boolean hasUploadedTimeStamp=false;
    public static boolean isDataStart = true;

    boolean isRecording = false; //is recording  audio or not
    AudioRecord audioRecord = null;

    File recordingFile;//file that stores recorded audio
    static File parent = null; //file directory

    //audio format
    int bufferSize = 0;
    int sampleRateInHz = 11025;
    int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //mono-channel
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT; //16-bit

    //log Tag
    private static final String TAG = "MainActivity";
    String mAudioTAG = "AudioRecord";

    //prefix of filename
    public static String nameAgeGender;
    public static String nameAgeGenderExp;

    //SensorManager
    private SensorManager mSensorManager;

    //Sensors: IMU + ambient light
    private Sensor mAccelerometer, mGyroscope, mMagnetometer, mAmbientlight;

    private HRCompute HeartRate;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession previewSession;
    private CaptureRequest.Builder previewCaptureRequestBuilder;

    //    private LineChart mChart;
    private boolean plotData = true;
    private Thread plotThread;

    public String name;
    public String age;
    public boolean isLong;
    public String sex;
    public String mode;



    @BindView(R.id.preview)
    CameraView preview;
    @BindView(R.id.preview2)
    CameraView preview2;

    @BindView(R.id.status)
    TextView statusTextView;

    @BindView(R.id.action)
    ImageButton actionButton;

    @BindView(R.id.zoomValue)
    TextView zoomValueTextView;

    @BindView(R.id.ambien_light)
    TextView ambientlight;

    @BindView(R.id.serial_port)
    TextView serial;

    @BindView(R.id.simpleSeekBar)
    SeekBar RGBGainBar;

    @BindView(R.id.CountDown)
    TextView Count1;

    @BindView(R.id.CountDown2)
    TextView Count2;

//    ECG Part
    public static TgStreamReader tgStreamReader;
    // TODO connection sdk
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice mBluetoothDevice;
    public static BluetoothDevice bd;
    public static int currentState = 0;

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

    private List<Entry> mEntries = new ArrayList<>();
    private List<String> bpmList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> heartBeatList = new ArrayList<>();
    private List<String> rawTimeList = new ArrayList<>();
    private List<String> rawDataList = new ArrayList<>();
    private Thread mThread;
    private Handler ecgHandler;
    private Random mRandom;
    private Integer dataNum;

    private int currentFlash = Values.FLASH_AUTO;

    private static final int[] FLASH_OPTIONS = {
            Values.FLASH_AUTO,
            Values.FLASH_OFF,
            Values.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };



    // initialize usbreceiver
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    if (usbService != null)
                        usbService.write(hexToAscii("2032").getBytes());
                    if (usbService != null)
                        usbService.write(hexToAscii("203420").getBytes());
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // Usb parameters
    public UsbService usbService;
    private MyHandler mHandler;


    //Usb handler
    private static class MyHandler extends Handler {
        private final WeakReference<PhotographerActivity> mActivity;

        public MyHandler(PhotographerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;

                    PhotographerActivity.serialstr = asciiToHex(data) + " " + String.valueOf(PhotographerActivity.isRecordingVideo);
                    if (PhotographerActivity.isRecordingVideo) {
                        try {

                            String accCsvFileName =  nameAgeGenderExp + "_wave.csv";
                            File accFile = new File(PhotographerActivity.parent, accCsvFileName);
                            if (!accFile.exists()) {
                                accFile.createNewFile();
                            }
//                        Toast.makeText(mActivity.get(), asciiToHex(data),Toast.LENGTH_SHORT).show();
                            FileWriter accFw = new FileWriter(accFile.getAbsoluteFile(), true);
                            BufferedWriter accBw = new BufferedWriter(accFw);
                            //creating Calendar instance
                            Calendar calendar = Calendar.getInstance();
                            //Returns current time in millis
                            long timeMilli = calendar.getTimeInMillis();
                            //from left to right: timestamp, x, y, z
                            String content = timeMilli + "," + asciiToHex(data);

                            accBw.write(content);
                            accBw.newLine();
                            accBw.close();

                        } catch (IOException e) {

                            e.printStackTrace();
                        }
                    }

                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }


    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    //add usbPermission
    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    //convert ascii string to Hex string
    private static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }

        return hex.toString();
    }

    //convert Hex string to ascii string
    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }


    @OnClick(R.id.action)
    void action() {
        TextureView cameraTextureView = findViewById(R.id.TV_Preview1);
        int mode = photographer.getMode();
        if (mode == Values.MODE_VIDEO) {
            if (isRecordingVideo) {
                stopRecording();//stop audio recording
                finishRecordingIfNeeded();
                if (usbService != null)
                    usbService.write(hexToAscii("2033").getBytes());

            } else {
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(this);
                isDataStart = true;
                String url ="http://192.168.1.100:5000";
                ConnectToBlueTooth();
                Toast.makeText(this, "startECG!", Toast.LENGTH_LONG).show();
// Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });


                stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

// Add the request to the RequestQueue.
                queue.add(stringRequest);



                if (usbService != null)
                    usbService.write(hexToAscii("2032").getBytes());
                if (usbService != null)
                    usbService.write(hexToAscii("203420").getBytes());

                isRecordingVideo = true;

                if (!isLong) {
                    HeartRate.measurePulse(cameraTextureView, this, nameAgeGender, nameAgeGenderExp);
                }
                else{

                    new CountDownTimer(30000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            if (MainActivity.isInfraredSurvey) {
                                Count2.setText(String.valueOf(millisUntilFinished / 1000));

                            } else {
                                Count1.setText(String.valueOf(millisUntilFinished / 1000));

                            }
                        }

                        public void onFinish() {
                            CameraStop();
                            isDataStart = false;
                            setIsRecordingVideo(false);
                        }
                    }.start();
                }
                actionButton.setEnabled(true);
                actionButton.setVisibility(View.VISIBLE);
            }
        } else if (mode == Values.MODE_IMAGE) {
            if (MainActivity.isInfraredSurvey)
                photographer.takePicture();
            else
                photographer.takePicture2();
        }
    }

    @OnClick(R.id.flash_torch)
    void toggleFlashTorch() {
        int flash = photographer.getFlash();
        if (flash == Values.FLASH_TORCH) {
            photographer.setFlash(currentFlash);
            photographer.setFlash2(currentFlash);
        } else {
            photographer.setFlash(Values.FLASH_TORCH);
            photographer.setFlash2(Values.FLASH_TORCH);
        }
    }

    public static void connectHTTP(String sUrl) throws Exception {
        URL url = new URL(sUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();
    }

    public void ConnectToBlueTooth(){
        GlobalBluetoothDevice t = ((GlobalBluetoothDevice) getApplication());
        bd = t.getBluetoothDevice();
        if (bd.equals(null)) {
            Toast.makeText(this, "null!", Toast.LENGTH_LONG);
        }
        else {
            createStreamReader(bd);
            tgStreamReader.connectAndStart();
        }
    }

    public void createStreamReader(BluetoothDevice bd){

        if(tgStreamReader == null){
            tgStreamReader = new TgStreamReader(bd,callback);
            tgStreamReader.startLog();
        }else{
            tgStreamReader.setTgStreamHandler(callback);
        }
    }

    public TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d("AutoConnect", "connectionStates change to: " + connectionStates);
            currentState  = connectionStates;
            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTED:
                    System.out.println("Connected");
                    Log.v("states","Connected");
                    //获取当前时间
                    break;
                case ConnectionStates.STATE_WORKING:

                    Log.v("states","Working");
                    System.out.println("Subscribe");
                    //每次进入工作状态时将前一次的记录清空
                    LinkDetectedHandler.sendEmptyMessageDelayed(1234, 5000);
                    break;
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    //get data time out
                    Log.v("states","STATE_GET_DATA_TIME_OUT");
                    break;
                case ConnectionStates.STATE_COMPLETE:
                    //read file complete
                    Log.v("states","Complete");
                    break;
                case ConnectionStates.STATE_STOPPED:
                    Log.v("states","Unsubscribe");
                    System.out.println("Unsubscribe");
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    Log.v("states","Disconnect");
                    System.out.println("Disconnect");

                    break;
                case ConnectionStates.STATE_ERROR:
                    Log.v("states","Error");
                    Log.d("AutoConnect","Connect error, Please try again!");
                    break;
                case ConnectionStates.STATE_FAILED:
                    Log.v("states","Failed");
                    Log.d("AutoConnect","Connect failed, Please try again!");
                    break;
            }
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_STATE;
            msg.arg1 = connectionStates;
            LinkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onRecordFail(int a) {
            // TODO Auto-generated method stub
            Log.e("AutoConnect","onRecordFail: " +a);

        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) {
            // TODO Auto-generated method stub
            badPacketCount ++;
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = MSG_UPDATE_BAD_PACKET;
            msg.arg1 = badPacketCount;
            LinkDetectedHandler.sendMessage(msg);

        }

        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            // TODO Auto-generated method stub
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype;
            msg.arg1 = data;
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
        }

    };

    @SuppressLint("HandlerLeak")
    public Handler LinkDetectedHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case BodyDataType.CODE_RAW:
                    System.out.println(msg.arg1);
                    double voltage = msg.arg1 * (1.8/4096) / 2000;
                    long rawTime = Calendar.getInstance().getTimeInMillis();
                    rawDataList.add(String.valueOf(voltage));
                    rawTimeList.add(String.valueOf(rawTime));
                    break;

                case BodyDataType.CODE_HEATRATE:
                    if (GlobalFlag != 0) {
                        bpm = Integer.toString(msg.arg1);
                        long currentSysTime = Calendar.getInstance().getTimeInMillis();
                        rri = String.valueOf(currentSysTime - preSysTime);
                        bpmList.add(bpm);
                        timeList.add(String.valueOf(currentSysTime));
                    }
                    preSysTime = Calendar.getInstance().getTimeInMillis();
                    GlobalFlag = 1;
                    dataNum += 1;
                    System.out.println("CODE_HEATRATE");
                    break;

                case BodyDataType.CODE_POOR_SIGNAL:
                    System.out.println("CODE_POOR_SIGNAL");

                    break;
                case MSG_UPDATE_BAD_PACKET:
                    System.out.println("MSG_UPDATE_BAD_PACKET");
                    break;

                case MSG_UPDATE_STATE:
                    System.out.println("MSG_UPDATE_STATE");
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private void saveData() {
        try {
            File dir = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + "ecgRecord");
            }
            else
            {
                dir = new File(Environment.getExternalStorageDirectory() + "/" + "ecgRecord");
            }

            // Make sure the path directory exists.
            if (!dir.exists())
            {
                // Make it, if it doesn't exit
                boolean success = dir.mkdirs();
                if (!success)
                {
                    dir = null;
                }
            }
            Toast.makeText(this,dir.toString(),Toast.LENGTH_LONG).show();
            File hrFile = new File(dir+ File.separator + "ecg_hr" + ".csv");
            File hbFile = new File(dir+ File.separator + "ecg_hb" + ".csv");
            hrFile.createNewFile();
            hbFile.createNewFile();
//
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hrFile), "UTF-8"));  // 防止出现乱码
            // 添加头部
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("TimeStamp", "Heart Rate"));
            // 添加内容
            Toast.makeText(this, String.valueOf(bpmList.size()), Toast.LENGTH_LONG).show();
            for (int i = 0; i < bpmList.size(); i++) {
                csvPrinter.printRecord(
                        timeList.get(i),
                        bpmList.get(i)
                );
            }
            csvPrinter.printRecord();
            csvPrinter.flush();

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(hbFile), "UTF-8"));  // 防止出现乱码
            // 添加头部
            csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("TimeStamp", "Heart Beat"));
            // 添加内容
            for (int i = 0; i < rawDataList.size(); i++) {
                csvPrinter.printRecord(
                        rawTimeList.get(i),
                        rawDataList.get(i)
                );
            }
            csvPrinter.printRecord();
            csvPrinter.flush();
            tgStreamReader.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onStop() {
//        finish();
        Log.d("photograph", "Stopped");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("photograph", "Destroyed");
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent it = getIntent();
        nameAgeGenderExp = it.getStringExtra("extra");
        nameAgeGender = it.getStringExtra("extra2");
        name = it.getStringExtra("name");
        age = it.getStringExtra("age");
        String l = it.getStringExtra("isLong");
        sex = it.getStringExtra("sex");
        mode = it.getStringExtra("mode");
        dataNum = 0;
        if (l.equals("true"))
        {
            isLong = true;
        }
        else {
            isLong = false;
        }
        Log.d("long", l + String.valueOf(isLong));

        if (usbService != null) {
            usbService.write(hexToAscii("2032").getBytes());
            usbService.write(hexToAscii("203420").getBytes());
        }

        PhotographerActivity.this.setTitle("Sensor Fusion");
        setContentView(R.layout.activity_video_record);
        //initialize and register motion sensors and ambient light sensor
        initializeSensors();
//        initializeChart();

        mHandler = new MyHandler(this);

        String cameraType;
        if (MainActivity.isInfraredSurvey)
            cameraType = "Infrared";
        else
            cameraType = "Front";

//        startPlot();
        //calculate buffersize
        //calculate buffersize
        bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        //Initialize AudioRecord
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize);
        //parent directory for file storage
        File dir = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + "ppgRecord");
        }
        else
        {
            dir = new File(Environment.getExternalStorageDirectory() + "/" + "ppgRecord");
        }

        // Make sure the path directory exists.
        if (!dir.exists())
        {
            // Make it, if it doesn't exit
            boolean success = dir.mkdirs();
            if (!success)
            {
                dir = null;
            }
        }
        parent = dir;

        if (!parent.exists())
            parent.mkdirs();//mkdir if not exist

        // Request camera permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        // Request saving permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        ButterKnife.bind(this);
        RGBGainBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        photographer.setRGBValue(i);
                        photographer.updatePreview2(null);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        preview.setFocusIndicatorDrawer(new CanvasDrawer() {
            private static final int SIZE = 300;
            private static final int LINE_LENGTH = 50;

            @Override
            public Paint[] initPaints() {
                Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                focusPaint.setStyle(Paint.Style.STROKE);
                focusPaint.setStrokeWidth(2);
                focusPaint.setColor(Color.WHITE);
                return new Paint[]{focusPaint};
            }

            @Override
            public void draw(Canvas canvas, Point point, Paint[] paints) {
                if (paints == null || paints.length == 0) return;

                int left = point.x - (SIZE / 2);
                int top = point.y - (SIZE / 2);
                int right = point.x + (SIZE / 2);
                int bottom = point.y + (SIZE / 2);

                Paint paint = paints[0];

                canvas.drawLine(left, top + LINE_LENGTH, left, top, paint);
                canvas.drawLine(left, top, left + LINE_LENGTH, top, paint);

                canvas.drawLine(right - LINE_LENGTH, top, right, top, paint);
                canvas.drawLine(right, top, right, top + LINE_LENGTH, paint);

                canvas.drawLine(right, bottom - LINE_LENGTH, right, bottom, paint);
                canvas.drawLine(right, bottom, right - LINE_LENGTH, bottom, paint);

                canvas.drawLine(left + LINE_LENGTH, bottom, left, bottom, paint);
                canvas.drawLine(left, bottom, left, bottom - LINE_LENGTH, paint);
            }
        });

        preview2.setFocusIndicatorDrawer(new CanvasDrawer() {
            private static final int SIZE = 300;
            private static final int LINE_LENGTH = 50;

            @Override
            public Paint[] initPaints() {
                Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                focusPaint.setStyle(Paint.Style.STROKE);
                focusPaint.setStrokeWidth(2);
                focusPaint.setColor(Color.WHITE);
                return new Paint[]{focusPaint};
            }

            @Override
            public void draw(Canvas canvas, Point point, Paint[] paints) {
                if (paints == null || paints.length == 0) return;

                int left = point.x - (SIZE / 2);
                int top = point.y - (SIZE / 2);
                int right = point.x + (SIZE / 2);
                int bottom = point.y + (SIZE / 2);

                Paint paint = paints[0];

                canvas.drawLine(left, top + LINE_LENGTH, left, top, paint);
                canvas.drawLine(left, top, left + LINE_LENGTH, top, paint);

                canvas.drawLine(right - LINE_LENGTH, top, right, top, paint);
                canvas.drawLine(right, top, right, top + LINE_LENGTH, paint);

                canvas.drawLine(right, bottom - LINE_LENGTH, right, bottom, paint);
                canvas.drawLine(right, bottom, right - LINE_LENGTH, bottom, paint);

                canvas.drawLine(left + LINE_LENGTH, bottom, left, bottom, paint);
                canvas.drawLine(left, bottom, left, bottom - LINE_LENGTH, paint);
            }
        });

        photographer = PhotographerFactory.createPhotographerWithCamera2(this, preview, preview2);
        photographerHelper = new PhotographerHelper(photographer);
        photographerHelper.setFileDir(Commons.MEDIA_DIR);
        photographer.setOnEventListener(new SimpleOnEventListener() {
            @Override
            public void onDeviceConfigured() {
                if (photographer.getMode() == Values.MODE_VIDEO) {
                    actionButton.setImageResource(R.drawable.record);
                } else {
                    actionButton.setImageResource(R.drawable.record);
                }
            }

            @Override
            public void onZoomChanged(float zoom) {
                zoomValueTextView.setText(String.format(Locale.getDefault(), "%.1fX", zoom));
            }

            @Override
            public void onStartRecording() {
                actionButton.setEnabled(true);
                actionButton.setImageResource(R.drawable.record);
                statusTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinishRecording(String filePath) {
                HeartRate.saveData(1);
                HeartRate.saveData(2);
                Toast.makeText(PhotographerActivity.this, "Finish!!!!", Toast.LENGTH_LONG).show();

                announcingNewFile(filePath);
            }

            @Override
            public void onShotFinished(String filePath) {
                HeartRate.saveData(1);
                HeartRate.saveData(2);
                Toast.makeText(PhotographerActivity.this, "Finish!!!!", Toast.LENGTH_LONG).show();
                announcingNewFile(filePath);
            }

            @Override
            public void onError(Error error) {
                Timber.e("Error happens: %s", error.getMessage());
            }
        });

        photographer.setOnEventListener2(new SimpleOnEventListener() {
            @Override
            public void onDeviceConfigured() {
                if (photographer.getMode() == Values.MODE_VIDEO) {
                    actionButton.setImageResource(R.drawable.record);
                } else {
                    actionButton.setImageResource(R.drawable.ic_camera);
                }
            }

            @Override
            public void onZoomChanged(float zoom) {
                zoomValueTextView.setText(String.format(Locale.getDefault(), "%.1fX", zoom));
            }

            @Override
            public void onStartRecording() {
                actionButton.setEnabled(true);
                actionButton.setImageResource(R.drawable.record);
                statusTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinishRecording(String filePath) {
                announcingNewFile(filePath);
            }

            @Override
            public void onShotFinished(String filePath) {
                announcingNewFile(filePath);
            }

            @Override
            public void onError(Error error) {
                Timber.e("Error happens: %s", error.getMessage());
            }
        });
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null);
        if (MainActivity.isInfraredSurvey) {
            if (usbService != null) {
                usbService.write(hexToAscii("2032").getBytes());
                usbService.write(hexToAscii("203420").getBytes());
            }
        } else {
            if (usbService != null) {
                usbService.write(hexToAscii("2032").getBytes());
                usbService.write(hexToAscii("203420").getBytes());
            }
        }

        if (!isLong) {
            HeartRate = new HRCompute(this, findViewById(R.id.TV_Plot1));
            TextureView cameraTextureView = findViewById(R.id.TV_Preview1);
            SurfaceTexture previewSurfaceTexture = cameraTextureView.getSurfaceTexture();
            // TextureView isn't quite ready at the first onResume
            if (previewSurfaceTexture != null) {
                Surface previewSurface = new Surface(previewSurfaceTexture);
                CameraStart(previewSurface);
            }
        }


    }

    @Override
    protected void onPause() {
        finishRecordingIfNeeded();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
        CameraStop();
        if (!isLong) {
            if (HeartRate != null) HeartRate.stop();
            HeartRate = new HRCompute(this, findViewById(R.id.TV_Plot1));
        }
//        finish();

        super.onPause();
    }

    private void enterFullscreen() {
        View decorView = getWindow().getDecorView();
        decorView.setBackgroundColor(Color.BLACK);
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void finishRecordingIfNeeded() {
        if (isRecordingVideo) {
            isRecordingVideo = false;
            statusTextView.setVisibility(View.VISIBLE);
            actionButton.setEnabled(true);
            actionButton.setImageResource(R.drawable.record);
            Intent myIntent = new Intent();
            myIntent = new Intent(PhotographerActivity.this, MainActivity.class);
            myIntent.putExtra("name", name);
            myIntent.putExtra("age", age);
            myIntent.putExtra("sex", sex);
            myIntent.putExtra("mode", mode);
            hasUploadedTimeStamp = false;
            Toast.makeText(PhotographerActivity.this,"Save begin!",Toast.LENGTH_LONG).show();
            HeartRate.saveData(1);
            HeartRate.saveData(2);
            saveData();
            Toast.makeText(PhotographerActivity.this,"Save end!",Toast.LENGTH_LONG).show();
            startActivity(myIntent);
        }
    }

    private void announcingNewFile(String filePath) {
        Toast.makeText(PhotographerActivity.this, "File: " + filePath, Toast.LENGTH_LONG).show();
        Utils.addMediaToGallery(PhotographerActivity.this, filePath);
    }


    //load and store sensor parameters while recording
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //Record & store audio
    private void record() {
        isRecording = true;

        Log.d(mAudioTAG, "Recording!");
        //new thread for audio recording
        new Thread(() -> {
            isRecording = true;
            //'name_age_gender.pcm'
            recordingFile = new File(parent, nameAgeGenderExp + ".pcm");

            if (recordingFile.exists()) {
                recordingFile.delete();
            }
            //create file
            try {
                recordingFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error occurred when storing audio!");
            }
            //record and write pcm file
            try {
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(recordingFile)));
                byte[] buffer = new byte[bufferSize];
                audioRecord.startRecording();//start recording

                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.write(buffer[i]);
                    }
                }
                audioRecord.stop();
                dos.close();
            } catch (Throwable t) {
                Log.e(TAG, "Recording Failed");
            }
        }).start();
    }

    //Stop
    private void stopRecording() {
        isRecording = false;
    }

    private void initializeSensors() {
        //Initialize sensormanager & IMU sensors
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAmbientlight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //register sensorlisteners
        if (mAccelerometer != null) {
            //register listener
            mSensorManager.registerListener(this, mAccelerometer, 10000);
        } else {
            Log.d(TAG, "no access to Accelerometer");
        }

        if (mGyroscope != null) {
            //register listener
            mSensorManager.registerListener(this, mGyroscope, 10000);
        } else {
            Log.d(TAG, "no access to Gyroscope");
        }

        if (mMagnetometer != null) {
            //register listener
            mSensorManager.registerListener(this, mMagnetometer, 10000);
            ;
        } else {
            Log.d(TAG, "no access to Magnetometer");
        }

        if (mAmbientlight != null) {
            //register listener
            mSensorManager.registerListener(this, mAmbientlight, 10000);
            ;
        } else {
            Log.d(TAG, "no access to Ambientlight");
        }
    }

    // Camera Part
    void CameraStart(Surface previewSurface) {
        CameraManager cameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);

        try {
            // Get cameraID
            cameraId = Objects.requireNonNull(cameraManager).getCameraIdList()[0];
        } catch (CameraAccessException | NullPointerException e) {
            // no access
            Log.println(Log.ERROR, "camera", "No access to camera!");
        }
        try {
            // Check permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.println(Log.ERROR, "camera", "No camera permission!");

            }
            Objects.requireNonNull(cameraManager).openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            previewSession = session;

                            // Configurations
                            try {
                                previewCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                previewCaptureRequestBuilder.addTarget(previewSurface);
                                if (isLong){
                                    previewCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                                }
                                else {
                                    previewCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);// Flash on
                                }
                                HandlerThread thread = new HandlerThread("CameraPreview");
                                thread.start();

                                previewSession.setRepeatingRequest(previewCaptureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                if (e.getMessage() != null) {
                                    Log.println(Log.ERROR, "camera", e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.println(Log.ERROR, "camera", "Session configuration failed!");
                        }
                    };

                    try {
                        // CaptureSession
                        camera.createCaptureSession(Collections.singletonList(previewSurface), stateCallback, null); //1
                    } catch (CameraAccessException e) {
                        if (e.getMessage() != null) {
                            Log.println(Log.ERROR, "camera", e.getMessage());
                        }
                    }
                }

                // Disconnected
                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                }

                // Error
                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    if (cameraDevice != null)
                        cameraDevice.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException | SecurityException e) {
            if (e.getMessage() != null) {
                Log.println(Log.ERROR, "camera", e.getMessage());
            }
        }
    }

    // Close the camera
    public void CameraStop() {
        try {
            cameraDevice.close();
            cameraDevice = null;
        } catch (Exception e) {
            Log.println(Log.ERROR, "camera", "Cannot close camera device!" + e.getMessage());
        }
    }

    public void setIsRecordingVideo(boolean t) {

        stopRecording();//
        // audio recording
        finishRecordingIfNeeded();
        isRecordingVideo = t;
    }
}
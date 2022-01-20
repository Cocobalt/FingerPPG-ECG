package com.felhr.serialportexample.CameraPPGutils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
//import android.support.annotation.RequiresApi;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.felhr.serialportexample.MainActivity;
import com.felhr.serialportexample.PhotographerActivity;
import com.felhr.serialportexample.R;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class HRCompute {
    private final AppCompatActivity activity;
    private final Plot plot;
    private final CopyOnWriteArrayList<Long> valleys = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Measurement<Integer>> measurements = new CopyOnWriteArrayList<>();
    private final List<String> heartRates = new ArrayList<>();
    private final List<String> hrTimeList = new ArrayList<>();
    private final List<String> heartBeats = new ArrayList<>();
    private final List<String> hbTimeList = new ArrayList<>();
    private CountDownTimer timer;

    private int detectedValleys = 0; // number of detected valleys
    private int ticksPassed = 0; // count of time passed
    private int minimum = 2147483647; // 2^31-1
    private int maximum = -2147483648; // -2^31

    private final int measurementInterval = 30; // time interval between two measurements (unit: millisecond)
    private final int clipLength = 0; // for the beginning, time to be clipped
    public int measurementLength = 60000; // total measurement length time (including clipLength)
    private static int countDownTime = 60;

    public HRCompute(PhotographerActivity activity, TextureView TVPlot) {
        this.activity = activity;
        this.plot = new Plot(TVPlot); // for waveform plot
    }

    // Add one measurement and update
    private void add(int measurement) {
        Measurement<Integer> measurementWithDate = new Measurement<>(new Date(), measurement);
        long rawTime = Calendar.getInstance().getTimeInMillis();
        hbTimeList.add(String.valueOf(rawTime));
        heartBeats.add(String.valueOf(measurement));
        measurements.add(measurementWithDate);
        // Update the minimum and maximum of measurement
        if (measurement < minimum) minimum = measurement;
        if (measurement > maximum) maximum = measurement;
    }

    // Calculate the rolling average and normalize the results (for plot, not for data save)
    private CopyOnWriteArrayList<Measurement<Float>> getStdValues() {
        CopyOnWriteArrayList<Measurement<Float>> stdValues = new CopyOnWriteArrayList<>();

        for (int i = 0; i < measurements.size(); i++) {
            // for every measurement value, calculate the sum of itself and the data before
            int sum = 0;
            int rollingAverageSize = 3;
            for (int rollingAverageCounter = 0; rollingAverageCounter < rollingAverageSize; rollingAverageCounter++) {
                sum += measurements.get(Math.max(0, i - rollingAverageCounter)).measurement;
            }
            // get the rolling average and do the Min-Max Normalization
            Measurement<Float> stdValue =
                    new Measurement<>(
                            measurements.get(i).timestamp,
                            ((float) sum / rollingAverageSize - minimum) / (maximum - minimum));
            stdValues.add(stdValue);
        }

        return stdValues;
    }

    // Get last measurement stdValues (length: count)
    private CopyOnWriteArrayList<Measurement<Integer>> getLastStdValues(int count) {
        if (count < measurements.size()) {
            return new CopyOnWriteArrayList<>(measurements.subList(measurements.size() - 1 - count, measurements.size() - 1));
        } else {
            return measurements;
        }
    }

    public int getIndex(int pixelidx, int width, int height){
        int smallwidth = pixelidx % 36 + width / 2;
        int smallheight = (int)((int)(pixelidx) / 36) + height / 2;
        int finalidx = smallheight * width + smallwidth;
//        Log.d("Count", "pixelidx" + String.valueOf(pixelidx));
//        Log.d("Count", "divide" + String.valueOf((pixelidx) / 36));
//        Log.d("Count", "width" + String.valueOf(width));
//        Log.d("Count", "height" + String.valueOf(height));
//        Log.d("Count", "small height"+String.valueOf(smallheight));
//        Log.d("Count", "small width "+String.valueOf(smallwidth));
//        Log.d("Count", "finalidx"+String.valueOf(finalidx));
//        Log.d("Count", String.valueOf(0/36));

        return finalidx;
    }

    // Get last measurement's timestamp
    private Date getLastTimestamp() {
        return measurements.get(measurements.size() - 1).timestamp;
    }

    // Detect if there is a valley in the measurement sequence
    private boolean detectValley() {
        // Get last 14 stdValues
        final float valleyDetectionWindowSize = 14;
        CopyOnWriteArrayList<Measurement<Integer>> subList = getLastStdValues((int) valleyDetectionWindowSize);

        // not enough last measurements
        if (subList.size() < valleyDetectionWindowSize) {
            return false;
        } else {
            // Set a referenceValue
            Integer referenceValue = subList.get((int) Math.ceil(valleyDetectionWindowSize / 2)).measurement;

            // only if all the measurements >= referenceValue, decide there is a valley
            for (Measurement<Integer> measurement : subList) {
                if (measurement.measurement < referenceValue) return false;
            }

            // Filter out consecutive measurements due to too high measurement rate
            return (!subList.get((int) Math.ceil(valleyDetectionWindowSize / 2)).measurement.equals(
                    subList.get((int) Math.ceil(valleyDetectionWindowSize / 2) - 1).measurement));
//        String nameAgeGender = MainActivity.nameAgeGender;
        }
    }

    // Get the amount of red on the picture
    // Detect local minimums, calculate pulse
    public void measurePulse(TextureView textureView, PhotographerActivity main, String nameagegender, String nameagegenderexp) {
        //prefix of filename
        String nameAgeGenderExp = nameagegenderexp;
        String cameraType;

        if (MainActivity.isInfraredSurvey)
            cameraType = "Infrared";
        else
            cameraType = "Front";

        detectedValleys = 0;

        timer = new CountDownTimer(measurementLength, measurementInterval) {
            TextView textView = activity.findViewById(R.id.TextView1);
            TextView countDown = activity.findViewById(R.id.CountDown);
            TextView countDown2 = activity.findViewById(R.id.CountDown2);
            TextView text = activity.findViewById(R.id.textHint);
            TextView text2 = activity.findViewById(R.id.textHint2);

            @SuppressLint("SetTextI18n")

            @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onTick(long millisUntilFinished) {
                // Skip the first measurements, which are broken by exposure metering
                if (clipLength > (++ticksPassed * measurementInterval)) return;

                // Get time stamps and get Bitmap values from the textureView
                Thread thread;
                thread = new Thread(() -> {
                    Bitmap currentBitmap = textureView.getBitmap();

                    int width = textureView.getWidth();
                    int height = textureView.getHeight();
                    int[] pixels = new int[width * height];

                    // Get pixels from the bitmap
                    // Starting at (x,y) = (0, 0), total rows: width, total columns: height
                    currentBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

                    // Get timestamp
                    // Create Calendar instance
                    Calendar calendar = Calendar.getInstance();
                    // Return current time in millis
                    long timeMilli = calendar.getTimeInMillis();

                    // ARGB components decoding
                    // https://developer.android.com/reference/android/graphics/Color.html#decoding
                    StringBuilder bitmapValue = new StringBuilder();
                    int red = 0;
                    for (int pixelIndex = 0; pixelIndex < 36 * 36; pixelIndex++) {
//                        Log.d("Count", String.valueOf(pixelIndex));
                        int alphaValue = (pixels[getIndex(pixelIndex, width, height)] >> 24) & 0xff;
                        int redValue = (pixels[getIndex(pixelIndex, width, height)] >> 16) & 0xff;
                        int greenValue = (pixels[getIndex(pixelIndex, width, height)] >> 8) & 0xff;
                        int blueValue = (pixels[getIndex(pixelIndex, width, height)]) & 0xff;

                        // add the red component to plot the PPG waveform
                        red += redValue;

                        // save all four components with timestamps
                        bitmapValue.append(timeMilli);
                        bitmapValue.append(',');
                        bitmapValue.append(alphaValue);
                        bitmapValue.append(',');
                        bitmapValue.append(redValue);
                        bitmapValue.append(',');
                        bitmapValue.append(greenValue);
                        bitmapValue.append(',');
                        bitmapValue.append(blueValue);
                        bitmapValue.append('\n');
                    }
                    add(red); // add one measurement

                    // Remove the last newline character
                    bitmapValue.setLength(bitmapValue.length() - 1);

//                     Save each pixel's data for every measurement to .csv file
//                     from left to right: timestamp, alphaData, redData, greenData, blueData
                    try {

                        File parent = null; //file directory
                        parent = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/Record/" + nameagegender + "/" + cameraType + '/' + nameAgeGenderExp);
                        if (!parent.exists())
                            parent.mkdirs();//mkdir if not exist
                        String accCsvFileName = nameAgeGenderExp + "_RearPPG.csv";
                        File accFile = new File(parent, accCsvFileName);
                        if (!accFile.exists()) {
                            accFile.createNewFile();
                        }
                        FileWriter accFw = new FileWriter(accFile.getAbsoluteFile(), true);
                        BufferedWriter accBw = new BufferedWriter(accFw);

                        // write csv file
                        accBw.write(String.valueOf(bitmapValue));
                        accBw.newLine();
                        accBw.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    activity.runOnUiThread(() -> {
                        if (detectValley()) {
                            detectedValleys = detectedValleys + 1;
                            valleys.add(getLastTimestamp().getTime());

                            String currentValue = String.format(
                                    Locale.getDefault(),
                                    activity.getResources().getQuantityString(R.plurals.measurement_output, detectedValleys),
                                    // calculate valleys in one minute
                                    (valleys.size() == 1)
                                            ? (60f * (detectedValleys) / (Math.max(1, (measurementLength - millisUntilFinished - clipLength) / 1000f)))
                                            // the number of intervals is the number of detectedValleys - 1
                                            : ((60f * (detectedValleys - 1) / (Math.max(1, (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f))) + 1f),
                                    detectedValleys, (valleys.size() == 1)
                                            // the time used
                                            ? (1f * (measurementLength - millisUntilFinished - clipLength) / 1000f)
                                            : (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f);
                            textView.setText(currentValue);
                            // Count-down Clock
                            long rawTime = Calendar.getInstance().getTimeInMillis();
                            hrTimeList.add(String.valueOf(rawTime));
                            heartRates.add(currentValue);
                            String clock = String.valueOf(Math.round(millisUntilFinished / 1000f));
                            if (MainActivity.isInfraredSurvey) {
                                countDown2.setText(clock);
                                text2.setText("Seconds Left");
                            } else {
                                countDown.setText(clock);
                                text.setText("Seconds Left");
                            }
                        }
                    });

                    // Plot the waveform on a separate thread

                    Thread PlotThread = new Thread(() -> plot.draw(getStdValues()));
                    PlotThread.start();

                });
                thread.start();
            }

            @Override
            public void onFinish() {
                String currentValue = String.format(
                        Locale.getDefault(),
                        activity.getResources().getQuantityString(R.plurals.measurement_result, detectedValleys),
                        // the number of intervals is the number of detectedValleys - 1
                        (60f * (detectedValleys - 1) / (Math.max(1, (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f)) + 1f),
                        detectedValleys,
                        // the time used
                        1f * (valleys.get(valleys.size() - 1) - valleys.get(0)) / 1000f);
                textView.setText(currentValue);
//                long rawTime = Calendar.getInstance().getTimeInMillis();
//                hrTimeList.add(String.valueOf(rawTime));
//                heartRates.add(currentValue);
                // Count-down Clock
                if (MainActivity.isInfraredSurvey) {
                    countDown2.setText(String.valueOf(0));
                    text2.setText("Seconds Left");
                } else {
                    countDown.setText(String.valueOf(0));
                    text.setText("Seconds Left");
                }
                main.CameraStop();
                if (main.usbService != null)
                    main.usbService.write(hexToAscii("2033").getBytes());

                PhotographerActivity.isDataStart = false;
                main.setIsRecordingVideo(false);
            }
        };
        timer.start();
    }

    // stop
    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public void setMeasurementLength(int ml) {
        measurementLength = ml;
    }

    public void saveData(int dataType) {
        String saveType;
        if(dataType == 1) {
            saveType = "ppgHeartBeat";
        }
        else{
            saveType = "ppgHeartRate";
        }
        try {
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
//            Toast.makeText(this,dir.toString(),Toast.LENGTH_LONG).show();
            File file = new File(dir+ File.separator + saveType + ".csv");
//
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));  // 防止出现乱码
            // 添加头部
            if(dataType == 1) {
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("TimeStamp", "Heart Beat"));
                // 添加内容
                CopyOnWriteArrayList<Measurement<Float>> stdValues = getStdValues();
                for (int i = 0; i < hbTimeList.size(); i++) {
                    csvPrinter.printRecord(
                            hbTimeList.get(i),
                            stdValues.get(i).measurement
                    );
                }
                csvPrinter.printRecord();
                csvPrinter.flush();
            }

            else {
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("TimeStamp", "Heart Rate"));
                // 添加内容
                for (int i = 0; i < heartRates.size(); i++) {
                    csvPrinter.printRecord(
                            hrTimeList.get(i),
                            heartRates.get(i)
                    );
                }
                csvPrinter.printRecord();
                csvPrinter.flush();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

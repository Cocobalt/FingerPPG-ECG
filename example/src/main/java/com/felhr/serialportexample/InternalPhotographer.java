package com.felhr.serialportexample;

import android.app.Activity;

interface InternalPhotographer extends Photographer {

    void initWithViewfinder(Activity activity, CameraView preview);

    void initWithViewfinder2(Activity activity, CameraView preview);
}

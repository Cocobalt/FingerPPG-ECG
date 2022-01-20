package com.felhr.serialportexample.options;

import com.felhr.serialportexample.AspectRatio;

public class AspectRatioItem implements top.defaults.cameraapp.options.PickerItemWrapper<AspectRatio> {

    private AspectRatio aspectRatio;

    public AspectRatioItem(AspectRatio ratio) {
        aspectRatio = ratio;
    }

    @Override
    public String getText() {
        return aspectRatio.toString();
    }

    @Override
    public AspectRatio get() {
        return aspectRatio;
    }
}

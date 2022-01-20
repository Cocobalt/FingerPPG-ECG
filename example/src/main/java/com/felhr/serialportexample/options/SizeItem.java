package com.felhr.serialportexample.options;

import com.felhr.serialportexample.Size;

public class SizeItem implements top.defaults.cameraapp.options.PickerItemWrapper<Size> {

    private Size size;

    public SizeItem(Size size) {
        this.size = size;
    }

    @Override
    public String getText() {
        return size.getWidth() + " * " + size.getHeight();
    }

    @Override
    public Size get() {
        return size;
    }
}

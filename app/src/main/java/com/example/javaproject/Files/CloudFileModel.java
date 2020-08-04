package com.example.javaproject.Files;

public class CloudFileModel {

    public String mTitle;
    public String mImageURL;
    public String mkey;
    public String mSize;


    public CloudFileModel() {
    }


    public CloudFileModel(String title, String imageURL, String key, String size) {
        mTitle = title;
        mImageURL = imageURL;
        mkey = key;
        mSize = size;
    }

}

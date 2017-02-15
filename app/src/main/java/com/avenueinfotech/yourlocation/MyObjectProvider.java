package com.avenueinfotech.yourlocation;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by suken on 23-10-2016.
 */

public class MyObjectProvider {

    Context context;

        public MyObjectProvider(Context context) {
            this.context = context;
        }

        public ArrayList<MyObject> getObject(){

            ArrayList<MyObject> myObjects = new ArrayList<>();

            for (int i =1; i<=40; i++){
                MyObject myObject = new MyObject();
                myObject.setImageId(context.getResources().getIdentifier("img_".concat(String.valueOf(i)),"drawable",context.getPackageName()));
                myObject.setText("Image: "+i);
                myObjects.add(myObject);
            }

                return myObjects;
        }



    public class MyObject {

        int imageId;
        String text;

        public int getImageId(){
            return imageId;
        }

        public void setImageId(int imageId){
            this.imageId = imageId;
        }

        public String getText(){
            return text;
        }

        public void setText(String text){
            this.text = text;
        }
    }
}

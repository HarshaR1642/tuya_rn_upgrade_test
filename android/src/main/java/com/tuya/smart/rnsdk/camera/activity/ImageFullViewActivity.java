package com.tuya.smart.rnsdk.camera.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.tuya.drawee.view.DecryptImageView;
import com.tuya.smart.rnsdk.R;

import java.net.URL;

public class ImageFullViewActivity extends AppCompatActivity {

        private ScaleGestureDetector mScaleGestureDetector;
        private float mScaleFactor = 1.0f;
        //private ImageView mImageView;
        private DecryptImageView mImageView;

    String TAG = "ImageFullViewActivity";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_image_full_view);

            String attachPics = getIntent().getStringExtra("image");
            Log.d(TAG, "elango-attachPics:" + attachPics);

            mImageView = findViewById(R.id.imageView);
            mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

            if (attachPics.contains("@")) {
                int index = attachPics.lastIndexOf("@");
                try {
                    String decryption = attachPics.substring(index + 1);
                    String imageUrl = attachPics.substring(0, index);
                    Log.d(TAG, "elango message showPicture : " + imageUrl +", " + decryption);
                    mImageView.setImageURI(imageUrl, decryption.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Uri uri = null;
                try {
                    uri = Uri.parse(attachPics);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DraweeController controller = Fresco.newDraweeControllerBuilder().setUri(uri).build();
                mImageView.setController(controller);
                /*try {
                    Log.d(TAG, "elango-attachPics-url:" + attachPics);
                    URL url = new URL(attachPics);
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mImageView.setImageBitmap(bmp);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            mScaleGestureDetector.onTouchEvent(motionEvent);
            return true;
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                mScaleFactor *= scaleGestureDetector.getScaleFactor();
                mScaleFactor = Math.max(0.1f,
                        Math.min(mScaleFactor, 10.0f));
                mImageView.setScaleX(mScaleFactor);
                mImageView.setScaleY(mScaleFactor);
                return true;
            }
        }
    }
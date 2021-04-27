package com.tuya.smart.rnsdk.camera.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.squareup.picasso.Picasso;
import com.tuya.drawee.view.DecryptImageView;
import com.tuya.smart.rnsdk.R;
import com.tuya.smart.rnsdk.camera.bean.CameraMessageBean;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.NoSuchPaddingException;

/**
 * Created by huangdaju on 2018/3/5.
 */

public class AlarmDetectionAdapter extends RecyclerView.Adapter<AlarmDetectionAdapter.MyViewHolder> {

    private Context context;
    private LayoutInflater mInflater;
    private List<CameraMessageBean> cameraMessageBeans;
    private OnItemListener listener;

    String TAG = "AlarmDetectionActivity";

    public AlarmDetectionAdapter(Context context, List<CameraMessageBean> cameraMessageBeans) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.cameraMessageBeans = cameraMessageBeans;
    }

    public void updateAlarmDetectionMessage(List<CameraMessageBean> messageBeans) {
        if (null != cameraMessageBeans) {
            cameraMessageBeans.clear();
            cameraMessageBeans.addAll(messageBeans);
            notifyDataSetChanged();
        }
    }

    public void setListener(OnItemListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.camera_newui_more_motion_recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final CameraMessageBean ipcVideoBean = cameraMessageBeans.get(position);
        holder.mTvStartTime.setText(ipcVideoBean.getDateTime());
        holder.mTvDescription.setText(ipcVideoBean.getMsgContent());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (null != listener) {
                    listener.onLongClick(ipcVideoBean);
                }
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != listener) {
                    listener.onItemClick(ipcVideoBean);
                }
            }
        });
        holder.showPicture(ipcVideoBean);
    }

    @Override
    public int getItemCount() {
        return cameraMessageBeans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvStartTime;
        private TextView mTvDescription;
        //private DecryptImageView mSnapshot;
        private ImageView mSnapshot;

        public MyViewHolder(final View view) {
            super(view);
            mTvStartTime = view.findViewById(R.id.tv_time_range_start_time);
            mTvDescription = view.findViewById(R.id.tv_alarm_detection_description);
            mSnapshot = view.findViewById(R.id.iv_time_range_snapshot);
        }


        private void showPicture(CameraMessageBean cameraMessageBean) {
            String attachPics = cameraMessageBean.getAttachPics();
            mSnapshot.setVisibility(View.VISIBLE);
            /*if (attachPics.contains("@")) {
                int index = attachPics.lastIndexOf("@");
                try {
                    String decryption = attachPics.substring(index + 1);
                    String imageUrl = attachPics.substring(0, index);
                    Log.d(TAG, "elango message showPicture : " + imageUrl +", " + decryption);
                    mSnapshot.setImageURI(imageUrl, decryption.getBytes());
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
                mSnapshot.setController(controller);
            }*/
            try {
                Picasso.get()
                        .load(attachPics)
                        .placeholder(R.drawable.img_place_holder)
                        .error(R.drawable.img_place_holder)
                        .into(mSnapshot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public interface OnItemListener {
        void onLongClick(CameraMessageBean o);

        void onItemClick(CameraMessageBean o);
    }
}

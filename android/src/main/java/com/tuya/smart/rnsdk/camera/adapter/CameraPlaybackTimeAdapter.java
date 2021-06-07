package com.tuya.smart.rnsdk.camera.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.rnsdk.R;
import com.tuya.smart.rnsdk.camera.bean.TimePieceBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by huangdaju on 2018/3/5.
 */

public class CameraPlaybackTimeAdapter extends RecyclerView.Adapter<CameraPlaybackTimeAdapter.MyViewHolder> {

    private Context context = null;
    private LayoutInflater mInflater;
    private List<TimePieceBean> timePieceBeans;
    private OnTimeItemListener listener;
    private int row_index = -1;

    private int selectedItem;

    public CameraPlaybackTimeAdapter(Context context, List<TimePieceBean> timePieceBeans) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.timePieceBeans = timePieceBeans;
        //setHasStableIds(true);

        selectedItem = 0;
    }

    public void setListener(OnTimeItemListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.activity_camera_playback_time_tem, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.itemView.setTag(timePieceBeans.get(position));

        holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.tuya_list_card_bg_white));
        if (selectedItem == position) {
            holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.tuya_list_card_bg_selected));
        }

        final TimePieceBean ipcVideoBean = timePieceBeans.get(position);
        holder.mTvStartTime.setText(timeFormat(ipcVideoBean.getStartTime() * 1000L));
        int lastTime = ipcVideoBean.getEndTime() - ipcVideoBean.getStartTime();
        holder.mTvDuration.setText("Duration:" + changeSecond(lastTime));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    /*row_index=position;
                    notifyDataSetChanged();*/

                    int previousItem = selectedItem;
                    selectedItem = position;
                    notifyItemChanged(previousItem);
                    notifyItemChanged(position);

                    listener.onClick(ipcVideoBean);
                }
            }
        });

        /*if(row_index==position){
            holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.tuya_list_card_bg_selected));
            //holder.tv1.setTextColor(Color.parseColor("#ffffff"));
        }
        else
        {
            holder.cardView.setBackgroundColor(ContextCompat.getColor(context, R.color.tuya_list_card_bg_white));
            //holder.tv1.setTextColor(Color.parseColor("#000000"));
        }*/
    }

    @Override
    public int getItemCount() {
        return timePieceBeans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTvStartTime;
        TextView mTvDuration;
        CardView cardView;

        public MyViewHolder(final View view) {
            super(view);
            mTvStartTime = view.findViewById(R.id.time_start);
            mTvDuration = view.findViewById(R.id.time_duration);
            cardView = view.findViewById(R.id.cardView);
        }
    }

    public static String timeFormat(long time) {
        //SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static String changeSecond(int seconds) {
        int temp;
        StringBuilder timer = new StringBuilder();
        temp = seconds / 3600;
        timer.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 / 60;
        timer.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 % 60;
        timer.append((temp < 10) ? "0" + temp : "" + temp);
        return timer.toString();
    }

    public interface OnTimeItemListener {
        void onClick(TimePieceBean o);
    }
}

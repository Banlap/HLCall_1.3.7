package hzhl.net.hlcall.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import hzhl.net.hlcall.R;
import hzhl.net.hlcall.recording.Recording;
import hzhl.net.hlcall.recording.RecordingListener;
import hzhl.net.hlcall.utils.MyLog;

/**
 * Created by guang on 2018/6/25.
 */

public class RecordsListAdapter extends SelectableAdapter<RecordsListAdapter.ViewHolder> {
    private Context mContext;
    private List<Recording> list;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener itemClickListener;

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }


    public interface OnRecyclerViewItemClickListener {
        void OnItemClick(int position);

    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public RecordsListAdapter(Context context, List<Recording> list) {
        this.mContext = context;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.item_records, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        final Recording record = list.get(viewHolder.getAdapterPosition());

        viewHolder.name.setSelected(true); // For automated horizontal scrolling of long texts

        Calendar recordTime = Calendar.getInstance();
        recordTime.setTime(record.getRecordDate());
        viewHolder.separatorText.setText(DateToHumanDate(recordTime));
        viewHolder.select.setVisibility(isEditionEnabled() ? View.VISIBLE : View.GONE);
        viewHolder.select.setChecked(isSelected(viewHolder.getAdapterPosition()));

        if (viewHolder.getAdapterPosition() > 0) {
            Recording previousRecord = list.get(viewHolder.getAdapterPosition() - 1);
            Date previousRecordDate = previousRecord.getRecordDate();
            Calendar previousRecordTime = Calendar.getInstance();
            previousRecordTime.setTime(previousRecordDate);

            if (isSameDay(previousRecordTime, recordTime)) {
                viewHolder.separator.setVisibility(View.GONE);
            } else {
                viewHolder.separator.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.separator.setVisibility(View.VISIBLE);
        }

        if (record.isPlaying()) {
            viewHolder.playButton.setImageResource(R.drawable.record_pause);
        } else {
            viewHolder.playButton.setImageResource(R.drawable.record_play);
        }
        viewHolder.playButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       /* if (record.isPlaying()) {
                            viewHolder.progressionBar.setMax(record.getDuration());
                            //viewHolder.progressionBar.setProgress(0);
                        }
                        Logger.d(record.isPaused());
                        if (record.isPaused()) {
                            Logger.d("播放");
                            record.play();
                            viewHolder.playButton.setImageResource(R.drawable.record_pause);
                        } else {
                            Logger.d("暂停");
                            record.pause();
                            viewHolder.playButton.setImageResource(R.drawable.record_play);
                        }*/
                        MyLog.d("wen " + record.getRecordPath());
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(FileProvider.getUriForFile(mContext,
                                mContext.getPackageName()+".file_provider",
                                new File(record.getRecordPath())),
                                "audio/*");
                        mContext.startActivity(intent);

                    }
                });

        viewHolder.name.setText(record.getName());
        viewHolder.date.setText(new SimpleDateFormat("HH:mm").format(record.getRecordDate()));

        int i = record.getCurrentPosition();
        viewHolder.currentPosition.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(i),
                        TimeUnit.MILLISECONDS.toSeconds(i)
                                - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(i))));

        int duration = record.getDuration();
        viewHolder.duration.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration)
                                - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(duration))));






        viewHolder.progressionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Logger.d(progress + " 是否fromUser：" + fromUser + " seekBar.getMax()" + seekBar.getMax());
                 /*       if (progress == seekBar.getMax()) {
                            Logger.d("eee");
                            record.pause();
                            record.seek(0);
                            viewHolder.progressionBar.setProgress(0);
                            viewHolder.currentPosition.setText("00:00");
                            viewHolder.playButton.setImageResource(R.drawable.record_play);
                        }*/
                        if (fromUser) {
                            int progressToSet =
                                    progress > 0 && progress < seekBar.getMax() ? progress : 0;

                            if (progress == seekBar.getMax()) {
                                if (record.isPlaying()) record.pause();
                            }

                            record.seek(progressToSet);
                            seekBar.setProgress(progressToSet);

                            int currentPosition = record.getCurrentPosition();
                            viewHolder.currentPosition.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d:%02d",
                                            TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                            TimeUnit.MILLISECONDS.toSeconds(currentPosition)
                                                    - TimeUnit.MINUTES.toSeconds(
                                                    TimeUnit.MILLISECONDS.toMinutes(
                                                            currentPosition))));
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        record.setRecordingListener(
                new RecordingListener() {
                    @Override
                    public void currentPositionChanged(int currentPosition) {
                        Logger.d(currentPosition);
                        viewHolder.currentPosition.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                        TimeUnit.MILLISECONDS.toSeconds(currentPosition)
                                                - TimeUnit.MINUTES.toSeconds(
                                                TimeUnit.MILLISECONDS.toMinutes(
                                                        currentPosition))));
                        viewHolder.progressionBar.setProgress(currentPosition);
                    }

                    @Override
                    public void endOfRecordReached() {
                        Logger.d("endOfRecordReached");
                        record.pause();
                        record.seek(0);
                        record.close();
                        viewHolder.progressionBar.setProgress(0);
                        viewHolder.currentPosition.setText("00:00");
                        viewHolder.playButton.setImageResource(R.drawable.record_play);
                    }
                });

    }


    public void setData(List<Recording> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView playButton;
        private TextView name;
        private TextView date;
        private TextView currentPosition;
        private TextView duration;
        private SeekBar progressionBar;
        private CheckBox select;
        private LinearLayout separator;
        private TextView separatorText;

        public ViewHolder(View itemView) {
            super(itemView);
            this.playButton = itemView.findViewById(R.id.record_play);
            this.name = itemView.findViewById(R.id.record_name);
            this.date = itemView.findViewById(R.id.record_date);
            this.currentPosition = itemView.findViewById(R.id.record_current_time);
            this.duration = itemView.findViewById(R.id.record_duration);
            this.progressionBar = itemView.findViewById(R.id.record_progression_bar);
            this.select = itemView.findViewById(R.id.delete);
            this.separator = itemView.findViewById(R.id.separator);
            this.separatorText = itemView.findViewById(R.id.separator_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.OnItemClick(getAdapterPosition());
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private String DateToHumanDate(Calendar cal) {
        SimpleDateFormat dateFormat;
       /* if (isToday(cal)) {
            return "今天";
        } else if (isYesterday(cal)) {
            return "昨天";
        } else {
            dateFormat =
                    new SimpleDateFormat(
                            "EEE d MMM");
        }*/

        dateFormat =
                new SimpleDateFormat(
                        "YYYY年 MM月 dd日");
        return dateFormat.format(cal.getTime());
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    private boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    private boolean isYesterday(Calendar cal) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.roll(Calendar.DAY_OF_MONTH, -1);
        return isSameDay(cal, yesterday);
    }
}

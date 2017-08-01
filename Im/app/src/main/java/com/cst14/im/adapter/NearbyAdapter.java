package com.cst14.im.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.bean.NearbyBean;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2016/9/1 0001.
 */
public class NearbyAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<NearbyBean> nearbyList;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:m:ss", Locale.CHINA);

    public NearbyAdapter(Context ctx, List<NearbyBean> nearbyList) {
        this.inflater = LayoutInflater.from(ctx);
        this.nearbyList = nearbyList;
    }

    @Override
    public int getCount() {
        return nearbyList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public NearbyBean getItem(int position) {
        return nearbyList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_nearby, null);

            holder = new ViewHolder();
            holder.tv_nick = (TextView) convertView.findViewById(R.id.tv_nick);
            holder.tv_age_man = (TextView) convertView.findViewById(R.id.tv_age_man);
            holder.tv_age_woman = (TextView) convertView.findViewById(R.id.tv_age_woman);
            holder.tv_intro = (TextView) convertView.findViewById(R.id.tv_intro);
            holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_distance);
            holder.tv_recentTime = (TextView) convertView.findViewById(R.id.tv_recentTime);
            convertView.setTag(holder);
        }
        holder = (ViewHolder) convertView.getTag();
        NearbyBean bean = getItem(position);

        holder.tv_nick.setText(bean.getNick());
        holder.tv_intro.setText(bean.getIntro());
        String distance = String.format(Locale.CHINA, "%.3fkm", bean.getDistance());
        holder.tv_distance.setText(distance);
        holder.tv_recentTime.setText(RelativeDateFormat.format(bean.getUpdateTime()));

        // display sex and age according to sex
        holder.tv_age_woman.setVisibility(View.INVISIBLE);
        holder.tv_age_man.setVisibility(View.INVISIBLE);
        if (bean.isMan()) {
            holder.tv_age_man.setVisibility(View.VISIBLE);
            holder.tv_age_man.setText(String.valueOf(bean.getAge()));
        } else {
            holder.tv_age_woman.setVisibility(View.VISIBLE);
            holder.tv_age_woman.setText(String.valueOf(bean.getAge()));
        }

        return convertView;
    }

    private class ViewHolder {
        ImageView iv_avatar;
        TextView tv_nick;
        TextView tv_age_man;
        TextView tv_age_woman;
        TextView tv_intro;
        TextView tv_distance;
        TextView tv_recentTime;
    }

    public static class RelativeDateFormat {

        private static final long ONE_MINUTE = 60*1000L;
        private static final long ONE_HOUR = 60*60*1000L;
        private static final long ONE_DAY = 24*60*60*1000L;
        private static final long ONE_WEEK = 7*24*60*60*1000L;

        private static final String ONE_SECOND_AGO = "秒前";
        private static final String ONE_MINUTE_AGO = "分钟前";
        private static final String ONE_HOUR_AGO = "小时前";
        private static final String ONE_DAY_AGO = "天前";
        private static final String ONE_MONTH_AGO = "月前";
        private static final String ONE_YEAR_AGO = "年前";

        public static String format(String timeStr) {
            long delta = new Date().getTime() - Timestamp.valueOf(timeStr).getTime();
            if (delta < 1L * ONE_MINUTE) {
                long seconds = toSeconds(delta);
                return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
            }
            if (delta < 45L * ONE_MINUTE) {
                long minutes = toMinutes(delta);
                return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
            }
            if (delta < 24L * ONE_HOUR) {
                long hours = toHours(delta);
                return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
            }
            if (delta < 48L * ONE_HOUR) {
                return "昨天";
            }
            if (delta < 30L * ONE_DAY) {
                long days = toDays(delta);
                return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
            }
            if (delta < 12L * 4L * ONE_WEEK) {
                long months = toMonths(delta);
                return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
            } else {
                long years = toYears(delta);
                return (years <= 0 ? 1 : years) + ONE_YEAR_AGO;
            }
        }

        private static long toSeconds(long date) {
            return date / 1000L;
        }
        private static long toMinutes(long date) {
            return toSeconds(date) / 60L;
        }
        private static long toHours(long date) {
            return toMinutes(date) / 60L;
        }
        private static long toDays(long date) {
            return toHours(date) / 24L;
        }
        private static long toMonths(long date) {
            return toDays(date) / 30L;
        }
        private static long toYears(long date) {
            return toMonths(date) / 365L;
        }
    }
}

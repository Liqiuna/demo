package com.cst14.im.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cst14.im.R;
import com.cst14.im.protobuf.ProtoClass;
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import java.util.Locale;

/**
 * Created by Administrator on 2016/9/2 0002.
 */
public class NearbyFilterDialog extends Dialog {

    private static final int SEX_ALL_CODE   = 1;
    private static final int SEX_MAN_CODE   = 1 << 1;
    private static final int SEX_WOMAN_CODE = 1 << 2;
    private static final int ACTIVE_FIFTEEN_MINUTE_CODE = 1;
    private static final int ACTIVE_SIXTY_MINUTE_CODE   = 1 << 1;
    private static final int ACTIVE_ONE_DAY_CODE        = 1 << 2;
    private static final int ACTIVE_THREE_DAY_CODE      = 1 << 3;

    public interface OnAcknowListener {
        void onAcknow(ProtoClass.NearbyCondition.Builder builder);
    }

    private OnAcknowListener onAcknowListener;
    private Context ctx;

    private int sexCode = SEX_ALL_CODE;
    private int activeCode = ACTIVE_ONE_DAY_CODE;
    private int minAge = 12;
    private int maxAge = 40;
    private int tmp_sexCode = 0;
    private int tmp_activeCode = 0;
    private int tmp_minAge = 12;
    private int tmp_maxAge = 40;

    private TextView tv_sex_all_gray;
    private TextView tv_sex_man_gray;
    private TextView tv_sex_woman_gray;
    private TextView tv_sex_all_blue;
    private TextView tv_sex_man_blue;
    private TextView tv_sex_woman_blue;

    private TextView tv_fifteen_minute_gray;
    private TextView tv_sixty_minute_gray;
    private TextView tv_one_day_gray;
    private TextView tv_three_day_gray;
    private TextView tv_fifteen_minute_blue;
    private TextView tv_sixty_minute_blue;
    private TextView tv_one_day_blue;
    private TextView tv_three_day_blue;

    private TextView tv_age;
    private RangeSeekBar<Integer> seekBar;

    private Button btn_acknow;
    private Button btn_cancel;

    public NearbyFilterDialog(Context ctx, OnAcknowListener listener) {
        super(ctx, R.style.nearby_filter_dialog);
        this.ctx = ctx;
        this.onAcknowListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_nearby_filter);

        initSexView();
        initActiveView();
        initAgeView();
        initButtonView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadStatus();
    }

    private void initSexView() {
        tv_sex_all_gray = (TextView) findViewById(R.id.tv_sex_all_gray);
        tv_sex_all_blue = (TextView) findViewById(R.id.tv_sex_all_blue);
        tv_sex_man_gray = (TextView) findViewById(R.id.tv_sex_man_gray);
        tv_sex_man_blue = (TextView) findViewById(R.id.tv_sex_man_blue);
        tv_sex_woman_gray = (TextView) findViewById(R.id.tv_sex_woman_gray);
        tv_sex_woman_blue = (TextView) findViewById(R.id.tv_sex_woman_blue);

        tv_sex_all_gray.setOnClickListener(sexOnClickListener);
        tv_sex_all_blue.setOnClickListener(sexOnClickListener);
        tv_sex_man_gray.setOnClickListener(sexOnClickListener);
        tv_sex_man_blue.setOnClickListener(sexOnClickListener);
        tv_sex_woman_gray.setOnClickListener(sexOnClickListener);
        tv_sex_woman_blue.setOnClickListener(sexOnClickListener);

        tv_sex_all_blue.setVisibility(View.VISIBLE);
    }

    private void initActiveView() {
        tv_fifteen_minute_gray = (TextView) findViewById(R.id.tv_fifteen_minute_gray);
        tv_fifteen_minute_blue = (TextView) findViewById(R.id.tv_fifteen_minute_blue);
        tv_sixty_minute_gray = (TextView) findViewById(R.id.tv_sixty_minute_gray);
        tv_sixty_minute_blue = (TextView) findViewById(R.id.tv_sixty_minute_blue);
        tv_one_day_gray = (TextView) findViewById(R.id.tv_one_day_gray);
        tv_one_day_blue = (TextView) findViewById(R.id.tv_one_day_blue);
        tv_three_day_gray = (TextView) findViewById(R.id.tv_three_day_gray);
        tv_three_day_blue = (TextView) findViewById(R.id.tv_three_day_blue);

        tv_fifteen_minute_gray.setOnClickListener(activeOnClickListener);
        tv_fifteen_minute_blue.setOnClickListener(activeOnClickListener);
        tv_sixty_minute_gray.setOnClickListener(activeOnClickListener);
        tv_sixty_minute_blue.setOnClickListener(activeOnClickListener);
        tv_one_day_gray.setOnClickListener(activeOnClickListener);
        tv_one_day_blue.setOnClickListener(activeOnClickListener);
        tv_three_day_gray.setOnClickListener(activeOnClickListener);
        tv_three_day_blue.setOnClickListener(activeOnClickListener);

        tv_one_day_blue.setVisibility(View.VISIBLE);
    }

    private void initAgeView() {
        tv_age = (TextView) findViewById(R.id.tv_age);
        seekBar = (RangeSeekBar<Integer>) findViewById(R.id.sb_age);

        seekBar.setRangeValues(12, 40);
        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                handleAgeText(bar, minValue, maxValue);
            }
        });
    }

    private void handleAgeText(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
        String age_text;

        if (minValue.equals(bar.getAbsoluteMinValue()) && maxValue.equals(bar.getAbsoluteMaxValue())) {
            age_text = "全部";
        }
        else if (minValue.equals(maxValue)) {
            age_text = String.valueOf(minValue);
        }
        else {
            age_text = String.format(Locale.CHINA, "(%d-%d)", minValue, maxValue);
        }

        tv_age.setText(age_text);
        tmp_minAge = minValue;
        tmp_maxAge = maxValue;
    }

    private void initButtonView() {
        btn_acknow = (Button) findViewById(R.id.btn_acknow);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);

        btn_acknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveStatus();
                dismiss();

                if (onAcknowListener != null) {
                    onPreAcknow();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private View.OnClickListener sexOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == null) return;

            tv_sex_all_gray.setVisibility(View.VISIBLE);
            tv_sex_man_gray.setVisibility(View.VISIBLE);
            tv_sex_woman_gray.setVisibility(View.VISIBLE);

            tv_sex_all_blue.setVisibility(View.INVISIBLE);
            tv_sex_man_blue.setVisibility(View.INVISIBLE);
            tv_sex_woman_blue.setVisibility(View.INVISIBLE);

            switch (v.getId()) {
                case R.id.tv_sex_all_gray:
                case R.id.tv_sex_all_blue:
                    tv_sex_all_blue.setVisibility(View.VISIBLE);
                    tmp_sexCode = SEX_ALL_CODE;
                    break;

                case R.id.tv_sex_man_gray:
                case R.id.tv_sex_man_blue:
                    tv_sex_man_blue.setVisibility(View.VISIBLE);
                    tmp_sexCode = SEX_MAN_CODE;
                    break;

                case R.id.tv_sex_woman_gray:
                case R.id.tv_sex_woman_blue:
                    tv_sex_woman_blue.setVisibility(View.VISIBLE);
                    tmp_sexCode = SEX_WOMAN_CODE;
                    break;
            }
        }
    };

    private View.OnClickListener activeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == null) return;

            tv_fifteen_minute_gray.setVisibility(View.VISIBLE);
            tv_sixty_minute_gray.setVisibility(View.VISIBLE);
            tv_one_day_gray.setVisibility(View.VISIBLE);
            tv_three_day_gray.setVisibility(View.VISIBLE);

            tv_fifteen_minute_blue.setVisibility(View.INVISIBLE);
            tv_sixty_minute_blue.setVisibility(View.INVISIBLE);
            tv_one_day_blue.setVisibility(View.INVISIBLE);
            tv_three_day_blue.setVisibility(View.INVISIBLE);

            switch (v.getId()) {
                case R.id.tv_fifteen_minute_gray:
                case R.id.tv_fifteen_minute_blue:
                    tv_fifteen_minute_blue.setVisibility(View.VISIBLE);
                    tmp_activeCode = ACTIVE_FIFTEEN_MINUTE_CODE;
                    break;

                case R.id.tv_sixty_minute_gray:
                case R.id.tv_sixty_minute_blue:
                    tv_sixty_minute_blue.setVisibility(View.VISIBLE);
                    tmp_activeCode = ACTIVE_SIXTY_MINUTE_CODE;
                    break;

                case R.id.tv_one_day_gray:
                case R.id.tv_one_day_blue:
                    tv_one_day_blue.setVisibility(View.VISIBLE);
                    tmp_activeCode = ACTIVE_ONE_DAY_CODE;
                    break;

                case R.id.tv_three_day_gray:
                case R.id.tv_three_day_blue:
                    tv_three_day_blue.setVisibility(View.VISIBLE);
                    tmp_activeCode = ACTIVE_THREE_DAY_CODE;
                    break;
            }
        }
    };

    private void onPreAcknow() {
        ProtoClass.NearbyCondition.Builder builder = ProtoClass.NearbyCondition.newBuilder();

        switch (sexCode) {
            case SEX_ALL_CODE:   builder.setSex(ProtoClass.Sex.ALL);  break;
            case SEX_MAN_CODE:   builder.setSex(ProtoClass.Sex.MAN);  break;
            case SEX_WOMAN_CODE: builder.setSex(ProtoClass.Sex.WOMAN);break;
        }

        switch (activeCode) {
            case ACTIVE_FIFTEEN_MINUTE_CODE: builder.setActive(ProtoClass.Active.FIFTEEN_MINUTES); break;
            case ACTIVE_SIXTY_MINUTE_CODE:   builder.setActive(ProtoClass.Active.SIXTY_MINUTES);   break;
            case ACTIVE_ONE_DAY_CODE:        builder.setActive(ProtoClass.Active.ONE_DAY);         break;
            case ACTIVE_THREE_DAY_CODE:      builder.setActive(ProtoClass.Active.THREE_DAYS);      break;
        }

        if (minAge == seekBar.getAbsoluteMinValue() && maxAge == seekBar.getAbsoluteMaxValue()) {
            builder.setIsAllAge(true);
        } else {
            builder.setMinAge(minAge).setMaxAge(maxAge);
        }

        onAcknowListener.onAcknow(builder);
    }

    private void saveStatus() {
        sexCode = tmp_sexCode;
        activeCode = tmp_activeCode;
        minAge = tmp_minAge;
        maxAge = tmp_maxAge;
    }

    private void loadStatus() {
        switch (sexCode) {
            case SEX_ALL_CODE:   tv_sex_all_blue.callOnClick();  break;
            case SEX_MAN_CODE:   tv_sex_man_blue.callOnClick();  break;
            case SEX_WOMAN_CODE: tv_sex_woman_blue.callOnClick();break;
        }

        switch (activeCode) {
            case ACTIVE_FIFTEEN_MINUTE_CODE: tv_fifteen_minute_blue.callOnClick(); break;
            case ACTIVE_SIXTY_MINUTE_CODE:   tv_sixty_minute_blue.callOnClick();   break;
            case ACTIVE_ONE_DAY_CODE:        tv_one_day_blue.callOnClick();        break;
            case ACTIVE_THREE_DAY_CODE:      tv_three_day_blue.callOnClick();      break;
        }

        handleAgeText(seekBar, minAge, maxAge);
        seekBar.setSelectedMinValue(minAge);
        seekBar.setSelectedMaxValue(maxAge);
    }
}

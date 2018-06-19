package com.example.zjp.client;

import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DetailFragment extends Fragment {

    private TextView mLimitTextView;
    private TextView mModeTextView;
    private TextView mTargetTempTextView;
    private TextView mCurrentTempTextView;
    private TextView mDefaultTempTextView;
    private TextView mWindTextView;
    private TextView mEnergyTextView;
    private TextView mCostTextView;

    private MainActivity mainActivity;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=(MainActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view=inflater.inflate(R.layout.detail_layout,container,false);

        mLimitTextView=(TextView) view.findViewById(R.id.limitTextView);
        mModeTextView=(TextView) view.findViewById(R.id.modeTextView);
        mTargetTempTextView=(TextView) view.findViewById(R.id.targetTempTextView);
        mCurrentTempTextView=(TextView) view.findViewById(R.id.currentTempTextView);
        mDefaultTempTextView=(TextView) view.findViewById(R.id.defaultextView);
        mWindTextView=(TextView) view.findViewById(R.id.windTextView);
        mEnergyTextView=(TextView) view.findViewById(R.id.energyTextView);
        mCostTextView=(TextView) view.findViewById(R.id.costTextView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setmLimitTextView(int low,int high){
        mLimitTextView.setText(Integer.toString(low)+"°C~"+Integer.toString(high)+"°C");
    }

    public void setmModeTextView(int mode){
        if(1==mode){
            mModeTextView.setText("制冷");
        }
        else {
            mModeTextView.setText("制热");
        }
    }

    public void setmTargetTempTextView(int target){
        mTargetTempTextView.setText(Integer.toString(target)+"°C");
    }

    public void setmCurrentTempTextView(int current){
        mCurrentTempTextView.setText(Integer.toString(current)+"°C");
    }

    public void setmDefaultTempTextView(int defaultTemp){
        mDefaultTempTextView.setText(Integer.toString(defaultTemp)+"°C");
    }

    public void setmWindTextView(int speed){
        String text="";
        switch (speed){
            case 1:
                text="弱风";
                break;
            case 2:
                text="中风";
                break;
            case 3:
                text="强风";
                break;
        }
        mWindTextView.setText(text);
    }

    public void setmEnergyTextView(double energy){
        mEnergyTextView.setText(Double.toString(energy)+"kwh");
    }

    public void setmCostTextView(double cost){
        mCostTextView.setText(Double.toString(cost)+"¥");
    }
}

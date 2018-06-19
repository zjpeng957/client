package com.example.zjp.client;

import android.support.v4.app.Fragment;
//import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

public class HomeFragment extends Fragment {

    private MainActivity mainActivity;
    private ImageButton mPowerButton;
    private Button mTempUpButton;
    private Button mTempDownButton;
    private TextView mModeTextView;
    private TextView mTempTextView;
    private TextView mWindTextView;
    private TextView mTextview;
    private SeekBar mWindSeekBar;

    private String roomID;
    private Socket socket;
    private boolean first=true;

    private static final int BUFF_SIZE = 255;

    private DecimalFormat one=new DecimalFormat("##0.0");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity=((MainActivity)(getActivity()));
        socket=mainActivity.getSocket();
        roomID=mainActivity.getRoomID();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view=inflater.inflate(R.layout.home_layout,container,false);

        mPowerButton=(ImageButton) view.findViewById(R.id.powerImageButton);
        mTempUpButton=(Button) view.findViewById(R.id.tempUpButton);
        mTempDownButton=(Button) view.findViewById(R.id.tempDownButton);
        mTempTextView=(TextView) view.findViewById(R.id.tempTextView);
        mWindTextView=(TextView) view.findViewById(R.id.windTextView);
        mModeTextView=(TextView) view.findViewById(R.id.modeTextView);
        mTextview=(TextView) view.findViewById(R.id.textView10);
        mWindSeekBar=(SeekBar) view.findViewById(R.id.windSeekBar);

        mWindSeekBar.setProgress(2);
        powerOff();
        setListener();
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

    private void setListener(){

        mPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //powerOn();
                Thread powerThread=new PowerThread();
                powerThread.start();
            }
        });

        mTempDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double targetTemp=mainActivity.getTargetTemp()-1;
                //mainActivity.getTempCtrlQueue().add(targetTemp);
                if(targetTemp<mainActivity.getLowestTemp()) return;

                mainActivity.setTargetTemp(targetTemp);
                Thread tempThread=new TempThread();
                tempThread.start();

                mTempTextView.setText(Integer.toString((int)(targetTemp+0.5f)));
                mainActivity.detailRefreshAll();
            }
        });

        mTempUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double targetTemp=mainActivity.getTargetTemp()+1;
                //mainActivity.getTempCtrlQueue().add(targetTemp);
                if(targetTemp>mainActivity.getHighestTemp()) return;

                mainActivity.setTargetTemp(targetTemp);
                Thread tempThread=new TempThread();
                tempThread.start();

                mTempTextView.setText(Integer.toString((int)(targetTemp+0.5f)));
                mainActivity.detailRefreshAll();
            }
        });

        mWindSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //if(progress==(mainActivity.getLastProgress())) return;
                if(first){
                    first=false;
                    return;
                }
                switch (progress){
                    case 0:
                        mWindTextView.setText("风速  弱风");
                        break;
                    case 1:
                        mWindTextView.setText("风速  中风");
                        break;
                    case 2:
                        mWindTextView.setText("风速  强风");
                        break;
                }

                Thread windThread=new WindThread();
                windThread.start();
                mainActivity.detailRefreshAll();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    private class PowerThread extends Thread implements Runnable{
        @Override
        public void run() {

            try{

                int power;
                if(mainActivity.isPower()) power=1;
                else power=0;

                //if (socket==null) return;


                if(!mainActivity.isPower()){
                    //socket=new Socket("10.8.223.127",8888);
                    //socket=new Socket("10.8.161.247",8888);
                   // socket = new Socket("10.8.179.30", 8888);
                    socket = new Socket("10.206.12.135", 8888);
                    mainActivity.setSocket(socket);

                    OutputStream outputStream=socket.getOutputStream();

                    mTextview.setText("开机");
                    double currentTemp=mainActivity.getCurrentTemp();
                    String msg="*"+Integer.toString(1)+"|"+"312A"+"|"+one.format(currentTemp);
                    byte []test=msg.getBytes();
                    outputStream.write(msg.getBytes());
                    mainActivity.pauseTempBack();
                }
                else{
                    OutputStream outputStream=socket.getOutputStream();

                    mTextview.setText("关机");
                    String msg="*"+Integer.toString(3)+"|"+"312A";
                    byte []test=msg.getBytes();
                    outputStream.write(msg.getBytes());
                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    private class TempThread extends Thread implements Runnable{
        @Override
        public void run() {
            socket=mainActivity.getSocket();
            if (socket==null) return;

            double targetTemp=mainActivity.getTargetTemp();
            try{
                OutputStream outputStream=socket.getOutputStream();
                String msg="*"+Integer.toString(7)+"|"+roomID+"|"+one.format(targetTemp);

                outputStream.write(msg.getBytes());
                mainActivity.resumeTempBack();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class WindThread extends Thread implements Runnable{
        @Override
        public void run() {
            socket=mainActivity.getSocket();
            if (socket==null) return;

            try{
                OutputStream outputStream=socket.getOutputStream();
                String msg="*"+Integer.toString(5)+"|"+roomID+"|"+Integer.toString(mWindSeekBar.getProgress()+1);

                outputStream.write(msg.getBytes());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void setTempTextView(double targetTemp) {
        mTempTextView.setText(Integer.toString((int)(targetTemp+0.5f)));
    }

    public void setWindSpeed(int speed){
        mWindSeekBar.setProgress(speed-1);
        switch (speed){
            case 1:
                mWindTextView.setText("风速  弱风");
                break;
            case 2:
                mWindTextView.setText("风速  中风");
                break;
            case 3:
                mWindTextView.setText("风速  强风");
                break;
        }
    }

    public void powerOff(){
        first=true;
        mTempTextView.setText("-- ");
        mWindTextView.setText("风速 关");
    }

    public void powerOn(){
        double targetTemp=mainActivity.getTargetTemp();
        int targetProgress=mainActivity.getLastProgress();
        mTempTextView.setText(Integer.toString((int)(targetTemp+0.5f)));
        switch (targetProgress){
            case 0:
                mWindTextView.setText("风速  弱风");
                break;
            case 1:
                mWindTextView.setText("风速  中风");
                break;
            case 2:
                mWindTextView.setText("风速  强风");
                break;
        }

    }

    public int getSeekBarProgess(){
        return mWindSeekBar.getProgress();
    }

    public void setmTextview(String x){
        mTextview.setText(x);
    }

}

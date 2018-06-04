package com.example.zjp.client;

import android.app.Fragment;
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
import java.net.Socket;

public class HomeFragment extends Fragment {
    private Button mOpenButton;
    private Button mTempUpButton;
    private Button mTempDownButton;
    private Button mWindUpButton;
    private Button mWindDownButton;
    private TextView mTempTextView;
    private TextView mWindTextView;
    private TextView mStateTextView;
    private SeekBar mWindSeekBar;
    private Socket socket;

    private static final int BUFF_SIZE = 255;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket=((MainActivity)(getActivity())).getSocket();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view=inflater.inflate(R.layout.home_layout,container,false);

        mOpenButton=(Button) view.findViewById(R.id.openButton);
        mTempUpButton=(Button) view.findViewById(R.id.tempUpButton);
        mTempDownButton=(Button) view.findViewById(R.id.tempDownButton);
        //mWindUpButton=(Button) view.findViewById(R.id.windUpButton);
        //mWindDownButton=(Button) view.findViewById(R.id.windDownButton);
        //mTempTextView=(TextView) view.findViewById(R.id.tempTextView);
        mWindTextView=(TextView) view.findViewById(R.id.windTextView);
        mStateTextView=(TextView)view.findViewById(R.id.stateTextView);
        mWindSeekBar=(SeekBar) view.findViewById(R.id.windSeekBar);

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

        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenThread openThread=new OpenThread();
                openThread.start();
            }
        });



        mTempUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TempUpThread tempUpThread=new TempUpThread();
                tempUpThread.start();
            }
        });
    }

    private class CloseThread extends Thread implements Runnable{
        @Override
        public void run() {
            try{
                OutputStream outputStream=socket.getOutputStream();
//                    InputStream inputStream=socket.getInputStream();
//                    byte[] rsp=new byte[BUFF_SIZE];
                String msg=Integer.toString(3)+"|"+"311A"+"|"+Integer.toString(0);

                outputStream.write(msg.getBytes());
//                    inputStream.read(rsp);
//                    String str=new String(rsp);
//                    String []code=str.split("|");

//                    mStateTextView.setText("code:"+code[0]+"\n"+"res:"+code[1]);
//                outputStream.close();
//                    inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class OpenThread extends Thread implements Runnable{
        @Override
        public void run() {
            try{
                OutputStream outputStream=socket.getOutputStream();
//                    InputStream inputStream=socket.getInputStream();
//                    byte[] rsp=new byte[BUFF_SIZE];
                String msg=Integer.toString(1)+"|"+"311A"+"|"+Integer.toString(0);
                byte []test=msg.getBytes();
                outputStream.write(msg.getBytes());
                //outputStream.flush();
//                    inputStream.read(rsp);
//                    String str=new String(rsp);
//                    String []code=str.split("|");

//                    mStateTextView.setText("code:"+code[0]+"\n"+"res:"+code[1]);
                //outputStream.close();
//                    inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class WindUpThread extends Thread implements Runnable{
        @Override
        public void run() {
            try{
                OutputStream outputStream=socket.getOutputStream();
//                    InputStream inputStream=socket.getInputStream();
//                    byte[] rsp=new byte[BUFF_SIZE];
                String msg=Integer.toString(5)+"|"+"311A"+"|"+Integer.toString(0)+"|"+Integer.toString(2);

                outputStream.write(msg.getBytes());
//                    inputStream.read(rsp);
//                    String str=new String(rsp);
//                    String []code=str.split("|");

//                    mStateTextView.setText("code:"+code[0]+"\n"+"rsp:"+code[1]);

//                outputStream.close();
//                    inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class TempUpThread extends Thread implements Runnable{
        @Override
        public void run() {
            try{
                OutputStream outputStream=socket.getOutputStream();
//                    InputStream inputStream=socket.getInputStream();
//                    byte[] rsp=new byte[BUFF_SIZE];
                String msg=Integer.toString(7)+"|"+"311A"+"|"+Integer.toString(0)+"|"+Integer.toString(26);

                outputStream.write(msg.getBytes());
//                    inputStream.read(rsp);
//                    String str=new String(rsp);
//                    String []code=str.split("|");

//                    mStateTextView.setText("code:"+code[0]+"\n"+"res:"+code[1]);

//                outputStream.close();
//                    inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}

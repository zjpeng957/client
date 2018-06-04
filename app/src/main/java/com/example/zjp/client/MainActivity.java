package com.example.zjp.client;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private DetailFragment detailFragment;
    private ExitFragment exitFragment;
    private int lastFragment=0;
    private Fragment[] fragments;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private Handler uiHandler=new Handler();

    private Socket socket;
    private static final int BUFF_SIZE = 512;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    if(lastFragment!=0){
                        switchFragments(lastFragment,0);
                    }
                    return true;
                case R.id.navigation_dashboard:

                    if(lastFragment!=1){
                        switchFragments(lastFragment,1);
                    }
                    return true;
                case R.id.navigation_notifications:

                    if(lastFragment!=2){
                        switchFragments(lastFragment,2);
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        RecvThread recvThread=new RecvThread();
        recvThread.start();

        //initFragments();
    }

    private void initFragments(){
        homeFragment=new HomeFragment();
        detailFragment=new DetailFragment();
        exitFragment=new ExitFragment();
        fragments=new Fragment[]{homeFragment,detailFragment,exitFragment};

        fragmentManager=getFragmentManager();
        fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.mFragmentLayout,homeFragment).show(homeFragment).commit();
    }

    private void switchFragments(int last,int now){
        FragmentTransaction transaction=getFragmentManager().beginTransaction();
        transaction.hide(fragments[last]);
        if(!fragments[now].isAdded()){
            transaction.replace(R.id.mFragmentLayout,fragments[now]);
        }
        transaction.show(fragments[now]).commit();
        lastFragment=now;
    }

    public Socket getSocket() {
        return socket;
    }

    private class RecvThread extends Thread implements Runnable{
        @Override
        public void run() {
            try {

                //socket.setSoTimeout(3000);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        initFragments();
                    }
                });
                socket=new Socket("192.168.43.64",8888);
                InputStream inputStream = socket.getInputStream();
                byte []rsp=new byte[BUFF_SIZE];

                while (true){
                    for(int i=0;i<BUFF_SIZE;i++){
                        rsp[i]='|';
                    }
                    inputStream.read(rsp);
                    String str=new String(rsp);
                    String []code=str.split("\\|",8);
                    String text=new String();
                    switch (code[0]){
                        case "2":
                            text="开机结果:\n"+"res:"+code[1];
                            break;
                        case "4":
                            text="关机结果:\n"+"res:"+code[1];
                            break;
                        case "6":
                            text="风速调整结果:\n"+"res:"+code[1];
                            break;
                        case "8":
                            text="温度调整结果:\n"+"res:"+code[1];
                            break;
                        case "9":
                            text="状态:\n"+" "+code[1]+" "+code[2]+" "+code[3]+" "+code[4]+" "+code[5]+" "+code[6];
                            break;
                        case "10":
                            text="主控机停机:\n"+"id:"+code[1];
                            break;
                    }

                    final String res=text;
                    /*
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });*/

                }
            }catch (UnknownHostException e){
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

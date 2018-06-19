package com.example.zjp.client;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,TabLayout.OnTabSelectedListener {

    private HomeFragment homeFragment;
    private DetailFragment detailFragment;
    private ExitFragment exitFragment;
    private int lastFragment = 0;
    private Fragment[] fragments;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private Handler uiHandler = new Handler();

    private double defaultTemp = 15.0;
    private double currentTemp = 15.0;
    private double targetTemp = 24.0;
    private double highestTemp = 30.0;
    private double lowestTemp = 16.0;
    private double controlTemp;
    private boolean power = false;
    private String roomID = "312A";
    private int mode = 0;

    private int lastProgress = 0;

    private double cost = 0;
    private double energy = 0;

    private Queue tempCtrlQueue;
    private boolean controlling=false;
    private Socket socket;
    private static final int BUFF_SIZE = 512;

    private DecimalFormat one=new DecimalFormat("##0.0");

    private TempBackThread tempBackThread=new TempBackThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragments();

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);

        mViewPager.addOnPageChangeListener(this);
        mTabLayout.addOnTabSelectedListener(this);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position < 2) return fragments[position];
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        //BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        RecvThread recvThread = new RecvThread();
        recvThread.start();


    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        detailFragment = new DetailFragment();
        exitFragment = new ExitFragment();
        fragments = new Fragment[]{homeFragment, detailFragment, exitFragment};

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.add(R.id.mFragmentLayout,homeFragment).show(homeFragment).commit();
    }

    public Socket getSocket() {
        return socket;
    }

    private class RecvThread extends Thread implements Runnable {
        @Override
        public void run() {
            try {
                //socket = new Socket("10.206.12.149", 8888);
                //InputStream inputStream = socket.getInputStream();
                //byte[] data = new byte[BUFF_SIZE];
                InputStream inputStream;
                byte[] data = new byte[BUFF_SIZE];
                while (true) {
                    if(socket==null || socket.isClosed()) continue;
                    else {
                        inputStream = socket.getInputStream();
                    }
                    for (int i = 0; i < BUFF_SIZE; i++) {
                        data[i] = '|';
                    }
                    inputStream.read(data);
                    String str = new String(data);
                    String[] commands = str.split("\\*");
                    for (String cmd : commands) {

                        String[] code = cmd.split("\\|");
                        String text = new String();
                        switch (code[0]) {
                            case "2":
                                if (code[1].equals("1")) {
                                    power=true;
                                    mode = Integer.parseInt(code[2]);
                                    targetTemp = Double.parseDouble(code[3]);
                                    final int defaultSpeed = Integer.parseInt(code[4]);
                                    lowestTemp = Double.parseDouble(code[5]);
                                    highestTemp = Double.parseDouble(code[6]);
                                    cost = Double.parseDouble(code[7]);
                                    energy = Double.parseDouble(code[8]);
                                    lastProgress = defaultSpeed - 1;
                                    final double tTemp = targetTemp;
                                    final int dSpeed = defaultSpeed;
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            homeFragment.setTempTextView(tTemp);
                                            homeFragment.setWindSpeed(dSpeed);
                                        }
                                    });
                                }
                                break;
                            case "4":
                                power = (code[1].equals("0"));
                                if (!power) {
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            homeFragment.powerOff();
                                        }
                                    });
                                    currentTemp=12.0;
                                    socket.close();
                                }
                                break;
                            case "6":
                                if (code[1].equals("1")) {
                                    lastProgress = homeFragment.getSeekBarProgess();
                                } else {
                                    final int p = lastProgress + 1;
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            homeFragment.setWindSpeed(p);
                                        }
                                    });
                                }
                                break;
                            case "8":
                                //当调温请求失败时
                                //若当前正在调温，则目标温度为当前的调温目标
                                //若当前不再调温，则显示当前温度
                                if (code[1].equals("0")) {
                                    if (!controlling) {
                                        targetTemp = currentTemp;
                                        final double tTemp = targetTemp;
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                homeFragment.setTempTextView(tTemp);
                                            }
                                        });
                                    } else {
                                        final double cTemp = controlTemp;
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                homeFragment.setTempTextView(cTemp);
                                            }
                                        });
                                    }
                                    //tempCtrlQueue.remove();
                                } else {
                                    controlling = true;
                                    //controlTemp = (double) tempCtrlQueue.remove();
                                }

                                break;
                            case "9":
                                if (code[1].equals(roomID)) {
                                    currentTemp = Double.parseDouble(code[2]);
                                    energy = Double.parseDouble(code[3]);
                                    cost = Double.parseDouble(code[4]);
                                    final double cTemp=currentTemp;
                                    final double en=energy;
                                    final double c=cost;
                                    uiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            detailFragment.setmEnergyTextView(en);
                                            detailFragment.setmCurrentTempTextView((int)(currentTemp+0.5f));
                                            detailFragment.setmCostTextView(cost);
                                        }
                                    });
                                }
                                break;
                            case "10":
                                if (code[1].equals(roomID)) {
                                    controlling = false;
                                    resumeTempBack();
                                }
                                break;
                        }
                    }
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            detailRefreshAll();
                        }
                    });

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        mTabLayout.getTabAt(position).select();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    public double getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(double targetTemp) {
        this.targetTemp = targetTemp;
    }

    public double getCurrentTemp() {
        return currentTemp;
    }

    public double getDefaultTemp() {
        return defaultTemp;
    }

    public String getRoomID() {
        return roomID;
    }

    public boolean isPower() {
        return power;
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public double getHighestTemp() {
        return highestTemp;
    }

    public double getLowestTemp() {
        return lowestTemp;
    }

    public int getLastProgress() {
        return lastProgress;
    }

    public void setLastProgress(int lastProgress) {
        this.lastProgress = lastProgress;
    }

    public Queue getTempCtrlQueue() {
        return tempCtrlQueue;
    }

    class TempBackThread extends Thread implements Runnable{
        private final Object lock=new Object();
        private boolean pause=false;
        int count=10;
        @Override
        public void run() {

            try {
                while (true){
                    while (pause){
                        onPause();
                    }
                    sleep(60000);
                    if(currentTemp>defaultTemp){
                        if((currentTemp-1)>=defaultTemp){
                            currentTemp-=1;
                        }
                        else {
                            currentTemp=defaultTemp;
                        }
                    }
                    else if((currentTemp<defaultTemp)) {
                        if((currentTemp+1)<=defaultTemp){
                            currentTemp+=1;
                        }
                        else {
                            currentTemp=defaultTemp;
                        }
                    }
                    //count++;
                    if((defaultTemp-currentTemp)<0.1&&(currentTemp-defaultTemp)<0.1) pause = true;
                    if(((currentTemp-targetTemp>=1)||(targetTemp-currentTemp>=1))){
                        if(power){
                            OutputStream outputStream=socket.getOutputStream();
                            String msg="*"+Integer.toString(11)+"|"+roomID+"|"+one.format(currentTemp);
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    detailRefreshAll();
                                }
                            });
                            outputStream.write(msg.getBytes());
                            pause=true;
                        }
                    }
                    //if((defaultTemp-currentTemp)<0.1&&(currentTemp-defaultTemp)<0.1) pause = true;
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void pauseThread(){
            pause=true;
        }

        public void resumeThread(){
            if(pause){
                pause=false;
                count=1;
                synchronized (lock){
                    lock.notifyAll();
                }
            }
        }

        private void onPause(){
            synchronized (lock){
                try{
                    lock.wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void detailRefreshAll(){
        detailFragment.setmCostTextView(cost);
        detailFragment.setmCurrentTempTextView((int)currentTemp);
        detailFragment.setmDefaultTempTextView((int)defaultTemp);
        detailFragment.setmEnergyTextView(energy);
        detailFragment.setmLimitTextView((int)lowestTemp,(int)highestTemp);
        detailFragment.setmModeTextView(mode);
        detailFragment.setmWindTextView(lastProgress+1);
        detailFragment.setmTargetTempTextView((int)targetTemp);
    }

    public void resumeTempBack(){
        if(!tempBackThread.isAlive())
            tempBackThread.start();
        tempBackThread.resumeThread();
    }
    public void pauseTempBack(){
        tempBackThread.pauseThread();
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

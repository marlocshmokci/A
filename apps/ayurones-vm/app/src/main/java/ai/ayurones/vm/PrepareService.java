package ai.ayurones.vm;

import android.app.*;
import android.content.*;
import android.os.*;
import java.io.*;

public class PrepareService extends Service {
    private static final String CH="ayurones_vm";
    @Override public void onCreate(){
        super.onCreate();
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel(CH,"Ayurones VM",NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
            Notification n=new Notification.Builder(this,CH).setContentTitle("Ayurones VM").setContentText("Подготовка виртуальной среды…").setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true).build();
            startForeground(77,n);
        }
    }
    @Override public int onStartCommand(Intent intent,int flags,int id){
        final String version=intent.getStringExtra("version");
        new Thread(()->{
            try{
                File guest=new File(getFilesDir(),"guest"); guest.mkdirs();
                File prep=new File(guest,".prepare_"+version.replace(" ","_"));
                for(int i=0;i<30;i++){
                    Thread.sleep(1000);
                    File f=new File(guest,"system_"+i+".dat");
                    try(FileOutputStream o=new FileOutputStream(f)){o.write((version+"\n").getBytes("UTF-8"));}
                }
                try(FileOutputStream o=new FileOutputStream(prep)){o.write(("READY\n"+version).getBytes("UTF-8"));}
            }catch(Exception ignored){}
            if(Build.VERSION.SDK_INT>=24) stopForeground(STOP_FOREGROUND_REMOVE); else stopForeground(true);
            stopSelf();
        }).start();
        return START_STICKY;
    }
    @Override public IBinder onBind(Intent i){return null;}
}
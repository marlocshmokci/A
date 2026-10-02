package ai.ayurones.vm;

import android.app.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class PrepareService extends Service {
    private static final String CH="ayurones_vm";
    @Override public void onCreate(){
        super.onCreate();
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel(CH,"Ayurones VM",NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
            Notification n=new Notification.Builder(this,CH)
                    .setContentTitle("Ayurones VM")
                    .setContentText("Preparing virtual device…")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true).build();
            startForeground(77,n);
        }
    }

    @Override public int onStartCommand(Intent intent,int flags,int id){
        final String version=intent==null?"Android 16":intent.getStringExtra("version");
        final String vmId=intent==null?"":intent.getStringExtra("vm_id");
        new Thread(()->{
            try{
                File guestRoot=new File(getFilesDir(),"guest");
                File guest=new File(guestRoot,vmId);
                new File(guest,"system/bin").mkdirs();
                new File(guest,"system/etc").mkdirs();
                new File(guest,"data/app").mkdirs();
                new File(guest,"data/cache").mkdirs();
                new File(guest,"var/log").mkdirs();
                for(int i=0;i<30;i++){
                    Thread.sleep(1000);
                    File chunk=new File(guest,"system/bin/module_"+String.format("%02d",i)+".ayr");
                    try(FileOutputStream o=new FileOutputStream(chunk)){
                        o.write(("Ayurones guest runtime\nVersion="+version+"\nModule="+i+"\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
                write(new File(guest,"etc/os-release"),
                        "NAME=Ayurones Guest\nVERSION="+version+"\nRUNTIME=AYR\nBASE_IMAGE=360MB\n");
                write(new File(guest,"var/log/prepare.log"),"Preparation complete for "+version+"\n");
                write(new File(guest,".ready"),"READY\n");
                VmStore.setState(this,vmId,"ready");
            }catch(Exception e){
                VmStore.setState(this,vmId,"error");
            }
            if(Build.VERSION.SDK_INT>=24) stopForeground(STOP_FOREGROUND_REMOVE); else stopForeground(true);
            stopSelf();
        }).start();
        return START_STICKY;
    }

    private void write(File f,String text)throws Exception{
        if(f.getParentFile()!=null)f.getParentFile().mkdirs();
        try(FileOutputStream o=new FileOutputStream(f)){o.write(text.getBytes(StandardCharsets.UTF_8));}
    }

    @Override public IBinder onBind(Intent i){return null;}
}
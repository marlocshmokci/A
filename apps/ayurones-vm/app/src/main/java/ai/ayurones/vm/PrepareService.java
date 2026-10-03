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
                    .setContentText("Preparing guest overlay…")
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
                File guest=new File(new File(getFilesDir(),"guest"),vmId);
                mkdir(new File(guest,"home/user"));
                mkdir(new File(guest,"etc"));
                mkdir(new File(guest,"var/log"));
                mkdir(new File(guest,"data"));
                mkdir(new File(guest,"tmp"));
                String runtime=readAsset("guest/README.runtime");
                write(new File(guest,"etc/os-release"),
                        "NAME=Ayurones Guest\nVERSION="+version+"\nRUNTIME=Alpine-aarch64-netboot\n");
                write(new File(guest,"etc/guest-runtime.conf"),
                        "base_image=embedded\narchitecture=aarch64\nboot_assets=kernel,initramfs,rootfs\n");
                write(new File(guest,"var/log/prepare.log"),
                        "Guest overlay prepared.\n"+runtime+"\n");
                write(new File(guest,"home/user/README.txt"),
                        "Ayurones guest environment\nBase runtime: Alpine Linux aarch64\nType help for the host-executed console commands.\n");
                for(int i=1;i<=6;i++){ Thread.sleep(500); write(new File(guest,"var/log/boot-"+i+".log"),"stage "+i+" complete\n"); }
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
    private void mkdir(File f){if(!f.exists())f.mkdirs();}
    private void write(File f,String s)throws Exception{
        if(f.getParentFile()!=null)f.getParentFile().mkdirs();
        try(FileOutputStream o=new FileOutputStream(f)){o.write(s.getBytes(StandardCharsets.UTF_8));}
    }
    private String readAsset(String name)throws Exception{
        try(InputStream in=getAssets().open(name)){
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] b=new byte[8192]; int n;
            while((n=in.read(b))>0)out.write(b,0,n);
            return out.toString("UTF-8");
        }
    }
    @Override public IBinder onBind(Intent i){return null;}
}
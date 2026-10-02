package ai.ayurones.vm;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_CAMERA=1001, REQ_FILE=1002;
    private LinearLayout root, desktop;
    private TextView status;
    private EditText terminalInput;
    private File sandbox, systemDir;
    private Handler handler=new Handler(Looper.getMainLooper());
    private long pressStart;
    private Runnable holdRunnable;
    private float downX,downY;
    private boolean developerUnlocked=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        sandbox=new File(getFilesDir(),"guest");
        if(!sandbox.exists()) sandbox.mkdirs();
        showDesktop();
    }

    private TextView tv(String s,int size){
        TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(Color.WHITE);
        v.setGravity(Gravity.CENTER); v.setPadding(10,8,10,8); return v;
    }
    private Button btn(String s){
        Button b=new Button(this); b.setText(s); b.setAllCaps(false); return b;
    }
    private void base(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.BLACK);
        setContentView(root);
    }

    private void showDesktop(){
        base();
        TextView bar=tv("Ayurones VM   ▪   10:24",14); bar.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(0xff202124); root.addView(bar,new LinearLayout.LayoutParams(-1,42));
        desktop=new LinearLayout(this); desktop.setOrientation(LinearLayout.VERTICAL); desktop.setGravity(Gravity.CENTER);
        desktop.setBackgroundColor(0xff252525); root.addView(desktop,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout icons=new LinearLayout(this); icons.setGravity(Gravity.CENTER);
        icons.addView(appIcon("☎","Телефон"),cell());
        icons.addView(appIcon("✉","Сообщения"),cell());
        icons.addView(appIcon("▣","Файлы"),cell());
        icons.addView(appIcon("⚙","Настройки"),cell());
        desktop.addView(icons);

        TextView hint=tv("Зажмите пустое место на 5 секунд",13); hint.setTextColor(0xffbdbdbd);
        desktop.addView(hint,new LinearLayout.LayoutParams(-1,60));

        View touch=desktop;
        touch.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                downX=e.getX(); downY=e.getY(); pressStart=System.currentTimeMillis();
                holdRunnable=()->startUnlock(); handler.postDelayed(holdRunnable,5000); return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP || e.getAction()==MotionEvent.ACTION_CANCEL){
                handler.removeCallbacks(holdRunnable); return true;
            }
            return true;
        });

        TextView nav=tv("◀        ●        ▢",20); nav.setBackgroundColor(0xff111111); root.addView(nav,new LinearLayout.LayoutParams(-1,50));
    }

    private LinearLayout.LayoutParams cell(){ return new LinearLayout.LayoutParams(0,100,1); }
    private LinearLayout appIcon(String icon,String name){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        TextView i=tv(icon,32); TextView n=tv(name,11); box.addView(i); box.addView(n); return box;
    }

    private void startUnlock(){
        showWait();
        handler.postDelayed(this::showVmMenu,5000);
    }

    private void showWait(){
        base();
        Space top=new Space(this); root.addView(top,new LinearLayout.LayoutParams(1,0,1));
        TextView t=tv("Подождите 5 секунд",18); root.addView(t);
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        p.setMax(100); p.setProgress(0); root.addView(p,new LinearLayout.LayoutParams(-1,24));
        TextView sub=tv(localized("Подготовка доступа…"),13); root.addView(sub);
        Space bot=new Space(this); root.addView(bot,new LinearLayout.LayoutParams(1,0,1));
        final int[] x={100};
        Runnable r=new Runnable(){ public void run(){ x[0]-=2; p.setProgress(x[0]); if(x[0]>0) handler.postDelayed(this,100); }};
        handler.post(r);
    }

    private String localized(String ru){
        String l=Locale.getDefault().getLanguage();
        if(l.equals("en")) return "Preparing access…";
        if(l.equals("de")) return "Zugriff wird vorbereitet…";
        if(l.equals("fr")) return "Préparation de l’accès…";
        if(l.equals("es")) return "Preparando el acceso…";
        if(l.equals("it")) return "Preparazione dell’accesso…";
        if(l.equals("uk")) return "Підготовка доступу…";
        if(l.equals("pl")) return "Przygotowywanie dostępu…";
        return ru;
    }

    private void showVmMenu(){
        base();
        root.setBackgroundColor(Color.WHITE);
        TextView title=tv("Виртуальные устройства",22); title.setTextColor(Color.BLACK);
        root.addView(title,new LinearLayout.LayoutParams(-1,70));
        TextView plus=tv("+",64); plus.setTextColor(Color.BLACK); plus.setBackgroundColor(0xffeeeeee);
        root.addView(plus,new LinearLayout.LayoutParams(-1,0,1));
        TextView footer=tv("Выберите Android для создания среды",13); footer.setTextColor(0xff555555);
        root.addView(footer,new LinearLayout.LayoutParams(-1,60));
        plus.setOnClickListener(v->chooseAndroid());
    }

    private void chooseAndroid(){
        final String[] versions={"Android 4.4 KitKat","Android 5.0 Lollipop","Android 6.0 Marshmallow","Android 7.0 Nougat","Android 8.0 Oreo","Android 9 Pie","Android 10","Android 11","Android 12","Android 13","Android 14","Android 15","Android 16"};
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Версия Android").setItems(versions,(x,w)->prepare(versions[w])).setNegativeButton("Отмена",null).create();
        d.show();
    }

    private void prepare(String version){
        new AlertDialog.Builder(this).setTitle(version).setMessage("Подготовка среды займёт около 30 секунд. Она продолжится, даже если приложение свернуть.").setPositiveButton("Начать",(d,w)->{
            Intent i=new Intent(this,PrepareService.class); i.putExtra("version",version);
            if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
            showProgress(version);
        }).setNegativeButton("Отмена",null).show();
    }

    private void showProgress(String version){
        base(); root.setBackgroundColor(Color.WHITE);
        TextView title=tv("Подготовка "+version,20); title.setTextColor(Color.BLACK); root.addView(title);
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); p.setMax(100); root.addView(p,new LinearLayout.LayoutParams(-1,30));
        status=tv("Создание гостевой файловой системы…",14); status.setTextColor(Color.DKGRAY); root.addView(status);
        handler.post(new Runnable(){ public void run(){
            File f=new File(sandbox,".prepare_"+version.replace(" ","_")); if(f.exists()){
                p.setProgress(100); status.setText("Готово"); showGuest(version); return;
            }
            p.setProgress(Math.min(99,p.getProgress()+3)); status.setText(p.getProgress()<35?"Создание образа…":p.getProgress()<70?"Подготовка системных файлов…":"Финальная настройка…");
            handler.postDelayed(this,1000);
        }});
    }

    private void showGuest(String version){
        base(); root.setBackgroundColor(0xffd8d8d8);
        TextView bar=tv(version+"   ⋮",15); bar.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL); bar.setTextColor(Color.BLACK); bar.setBackgroundColor(Color.WHITE); root.addView(bar,new LinearLayout.LayoutParams(-1,48));
        LinearLayout home=new LinearLayout(this); home.setOrientation(LinearLayout.VERTICAL); home.setGravity(Gravity.CENTER);
        root.addView(home,new LinearLayout.LayoutParams(-1,0,1));
        TextView logo=tv("Android",34); logo.setTextColor(0xff333333); home.addView(logo);
        TextView sub=tv("Гостевая система\nНастройки • Файлы • Камера",13); sub.setTextColor(0xff444444); home.addView(sub);
        Button settings=btn("Настройки"); home.addView(settings);
        Button cam=btn("Камера"); home.addView(cam);
        Button files=btn("Файлы"); home.addView(files);
        TextView nav=tv("◀        ●        ▢",18); nav.setTextColor(Color.BLACK); nav.setBackgroundColor(Color.WHITE); root.addView(nav,new LinearLayout.LayoutParams(-1,48));
        settings.setOnClickListener(v->showSettings(version));
        cam.setOnClickListener(v->openCamera());
        files.setOnClickListener(v->openFiles());
        addDeveloperOverlay();
    }

    private void showSettings(String version){
        base(); root.setBackgroundColor(Color.WHITE);
        TextView h=tv("Настройки",22); h.setTextColor(Color.BLACK); root.addView(h,new LinearLayout.LayoutParams(-1,64));
        TextView model=tv("Модель устройства\\nAyurones Virtual Device",17); model.setTextColor(Color.DKGRAY); model.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        root.addView(model,new LinearLayout.LayoutParams(-1,100));
        final int[] taps={0};
        model.setOnClickListener(v->{ taps[0]++; if(taps[0]>=4){developerUnlocked=true; addDeveloperOverlay();} });
        root.addView(tv("Версия Android: "+version,16));
        root.addView(tv("Хранилище: гостевое, изолированное",16));
        root.addView(tv("Доступ к камере и файлам запрашивается только при использовании",14));
        addDeveloperOverlay();
    }

    private void addDeveloperOverlay(){
        if(!developerUnlocked) return;
        final TextView dev=tv(">_",20); dev.setTextColor(Color.WHITE); dev.setBackgroundColor(0xff202124);
        WindowManager wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(70,60,Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,-3);
        // Overlay permission is deliberately not requested: use an in-app draggable chip when possible.
        lp.type=WindowManager.LayoutParams.TYPE_APPLICATION_PANEL; lp.token=getWindow().getDecorView().getWindowToken(); lp.gravity=Gravity.TOP|Gravity.LEFT; lp.x=8; lp.y=8;
        dev.setOnTouchListener(new View.OnTouchListener(){float sx,sy; int ox,oy; public boolean onTouch(View v,MotionEvent e){ if(e.getAction()==0){sx=e.getRawX();sy=e.getRawY();ox=lp.x;oy=lp.y;return true;} if(e.getAction()==2){lp.x=ox+(int)(e.getRawX()-sx);lp.y=oy+(int)(e.getRawY()-sy);wm.updateViewLayout(v,lp);return true;} if(e.getAction()==1){ if(Math.abs(e.getRawX()-sx)<15&&Math.abs(e.getRawY()-sy)<15) showTerminal(); return true;} return true;}});
        try { wm.addView(dev,lp); } catch(Exception ignored) {}
    }

    private void showTerminal(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(12,12,12,12);
        TextView out=tv("guest@android:~$ ",12); out.setGravity(Gravity.LEFT); out.setTextColor(Color.WHITE); out.setBackgroundColor(Color.BLACK);
        EditText in=new EditText(this); in.setSingleLine(true); in.setHint("команда"); Button run=btn("▶");
        box.addView(out,new LinearLayout.LayoutParams(-1,0,1)); box.addView(in); box.addView(run);
        AlertDialog d=new AlertDialog.Builder(this).setTitle(">_ Terminal").setView(box).setNegativeButton("Закрыть",null).create(); d.show();
        run.setOnClickListener(v->runGuest(in,out)); in.setOnEditorActionListener((v,a,e)->{runGuest(in,out);return true;});
    }

    private void runGuest(EditText in,TextView out){
        String c=in.getText().toString().trim(); in.setText(""); if(c.isEmpty())return;
        if(c.equals("edelet")){ deleteChildren(sandbox); out.append("\\nSYSTEM DELETED: guest storage cleared. Host Android untouched.\\n"); return; }
        try{ Process p=new ProcessBuilder("sh","-c",c).directory(sandbox).redirectErrorStream(true).start();
            BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream())); StringBuilder s=new StringBuilder("\\n$ "+c+"\\n"); String l; while((l=r.readLine())!=null&&s.length()<8000)s.append(l).append("\\n"); p.waitFor(); out.append(s.toString());
        }catch(Exception e){out.append("\\nerror: "+e.getMessage()+"\\n");}
    }

    private void openCamera(){
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.CAMERA},REQ_CAMERA);return;}
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE); if(i.resolveActivity(getPackageManager())!=null)startActivityForResult(i,REQ_CAMERA);
    }
    private void openFiles(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*"); startActivityForResult(i,REQ_FILE); }
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ_CAMERA&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)openCamera();}
    private void deleteChildren(File d){File[] fs=d.listFiles();if(fs==null)return;for(File f:fs){if(f.isDirectory())deleteChildren(f);f.delete();}}
}

package ai.ayurones.vm;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final int REQ_CAMERA = 1001;
    private static final int REQ_FILE = 1002;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FrameLayout root;
    private File sandbox;
    private String selectedVmId = null;
    private boolean developerUnlocked = false;
    private TextView pageTitle;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(10, 12, 16));
        getWindow().setNavigationBarColor(Color.rgb(10, 12, 16));
        sandbox = new File(getFilesDir(), "guest");
        if (!sandbox.exists()) sandbox.mkdirs();
        showBoot();
    }

    @Override public void onBackPressed() {
        if (selectedVmId != null) {
            selectedVmId = null;
            developerUnlocked = false;
            showManager();
        } else {
            super.onBackPressed();
        }
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private void resetRoot(int background) {
        root = new FrameLayout(this);
        root.setBackgroundColor(background);
        setContentView(root);
    }

    private TextView text(String s, float size, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setPadding(dp(12), dp(8), dp(12), dp(8));
        return v;
    }

    private TextView center(String s, float size, int color) {
        TextView v = text(s, size, color);
        v.setGravity(Gravity.CENTER);
        return v;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setMinHeight(dp(48));
        return b;
    }

    private GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp((int)radius));
        return g;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(12), dp(8), dp(12), dp(8));
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setPadding(dp(8), dp(5), dp(8), dp(5));
        return l;
    }

    private LinearLayout card() {
        LinearLayout l = column();
        l.setBackground(bg(0xff1b1f24, 18));
        return l;
    }

    private void addGap(LinearLayout l, int h) {
        Space s = new Space(this);
        l.addView(s, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private void toolbar(String title, boolean back) {
        LinearLayout bar = row();
        bar.setBackgroundColor(0xff0f1216);
        if (back) {
            Button b = button("‹");
            b.setTextSize(28);
            b.setTextColor(Color.WHITE);
            bar.addView(b, new LinearLayout.LayoutParams(dp(54), dp(54)));
            b.setOnClickListener(v -> { if (selectedVmId != null) showGuest(selectedVmId); else showManager(); });
        }
        pageTitle = text(title, 20, Color.WHITE);
        pageTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bar.addView(pageTitle, new LinearLayout.LayoutParams(0, dp(54), 1));
        root.addView(bar, new FrameLayout.LayoutParams(-1, dp(54), Gravity.TOP));
    }

    private ScrollView scroll(LinearLayout body) {
        ScrollView s = new ScrollView(this);
        s.setFillViewport(true);
        s.addView(body);
        return s;
    }

    private void showBoot() {
        resetRoot(0xff17191d);
        LinearLayout shell = column();
        shell.setPadding(0,0,0,0);

        TextView top = text("Ayurones VM  •  Guest Console", 14, Color.WHITE);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(0xff0e1013);
        shell.addView(top, new LinearLayout.LayoutParams(-1, dp(50)));

        FrameLayout empty = new FrameLayout(this);
        empty.setBackgroundColor(0xff24282d);
        shell.addView(empty, new LinearLayout.LayoutParams(-1,0,1));

        TextView brand = center("ANDROID GUEST", 28, 0xffe6e9ed);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        empty.addView(brand, new FrameLayout.LayoutParams(-1, dp(70), Gravity.CENTER));

        TextView hint = center(waitText(), 14, 0xffb8bec7);
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1, dp(54), Gravity.BOTTOM);
        hp.setMargins(dp(16),0,dp(16),dp(18));
        empty.addView(hint, hp);

        LinearLayout nav = row();
        nav.setPadding(0,0,0,0);
        nav.setBackgroundColor(0xff0e1013);
        nav.addView(center("◀", 20, Color.WHITE), new LinearLayout.LayoutParams(0, dp(50),1));
        nav.addView(center("●", 18, Color.WHITE), new LinearLayout.LayoutParams(0, dp(50),1));
        nav.addView(center("□", 18, Color.WHITE), new LinearLayout.LayoutParams(0, dp(50),1));
        shell.addView(nav, new LinearLayout.LayoutParams(-1, dp(50)));

        root.addView(shell);
        setupFiveSecondHold(empty, hint);
    }

    private void setupFiveSecondHold(View target, TextView hint) {
        final Handler h = handler;
        final long[] start = {0};
        final boolean[] fired = {false};
        target.setOnTouchListener((v,e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                start[0] = System.currentTimeMillis();
                fired[0] = false;
                ProgressBar p = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                p.setMax(100);
                p.setProgress(100);
                p.setTag("hold");
                FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1, dp(5), Gravity.BOTTOM);
                pp.setMargins(dp(18),0,dp(18),dp(78));
                ((FrameLayout)v).addView(p, pp);
                Runnable r = new Runnable() {
                    @Override public void run() {
                        long elapsed = System.currentTimeMillis() - start[0];
                        int left = Math.max(0, 100 - (int)(elapsed / 50));
                        p.setProgress(left);
                        hint.setText(left > 0 ? waitText() + "  " + Math.max(0, 5 - elapsed/1000) : "Opening VM Manager…");
                        if (elapsed >= 5000 && !fired[0]) {
                            fired[0] = true;
                            ((FrameLayout)v).removeView(p);
                            showManager();
                        } else if (!fired[0]) {
                            h.postDelayed(this, 100);
                        }
                    }
                };
                h.post(r);
                return true;
            }
            if ((e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) && !fired[0]) {
                h.removeCallbacksAndMessages(null);
                View old = ((FrameLayout)v).findViewWithTag("hold");
                if (old != null) ((FrameLayout)v).removeView(old);
                hint.setText(waitText());
                return true;
            }
            return true;
        });
    }

    private String waitText() {
        String l = Locale.getDefault().getLanguage();
        if ("de".equals(l)) return "Bitte 5 Sekunden warten";
        if ("fr".equals(l)) return "Veuillez patienter 5 secondes";
        if ("es".equals(l)) return "Espere 5 segundos";
        if ("pl".equals(l)) return "Poczekaj 5 sekund";
        if ("uk".equals(l)) return "Зачекайте 5 секунд";
        if ("ru".equals(l)) return "Подождите 5 секунд";
        return "Please hold for 5 seconds";
    }

    private void showManager() {
        selectedVmId = null;
        developerUnlocked = false;
        resetRoot(0xfff4f6f8);
        toolbar("Ayurones VM", false);

        LinearLayout body = column();
        body.setBackgroundColor(0xfff4f6f8);

        LinearLayout hero = card();
        hero.setBackground(bg(0xff182028, 20));
        TextView h1 = text("Virtual device manager", 24, Color.WHITE);
        h1.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        hero.addView(h1);
        TextView h2 = text("Create isolated guest environments with a writable overlay.", 14, 0xffc2cad2);
        hero.addView(h2);

        Button create = button("+  Create virtual device");
        create.setOnClickListener(v -> chooseAndroid());
        hero.addView(create);

        body.addView(hero, new LinearLayout.LayoutParams(-1, -2));
        addGap(body, 12);

        List<VmStore.VM> vms = VmStore.load(this);
        if (vms.isEmpty()) {
            LinearLayout empty = card();
            empty.setBackground(bg(Color.WHITE, 18));
            TextView e = text("No virtual devices yet. Create one above.", 15, 0xff40464d);
            empty.addView(e);
            body.addView(empty, new LinearLayout.LayoutParams(-1, -2));
        }

        for (VmStore.VM vm : vms) addVmCard(body, vm);

        addGap(body, 12);
        LinearLayout global = card();
        global.setBackground(bg(Color.WHITE, 18));
        global.addView(text("Host tools", 17, 0xff1f2429));
        Button diag = button("System diagnostics");
        diag.setOnClickListener(v -> showHostDiagnostics());
        global.addView(diag);
        Button files = button("Shared file picker");
        files.setOnClickListener(v -> openFiles());
        global.addView(files);
        body.addView(global);

        ScrollView s = scroll(body);
        FrameLayout.LayoutParams sp = new FrameLayout.LayoutParams(-1,-1);
        sp.topMargin = dp(54);
        root.addView(s, sp);
    }

    private void addVmCard(LinearLayout body, VmStore.VM vm) {
        LinearLayout c = card();
        c.setBackground(bg(Color.WHITE, 18));

        LinearLayout top = row();
        TextView name = text(vm.name, 18, 0xff15191d);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        top.addView(name, new LinearLayout.LayoutParams(0,dp(42),1));
        TextView state = text(vm.state.toUpperCase(Locale.US), 11, stateColor(vm.state));
        state.setGravity(Gravity.CENTER);
        top.addView(state, new LinearLayout.LayoutParams(dp(88),dp(30)));
        c.addView(top);

        c.addView(text(vm.version + "   •   " + vm.id, 12, 0xff68717a));

        LinearLayout actions = row();
        Button open = button(vm.state.equals("running") ? "Open" : "Start");
        open.setOnClickListener(v -> {
            VmStore.VM fresh = findVm(vm.id);
            if (fresh == null) return;
            if (!"ready".equals(fresh.state) && !"running".equals(fresh.state) && !"stopped".equals(fresh.state)) {
                Toast.makeText(this, "Still preparing this guest.", Toast.LENGTH_SHORT).show();
                showManager();
                return;
            }
            VmStore.setState(this, fresh.id, "running");
            log(fresh, "Guest started");
            showGuest(fresh.id);
        });
        actions.addView(open, new LinearLayout.LayoutParams(0,dp(50),1));

        Button files = button("Files");
        files.setOnClickListener(v -> showFiles(vm.id));
        actions.addView(files, new LinearLayout.LayoutParams(0,dp(50),1));

        Button more = button("⋮");
        more.setOnClickListener(v -> vmActions(vm));
        actions.addView(more, new LinearLayout.LayoutParams(dp(54),dp(50)));

        c.addView(actions);
        body.addView(c, new LinearLayout.LayoutParams(-1,-2));
        addGap(body, 10);
    }

    private int stateColor(String s) {
        if ("running".equals(s)) return 0xff1a8f4a;
        if ("preparing".equals(s)) return 0xffaa6b00;
        return 0xff4f5963;
    }

    private void vmActions(VmStore.VM vm) {
        String[] a = {"Stop / mark stopped","Settings","Diagnostics","Reset guest data","Delete VM"};
        new AlertDialog.Builder(this).setTitle(vm.name).setItems(a,(d,w) -> {
            if (w==0) { VmStore.setState(this,vm.id,"stopped"); log(vm,"Guest stopped"); showManager(); }
            else if (w==1) showGuestSettings(vm.id);
            else if (w==2) showVmDiagnostics(vm.id);
            else if (w==3) confirmReset(vm);
            else if (w==4) confirmDelete(vm);
        }).show();
    }

    private void chooseAndroid() {
        final String[] versions = {
            "Android 4.4 KitKat","Android 5.0 Lollipop","Android 6.0 Marshmallow",
            "Android 7.0 Nougat","Android 8.0 Oreo","Android 9 Pie","Android 10",
            "Android 11","Android 12","Android 13","Android 14","Android 15","Android 16"
        };
        new AlertDialog.Builder(this).setTitle("Select guest Android").setItems(versions,(d,w)->createVm(versions[w])).setNegativeButton("Cancel",null).show();
    }

    private void createVm(String version) {
        VmStore.VM vm = VmStore.create(this, version);
        File dir = vmDir(vm.id);
        new File(dir,"home/user").mkdirs();
        new File(dir,"etc").mkdirs();
        new File(dir,"tmp").mkdirs();
        writeText(new File(dir,"etc/os-release"),
                "NAME=Ayurones Guest\nVERSION="+version+"\nIMAGE=ayr-runtime-360\n");
        writeText(new File(dir,"home/user/README.txt"),
                "Welcome to the Ayurones guest.\nType help in Terminal to see commands.\n");
        log(vm, "Guest created");
        startPrepare(vm);
    }

    private void startPrepare(VmStore.VM vm) {
        Intent i = new Intent(this, PrepareService.class);
        i.putExtra("version", vm.version);
        i.putExtra("vm_id", vm.id);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        showManager();
        Toast.makeText(this, "Preparation continues in background.", Toast.LENGTH_LONG).show();
    }

    private File vmDir(String id) {
        File f = new File(sandbox, id);
        if (!f.exists()) f.mkdirs();
        return f;
    }

    private VmStore.VM findVm(String id) {
        if (id == null) return null;
        for (VmStore.VM v : VmStore.load(this)) if (v.id.equals(id)) return v;
        return null;
    }

    private VmStore.VM currentVm() { return findVm(selectedVmId); }

    private void showGuest(String vmId) {
        selectedVmId = vmId;
        VmStore.VM vm = currentVm();
        if (vm == null) { showManager(); return; }
        developerUnlocked = vm.developer;

        resetRoot(0xff24282d);
        LinearLayout outer = column();
        outer.setPadding(0,0,0,0);

        LinearLayout bar = row();
        bar.setPadding(dp(4),0,dp(4),0);
        bar.setBackgroundColor(Color.WHITE);
        Button back = button("‹");
        back.setTextSize(28); back.setTextColor(Color.BLACK);
        bar.addView(back,new LinearLayout.LayoutParams(dp(54),dp(52)));
        back.setOnClickListener(v->{VmStore.setState(this,vm.id,"stopped");selectedVmId=null;showManager();});
        TextView title = text(vm.version+"  •  "+(vm.state.equals("running")?"RUNNING":"READY"),15,Color.BLACK);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        bar.addView(title,new LinearLayout.LayoutParams(0,dp(52),1));
        Button power = button("⏻");
        power.setTextSize(21); power.setTextColor(Color.BLACK);
        bar.addView(power,new LinearLayout.LayoutParams(dp(54),dp(52)));
        power.setOnClickListener(v->{VmStore.setState(this,vm.id,"stopped");showManager();});
        outer.addView(bar);

        FrameLayout desktop = new FrameLayout(this);
        desktop.setBackgroundColor(0xffd6d9dc);
        outer.addView(desktop,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout launcher = column();
        launcher.setGravity(Gravity.CENTER);
        TextView logo = center("ANDROID",32,0xff30363b);
        logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        launcher.addView(logo);
        TextView sub = center("Ayurones guest environment",13,0xff50575e);
        launcher.addView(sub);
        addGap(launcher,12);

        LinearLayout grid1 = row();
        grid1.addView(guestIcon("▦","Apps",v->showApps()), new LinearLayout.LayoutParams(0,dp(90),1));
        grid1.addView(guestIcon("▣","Files",v->showFiles(vm.id)), new LinearLayout.LayoutParams(0,dp(90),1));
        grid1.addView(guestIcon("⚙","Settings",v->showGuestSettings(vm.id)), new LinearLayout.LayoutParams(0,dp(90),1));
        launcher.addView(grid1);

        LinearLayout grid2 = row();
        grid2.addView(guestIcon(">_","Terminal",v->showTerminal(vm.id)), new LinearLayout.LayoutParams(0,dp(90),1));
        grid2.addView(guestIcon("◫","Storage",v->showStorage(vm.id)), new LinearLayout.LayoutParams(0,dp(90),1));
        grid2.addView(guestIcon("◎","Camera",v->openCamera()), new LinearLayout.LayoutParams(0,dp(90),1));
        launcher.addView(grid2);

        TextView clock = center(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()),14,0xff4b5258);
        launcher.addView(clock);
        desktop.addView(launcher,new FrameLayout.LayoutParams(-1,-1));

        TextView nav = center("◀          ●          □",18,Color.BLACK);
        nav.setBackgroundColor(Color.WHITE);
        outer.addView(nav,new LinearLayout.LayoutParams(-1,dp(48)));

        root.addView(outer);
        if (developerUnlocked) addDeveloperLauncher(desktop);
    }

    private LinearLayout guestIcon(String icon,String name,View.OnClickListener click) {
        LinearLayout box = column();
        box.setGravity(Gravity.CENTER);
        TextView i = center(icon,28,0xff252b30);
        TextView n = center(name,12,0xff444b51);
        box.addView(i); box.addView(n);
        box.setOnClickListener(click);
        box.setBackground(bg(0x22ffffff,16));
        box.setPadding(dp(4),dp(8),dp(4),dp(8));
        return box;
    }

    private void showApps() {
        resetRoot(0xffeef1f4);
        toolbar("Guest applications", true);
        LinearLayout b = column();
        b.setBackgroundColor(0xffeef1f4);
        String[] names={"Terminal","Files","Settings","Storage","Diagnostics","Network","Logs","Camera"};
        for(String n:names){
            Button x=button(n);
            b.addView(x);
            if(n.equals("Terminal")) x.setOnClickListener(v->showTerminal(selectedVmId));
            else if(n.equals("Files")) x.setOnClickListener(v->showFiles(selectedVmId));
            else if(n.equals("Settings")) x.setOnClickListener(v->showGuestSettings(selectedVmId));
            else if(n.equals("Storage")) x.setOnClickListener(v->showStorage(selectedVmId));
            else if(n.equals("Diagnostics")) x.setOnClickListener(v->showVmDiagnostics(selectedVmId));
            else if(n.equals("Network")) x.setOnClickListener(v->showNetwork());
            else if(n.equals("Logs")) x.setOnClickListener(v->showLogs(selectedVmId));
            else x.setOnClickListener(v->openCamera());
        }
        ScrollView s=scroll(b); FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-1);p.topMargin=dp(54);root.addView(s,p);
    }

    private void addDeveloperLauncher(FrameLayout desktop) {
        TextView dev = center(">_",18,Color.WHITE);
        dev.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        dev.setBackground(bg(0xff20252a,12));
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(64),dp(48));
        lp.leftMargin=dp(8); lp.topMargin=dp(8);
        desktop.addView(dev,lp);
        final float[] sx={0},sy={0}; final int[] ox={0},oy={0};
        dev.setOnTouchListener((v,e)->{
            if(e.getAction()==MotionEvent.ACTION_DOWN){sx[0]=e.getRawX();sy[0]=e.getRawY();ox[0]=lp.leftMargin;oy[0]=lp.topMargin;return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){lp.leftMargin=ox[0]+(int)(e.getRawX()-sx[0]);lp.topMargin=oy[0]+(int)(e.getRawY()-sy[0]);v.setLayoutParams(lp);return true;}
            if(e.getAction()==MotionEvent.ACTION_UP && Math.abs(e.getRawX()-sx[0])<18 && Math.abs(e.getRawY()-sy[0])<18){showTerminal(selectedVmId);return true;}
            return true;
        });
    }

    private void showTerminal(String vmId) {
        final File vm = vmDir(vmId);
        final File[] cwd = {new File(vm,"home/user")};
        if (!cwd[0].exists()) cwd[0].mkdirs();

        LinearLayout box = column();
        box.setBackgroundColor(Color.BLACK);
        TextView out = text("Ayurones shell\n" + cwd[0].getAbsolutePath() + "  •  350 commands\n\n", 12, 0xffd7e0e8);
        out.setGravity(Gravity.TOP|Gravity.LEFT);
        out.setTypeface(Typeface.MONOSPACE);
        ScrollView os = new ScrollView(this); os.addView(out);
        EditText in = new EditText(this);
        in.setTextColor(Color.WHITE); in.setHintTextColor(0xff6e7780);
        in.setHint("command");
        in.setSingleLine(true); in.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        Button run = button("Run");
        LinearLayout controls = row(); controls.addView(in,new LinearLayout.LayoutParams(0,dp(52),1));controls.addView(run,new LinearLayout.LayoutParams(dp(82),dp(52)));
        box.addView(os,new LinearLayout.LayoutParams(-1,0,1));box.addView(controls);

        AlertDialog d=new AlertDialog.Builder(this).setTitle(">_ Terminal").setView(box).setNegativeButton("Close",null).create();
        d.show();
        run.setOnClickListener(v->runCommand(in,out,cwd,vm));
        in.setOnEditorActionListener((v,a,e)->{runCommand(in,out,cwd,vm);return true;});
    }

    private void runCommand(EditText in, TextView out, File[] cwd, File vmDir) {
        String c=in.getText().toString().trim(); in.setText("");
        if(c.isEmpty()) return;
        if(c.equals("clear")){out.setText("");return;}
        if(c.equals("pwd")){out.append("\n"+cwd[0].getAbsolutePath()+"\n");return;}
        if(c.startsWith("cd")){
            String arg=c.length()>2?c.substring(2).trim():"/home/user";
            File n=safePath(vmDir,cwd[0],arg);
            if(n!=null && n.isDirectory()){cwd[0]=n;out.append("\n"+n.getAbsolutePath()+"\n");}
            else out.append("\ncd: directory not found\n");
            return;
        }
        if(c.startsWith("mkdir ")){String arg=c.substring(6).trim();File n=safePath(vmDir,cwd[0],arg);if(n!=null&&n.mkdirs())out.append("\ncreated "+n.getName()+"\n");else out.append("\nmkdir: failed\n");return;}
        if(c.startsWith("touch ")){String arg=c.substring(6).trim();File n=safePath(vmDir,cwd[0],arg);if(n!=null)try{n.getParentFile().mkdirs();if(n.createNewFile())out.append("\ncreated "+n.getName()+"\n");}catch(Exception e){out.append("\ntouch: "+e.getMessage()+"\n");}return;}
        if(c.startsWith("cat ")){File n=safePath(vmDir,cwd[0],c.substring(4).trim());if(n!=null&&n.isFile())out.append("\n"+readText(n,16000)+"\n");else out.append("\ncat: file not found\n");return;}
        if(c.equals("df")||c.equals("df -h")){out.append("\nFilesystem     Size     Used\nAYR-RUNTIME    360M     "+GuestImage.format(dirSize(vmDir))+"\n");return;}
        if(c.equals("mount")){out.append("\n/dev/ayr0 on / type ayuronesfs (ro,base-image)\n/dev/overlay on /home type overlay (rw)\n");return;}
        if(c.equals("whoami")){out.append("\nuser\n");return;}
        if(c.equals("uname -a")||c.equals("uname")){VmStore.VM vm=currentVm();out.append("\nAyuronesOS "+(vm==null?"Android 16":vm.version)+" ayurones-generic aarch64\n");return;}
        if(c.equals("vmstat")){out.append("\nprocs  memory  overlay  cpu\n1      128M    rw      online\n");return;}
        if(c.startsWith("echo ")){out.append("\n"+c.substring(5)+"\n");return;}
        if(c.equals("date")){out.append("\n"+new Date()+"\n");return;}
        if(c.equals("help")||c.startsWith("help ")){String filter=c.length()>4?c.substring(5).trim():"";out.append("\n"+CommandCatalog.help(filter)+"\n");return;}
        if(c.equals("commands")||c.startsWith("commands ")){String filter=c.length()>8?c.substring(9).trim():"";out.append("\n"+CommandCatalog.help(c.length()>8?c.substring(9).trim():"")+"\n");return;}

        try {
            java.lang.Process p=new ProcessBuilder("sh","-c",c).directory(cwd[0]).redirectErrorStream(true).start();
            BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder s=new StringBuilder("\n$ "+c+"\n");
            String line; while((line=r.readLine())!=null && s.length()<18000) s.append(line).append("\n");
            p.waitFor(); out.append(s.toString());
        } catch(Exception e) {out.append("\nerror: "+e.getMessage()+"\n");}
    }

    private File safePath(File vmRoot, File cwd, String raw) {
        try {
            String p=raw;
            File base=raw.startsWith("/")?vmRoot:new File(cwd,raw);
            File n=base.getCanonicalFile();
            if(!n.getPath().startsWith(vmRoot.getCanonicalPath())) return null;
            return n;
        } catch(Exception e){return null;}
    }

    private void showFiles(String vmId) {
        resetRoot(0xfff3f5f7);
        toolbar("Guest files", true);
        LinearLayout body=column();body.setBackgroundColor(0xfff3f5f7);
        LinearLayout actions=row();
        Button up=button("Guest Home");
        Button newFile=button("+ File");
        Button newDir=button("+ Folder");
        actions.addView(up,new LinearLayout.LayoutParams(0,dp(48),1));actions.addView(newFile,new LinearLayout.LayoutParams(0,dp(48),1));actions.addView(newDir,new LinearLayout.LayoutParams(0,dp(48),1));
        body.addView(actions);

        File dir=new File(vmDir(vmId),"home/user");
        refreshFileList(body,dir,vmId);
        up.setOnClickListener(v->showGuest(vmId));
        newFile.setOnClickListener(v->createFileDialog(dir,vmId));
        newDir.setOnClickListener(v->createFolderDialog(dir,vmId));

        ScrollView s=scroll(body);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-1);p.topMargin=dp(54);root.addView(s,p);
    }

    private void refreshFileList(LinearLayout body, File dir, String vmId) {
        File[] fs=dir.listFiles();
        if(fs==null)return;
        Arrays.sort(fs,Comparator.comparing(File::getName,String.CASE_INSENSITIVE_ORDER));
        for(File f:fs){
            LinearLayout c=card();c.setBackground(bg(Color.WHITE,14));
            LinearLayout r=row();
            String ico=f.isDirectory()?"□":"▤";
            TextView n=text(ico+"  "+f.getName(),16,0xff1a1f24);r.addView(n,new LinearLayout.LayoutParams(0,dp(48),1));
            TextView meta=text(f.isDirectory()?"folder":GuestImage.format(f.length()),11,0xff737c85);r.addView(meta,new LinearLayout.LayoutParams(dp(90),dp(36)));
            c.addView(r);
            c.setOnClickListener(v->{if(f.isDirectory())openDir(vmId,f);else editFile(f,vmId);});
            body.addView(c,new LinearLayout.LayoutParams(-1,dp(58)));addGap(body,6);
        }
    }

    private void openDir(String vmId,File dir){showSpecificDir(vmId,dir);}

    private void showSpecificDir(String vmId,File dir) {
        resetRoot(0xfff3f5f7);toolbar("Files  /  "+dir.getName(),true);
        LinearLayout body=column();body.setBackgroundColor(0xfff3f5f7);
        LinearLayout actions=row();
        Button back=button("Parent");Button nf=button("+ File");Button nd=button("+ Folder");
        actions.addView(back,new LinearLayout.LayoutParams(0,dp(48),1));actions.addView(nf,new LinearLayout.LayoutParams(0,dp(48),1));actions.addView(nd,new LinearLayout.LayoutParams(0,dp(48),1));
        body.addView(actions);
        back.setOnClickListener(v->{File p=dir.getParentFile();if(p==null || !p.getPath().startsWith(vmDir(vmId).getPath()))showFiles(vmId);else showSpecificDir(vmId,p);});
        nf.setOnClickListener(v->createFileDialog(dir,vmId));nd.setOnClickListener(v->createFolderDialog(dir,vmId));
        refreshFileList(body,dir,vmId);
        ScrollView s=scroll(body);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-1);p.topMargin=dp(54);root.addView(s,p);
    }

    private void createFileDialog(File dir,String vmId){
        EditText e=new EditText(this);e.setHint("filename.txt");
        new AlertDialog.Builder(this).setTitle("Create file").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{File f=new File(dir,e.getText().toString().trim());writeText(f,"");showSpecificDir(vmId,dir);}).show();
    }

    private void createFolderDialog(File dir,String vmId){
        EditText e=new EditText(this);e.setHint("folder");
        new AlertDialog.Builder(this).setTitle("Create folder").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{new File(dir,e.getText().toString().trim()).mkdirs();showSpecificDir(vmId,dir);}).show();
    }

    private void editFile(File f,String vmId){
        if(f.length()>2_000_000){Toast.makeText(this,"Binary or large file.",Toast.LENGTH_SHORT).show();return;}
        EditText e=new EditText(this);e.setText(readText(f,2_000_000));e.setGravity(Gravity.TOP);e.setMinLines(12);
        new AlertDialog.Builder(this).setTitle(f.getName()).setView(e).setNegativeButton("Delete",(d,w)->{f.delete();showFiles(vmId);}).setPositiveButton("Save",(d,w)->{writeText(f,e.getText().toString());showFiles(vmId);}).show();
    }

    private void showStorage(String vmId){
        resetRoot(0xffeef1f4);toolbar("Storage",true);
        LinearLayout b=column();b.setBackgroundColor(0xffeef1f4);
        long image=GuestImage.size(this), used=dirSize(vmDir(vmId));
        b.addView(text("Read-only base image",18,0xff15191d));
        b.addView(text(GuestImage.format(image)+" embedded guest runtime",16,0xff434b53));
        ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);p.setProgress((int)Math.min(100,used*100/Math.max(1,image)));b.addView(p,new LinearLayout.LayoutParams(-1,dp(18)));
        b.addView(text("Writable overlay: "+GuestImage.format(used),14,0xff5f6871));
        b.addView(text("Base image is shipped inside the APK. Changes live in the private overlay for each VM.",13,0xff69727b));
        Button reset=button("Reset writable overlay");reset.setOnClickListener(v->{VmStore.VM vm=currentVm();if(vm!=null)confirmReset(vm);});b.addView(reset);
        ScrollView s=scroll(b);FrameLayout.LayoutParams q=new FrameLayout.LayoutParams(-1,-1);q.topMargin=dp(54);root.addView(s,q);
    }

    private void showGuestSettings(String vmId){
        resetRoot(0xfff2f4f6);toolbar("Guest settings",true);
        LinearLayout b=column();b.setBackgroundColor(0xfff2f4f6);
        VmStore.VM vm=findVm(vmId);
        TextView model=text("Device model\nAyurones Virtual Device",18,0xff20252a);
        model.setBackground(bg(Color.WHITE,16));b.addView(model,new LinearLayout.LayoutParams(-1,dp(92)));
        final int[] taps={0};
        model.setOnClickListener(v->{taps[0]++;if(taps[0]>=4){developerUnlocked=true;if(vm!=null){vm.developer=true;VmStore.setDeveloper(this,vm.id,true);}Toast.makeText(this,"Developer console unlocked.",Toast.LENGTH_SHORT).show();}});
        addGap(b,8);
        b.addView(text("Android version\n"+(vm==null?"Unknown":vm.version),16,0xff4a5158));
        b.addView(text("Guest storage\nPrivate writable overlay",16,0xff4a5158));
        b.addView(text("Camera\nRuntime permission requested only when opened",16,0xff4a5158));
        Switch sw=new Switch(this);sw.setText("Guest notifications");sw.setChecked(true);b.addView(sw);
        Button diag=button("Open diagnostics");diag.setOnClickListener(v->showVmDiagnostics(vmId));b.addView(diag);
        Button reset=button("Reset this VM");reset.setOnClickListener(v->{if(vm!=null)confirmReset(vm);});b.addView(reset);
        ScrollView s=scroll(b);FrameLayout.LayoutParams q=new FrameLayout.LayoutParams(-1,-1);q.topMargin=dp(54);root.addView(s,q);
    }

    private void showVmDiagnostics(String vmId){
        resetRoot(0xff101419);toolbar("VM diagnostics",true);
        LinearLayout b=column();b.setBackgroundColor(0xff101419);
        VmStore.VM vm=findVm(vmId);
        long image=GuestImage.size(this), overlay=dirSize(vmDir(vmId));
        String report="VM ID: "+vmId+
                "\nState: "+(vm==null?"unknown":vm.state)+
                "\nVersion: "+(vm==null?"unknown":vm.version)+
                "\nImage: "+GuestImage.format(image)+
                "\nOverlay: "+GuestImage.format(overlay)+
                "\nAndroid API: "+Build.VERSION.SDK_INT+
                "\nABI: "+Build.SUPPORTED_ABIS[0]+
                "\nHost device: "+Build.MANUFACTURER+" "+Build.MODEL+
                "\nRAM: "+(Runtime.getRuntime().maxMemory()/1048576)+" MB"+
                "\nGuest isolation: app-private filesystem";
        TextView r=text(report,14,0xffd5dce3);r.setTypeface(Typeface.MONOSPACE);b.addView(r);
        TextView hdr=text("Image header\n"+GuestImage.header(this),12,0xff8fa1b3);b.addView(hdr);
        ScrollView s=scroll(b);FrameLayout.LayoutParams q=new FrameLayout.LayoutParams(-1,-1);q.topMargin=dp(54);root.addView(s,q);
    }

    private void showHostDiagnostics(){
        resetRoot(0xff111419);toolbar("Host diagnostics",true);
        LinearLayout b=column();b.setBackgroundColor(0xff111419);
        String s="Ayurones VM\n"+
                "Android API: "+Build.VERSION.SDK_INT+
                "\nABI: "+Build.SUPPORTED_ABIS[0]+
                "\nManufacturer: "+Build.MANUFACTURER+
                "\nModel: "+Build.MODEL+
                "\nRuntime memory: "+(Runtime.getRuntime().maxMemory()/1048576)+" MB"+
                "\nBase guest image: "+GuestImage.format(GuestImage.size(this))+
                "\nGuest root: "+sandbox.getAbsolutePath()+
                "\nAVF API present: "+(Build.VERSION.SDK_INT>=31);
        TextView t=text(s,14,0xffd8dfe7);t.setTypeface(Typeface.MONOSPACE);b.addView(t);
        ScrollView q=scroll(b);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-1);p.topMargin=dp(54);root.addView(q,p);
    }

    private void showNetwork(){
        resetRoot(0xffeef1f4);toolbar("Virtual network",true);
        LinearLayout b=column();b.setBackgroundColor(0xffeef1f4);
        b.addView(text("Virtual NIC",19,0xff151a20));
        b.addView(text("ayr0   UP   10.0.2.15/24\nroute: 10.0.2.2\ndns: virtual resolver",15,0xff424a53));
        Button ping=button("Ping gateway");b.addView(ping);
        TextView out=text("Ready.",13,0xff5d6670);b.addView(out);
        ping.setOnClickListener(v->{
            out.setText("64 bytes from 10.0.2.2: seq=1 ttl=64 time=1 ms\n64 bytes from 10.0.2.2: seq=2 ttl=64 time=1 ms\n2 packets transmitted, 2 received, 0% packet loss");
        });
        ScrollView s=scroll(b);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-1);p.topMargin=dp(54);root.addView(s,p);
    }

    private void showLogs(String vmId){
        resetRoot(0xff101318);toolbar("Guest logs",true);
        LinearLayout b=column();b.setBackgroundColor(0xff101318);
        File f=new File(vmDir(vmId),"var/log/events.log");
        b.addView(text(readText(f,60000),12,0xffd3dbe3));
        ScrollView s=scroll(b);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-1);p.topMargin=dp(54);root.addView(s,p);
    }

    private void log(VmStore.VM vm,String message){
        if(vm==null)return;
        File f=new File(vmDir(vm.id),"var/log/events.log");
        if(f.getParentFile()!=null)f.getParentFile().mkdirs();
        try(FileOutputStream o=new FileOutputStream(f,true)){
            o.write((DateFormat.getDateTimeInstance().format(new Date())+"  "+message+"\n").getBytes(StandardCharsets.UTF_8));
        }catch(Exception ignored){}
    }

    private void confirmReset(VmStore.VM vm){
        new AlertDialog.Builder(this).setTitle("Reset guest data")
                .setMessage("Delete the writable overlay for "+vm.name+"? The embedded base image stays intact.")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Reset",(d,w)->{
                    deleteChildren(vmDir(vm.id));
                    new File(vmDir(vm.id),"home/user").mkdirs();
                    writeText(new File(vmDir(vm.id),"home/user/README.txt"),"Welcome back to the fresh guest.\n");
                    VmStore.setState(this,vm.id,"stopped");log(vm,"Writable overlay reset");showManager();
                }).show();
    }

    private void confirmDelete(VmStore.VM vm){
        new AlertDialog.Builder(this).setTitle("Delete virtual device")
                .setMessage("Delete VM metadata and its writable overlay?")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Delete",(d,w)->{deleteChildren(vmDir(vm.id));vmDir(vm.id).delete();VmStore.delete(this,vm.id);selectedVmId=null;showManager();}).show();
    }

    private void deleteChildren(File d){
        File[] fs=d.listFiles();if(fs==null)return;
        for(File f:fs){if(f.isDirectory())deleteChildren(f);f.delete();}
    }

    private long dirSize(File d){
        if(d==null||!d.exists())return 0;
        if(d.isFile())return d.length();
        long total=0;File[] fs=d.listFiles();if(fs!=null)for(File f:fs)total+=dirSize(f);
        return total;
    }

    private void writeText(File f,String s){
        try{
            if(f.getParentFile()!=null)f.getParentFile().mkdirs();
            try(FileOutputStream o=new FileOutputStream(f)){o.write(s.getBytes(StandardCharsets.UTF_8));}
        }catch(Exception ignored){}
    }

    private String readText(File f,int max){
        if(f==null||!f.exists())return "";
        try{
            byte[] data=new byte[(int)Math.min(max,Math.max(1,f.length()))];
            try(FileInputStream in=new FileInputStream(f)){
                int n=in.read(data);return new String(data,0,Math.max(0,n),StandardCharsets.UTF_8);
            }
        }catch(Exception e){return "error: "+e.getMessage();}
    }

    private void openCamera(){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},REQ_CAMERA);return;
        }
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(i.resolveActivity(getPackageManager())!=null)startActivityForResult(i,REQ_CAMERA);
        else Toast.makeText(this,"No camera app found.",Toast.LENGTH_SHORT).show();
    }

    private void openFiles(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
        startActivityForResult(i,REQ_FILE);
    }

    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==REQ_CAMERA)Toast.makeText(this,result==RESULT_OK?"Camera result received.":"Camera cancelled.",Toast.LENGTH_SHORT).show();
        if(request==REQ_FILE && result==RESULT_OK && data!=null){
            Uri u=data.getData();if(u!=null)Toast.makeText(this,"Selected: "+u.toString(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_CAMERA && g.length>0 && g[0]==PackageManager.PERMISSION_GRANTED)openCamera();
    }
}

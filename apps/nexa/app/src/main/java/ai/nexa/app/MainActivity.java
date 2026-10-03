package ai.nexa.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    AppStore store;
    FrameLayout root, body;
    LinearLayout page, bottom;
    String currentScreen = "home";
    int fg, muted, card, line, surface;
    int accent;
    String incomingShare = null;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        store = new AppStore(this);
        incomingShare = getIntent() != null ? getIntent().getStringExtra(Intent.EXTRA_TEXT) : null;
        buildShell();
        showHome();
        if (incomingShare != null && !incomingShare.trim().isEmpty()) {
            new Handler().postDelayed(() -> openComposer(incomingShare), 300);
        }
    }

    @Override protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        String s = i != null ? i.getStringExtra(Intent.EXTRA_TEXT) : null;
        if (s != null && !s.trim().isEmpty()) openComposer(s);
    }

    void colors() {
        if ("light".equals(store.theme)) {
            fg=Color.rgb(18,19,23); muted=Color.rgb(100,103,112); card=Color.WHITE; line=Color.rgb(222,224,230); surface=Color.rgb(245,246,249);
        } else if ("amoled".equals(store.theme)) {
            fg=Color.WHITE; muted=Color.rgb(145,149,160); card=Color.BLACK; line=Color.rgb(40,40,44); surface=Color.rgb(10,10,12);
        } else {
            fg=Color.rgb(245,246,250); muted=Color.rgb(151,155,166); card=Color.rgb(22,24,31); line=Color.rgb(46,49,59); surface=Color.rgb(10,11,15);
        }
        try { accent=Color.parseColor(store.accent); } catch(Exception e){accent=Color.rgb(108,99,255);}
    }

    int dp(int v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    TextView tv(String s,float size){ TextView t=new TextView(this);t.setText(s);t.setTextSize(size*store.textScale);t.setTextColor(fg);t.setGravity(Gravity.CENTER_VERTICAL);return t; }
    TextView tvm(String s,float size){TextView t=tv(s,size);t.setTextColor(muted);return t;}
    GradientDrawable bg(int color,float radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp((int)radius));return g;}
    GradientDrawable stroke(int color,int stroke,float radius){GradientDrawable g=bg(Color.TRANSPARENT,radius);g.setStroke(dp(stroke),color);return g;}
    Button action(String label){Button b=new Button(this);b.setText(label);b.setTextSize(13*store.textScale);b.setTextColor(fg);b.setAllCaps(false);b.setPadding(dp(10),0,dp(10),0);b.setMinHeight(0);b.setMinimumHeight(0);b.setBackground(stroke(line,1,12));return b;}
    Button primary(String label){Button b=action(label);b.setTextColor(Color.WHITE);b.setTextSize(14*store.textScale);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(bg(accent,14));return b;}
    void pad(View v,int l,int t,int r,int b){v.setPadding(dp(l),dp(t),dp(r),dp(b));}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    void add(LinearLayout p,View v,int w,int h){p.addView(v,new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h)));}

    void buildShell(){
        colors();
        root=new FrameLayout(this);root.setBackgroundColor(surface);setContentView(root);
        body=new FrameLayout(this);root.addView(body,new FrameLayout.LayoutParams(-1,-1));
        bottom=new LinearLayout(this);bottom.setOrientation(LinearLayout.HORIZONTAL);bottom.setGravity(Gravity.CENTER);
        bottom.setBackgroundColor(card);FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,dp(70),Gravity.BOTTOM);root.addView(bottom,bp);
        addNav("⌂","Home",0);addNav("✉","Chats",1);addNav("⌕","Search",2);addNav("♡","Alerts",3);addNav("◉","Profile",4);
    }

    void refreshShell(){colors();root.setBackgroundColor(surface);bottom.setBackgroundColor(card);showScreen(currentScreen);}

    void addNav(String icon,String label,int index){
        LinearLayout cell=col();cell.setGravity(Gravity.CENTER);TextView i=tv(icon,22);i.setGravity(Gravity.CENTER);TextView l=tvm(label,11);l.setGravity(Gravity.CENTER);
        add(cell,i,-1,30);add(cell,l,-1,22);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(70),1);bottom.addView(cell,lp);
        cell.setOnClickListener(v->{if(index==0)showHome();if(index==1)showChats();if(index==2)showSearch();if(index==3)showAlerts();if(index==4)showProfile();});
    }

    void clearBody(){body.removeAllViews();}
    void showScreen(String s){if("home".equals(s))showHome();else if("chats".equals(s))showChats();else if("search".equals(s))showSearch();else if("alerts".equals(s))showAlerts();else if("profile".equals(s))showProfile();}

    void base(String title,String subtitle,boolean back){
        clearBody();currentScreen=title.toLowerCase(Locale.US);colors();
        LinearLayout shell=col();shell.setBackgroundColor(surface);body.addView(shell,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout top=row();pad(top,14,10,14,10);top.setBackgroundColor(card);
        if(back){TextView b=tv("‹",32);b.setGravity(Gravity.CENTER);top.addView(b,new LinearLayout.LayoutParams(dp(40),dp(52)));b.setOnClickListener(v->showScreen("home"));}
        LinearLayout tt=col();TextView h=tv(title,22);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView sub=subtitle==null?null:tvm(subtitle,11);add(tt,h,-1,30);if(sub!=null)add(tt,sub,-1,22);top.addView(tt,new LinearLayout.LayoutParams(0,dp(52),1));
        shell.addView(top,new LinearLayout.LayoutParams(-1,dp(72)));
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(page,new ScrollView.LayoutParams(-1,-2));shell.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
    }

    void showHome(){base("NEXA","Public posts • local-first",false);
        LinearLayout hero=row();pad(hero,14,12,14,8);
        TextView x=tv("What’s happening?",16);x.setTypeface(Typeface.DEFAULT,Typeface.BOLD);hero.addView(x,new LinearLayout.LayoutParams(0,40,1));
        Button c=primary("Post");hero.addView(c,new LinearLayout.LayoutParams(dp(84),dp(40)));c.setOnClickListener(v->openComposer(""));
        page.addView(hero,new LinearLayout.LayoutParams(-1,dp(60)));
        for(AppStore.Post p:store.posts) page.addView(postCard(p));
        if(store.posts.isEmpty()){TextView e=tvm("No posts yet. Start the conversation.",16);pad(e,18,40,18,40);page.addView(e);}
    }

    View postCard(AppStore.Post p){
        LinearLayout box=col();box.setBackgroundColor(card);pad(box,16,14,16,12);
        LinearLayout head=row();TextView av=avatar(p.name.substring(0,1).toUpperCase(Locale.US));head.addView(av,new LinearLayout.LayoutParams(dp(44),dp(44)));
        LinearLayout who=col();TextView n=tv(p.name+"  @"+p.handle,15);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView tm=tvm(p.time,12);add(who,n,-1,24);add(who,tm,-1,20);head.addView(who,new LinearLayout.LayoutParams(0,44,1));box.addView(head,new LinearLayout.LayoutParams(-1,dp(44)));
        TextView tx=tv(p.text,16);tx.setLineSpacing(0,1.08f);pad(tx,0,12,0,10);box.addView(tx,new LinearLayout.LayoutParams(-1,-2));
        if(p.id.equals("p4")){TextView media=tv("▧   MEDIA DROP",12);media.setGravity(Gravity.CENTER);media.setTextColor(accent);media.setBackground(bg(surface,14));box.addView(media,new LinearLayout.LayoutParams(-1,dp(120)));pad(media,0,0,0,0);}
        LinearLayout acts=row();pad(acts,0,8,0,0);
        Button rep=action("Reply  "+p.replies), rt=action("↻  "+p.reposts), lk=action((p.liked?"♥  ":"♡  ")+p.likes), bm=action("▱");
        acts.addView(rep,new LinearLayout.LayoutParams(0,dp(40),1));acts.addView(rt,new LinearLayout.LayoutParams(0,dp(40),1));acts.addView(lk,new LinearLayout.LayoutParams(0,dp(40),1));acts.addView(bm,new LinearLayout.LayoutParams(0,dp(40),1));
        rep.setOnClickListener(v->replyTo(p));lk.setOnClickListener(v->{p.liked=!p.liked;int n=toInt(p.likes)+(p.liked?1:-1);p.likes=String.valueOf(Math.max(0,n));store.save();showHome();});
        rt.setOnClickListener(v->{p.reposts=String.valueOf(toInt(p.reposts)+1);store.save();showHome();toast("Reposted to your profile");});
        bm.setOnClickListener(v->toast("Saved to bookmarks"));
        box.addView(acts,new LinearLayout.LayoutParams(-1,dp(42)));Space sp=new Space(this);sp.setBackgroundColor(line);box.addView(sp,new LinearLayout.LayoutParams(-1,dp(1)));return box;
    }

    int toInt(String s){try{return Integer.parseInt(s.replaceAll("[^0-9]",""));}catch(Exception e){return 0;}}
    TextView avatar(String initial){TextView a=tv(initial,18);a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);a.setGravity(Gravity.CENTER);a.setTextColor(Color.WHITE);a.setBackground(bg(accent,50));return a;}

    void replyTo(AppStore.Post p){openComposer("↳ @"+p.handle+" ");}

    void openComposer(String seed){
        final EditText input=new EditText(this);input.setText(seed);input.setTextColor(fg);input.setHintTextColor(muted);input.setHint("Say something…");input.setTextSize(17*store.textScale);input.setGravity(Gravity.TOP);input.setMinLines(5);input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);input.setBackground(stroke(line,1,16));pad(input,14,12,14,12);
        LinearLayout c=col();pad(c,16,12,16,8);
        TextView count=tvm("300 characters",12);c.addView(count,new LinearLayout.LayoutParams(-1,dp(28)));c.addView(input,new LinearLayout.LayoutParams(-1,dp(150)));
        LinearLayout tools=row();TextView hint=tvm("Text • replies • mentions",12);tools.addView(hint,new LinearLayout.LayoutParams(0,dp(44),1));Button send=primary("Publish");tools.addView(send,new LinearLayout.LayoutParams(dp(110),dp(44)));c.addView(tools);
        input.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int d){}public void onTextChanged(CharSequence s,int a,int b,int d){count.setText((300-s.length())+" characters");}public void afterTextChanged(android.text.Editable e){}});
        AlertDialog d=new AlertDialog.Builder(this).setTitle("New post").setView(c).create();send.setOnClickListener(v->{String text=input.getText().toString().trim();if(text.isEmpty()){toast("Write something first");return;}store.posts.add(0,new AppStore.Post("p"+System.currentTimeMillis(),store.name,store.handle,text,"now","0","0","0",false));store.save();d.dismiss();showHome();toast("Published");});d.show();
    }

    void showChats(){base("Chats","Private messages & rooms",false);
        LinearLayout top=row();pad(top,14,10,14,8);Button dm=primary("New chat");Button group=action("New group");top.addView(dm,new LinearLayout.LayoutParams(0,dp(42),1));top.addView(group,new LinearLayout.LayoutParams(dp(120),dp(42)));dm.setOnClickListener(v->newChatDialog());group.setOnClickListener(v->toast("Group creator is ready for backend sync"));page.addView(top);
        TextView label=tvm("MESSAGES",11);label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);pad(label,16,14,16,8);page.addView(label);
        for(AppStore.Chat c:store.chats) page.addView(chatRow(c));
        TextView cl=tvm("CHANNELS",11);cl.setTypeface(Typeface.DEFAULT,Typeface.BOLD);pad(cl,16,22,16,8);page.addView(cl);
        for(AppStore.Channel c:store.channels) page.addView(channelRow(c));
    }

    View chatRow(AppStore.Chat c){
        LinearLayout r=row();r.setBackgroundColor(card);pad(r,14,11,14,11);TextView av=avatar(c.title.substring(0,1));r.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));LinearLayout mid=col();TextView n=tv(c.title,16);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView pr=tvm(c.preview,13);add(mid,n,-1,25);add(mid,pr,-1,24);r.addView(mid,new LinearLayout.LayoutParams(0,48,1));LinearLayout rr=col();rr.setGravity(Gravity.RIGHT);TextView tm=tvm(c.time,11);add(rr,tm,-1,22);if(!c.badge.isEmpty()){TextView bd=tv(c.badge,10);bd.setTextColor(Color.WHITE);bd.setGravity(Gravity.CENTER);bd.setBackground(bg(accent,12));add(rr,bd,26,22);}r.addView(rr,new LinearLayout.LayoutParams(dp(45),48));r.setOnClickListener(v->openChat(c));return r;
    }
    View channelRow(AppStore.Channel c){
        LinearLayout r=row();pad(r,14,10,14,10);TextView av=avatar(c.initial);r.addView(av,new LinearLayout.LayoutParams(dp(46),dp(46)));LinearLayout mid=col();TextView n=tv(c.name+"  ·  "+c.members,15);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView d=tvm(c.description,12);add(mid,n,-1,24);add(mid,d,-1,22);r.addView(mid,new LinearLayout.LayoutParams(0,46,1));Button b=action(c.joined?"Joined":"Join");r.addView(b,new LinearLayout.LayoutParams(dp(78),dp(40)));b.setOnClickListener(v->{c.joined=!c.joined;store.save();showChats();toast(c.joined?"Joined "+c.name:"Left "+c.name);});return r;
    }

    void openChat(AppStore.Chat c){
        clearBody();currentScreen="chats";colors();
        LinearLayout shell=col();body.addView(shell,new FrameLayout.LayoutParams(-1,-1));LinearLayout top=row();top.setBackgroundColor(card);pad(top,10,8,10,8);TextView back=tv("‹",32);top.addView(back,new LinearLayout.LayoutParams(dp(42),dp(54)));LinearLayout ttl=col();TextView n=tv(c.title,18);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView st=tvm(c.online?"online":"last active today",11);add(ttl,n,-1,27);add(ttl,st,-1,22);top.addView(ttl,new LinearLayout.LayoutParams(0,54,1));Button info=action("⋯");top.addView(info,new LinearLayout.LayoutParams(dp(48),dp(40)));back.setOnClickListener(v->showChats());
        shell.addView(top,new LinearLayout.LayoutParams(-1,dp(70)));
        LinearLayout msgs=col();ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(msgs,new ScrollView.LayoutParams(-1,-2));shell.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        for(AppStore.Message m:c.messages) msgs.addView(messageBubble(m));
        LinearLayout composer=row();composer.setBackgroundColor(card);pad(composer,10,10,10,10);EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Message "+c.title);input.setTextColor(fg);input.setHintTextColor(muted);input.setTextSize(15*store.textScale);input.setBackground(stroke(line,1,18));pad(input,12,0,12,0);composer.addView(input,new LinearLayout.LayoutParams(0,48,1));Button send=primary("Send");composer.addView(send,new LinearLayout.LayoutParams(dp(84),dp(44)));send.setOnClickListener(v->{String s=input.getText().toString().trim();if(s.isEmpty())return;AppStore.Message m=new AppStore.Message("m"+System.currentTimeMillis(),store.name,s,"now",true);c.messages.add(m);c.preview=s;c.time="now";store.save();msgs.addView(messageBubble(m));input.setText("");sc.postDelayed(()->sc.fullScroll(View.FOCUS_DOWN),50);});shell.addView(composer,new LinearLayout.LayoutParams(-1,dp(70)));
        sc.postDelayed(()->sc.fullScroll(View.FOCUS_DOWN),100);
    }

    View messageBubble(AppStore.Message m){
        LinearLayout wrap=row();wrap.setGravity(m.me?Gravity.RIGHT:Gravity.LEFT);pad(wrap,14,5,14,5);LinearLayout b=col();b.setPadding(dp(12),dp(8),dp(12),dp(7));b.setBackground(bg(m.me?accent:card,18));TextView sender=tvm(m.me?"You":m.sender,11);if(m.me)sender.setTextColor(0xDDEFFFFF);TextView text=tv(m.text,15);text.setLineSpacing(0,1.05f);TextView tm=tvm((m.time==null?"":m.time)+(m.reaction.isEmpty()?"":"   "+m.reaction),10);if(m.me)tm.setTextColor(0xBBFFFFFF);b.addView(sender);b.addView(text);b.addView(tm);wrap.addView(b,new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.78),-2));wrap.setOnLongClickListener(v->{reactMessage(m);return true;});return wrap;
    }

    void reactMessage(AppStore.Message m){String[] a={"♡","👍","😂","🔥","✅","!",""};new AlertDialog.Builder(this).setTitle("React to message").setItems(a,(d,w)->{m.reaction=a[w];store.save();toast(m.reaction.isEmpty()?"Reaction removed":"Reaction added");}).show();}

    void newChatDialog(){
        LinearLayout c=col();pad(c,16,6,16,6);EditText e=new EditText(this);e.setHint("Name or @handle");e.setTextColor(fg);e.setHintTextColor(muted);e.setBackground(stroke(line,1,14));pad(e,12,0,12,0);c.addView(e,new LinearLayout.LayoutParams(-1,52));new AlertDialog.Builder(this).setTitle("New chat").setView(c).setPositiveButton("Start",(d,w)->{String name=e.getText().toString().trim();if(name.isEmpty())return;AppStore.Chat x=new AppStore.Chat("c"+System.currentTimeMillis(),name,name.toLowerCase(Locale.US).replace(" ",""),"New conversation","now","",true);store.chats.add(0,x);store.save();openChat(x);}).setNegativeButton("Cancel",null).show();
    }

    void showSearch(){base("Search","People, posts, chats & channels",false);
        EditText q=new EditText(this);q.setHint("Search NEXA");q.setTextColor(fg);q.setHintTextColor(muted);q.setTextSize(16*store.textScale);q.setSingleLine(true);q.setBackground(stroke(line,1,15));pad(q,14,0,14,0);page.addView(q,new LinearLayout.LayoutParams(-1,52));LinearLayout results=col();page.addView(results);
        q.setOnEditorActionListener((v,id,e)->{runSearch(q.getText().toString(),results);return true;});
        q.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){runSearch(s.toString(),results);}public void afterTextChanged(android.text.Editable e){}});runSearch("",results);
    }

    void runSearch(String q,LinearLayout r){
        r.removeAllViews();String qq=q.trim().toLowerCase(Locale.US);
        TextView hint=tvm(qq.isEmpty()?"Try “design”, “Maya” or “product”.":"Results for “"+q+"”",12);pad(hint,16,16,16,10);r.addView(hint);
        int hits=0;
        for(AppStore.Post p:store.posts)if(qq.isEmpty()||p.text.toLowerCase(Locale.US).contains(qq)||p.name.toLowerCase(Locale.US).contains(qq)||p.handle.toLowerCase(Locale.US).contains(qq)){r.addView(postCard(p));hits++;}
        for(AppStore.Chat c:store.chats)if(!qq.isEmpty()&&(c.title.toLowerCase(Locale.US).contains(qq)||c.preview.toLowerCase(Locale.US).contains(qq))){TextView t=tv("✉  "+c.title+" — "+c.preview,15);t.setBackgroundColor(card);pad(t,16,12,16,12);r.addView(t);hits++;}
        for(AppStore.Channel c:store.channels)if(!qq.isEmpty()&&(c.name.toLowerCase(Locale.US).contains(qq)||c.handle.toLowerCase(Locale.US).contains(qq))){r.addView(channelRow(c));hits++;}
        if(hits==0&&!qq.isEmpty()){TextView e=tvm("Nothing found. Try another phrase.",14);pad(e,16,24,16,24);r.addView(e);}
    }

    void showAlerts(){base("Alerts","Mentions, likes & messages",false);Button mark=primary("Mark all read");pad(mark,14,12,14,4);page.addView(mark,new LinearLayout.LayoutParams(-1,44));mark.setOnClickListener(v->{for(AppStore.Notice n:store.notices)n.unread=false;store.save();showAlerts();});
        for(AppStore.Notice n:store.notices){LinearLayout r=row();pad(r,15,13,15,13);TextView ic=tv(n.icon,22);ic.setGravity(Gravity.CENTER);r.addView(ic,new LinearLayout.LayoutParams(dp(42),dp(42)));LinearLayout mid=col();TextView tx=tv(n.text,15);TextView tm=tvm(n.time,11);add(mid,tx,-1,25);add(mid,tm,-1,20);r.addView(mid,new LinearLayout.LayoutParams(0,42,1));if(n.unread){TextView dot=tv("●",10);dot.setTextColor(accent);r.addView(dot,new LinearLayout.LayoutParams(dp(20),42));}page.addView(r);Space sp=new Space(this);sp.setBackgroundColor(line);page.addView(sp,new LinearLayout.LayoutParams(-1,1));}
    }

    void showProfile(){base("Profile","@"+store.handle,false);
        LinearLayout hero=col();pad(hero,16,18,16,16);LinearLayout hr=row();TextView av=avatar(store.name.substring(0,1).toUpperCase(Locale.US));hr.addView(av,new LinearLayout.LayoutParams(dp(72),dp(72)));LinearLayout inf=col();TextView n=tv(store.name,22);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView h=tvm("@"+store.handle,13);add(inf,n,-1,34);add(inf,h,-1,22);hr.addView(inf,new LinearLayout.LayoutParams(0,72,1));Button edit=action("Edit");hr.addView(edit,new LinearLayout.LayoutParams(dp(70),dp(42)));hero.addView(hr);edit.setOnClickListener(v->editProfile());TextView bioT=tv(store.bio,15);bioT.setLineSpacing(0,1.05f);pad(bioT,0,14,0,12);hero.addView(bioT);TextView stats=tvm("128 following   •   2.4K followers   •   "+store.posts.size()+" posts",12);hero.addView(stats);page.addView(hero);
        TextView tabs=tv("Posts        Replies        Media        Likes",13);tabs.setTextColor(accent);tabs.setGravity(Gravity.CENTER);tabs.setBackgroundColor(card);page.addView(tabs,new LinearLayout.LayoutParams(-1,46));for(AppStore.Post p:store.posts)if(p.handle.equals(store.handle))page.addView(postCard(p));
        TextView sh=tv("Settings",18);sh.setTypeface(Typeface.DEFAULT,Typeface.BOLD);pad(sh,16,22,16,10);page.addView(sh);page.addView(settingsRow("Appearance","Theme, accent, typography",v->appearanceDialog()));page.addView(settingsRow("Privacy","Read receipts, who can message you",v->privacyDialog()));page.addView(settingsRow("Notifications","Messages and mentions",v->notificationDialog()));page.addView(settingsRow("Server",""+(store.serverUrl.isEmpty()?"Local-first":"Connected: "+store.serverUrl),v->serverDialog()));page.addView(settingsRow("About NEXA","Version 1.0.0",v->aboutDialog()));
    }

    View settingsRow(String title,String sub,View.OnClickListener click){LinearLayout r=row();r.setBackgroundColor(card);pad(r,16,13,16,13);LinearLayout mid=col();TextView t=tv(title,15);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView s=tvm(sub,12);add(mid,t,-1,24);add(mid,s,-1,22);r.addView(mid,new LinearLayout.LayoutParams(0,46,1));TextView go=tvm("›",28);r.addView(go,new LinearLayout.LayoutParams(dp(24),46));r.setOnClickListener(click);return r;}

    void editProfile(){
        LinearLayout c=col();pad(c,16,4,16,4);EditText n=field("Display name",store.name),h=field("Handle",store.handle),b=field("Bio",store.bio);b.setMinLines(3);add(c,n,-1,52);add(c,h,-1,52);add(c,b,-1,100);
        new AlertDialog.Builder(this).setTitle("Edit profile").setView(c).setPositiveButton("Save",(d,w)->{store.name=n.getText().toString().trim();store.handle=h.getText().toString().trim().replace("@","");store.bio=b.getText().toString().trim();if(store.name.isEmpty())store.name="NEXA User";if(store.handle.isEmpty())store.handle="nexauser";store.save();showProfile();}).setNegativeButton("Cancel",null).show();
    }
    EditText field(String hint,String val){EditText e=new EditText(this);e.setHint(hint);e.setText(val);e.setTextColor(fg);e.setHintTextColor(muted);e.setTextSize(15*store.textScale);e.setBackground(stroke(line,1,14));pad(e,12,0,12,0);return e;}

    void appearanceDialog(){
        LinearLayout c=col();pad(c,14,4,14,4);TextView t=tvm("Theme",12);c.addView(t);RadioGroup rg=new RadioGroup(this);String[] themes={"Dark","AMOLED black","Light"};String[] vals={"dark","amoled","light"};for(int i=0;i<3;i++){RadioButton r=new RadioButton(this);r.setText(themes[i]);r.setTextColor(fg);r.setTextSize(14);r.setTag(vals[i]);rg.addView(r);if(store.theme.equals(vals[i]))r.setChecked(true);}c.addView(rg);TextView a=tvm("Accent color",12);pad(a,0,12,0,6);c.addView(a);LinearLayout colorsRow=row();String[] cs={"#6C63FF","#FF3D81","#00C2FF","#00D68F","#FFB020","#FF5C35","#A855F7","#22C55E","#14B8A6","#FFFFFF"};for(String s:cs){TextView sw=tv("",1);sw.setBackground(bg(Color.parseColor(s),10));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,34,1);lp.setMargins(dp(3),0,dp(3),0);colorsRow.addView(sw,lp);sw.setOnClickListener(v->{store.accent=s;store.save();});}c.addView(colorsRow);TextView sc=tvm("Text size",12);pad(sc,0,12,0,4);c.addView(sc);SeekBar seek=new SeekBar(this);seek.setMax(5);seek.setProgress(Math.max(0,Math.min(5,(int)((store.textScale-0.9f)*10))));c.addView(seek);CheckBox compactBox=new CheckBox(this);compactBox.setText("Compact cards");compactBox.setTextColor(fg);compactBox.setChecked(store.compact);c.addView(compactBox);
        new AlertDialog.Builder(this).setTitle("Appearance").setView(c).setPositiveButton("Apply",(d,w)->{int i=rg.indexOfChild(rg.findViewById(rg.getCheckedRadioButtonId()));if(i<0)i=0;store.theme=vals[i];store.textScale=0.9f+seek.getProgress()*0.1f;store.compact=compactBox.isChecked();store.save();refreshShell();}).setNegativeButton("Cancel",null).show();
    }

    void privacyDialog(){
        LinearLayout c=col();CheckBox receipts=check("Show read receipts",store.showReadReceipts), messages=check("Allow messages from everyone",store.allowMessages);c.addView(receipts);c.addView(messages);new AlertDialog.Builder(this).setTitle("Privacy").setView(c).setPositiveButton("Save",(d,w)->{store.showReadReceipts=receipts.isChecked();store.allowMessages=messages.isChecked();store.save();toast("Privacy updated");}).show();
    }
    void notificationDialog(){LinearLayout c=col();CheckBox m=check("Message notifications",store.notifyMessages),n=check("Mentions and replies",store.notifyMentions);c.addView(m);c.addView(n);new AlertDialog.Builder(this).setTitle("Notifications").setView(c).setPositiveButton("Save",(d,w)->{store.notifyMessages=m.isChecked();store.notifyMentions=n.isChecked();store.save();toast("Notifications updated");}).show();}
    CheckBox check(String s,boolean b){CheckBox c=new CheckBox(this);c.setText(s);c.setTextColor(fg);c.setTextSize(14);c.setChecked(b);return c;}
    void serverDialog(){EditText e=field("https://your-server.example",store.serverUrl);new AlertDialog.Builder(this).setTitle("Server connection").setMessage("Optional. Leave empty to keep the app local-first.").setView(e).setPositiveButton("Save",(d,w)->{store.serverUrl=e.getText().toString().trim();store.save();showProfile();}).setNegativeButton("Cancel",null).show();}
    void aboutDialog(){new AlertDialog.Builder(this).setTitle("NEXA 1.0.0").setMessage("A social messenger built around two ideas: a public timeline and private conversation spaces.\n\nLocal data stays on this device until a server is configured.\n\nOpen source project in your Git repository.").setPositiveButton("OK",null).show();}

    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}

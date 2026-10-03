package ai.nexa.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private AppStore store;
    private FrameLayout root, body;
    private LinearLayout page, bottom;
    private String current = "home";
    private int fg, muted, card, line, surface, accent;
    private String incoming;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        store = new AppStore(this);
        incoming = getIntent() == null ? null : getIntent().getStringExtra(Intent.EXTRA_TEXT);
        buildShell();
        showHome();
        if (incoming != null && !incoming.trim().isEmpty()) new Handler().postDelayed(() -> compose(incoming), 250);
    }

    @Override protected void onNewIntent(Intent i) {
        super.onNewIntent(i); setIntent(i);
        String s=i==null?null:i.getStringExtra(Intent.EXTRA_TEXT);
        if(s!=null&&!s.trim().isEmpty()) compose(s);
    }

    private void theme() {
        try { accent=Color.parseColor(store.accent); } catch(Exception e) { accent=Color.rgb(108,99,255); }
        if ("light".equals(store.theme)) { fg=0xFF15171C; muted=0xFF6D717C; card=Color.WHITE; line=0xFFE0E2E7; surface=0xFFF4F5F8; }
        else if ("amoled".equals(store.theme)) { fg=Color.WHITE; muted=0xFF999CA5; card=Color.BLACK; line=0xFF303035; surface=0xFF050507; }
        else { fg=0xFFF4F5F8; muted=0xFF9BA0AC; card=0xFF171922; line=0xFF30333E; surface=0xFF0B0C11; }
    }
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable rounded(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private GradientDrawable outlined(){GradientDrawable g=rounded(Color.TRANSPARENT,14);g.setStroke(dp(1),line);return g;}
    private TextView text(String s,float size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size*store.textScale);t.setTextColor(fg);return t;}
    private TextView sub(String s,float size){TextView t=text(s,size);t.setTextColor(muted);return t;}
    private Button btn(String s,boolean primary){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(13*store.textScale);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(dp(9),0,dp(9),0);b.setTextColor(primary?Color.WHITE:fg);b.setBackground(rounded(primary?accent:card,13));if(!primary){GradientDrawable g=outlined();b.setBackground(g);}return b;}
    private LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    private LinearLayout col(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);return x;}
    private void pad(View v,int l,int t,int r,int b){v.setPadding(dp(l),dp(t),dp(r),dp(b));}

    private void buildShell(){
        theme(); root=new FrameLayout(this);root.setBackgroundColor(surface);setContentView(root);
        body=new FrameLayout(this);root.addView(body,new FrameLayout.LayoutParams(-1,-1));
        bottom=new LinearLayout(this);bottom.setOrientation(LinearLayout.HORIZONTAL);bottom.setGravity(Gravity.CENTER);bottom.setBackgroundColor(card);
        root.addView(bottom,new FrameLayout.LayoutParams(-1,dp(70),Gravity.BOTTOM));
        nav("⌂","Home",0);nav("✉","Chats",1);nav("⌕","Search",2);nav("♡","Alerts",3);nav("◉","Profile",4);
    }
    private void nav(String icon,String label,int n){
        LinearLayout x=col();x.setGravity(Gravity.CENTER);TextView a=text(icon,21);a.setGravity(Gravity.CENTER);TextView b=sub(label,10);b.setGravity(Gravity.CENTER);
        x.addView(a,new LinearLayout.LayoutParams(-1,dp(31)));x.addView(b,new LinearLayout.LayoutParams(-1,dp(24)));bottom.addView(x,new LinearLayout.LayoutParams(0,dp(70),1));
        x.setOnClickListener(v->{if(n==0)showHome();else if(n==1)showChats();else if(n==2)showSearch();else if(n==3)showAlerts();else showProfile();});
    }
    private void reset(String title,String desc,boolean back){
        body.removeAllViews();theme();
        LinearLayout shell=col();shell.setBackgroundColor(surface);body.addView(shell,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout bar=row();bar.setBackgroundColor(card);pad(bar,10,8,10,8);
        if(back){TextView bk=text("‹",32);bk.setGravity(Gravity.CENTER);bar.addView(bk,new LinearLayout.LayoutParams(dp(42),dp(54)));bk.setOnClickListener(v->showChats());}
        LinearLayout tt=col();TextView h=text(title,21);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(tt,h,-1,30);if(desc!=null)add(tt,sub(desc,11),-1,22);
        bar.addView(tt,new LinearLayout.LayoutParams(0,dp(54),1));shell.addView(bar,new LinearLayout.LayoutParams(-1,dp(72)));
        ScrollView sc=new ScrollView(this);page=col();sc.addView(page,new ScrollView.LayoutParams(-1,-2));shell.addView(sc,new LinearLayout.LayoutParams(-1,0,1));current=title.toLowerCase(Locale.US);
    }
    private void add(LinearLayout p,View v,int w,int h){p.addView(v,new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h)));}

    private void showHome(){
        reset("NEXA","Public timeline • people you follow",false);
        LinearLayout head=row();pad(head,14,10,14,8);TextView q=text("The world in small messages.",16);q.setTypeface(Typeface.DEFAULT,Typeface.BOLD);head.addView(q,new LinearLayout.LayoutParams(0,42,1));Button post=btn("Post",true);head.addView(post,new LinearLayout.LayoutParams(dp(84),dp(42)));post.setOnClickListener(v->compose(""));page.addView(head);
        for(AppStore.Post p:store.posts)page.addView(postView(p));
    }
    private TextView avatar(String s,int size){TextView a=text(s.substring(0,1).toUpperCase(Locale.US),size);a.setTextColor(Color.WHITE);a.setTypeface(Typeface.DEFAULT,Typeface.BOLD);a.setGravity(Gravity.CENTER);a.setBackground(rounded(accent,50));return a;}
    private View postView(AppStore.Post p){
        LinearLayout box=col();box.setBackgroundColor(card);pad(box,15,13,15,12);
        LinearLayout h=row();h.addView(avatar(p.name,17),new LinearLayout.LayoutParams(dp(46),dp(46)));
        LinearLayout who=col();TextView n=text(p.name+"  @"+p.handle,15);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(who,n,-1,25);add(who,sub(p.time,11),-1,20);h.addView(who,new LinearLayout.LayoutParams(0,46,1));box.addView(h);
        TextView bodyText=text(p.text,16);bodyText.setLineSpacing(0,1.06f);pad(bodyText,0,11,0,9);box.addView(bodyText);
        LinearLayout acts=row();Button r=btn("Reply "+p.replies,false),rp=btn("↻ "+p.reposts,false),lk=btn((p.liked?"♥ ":"♡ ")+p.likes,false),sv=btn("Share",false);
        acts.addView(r,new LinearLayout.LayoutParams(0,40,1));acts.addView(rp,new LinearLayout.LayoutParams(0,40,1));acts.addView(lk,new LinearLayout.LayoutParams(0,40,1));acts.addView(sv,new LinearLayout.LayoutParams(0,40,1));box.addView(acts);
        r.setOnClickListener(v->compose("↳ @"+p.handle+" "));lk.setOnClickListener(v->{p.liked=!p.liked;p.likes=String.valueOf(Math.max(0,Integer.parseInt(p.likes)+(p.liked?1:-1)));store.save();showHome();});rp.setOnClickListener(v->{p.reposts=String.valueOf(Integer.parseInt(p.reposts)+1);store.save();showHome();toast("Reposted");});sv.setOnClickListener(v->sharePost(p));
        return box;
    }
    private void compose(String seed){
        theme();LinearLayout c=col();pad(c,14,6,14,6);EditText e=input("Write a post…",seed);e.setMinLines(6);c.addView(e,new LinearLayout.LayoutParams(-1,dp(160)));
        TextView count=sub("300 characters",11);c.addView(count,new LinearLayout.LayoutParams(-1,dp(30)));
        e.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int d){}public void onTextChanged(CharSequence s,int a,int b,int d){count.setText((300-s.length())+" characters");}public void afterTextChanged(android.text.Editable x){}});
        LinearLayout row=row();row.addView(sub("Mentions • replies • links",11),new LinearLayout.LayoutParams(0,44,1));Button send=btn("Publish",true);row.addView(send,new LinearLayout.LayoutParams(dp(108),dp(44)));c.addView(row);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("New post").setView(c).setNegativeButton("Cancel",null).create();send.setOnClickListener(v->{String s=e.getText().toString().trim();if(s.isEmpty())return;if(s.length()>300){toast("Maximum is 300 characters");return;}store.posts.add(0,new AppStore.Post("p"+System.currentTimeMillis(),store.name,store.handle,s,"now","0","0","0",false));store.save();d.dismiss();showHome();});d.show();
    }
    private EditText input(String hint,String val){EditText e=new EditText(this);e.setHint(hint);e.setText(val);e.setTextColor(fg);e.setHintTextColor(muted);e.setTextSize(16*store.textScale);e.setBackground(outlined());pad(e,12,10,12,10);e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);return e;}

    private void showChats(){
        reset("Chats","Private conversations • groups • channels",false);
        LinearLayout bar=row();pad(bar,14,10,14,6);Button n=btn("New chat",true),g=btn("New group",false);bar.addView(n,new LinearLayout.LayoutParams(0,42,1));bar.addView(g,new LinearLayout.LayoutParams(dp(120),42));n.setOnClickListener(v->newChat());g.setOnClickListener(v->newChat());page.addView(bar);
        label("MESSAGES");for(AppStore.Chat c:store.chats)page.addView(chatItem(c));label("CHANNELS");for(AppStore.Channel c:store.channels)page.addView(channelItem(c));
    }
    private void label(String s){TextView t=sub(s,10);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);pad(t,15,16,15,7);page.addView(t);}
    private View chatItem(AppStore.Chat c){
        LinearLayout r=row();r.setBackgroundColor(card);pad(r,14,10,14,10);r.addView(avatar(c.title,17),new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout m=col();TextView n=text(c.title,15);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(m,n,-1,24);add(m,sub(c.preview,12),-1,23);r.addView(m,new LinearLayout.LayoutParams(0,48,1));
        LinearLayout rr=col();rr.setGravity(Gravity.RIGHT);add(rr,sub(c.time,10),-1,21);if(!c.badge.isEmpty()){TextView b=text(c.badge,9);b.setGravity(Gravity.CENTER);b.setTextColor(Color.WHITE);b.setBackground(rounded(accent,12));add(rr,b,28,20);}r.addView(rr,new LinearLayout.LayoutParams(38,48));r.setOnClickListener(v->openChat(c));return r;
    }
    private View channelItem(AppStore.Channel c){
        LinearLayout r=row();pad(r,14,9,14,9);r.addView(avatar(c.initial,16),new LinearLayout.LayoutParams(dp(46),dp(46)));LinearLayout m=col();TextView n=text(c.name+"  ·  "+c.members,15);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(m,n,-1,24);add(m,sub(c.description,12),-1,22);r.addView(m,new LinearLayout.LayoutParams(0,46,1));Button j=btn(c.joined?"Joined":"Join",!c.joined);r.addView(j,new LinearLayout.LayoutParams(dp(80),40));j.setOnClickListener(v->{c.joined=!c.joined;store.save();showChats();});return r;
    }
    private void openChat(AppStore.Chat c){
        body.removeAllViews();theme();current="chats";LinearLayout shell=col();shell.setBackgroundColor(surface);body.addView(shell,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout bar=row();bar.setBackgroundColor(card);pad(bar,8,7,8,7);TextView bk=text("‹",32);bk.setGravity(Gravity.CENTER);bar.addView(bk,new LinearLayout.LayoutParams(42,54));bk.setOnClickListener(v->showChats());LinearLayout title=col();TextView n=text(c.title,18);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(title,n,-1,28);add(title,sub(c.online?"online":"last active today",11),-1,21);bar.addView(title,new LinearLayout.LayoutParams(0,54,1));bar.addView(btn("⋯",false),new LinearLayout.LayoutParams(48,40));shell.addView(bar,new LinearLayout.LayoutParams(-1,70));
        LinearLayout messages=col();ScrollView sc=new ScrollView(this);sc.addView(messages,new ScrollView.LayoutParams(-1,-2));shell.addView(sc,new LinearLayout.LayoutParams(-1,0,1));for(AppStore.Message m:c.messages)messages.addView(bubble(m));
        LinearLayout composer=row();composer.setBackgroundColor(card);pad(composer,9,9,9,9);EditText e=input("Message…","");e.setSingleLine(true);composer.addView(e,new LinearLayout.LayoutParams(0,48,1));Button send=btn("Send",true);composer.addView(send,new LinearLayout.LayoutParams(82,44));send.setOnClickListener(v->{String s=e.getText().toString().trim();if(s.isEmpty())return;AppStore.Message m=new AppStore.Message("m"+System.currentTimeMillis(),store.name,s,"now",true);c.messages.add(m);c.preview=s;c.time="now";store.save();messages.addView(bubble(m));e.setText("");sc.postDelayed(()->sc.fullScroll(View.FOCUS_DOWN),80);});shell.addView(composer,new LinearLayout.LayoutParams(-1,70));sc.postDelayed(()->sc.fullScroll(View.FOCUS_DOWN),80);
    }
    private View bubble(AppStore.Message m){
        LinearLayout wrap=row();wrap.setGravity(m.me?Gravity.RIGHT:Gravity.LEFT);pad(wrap,13,5,13,5);LinearLayout b=col();b.setPadding(dp(12),dp(8),dp(12),dp(7));b.setBackground(rounded(m.me?accent:card,17));TextView t=text(m.text,15),tm=sub(m.me?"You":m.sender,10);b.addView(t);b.addView(tm);wrap.addView(b,new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*.78),-2));wrap.setOnLongClickListener(v->{react(m);return true;});return wrap;
    }
    private void sharePost(AppStore.Post p){ Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,p.text+"\\n\\n@"+p.handle+" via NEXA");startActivity(Intent.createChooser(i,"Share post")); }\n    private void react(AppStore.Message m){String[] a={"♡","👍","😂","🔥","✅","Remove"};new AlertDialog.Builder(this).setTitle("Reaction").setItems(a,(d,w)->{m.reaction=w==5?"":a[w];store.save();toast(m.reaction.isEmpty()?"Reaction removed":"Reaction added");}).show();}
    private void newChat(){EditText e=input("Name or @handle","");new AlertDialog.Builder(this).setTitle("New conversation").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{String s=e.getText().toString().trim();if(s.isEmpty())return;AppStore.Chat c=new AppStore.Chat("c"+System.currentTimeMillis(),s,s.toLowerCase(Locale.US).replace(" ",""),"New conversation","now","",true);store.chats.add(0,c);store.save();openChat(c);}).show();}

    private void showSearch(){
        reset("Search","People • posts • chats • channels",false);EditText q=input("Search NEXA","");q.setSingleLine(true);page.addView(q,new LinearLayout.LayoutParams(-1,52));LinearLayout results=col();page.addView(results);
        q.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int d){}public void onTextChanged(CharSequence s,int a,int b,int d){search(s.toString(),results);}public void afterTextChanged(android.text.Editable e){}});search("",results);
    }
    private void search(String query,LinearLayout out){
        out.removeAllViews();String q=query.trim().toLowerCase(Locale.US);int hits=0;TextView h=sub(q.isEmpty()?"Start typing to search the app.":"Results for “"+query+"”",11);pad(h,15,14,15,8);out.addView(h);
        for(AppStore.Post p:store.posts)if(q.isEmpty()||p.text.toLowerCase(Locale.US).contains(q)||p.name.toLowerCase(Locale.US).contains(q)||p.handle.toLowerCase(Locale.US).contains(q)){out.addView(postView(p));hits++;}
        for(AppStore.Chat c:store.chats)if(!q.isEmpty()&&(c.title.toLowerCase(Locale.US).contains(q)||c.preview.toLowerCase(Locale.US).contains(q))){TextView x=text("✉  "+c.title+" — "+c.preview,14);x.setBackgroundColor(card);pad(x,15,13,15,13);out.addView(x);hits++;}
        for(AppStore.Channel c:store.channels)if(!q.isEmpty()&&(c.name.toLowerCase(Locale.US).contains(q)||c.handle.toLowerCase(Locale.US).contains(q))){out.addView(channelItem(c));hits++;}
        if(hits==0&&!q.isEmpty()){TextView z=sub("Nothing found.",14);pad(z,15,25,15,25);out.addView(z);}
    }

    private void showAlerts(){
        reset("Alerts","Mentions • likes • message activity",false);Button all=btn("Mark all read",true);pad(all,14,10,14,4);page.addView(all,new LinearLayout.LayoutParams(-1,42));all.setOnClickListener(v->{for(AppStore.Notice n:store.notices)n.unread=false;store.save();showAlerts();});
        for(AppStore.Notice n:store.notices){LinearLayout r=row();pad(r,15,12,15,12);TextView i=text(n.icon,21);i.setGravity(Gravity.CENTER);r.addView(i,new LinearLayout.LayoutParams(42,42));LinearLayout m=col();add(m,text(n.text,14),-1,24);add(m,sub(n.time,11),-1,18);r.addView(m,new LinearLayout.LayoutParams(0,42,1));if(n.unread){TextView dot=text("●",9);dot.setTextColor(accent);r.addView(dot,new LinearLayout.LayoutParams(20,42));}page.addView(r);}}
    
    private void showProfile(){
        reset("Profile","@"+store.handle,false);LinearLayout hero=col();pad(hero,16,17,16,16);LinearLayout h=row();h.addView(avatar(store.name,23),new LinearLayout.LayoutParams(72,72));LinearLayout m=col();TextView n=text(store.name,22);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(m,n,-1,34);add(m,sub("@"+store.handle,12),-1,22);h.addView(m,new LinearLayout.LayoutParams(0,72,1));Button e=btn("Edit",false);h.addView(e,new LinearLayout.LayoutParams(72,42));e.setOnClickListener(v->editProfile());hero.addView(h);TextView bio=text(store.bio,15);bio.setLineSpacing(0,1.05f);pad(bio,0,14,0,11);hero.addView(bio);hero.addView(sub("128 following   •   2.4K followers   •   "+store.posts.size()+" posts",11));page.addView(hero);
        page.addView(settings("Appearance","Theme, color, typography",v->appearance()));page.addView(settings("Privacy","Read receipts and message permissions",v->privacy()));page.addView(settings("Notifications","Messages, replies and mentions",v->notifications()));page.addView(settings("Server",""+(store.serverUrl.isEmpty()?"Local-first mode":"Connected"),v->server()));page.addView(settings("About NEXA","Version 1.0.0",v->about()));
        TextView p=text("Your posts",18);p.setTypeface(Typeface.DEFAULT,Typeface.BOLD);pad(p,16,20,16,8);page.addView(p);for(AppStore.Post x:store.posts)if(store.handle.equals(x.handle))page.addView(postView(x));
    }
    private View settings(String a,String b,View.OnClickListener c){LinearLayout r=row();r.setBackgroundColor(card);pad(r,15,12,15,12);LinearLayout m=col();TextView t=text(a,15);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(m,t,-1,24);add(m,sub(b,11),-1,21);r.addView(m,new LinearLayout.LayoutParams(0,45,1));r.addView(sub("›",28),new LinearLayout.LayoutParams(24,45));r.setOnClickListener(c);return r;}

    private void editProfile(){LinearLayout c=col();pad(c,14,3,14,3);EditText n=input("Display name",store.name),h=input("Handle",store.handle),b=input("Bio",store.bio);b.setMinLines(3);c.addView(n,new LinearLayout.LayoutParams(-1,52));c.addView(h,new LinearLayout.LayoutParams(-1,52));c.addView(b,new LinearLayout.LayoutParams(-1,100));new AlertDialog.Builder(this).setTitle("Edit profile").setView(c).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{store.name=n.getText().toString().trim();store.handle=h.getText().toString().trim().replace("@","");store.bio=b.getText().toString().trim();if(store.name.isEmpty())store.name="NEXA User";if(store.handle.isEmpty())store.handle="nexa";store.save();showProfile();}).show();}
    private void appearance(){LinearLayout c=col();pad(c,12,0,12,0);RadioGroup rg=new RadioGroup(this);String[] names={"Dark","AMOLED black","Light"},vals={"dark","amoled","light"};for(int i=0;i<3;i++){RadioButton r=new RadioButton(this);r.setText(names[i]);r.setTextColor(fg);r.setTag(vals[i]);rg.addView(r);if(vals[i].equals(store.theme))r.setChecked(true);}c.addView(rg);TextView a=sub("Accent color",12);pad(a,0,10,0,5);c.addView(a);LinearLayout colors=row();String[] cs={"#6C63FF","#FF3D81","#00C2FF","#00D68F","#FFB020","#FF5C35","#A855F7","#22C55E","#14B8A6","#FFFFFF"};for(String s:cs){TextView sw=text("",1);sw.setBackground(rounded(Color.parseColor(s),9));colors.addView(sw,new LinearLayout.LayoutParams(0,34,1));sw.setOnClickListener(v->store.accent=s);}c.addView(colors);CheckBox compact=new CheckBox(this);compact.setText("Compact timeline");compact.setTextColor(fg);compact.setChecked(store.compact);c.addView(compact);CheckBox motion=new CheckBox(this);motion.setText("Reduce motion");motion.setTextColor(fg);motion.setChecked(store.reduceMotion);c.addView(motion);SeekBar scale=new SeekBar(this);scale.setMax(4);scale.setProgress(Math.max(0,Math.min(4,(int)((store.textScale-.9f)*10))));c.addView(scale);new AlertDialog.Builder(this).setTitle("Appearance").setView(c).setNegativeButton("Cancel",null).setPositiveButton("Apply",(d,w)->{int x=rg.getCheckedRadioButtonId();RadioButton rr=rg.findViewById(x);if(rr!=null)store.theme=String.valueOf(rr.getTag());store.textScale=.9f+scale.getProgress()*.1f;store.compact=compact.isChecked();store.reduceMotion=motion.isChecked();store.save();showProfile();}).show();}
    private void privacy(){CheckBox a=check("Show read receipts",store.showReadReceipts),b=check("Allow messages from everyone",store.allowMessages);LinearLayout c=col();c.addView(a);c.addView(b);new AlertDialog.Builder(this).setTitle("Privacy").setView(c).setPositiveButton("Save",(d,w)->{store.showReadReceipts=a.isChecked();store.allowMessages=b.isChecked();store.save();}).show();}
    private void notifications(){CheckBox a=check("Message notifications",store.notifyMessages),b=check("Mentions & replies",store.notifyMentions);LinearLayout c=col();c.addView(a);c.addView(b);new AlertDialog.Builder(this).setTitle("Notifications").setView(c).setPositiveButton("Save",(d,w)->{store.notifyMessages=a.isChecked();store.notifyMentions=b.isChecked();store.save();}).show();}
    private CheckBox check(String s,boolean v){CheckBox c=new CheckBox(this);c.setText(s);c.setTextColor(fg);c.setTextSize(14);c.setChecked(v);return c;}
    private void server(){EditText e=input("https://server.example",store.serverUrl);new AlertDialog.Builder(this).setTitle("Server").setMessage("Empty means local-first mode.\nThe reference backend lives in /backend.").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{store.serverUrl=e.getText().toString().trim();store.save();showProfile();}).show();}
    private void about(){new AlertDialog.Builder(this).setTitle("NEXA 1.0.0").setMessage("A social messenger that combines a public timeline with private conversations, groups and channels.\n\nThe Android client works offline-first and stores its demo data locally. The repository also contains a REST/WebSocket reference backend.").setPositiveButton("OK",null).show();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}

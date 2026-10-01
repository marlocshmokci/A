package ai.ayurones.messenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileActivity extends BaseActivity {
    private FrameLayout avatarBox;
    private ImageView avatar;
    private TextView ornament, name, handle, bio;
    private static final int PICK=91;

    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}

    private void build(){
        LinearLayout root=root();
        LinearLayout top=row();
        TextView back=Ui.text(this,"‹",34);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());
        top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));
        TextView t=Ui.text(this,"Профиль",22);t.setTypeface(null,android.graphics.Typeface.BOLD);
        top.addView(t,new LinearLayout.LayoutParams(0,Ui.dp(this,64),1));
        TextView edit=Ui.text(this,"Изменить",13);edit.setGravity(Gravity.CENTER);edit.setOnClickListener(v->editProfile());
        top.addView(edit,new LinearLayout.LayoutParams(Ui.dp(this,82),Ui.dp(this,64)));
        root.addView(top);root.addView(Ui.divider(this));

        LinearLayout center=new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);
        center.setPadding(0,Ui.dp(this,18),0,Ui.dp(this,10));

        avatarBox=new FrameLayout(this);
        avatarBox.setBackground(Ui.bg(Color.rgb(28,28,28),Color.rgb(80,80,80),Ui.dp(this,90)));
        avatar=new ImageView(this);avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        refreshAvatar();
        avatarBox.addView(avatar,new FrameLayout.LayoutParams(Ui.dp(this,150),Ui.dp(this,150)));
        ornament=Ui.text(this,"",38);ornament.setGravity(Gravity.CENTER);ornament.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(Ui.dp(this,70),Ui.dp(this,70),Gravity.TOP|Gravity.END);
        op.topMargin=-Ui.dp(this,10);op.rightMargin=-Ui.dp(this,8);
        avatarBox.addView(ornament,op);
        center.addView(avatarBox,new LinearLayout.LayoutParams(Ui.dp(this,150),Ui.dp(this,150)));

        name=Ui.text(this,Store.avatarName(this),22);name.setGravity(Gravity.CENTER);name.setTypeface(null,android.graphics.Typeface.BOLD);
        center.addView(name,new LinearLayout.LayoutParams(-1,Ui.dp(this,44)));
        handle=Ui.text(this,"@"+Store.username(this),14);handle.setGravity(Gravity.CENTER);handle.setTextColor(Color.LTGRAY);
        center.addView(handle,new LinearLayout.LayoutParams(-1,Ui.dp(this,30)));
        bio=Ui.text(this,Store.bio(this).isEmpty()?"Расскажите о себе":Store.bio(this),13);bio.setGravity(Gravity.CENTER);bio.setTextColor(Color.GRAY);
        center.addView(bio,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));

        TextView score=Ui.text(this,"Баллы: "+(Store.unlimited(this)?"∞":Store.points(this)),16);
        score.setGravity(Gravity.CENTER);score.setTextColor(Color.LTGRAY);
        center.addView(score,new LinearLayout.LayoutParams(-1,Ui.dp(this,38)));
        root.addView(center,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout actions=new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setPadding(Ui.dp(this,16),Ui.dp(this,8),Ui.dp(this,16),Ui.dp(this,16));

        TextView photo=Ui.text(this,"Фото профиля",16);
        photo.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(45,45,45),Ui.dp(this,16)));photo.setOnClickListener(v->pickPhoto());
        TextView dec=Ui.text(this,"Украшения",16);
        dec.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(45,45,45),Ui.dp(this,16)));dec.setOnClickListener(v->startActivity(new Intent(this,DecorationsActivity.class)));
        TextView points=Ui.text(this,"Активность и баллы",16);
        points.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(45,45,45),Ui.dp(this,16)));points.setOnClickListener(v->daily());
        actions.addView(photo,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));
        LinearLayout.LayoutParams lp1=new LinearLayout.LayoutParams(-1,Ui.dp(this,52));lp1.topMargin=Ui.dp(this,8);actions.addView(dec,lp1);
        LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(-1,Ui.dp(this,52));lp2.topMargin=Ui.dp(this,8);actions.addView(points,lp2);
        root.addView(actions);
        applyDecoration();
    }

    @Override protected void onResume(){super.onResume();if(avatarBox!=null){refreshAvatar();refreshText();applyDecoration();}}

    private void refreshAvatar(){
        Uri u=Store.avatar(this);
        if(u==null) avatar.setImageResource(android.R.drawable.sym_def_app_icon); else avatar.setImageURI(u);
    }

    private void refreshText(){
        name.setText(Store.avatarName(this));
        handle.setText("@"+Store.username(this));
        bio.setText(Store.bio(this).isEmpty()?"Расскажите о себе":Store.bio(this));
    }

    private void pickPhoto(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,PICK);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK&&resultCode==Activity.RESULT_OK&&data!=null&&data.getData()!=null){
            Store.setAvatar(this,data.getData());
            refreshAvatar();Store.addPoints(this,10);
        }
    }

    private void editProfile(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);
        EditText n=field("Имя",Store.avatarName(this));
        EditText u=field("Username",Store.username(this));
        EditText b=field("О себе",Store.bio(this));
        box.addView(n);box.addView(u);box.addView(b);

        new AlertDialog.Builder(this).setTitle("Профиль").setView(box)
            .setNegativeButton("Отмена",null)
            .setPositiveButton("Сохранить",(d,w)->{
                String user=u.getText().toString().trim();
                if(user.startsWith("@"))user=user.substring(1);
                Store.setProfile(this,n.getText().toString(),user,b.getText().toString());
                refreshText();
            }).show();
    }

    private EditText field(String hint,String value){
        EditText e=new EditText(this);e.setText(value);e.setHint(hint);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY);e.setSingleLine(false);
        e.setPadding(Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8));return e;
    }

    private void applyDecoration(){
        String d=Store.equipped(this);String icon="";
        switch(d){case "sparkle":icon="✦";break;case "crown":icon="♛";break;case "halo":icon="◉";break;case "frame":icon="◇";break;case "cat":icon="🐈";break;}
        ornament.setText(icon);
    }

    private void daily(){
        Store.dailyBonus(this);
        new AlertDialog.Builder(this).setTitle("Активность")
            .setMessage("Сообщение  +5\nНовое фото  +10\nЕжедневный вход  +20\n\nСейчас: "+(Store.unlimited(this)?"∞":Store.points(this)))
            .setPositiveButton("Понятно",null).show();
    }
}

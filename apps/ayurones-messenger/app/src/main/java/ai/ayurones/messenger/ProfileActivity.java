package ai.ayurones.messenger;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProfileActivity extends BaseActivity {
    private FrameLayout avatarBox;
    private ImageView avatar;
    private TextView ornament;
    private static final int PICK=91;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LinearLayout root=root();
        LinearLayout top=row();
        TextView back=Ui.text(this,"‹",34);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());
        top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));
        TextView t=Ui.text(this,"Профиль",22);t.setTypeface(null,android.graphics.Typeface.BOLD);
        top.addView(t,new LinearLayout.LayoutParams(0,Ui.dp(this,64),1));
        root.addView(top);root.addView(Ui.divider(this));

        LinearLayout center=new LinearLayout(this);center.setOrientation(LinearLayout.VERTICAL);center.setGravity(Gravity.CENTER_HORIZONTAL);
        avatarBox=new FrameLayout(this);
        GradientDrawable circle=Ui.bg(Color.rgb(28,28,28),Color.rgb(80,80,80),Ui.dp(this,90));
        avatarBox.setBackground(circle);
        avatar=new ImageView(this);avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setImageURI(Store.avatar(this));
        if(Store.avatar(this)==null) avatar.setImageResource(android.R.drawable.sym_def_app_icon);
        avatarBox.addView(avatar,new FrameLayout.LayoutParams(Ui.dp(this,150),Ui.dp(this,150)));
        ornament=Ui.text(this,"",38);ornament.setGravity(Gravity.CENTER);ornament.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(Ui.dp(this,70),Ui.dp(this,70),Gravity.TOP|Gravity.END);op.topMargin=-Ui.dp(this,10);op.rightMargin=-Ui.dp(this,8);
        avatarBox.addView(ornament,op);
        center.addView(avatarBox,new LinearLayout.LayoutParams(Ui.dp(this,150),Ui.dp(this,150)));
        TextView name=Ui.text(this,"Ayurones User",22);name.setGravity(Gravity.CENTER);name.setTypeface(null,android.graphics.Typeface.BOLD);
        center.addView(name);
        TextView score=Ui.text(this,"Баллы: "+Store.points(this),16);score.setGravity(Gravity.CENTER);score.setTextColor(Color.LTGRAY);
        center.addView(score);
        root.addView(center,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.VERTICAL);actions.setPadding(Ui.dp(this,16),Ui.dp(this,8),Ui.dp(this,16),Ui.dp(this,16));
        TextView photo=Ui.text(this,"Фото профиля",16);photo.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(45,45,45),Ui.dp(this,16)));photo.setOnClickListener(v->pickPhoto());
        TextView dec=Ui.text(this,"Украшения",16);dec.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(45,45,45),Ui.dp(this,16)));dec.setOnClickListener(v->startActivity(new Intent(this,DecorationsActivity.class)));
        TextView points=Ui.text(this,"Активность и баллы",16);points.setBackground(Ui.bg(Color.rgb(18,18,18),Color.rgb(45,45,45),Ui.dp(this,16)));points.setOnClickListener(v->daily());
        actions.addView(photo);actions.addView(dec,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));actions.addView(points,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));
        root.addView(actions);
        applyDecoration();
    }

    @Override protected void onResume(){super.onResume();if(avatarBox!=null){avatar.setImageURI(Store.avatar(this));applyDecoration();}}
    private void pickPhoto(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("image/*");startActivityForResult(i,PICK);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK&&resultCode==Activity.RESULT_OK&&data!=null&&data.getData()!=null){
            Store.setAvatar(this,data.getData());avatar.setImageURI(data.getData());Store.addPoints(this,10);
        }
    }
    private void applyDecoration(){
        String d=Store.equipped(this);
        String icon="";
        switch(d){case "sparkle":icon="✦";break;case "crown":icon="♛";break;case "halo":icon="◉";break;case "frame":icon="◇";break;case "cat":icon="🐈";break;}
        ornament.setText(icon);
    }
    private void daily(){
        Store.dailyBonus(this);
        new android.app.AlertDialog.Builder(this).setTitle("Активность")
            .setMessage("Баллы используются для украшений.\n\n+5 за отправку сообщения\n+10 за обновление фото\n+20 за ежедневный вход\n\nСейчас: "+Store.points(this))
            .setPositiveButton("Понятно",null).show();
    }
}

package ai.ayurones.messenger;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DecorationsActivity extends BaseActivity {
    static final Store.Decoration[] ITEMS = {
        new Store.Decoration("sparkle","Искры","✦",80,"Маленький светящийся акцент"),
        new Store.Decoration("crown","Корона","♛",180,"Строгая корона поверх аватара"),
        new Store.Decoration("halo","Ореол","◉",320,"Круглый ореол над фотографией"),
        new Store.Decoration("frame","Рамка","◇",450,"Геометрическая рамка профиля"),
        new Store.Decoration("cat","Кот","🐈",600,"Кот на краю фотографии")
    };
    private LinearLayout list;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LinearLayout root=root();
        TextView top=Ui.text(this,"‹   Украшения",22);top.setTypeface(null,android.graphics.Typeface.BOLD);top.setOnClickListener(v->finish());
        root.addView(top,new LinearLayout.LayoutParams(-1,Ui.dp(this,64)));root.addView(Ui.divider(this));
        TextView balance=Ui.text(this,"Баллы: "+(Store.unlimited(this)?"∞":Store.points(this)),15);balance.setTextColor(Color.LTGRAY);root.addView(balance);
        ScrollView sv=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);list.setPadding(Ui.dp(this,14),Ui.dp(this,8),Ui.dp(this,14),Ui.dp(this,20));sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        render();
    }
    private void render(){
        list.removeAllViews();
        for(Store.Decoration d:ITEMS){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(android.view.Gravity.CENTER_VERTICAL);row.setPadding(Ui.dp(this,12),Ui.dp(this,8),Ui.dp(this,8),Ui.dp(this,8));
            row.setBackground(Ui.bg(Color.rgb(16,16,16),Color.rgb(42,42,42),Ui.dp(this,18)));
            TextView icon=Ui.text(this,d.icon,30);icon.setGravity(android.view.Gravity.CENTER);row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,58)));
            LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);
            TextView title=Ui.text(this,d.title,17);title.setTypeface(null,android.graphics.Typeface.BOLD);
            TextView detail=Ui.text(this,d.detail+" · "+d.cost+" баллов",12);detail.setTextColor(Color.LTGRAY);
            info.addView(title);info.addView(detail);row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            TextView action;
            if(Store.owned(this,d.id)){
                action=Ui.text(this,Store.equipped(this).equals(d.id)?"Надето":"Надеть",14);action.setGravity(android.view.Gravity.CENTER);
                action.setBackground(Ui.bg(Store.equipped(this).equals(d.id)?Color.WHITE:Color.rgb(35,35,35),Color.rgb(90,90,90),Ui.dp(this,14)));
                if(Store.equipped(this).equals(d.id)) action.setTextColor(Color.BLACK);
                action.setOnClickListener(v->{Store.equip(this,d.id);render();});
            }else{
                action=Ui.text(this,(Store.unlimited(this)?"∞":"Купить"),14);action.setGravity(android.view.Gravity.CENTER);
                action.setBackground(Ui.bg(Color.rgb(28,28,28),Color.rgb(70,70,70),Ui.dp(this,14)));
                action.setOnClickListener(v->buy(d));
            }
            row.addView(action,new LinearLayout.LayoutParams(Ui.dp(this,88),Ui.dp(this,44)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,78));lp.bottomMargin=Ui.dp(this,8);list.addView(row,lp);
        }
        TextView note=Ui.text(this,"Украшения меняются поверх фото профиля. Баллы начисляются за активность.",13);note.setTextColor(Color.GRAY);list.addView(note);
    }
    private void buy(Store.Decoration d){
        if(!Store.unlimited(this)&&Store.points(this)<d.cost){
            new AlertDialog.Builder(this).setTitle("Нужно больше баллов").setMessage("Не хватает "+(d.cost-Store.points(this))+" баллов. Общайся, открывай приложение каждый день и меняй фото профиля.").setPositiveButton("Понятно",null).show();return;
        }
        if(!Store.unlimited(this)) Store.addPoints(this,-d.cost);
        Store.buy(this,d.id);Store.equip(this,d.id);render();
    }
}

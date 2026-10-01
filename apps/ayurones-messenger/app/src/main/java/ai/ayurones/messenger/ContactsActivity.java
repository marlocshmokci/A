package ai.ayurones.messenger;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactsActivity extends BaseActivity {
    private LinearLayout list;
    @Override protected void onCreate(Bundle b){super.onCreate(b);build();}
    private void build(){
        LinearLayout root=root();
        LinearLayout top=row();
        TextView back=Ui.text(this,"‹",34); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->finish());
        top.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));
        TextView title=Ui.text(this,"Контакты",22); title.setTypeface(null,android.graphics.Typeface.BOLD);
        top.addView(title,new LinearLayout.LayoutParams(0,Ui.dp(this,64),1));
        TextView add=Ui.text(this,"＋",26); add.setGravity(Gravity.CENTER); add.setOnClickListener(v->add());
        top.addView(add,new LinearLayout.LayoutParams(Ui.dp(this,52),Ui.dp(this,64)));
        root.addView(top);root.addView(Ui.divider(this));
        ScrollView sv=new ScrollView(this);
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));
        sv.addView(list);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        render();
    }
    private void render(){
        list.removeAllViews();
        List<String> all=new ArrayList<>(Store.contacts(this));Collections.sort(all,String.CASE_INSENSITIVE_ORDER);
        if(all.isEmpty()){
            TextView v=Ui.text(this,"Контактов пока нет. Нажми ＋, чтобы создать чат.",16);v.setTextColor(Color.LTGRAY);list.addView(v);
        }
        for(String n:all){
            TextView v=Ui.text(this,"●   "+n+"\n     @"+n.replace(" ","_").toLowerCase(),16);
            v.setBackground(Ui.bg(Color.rgb(15,15,15),Color.rgb(35,35,35),Ui.dp(this,16)));
            v.setPadding(Ui.dp(this,16),Ui.dp(this,12),Ui.dp(this,16),Ui.dp(this,12));
            v.setOnClickListener(x->startActivity(ChatActivity.intent(this,n)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,72));lp.bottomMargin=Ui.dp(this,8);list.addView(v,lp);
        }
    }
    private void add(){
        EditText e=new EditText(this);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY);e.setHint("Имя контакта");e.setSingleLine(true);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Новый контакт").setView(e)
            .setNegativeButton("Отмена",null).setPositiveButton("Создать",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String n=e.getText().toString().trim();if(n.isEmpty())return;
            Store.addContact(this,n);d.dismiss();render();
        }));d.show();
    }
}

package ai.ayurones.drops;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Color.WHITE); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); setContentView(new GameView(this));}

  static class GameView extends View {
    Paint p=new Paint(3); Random r=new Random(); ArrayList<Drop> drops=new ArrayList<>(); SharedPreferences sp;
    float bowlX,bowlW,bowlY; int score; long last=0, spawn=0; boolean gameOver=false;
    GameView(Context c){super(c); p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); sp=c.getSharedPreferences("score",0); score=sp.getInt("best",0); setBackgroundColor(Color.WHITE);}
    protected void onDraw(Canvas c){
      super.onDraw(c); float w=getWidth(),h=getHeight(); bowlW=Math.min(w*.55f,360); bowlX=w/2; bowlY=h-100;
      p.setStyle(Paint.Style.FILL); p.setColor(Color.BLACK);
      c.drawText("Счёт: "+score,28,48,p); p.setTextSize(16); p.setColor(Color.DKGRAY); c.drawText("Рекорд: "+sp.getInt("best",0),28,72,p);
      p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5); p.setColor(Color.BLACK);
      RectF bowl=new RectF(bowlX-bowlW/2,bowlY-38,bowlX+bowlW/2,bowlY+48); c.drawArc(bowl,0,180,false,p); c.drawLine(bowl.left,bowlY,bowl.right,bowlY,p);
      p.setStyle(Paint.Style.FILL); p.setColor(Color.BLACK);
      for(Drop d:drops)c.drawCircle(d.x,d.y,d.r,p);
      if(last==0)last=System.currentTimeMillis();
      long now=System.currentTimeMillis(), dt=Math.min(40,now-last); last=now;
      if(now-spawn>Math.max(260,850-score*4)){drops.add(new Drop(30+r.nextFloat()*(w-60),110,7+r.nextFloat()*5));spawn=now;}
      Iterator<Drop> it=drops.iterator();
      while(it.hasNext()){Drop d=it.next(); d.y+=dt*(0.16f+score*0.002f);
        if(d.y+d.r>=bowlY-2 && d.x>bowlX-bowlW/2 && d.x<bowlX+bowlW/2){score++; int best=sp.getInt("best",0); if(score>best)sp.edit().putInt("best",score).apply(); it.remove();}
        else if(d.y>h+30){gameOver=true; score=0; it.remove();}
      }
      if(gameOver){p.setColor(Color.BLACK);p.setTextSize(28);p.setTextAlign(Paint.Align.CENTER);c.drawText("Промах!",w/2,h/2-10,p);p.setTextSize(17);c.drawText("Коснись экрана, чтобы продолжить",w/2,h/2+25,p);p.setTextAlign(Paint.Align.LEFT);}
      postInvalidateDelayed(16);
    }
    public boolean onTouchEvent(android.view.MotionEvent e){
      if(e.getAction()==MotionEvent.ACTION_DOWN){ if(gameOver){gameOver=false; score=0; last=System.currentTimeMillis();} else {bowlX=Math.max(bowlW/2,Math.min(getWidth()-bowlW/2,e.getX()));} return true;}
      if(e.getAction()==MotionEvent.ACTION_MOVE && !gameOver){bowlX=Math.max(bowlW/2,Math.min(getWidth()-bowlW/2,e.getX())); return true;} return true;
    }
    static class Drop{float x,y,r;Drop(float a,float b,float c){x=a;y=b;r=c;}}
  }
}
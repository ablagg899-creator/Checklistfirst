package com.example.chronicles;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.*;
import android.content.Context;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(1024,1024);
        setContentView(new RPGView(this));
    }
}

class RPGView extends View {
    final int W=960,H=540;
    Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), t=new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap hero,mapImage;
    float px=480, py=285, joyX=0, joyY=0;
    boolean menu=false, dialogue=false;
    String dialogueText=""; int dialogueIndex=0, chapter=1; boolean shardQuest=false;
    int area=0; ArrayList<NPC> npcs=new ArrayList<>(); ArrayList<RectF> walls=new ArrayList<>();

    RPGView(Context c){ super(c); setFocusable(true); t.setTypeface(Typeface.MONOSPACE); loadArt(); setup(); }

    void loadArt(){ try { hero=decode("hero"); mapImage=decode("village"); } catch(Exception ignored){} }
    Bitmap decode(String n)throws Exception{
        InputStream in=getResources().getAssets().open(n+".txt"); ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] b=new byte[8192]; int k; while((k=in.read(b))>0) out.write(b,0,k);
        byte[] raw=android.util.Base64.decode(out.toString("UTF-8"),android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(raw,0,raw.length);
    }
    void setup(){
        npcs.clear(); walls.clear();
        walls.add(new RectF(0,0,W,35)); walls.add(new RectF(0,H-150,W,H)); walls.add(new RectF(0,0,35,H)); walls.add(new RectF(W-35,0,W,H));
        if(area==0){
            npcs.add(new NPC(485,225,"Elyse","The river bells rang beneath Asterfall again last night."));
            npcs.add(new NPC(630,330,"Old Rowan","Your father left a journal at the ruined watchtower. Take this path east."));
            npcs.add(new NPC(350,360,"Mira","Beyond the forest lies a road no map remembers."));
            walls.add(new RectF(330,175,620,215)); walls.add(new RectF(355,390,605,430));
        } else if(area==1){
            npcs.add(new NPC(470,250,"Kai","The forest remembers every promise. Even the ones we regret."));
            npcs.add(new NPC(690,370,"Warden","Three Star Shards once sealed the Hollow King. One is near the tower."));
            walls.add(new RectF(170,120,260,360)); walls.add(new RectF(690,120,790,420)); walls.add(new RectF(390,355,600,405));
        } else {
            npcs.add(new NPC(500,250,"Bran","The tower is ahead. Whatever waits inside already knows your name."));
            walls.add(new RectF(160,130,330,230)); walls.add(new RectF(620,130,800,230)); walls.add(new RectF(350,355,610,430));
        }
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c); float sx=getWidth()/(float)W, sy=getHeight()/(float)H; c.save(); c.scale(sx,sy); drawGame(c); c.restore();
    }
    void drawGame(Canvas c){ drawMap(c); drawNPCs(c); drawHero(c); drawHud(c); if(dialogue)drawDialogue(c); if(menu)drawMenu(c); drawControls(c); }

    void drawMap(Canvas c){
        if(area==0 && mapImage!=null) c.drawBitmap(mapImage,null,new RectF(35,35,925,390),p);
        else {
            p.setColor(area==1?Color.rgb(35,96,55):Color.rgb(67,53,72)); c.drawRect(0,0,W,H,p);
            p.setColor(area==1?Color.rgb(47,128,67):Color.rgb(88,72,76)); c.drawRect(35,35,925,390,p);
            Random r=new Random(area*41+7); for(int i=0;i<95;i++){ float x=45+r.nextInt(860), y=45+r.nextInt(330); p.setColor(area==1?Color.rgb(20,72,36):Color.rgb(104,82,86)); c.drawCircle(x,y,7+r.nextInt(10),p); }
            if(area==2){p.setColor(Color.rgb(125,72,45));c.drawRect(390,100,570,340,p);p.setColor(Color.rgb(45,31,51));c.drawRect(425,145,535,340,p);p.setColor(Color.rgb(215,135,65));c.drawRect(450,260,510,340,p);}
        }
        p.setColor(Color.argb(150,0,0,0)); c.drawRect(0,390,W,H,p);
        t.setTypeface(Typeface.DEFAULT_BOLD); t.setTextSize(20); t.setColor(Color.WHITE);
        c.drawText(area==0?"Asterfall Village":area==1?"Whispering Green":"Ruined Watchtower",48,65,t);
    }
    void drawNPCs(Canvas c){
        for(NPC n:npcs){
            p.setColor(Color.rgb(66,38,31));c.drawCircle(n.x,n.y-10,15,p);
            p.setColor(n.name.equals("Kai")?Color.rgb(60,85,170):Color.rgb(178,128,68));c.drawRect(n.x-13,n.y+2,n.x+13,n.y+25,p);
            if(distance(px,py,n.x,n.y)<65){p.setColor(Color.WHITE);c.drawCircle(n.x,n.y-35,12,p);t.setColor(Color.BLACK);t.setTextSize(18);c.drawText("!",n.x-5,n.y-29,t);}
        }
    }
    void drawHero(Canvas c){
        if(hero!=null)c.drawBitmap(hero,null,new RectF(px-28,py-58,px+28,py),p);
        else {p.setColor(Color.rgb(65,95,190));c.drawRect(px-14,py-35,px+14,py,p);p.setColor(Color.rgb(110,70,45));c.drawCircle(px,py-45,14,p);}
    }
    void drawHud(Canvas c){
        p.setColor(Color.argb(205,4,16,34));c.drawRoundRect(18,15,350,105,18,18,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.rgb(235,194,85));c.drawRoundRect(18,15,350,105,18,18,p);p.setStyle(Paint.Style.FILL);
        t.setTypeface(Typeface.DEFAULT_BOLD);t.setColor(Color.WHITE);t.setTextSize(18);c.drawText("Chronicles of Asterfall",35,42,t);
        t.setTypeface(Typeface.DEFAULT);t.setTextSize(14);c.drawText("Chapter "+chapter+"  •  Star Shards: "+(shardQuest?"1 / 3":"0 / 3"),35,66,t);c.drawText("Analog Move  •  A Talk  •  B Menu",35,88,t);
    }
    void drawControls(Canvas c){
        p.setColor(Color.argb(105,255,255,255));c.drawCircle(105,450,70,p);p.setColor(Color.argb(155,20,40,65));c.drawCircle(105+joyX*32,450+joyY*32,27,p);
        p.setColor(Color.argb(165,235,194,85));c.drawCircle(850,445,38,p);c.drawCircle(775,475,30,p);
        t.setTypeface(Typeface.DEFAULT_BOLD);t.setColor(Color.WHITE);t.setTextSize(25);c.drawText("A",842,454,t);t.setTextSize(20);c.drawText("B",769,482,t);
    }
    void drawDialogue(Canvas c){
        p.setColor(Color.argb(238,7,18,35));c.drawRoundRect(45,345,915,505,20,20,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.rgb(235,194,85));c.drawRoundRect(45,345,915,505,20,20,p);p.setStyle(Paint.Style.FILL);
        t.setTypeface(Typeface.DEFAULT_BOLD);t.setTextSize(19);t.setColor(Color.rgb(255,220,125));String s=dialogueText.substring(0,Math.min(dialogueIndex,dialogueText.length()));
        c.drawText(s,70,385,t);t.setTypeface(Typeface.DEFAULT);t.setTextSize(15);t.setColor(Color.WHITE);c.drawText("A: continue",760,485,t);
    }
    void drawMenu(Canvas c){
        p.setColor(Color.argb(245,4,12,28));c.drawRect(180,70,780,470,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.rgb(235,194,85));c.drawRect(180,70,780,470,p);p.setStyle(Paint.Style.FILL);
        t.setTypeface(Typeface.DEFAULT_BOLD);t.setTextSize(28);t.setColor(Color.WHITE);c.drawText("CHRONICLE MENU",225,120,t);
        t.setTypeface(Typeface.DEFAULT);t.setTextSize(19);c.drawText("Party",235,165,t);c.drawText("Lira  Lv. 6   HP 84/84",260,200,t);c.drawText("Kai   Lv. 5   HP 62/62",260,230,t);c.drawText("Bran  Lv. 5   HP 91/91",260,260,t);
        c.drawText("Journal",235,315,t);c.drawText("The river bells • The missing journal • Star Shards",260,350,t);c.drawText("B: close",650,430,t);
    }
    float distance(float a,float b,float x,float y){return (float)Math.hypot(a-x,b-y);}
    void move(float dx,float dy){
        if(menu||dialogue)return; float speed=3.5f; float nx=px+dx*speed,ny=py+dy*speed;
        if(nx<45||nx>915||ny<70||ny>375)return; for(RectF r:walls)if(r.contains(nx,ny))return; px=nx;py=ny;
        if(px>890&&area<2){area++;px=70;py=220;setup();} else if(px<55&&area>0){area--;px=870;py=220;setup();}
    }
    void action(){
        if(menu){menu=false;invalidate();return;}
        if(dialogue){ if(dialogueIndex<dialogueText.length())dialogueIndex=dialogueText.length(); else {dialogue=false;dialogueIndex=0;} invalidate();return; }
        for(NPC n:npcs)if(distance(px,py,n.x,n.y)<65){ dialogue=true;dialogueText=n.name+": "+n.line;dialogueIndex=0; if(n.name.equals("Old Rowan")){shardQuest=true;chapter=2;} invalidate();return; }
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        float x=e.getX()*W/getWidth(),y=e.getY()*H/getHeight();int a=e.getAction();
        if(a==MotionEvent.ACTION_DOWN||a==MotionEvent.ACTION_MOVE){
            if(distance(x,y,105,450)<95){joyX=Math.max(-1,Math.min(1,(x-105)/55));joyY=Math.max(-1,Math.min(1,(y-450)/55));move(joyX,joyY);invalidate();return true;}
            if(a==MotionEvent.ACTION_DOWN&&distance(x,y,850,445)<55){action();return true;}
            if(a==MotionEvent.ACTION_DOWN&&distance(x,y,775,475)<48){menu=!menu;dialogue=false;invalidate();return true;}
        }
        if(a==MotionEvent.ACTION_UP){joyX=joyY=0;invalidate();}return true;
    }
    static class NPC{float x,y;String name,line;NPC(float x,float y,String n,String l){this.x=x;this.y=y;name=n;line=l;}}
}

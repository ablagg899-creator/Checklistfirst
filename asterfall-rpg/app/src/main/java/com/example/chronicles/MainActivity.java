package com.example.chronicles;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.*;
import android.content.Context;
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
    float px=480, py=285, joyX=0, joyY=0;
    boolean menu=false, dialogue=false;
    String dialogueText=""; int dialogueIndex=0, chapter=1; boolean shardQuest=false;
    int area=0; ArrayList<NPC> npcs=new ArrayList<>(); ArrayList<RectF> walls=new ArrayList<>();
    int walkFrame=0;

    RPGView(Context c){ super(c); setFocusable(true); t.setTypeface(Typeface.MONOSPACE); setup(); }

    void setup(){
        npcs.clear(); walls.clear();
        walls.add(new RectF(0,0,W,35)); walls.add(new RectF(0,H-150,W,H)); walls.add(new RectF(0,0,35,H)); walls.add(new RectF(W-35,0,W,H));
        if(area==0){
            npcs.add(new NPC(485,225,"Elyse","The river bells rang beneath Asterfall again last night.",3));
            npcs.add(new NPC(630,330,"Old Rowan","Your father left a journal at the ruined watchtower. Take this path east.",4));
            npcs.add(new NPC(350,360,"Mira","Beyond the forest lies a road no map remembers.",6));
            npcs.add(new NPC(745,180,"Kai","The old road is waking. We should leave before sunset.",2));
            npcs.add(new NPC(215,315,"Bran","If trouble comes, stand behind me. I have broad shoulders.",1));
            walls.add(new RectF(330,175,620,215)); walls.add(new RectF(355,390,605,430));
        } else if(area==1){
            npcs.add(new NPC(470,250,"Kai","The forest remembers every promise. Even the ones we regret.",2));
            npcs.add(new NPC(690,370,"Warden","Three Star Shards once sealed the Hollow King. One is near the tower.",5));
            npcs.add(new NPC(270,170,"Sela","Keep to the stone path. The eastern ruins are older than the kingdom.",7));
            walls.add(new RectF(170,120,260,360)); walls.add(new RectF(690,120,790,420)); walls.add(new RectF(390,355,600,405));
        } else {
            npcs.add(new NPC(500,250,"Bran","The tower is ahead. Whatever waits inside already knows your name.",1));
            npcs.add(new NPC(735,330,"Lira","The journal is real. I can feel the shard reacting to it.",8));
            npcs.add(new NPC(275,300,"Keeper","Turn back while the bells are silent. The tower remembers the fallen.",9));
            walls.add(new RectF(160,130,330,230)); walls.add(new RectF(620,130,800,230)); walls.add(new RectF(350,355,610,430));
        }
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c); float sx=getWidth()/(float)W, sy=getHeight()/(float)H; c.save(); c.scale(sx,sy); drawGame(c); c.restore(); }
    void drawGame(Canvas c){ drawMap(c); drawNPCs(c); drawHero(c); drawHud(c); if(dialogue)drawDialogue(c); if(menu)drawMenu(c); drawControls(c); }

    void drawMap(Canvas c){
        if(area==0) drawVillage(c); else if(area==1) drawForest(c); else drawTower(c);
        p.setColor(Color.argb(145,0,0,0)); c.drawRect(0,390,W,H,p);
        t.setTypeface(Typeface.DEFAULT_BOLD); t.setTextSize(20); t.setColor(Color.WHITE);
        c.drawText(area==0?"ASTERFALL VILLAGE":area==1?"WHISPERING GREEN":"RUINED WATCHTOWER",48,65,t);
    }

    void drawVillage(Canvas c){
        p.setColor(Color.rgb(92,160,83)); c.drawRect(0,0,W,H,p);
        // water and plaza
        p.setColor(Color.rgb(52,128,184)); c.drawRect(0,70,115,390,p); c.drawRect(845,75,960,390,p);
        p.setColor(Color.rgb(194,176,130)); c.drawRect(115,80,845,390,p);
        p.setColor(Color.rgb(220,201,153)); for(int x=120;x<840;x+=45)c.drawRect(x,80, x+5,390,p);
        // houses
        house(c,150,105,275,205,Color.rgb(72,112,188),Color.rgb(217,188,139));
        house(c,675,105,820,205,Color.rgb(177,67,66),Color.rgb(224,190,137));
        house(c,300,265,450,380,Color.rgb(66,132,81),Color.rgb(213,188,139));
        house(c,510,265,660,380,Color.rgb(87,102,175),Color.rgb(216,187,138));
        // fountain
        p.setColor(Color.rgb(116,116,124)); c.drawCircle(480,235,52,p); p.setColor(Color.rgb(59,146,201)); c.drawCircle(480,235,38,p); p.setColor(Color.rgb(98,201,237)); c.drawRect(476,175,484,220,p); c.drawCircle(480,174,10,p);
        // trees
        for(int x=50;x<930;x+=90){ tree(c,x,85); tree(c,x+35,365); }
    }
    void house(Canvas c,float x,float y,float x2,float y2,int roof,int wall){
        p.setColor(wall); c.drawRect(x,y+35,x2,y2,p); p.setColor(roof); Path q=new Path(); q.moveTo(x-12,y+45);q.lineTo((x+x2)/2,y-15);q.lineTo(x2+12,y+45);q.close();c.drawPath(q,p); p.setColor(Color.rgb(91,60,45));c.drawRect((x+x2)/2-18,y2-65,(x+x2)/2+18,y2,p); p.setColor(Color.rgb(110,170,200));c.drawRect(x+20,y+70,x+45,y+100,p);c.drawRect(x2-45,y+70,x2-20,y+100,p);
    }
    void tree(Canvas c,float x,float y){ p.setColor(Color.rgb(91,67,45));c.drawRect(x-5,y+22,x+5,y+45,p);p.setColor(Color.rgb(35,108,57));c.drawCircle(x,y+10,25,p);p.setColor(Color.rgb(49,133,69));c.drawCircle(x-12,y,17,p);c.drawCircle(x+14,y-2,16,p); }

    void drawForest(Canvas c){
        p.setColor(Color.rgb(31,101,55)); c.drawRect(0,0,W,H,p); p.setColor(Color.rgb(96,154,77)); c.drawRect(75,55,885,390,p);
        p.setColor(Color.rgb(166,151,103)); Path path=new Path();path.moveTo(390,390);path.lineTo(520,390);path.lineTo(640,55);path.lineTo(520,55);path.close();c.drawPath(path,p);
        for(int x=35;x<930;x+=65){tree(c,x,70+(x%3)*80);tree(c,x+25,250+(x%2)*50);} 
        p.setColor(Color.rgb(53,137,191));c.drawRect(0,320,150,390,p); p.setColor(Color.rgb(197,186,129));c.drawRect(120,345,210,365,p);
    }
    void drawTower(Canvas c){
        p.setColor(Color.rgb(54,45,61));c.drawRect(0,0,W,H,p); p.setColor(Color.rgb(94,77,78));c.drawRect(55,60,905,390,p);
        p.setColor(Color.rgb(70,59,65)); for(int y=75;y<390;y+=45)for(int x=65;x<900;x+=90)c.drawRect(x,y,x+78,y+35,p);
        p.setColor(Color.rgb(112,72,48));c.drawRect(385,100,575,390,p);p.setColor(Color.rgb(36,29,39));c.drawRect(425,160,535,390,p);p.setColor(Color.rgb(210,120,48));c.drawCircle(480,250,18,p);c.drawRect(474,245,486,330,p);
        p.setColor(Color.rgb(225,157,68));c.drawCircle(165,165,18,p);c.drawCircle(795,165,18,p);
    }

    void drawNPCs(Canvas c){ for(NPC n:npcs){ drawCharacter(c,n.x,n.y,n.kind,false); if(distance(px,py,n.x,n.y)<65){p.setColor(Color.WHITE);c.drawCircle(n.x,n.y-55,12,p);t.setColor(Color.rgb(30,45,65));t.setTextSize(18);t.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("!",n.x-5,n.y-49,t);} } }

    void drawHero(Canvas c){ walkFrame++; drawCharacter(c,px,py,0,true); }

    // Character designs are based on the uploaded Asterfall sprite sheet: blue-haired hero, purple mage, red/white healer, armored warrior and varied townsfolk.
    void drawCharacter(Canvas c,float x,float y,int kind,boolean hero){
        int bob=((walkFrame/10)&1); float yy=y+bob;
        if(hero) kind=0;
        // shadow
        p.setColor(Color.argb(90,0,0,0));c.drawOval(x-18,yy-3,x+18,yy+7,p);
        if(kind==0) spriteHero(c,x,yy);
        else if(kind==1) spriteBran(c,x,yy);
        else if(kind==2) spriteKai(c,x,yy);
        else if(kind==3) spriteElyse(c,x,yy);
        else spriteVillager(c,x,yy,kind);
    }
    void pixel(Canvas c,int col,float x,float y,float w,float h){p.setColor(col);c.drawRect(x,y,x+w,y+h,p);}
    void spriteHero(Canvas c,float x,float y){
        pixel(c,Color.rgb(40,49,67),x-13,y-48,26,11); pixel(c,Color.rgb(45,83,157),x-15,y-38,30,25); pixel(c,Color.rgb(37,66,125),x-20,y-20,40,18);
        pixel(c,Color.rgb(66,110,205),x-8,y-42,16,9); pixel(c,Color.rgb(42,55,83),x-10,y-52,20,8); pixel(c,Color.rgb(67,38,39),x-7,y-48,14,7);
        pixel(c,Color.rgb(235,198,110),x-16,y-34,5,25);pixel(c,Color.rgb(235,198,110),x+11,y-34,5,25);pixel(c,Color.rgb(241,215,136),x-3,y-31,7,18);
        pixel(c,Color.rgb(49,61,79),x-15,y-2,11,12);pixel(c,Color.rgb(49,61,79),x+4,y-2,11,12);
    }
    void spriteBran(Canvas c,float x,float y){
        pixel(c,Color.rgb(79,48,39),x-14,y-49,28,15);pixel(c,Color.rgb(105,63,43),x-11,y-45,22,13);pixel(c,Color.rgb(57,39,36),x-16,y-52,32,8);
        pixel(c,Color.rgb(104,78,63),x-17,y-31,34,29);pixel(c,Color.rgb(80,60,54),x-22,y-22,9,20);pixel(c,Color.rgb(80,60,54),x+13,y-22,9,20);pixel(c,Color.rgb(57,53,55),x-14,y-2,11,12);pixel(c,Color.rgb(57,53,55),x+3,y-2,11,12);pixel(c,Color.rgb(214,174,85),x-4,y-27,8,7);
    }
    void spriteKai(Canvas c,float x,float y){
        Path h=new Path();h.moveTo(x-20,y-32);h.lineTo(x,y-62);h.lineTo(x+22,y-30);h.close();p.setColor(Color.rgb(41,55,130));c.drawPath(h,p);pixel(c,Color.rgb(67,82,177),x-18,y-34,36,32);pixel(c,Color.rgb(116,79,45),x-8,y-50,16,16);pixel(c,Color.rgb(32,42,93),x-22,y-33,8,25);pixel(c,Color.rgb(32,42,93),x+14,y-33,8,25);pixel(c,Color.rgb(91,48,121),x-13,y-2,10,12);pixel(c,Color.rgb(91,48,121),x+3,y-2,10,12);pixel(c,Color.rgb(94,196,241),x-4,y-28,8,7);
    }
    void spriteElyse(Canvas c,float x,float y){
        pixel(c,Color.rgb(239,208,165),x-10,y-52,20,17);pixel(c,Color.rgb(246,227,198),x-13,y-35,26,15);pixel(c,Color.rgb(247,247,239),x-18,y-22,36,30);pixel(c,Color.rgb(196,58,69),x-17,y-22,7,30);pixel(c,Color.rgb(196,58,69),x+10,y-22,7,30);pixel(c,Color.rgb(226,180,77),x-11,y-58,22,7);pixel(c,Color.rgb(224,191,86),x-12,y-2,9,12);pixel(c,Color.rgb(224,191,86),x+3,y-2,9,12);pixel(c,Color.rgb(82,114,190),x-5,y-29,10,5);
    }
    void spriteVillager(Canvas c,float x,float y,int k){
        int hair=(k%3==0)?Color.rgb(77,47,36):(k%3==1?Color.rgb(58,48,44):Color.rgb(139,91,46));
        int cloth=(k%4==0)?Color.rgb(72,125,76):(k%4==1?Color.rgb(173,88,76):(k%4==2?Color.rgb(70,104,160):Color.rgb(145,117,62)));
        pixel(c,hair,x-10,y-50,20,14);pixel(c,Color.rgb(222,173,126),x-9,y-40,18,15);pixel(c,cloth,x-14,y-26,28,26);pixel(c,Color.rgb(57,58,65),x-13,y,10,11);pixel(c,Color.rgb(57,58,65),x+3,y,10,11);
        if(k==4) pixel(c,Color.rgb(70,57,42),x-14,y-54,28,7); if(k==5) pixel(c,Color.rgb(37,81,120),x-15,y-55,30,7); if(k==6) pixel(c,Color.rgb(207,180,86),x-13,y-53,26,5); if(k==7) pixel(c,Color.rgb(55,91,61),x-17,y-53,34,6); if(k==8) pixel(c,Color.rgb(181,65,73),x-14,y-52,28,6); if(k==9) pixel(c,Color.rgb(82,61,98),x-16,y-53,32,9);
    }

    void drawHud(Canvas c){
        p.setColor(Color.argb(215,4,16,34));c.drawRoundRect(18,15,350,105,18,18,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.rgb(235,194,85));c.drawRoundRect(18,15,350,105,18,18,p);p.setStyle(Paint.Style.FILL);
        t.setTypeface(Typeface.DEFAULT_BOLD);t.setColor(Color.WHITE);t.setTextSize(18);c.drawText("Chronicles of Asterfall",35,42,t);t.setTypeface(Typeface.DEFAULT);t.setTextSize(14);c.drawText("Chapter "+chapter+"  •  Star Shards: "+(shardQuest?"1 / 3":"0 / 3"),35,66,t);c.drawText("Analog Move  •  A Talk  •  B Menu",35,88,t);
    }
    void drawControls(Canvas c){
        p.setColor(Color.argb(105,255,255,255));c.drawCircle(105,450,70,p);p.setColor(Color.argb(175,20,40,65));c.drawCircle(105+joyX*32,450+joyY*32,27,p);
        p.setColor(Color.argb(180,235,194,85));c.drawCircle(850,445,38,p);c.drawCircle(775,475,30,p);t.setTypeface(Typeface.DEFAULT_BOLD);t.setColor(Color.WHITE);t.setTextSize(25);c.drawText("A",842,454,t);t.setTextSize(20);c.drawText("B",769,482,t);
    }
    void drawDialogue(Canvas c){
        p.setColor(Color.argb(240,7,18,35));c.drawRoundRect(45,345,915,505,20,20,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.rgb(235,194,85));c.drawRoundRect(45,345,915,505,20,20,p);p.setStyle(Paint.Style.FILL);
        t.setTypeface(Typeface.DEFAULT_BOLD);t.setTextSize(19);t.setColor(Color.rgb(255,220,125));String s=dialogueText.substring(0,Math.min(dialogueIndex,dialogueText.length()));c.drawText(s,70,385,t);t.setTypeface(Typeface.DEFAULT);t.setTextSize(15);t.setColor(Color.WHITE);c.drawText("A: continue",760,485,t);
    }
    void drawMenu(Canvas c){
        p.setColor(Color.argb(246,4,12,28));c.drawRect(180,70,780,470,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.rgb(235,194,85));c.drawRect(180,70,780,470,p);p.setStyle(Paint.Style.FILL);
        t.setTypeface(Typeface.DEFAULT_BOLD);t.setTextSize(28);t.setColor(Color.WHITE);c.drawText("CHRONICLE MENU",225,120,t);t.setTypeface(Typeface.DEFAULT);t.setTextSize(19);c.drawText("Party",235,165,t);
        drawCharacter(c,410,194,0,false);drawCharacter(c,490,194,2,false);drawCharacter(c,570,194,3,false);drawCharacter(c,650,194,1,false);
        c.drawText("Hero",390,225,t);c.drawText("Kai",470,225,t);c.drawText("Elyse",545,225,t);c.drawText("Bran",625,225,t);
        c.drawText("Journal",235,285,t);c.drawText("The river bells • The missing journal • Star Shards",260,320,t);c.drawText("B: close",650,430,t);
    }
    float distance(float a,float b,float x,float y){return (float)Math.hypot(a-x,b-y);}
    void move(float dx,float dy){
        if(menu||dialogue)return; float speed=3.5f; float nx=px+dx*speed,ny=py+dy*speed;if(nx<45||nx>915||ny<70||ny>375)return;for(RectF r:walls)if(r.contains(nx,ny))return;px=nx;py=ny;
        if(px>890&&area<2){area++;px=70;py=220;setup();}else if(px<55&&area>0){area--;px=870;py=220;setup();}
    }
    void action(){
        if(menu){menu=false;invalidate();return;}if(dialogue){if(dialogueIndex<dialogueText.length())dialogueIndex=dialogueText.length();else{dialogue=false;dialogueIndex=0;}invalidate();return;}
        for(NPC n:npcs)if(distance(px,py,n.x,n.y)<65){dialogue=true;dialogueText=n.name+": "+n.line;dialogueIndex=0;if(n.name.equals("Old Rowan")){shardQuest=true;chapter=2;}invalidate();return;}
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
    static class NPC{float x,y;String name,line;int kind;NPC(float x,float y,String n,String l,int k){this.x=x;this.y=y;name=n;line=l;kind=k;}}
}

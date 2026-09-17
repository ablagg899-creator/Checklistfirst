package com.example.checklistfirst;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.os.Vibrator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    MineView game;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(1024, 1024);
        game = new MineView(this);
        setContentView(game);
    }
    @Override protected void onPause() { if (game != null) game.save(); super.onPause(); }
    @Override protected void onResume() { super.onResume(); if (game != null) game.applyOffline(); }

    static final class OreDrop {
        float x,y,vx,vy,size,rest;
        int type,tier;
        boolean settled;
        OreDrop(float x,float y,int type,int tier,float size){
            this.x=x;this.y=y;this.type=type;this.tier=tier;this.size=size;
            this.vx=(float)(Math.random()*2.2-1.1); this.vy=-(float)(Math.random()*3+2);
        }
        float radius(){ return 5f+size*3f; }
    }
    static final class Spark {
        float x,y,vx,vy,life; int color;
        Spark(float x,float y,int color){this.x=x;this.y=y;this.color=color;life=1f;vx=(float)(Math.random()*6-3);vy=(float)(Math.random()*6-4);}
    }

    static final class MineView extends View {
        final Paint p=new Paint();
        final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Handler handler=new Handler(Looper.getMainLooper());
        final Random rng=new Random(921);
        final SharedPreferences prefs;
        final List<OreDrop> drops=new ArrayList<>();
        final List<Spark> sparks=new ArrayList<>();
        final int[] oreColors={Color.rgb(198,92,46),Color.rgb(166,177,194),Color.rgb(255,192,48),Color.rgb(74,211,255)};
        final String[] upgradeNames={"POWER","SPEED","WIDTH","VALUE","CRIT","LUCK","MAGNET","BACKPACK","AUTO MINER","COMBO","DEEP DRILL","OFFLINE"};
        final String[] upgradeDesc={"more ore per tap","faster passive mining","wider beam + splash","sell ore for more","chance for x3 haul","rarer ores appear sooner","pull loose ore inward","more ore capacity","automatic mining taps","combo multiplier grows","deeper layers pay more","more offline earnings"};
        final int[] upgradeColors={Color.CYAN,Color.rgb(255,198,58),Color.MAGENTA,Color.rgb(130,255,180),Color.rgb(255,104,125),Color.rgb(110,190,255),Color.rgb(170,120,255),Color.rgb(120,220,255),Color.rgb(255,150,70),Color.rgb(255,100,220),Color.rgb(100,255,160),Color.rgb(190,170,255)};

        long coins,totalTaps,lifetime,lastTime,lastFrame,lastSave,lastTap;
        int depth=1,power=1,speed=1,width=1,value=1,crit=1,luck=1,magnet=1,backpack=30,oreCount=0,auto=1,comboLevel=1,deep=1,offline=1;
        int copper,iron,gold,crystal,combo,bestCombo;
        float beamX,beamY,beamAlpha,minerBob,scroll;
        boolean upgrades=false,bag=false;
        int upgradePage=0;
        int w,h;

        final int bg=Color.rgb(5,9,18),panel=Color.rgb(12,20,34),panel2=Color.rgb(18,29,48),cyan=Color.rgb(69,210,255),goldCol=Color.rgb(255,193,57),white=Color.rgb(235,244,255);

        MineView(Context c){
            super(c);
            p.setAntiAlias(false);
            text.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
            prefs=c.getSharedPreferences("pixel_mine_save_v2",Context.MODE_PRIVATE);
            load();
            lastFrame=System.currentTimeMillis();
            handler.post(tick);
        }
        int get(String key,int def){return prefs.getInt(key,def);}
        void load(){
            coins=prefs.getLong("coins",0);totalTaps=prefs.getLong("taps",0);lifetime=prefs.getLong("life",0);
            depth=get("depth",1);power=get("power",1);speed=get("speed",1);width=get("width",1);value=get("value",1);crit=get("crit",1);luck=get("luck",1);magnet=get("magnet",1);backpack=get("bag",30);auto=get("auto",1);comboLevel=get("comboLevel",1);deep=get("deep",1);offline=get("offline",1);
            oreCount=get("oreCount",0);copper=get("copper",0);iron=get("iron",0);gold=get("gold",0);crystal=get("crystal",0);combo=get("combo",0);bestCombo=get("best",0);
            lastTime=prefs.getLong("time",System.currentTimeMillis());
            normalize();
        }
        void normalize(){
            if(backpack<30)backpack=30;if(power<1)power=1;if(speed<1)speed=1;if(width<1)width=1;if(value<1)value=1;if(crit<1)crit=1;if(luck<1)luck=1;if(magnet<1)magnet=1;if(auto<1)auto=1;if(comboLevel<1)comboLevel=1;if(deep<1)deep=1;if(offline<1)offline=1;
            if(oreCount<0)oreCount=0;
        }
        void save(){
            lastTime=System.currentTimeMillis();
            prefs.edit().putLong("coins",coins).putLong("taps",totalTaps).putLong("life",lifetime).putInt("depth",depth).putInt("power",power).putInt("speed",speed).putInt("width",width).putInt("value",value).putInt("crit",crit).putInt("luck",luck).putInt("magnet",magnet).putInt("bag",backpack).putInt("auto",auto).putInt("comboLevel",comboLevel).putInt("deep",deep).putInt("offline",offline).putInt("oreCount",oreCount).putInt("copper",copper).putInt("iron",iron).putInt("gold",gold).putInt("crystal",crystal).putInt("combo",combo).putInt("best",bestCombo).putLong("time",lastTime).apply();
            lastSave=lastTime;
        }
        void applyOffline(){
            long now=System.currentTimeMillis();
            long sec=Math.max(0,Math.min(28800,(now-lastTime)/1000));
            if(sec>2){long earned=(long)(sec*(power+speed+auto)*offline*.35f);coins+=earned;lifetime+=earned;}
            lastTime=now;invalidate();
        }

        final Runnable tick=new Runnable(){public void run(){
            long now=System.currentTimeMillis();float dt=Math.min(.05f,Math.max(0,(now-lastFrame)/1000f));lastFrame=now;
            minerBob+=dt*5;scroll+=dt*(.6f+depth*.018f);if(beamAlpha>0)beamAlpha-=dt*4;
            long passive=(long)((speed+auto)*.22f*dt*10f);if(passive>0){coins+=passive;lifetime+=passive;}
            simulateDrops(dt);
            simulateSparks(dt);
            if(now-lastTap>1400)combo=0;
            if(now-lastSave>10000)save();
            invalidate();handler.postDelayed(this,16);
        }};

        void simulateDrops(float dt){
            float floor=h*.665f;
            for(OreDrop d:drops){
                if(d.settled){d.rest+=dt;continue;}
                d.vy+=38f*dt;d.x+=d.vx;d.y+=d.vy;
                if(magnet>1){float dx=w*.5f-d.x,dy=(h*.64f)-d.y;float dist=(float)Math.sqrt(dx*dx+dy*dy);if(dist>1&&dist<45+magnet*18){d.vx+=dx/dist*magnet*.9f*dt;d.vy+=dy/dist*magnet*.9f*dt;}}
                float r=d.radius();
                if(d.x<20+r){d.x=20+r;d.vx=Math.abs(d.vx)*.55f;}if(d.x>w-20-r){d.x=w-20-r;d.vx=-Math.abs(d.vx)*.55f;}
                if(d.y>=floor-r){d.y=floor-r;d.vy*=-.20f;d.vx*=.86f;if(Math.abs(d.vy)<.8f){d.vy=0;d.settled=true;d.rest=0;}}
            }
            combineDrops();
            for(int i=drops.size()-1;i>=0;i--){OreDrop d=drops.get(i);if(d.settled&&d.rest>2.5f){collectDrop(d);drops.remove(i);}}
            if(drops.size()>90){for(int i=drops.size()-1;i>=60;i--){if(drops.get(i).settled){collectDrop(drops.get(i));drops.remove(i);}}}
        }
        void combineDrops(){
            for(int i=0;i<drops.size();i++){
                OreDrop a=drops.get(i);if(!a.settled)continue;
                for(int j=i+1;j<drops.size();j++){
                    OreDrop b=drops.get(j);if(!b.settled||a.type!=b.type||a.tier!=b.tier)continue;
                    float dx=a.x-b.x,dy=a.y-b.y,rr=a.radius()+b.radius();
                    if(dx*dx+dy*dy<rr*rr){
                        a.tier=Math.min(5,a.tier+1);a.size=Math.min(5f,a.size+.75f);a.x=(a.x+b.x)*.5f;a.y=Math.min(a.y,b.y);b.rest=99;b.settled=true;b.x=-100;b.y=-100;
                        sparks.add(new Spark(a.x,a.y,oreColors[a.type]));drops.remove(j);j--;break;
                    }
                }
            }
        }
        void collectDrop(OreDrop d){
            if(oreCount>=backpack)return;
            int amount=Math.max(1,1<<Math.min(4,d.tier));int free=backpack-oreCount;amount=Math.min(amount,free);for(int i=0;i<amount;i++)addOre(d.type);oreCount+=amount;
        }
        void simulateSparks(float dt){for(Spark s:sparks){s.life-=dt*3;s.x+=s.vx*dt*12;s.y+=s.vy*dt*12;s.vy+=dt*5;}for(int i=sparks.size()-1;i>=0;i--)if(sparks.get(i).life<=0)sparks.remove(i);}

        @Override protected void onDraw(Canvas c){super.onDraw(c);w=getWidth();h=getHeight();if(w<=0||h<=0)return;c.drawColor(bg);drawBackdrop(c);drawHeader(c);drawMine(c);drawControls(c);if(upgrades)drawUpgrades(c);if(bag)drawBag(c);}
        void rect(Canvas c,float l,float t,float r,float b,int col){p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRect(l,t,r,b,p);}
        void round(Canvas c,float l,float t,float r,float b,float rad,int col){p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p);}
        void stroke(Canvas c,float l,float t,float r,float b,int col,float sw){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(col);c.drawRoundRect(new RectF(l,t,r,b),8,8,p);p.setStyle(Paint.Style.FILL);}
        void txt(Canvas c,String s,float x,float y,float size,int col){text.setTextAlign(Paint.Align.LEFT);text.setTextSize(size);text.setColor(col);c.drawText(s,x,y,text);}
        void center(Canvas c,String s,float x,float y,float size,int col){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(size);text.setColor(col);c.drawText(s,x,y,text);}
        String fmt(long n){if(n>=1000000000)return String.format("%.1fB",n/1000000000f);if(n>=1000000)return String.format("%.1fM",n/1000000f);if(n>=1000)return String.format("%.1fK",n/1000f);return String.valueOf(n);}

        void drawBackdrop(Canvas c){
            for(int i=0;i<10;i++){float yy=i*180-(scroll%180);rect(c,0,yy,w,yy+180,Color.rgb(7+(i%3)*2,12+(i%4)*2,23+(i%5)*2));}
            for(int i=0;i<24;i++){float x=(i*79+17)%w;float y=(i*113+(int)scroll)%Math.max(1,(int)(h*.68f));rect(c,x,y,x+3,y+3,Color.rgb(28,45,66));}
        }
        void drawHeader(Canvas c){
            rect(c,0,0,w,104,Color.rgb(7,14,27));rect(c,0,100,w,104,Color.rgb(48,126,230));
            txt(c,"PIXEL MINE",18,36,24,white);txt(c,"TYCOON",18,64,17,cyan);
            stat(c,154,13,274,89,"COINS",fmt(coins),goldCol);stat(c,282,13,382,89,"DEPTH",depth+"m",cyan);
            round(c,w-122,17,w-16,88,15,panel2);center(c,"ORE",w-69,42,10,white);center(c,oreCount+"/"+backpack,w-69,69,17,cyan);
        }
        void stat(Canvas c,float l,float t,float r,float b,String a,String v,int col){round(c,l,t,r,b,13,panel);txt(c,a,l+10,t+20,9,Color.rgb(140,163,188));txt(c,v,l+10,t+51,18,col);}

        void drawMine(Canvas c){
            float top=118,bottom=h*.68f;round(c,10,top,w-10,bottom,17,Color.rgb(10,17,29));stroke(c,10,top,w-10,bottom,Color.rgb(36,70,101),3);
            int[] strata={Color.rgb(80,57,43),Color.rgb(69,68,77),Color.rgb(56,62,76),Color.rgb(77,53,49),Color.rgb(46,58,74)};
            for(int i=0;i<5;i++){float sy=top+40+i*88+(scroll%88);rect(c,13,sy,w-13,sy+88,strata[(i+depth)%strata.length]);}
            int widthSpan=Math.max(1,w-40);int heightSpan=Math.max(1,(int)(bottom-top-60));
            for(int i=0;i<48;i++){float x=20+(i*97)%widthSpan;float y=top+35+((i*53+(int)scroll*2)%heightSpan);int col=(i+depth)%5==0?Color.rgb(105,91,87):Color.rgb(53,58,68);rect(c,x,y,x+7,y+5,col);}
            for(int i=0;i<11;i++){float x=30+(i*139)%widthSpan;float y=top+60+((i*87+(int)scroll)%heightSpan);drawCluster(c,x,y,(i+depth+luck)%4,1);}
            float mx=w*.5f,my=bottom-75+(float)Math.sin(minerBob)*2;drawMiner(c,mx,my);
            if(beamAlpha>0)drawBeam(c,mx,my-28,beamX,beamY,beamAlpha);
            for(OreDrop d:drops)if(d.x>-50)drawOre(c,d.x,d.y,d.type,d.tier,d.size);
            for(Spark s:sparks){p.setAlpha((int)(255*Math.max(0,s.life)));rect(c,s.x-2,s.y-2,s.x+4,s.y+4,s.color);p.setAlpha(255);}
            center(c,"TAP THE ROCK TO FIRE THE MINING BEAM",w/2,bottom-10,10,Color.rgb(154,176,201));
        }
        void drawMiner(Canvas c,float x,float y){
            round(c,x-27,y+27,x+27,y+37,5,Color.argb(100,0,0,0));
            rect(c,x-17,y+11,x-5,y+31,Color.rgb(34,41,52));rect(c,x+5,y+11,x+17,y+31,Color.rgb(34,41,52));
            rect(c,x-20,y-10,x+20,y+15,Color.rgb(29,104,139));rect(c,x-16,y-6,x+16,y+12,Color.rgb(37,135,164));
            rect(c,x-15,y-29,x+15,y-8,Color.rgb(248,191,80));rect(c,x-19,y-35,x+19,y-27,Color.rgb(244,178,38));rect(c,x-22,y-30,x+22,y-26,Color.rgb(215,139,22));rect(c,x-4,y-35,x+5,y-30,Color.rgb(240,250,255));
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(Color.rgb(196,211,224));c.drawLine(x+20,y+3,x+38,y-20,p);c.drawLine(x+29,y-22,x+42,y-14,p);p.setStyle(Paint.Style.FILL);
        }
        void drawBeam(Canvas c,float x1,float y1,float x2,float y2,float a){float dx=x2-x1,dy=y2-y1,len=(float)Math.sqrt(dx*dx+dy*dy);if(len<1)return;int alpha=(int)(220*Math.max(0,a));float sw=5+width*3;p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.SQUARE);p.setStrokeWidth(sw*2);p.setColor(Color.argb(alpha/3,40,190,255));c.drawLine(x1,y1,x2,y2,p);p.setStrokeWidth(sw);p.setColor(Color.argb(alpha,65,215,255));c.drawLine(x1,y1,x2,y2,p);p.setStrokeWidth(Math.max(2,sw/3));p.setColor(Color.argb(255,230,252,255));c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);}
        void drawCluster(Canvas c,float x,float y,int type,float s){for(int i=0;i<5;i++)drawOre(c,x+(i%3-1)*8*s,y+(i/3-1)*7*s,type,0,0.9f);}
        void drawOre(Canvas c,float x,float y,int type,int tier,float size){int col=oreColors[Math.max(0,Math.min(3,type))];float mult=Math.min(2.7f,size*(1+tier*.28f));rect(c,x-6*mult,y-5*mult,x+6*mult,y+6*mult,col);rect(c,x-3*mult,y-8*mult,x+4*mult,y-4*mult,light(col));rect(c,x-4*mult,y+2*mult,x+2*mult,y+5*mult,dark(col));if(tier>0)center(c,""+(tier+1),x,y+3,Math.max(6,7*mult),Color.WHITE);}
        int light(int c){return Color.rgb(Math.min(255,Color.red(c)+55),Math.min(255,Color.green(c)+55),Math.min(255,Color.blue(c)+55));}
        int dark(int c){return Color.rgb(Color.red(c)/2,Color.green(c)/2,Color.blue(c)/2);}

        void drawControls(Canvas c){
            float y=h*.70f;rect(c,0,y,w,h,bg);
            round(c,18,y+10,w-18,y+108,21,Color.rgb(17,38,61));stroke(c,18,y+10,w-18,y+108,Color.rgb(50,117,171),3);round(c,w/2-76,y+21,w/2+76,y+97,38,Color.rgb(28,122,178));stroke(c,w/2-76,y+21,w/2+76,y+97,Color.rgb(99,224,255),3);center(c,"MINE",w/2,y+57,23,white);center(c,"+"+power+" ore",w/2,y+80,10,Color.rgb(176,234,255));
            float by=y+122,bw=(w-70)/3f;card(c,20,by,20+bw,"UPGRADES","TREE",Color.rgb(255,100,220));card(c,35+bw,by,35+2*bw,"SELL","$"+sellValue(),goldCol);card(c,50+2*bw,by,50+3*bw,"SATCHEL",oreCount+"/"+backpack,Color.rgb(145,120,255));
            if(combo>1)center(c,"COMBO x"+combo,w/2,by+102,15,Color.rgb(255,116,238));
            txt(c,"Depth milestone: every 25 taps",20,by+105,9,Color.rgb(125,149,177));
        }
        void card(Canvas c,float l,float t,float r,String a,String b,int col){round(c,l,t,r,t+72,14,panel2);txt(c,a,l+10,t+25,9,Color.rgb(139,161,185));txt(c,b,l+10,t+53,16,col);}

        void drawUpgrades(Canvas c){
            rect(c,0,0,w,h,Color.argb(220,0,0,0));float l=18,r=w-18,t=72,b=h-60;round(c,l,t,r,b,20,Color.rgb(9,17,30));stroke(c,l,t,r,b,Color.rgb(61,133,188),3);
            txt(c,"UPGRADE TREE",l+18,t+34,22,white);txt(c,"12 branches • page "+(upgradePage+1)+"/3",l+18,t+56,10,Color.rgb(139,164,190));
            int start=upgradePage*4;for(int i=0;i<4;i++){int id=start+i;float yy=t+72+i*82;upgradeRow(c,id,yy);}
            round(c,l+18,b-46,l+75,b-12,12,panel2);round(c,w/2-30,b-46,w/2+30,b-12,12,panel2);round(c,r-75,b-46,r-18,b-12,12,panel2);center(c,"<",l+46,b-24,16,white);center(c,"CLOSE",w/2,b-24,10,white);center(c,">",r-46,b-24,16,white);
        }
        void upgradeRow(Canvas c,int id,float y){int lv=level(id);long cost=cost(id);int col=upgradeColors[id];round(c,32,y,w-32,y+70,14,panel2);round(c,43,y+13,76,y+57,11,Color.argb(45,Color.red(col),Color.green(col),Color.blue(col)));center(c,""+lv,59,y+41,17,col);txt(c,upgradeNames[id],88,y+24,13,white);txt(c,upgradeDesc[id],88,y+45,9,Color.rgb(137,161,188));round(c,w-136,y+12,w-46,y+58,12,cost<=coins?Color.rgb(29,91,93):Color.rgb(40,47,59));center(c,"$"+fmt(cost),w-91,y+39,12,cost<=coins?goldCol:Color.rgb(135,147,163));}

        void drawBag(Canvas c){rect(c,0,0,w,h,Color.argb(190,0,0,0));float l=25,r=w-25,t=145,b=h-95;round(c,l,t,r,b,20,Color.rgb(10,18,31));txt(c,"ORE SATCHEL",l+20,t+38,22,white);txt(c,"same ores combine into larger chunks",l+20,t+60,10,Color.rgb(137,163,190));rowBag(c,t+85,"COPPER",copper,10,0);rowBag(c,t+140,"IRON",iron,25,1);rowBag(c,t+195,"GOLD",gold,80,2);rowBag(c,t+250,"CRYSTAL",crystal,200,3);txt(c,"TOTAL SELL VALUE",l+20,t+337,10,Color.rgb(137,163,190));txt(c,"$"+fmt(sellValue()),l+20,t+370,26,goldCol);center(c,"TAP OUTSIDE TO CLOSE",w/2,b-20,10,Color.rgb(128,152,180));}
        void rowBag(Canvas c,float y,String name,int n,int val,int type){round(c,43,y,w-43,y+45,12,panel2);drawOre(c,61,y+22,type,0,.75f);txt(c,name,82,y+19,11,white);txt(c,"x"+n,82,y+37,10,Color.rgb(149,171,198));txt(c,"$"+fmt((long)n*val*value),w-110,y+28,12,goldCol);}

        int level(int id){switch(id){case 0:return power;case 1:return speed;case 2:return width;case 3:return value;case 4:return crit;case 5:return luck;case 6:return magnet;case 7:return Math.max(1,(backpack-15)/15);case 8:return auto;case 9:return comboLevel;case 10:return deep;default:return offline;}}
        long cost(int id){int lv=level(id);long base=new long[]{35,55,80,100,140,175,220,260,350,425,500,650}[id];return Math.min(Long.MAX_VALUE/4,base*(long)(lv+1)*(lv+1));}
        void buy(int id){long c=cost(id);if(coins<c)return;coins-=c;switch(id){case 0:power++;break;case 1:speed++;break;case 2:width++;break;case 3:value++;break;case 4:crit++;break;case 5:luck++;break;case 6:magnet++;break;case 7:backpack+=15;break;case 8:auto++;break;case 9:comboLevel++;break;case 10:deep++;break;case 11:offline++;break;}invalidate();vibrate();}

        long sellValue(){return (long)(copper*10+iron*25+gold*80+crystal*200)*value;}
        int randomOre(){int roll=rng.nextInt(100)+luck*2;if(depth<6)return roll<68?0:roll<94?1:2;if(depth<18)return roll<45?0:roll<75?1:roll<97?2:3;return roll<25?0:roll<54?1:roll<86?2:3;}
        void addOre(int type){if(type==0)copper++;else if(type==1)iron++;else if(type==2)gold++;else crystal++;}
        void doMine(float tx,float ty){
            if(upgrades||bag)return;
            totalTaps++;if(now()-lastTap<1400)combo++;else combo=1;lastTap=now();bestCombo=Math.max(bestCombo,combo);
            int gain=power;if(combo>3)gain+=Math.min(comboLevel,combo/4);if(rng.nextInt(100)<Math.min(50,crit*3))gain*=3;
            int free=backpack-oreCount;gain=Math.min(gain,Math.max(0,free));
            for(int i=0;i<gain;i++)drops.add(new OreDrop(tx+(float)(Math.random()*18-9),ty+(float)(Math.random()*12-6),randomOre(),0,1f));
            beamX=tx;beamY=ty;beamAlpha=1f;for(int i=0;i<8+width*2;i++)sparks.add(new Spark(tx,ty,i%2==0?cyan:goldCol));
            long reward=(long)gain*(1+depth/10+deep/5);if(combo>1)reward+=combo*comboLevel;coins+=reward;lifetime+=reward;
            if(totalTaps%25==0){depth++;coins+=25L*depth*deep;for(int i=0;i<10;i++)sparks.add(new Spark(w/2,h*.52f,Color.rgb(255,230,112)));}
            vibrate();
        }
        long now(){return System.currentTimeMillis();}
        void sell(){if(oreCount<=0)return;long v=sellValue();coins+=v;lifetime+=v;copper=iron=gold=crystal=oreCount=0;for(int i=0;i<12;i++)sparks.add(new Spark(w*.75f,h*.78f,goldCol));vibrate();}
        void vibrate(){try{Vibrator v=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);if(v!=null)v.vibrate(18);}catch(Exception ignored){}}

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX(),y=e.getY();
            if(upgrades){float top=72,bottom=h-60;if(y>=bottom-55){if(x<100){upgradePage=(upgradePage+2)%3;}else if(x>w-100){upgradePage=(upgradePage+1)%3;}else{upgrades=false;}invalidate();return true;}int start=upgradePage*4;for(int i=0;i<4;i++){float yy=top+72+i*82;if(y>=yy&&y<=yy+70){buy(start+i);return true;}}return true;}
            if(bag){bag=false;invalidate();return true;}
            float cy=h*.70f,by=cy+122,bw=(w-70)/3f;
            if(y>=by&&y<=by+72){if(x<20+bw){upgrades=true;invalidate();return true;}if(x<35+2*bw){sell();invalidate();return true;}if(x<50+3*bw){bag=true;invalidate();return true;}}
            if(y<95&&x>w-135){bag=true;invalidate();return true;}
            if(y>=118&&y<=h*.68f)doMine(x,y);else if(y>=cy&&y<cy+115)doMine(w/2,y);
            return true;
        }
    }
}

package com.example.checklistfirst;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
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
        getWindow().setFlags(1024,1024);
        game=new MineView(this);
        setContentView(game);
    }
    @Override protected void onPause(){if(game!=null)game.save();super.onPause();}
    @Override protected void onResume(){super.onResume();if(game!=null)game.applyOffline();}

    static final class OreDrop {
        float x,y,vx,vy,size,rest; int type,tier; boolean settled;
        OreDrop(float x,float y,int type,int tier,float size){this.x=x;this.y=y;this.type=type;this.tier=tier;this.size=size;vx=(float)(Math.random()*2.8-1.4);vy=-(float)(Math.random()*3.5+2);}
        float radius(){return 5f+size*3.2f;}
    }
    static final class Spark {
        float x,y,vx,vy,life,max;int color;
        Spark(float x,float y,int color){this.x=x;this.y=y;this.color=color;life=max=1f;vx=(float)(Math.random()*7-3.5);vy=(float)(Math.random()*7-4.5);}
    }

    static final class MineView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint pixel=new Paint();
        final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Handler handler=new Handler(Looper.getMainLooper());
        final Random rng=new Random(921);
        final SharedPreferences prefs;
        final List<OreDrop> drops=new ArrayList<>();
        final List<Spark> sparks=new ArrayList<>();
        final int[] oreColors={Color.rgb(240,104,53),Color.rgb(187,205,226),Color.rgb(255,199,45),Color.rgb(67,218,255)};
        final int[] oreGlow={Color.rgb(255,111,52),Color.rgb(184,220,255),Color.rgb(255,226,72),Color.rgb(71,241,255)};
        final String[] upgradeNames={"POWER","SPEED","WIDTH","VALUE","CRIT","LUCK","MAGNET","BACKPACK","AUTO MINER","COMBO","DEEP DRILL","OFFLINE"};
        final String[] upgradeDesc={"more ore per tap","faster passive mining","wider beam + splash","sell ore for more","chance for x3 haul","rarer ores appear sooner","pull loose ore inward","more ore capacity","automatic mining taps","combo multiplier grows","deeper layers pay more","more offline earnings"};
        final int[] upgradeColors={Color.CYAN,Color.rgb(255,198,58),Color.MAGENTA,Color.rgb(130,255,180),Color.rgb(255,104,125),Color.rgb(110,190,255),Color.rgb(170,120,255),Color.rgb(120,220,255),Color.rgb(255,150,70),Color.rgb(255,100,220),Color.rgb(100,255,160),Color.rgb(190,170,255)};

        long coins,totalTaps,lifetime,lastTime,lastFrame,lastSave,lastTap;
        int depth=1,power=1,speed=1,width=1,value=1,crit=1,luck=1,magnet=1,backpack=30,oreCount=0,auto=1,comboLevel=1,deep=1,offline=1;
        int copper,iron,gold,crystal,combo,bestCombo;
        float beamX,beamY,beamAlpha,minerBob,scroll,flash;
        boolean upgrades=false,bag=false;
        int upgradePage=0;
        int w,h; float S=1f,HW=768f,HH=1365f;

        final int bg=Color.rgb(3,7,16),panel=Color.rgb(8,18,32),panel2=Color.rgb(14,30,51),cyan=Color.rgb(62,214,255),goldCol=Color.rgb(255,196,55),white=Color.rgb(238,247,255);

        MineView(Context c){super(c);pixel.setAntiAlias(false);text.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));prefs=c.getSharedPreferences("pixel_mine_save_v2",Context.MODE_PRIVATE);load();lastFrame=System.currentTimeMillis();handler.post(tick);}
        int get(String k,int d){return prefs.getInt(k,d);}
        void load(){
            coins=prefs.getLong("coins",0);totalTaps=prefs.getLong("taps",0);lifetime=prefs.getLong("life",0);
            depth=get("depth",1);power=get("power",1);speed=get("speed",1);width=get("width",1);value=get("value",1);crit=get("crit",1);luck=get("luck",1);magnet=get("magnet",1);backpack=get("bag",30);auto=get("auto",1);comboLevel=get("comboLevel",1);deep=get("deep",1);offline=get("offline",1);
            oreCount=get("oreCount",0);copper=get("copper",0);iron=get("iron",0);gold=get("gold",0);crystal=get("crystal",0);combo=get("combo",0);bestCombo=get("best",0);lastTime=prefs.getLong("time",System.currentTimeMillis());normalize();
        }
        void normalize(){if(backpack<30)backpack=30;if(power<1)power=1;if(speed<1)speed=1;if(width<1)width=1;if(value<1)value=1;if(crit<1)crit=1;if(luck<1)luck=1;if(magnet<1)magnet=1;if(auto<1)auto=1;if(comboLevel<1)comboLevel=1;if(deep<1)deep=1;if(offline<1)offline=1;if(oreCount<0)oreCount=0;}
        void save(){lastTime=System.currentTimeMillis();prefs.edit().putLong("coins",coins).putLong("taps",totalTaps).putLong("life",lifetime).putInt("depth",depth).putInt("power",power).putInt("speed",speed).putInt("width",width).putInt("value",value).putInt("crit",crit).putInt("luck",luck).putInt("magnet",magnet).putInt("bag",backpack).putInt("auto",auto).putInt("comboLevel",comboLevel).putInt("deep",deep).putInt("offline",offline).putInt("oreCount",oreCount).putInt("copper",copper).putInt("iron",iron).putInt("gold",gold).putInt("crystal",crystal).putInt("combo",combo).putInt("best",bestCombo).putLong("time",lastTime).apply();lastSave=lastTime;}
        void applyOffline(){long now=System.currentTimeMillis();long sec=Math.max(0,Math.min(28800,(now-lastTime)/1000));if(sec>2){long earned=(long)(sec*(power+speed+auto)*offline*.35f);coins+=earned;lifetime+=earned;}lastTime=now;invalidate();}

        final Runnable tick=new Runnable(){public void run(){long now=System.currentTimeMillis();float dt=Math.min(.05f,Math.max(0,(now-lastFrame)/1000f));lastFrame=now;minerBob+=dt*5;scroll+=dt*(5.5f+depth*.06f);if(beamAlpha>0)beamAlpha-=dt*4.4f;if(flash>0)flash-=dt*2.2f;long passive=(long)((speed+auto)*.22f*dt*10f);if(passive>0){coins+=passive;lifetime+=passive;}simulateDrops(dt);simulateSparks(dt);if(now-lastTap>1400)combo=0;if(now-lastSave>10000)save();invalidate();handler.postDelayed(this,16);}};

        void simulateDrops(float dt){float floor=HH*.955f;for(OreDrop d:drops){if(d.settled){d.rest+=dt;continue;}d.vy+=46f*dt;d.x+=d.vx;d.y+=d.vy;if(magnet>1){float dx=HW*.5f-d.x,dy=HH*.82f-d.y,dist=(float)Math.sqrt(dx*dx+dy*dy);if(dist>1&&dist<50+magnet*18){d.vx+=dx/dist*magnet*.8f*dt;d.vy+=dy/dist*magnet*.8f*dt;}}float r=d.radius();if(d.x<26+r){d.x=26+r;d.vx=Math.abs(d.vx)*.55f;}if(d.x>HW-26-r){d.x=HW-26-r;d.vx=-Math.abs(d.vx)*.55f;}if(d.y>=floor-r){d.y=floor-r;d.vy*=-.22f;d.vx*=.86f;if(Math.abs(d.vy)<.8f){d.vy=0;d.settled=true;d.rest=0;}}}combineDrops();for(int i=drops.size()-1;i>=0;i--){OreDrop d=drops.get(i);if(d.settled&&d.rest>2.5f){collectDrop(d);drops.remove(i);}}if(drops.size()>100){for(int i=drops.size()-1;i>=65;i--){if(drops.get(i).settled){collectDrop(drops.get(i));drops.remove(i);}}}}
        void combineDrops(){for(int i=0;i<drops.size();i++){OreDrop a=drops.get(i);if(!a.settled)continue;for(int j=i+1;j<drops.size();j++){OreDrop b=drops.get(j);if(!b.settled||a.type!=b.type||a.tier!=b.tier)continue;float dx=a.x-b.x,dy=a.y-b.y,rr=a.radius()+b.radius();if(dx*dx+dy*dy<rr*rr){a.tier=Math.min(5,a.tier+1);a.size=Math.min(5f,a.size+.75f);a.x=(a.x+b.x)*.5f;a.y=Math.min(a.y,b.y);sparks.add(new Spark(a.x,a.y,oreGlow[a.type]));drops.remove(j);j--;break;}}}}
        void collectDrop(OreDrop d){if(oreCount>=backpack)return;int amount=Math.max(1,1<<Math.min(4,d.tier));int free=backpack-oreCount;amount=Math.min(amount,free);for(int i=0;i<amount;i++)addOre(d.type);oreCount+=amount;}
        void simulateSparks(float dt){for(Spark s:sparks){s.life-=dt*3;s.x+=s.vx*dt*12;s.y+=s.vy*dt*12;s.vy+=dt*5;}for(int i=sparks.size()-1;i>=0;i--)if(sparks.get(i).life<=0)sparks.remove(i);}

        @Override protected void onDraw(Canvas real){super.onDraw(real);w=getWidth();h=getHeight();if(w<=0||h<=0)return;S=w/HW;HH=h/S;Canvas c=real;c.save();c.scale(S,S);pixel.setAlpha(255);c.drawColor(bg);drawBackdrop(c);drawHeader(c);drawMine(c);drawControls(c);if(upgrades)drawUpgrades(c);if(bag)drawBag(c);if(flash>0){pixel.setColor(Color.argb((int)(70*flash),70,220,255));c.drawRect(0,0,HW,HH,pixel);}c.restore();}
        void rect(Canvas c,float l,float t,float r,float b,int col){pixel.setStyle(Paint.Style.FILL);pixel.setColor(col);pixel.setAlpha(Color.alpha(col));c.drawRect(l,t,r,b,pixel);pixel.setAlpha(255);}
        void round(Canvas c,float l,float t,float r,float b,float rad,int col){p.setStyle(Paint.Style.FILL);p.setShader(null);p.setColor(col);c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p);}
        void stroke(Canvas c,float l,float t,float r,float b,int col,float sw){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(col);p.setShader(null);c.drawRoundRect(new RectF(l,t,r,b),10,10,p);p.setStyle(Paint.Style.FILL);}
        void line(Canvas c,float x1,float y1,float x2,float y2,float sw,int col){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setStrokeCap(Paint.Cap.SQUARE);p.setColor(col);p.setShader(null);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);}
        void txt(Canvas c,String s,float x,float y,float size,int col){text.setTextAlign(Paint.Align.LEFT);text.setTextSize(size);text.setColor(col);text.setShader(null);c.drawText(s,x,y,text);}
        void center(Canvas c,String s,float x,float y,float size,int col){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(size);text.setColor(col);text.setShader(null);c.drawText(s,x,y,text);}
        String fmt(long n){if(n>=1000000000)return String.format("%.1fB",n/1000000000f);if(n>=1000000)return String.format("%.1fM",n/1000000f);if(n>=1000)return String.format("%.1fK",n/1000f);return String.valueOf(n);}

        void drawBackdrop(Canvas c){
            LinearGradient g=new LinearGradient(0,0,0,HH,Color.rgb(4,10,23),Color.rgb(2,5,12),Shader.TileMode.CLAMP);p.setShader(g);p.setStyle(Paint.Style.FILL);c.drawRect(0,0,HW,HH,p);p.setShader(null);
            for(int i=0;i<44;i++){float x=(i*137+23)%HW;float y=102+((i*83+(int)(scroll*.35f))%900);int a=35+(i%4)*18;rect(c,x,y,x+2+(i%2),y+2,Color.argb(a,70,120,165));}
            // distant crystalline silhouettes
            for(int i=0;i<9;i++){float x=i*96-20+(scroll*.08f%40);Path q=new Path();q.moveTo(x,118);q.lineTo(x+25,86-(i%3)*8);q.lineTo(x+50,118);q.close();p.setColor(Color.rgb(7,20,36));p.setStyle(Paint.Style.FILL);c.drawPath(q,p);}
        }

        void drawHeader(Canvas c){
            rect(c,0,0,HW,100,Color.rgb(5,12,24));rect(c,0,96,HW,100,Color.rgb(28,111,188));
            // tiny logo pickaxe
            line(c,20,25,43,48,5,Color.rgb(231,154,43));line(c,20,25,32,15,5,Color.rgb(231,154,43));line(c,31,15,51,27,4,Color.rgb(80,221,255));
            txt(c,"PIXEL",58,35,27,white);txt(c,"MINE",149,35,27,goldCol);txt(c,"TYCOON",59,59,13,cyan);txt(c,"DIG • MINE • UPGRADE • REPEAT",139,58,8,Color.rgb(128,162,194));
            stat(c,330,10,460,88,"COINS",fmt(coins),goldCol);stat(c,470,10,590,88,"DEPTH",depth+"m",cyan);stat(c,600,10,750,88,"ORE",oreCount+"/"+backpack,Color.rgb(165,135,255));
            round(c,708,20,748,60,20,Color.rgb(14,34,55));center(c,"+",728,47,20,Color.rgb(80,225,255));
        }
        void stat(Canvas c,float l,float t,float r,float b,String a,String v,int col){round(c,l,t,r,b,13,panel);stroke(c,l,t,r,b,Color.rgb(23,52,78),1);txt(c,a,l+11,t+18,8,Color.rgb(119,151,181));txt(c,v,l+11,t+50,18,col);}

        int layerFor(int d){return Math.min(7,d/80);}
        int layerColor(int layer){int[] a={Color.rgb(83,57,42),Color.rgb(61,65,79),Color.rgb(46,54,72),Color.rgb(72,49,54),Color.rgb(37,62,78),Color.rgb(34,38,61),Color.rgb(48,30,64),Color.rgb(22,25,45)};return a[Math.min(a.length-1,layer)];}

        void drawMine(Canvas c){
            float top=112,bottom=1030;round(c,10,top,758,bottom,18,Color.rgb(4,9,17));stroke(c,10,top,758,bottom,Color.rgb(29,83,123),3);stroke(c,16,118,752,bottom-6,Color.rgb(12,40,63),1);
            // living strata with pixel seams
            float bandH=96, phase=scroll%bandH;
            for(int i=-1;i<11;i++){float sy=top+40+i*bandH-phase;int layer=Math.max(0,Math.min(7,layerFor(depth)+i));rect(c,13,sy,755,sy+bandH+2,layerColor(layer));drawRockTexture(c,sy,bandH,i+depth);}
            // luminous crystal clusters and ore veins
            int widthSpan=700;
            for(int i=0;i<18;i++){float x=34+(i*83)%widthSpan;float y=150+((i*109+(int)(scroll*1.2f))%790);int type=(i+depth+luck)%4;drawCrystalCluster(c,x,y,type,1+(i%2)*.35f);}
            // side walls / deep void for depth drama
            if(depth>10){for(int i=0;i<5;i++){float x=25+i*10;rect(c,x,500,x+3,980,Color.argb(35,0,0,0));float rx=740-i*10;rect(c,rx,500,rx+3,980,Color.argb(35,0,0,0));}}
            drawDepthRuler(c,760,145,1000);
            float mx=384,my=965+(float)Math.sin(minerBob)*2;drawMiner(c,mx,my);
            if(beamAlpha>0)drawBeam(c,mx,my-32,beamX,beamY,beamAlpha);
            for(OreDrop d:drops)if(d.x>-50)drawOre(c,d.x,d.y,d.type,d.tier,d.size);
            for(Spark s:sparks){int a=(int)(255*Math.max(0,s.life));pixel.setAlpha(a);rect(c,s.x-2,s.y-2,s.x+4,s.y+4,s.color);pixel.setAlpha(255);}
            center(c,"TAP THE ROCK TO FIRE THE MINING BEAM",384,1014,9,Color.rgb(144,180,208));
        }
        void drawRockTexture(Canvas c,float sy,float bh,int seed){
            int[] shades={Color.rgb(27,38,53),Color.rgb(39,48,62),Color.rgb(58,56,65),Color.rgb(76,58,55)};
            for(int i=0;i<20;i++){float x=17+((i*67+seed*31)%735);float y=sy+12+((i*29+seed*13)&63);int col=shades[(i+seed)&3];rect(c,x,y,x+7+(i&2),y+4,col);if((i+seed)%5==0)rect(c,x+2,y+4,x+5,y+8,Color.rgb(18,27,40));}
            for(int i=0;i<6;i++){float x=25+((i*123+seed*17)%710);float y=sy+22+((i*41+seed*9)%65);line(c,x,y,x+5,y+3,2,Color.rgb(92,75,71));}
        }
        void drawDepthRuler(Canvas c,float x,float y,float bottom){line(c,x,y,x,bottom,2,Color.rgb(44,105,151));int start=Math.max(1,depth-4);for(int i=0;i<9;i++){float yy=y+i*106;line(c,x-7,yy,x+5,yy,2,Color.rgb(51,129,177));if(i%2==0)txt(c,(start+i*25)+"m",x-62,yy+4,8,Color.rgb(118,177,212));}}

        void drawMiner(Canvas c,float x,float y){
            round(c,x-30,y+30,x+30,y+40,6,Color.argb(120,0,0,0));
            // boots
            rect(c,x-20,y+10,x-4,y+34,Color.rgb(17,25,38));rect(c,x+4,y+10,x+20,y+34,Color.rgb(17,25,38));rect(c,x-22,y+28,x-2,y+36,Color.rgb(26,35,48));rect(c,x+2,y+28,x+22,y+36,Color.rgb(26,35,48));
            // backpack glow and body
            round(c,x-25,y-13,x+24,y+18,4,Color.rgb(18,75,106));rect(c,x-20,y-9,x+19,y+14,Color.rgb(31,132,163));rect(c,x-18,y-5,x+16,y+10,Color.rgb(37,157,181));rect(c,x-24,y-3,x-19,y+13,Color.rgb(12,62,91));
            // helmet + lamp
            rect(c,x-17,y-33,x+17,y-10,Color.rgb(244,174,35));rect(c,x-23,y-31,x+23,y-25,Color.rgb(226,143,19));rect(c,x-19,y-36,x+19,y-31,Color.rgb(255,201,55));rect(c,x-5,y-37,x+7,y-30,Color.rgb(234,249,255));rect(c,x-15,y-12,x+15,y-8,Color.rgb(126,73,25));
            // face visor
            rect(c,x-11,y-23,x+12,y-13,Color.rgb(35,56,70));rect(c,x-8,y-21,x+10,y-16,Color.rgb(83,194,217));
            // laser tool
            line(c,x+18,y+5,x+37,y-18,5,Color.rgb(202,217,230));line(c,x+27,y-21,x+42,y-14,4,Color.rgb(223,235,244));rect(c,x+34,y-19,x+42,y-11,Color.rgb(85,120,143));
            // suit highlights
            rect(c,x-14,y+1,x-10,y+9,Color.rgb(79,205,225));rect(c,x+10,y+1,x+14,y+9,Color.rgb(22,88,116));
        }

        void drawBeam(Canvas c,float x1,float y1,float x2,float y2,float a){float dx=x2-x1,dy=y2-y1,len=(float)Math.sqrt(dx*dx+dy*dy);if(len<1)return;int alpha=(int)(235*Math.max(0,a));float sw=4+width*2.6f;line(c,x1,y1,x2,y2,sw*3,Color.argb(alpha/5,40,180,255));line(c,x1,y1,x2,y2,sw*1.7f,Color.argb(alpha/3,50,210,255));line(c,x1,y1,x2,y2,sw,Color.argb(alpha,51,210,255));line(c,x1,y1,x2,y2,Math.max(1.5f,sw*.25f),Color.argb(255,232,253,255));float nx=-dy/len*sw*.7f,ny=dx/len*sw*.7f;pixel.setColor(Color.argb(alpha/2,90,240,255));pixel.setAlpha(alpha/2);Path q=new Path();q.moveTo(x2,y2);q.lineTo(x2+nx-8,y2+ny-8);q.lineTo(x2-nx-8,y2-ny-8);q.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(alpha/2,80,225,255));c.drawPath(q,p);pixel.setAlpha(255);}
        void drawCrystalCluster(Canvas c,float x,float y,int type,float s){int col=oreColors[type];for(int i=0;i<5;i++){float ox=(i%3-1)*9*s,oy=(i/3-1)*8*s;drawOre(c,x+ox,y+oy,type,0,.9f*s);}p.setColor(Color.argb(38,Color.red(col),Color.green(col),Color.blue(col)));p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,22*s,p);}
        void drawOre(Canvas c,float x,float y,int type,int tier,float size){int col=oreColors[Math.max(0,Math.min(3,type))];float m=Math.min(3.2f,size*(1+tier*.32f));int glow=oreGlow[type];p.setColor(Color.argb(38,Color.red(glow),Color.green(glow),Color.blue(glow)));p.setStyle(Paint.Style.FILL);c.drawCircle(x,y,12*m,p);Path q=new Path();q.moveTo(x-7*m,y-4*m);q.lineTo(x-3*m,y-8*m);q.lineTo(x+7*m,y-6*m);q.lineTo(x+9*m,y+4*m);q.lineTo(x+2*m,y+8*m);q.lineTo(x-8*m,y+6*m);q.close();p.setColor(col);c.drawPath(q,p);rect(c,x-4*m,y-7*m,x+4*m,y-4*m,light(col));rect(c,x-7*m,y+2*m,x-2*m,y+5*m,dark(col));if(tier>0){center(c,""+(tier+1),x,y+3,Math.max(6,7*m),Color.WHITE);}}
        int light(int c){return Color.rgb(Math.min(255,Color.red(c)+65),Math.min(255,Color.green(c)+65),Math.min(255,Color.blue(c)+65));}
        int dark(int c){return Color.rgb(Color.red(c)/2,Color.green(c)/2,Color.blue(c)/2);}

        void drawControls(Canvas c){
            float y=1040;rect(c,0,y,HW,HH,bg);
            // hero mining control
            round(c,16,y+8,752,y+104,18,Color.rgb(9,28,47));stroke(c,16,y+8,752,y+104,Color.rgb(30,116,168),2);
            round(c,258,y+16,510,y+96,40,Color.rgb(16,113,166));stroke(c,258,y+16,510,y+96,Color.rgb(73,226,255),3);center(c,"MINE",384,y+51,24,white);center(c,"+"+power+" ORE  •  TAP",384,y+76,9,Color.rgb(184,240,255));
            if(combo>1){round(c,31,y+22,190,y+78,14,Color.rgb(24,45,65));txt(c,"COMBO",45,y+43,9,Color.rgb(126,161,190));txt(c,"x"+combo,45,y+67,20,Color.rgb(255,111,223));rect(c,94,y+63,180,y+68,Color.rgb(255,190,49));}
            round(c,535,y+22,737,y+78,14,Color.rgb(12,39,60));txt(c,"AUTO MINER",552,y+43,9,Color.rgb(125,160,191));txt(c,"LV "+auto,552,y+65,15,Color.rgb(119,227,255));
            float by=y+116,bw=232;
            card(c,18,by,18+bw,"UPGRADES","TREE",Color.rgb(255,100,220));card(c,268,by,268+bw,"SELL","$"+fmt(sellValue()),goldCol);card(c,518,by,518+bw,"SATCHEL",oreCount+"/"+backpack,Color.rgb(158,128,255));
            txt(c,"DEPTH MILESTONE  •  EVERY 25 TAPS",18,by+93,8,Color.rgb(105,139,171));
            txt(c,"BEST COMBO  "+bestCombo,518,by+93,8,Color.rgb(105,139,171));
            // bottom nav
            float ny=1260;rect(c,12,ny,756,HH,Color.rgb(6,14,26));String[] nav={"HOME","UPGRADES","INVENTORY","SHOP","ACHIEVEMENTS","SETTINGS"};String[] ico={"⌂","↑","◆","▣","★","⚙"};for(int i=0;i<6;i++){float l=14+i*124;round(c,l,ny+5,l+120,HH-5,10,i==0?Color.rgb(11,70,103):Color.rgb(8,22,37));if(i==0)stroke(c,l,ny+5,l+120,HH-5,cyan,1);center(c,ico[i],l+60,ny+29,18,i==0?cyan:Color.rgb(176,201,225));center(c,nav[i],l+60,ny+50,7,i==0?white:Color.rgb(119,148,177));}
        }
        void card(Canvas c,float l,float t,float r,String a,String b,int col){round(c,l,t,r,t+68,13,panel2);stroke(c,l,t,r,t+68,Color.rgb(24,57,83),1);txt(c,a,l+11,t+23,8,Color.rgb(124,154,184));txt(c,b,l+11,t+50,16,col);}

        void drawUpgrades(Canvas c){rect(c,0,0,HW,HH,Color.argb(215,0,2,8));float l=18,r=750,t=116,b=1240;round(c,l,t,r,b,22,Color.rgb(6,15,27));stroke(c,l,t,r,b,Color.rgb(43,137,193),2);txt(c,"UPGRADE TREE",l+20,t+35,23,white);txt(c,"12 SPECIALIZATIONS  •  PAGE "+(upgradePage+1)+"/3",l+20,t+57,9,Color.rgb(126,159,188));int start=upgradePage*4;for(int i=0;i<4;i++)upgradeRow(c,start+i,t+78+i*105);round(c,l+18,b-53,l+85,b-16,12,panel2);round(c,350,b-53,418,b-16,12,panel2);round(c,r-85,b-53,r-18,b-16,12,panel2);center(c,"<",51,b-27,17,white);center(c,"CLOSE",384,b-27,8,white);center(c,">",717,b-27,17,white);}
        void upgradeRow(Canvas c,int id,float y){int lv=level(id);long co=cost(id);int col=upgradeColors[id];round(c,32,y,736,y+88,15,panel2);round(c,43,y+13,91,y+75,13,Color.argb(45,Color.red(col),Color.green(col),Color.blue(col)));center(c,""+lv,67,y+50,20,col);txt(c,upgradeNames[id],106,y+31,14,white);txt(c,upgradeDesc[id],106,y+54,9,Color.rgb(133,163,190));txt(c,"NEXT LVL",106,y+72,7,Color.rgb(87,125,157));round(c,610,y+18,716,y+67,13,co<=coins?Color.rgb(21,91,87):Color.rgb(30,42,55));center(c,"$"+fmt(co),663,y+48,12,co<=coins?goldCol:Color.rgb(128,145,164));}
        void drawBag(Canvas c){rect(c,0,0,HW,HH,Color.argb(205,0,2,8));float l=30,r=738,t=170,b=1125;round(c,l,t,r,b,22,Color.rgb(6,15,27));stroke(c,l,t,r,b,Color.rgb(43,137,193),2);txt(c,"ORE SATCHEL",l+22,t+40,23,white);txt(c,"COMBINE • COLLECT • SELL",l+22,t+63,9,Color.rgb(128,161,190));rowBag(c,t+90,"COPPER",copper,10,0);rowBag(c,t+150,"IRON",iron,25,1);rowBag(c,t+210,"GOLD",gold,80,2);rowBag(c,t+270,"CRYSTAL",crystal,200,3);txt(c,"TOTAL SELL VALUE",l+22,t+350,9,Color.rgb(128,161,190));txt(c,"$"+fmt(sellValue()),l+22,t+385,28,goldCol);center(c,"TAP OUTSIDE TO CLOSE",384,b-20,9,Color.rgb(119,149,177));}
        void rowBag(Canvas c,float y,String name,int n,int val,int type){round(c,48,y,720,y+48,12,panel2);drawOre(c,68,y+24,type,0,.8f);txt(c,name,91,y+20,10,white);txt(c,"x"+n,91,y+38,9,Color.rgb(145,171,198));txt(c,"$"+fmt((long)n*val*value),600,y+29,12,goldCol);}

        int level(int id){switch(id){case 0:return power;case 1:return speed;case 2:return width;case 3:return value;case 4:return crit;case 5:return luck;case 6:return magnet;case 7:return Math.max(1,(backpack-15)/15);case 8:return auto;case 9:return comboLevel;case 10:return deep;default:return offline;}}
        long cost(int id){int lv=level(id);long base=new long[]{35,55,80,100,140,175,220,260,350,425,500,650}[id];return Math.min(Long.MAX_VALUE/4,base*(long)(lv+1)*(lv+1));}
        void buy(int id){long co=cost(id);if(coins<co)return;coins-=co;switch(id){case 0:power++;break;case 1:speed++;break;case 2:width++;break;case 3:value++;break;case 4:crit++;break;case 5:luck++;break;case 6:magnet++;break;case 7:backpack+=15;break;case 8:auto++;break;case 9:comboLevel++;break;case 10:deep++;break;case 11:offline++;break;}flash=1;invalidate();vibrate();}
        long sellValue(){return (long)(copper*10+iron*25+gold*80+crystal*200)*value;}
        int randomOre(){int roll=rng.nextInt(100)+luck*2;if(depth<6)return roll<68?0:roll<94?1:2;if(depth<18)return roll<45?0:roll<75?1:roll<97?2:3;return roll<25?0:roll<54?1:roll<86?2:3;}
        void addOre(int type){if(type==0)copper++;else if(type==1)iron++;else if(type==2)gold++;else crystal++;}
        void doMine(float tx,float ty){if(upgrades||bag)return;totalTaps++;if(now()-lastTap<1400)combo++;else combo=1;lastTap=now();bestCombo=Math.max(bestCombo,combo);int gain=power;if(combo>3)gain+=Math.min(comboLevel,combo/4);if(rng.nextInt(100)<Math.min(50,crit*3))gain*=3;int free=backpack-oreCount;gain=Math.min(gain,Math.max(0,free));for(int i=0;i<gain;i++)drops.add(new OreDrop(tx+(float)(Math.random()*22-11),ty+(float)(Math.random()*14-7),randomOre(),0,1f));beamX=tx;beamY=ty;beamAlpha=1f;flash=1f;for(int i=0;i<10+width*2;i++)sparks.add(new Spark(tx,ty,i%2==0?cyan:goldCol));long reward=(long)gain*(1+depth/10+deep/5);if(combo>1)reward+=combo*comboLevel;coins+=reward;lifetime+=reward;if(totalTaps%25==0){depth++;coins+=25L*depth*deep;for(int i=0;i<16;i++)sparks.add(new Spark(384,650,Color.rgb(255,230,112)));}vibrate();}
        long now(){return System.currentTimeMillis();}
        void sell(){if(oreCount<=0)return;long v=sellValue();coins+=v;lifetime+=v;copper=iron=gold=crystal=oreCount=0;for(int i=0;i<18;i++)sparks.add(new Spark(640,1190,goldCol));flash=1;vibrate();}
        void vibrate(){try{Vibrator v=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);if(v!=null)v.vibrate(18);}catch(Exception ignored){}}

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX()/S,y=e.getY()/S;
            if(upgrades){float top=116,bottom=1240;if(y>=bottom-70){if(x<120)upgradePage=(upgradePage+2)%3;else if(x>648)upgradePage=(upgradePage+1)%3;else upgrades=false;invalidate();return true;}int start=upgradePage*4;for(int i=0;i<4;i++){float yy=top+78+i*105;if(y>=yy&&y<=yy+88){buy(start+i);return true;}}return true;}
            if(bag){bag=false;invalidate();return true;}
            float by=1156;
            if(y>=by&&y<=by+68){if(x<250){upgrades=true;invalidate();return true;}if(x<500){sell();invalidate();return true;}bag=true;invalidate();return true;}
            if(y<100&&x>690){bag=true;invalidate();return true;}
            if(y>=112&&y<=1035)doMine(x,y);else if(y>=1040&&y<1150)doMine(384,y);return true;
        }
    }
}

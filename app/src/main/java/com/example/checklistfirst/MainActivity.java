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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    MineView game;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(1024, 1024);
        game = new MineView(this);
        setContentView(game);
    }

    @Override protected void onPause() { super.onPause(); if (game != null) game.save(); }
    @Override protected void onResume() { super.onResume(); if (game != null) game.applyOffline(); }

    static class OreDrop {
        float x, y, vx, vy, size;
        int type;
        boolean alive = true;
        OreDrop(float x, float y, int type, float size) {
            this.x=x; this.y=y; this.type=type; this.size=size;
            vx=(float)(Math.random()*2.4-1.2); vy=-(float)(Math.random()*3+2);
        }
    }

    static class Spark { float x,y,vx,vy,life; int color; Spark(float x,float y,int c){this.x=x;this.y=y;color=c;life=1f;vx=(float)(Math.random()*5-2.5);vy=(float)(Math.random()*5-3.5);} }

    static class MineView extends View {
        Paint p = new Paint();
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        Handler handler = new Handler(Looper.getMainLooper());
        Random rng = new Random(71);
        SharedPreferences prefs;
        List<OreDrop> drops = new ArrayList<>();
        List<Spark> sparks = new ArrayList<>();
        long lastFrame = System.currentTimeMillis();
        long lastSave = 0;
        long lastTap = 0;
        long coins = 0;
        long totalTaps = 0;
        long lifetimeCoins = 0;
        int depth = 1;
        int miningPower = 1;
        int beamWidth = 1;
        int miningSpeed = 1;
        int backpack = 30;
        int oreCount = 0;
        int combo = 0;
        int bestCombo = 0;
        int oreCopper=0, oreIron=0, oreGold=0, oreCrystal=0;
        float beamX=0, beamY=0, beamAlpha=0;
        float minerBob=0;
        float drillPulse=0;
        float scroll=0;
        long passiveCarry=0;
        long lastTime;
        boolean shop=false;
        boolean bag=false;
        int w,h;

        int bg = Color.rgb(5,9,18), panel=Color.rgb(12,20,34), panel2=Color.rgb(18,29,48);
        int cyan=Color.rgb(69,210,255), blue=Color.rgb(48,126,230), gold=Color.rgb(255,193,57), white=Color.rgb(235,244,255);
        int[] oreColors={Color.rgb(202,102,53),Color.rgb(164,174,190),Color.rgb(255,192,53),Color.rgb(83,211,255)};

        MineView(Context c){
            super(c); setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            p.setAntiAlias(false); text.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
            prefs=c.getSharedPreferences("mine_save",Context.MODE_PRIVATE);
            load();
            handler.post(tick);
        }

        void load(){
            coins=prefs.getLong("coins",0); totalTaps=prefs.getLong("taps",0); lifetimeCoins=prefs.getLong("life",0);
            depth=prefs.getInt("depth",1); miningPower=prefs.getInt("power",1); beamWidth=prefs.getInt("width",1); miningSpeed=prefs.getInt("speed",1);
            backpack=prefs.getInt("bag",30); oreCount=prefs.getInt("oreCount",0); combo=prefs.getInt("combo",0); bestCombo=prefs.getInt("best",0);
            oreCopper=prefs.getInt("copper",0); oreIron=prefs.getInt("iron",0); oreGold=prefs.getInt("gold",0); oreCrystal=prefs.getInt("crystal",0);
            lastTime=prefs.getLong("time",System.currentTimeMillis());
        }
        void save(){
            lastTime=System.currentTimeMillis();
            prefs.edit().putLong("coins",coins).putLong("taps",totalTaps).putLong("life",lifetimeCoins).putInt("depth",depth)
                .putInt("power",miningPower).putInt("width",beamWidth).putInt("speed",miningSpeed).putInt("bag",backpack)
                .putInt("oreCount",oreCount).putInt("combo",combo).putInt("best",bestCombo).putInt("copper",oreCopper).putInt("iron",oreIron)
                .putInt("gold",oreGold).putInt("crystal",oreCrystal).putLong("time",lastTime).apply();
            lastSave=lastTime;
        }
        void applyOffline(){
            long now=System.currentTimeMillis(); long sec=Math.max(0,Math.min(7200,(now-lastTime)/1000));
            long earned=sec*(miningPower+miningSpeed*2)/2;
            if(earned>0){coins+=earned; lifetimeCoins+=earned; passiveCarry=earned;}
            lastTime=now; invalidate();
        }

        Runnable tick=new Runnable(){@Override public void run(){
            long now=System.currentTimeMillis(); float dt=Math.min(.05f,(now-lastFrame)/1000f); lastFrame=now;
            minerBob+=dt*5; drillPulse+=dt*8; scroll+=dt*(0.8f+depth*.025f);
            if(beamAlpha>0) beamAlpha-=dt*5;
            long passive=(long)((miningPower+miningSpeed*2)*dt/3f);
            if(passive>0){coins+=passive;lifetimeCoins+=passive;}
            for(OreDrop d:drops){ if(!d.alive)continue; d.vy+=dt*18; d.x+=d.vx; d.y+=d.vy; if(d.y>h*.73f){d.y=h*.73f;d.vy*= -.18f;d.vx*=.88f;} }
            for(Spark s:sparks){s.life-=dt*2.8f;s.x+=s.vx;s.y+=s.vy;s.vy+=dt*8;}
            for(int i=drops.size()-1;i>=0;i--)if(!drops.get(i).alive)drops.remove(i);
            for(int i=sparks.size()-1;i>=0;i--)if(sparks.get(i).life<=0)sparks.remove(i);
            if(now-lastSave>8000)save();
            invalidate(); handler.postDelayed(this,16);
        }};

        @Override protected void onDraw(Canvas c){super.onDraw(c);w=getWidth();h=getHeight();
            c.drawColor(bg); drawBackdrop(c); drawHeader(c); drawMine(c); drawControls(c); if(shop)drawShop(c); if(bag)drawBag(c);
        }
        void rect(Canvas c,float l,float t,float r,float b,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(l,t,r,b,p);}
        void stroke(Canvas c,float l,float t,float r,float b,int color,float sw){p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);c.drawRect(l,t,r,b,p);p.setStyle(Paint.Style.FILL);}
        void txt(Canvas c,String s,float x,float y,float size,int color){text.setTextSize(size);text.setColor(color);text.setTextAlign(Paint.Align.LEFT);c.drawText(s,x,y,text);}
        void center(Canvas c,String s,float x,float y,float size,int color){text.setTextSize(size);text.setColor(color);text.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y,text);}
        void round(Canvas c,float l,float t,float r,float b,float rad,int color){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p);}

        void drawBackdrop(Canvas c){
            for(int i=0;i<12;i++){float yy=(i*170-(scroll*.35f)%170);int shade=Color.rgb(7+i%3*2,12+i%4*2,23+i%5*2);rect(c,0,yy,w,yy+170,shade);}
            for(int i=0;i<28;i++){float x=(i*83+31)%w;float y=(i*137+(scroll*.7f))%(h*.72f);rect(c,x,y,x+3,y+3,Color.rgb(30,48,70));}
        }
        void drawHeader(Canvas c){
            rect(c,0,0,w,104,Color.rgb(7,14,27)); rect(c,0,100,w,104,blue);
            txt(c,"PIXEL MINE",22,37,25,white); txt(c,"TYCOON",22,67,18,cyan);
            panelStat(c,170,14,310,88,"COINS",format(coins),gold); panelStat(c,322,14,462,88,"DEPTH",""+depth+"m",cyan);
            round(c,w-118,18,w-18,86,16,panel2); center(c,"BAG",w-68,44,12,white); center(c,oreCount+"/"+backpack,w-68,70,17,cyan);
        }
        void panelStat(Canvas c,float l,float t,float r,float b,String label,String value,int color){round(c,l,t,r,b,14,panel);txt(c,label,l+12,t+22,10,Color.rgb(140,163,188));txt(c,value,l+12,t+53,20,color);}

        void drawMine(Canvas c){
            float top=120,bottom=h*.68f; round(c,12,top,w-12,bottom,18,Color.rgb(10,17,29)); stroke(c,12,top,w-12,bottom,Color.rgb(36,70,101),3);
            // strata
            int[] strata={Color.rgb(80,57,43),Color.rgb(70,70,78),Color.rgb(57,63,78),Color.rgb(78,53,49),Color.rgb(45,58,75)};
            for(int i=0;i<5;i++){float sy=top+45+i*88+(scroll%88);rect(c,15,sy,w-15,sy+88,strata[(i+depth)%strata.length]);}
            // pixel rock pattern
            for(int i=0;i<60;i++){float x=22+(i*97)%((int)(w-45));float y=top+35+((i*53+(int)scroll*2)%((int)(bottom-top-55)));int col=(i+depth)%4==0?Color.rgb(105,91,87):Color.rgb(53,58,68);rect(c,x,y,x+7,y+5,col);if(i%3==0)rect(c,x+10,y+7,x+14,y+10,col);}
            // ore clusters
            for(int i=0;i<14;i++){float x=35+(i*137)%((int)(w-70));float y=top+60+((i*91+(int)scroll*1)%((int)(bottom-top-75)));int oc=(i+depth)%4;drawOreCluster(c,x,y,oc,1.0f);}
            // miner and beam origin
            float mx=w*.5f,my=bottom-76+(float)Math.sin(minerBob)*2;
            drawMiner(c,mx,my);
            if(beamAlpha>0){drawBeam(c,mx,my-28,beamX,beamY,beamAlpha);}
            for(OreDrop d:drops)drawOre(c,d.x,d.y,d.type,d.size);
            for(Spark s:sparks){p.setAlpha((int)(255*Math.max(0,s.life)));rect(c,s.x-2,s.y-2,s.x+4,s.y+4,s.color);p.setAlpha(255);}
            center(c,"TAP ANYWHERE IN THE MINE TO FIRE",w/2,bottom-10,11,Color.rgb(154,176,201));
        }
        void drawMiner(Canvas c,float x,float y){
            // shadow
            round(c,x-27,y+27,x+27,y+37,5,Color.argb(100,0,0,0));
            // legs/boots
            rect(c,x-17,y+11,x-5,y+31,Color.rgb(34,41,52));rect(c,x+5,y+11,x+17,y+31,Color.rgb(34,41,52));rect(c,x-19,y+27,x-3,y+33,Color.rgb(18,22,28));rect(c,x+3,y+27,x+19,y+33,Color.rgb(18,22,28));
            // body
            rect(c,x-20,y-10,x+20,y+15,Color.rgb(29,104,139));rect(c,x-16,y-6,x+16,y+12,Color.rgb(37,135,164));rect(c,x-4,y-2,x+4,y+6,Color.rgb(248,188,62));
            // arms
            rect(c,x-28,y-6,x-18,y+12,Color.rgb(246,178,56));rect(c,x+18,y-6,x+28,y+12,Color.rgb(246,178,56));
            // head
            rect(c,x-15,y-29,x+15,y-8,Color.rgb(248,191,80));rect(c,x-12,y-26,x+12,y-11,Color.rgb(255,211,120));
            // helmet
            rect(c,x-19,y-35,x+19,y-27,Color.rgb(244,178,38));rect(c,x-13,y-40,x+14,y-33,Color.rgb(255,205,49));rect(c,x-22,y-30,x+22,y-26,Color.rgb(215,139,22));rect(c,x-4,y-38,x+5,y-34,Color.rgb(255,232,119));
            // lamp
            rect(c,x-4,y-35,x+5,y-30,Color.rgb(240,250,255));
            // pickaxe
            p.setColor(Color.rgb(196,211,224));p.setStrokeWidth(4);p.setStyle(Paint.Style.STROKE);c.drawLine(x+20,y+3,x+37,y-20,p);c.drawLine(x+29,y-22,x+42,y-14,p);p.setStyle(Paint.Style.FILL);
        }
        void drawBeam(Canvas c,float x1,float y1,float x2,float y2,float alpha){
            int a=(int)(210*alpha);float dx=x2-x1,dy=y2-y1,len=(float)Math.sqrt(dx*dx+dy*dy);if(len<1)return;float nx=-dy/len,ny=dx/len;float ww=5+beamWidth*4;
            p.setStrokeCap(Paint.Cap.SQUARE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(ww*2);p.setColor(Color.argb(a/3,40,190,255));c.drawLine(x1,y1,x2,y2,p);p.setStrokeWidth(ww);p.setColor(Color.argb(a,65,215,255));c.drawLine(x1,y1,x2,y2,p);p.setStrokeWidth(Math.max(2,ww/3));p.setColor(Color.argb(255,230,252,255));c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);
            for(int i=1;i<5;i++){float q=i/5f;float xx=x1+dx*q,yy=y1+dy*q;rect(c,xx-2,yy-2,xx+2,yy+2,Color.argb(a,255,255,255));}
        }
        void drawOreCluster(Canvas c,float x,float y,int type,float s){for(int i=0;i<5;i++){float ox=(i%3-1)*8*s,oy=(i/3-1)*7*s;drawOre(c,x+ox,y+oy,type,s*.9f);}}
        void drawOre(Canvas c,float x,float y,int type,float s){int col=oreColors[type];rect(c,x-6*s,y-5*s,x+6*s,y+6*s,col);rect(c,x-3*s,y-8*s,x+4*s,y-4*s,light(col));rect(c,x-4*s,y+2*s,x+2*s,y+5*s,dark(col));rect(c,x+2*s,y-2*s,x+5*s,y+1*s,light(col));}
        int light(int c){return Color.rgb(Math.min(255,Color.red(c)+55),Math.min(255,Color.green(c)+55),Math.min(255,Color.blue(c)+55));}
        int dark(int c){return Color.rgb(Color.red(c)/2,Color.green(c)/2,Color.blue(c)/2);}

        void drawControls(Canvas c){
            float y=h*.70f; rect(c,0,y,w,h,bg);
            // primary mine button
            round(c,22,y+12,w-22,y+116,22,Color.rgb(17,38,61));stroke(c,22,y+12,w-22,y+116,Color.rgb(50,117,171),3);
            round(c,w*.5f-76,y+23,w*.5f+76,y+103,40,Color.rgb(28,122,178));stroke(c,w*.5f-76,y+23,w*.5f+76,y+103,Color.rgb(99,224,255),3);
            center(c,"MINE",w/2,y+59,24,white);center(c,"+"+miningPower+" ORE",w/2,y+84,11,Color.rgb(176,234,255));
            // lower action cards
            float by=y+130,bw=(w-70)/3f;
            button(c,20,by,20+bw,by+82,"SPEED","Lv "+miningSpeed,cyan);
            button(c,35+bw,by,35+2*bw,by+82,"BEAM","Lv "+beamWidth,gold);
            button(c,50+2*bw,by,50+3*bw,by+82,"BAG",""+oreCount+"/"+backpack,Color.rgb(151,116,255));
            txt(c,"SELL ORE",20,by+107,11,Color.rgb(139,161,185));txt(c,"+"+sellValue()+" coins",20,by+126,18,gold);
            round(c,w-160,by+92,w-20,by+134,14,Color.rgb(24,65,63));center(c,"SELL",w-90,by+119,14,Color.rgb(133,255,221));
            if(combo>1){center(c,"COMBO x"+combo,w/2,by+112,16,Color.rgb(255,116,238));}
        }
        void button(Canvas c,float l,float t,float r,float b,String a,String btxt,int color){round(c,l,t,r,b,15,panel2);stroke(c,l,t,r,b,Color.rgb(43,74,105),2);txt(c,a,l+12,t+27,10,Color.rgb(139,161,185));txt(c,btxt,l+12,t+58,18,color);}

        void drawShop(Canvas c){
            rect(c,0,0,w,h,Color.argb(190,0,0,0));float l=28,r=w-28,t=145,b=h-110;round(c,l,t,r,b,22,Color.rgb(10,18,31));stroke(c,l,t,r,b,Color.rgb(61,133,188),3);
            txt(c,"UPGRADE BAY",l+24,t+43,23,white);txt(c,"Spend coins to accelerate your mine",l+24,t+67,11,Color.rgb(144,170,197));
            shopRow(c,l+18,t+92,"MINING POWER","+1 ore per tap",powerCost(),0,cyan);
            shopRow(c,l+18,t+180,"MINING SPEED","more passive coins",speedCost(),1,gold);
            shopRow(c,l+18,t+268,"BEAM WIDTH","larger mining hit",widthCost(),2,Color.rgb(255,95,205));
            shopRow(c,l+18,t+356,"BACKPACK","+15 ore capacity",bagCost(),3,Color.rgb(132,113,255));
            center(c,"TAP OUTSIDE TO CLOSE",w/2,b-25,11,Color.rgb(128,152,180));
        }
        void shopRow(Canvas c,float x,float y,String title,String sub,long cost,int id,int color){float r=w-46;round(c,x,y,r,y+70,16,panel2);txt(c,title,x+16,y+24,13,white);txt(c,sub,x+16,y+45,10,Color.rgb(136,161,188));round(c,r-120,y+11,r-12,y+59,13,cost<=coins?Color.rgb(29,91,93):Color.rgb(41,49,61));center(c,"$"+format(cost),r-66,y+41,13,cost<=coins?gold:Color.rgb(132,145,163));}
        void drawBag(Canvas c){
            rect(c,0,0,w,h,Color.argb(180,0,0,0));float l=26,r=w-26,t=165,b=h-125;round(c,l,t,r,b,22,Color.rgb(10,18,31));txt(c,"ORE SATCHEL",l+22,t+43,23,white);txt(c,"matching pieces merge into richer chunks",l+22,t+65,10,Color.rgb(137,163,190));
            bagRow(c,l+20,t+94,"COPPER",oreCopper,10,oreColors[0]);bagRow(c,l+20,t+158,"IRON",oreIron,25,oreColors[1]);bagRow(c,l+20,t+222,"GOLD",oreGold,80,oreColors[2]);bagRow(c,l+20,t+286,"CRYSTAL",oreCrystal,200,oreColors[3]);
            txt(c,"SELL VALUE",l+22,t+374,11,Color.rgb(136,161,188));txt(c,"$"+sellValue(),l+22,t+405,28,gold);center(c,"TAP OUTSIDE TO CLOSE",w/2,b-25,11,Color.rgb(128,152,180));
        }
        void bagRow(Canvas c,float x,float y,String name,int count,int value,int color){round(c,x,y,w-46,y+54,14,panel2);drawOreCluster(c,x+30,y+27, name.equals("COPPER")?0:name.equals("IRON")?1:name.equals("GOLD")?2:3,.8f);txt(c,name,x+65,y+23,13,white);txt(c,"x"+count,x+65,y+42,11,Color.rgb(149,171,198));txt(c,"$"+(count*value),w-110,y+32,13,gold);}

        long powerCost(){return 35L*miningPower*miningPower;}
        long speedCost(){return 50L*miningSpeed*miningSpeed;}
        long widthCost(){return 90L*beamWidth*beamWidth;}
        long bagCost(){return 120L*((backpack-15)/15);}
        int sellValue(){return oreCopper*10+oreIron*25+oreGold*80+oreCrystal*200;}
        String format(long n){if(n>=1000000)return String.format("%.1fM",n/1000000f);if(n>=1000)return String.format("%.1fK",n/1000f);return ""+n;}

        void doMine(float tx,float ty){
            if(shop||bag)return;
            totalTaps++;combo++;bestCombo=Math.max(bestCombo,combo);lastTap=System.currentTimeMillis();
            int gain=Math.max(1,miningPower+(combo>10?1:0));
            int free=backpack-oreCount; if(free>0){gain=Math.min(gain,free);for(int i=0;i<gain;i++)addOre(randomOre());oreCount+=gain;}
            coins+=gain*(1+depth/12);lifetimeCoins+=gain*(1+depth/12);
            beamX=tx;beamY=ty;beamAlpha=1f;
            drillPulse=0;for(int i=0;i<7+beamWidth*2;i++)sparks.add(new Spark(tx,ty, i%3==0?gold:cyan));
            for(int i=0;i<Math.min(3,gain+1);i++)drops.add(new OreDrop(tx+(float)(Math.random()*18-9),ty+(float)(Math.random()*18-9),randomOre(),1f+Math.min(1.5f,depth/30f)));
            if(totalTaps%25==0){depth++;coins+=25*depth;for(int i=0;i<8;i++)sparks.add(new Spark(w/2,h*.55f,Color.rgb(255,230,112)));}
            if(System.currentTimeMillis()-lastTap>1300)combo=1;
            if(combo%12==0)coins+=combo*2;
            vibrate();
        }
        int randomOre(){int roll=rng.nextInt(100);if(depth<5)return roll<70?0:roll<95?1:2;if(depth<15)return roll<48?0:roll<78?1:roll<96?2:3;return roll<28?0:roll<58?1:roll<88?2:3;}
        void addOre(int t){if(t==0)oreCopper++;else if(t==1)oreIron++;else if(t==2)oreGold++;else oreCrystal++;}
        void sell(){if(oreCount<=0)return;int v=sellValue();coins+=v;lifetimeCoins+=v;oreCopper=oreIron=oreGold=oreCrystal=0;oreCount=0;for(int i=0;i<12;i++)sparks.add(new Spark(w*.75f,h*.82f,gold));vibrate();}
        void buy(int id){long cost=id==0?powerCost():id==1?speedCost():id==2?widthCost():bagCost();if(coins<cost)return;coins-=cost;if(id==0)miningPower++;else if(id==1)miningSpeed++;else if(id==2)beamWidth++;else backpack+=15;vibrate();}
        void vibrate(){try{Vibrator v=(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);if(v!=null&&v.hasVibrator())v.vibrate(VibrationEffect.createOneShot(18,VibrationEffect.DEFAULT_AMPLITUDE));}catch(Exception ignored){}}

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX(),y=e.getY();
            if(shop){if(y>237&&y<307){buy(0);return true;}if(y>325&&y<397){buy(1);return true;}if(y>413&&y<485){buy(2);return true;}if(y>501&&y<573){buy(3);return true;}shop=false;invalidate();return true;}
            if(bag){bag=false;invalidate();return true;}
            float cy=h*.70f;if(y>=cy+130&&y<=cy+212){float bw=(w-70)/3f;if(x<20+bw){shop=true;invalidate();return true;}if(x<35+2*bw){shop=true;invalidate();return true;}if(x<50+3*bw){bag=true;invalidate();return true;}}
            if(y>=cy+92&&y<=cy+134&&x>w-180){sell();return true;}
            if(y<105&&x>w-135){bag=true;invalidate();return true;}
            doMine(x,y);invalidate();return true;
        }
    }
}

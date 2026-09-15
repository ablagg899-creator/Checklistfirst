package com.example.checklistfirst;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("Tap Galaxy");
        setContentView(new TapView(this));
    }

    static class TapView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rng = new Random();
        final SharedPreferences save;
        final ToneGenerator sound = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
        final Vibrator vibrator;
        final RectF orb = new RectF(), golden = new RectF(), boss = new RectF();
        final String[] names = {"POWER GLOVE","AUTO BOT","CRIT CORE","COMBO ENGINE","COIN MAGNET","STAR REACTOR","DRONE SWARM","QUANTUM TAP"};
        final String[] desc = {"+1 tap power","+2 coins / sec","+5% crit chance","+0.5x combo cap","+10% all earnings","+10 coins / sec","+25 coins / sec","+2 tap power"};
        final int[] base = {25,80,150,300,700,1800,5000,15000};
        float density, tapScale=1f, fever=0, combo=1f;
        long coins,total,lifetime,lastFrame,lastSave,lastGolden,lastDaily,lastBoss,lastTap,playSeconds;
        long comboUntil,eventUntil,goldenUntil,bossUntil;
        int tapPower=1,autoLevel,critLevel,comboLevel,magnetLevel,reactorLevel,swarmLevel,quantumLevel;
        int prestige,achievements,taps,critTaps,biggestCrit,highestCombo,upgradesBought,collectibles;
        int questDay,questTaps,questUpgrades,questEarned;
        int eventType=-1,bossHits,bossNeed;
        boolean pulse,soundOn=true,frenzy,bossActive;
        int screen=0;
        String[] floats=new String[18]; float[] fx=new float[18],fy=new float[18]; long[] ft=new long[18];

        TapView(Context c) {
            super(c); density=getResources().getDisplayMetrics().density;
            save=c.getSharedPreferences("tap_galaxy",Context.MODE_PRIVATE); vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            coins=save.getLong("coins",0); total=save.getLong("total",0); lifetime=save.getLong("lifetime",0);
            tapPower=save.getInt("tap",1); autoLevel=save.getInt("auto",0); critLevel=save.getInt("crit",0); comboLevel=save.getInt("combo",0);
            magnetLevel=save.getInt("magnet",0); reactorLevel=save.getInt("reactor",0); swarmLevel=save.getInt("swarm",0); quantumLevel=save.getInt("quantum",0);
            prestige=save.getInt("prestige",0); achievements=save.getInt("achievements",0); taps=save.getInt("taps",0); critTaps=save.getInt("critTaps",0);
            biggestCrit=save.getInt("biggestCrit",0); highestCombo=save.getInt("highestCombo",10); upgradesBought=save.getInt("upgradesBought",0); collectibles=save.getInt("collectibles",0);
            questDay=save.getInt("questDay",0); questTaps=save.getInt("questTaps",0); questUpgrades=save.getInt("questUpgrades",0); questEarned=save.getInt("questEarned",0);
            lastDaily=save.getLong("daily",0); lastGolden=save.getLong("golden",0); lastBoss=save.getLong("boss",0); playSeconds=save.getLong("play",0);
            text.setTypeface(android.graphics.Typeface.create("sans",android.graphics.Typeface.BOLD)); setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            long now=System.currentTimeMillis(), closed=Math.max(0,(now-save.getLong("closed",now))/1000);
            if(closed>60 && perSecond()>0){long earned=Math.min(999999999999L,(long)(closed*perSecond()*multiplier())); coins+=earned; lifetime+=earned; total+=earned; Toast.makeText(c,"Offline earnings: +"+format(earned)+" coins",Toast.LENGTH_LONG).show();}
            int today=(int)(now/86400000L); if(today!=questDay){questDay=today;questTaps=questUpgrades=questEarned=0;}
            checkAchievements();
        }
        float d(float v){return v*density;} float W(){return getWidth();} float H(){return getHeight();}
        int level(int i){switch(i){case 0:return tapPower-1;case 1:return autoLevel;case 2:return critLevel;case 3:return comboLevel;case 4:return magnetLevel;case 5:return reactorLevel;case 6:return swarmLevel;default:return quantumLevel;}}
        long cost(int i){return (long)Math.min(9.22e18,base[i]*Math.pow(1.62,level(i)));}
        long perSecond(){return autoLevel*2L+reactorLevel*10L+swarmLevel*25L;}
        float multiplier(){return (1f+magnetLevel*.10f)*(1f+prestige*.25f)*(frenzy?10f:1f)*(eventType==0?2f:1f);}
        float comboCap(){return 1f+comboLevel*.5f;}
        void beep(int tone){if(soundOn)sound.startTone(tone,45);} void buzz(){if(vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(VibrationEffect.createOneShot(25,VibrationEffect.DEFAULT_AMPLITUDE));}
        void addFloat(String s,float x,float y){for(int i=0;i<floats.length;i++)if(ft[i]<SystemClock.elapsedRealtime()-600){floats[i]=s;fx[i]=x;fy[i]=y;ft[i]=SystemClock.elapsedRealtime();break;}}
        void addReward(long n){coins+=n;total+=n;lifetime+=n;questEarned+=Math.max(0,(int)Math.min(Integer.MAX_VALUE,n));}

        @Override protected void onDraw(Canvas c){
            long now=SystemClock.elapsedRealtime(); float dt=Math.min(.1f,lastFrame==0?0:(now-lastFrame)/1000f); lastFrame=now; playSeconds+=(long)dt;
            long auto=(long)(perSecond()*dt*multiplier()); if(auto>0)addReward(auto);
            if(frenzy&&now>eventUntil)frenzy=false; if(eventType>=0&&now>eventUntil)eventType=-1;
            if(bossActive&&now>bossUntil){bossActive=false;Toast.makeText(getContext(),"The boss escaped!",Toast.LENGTH_SHORT).show();}
            if(screen==0)drawGame(c); else if(screen==1)drawStats(c); else drawCollection(c);
            save(); postInvalidateDelayed(33);
        }
        void bg(Canvas c){c.drawColor(Color.rgb(7,9,25));p.setStyle(Paint.Style.FILL);p.setColor(frenzy?Color.rgb(45,15,70):Color.rgb(13,17,45));c.drawRect(0,0,W(),H(),p);p.setColor(frenzy?Color.rgb(100,25,110):Color.rgb(24,29,68));c.drawCircle(W()*.15f,H()*.22f,d(95),p);p.setColor(Color.rgb(17,23,58));c.drawCircle(W()*.88f,H()*.28f,d(120),p);p.setColor(Color.WHITE);for(int i=0;i<55;i++)c.drawCircle((i*97)%Math.max(1,getWidth()),(i*173+35)%Math.max(1,(int)(H()*.72f)),d(i%4==0?1.5f:.7f),p);}
        void header(Canvas c){p.setColor(Color.rgb(17,22,52));c.drawRoundRect(d(10),d(8),W()-d(10),d(78),d(18),d(18),p);text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(160,180,225));text.setTextSize(d(11));c.drawText("TAP GALAXY",d(22),d(27),text);text.setColor(Color.WHITE);text.setTextSize(d(23));c.drawText(format(coins),d(22),d(57),text);text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(d(9));text.setColor(Color.rgb(120,230,190));c.drawText("+"+format(perSecond())+" / SEC",W()-d(20),d(27),text);c.drawText("REBIRTHS "+prestige,W()-d(20),d(42),text);}
        void nav(Canvas c){float y=H()-d(46);p.setColor(Color.rgb(17,22,52));c.drawRect(0,y,W(),H(),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(10));String[] a={"GAME","STATS","COLLECT"};for(int i=0;i<3;i++){text.setColor(screen==i?Color.WHITE:Color.rgb(120,135,175));c.drawText(a[i],W()*(i+.5f)/3f,y+d(27),text);}}
        void drawGame(Canvas c){bg(c);header(c);long now=SystemClock.elapsedRealtime();
            // fever meter
            p.setColor(Color.rgb(30,35,70));c.drawRoundRect(d(25),d(88),W()-d(25),d(103),d(8),d(8),p);p.setColor(frenzy?Color.rgb(255,105,220):Color.rgb(245,175,65));c.drawRoundRect(d(25),d(88),d(25)+(W()-d(50))*Math.min(1,fever),d(103),d(8),d(8),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(9));text.setColor(Color.WHITE);c.drawText(frenzy?"TAP FRENZY ×10!":"FEVER METER",W()/2,d(100),text);
            float cy=H()*.28f,r=Math.min(W()*.29f,d(92))*tapScale;orb.set(W()/2-r,cy-r,W()/2+r,cy+r);p.setShadowLayer(d(30),0,d(5),Color.rgb(80,125,255));p.setColor(frenzy?Color.rgb(255,90,205):Color.rgb(65,105,240));c.drawCircle(W()/2,cy,r+d(13),p);p.clearShadowLayer();p.setColor(frenzy?Color.rgb(255,205,245):Color.rgb(105,220,255));c.drawCircle(W()/2,cy,r,p);p.setColor(Color.rgb(30,55,170));c.drawCircle(W()/2,cy,r*.77f,p);text.setTextAlign(Paint.Align.CENTER);text.setColor(Color.WHITE);text.setTextSize(d(19));c.drawText("TAP!",W()/2,cy+d(7),text);text.setTextSize(d(10));text.setColor(Color.rgb(190,225,255));c.drawText("POWER CORE",W()/2,cy+d(25),text);
            text.setTextSize(d(11));text.setColor(Color.rgb(235,200,90));c.drawText("COMBO ×"+String.format(Locale.US,"%.1f",combo),W()/2,cy+r+d(27),text);
            if(now-lastGolden>7000 && rng.nextInt(100)<2){lastGolden=now;goldenUntil=now+7000;}
            if(goldenUntil>now){float gx=W()*.78f,gy=H()*.22f;golden.set(gx-d(28),gy-d(28),gx+d(28),gy+d(28));p.setShadowLayer(d(22),0,0,Color.YELLOW);p.setColor(Color.rgb(255,205,55));c.drawCircle(gx,gy,d(24),p);p.clearShadowLayer();text.setColor(Color.WHITE);text.setTextSize(d(8));c.drawText("BONUS",gx,gy+d(3),text);}
            if(!bossActive && now-lastBoss>45000 && rng.nextInt(1000)<3){bossActive=true;bossNeed=30+prestige*10;bossHits=0;bossUntil=now+20000;lastBoss=now;}
            if(bossActive){float by=H()*.39f;p.setColor(Color.rgb(100,25,50));c.drawRoundRect(d(20),by,W()-d(20),by+d(38),d(12),d(12),p);text.setColor(Color.WHITE);text.setTextSize(d(11));c.drawText("BOSS  "+bossHits+" / "+bossNeed,W()/2,by+d(16),text);text.setTextSize(d(8));c.drawText("TAP TO DEFEAT!",W()/2,by+d(30),text);}
            float sy=bossActive?H()*.48f:H()*.42f;p.setColor(Color.rgb(17,23,54));c.drawRoundRect(d(12),sy,W()-d(12),sy+d(52),d(14),d(14),p);text.setTextSize(d(9));text.setColor(Color.rgb(145,160,205));String[] s={"TAPS","CRIT","HIGHEST COMBO","MEDALS"};float[] xs={.15f,.38f,.64f,.86f};for(int i=0;i<4;i++)c.drawText(s[i],W()*xs[i],sy+d(18),text);text.setTextSize(d(13));text.setColor(Color.WHITE);c.drawText(format(taps),W()*.15f,sy+d(40),text);c.drawText(critTaps+"",W()*.38f,sy+d(40),text);c.drawText("x"+String.format(Locale.US,"%.1f",highestCombo/10f),W()*.64f,sy+d(40),text);c.drawText(achievements+"/12",W()*.86f,sy+d(40),text);
            drawUpgrades(c,sy+d(62)); drawFloats(c);nav(c);
        }
        void drawUpgrades(Canvas c,float top){text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.WHITE);text.setTextSize(d(14));c.drawText("UPGRADE SHOP",d(18),top,text);float y=top+d(7),gap=d(4),h=Math.max(d(30),Math.min(d(38),(H()-y-d(53)-gap*7)/8f));for(int i=0;i<8;i++){float yy=y+i*(h+gap);p.setColor(Color.rgb(20,27,61));c.drawRoundRect(d(9),yy,W()-d(9),yy+h,d(9),d(9),p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(d(8));text.setColor(Color.WHITE);c.drawText(names[i]+" LV"+level(i),d(18),yy+d(14),text);text.setColor(Color.rgb(150,170,210));c.drawText(desc[i],d(18),yy+d(26),text);long price=cost(i);p.setColor(coins>=price?Color.rgb(42,165,112):Color.rgb(53,60,86));c.drawRoundRect(W()-d(80),yy+d(4),W()-d(11),yy+h-d(4),d(7),d(7),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(7));text.setColor(Color.WHITE);c.drawText(format(price),W()-d(45),yy+d(15),text);c.drawText("BUY",W()-d(45),yy+d(27),text);}}
        void drawFloats(Canvas c){long n=SystemClock.elapsedRealtime();text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(13));for(int i=0;i<floats.length;i++)if(n-ft[i]<700){text.setColor(Color.WHITE);c.drawText(floats[i],fx[i],fy[i]-(n-ft[i])*.06f,text);}}

        void drawStats(Canvas c){bg(c);header(c);text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.WHITE);text.setTextSize(d(20));c.drawText("STATISTICS",d(20),d(112),text);String[][] rows={{"Total taps",format(taps)},{"Lifetime points",format(lifetime)},{"Biggest critical",format(biggestCrit)+"x"},{"Critical taps",format(critTaps)},{"Highest combo","x"+String.format(Locale.US,"%.1f",highestCombo/10f)},{"Play time",formatTime(playSeconds)},{"Rebirths",prestige+""},{"Upgrades bought",format(upgradesBought)},{"Tap power",format(tapPower)},{"Auto income",format(perSecond())},{"Daily streak",format(Math.max(0,questDay-(int)(lastDaily/86400000L)))}};float y=d(145);for(String[] row:rows){p.setColor(Color.rgb(18,24,56));c.drawRoundRect(d(15),y,W()-d(15),y+d(39),d(9),d(9),p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(d(10));text.setColor(Color.rgb(155,175,215));c.drawText(row[0],d(28),y+d(15),text);text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(d(12));text.setColor(Color.WHITE);c.drawText(row[1],W()-d(28),y+d(25),text);y+=d(44);if(y>H()-d(70))break;}nav(c);}
        void drawCollection(Canvas c){bg(c);header(c);text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.WHITE);text.setTextSize(d(19));c.drawText("COLLECTION + QUESTS",d(18),d(108),text);p.setColor(Color.rgb(20,27,61));c.drawRoundRect(d(15),d(122),W()-d(15),d(205),d(14),d(14),p);text.setTextSize(d(12));text.setColor(Color.rgb(245,200,85));c.drawText("DAILY QUESTS",d(28),d(145),text);text.setTextSize(d(9));text.setColor(Color.WHITE);c.drawText("Tap 250: "+Math.min(questTaps,250)+" / 250",d(30),d(165),text);c.drawText("Buy 5 upgrades: "+Math.min(questUpgrades,5)+" / 5",d(30),d(181),text);c.drawText("Earn 10K: "+Math.min(questEarned,10000)+" / 10,000",d(30),d(197),text);text.setColor(Color.rgb(130,235,185));c.drawText("Collectible relics: "+collectibles+" / 20",W()-d(125),d(165),text);
            text.setTextSize(d(14));text.setColor(Color.WHITE);c.drawText("MILESTONES",d(18),d(230),text);String[] m={"100","1K","10K","1M","1B"};for(int i=0;i<5;i++){float x=d(18)+i*(W()-d(36))/5f;p.setColor(lifetime>=new long[]{100,1000,10000,1000000,1000000000L}[i]?Color.rgb(45,170,115):Color.rgb(35,42,75));c.drawCircle(x+d(22),d(265),d(22),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(8));text.setColor(Color.WHITE);c.drawText(m[i],x+d(22),d(268),text);}text.setTextAlign(Paint.Align.LEFT);text.setTextSize(d(14));c.drawText("ACHIEVEMENTS",d(18),d(320),text);String[] ach={"Tap 100","Tap 1,000","Tap 10,000","Earn 100K","Power Glove LV10","First Rebirth","10 Criticals","100 Criticals","Combo x5","Earn 1M","Earn 1B","Defeat a Boss"};for(int i=0;i<12;i++){float x=d(15)+(i%2)*(W()/2-d(5)),y=d(335)+(i/2)*d(30);p.setColor(i<achievements?Color.rgb(45,170,115):Color.rgb(25,31,60));c.drawRoundRect(x,y,x+W()/2-d(10),y+d(25),d(7),d(7),p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(d(8));text.setColor(Color.WHITE);c.drawText((i<achievements?"✓ ":"○ ")+ach[i],x+d(8),y+d(16),text);}nav(c);}

        void tap(){long now=SystemClock.elapsedRealtime();if(bossActive){bossHits++;buzz();addFloat("BOSS HIT!",W()/2,H()*.45f);if(bossHits>=bossNeed){bossActive=false;long reward=10000L*(prestige+1);addReward(reward);collectibles=Math.min(20,collectibles+1);achievements=Math.max(achievements,12);Toast.makeText(getContext(),"BOSS DEFEATED! +"+format(reward),Toast.LENGTH_LONG).show();beep(ToneGenerator.TONE_PROP_ACK);}return;}
            long gain=tapPower+quantumLevel*2L;int crit=0;if(rng.nextFloat()<critLevel*.05f){float r=rng.nextFloat();if(r<.01f){gain*=100;crit=100;}else if(r<.08f){gain*=10;crit=10;}else{gain*=5;crit=5;}critTaps++;biggestCrit=Math.max(biggestCrit,crit);}
            if(now<comboUntil)combo=Math.min(comboCap(),combo+.1f);else combo=1f;comboUntil=now+1300;highestCombo=Math.max(highestCombo,Math.round(combo*10));fever=Math.min(1f,fever+.012f+(crit>0?.025f:0));if(fever>=1&&!frenzy){frenzy=true;eventUntil=now+10000;fever=0;Toast.makeText(getContext(),"🔥 TAP FRENZY! ×10 FOR 10 SECONDS!",Toast.LENGTH_SHORT).show();beep(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);}
            long mult=Math.max(1,Math.round(gain*combo*multiplier()));addReward(mult);taps++;questTaps++;tapScale=.88f;pulse=true;buzz();addFloat((crit>0?"CRIT ×"+crit+"  ":"")+"+"+format(mult),W()/2,H()*.28f);if(crit>0)beep(ToneGenerator.TONE_PROP_BEEP);checkAchievements();}
        void buy(int i){long price=cost(i);if(coins<price)return;coins-=price;switch(i){case 0:tapPower++;break;case 1:autoLevel++;break;case 2:critLevel++;break;case 3:comboLevel++;break;case 4:magnetLevel++;break;case 5:reactorLevel++;break;case 6:swarmLevel++;break;default:quantumLevel++;}upgradesBought++;questUpgrades++;tapScale=.96f;pulse=true;beep(ToneGenerator.TONE_PROP_ACK);}
        void rebirth(){long need=100000L*(prestige+1);if(lifetime<need){Toast.makeText(getContext(),"Need "+format(need)+" lifetime points",Toast.LENGTH_SHORT).show();return;}prestige++;coins=0;tapPower=1;autoLevel=critLevel=comboLevel=magnetLevel=reactorLevel=swarmLevel=quantumLevel=0;combo=1;beep(ToneGenerator.TONE_PROP_ACK);Toast.makeText(getContext(),"REBIRTH! Permanent +25% earnings",Toast.LENGTH_SHORT).show();}
        void daily(){long day=System.currentTimeMillis()/86400000L,last=lastDaily/86400000L;if(day==last)return;long reward=100L*(long)Math.min(1000,Math.max(1,day-last==1?questDay+1:1));addReward(reward);lastDaily=System.currentTimeMillis();Toast.makeText(getContext(),"Daily reward +"+format(reward)+"!",Toast.LENGTH_SHORT).show();}
        void checkAchievements(){int a=0;if(taps>=100)a++;if(taps>=1000)a++;if(taps>=10000)a++;if(lifetime>=100000)a++;if(tapPower>=10)a++;if(prestige>=1)a++;if(critTaps>=10)a++;if(critTaps>=100)a++;if(highestCombo>=50)a++;if(lifetime>=1000000)a++;if(lifetime>=1000000000L)a++;if(collectibles>0)a++;if(a>achievements){achievements=a;beep(ToneGenerator.TONE_PROP_BEEP);}}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX(),y=e.getY();long now=SystemClock.elapsedRealtime();
            if(y>H()-d(50)){screen=Math.min(2,(int)(x/(W()/3f)));return true;}
            if(screen!=0)return true;
            if(y<d(78)&&x>W()-d(150)){rebirth();return true;}
            if(orb.contains(x,y)){tap();performClick();return true;}
            if(goldenUntil>now&&golden.contains(x,y)){goldenUntil=0;long r=5000L*(prestige+1);addReward(r);collectibles=Math.min(20,collectibles+1);addFloat("GOLDEN +"+format(r),x,y);beep(ToneGenerator.TONE_CDMA_HIGH_L);buzz();return true;}
            float sy=bossActive?H()*.48f:H()*.42f,top=sy+d(62),gap=d(4),h=Math.max(d(30),Math.min(d(38),(H()-top-d(53)-gap*7)/8f));for(int i=0;i<8;i++){float yy=top+d(7)+i*(h+gap);if(y>=yy&&y<=yy+h&&x>W()-d(95)){buy(i);return true;}}
            if(now-lastDaily>86400000L)daily(); return true;}
        @Override public boolean performClick(){super.performClick();return true;}
        void save(){save.edit().putLong("coins",coins).putLong("total",total).putLong("lifetime",lifetime).putInt("tap",tapPower).putInt("auto",autoLevel).putInt("crit",critLevel).putInt("combo",comboLevel).putInt("magnet",magnetLevel).putInt("reactor",reactorLevel).putInt("swarm",swarmLevel).putInt("quantum",quantumLevel).putInt("prestige",prestige).putInt("achievements",achievements).putInt("taps",taps).putInt("critTaps",critTaps).putInt("biggestCrit",biggestCrit).putInt("highestCombo",highestCombo).putInt("upgradesBought",upgradesBought).putInt("collectibles",collectibles).putInt("questDay",questDay).putInt("questTaps",questTaps).putInt("questUpgrades",questUpgrades).putInt("questEarned",questEarned).putLong("daily",lastDaily).putLong("golden",lastGolden).putLong("boss",lastBoss).putLong("play",playSeconds).putLong("closed",System.currentTimeMillis()).apply();}
        String format(long n){if(n<1000)return Long.toString(n);String[] u={"K","M","B","T","Qa","Qi"};double v=n;int i=-1;while(Math.abs(v)>=1000&&i<u.length-1){v/=1000;i++;}if(i==u.length-1&&v>=1000)return String.format(Locale.US,"%.2e",(double)n);return String.format(Locale.US,"%.1f%s",v,u[i]);}
        String formatTime(long s){long h=s/3600,m=(s%3600)/60;return h+"h "+m+"m";}
    }
}

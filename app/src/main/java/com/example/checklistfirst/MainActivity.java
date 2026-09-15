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
import android.view.MotionEvent;
import android.view.View;
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
        final RectF orb = new RectF();
        final ToneGenerator sound = new ToneGenerator(AudioManager.STREAM_MUSIC, 65);
        float density, tapScale = 1f;
        long coins, total, lifetime;
        long lastFrame;
        int tapPower = 1, autoLevel, critLevel, comboLevel, magnetLevel, reactorLevel, swarmLevel, quantumLevel;
        int prestige;
        float combo = 1f;
        long comboUntil;
        boolean pulse, soundOn = true;
        int achievements;

        final String[] names = {"POWER GLOVE", "AUTO BOT", "CRIT CORE", "COMBO ENGINE", "COIN MAGNET", "STAR REACTOR", "DRONE SWARM", "QUANTUM TAP"};
        final String[] desc = {"+1 coin per tap", "+2 coins / sec", "+5% critical chance", "+0.5x combo cap", "+10% all earnings", "+10 coins / sec", "+25 coins / sec", "+2 tap power"};
        final int[] base = {25, 80, 150, 300, 700, 1800, 5000, 15000};

        TapView(Context c) {
            super(c);
            density = getResources().getDisplayMetrics().density;
            save = c.getSharedPreferences("tap_galaxy", Context.MODE_PRIVATE);
            coins = save.getLong("coins", 0); total = save.getLong("total", 0); lifetime = save.getLong("lifetime", 0);
            tapPower = save.getInt("tap", 1); autoLevel = save.getInt("auto", 0); critLevel = save.getInt("crit", 0); comboLevel = save.getInt("combo", 0);
            magnetLevel = save.getInt("magnet", 0); reactorLevel = save.getInt("reactor", 0); swarmLevel = save.getInt("swarm", 0); quantumLevel = save.getInt("quantum", 0);
            prestige = save.getInt("prestige", 0); achievements = save.getInt("achievements", 0);
            text.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        float d(float v) { return v * density; }
        float W() { return getWidth(); }
        float H() { return getHeight(); }
        int level(int i) { switch(i){case 0:return tapPower-1;case 1:return autoLevel;case 2:return critLevel;case 3:return comboLevel;case 4:return magnetLevel;case 5:return reactorLevel;case 6:return swarmLevel;default:return quantumLevel;} }
        long cost(int i) { return (long)(base[i] * Math.pow(1.62, level(i))); }
        long perSecond() { return autoLevel*2L + reactorLevel*10L + swarmLevel*25L; }
        float multiplier() { return (1f + magnetLevel*.10f) * (1f + prestige*.25f) * (1f + (achievements>0?.05f:0f)); }
        float comboCap() { return 1f + comboLevel*.5f; }
        void beep(int tone) { if(soundOn) sound.startTone(tone, 45); }

        @Override protected void onDraw(Canvas c) {
            long now=SystemClock.elapsedRealtime();
            float dt=Math.min(.1f, lastFrame==0?0:(now-lastFrame)/1000f); lastFrame=now;
            long gain=(long)(perSecond()*dt*multiplier());
            coins+=gain; total+=gain; lifetime+=gain;
            checkAchievements();
            drawBackground(c); drawHeader(c); drawOrb(c); drawStats(c); drawUpgrades(c);
            if(pulse){tapScale+=(1f-tapScale)*.22f;if(Math.abs(tapScale-1f)<.01f){tapScale=1f;pulse=false;}}
            save(); postInvalidateDelayed(33);
        }
        void drawBackground(Canvas c){
            c.drawColor(Color.rgb(8,10,28)); p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(14,18,48)); c.drawRect(0,0,W(),H(),p);
            p.setColor(Color.rgb(24,28,68)); c.drawCircle(W()*.18f,H()*.30f,d(90),p); p.setColor(Color.rgb(18,22,55)); c.drawCircle(W()*.86f,H()*.20f,d(120),p);
            p.setColor(Color.WHITE); for(int i=0;i<50;i++) c.drawCircle((i*97)%Math.max(1,getWidth()),(i*173+35)%Math.max(1,(int)(H()*.67f)),d(i%3==0?1.5f:.8f),p);
        }
        void drawHeader(Canvas c){
            p.setColor(Color.rgb(16,21,48)); c.drawRoundRect(d(12),d(10),W()-d(12),d(88),d(20),d(20),p);
            text.setTextAlign(Paint.Align.LEFT); text.setTextSize(d(13)); text.setColor(Color.rgb(150,170,220)); c.drawText("TAP GALAXY",d(28),d(30),text);
            text.setTextSize(d(25)); text.setColor(Color.WHITE); c.drawText(format(coins)+" COINS",d(28),d(60),text);
            text.setTextSize(d(10)); text.setColor(Color.rgb(125,220,190)); c.drawText("+"+perSecond()+" / SEC",W()-d(28),d(28),text); c.drawText("REBIRTHS "+prestige,W()-d(28),d(44),text);
            p.setColor(Color.rgb(112,72,190)); c.drawRoundRect(W()-d(125),d(52),W()-d(16),d(79),d(10),d(10),p); text.setTextAlign(Paint.Align.CENTER); text.setTextSize(d(10)); c.drawText("REBIRTH",W()-d(70),d(69),text);
        }
        void drawOrb(Canvas c){
            float cx=W()/2f,cy=H()*.285f,r=Math.min(W()*.30f,d(96))*tapScale; orb.set(cx-r,cy-r,cx+r,cy+r);
            p.setShadowLayer(d(28),0,d(4),Color.rgb(75,120,255));p.setColor(Color.rgb(62,100,235));c.drawCircle(cx,cy,r+d(12),p);p.clearShadowLayer();p.setColor(Color.rgb(105,215,255));c.drawCircle(cx,cy,r,p);p.setColor(Color.rgb(30,58,175));c.drawCircle(cx,cy,r*.78f,p);p.setColor(Color.rgb(140,235,255));c.drawCircle(cx-r*.28f,cy-r*.28f,r*.22f,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(4));p.setColor(Color.rgb(190,245,255));c.drawCircle(cx,cy,r*.88f,p);p.setStyle(Paint.Style.FILL);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(17));text.setColor(Color.WHITE);c.drawText("TAP!",cx,cy+d(7),text);text.setTextSize(d(11));text.setColor(Color.rgb(180,220,255));c.drawText("POWER CORE",cx,cy+d(27),text);
        }
        void drawStats(Canvas c){
            float y=H()*.445f; p.setColor(Color.rgb(17,23,54));c.drawRoundRect(d(12),y,W()-d(12),y+d(58),d(16),d(16),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(10));text.setColor(Color.rgb(145,160,205));c.drawText("TOTAL",W()*.17f,y+d(19),text);c.drawText("COMBO",W()*.38f,y+d(19),text);c.drawText("CRIT",W()*.59f,y+d(19),text);c.drawText("MEDALS",W()*.82f,y+d(19),text);
            text.setTextSize(d(15));text.setColor(Color.WHITE);c.drawText(format(total),W()*.17f,y+d(42),text);c.drawText(String.format(Locale.US,"x%.1f",combo),W()*.38f,y+d(42),text);c.drawText((critLevel*5)+"%",W()*.59f,y+d(42),text);c.drawText(achievements+"/6",W()*.82f,y+d(42),text);
        }
        void drawUpgrades(Canvas c){
            float top=H()*.535f; text.setTextAlign(Paint.Align.LEFT);text.setTextSize(d(16));text.setColor(Color.WHITE);c.drawText("UPGRADES",d(18),top,text);
            float cardTop=top+d(9),gap=d(5),cardH=Math.min(d(43),(H()-cardTop-d(8)-gap*7)/8f);
            for(int i=0;i<8;i++){float y=cardTop+i*(cardH+gap);p.setColor(Color.rgb(20,27,61));c.drawRoundRect(d(10),y,W()-d(10),y+cardH,d(11),d(11),p);p.setColor(Color.rgb(65+i*20,100+i*10,190-i*10));c.drawCircle(d(31),y+cardH/2,d(12),p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(d(10));text.setColor(Color.WHITE);c.drawText(names[i]+" LV"+level(i),d(50),y+d(17),text);text.setTextSize(d(8));text.setColor(Color.rgb(155,175,215));c.drawText(desc[i],d(50),y+d(30),text);long price=cost(i);p.setColor(coins>=price?Color.rgb(45,170,115):Color.rgb(55,62,88));c.drawRoundRect(W()-d(83),y+d(5),W()-d(13),y+cardH-d(5),d(8),d(8),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(d(8));text.setColor(Color.WHITE);c.drawText(format(price),W()-d(48),y+d(18),text);c.drawText("BUY",W()-d(48),y+d(30),text);}
        }
        void tap(){long gain=tapPower+quantumLevel*2L;if(rng.nextFloat()<critLevel*.05f)gain*=3;long now=SystemClock.elapsedRealtime();if(now<comboUntil)combo=Math.min(comboCap(),combo+.1f);else combo=1f;comboUntil=now+1300;gain=Math.max(1,Math.round(gain*combo*multiplier()));coins+=gain;total+=gain;lifetime+=gain;tapScale=.92f;pulse=true;beep(ToneGenerator.TONE_PROP_BEEP);checkAchievements();}
        void buy(int i){long price=cost(i);if(coins<price)return;coins-=price;switch(i){case 0:tapPower++;break;case 1:autoLevel++;break;case 2:critLevel++;break;case 3:comboLevel++;break;case 4:magnetLevel++;break;case 5:reactorLevel++;break;case 6:swarmLevel++;break;default:quantumLevel++;}tapScale=.96f;pulse=true;beep(ToneGenerator.TONE_PROP_ACK);}
        void rebirth(){long need=100000L*(prestige+1);if(lifetime<need)return;prestige++;coins=0;tapPower=1;autoLevel=critLevel=comboLevel=magnetLevel=reactorLevel=swarmLevel=quantumLevel=0;combo=1f;beep(ToneGenerator.TONE_PROP_ACK);}
        void checkAchievements(){int a=0;if(total>=100)a++;if(total>=1000)a++;if(total>=10000)a++;if(total>=100000)a++;if(tapPower>=10)a++;if(prestige>=1)a++;if(a>achievements){achievements=a;beep(ToneGenerator.TONE_PROP_BEEP);}}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX(),y=e.getY();if(orb.contains(x,y)){tap();performClick();return true;}if(y>=d(50)&&y<=d(84)&&x>W()-d(135)){rebirth();return true;}float top=H()*.535f,cardTop=top+d(9),gap=d(5),cardH=Math.min(d(43),(H()-cardTop-d(8)-gap*7)/8f);for(int i=0;i<8;i++){float yy=cardTop+i*(cardH+gap);if(y>=yy&&y<=yy+cardH&&x>=W()-d(95)){buy(i);return true;}}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
        void save(){save.edit().putLong("coins",coins).putLong("total",total).putLong("lifetime",lifetime).putInt("tap",tapPower).putInt("auto",autoLevel).putInt("crit",critLevel).putInt("combo",comboLevel).putInt("magnet",magnetLevel).putInt("reactor",reactorLevel).putInt("swarm",swarmLevel).putInt("quantum",quantumLevel).putInt("prestige",prestige).putInt("achievements",achievements).apply();}
        String format(long n){if(n<1000)return Long.toString(n);if(n<1000000)return String.format(Locale.US,"%.1fK",n/1000.0);if(n<1000000000)return String.format(Locale.US,"%.1fM",n/1000000.0);return String.format(Locale.US,"%.1fB",n/1000000000.0);}
    }
}

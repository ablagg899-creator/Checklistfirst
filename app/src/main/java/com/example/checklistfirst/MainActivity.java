package com.example.checklistfirst;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
        float density, tapScale = 1f;
        long coins = 0, total = 0, lastFrame;
        int tapPower = 1, autoLevel = 0, critLevel = 0, comboLevel = 0;
        float combo = 0f;
        long comboUntil = 0;
        boolean pulse = false;

        final String[] names = {"POWER GLOVE", "AUTO BOT", "CRIT CORE", "COMBO ENGINE"};
        final String[] desc = {"+1 coin per tap", "+2 coins / sec", "+5% critical chance", "+0.5x combo cap"};
        final int[] base = {25, 80, 150, 300};

        TapView(Context c) {
            super(c);
            density = getResources().getDisplayMetrics().density;
            save = c.getSharedPreferences("tap_galaxy", Context.MODE_PRIVATE);
            coins = save.getLong("coins", 0);
            total = save.getLong("total", 0);
            tapPower = save.getInt("tap", 1);
            autoLevel = save.getInt("auto", 0);
            critLevel = save.getInt("crit", 0);
            comboLevel = save.getInt("combo", 0);
            text.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        float d(float v) { return v * density; }
        float W() { return getWidth(); }
        float H() { return getHeight(); }
        long cost(int i) { return (long)(base[i] * Math.pow(1.65, level(i))); }
        int level(int i) { return i == 0 ? tapPower - 1 : i == 1 ? autoLevel : i == 2 ? critLevel : comboLevel; }
        long perSecond() { return autoLevel * 2L; }
        float comboCap() { return 1.0f + comboLevel * .5f; }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            long now = SystemClock.elapsedRealtime();
            float dt = Math.min(.1f, (now - lastFrame) / 1000f);
            lastFrame = now;
            if (perSecond() > 0) {
                long gain = (long)(perSecond() * dt);
                coins += gain;
                total += gain;
            }
            drawBackground(c);
            drawHeader(c);
            drawOrb(c);
            drawStats(c);
            drawUpgrades(c);
            if (pulse) { tapScale += (1f - tapScale) * .22f; if (Math.abs(tapScale - 1f) < .01f) { tapScale = 1f; pulse = false; } }
            save();
            postInvalidateDelayed(33);
        }

        void drawBackground(Canvas c) {
            c.drawColor(Color.rgb(8, 10, 28));
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(14, 18, 48));
            c.drawRect(0, 0, W(), H(), p);
            p.setColor(Color.rgb(24, 28, 68));
            c.drawCircle(W() * .18f, H() * .30f, d(90), p);
            p.setColor(Color.rgb(18, 22, 55));
            c.drawCircle(W() * .86f, H() * .20f, d(120), p);
            p.setColor(Color.WHITE);
            for (int i = 0; i < 50; i++) {
                float x = (i * 97) % Math.max(1, getWidth());
                float y = (i * 173 + 35) % Math.max(1, (int)(H() * .67f));
                c.drawCircle(x, y, d(i % 3 == 0 ? 1.5f : .8f), p);
            }
        }

        void drawHeader(Canvas c) {
            p.setColor(Color.rgb(16, 21, 48));
            c.drawRoundRect(d(12), d(12), W() - d(12), d(92), d(20), d(20), p);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(d(14)); text.setColor(Color.rgb(150, 170, 220));
            c.drawText("TAP GALAXY", d(28), d(34), text);
            text.setTextSize(d(29)); text.setColor(Color.WHITE);
            c.drawText(format(coins) + "  COINS", d(28), d(67), text);
            text.setTextAlign(Paint.Align.RIGHT); text.setTextSize(d(12)); text.setColor(Color.rgb(125, 220, 190));
            c.drawText("+" + perSecond() + " / SEC", W() - d(28), d(35), text);
            c.drawText("TAP POWER " + tapPower, W() - d(28), d(57), text);
        }

        void drawOrb(Canvas c) {
            float cx = W() / 2f, cy = H() * .33f, r = Math.min(W() * .30f, d(105)) * tapScale;
            orb.set(cx-r, cy-r, cx+r, cy+r);
            p.setShadowLayer(d(28), 0, d(4), Color.rgb(75, 120, 255));
            p.setColor(Color.rgb(62, 100, 235)); c.drawCircle(cx, cy, r + d(12), p);
            p.clearShadowLayer();
            p.setColor(Color.rgb(105, 215, 255)); c.drawCircle(cx, cy, r, p);
            p.setColor(Color.rgb(30, 58, 175)); c.drawCircle(cx, cy, r * .78f, p);
            p.setColor(Color.rgb(140, 235, 255)); c.drawCircle(cx-r*.28f, cy-r*.28f, r*.22f, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(d(4)); p.setColor(Color.rgb(190, 245, 255));
            c.drawCircle(cx, cy, r * .88f, p); p.setStyle(Paint.Style.FILL);
            text.setTextAlign(Paint.Align.CENTER); text.setTextSize(d(17)); text.setColor(Color.WHITE);
            c.drawText("TAP!", cx, cy + d(7), text);
            text.setTextSize(d(12)); text.setColor(Color.rgb(180, 220, 255));
            c.drawText("POWER CORE", cx, cy + d(28), text);
        }

        void drawStats(Canvas c) {
            float y = H() * .52f;
            p.setColor(Color.rgb(17, 23, 54));
            c.drawRoundRect(d(14), y, W()-d(14), y+d(58), d(16), d(16), p);
            text.setTextAlign(Paint.Align.CENTER); text.setTextSize(d(12)); text.setColor(Color.rgb(145, 160, 205));
            c.drawText("TOTAL EARNED", W()*.25f, y+d(21), text);
            c.drawText("COMBO", W()*.5f, y+d(21), text);
            c.drawText("CRIT CHANCE", W()*.75f, y+d(21), text);
            text.setTextSize(d(17)); text.setColor(Color.WHITE);
            c.drawText(format(total), W()*.25f, y+d(44), text);
            c.drawText(String.format(Locale.US, "x%.1f", Math.max(1f, combo)), W()*.5f, y+d(44), text);
            c.drawText((critLevel*5) + "%", W()*.75f, y+d(44), text);
        }

        void drawUpgrades(Canvas c) {
            float top = H() * .60f;
            text.setTextAlign(Paint.Align.LEFT); text.setTextSize(d(18)); text.setColor(Color.WHITE);
            c.drawText("UPGRADES", d(18), top, text);
            float cardTop = top + d(12), gap = d(8), cardH = Math.min(d(62), (H()-cardTop-d(12)-gap*3)/4f);
            for (int i=0;i<4;i++) {
                float y = cardTop + i*(cardH+gap);
                p.setColor(Color.rgb(20, 27, 61)); c.drawRoundRect(d(12), y, W()-d(12), y+cardH, d(15), d(15), p);
                p.setColor(i==0?Color.rgb(78,128,255):i==1?Color.rgb(70,190,150):i==2?Color.rgb(230,150,75):Color.rgb(170,95,230));
                c.drawCircle(d(40), y+cardH/2, d(17), p);
                text.setTextSize(d(13)); text.setColor(Color.WHITE); c.drawText(names[i] + "  LV " + level(i), d(66), y+d(23), text);
                text.setTextSize(d(10)); text.setColor(Color.rgb(155,175,215)); c.drawText(desc[i], d(66), y+d(41), text);
                long price=cost(i); boolean can=coins>=price;
                p.setColor(can?Color.rgb(45,170,115):Color.rgb(55,62,88)); c.drawRoundRect(W()-d(100), y+d(10), W()-d(14), y+cardH-d(10), d(11), d(11), p);
                text.setTextAlign(Paint.Align.CENTER); text.setTextSize(d(10)); text.setColor(Color.WHITE); c.drawText(format(price), W()-d(57), y+d(33), text);
                text.setTextSize(d(8)); c.drawText("BUY", W()-d(57), y+d(47), text);
                text.setTextAlign(Paint.Align.LEFT);
            }
        }

        void tap() {
            long gain = tapPower;
            if (rng.nextFloat() < critLevel * .05f) gain *= 3;
            long now = SystemClock.elapsedRealtime();
            if (now < comboUntil) combo = Math.min(comboCap(), combo + .1f);
            else combo = 1f;
            comboUntil = now + 1300;
            gain = Math.max(1, Math.round(gain * combo));
            coins += gain; total += gain; tapScale = .92f; pulse = true;
        }

        void buy(int i) {
            long price = cost(i);
            if (coins < price) return;
            coins -= price;
            if (i==0) tapPower++;
            else if (i==1) autoLevel++;
            else if (i==2) critLevel++;
            else comboLevel++;
            tapScale = .96f; pulse = true;
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() != MotionEvent.ACTION_DOWN) return true;
            float x=e.getX(), y=e.getY();
            if (orb.contains(x,y)) { tap(); performClick(); return true; }
            float top=H()*.60f, cardTop=top+d(12), gap=d(8), cardH=Math.min(d(62),(H()-cardTop-d(12)-gap*3)/4f);
            for(int i=0;i<4;i++) {
                float yy=cardTop+i*(cardH+gap);
                if(y>=yy && y<=yy+cardH && x>=W()-d(112)) { buy(i); return true; }
            }
            return true;
        }

        @Override public boolean performClick() { super.performClick(); return true; }

        void save() {
            save.edit().putLong("coins",coins).putLong("total",total).putInt("tap",tapPower).putInt("auto",autoLevel).putInt("crit",critLevel).putInt("combo",comboLevel).apply();
        }

        String format(long n) {
            if(n<1000) return Long.toString(n);
            if(n<1000000) return String.format(Locale.US,"%.1fK",n/1000.0);
            if(n<1000000000) return String.format(Locale.US,"%.1fM",n/1000000.0);
            return String.format(Locale.US,"%.1fB",n/1000000000.0);
        }
    }
}

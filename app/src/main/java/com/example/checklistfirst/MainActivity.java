package com.example.checklistfirst;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) { super.onCreate(b); setTitle("Galaga Clone"); setContentView(new GameView(this)); }

    static class GameView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random random = new Random();
        ArrayList<Enemy> enemies = new ArrayList<>();
        ArrayList<Bullet> bullets = new ArrayList<>();
        ArrayList<Bullet> enemyBullets = new ArrayList<>();
        float playerX, enemyDir = 1, enemyTimer, fireTimer, enemyFireTimer;
        int score=0, lives=3, level=1, highScore=0;
        boolean running=false, gameOver=false;
        long last;
        RectF player = new RectF();
        float density;

        GameView(Context c) { super(c); density=getResources().getDisplayMetrics().density; p.setStrokeWidth(3*density); text.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD)); setFocusable(true); }
        float d(float x){return x*density;}
        void reset(){
            enemies.clear(); bullets.clear(); enemyBullets.clear(); score=0; lives=3; level=1; gameOver=false; running=true; enemyDir=1; enemyTimer=0; fireTimer=0; enemyFireTimer=0;
            spawnWave(); last=System.nanoTime(); invalidate();
        }
        void spawnWave(){
            enemies.clear(); int cols=7, rows=3; float w=d(42), h=d(30), gap=d(12); float total=cols*w+(cols-1)*gap; float sx=(getWidth()-total)/2f;
            for(int r=0;r<rows;r++) for(int c=0;c<cols;c++) enemies.add(new Enemy(sx+c*(w+gap),d(85)+r*d(48),w,h,r));
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c); c.drawColor(Color.rgb(3,5,18));
            p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE);
            for(int i=0;i<55;i++){ float x=(i*83)%Math.max(1,getWidth()); float y=(i*137)%Math.max(1,getHeight()); c.drawCircle(x,y,d(i%2==0?1:1.5f),p); }
            text.setTextSize(d(18)); text.setColor(Color.WHITE); text.setTextAlign(Paint.Align.LEFT);
            c.drawText("SCORE " + score, d(14), d(27), text); c.drawText("LIVES " + lives, getWidth()-d(105), d(27), text);
            if(!running){
                text.setTextAlign(Paint.Align.CENTER); text.setTextSize(d(38)); text.setColor(Color.CYAN); c.drawText("GALAGA",getWidth()/2f,d(230),text);
                text.setTextSize(d(18)); text.setColor(Color.WHITE); c.drawText(gameOver?"GAME OVER":"READY",getWidth()/2f,d(270),text);
                text.setTextSize(d(15)); c.drawText("TAP TO START",getWidth()/2f,d(315),text); c.drawText("DRAG TO MOVE • TAP TO FIRE",getWidth()/2f,d(345),text); return;
            }
            drawPlayer(c); for(Enemy e:enemies) drawEnemy(c,e); for(Bullet b:bullets) drawBullet(c,b,false); for(Bullet b:enemyBullets) drawBullet(c,b,true);
            long now=System.nanoTime(); float dt=Math.min(.05f,(now-last)/1_000_000_000f); last=now; update(dt); postInvalidateDelayed(16);
        }
        void drawPlayer(Canvas c){
            p.setColor(Color.CYAN); p.setStyle(Paint.Style.FILL); Path q=new Path(); q.moveTo(playerX,d(540)); q.lineTo(playerX-d(23),d(585)); q.lineTo(playerX,d(575)); q.lineTo(playerX+d(23),d(585)); q.close(); c.drawPath(q,p); p.setColor(Color.WHITE); c.drawRect(playerX-d(3),d(550),playerX+d(3),d(575),p);
        }
        void drawEnemy(Canvas c,Enemy e){
            p.setColor(e.row==0?Color.MAGENTA:(e.row==1?Color.YELLOW:Color.GREEN)); p.setStyle(Paint.Style.FILL); c.drawOval(e.x,e.y,e.x+e.w,e.y+e.h,p); p.setColor(Color.rgb(3,5,18)); c.drawCircle(e.x+e.w*.32f,e.y+e.h*.43f,d(3),p); c.drawCircle(e.x+e.w*.68f,e.y+e.h*.43f,d(3),p); p.setColor(Color.WHITE); c.drawRect(e.x+e.w*.43f,e.y+e.h*.72f,e.x+e.w*.57f,e.y+e.h*.84f,p);
        }
        void drawBullet(Canvas c,Bullet b,boolean enemy){ p.setColor(enemy?Color.RED:Color.CYAN); c.drawRect(b.x-d(2),b.y-d(7),b.x+d(2),b.y+d(7),p); }
        void update(float dt){
            if(!running)return;
            player.set(playerX-d(23),d(540),playerX+d(23),d(585));
            float speed=d(42); for(Enemy e:enemies)e.x+=enemyDir*speed*dt;
            float min=d(10), max=getWidth()-d(10); for(Enemy e:enemies){if(e.x<min||e.x+e.w>max){enemyDir=-enemyDir; for(Enemy z:enemies)z.y+=d(12); break;}}
            fireTimer-=dt; enemyFireTimer-=dt;
            if(fireTimer<=0){bullets.add(new Bullet(playerX,d(535),-d(480))); fireTimer=.22f;}
            if(enemyFireTimer<=0&&!enemies.isEmpty()){Enemy e=enemies.get(random.nextInt(enemies.size())); enemyBullets.add(new Bullet(e.x+e.w/2,e.y+e.h,d(190))); enemyFireTimer=.7f+random.nextFloat()*1.2f;}
            for(Bullet b:bullets)b.y+=b.v*dt; for(Bullet b:enemyBullets)b.y+=b.v*dt;
            for(int i=bullets.size()-1;i>=0;i--){Bullet b=bullets.get(i); boolean hit=false; for(int j=enemies.size()-1;j>=0;j--){Enemy e=enemies.get(j); if(e.rect().contains(b.x,b.y)){enemies.remove(j);score+=10;hit=true;break;}} if(hit||b.y<d(35))bullets.remove(i);}
            for(int i=enemyBullets.size()-1;i>=0;i--){Bullet b=enemyBullets.get(i); if(player.contains(b.x,b.y)){enemyBullets.remove(i); lives--; if(lives<=0){running=false;gameOver=true;} break;} if(b.y>getHeight())enemyBullets.remove(i);}
            for(Enemy e:enemies)if(e.y+e.h> d(515)){running=false;gameOver=true;}
            if(enemies.isEmpty()){level++;spawnWave(); enemyFireTimer=.4f;}
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent ev){
            if(ev.getAction()==MotionEvent.ACTION_DOWN){ if(!running){reset();return true;} playerX=clamp(ev.getX()); bullets.add(new Bullet(playerX,d(535),-d(480))); fireTimer=.22f; return true; }
            if(ev.getAction()==MotionEvent.ACTION_MOVE&&running){playerX=clamp(ev.getX());return true;} return true;
        }
        float clamp(float x){return Math.max(d(25),Math.min(getWidth()-d(25),x));}
        @Override protected void onSizeChanged(int w,int h,int ow,int oh){playerX=w/2f;}
        static class Enemy {float x,y,w,h;int row;Enemy(float x,float y,float w,float h,int r){this.x=x;this.y=y;this.w=w;this.h=h;row=r;}RectF rect(){return new RectF(x,y,x+w,y+h);}}
        static class Bullet {float x,y,v;Bullet(float x,float y,float v){this.x=x;this.y=y;this.v=v;}}
    }
}

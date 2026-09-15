package com.example.checklistfirst;
import android.content.*;import java.util.*;
class GameState{
 SharedPreferences s;long coins,life,taps,crits,bigCrit,play,closed,lastTap,goldEnd,bossEnd,eventEnd;int rebirths,collect,achievements,streak,quests,event=-1,bossHP,bossMax;int[] u=new int[9];
 String[] names={"Power Glove","Auto Tapper","Crit Core","Combo Engine","Coin Magnet","Star Reactor","Drone Swarm","Quantum Tap","Time Drive"};
 long[] base={25,80,150,300,700,1800,5000,15000,30000};
 GameState(Context c){s=c.getSharedPreferences("aaron_tap",0);load();}
 void load(){coins=s.getLong("coins",0);life=s.getLong("life",0);taps=s.getLong("taps",0);crits=s.getLong("crits",0);bigCrit=s.getLong("big",0);play=s.getLong("play",0);rebirths=s.getInt("rebirth",0);collect=s.getInt("collect",0);achievements=s.getInt("ach",0);streak=s.getInt("streak",0);quests=s.getInt("quests",0);closed=s.getLong("closed",System.currentTimeMillis());for(int i=0;i<9;i++)u[i]=s.getInt("u"+i,0);}
 long income(){return u[1]*2L+u[5]*10L+u[6]*25L;}
 long tap(){return 1+u[0]+u[7]*2L;}
 double incomeMult(){return (1+u[4]*.1)*(1+rebirths*.25)*(event==0?2:1);}
 double comboCap(){return 1+u[3]*.5+rebirths*.1;}
 double critChance(){return Math.min(.8,.02+u[2]*.02+(event==1?.2:0));}
 long cost(int i){double x=base[i]*Math.pow(1.62,u[i]);return x>9e18?Long.MAX_VALUE:(long)x;}
 void add(long n){coins+=n;life+=n;checkAchievements();}
 void buy(int i){long c=cost(i);if(coins>=c){coins-=c;u[i]++;quests++;}}
 void rebirth(){long need=100000L*(rebirths+1);if(life>=need){rebirths++;coins=0;Arrays.fill(u,0);}}
 void checkAchievements(){int a=0;if(taps>=100)a++;if(taps>=1000)a++;if(taps>=10000)a++;if(life>=100000)a++;if(life>=1000000)a++;if(life>=1000000000L)a++;if(crits>=10)a++;if(crits>=100)a++;if(bigCrit>=50)a++;if(rebirths>0)a++;if(collect>0)a++;if(totalUpgrades()>=100)a++;achievements=Math.max(achievements,a);}
 int totalUpgrades(){int x=0;for(int n:u)x+=n;return x;}
 void save(){SharedPreferences.Editor e=s.edit().putLong("coins",coins).putLong("life",life).putLong("taps",taps).putLong("crits",crits).putLong("big",bigCrit).putLong("play",play).putInt("rebirth",rebirths).putInt("collect",collect).putInt("ach",achievements).putInt("streak",streak).putInt("quests",quests).putLong("closed",System.currentTimeMillis());for(int i=0;i<9;i++)e.putInt("u"+i,u[i]);e.apply();}
 String fmt(long n){if(n<1000)return ""+n;String[] a={"K","M","B","T","Qa","Qi","Sx","Sp","Oc","No"};double x=n;int i=-1;while(x>=1000&&i<a.length-1){x/=1000;i++;}if(i==a.length-1&&x>=1000)return String.format(Locale.US,"%.2e",(double)n);return x<10?String.format(Locale.US,"%.2f%s",x,a[i]):String.format(Locale.US,"%.1f%s",x,a[i]);}
}

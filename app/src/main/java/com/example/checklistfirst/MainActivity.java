package com.aarontap.rpg;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(6,12,22));
        getWindow().setNavigationBarColor(Color.rgb(6,12,22));
        setTitle("Monster Tamer: Generations");
        setContentView(new Game(this));
    }

    static class Game extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rng = new Random();
        final SharedPreferences save;
        final Vibrator vibrator;
        final ToneGenerator tone;
        final float den;

        int screen = 0, chapter = 1, generation = 1, year = 8, zone = 0, level = 1, xp = 0, gold = 120;
        int wins = 0, tames = 0, enemy = 0, enemyHp = 0, playerHp = 120, maxHp = 120;
        int selected = 0, battleTurn = 0, anim = 0, weapon = 0, flash = 0;
        boolean boss = false, defending = false, married = false, children = false;
        String spouse = "";
        final ArrayList<Integer> party = new ArrayList<>();
        final boolean[] caught = new boolean[120];
        final int[] monsterLevel = new int[120];

        Bitmap heroWalk, worldMap;
        final Bitmap[] monsterArt = new Bitmap[12];
        final String[] monsterNames = {
            "Azure Slime","Metal Slime","King Slime","Drake","Wyvern","Golem","Wolf","Tiger","Ogre","Demon","Phoenix","Unicorn",
            "Green Slime","Red Slime","Blue Slime","Purple Slime","Gooey Slime","Liquid Slime","Muddy Slime","Slime Mage",
            "Sparkle Slime","Giant Slime","Crystal Slime","Wild Boar","Great Boar","Bear","Ice Bear","Snow Tiger","Leopard","Sabertooth",
            "Boar King","Lion","Panther","Hyena","Baby Dragon","Green Dragon","Red Dragon","Blue Dragon","Black Dragon","Ice Dragon",
            "Golden Dragon","Ancient Dragon","Skeleton","Skeleton Archer","Zombie","Ghoul","Wight","Lich","Mummy","Ghost","Phantom",
            "Bone Dragon","Dark Knight","Mandrake","Cactuar","Shroom","Mushroom King","Flower","Sunflower","Vine","Treant","Ent","Will-o-Wisp",
            "Pumpkin","Carnivine","Blooming Flower","Roc Golem","Creeper","Fire Slime","Ice Slime","Wind Sprite","Earth Golem","Water Elemental",
            "Lightning Sprite","Sand Golem","Lava Golem","Storm Elemental","Crystal Elemental","Fish","Shark","Giant Fish","Angler","Jellyfish",
            "Octopus","Squid","Kraken","Sea Serpent","Mermaid","Water Dragon","Griffin","Pegasus","Unicorn Queen","Chimera","Basilisk",
            "Hydra","Cerberus","Minotaur","Colossus","Jormungandr","Fenrir","Troll","Ogre King","Cyclops","Titan","Demon Lord","Archdemon",
            "Nightmare","Behemoth","The Ancient One","Phoenix","Solar Phoenix","Star Phoenix","Goblin","Goblin King","Orc King","Slime Lord","Shadow Fenrir","World Serpent"
        };
        final String[] zones = {"Greenvale","Whisperwood","Sunscar","Moonlit Coast","Frostpeak","Elder Ruins","Skyreach","Shadow Vale"};
        final String[] towns = {"Greenvale Village","Willowbrook","Dustmere","Coralport","Frostholm","Ruinhaven","Skyspire","Nightfall"};
        final String[] chapters = {
            "The First Bond","Whispers in the Woods","The Sunken Crown","Storm over Moonlit Coast",
            "Heart of Frostpeak","The Elder Gate","Road to Skyreach","The World Serpent"
        };
        final String[] chapterText = {
            "As a child, you discover a strange truth: a defeated monster may choose friendship instead of fleeing.",
            "The old forest is waking. You meet rangers, merchants and a breeder who teaches you how to read a monster's heart.",
            "A stolen crown lies beneath the coast. Your first voyage reveals that the kingdom's history is tied to the monster clans.",
            "Years pass. You become an adult, build a home, and sail toward a storm that changes your family forever.",
            "The mountains reveal a prophecy: when the Elder Gate opens, a family bond will be stronger than any single hero.",
            "The Gate awakens. Your children inherit the journey and can fight beside the companions you raised.",
            "Ships, sky roads and ancient ruins open new regions. Rare monsters begin appearing as your party grows.",
            "Thirty years after the first bond, the World Serpent rises. Your family and monsters must decide the fate of the realm."
        };
        final String[] npcs = {"Mira the Innkeeper","Tobin the Smith","Elder Rowan","Lyra the Ranger","Bram the Breeder","Sage Orin","Captain Vale","The Wandering Merchant"};
        final String[] weapons = {"Wooden Blade","Iron Sword","Knight Saber","Moonsteel","Dragonfang"};
        final int[] weaponPower = {0,8,18,30,48};

        Game(Context c) {
            super(c); den=getResources().getDisplayMetrics().density;
            save=c.getSharedPreferences("monster_tamer_generations",Context.MODE_PRIVATE);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            tone=new ToneGenerator(AudioManager.STREAM_MUSIC,70);
            text.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));
            setFocusable(true); load(); loadArt(); postInvalidateDelayed(120);
        }
        float dp(float v){return v*den;} float W(){return getWidth();} float H(){return getHeight();}
        Bitmap asset(String n){try{return BitmapFactory.decodeStream(getContext().getAssets().open(n));}catch(Exception e){return null;}}
        void loadArt(){
            heroWalk=asset("hero_walk.png"); worldMap=asset("world_map.png");
            String[] f={"slime.png","metal.png","king_slime.png","dragon.png","wyvern.png","golem.png","wolf.png","tiger.png","ogre.png","demon.png","phoenix.png","unicorn.png"};
            for(int i=0;i<f.length;i++)monsterArt[i]=asset(f[i]);
        }
        void load(){
            level=save.getInt("level",1); xp=save.getInt("xp",0); gold=save.getInt("gold",120); wins=save.getInt("wins",0); tames=save.getInt("tames",0);
            chapter=save.getInt("chapter",1); generation=save.getInt("generation",1); year=save.getInt("year",8); zone=save.getInt("zone",0);
            playerHp=save.getInt("hp",120); married=save.getBoolean("married",false); children=save.getBoolean("children",false); spouse=save.getString("spouse",""); weapon=save.getInt("weapon",0);
            for(int i=0;i<120;i++){caught[i]=save.getBoolean("c"+i,false);monsterLevel[i]=save.getInt("m"+i,1);if(caught[i]&&party.size()<4)party.add(i);}
            maxHp=100+level*24; if(party.isEmpty()){caught[0]=true;party.add(0);tames=Math.max(1,tames);} if(playerHp<=0||playerHp>maxHp)playerHp=maxHp;
        }
        void persist(){
            SharedPreferences.Editor e=save.edit().putInt("level",level).putInt("xp",xp).putInt("gold",gold).putInt("wins",wins).putInt("tames",tames).putInt("chapter",chapter).putInt("generation",generation).putInt("year",year).putInt("zone",zone).putInt("hp",playerHp).putBoolean("married",married).putBoolean("children",children).putString("spouse",spouse).putInt("weapon",weapon);
            for(int i=0;i<120;i++)if(caught[i])e.putBoolean("c"+i,true).putInt("m"+i,monsterLevel[i]); e.apply();
        }
        void beep(boolean good){try{tone.startTone(good?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_NACK,90);}catch(Exception ignored){} try{if(vibrator!=null)vibrator.vibrate(VibrationEffect.createOneShot(24,45));}catch(Exception ignored){}}
        void box(Canvas c,float l,float y,float r,float b,int col){p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRoundRect(l,y,r,b,dp(10),dp(10),p);}
        void outline(Canvas c,float l,float y,float r,float b,int col){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(col);c.drawRoundRect(l,y,r,b,dp(10),dp(10),p);p.setStyle(Paint.Style.FILL);}
        void label(Canvas c,String s,float x,float y,float z,int col){text.setTextSize(dp(z));text.setColor(col);text.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y,text);}
        void left(Canvas c,String s,float x,float y,float z,int col){text.setTextSize(dp(z));text.setColor(col);text.setTextAlign(Paint.Align.LEFT);c.drawText(s,x,y,text);}
        void button(Canvas c,float l,float y,float r,float b,String s,int col){box(c,l,y,r,b,col);outline(c,l,y,r,b,0xFF5C8BB8);label(c,s,(l+r)/2,y+(b-y)*.67f,13,Color.WHITE);}
        void bmp(Canvas c,Bitmap bm,float cx,float cy,float w,float h){if(bm==null)return;p.setFilterBitmap(false);c.drawBitmap(bm,null,new RectF(cx-w/2,cy-h/2,cx+w/2,cy+h/2),p);}

        @Override protected void onDraw(Canvas c){c.drawColor(0xFF071321);if(screen==0)title(c);else if(screen==1)world(c);else if(screen==2)battle(c);else if(screen==3)book(c);else if(screen==4)town(c);else if(screen==5)party(c);else if(screen==6)story(c);else breed(c);anim++;if(flash>0)flash--;postInvalidateDelayed(120);}
        void title(Canvas c){
            if(worldMap!=null)bmp(c,worldMap,W()/2,H()*.32f,W()*.94f,H()*.50f);p.setColor(0xAA071321);c.drawRect(0,0,W(),H(),p);
            label(c,"MONSTER TAMER",W()/2,dp(100),30,Color.WHITE);label(c,"GENERATIONS",W()/2,dp(132),18,0xFFFFD45A);label(c,"A family • a world • 120 monster bonds",W()/2,dp(168),12,0xFFD5E5F5);
            if(heroWalk!=null)bmp(c,heroWalk,W()/2+(float)Math.sin(anim*.08)*dp(5),H()*.33f,dp(95),dp(120));
            if(monsterArt[0]!=null)bmp(c,monsterArt[0],W()*.76f,H()*.35f,dp(85),dp(85));if(monsterArt[3]!=null)bmp(c,monsterArt[3],W()*.24f,H()*.35f,dp(100),dp(90));
            button(c,W()*.12f,H()*.57f,W()*.88f,H()*.65f,"CONTINUE ADVENTURE",0xFF1C4D70);button(c,W()*.12f,H()*.68f,W()*.88f,H()*.76f,"NEW JOURNEY",0xFF634027);
            label(c,"Childhood  →  Marriage  →  Children  →  Final Battle",W()/2,H()*.84f,11,Color.WHITE);label(c,"Tap to begin",W()/2,H()*.91f,10,0xFF9BB7D1);
        }
        void header(Canvas c,String s){p.setColor(0xFF12263C);c.drawRect(0,0,W(),dp(64),p);label(c,s,W()/2,dp(39),19,Color.WHITE);}
        void nav(Canvas c){float y=H()-dp(65);p.setColor(0xFF102238);c.drawRect(0,y,W(),H(),p);String[] n={"MAP","BOOK","TOWN","PARTY","BREED"};for(int i=0;i<5;i++)label(c,n[i],W()*(.10f+i*.20f),H()-dp(27),11,Color.WHITE);}
        void world(Canvas c){
            header(c,"WORLD • "+zones[zone]);if(worldMap!=null)bmp(c,worldMap,W()/2,H()*.33f,W()*.92f,H()*.44f);
            box(c,W()*.05f,H()*.52f,W()*.95f,H()*.70f,0xE8172B40);label(c,chapters[chapter-1],W()/2,H()*.57f,18,0xFFFFD45A);label(c,towns[zone],W()/2,H()*.615f,14,Color.WHITE);
            label(c,"Year "+year+" • Generation "+generation+" • Chapter "+chapter+"/8",W()/2,H()*.655f,11,0xFFD4E3F2);
            button(c,W()*.08f,H()*.73f,W()*.43f,H()*.81f,"EXPLORE",0xFF24536C);button(c,W()*.48f,H()*.73f,W()*.71f,H()*.81f,"STORY",0xFF63432D);button(c,W()*.76f,H()*.73f,W()*.92f,H()*.81f,"NEXT",0xFF3B5E45);nav(c);
        }
        void battle(Canvas c){
            header(c,boss?"FINAL BATTLE • WORLD SERPENT":"WILD ENCOUNTER");p.setColor(0xFF142F31);c.drawRect(0,dp(64),W(),H(),p);
            p.setColor(0xFF234E51);c.drawCircle(W()*.72f,dp(145),dp(68),p);p.setColor(0xFF1A383A);c.drawOval(W()*.35f,H()*.43f,W()*.98f,H()*.68f,p);
            Bitmap enemyBm=monsterFor(enemy);float bob=(float)Math.sin(anim*.12)*dp(4);if(enemyBm!=null)bmp(c,enemyBm,W()*.72f,dp(185)+bob,boss?dp(135):dp(92),boss?dp(125):dp(90));
            label(c,boss?"World Serpent":monsterDisplayName(enemy),W()*.72f,dp(250),15,Color.WHITE);bar(c,W()*.48f,dp(264),W()*.94f,dp(281),enemyHp,enemyMax(),0xFFD04D5B);
            if(heroWalk!=null)bmp(c,heroWalk,W()*.23f,dp(345),dp(88),dp(110));label(c,"Hero  Lv."+level,W()*.23f,dp(408),14,Color.WHITE);label(c,"HP "+playerHp+" / "+maxHp,W()*.23f,dp(431),11,0xFFFFD45A);label(c,"Bonded "+party.size()+"/4",W()*.23f,dp(452),10,0xFFBDEBC9);
            String[] a={"ATTACK","SKILL","TAME","GUARD","ITEM","RUN"};for(int i=0;i<6;i++){float l=i%2==0?W()*.06f:W()*.53f,y=dp(470)+(i/2)*dp(48);button(c,l,y,l+W()*.41f,y+dp(38),a[i],i==2?0xFF70472E:0xFF244B68);}
            if(flash>0){p.setColor(0x55FFFFFF);c.drawRect(0,0,W(),H(),p);}label(c,boss?"The final boss cannot be tamed.":"Weaken it before TAME for better odds.",W()/2,H()-dp(88),10,0xFFD5E5F5);
        }
        void bar(Canvas c,float l,float y,float r,float b,int value,int max,int col){box(c,l,y,r,b,0xFF0A1724);float q=Math.max(0,Math.min(1,value/(float)Math.max(1,max)));p.setColor(col);c.drawRoundRect(l+dp(2),y+dp(2),l+dp(2)+(r-l-dp(4))*q,b-dp(2),dp(6),dp(6),p);}
        int enemyMax(){return 70+chapter*22+(boss?160:enemy%5*10);} 
        String monsterDisplayName(int id){return id<monsterNames.length?monsterNames[id]:"Unknown Monster";}
        Bitmap monsterFor(int id){if(id<0)return null;return monsterArt[id%monsterArt.length];}
        void book(Canvas c){
            header(c,"MONSTER BOOK • "+tames+" / 120");int page=selected/12,start=page*12;for(int i=0;i<12;i++){int id=start+i;if(id>=120)break;float x=W()*(.17f+(i%3)*.33f),y=dp(105)+(i/3)*dp(78);Bitmap bm=monsterFor(id);if(caught[id]&&bm!=null)bmp(c,bm,x,y,dp(52),dp(52));else{p.setColor(0xFF243446);c.drawCircle(x,y,dp(24),p);label(c,"?",x,y+dp(8),20,0xFF73869B);}label(c,caught[id]?monsterDisplayName(id):"???",x,y+dp(31),9,caught[id]?Color.WHITE:0xFF718196);}label(c,"Page "+(page+1)+" / 10 • Tap left/right to browse",W()/2,H()-dp(83),11,0xFFFFD45A);nav(c);
        }
        void town(Canvas c){
            header(c,towns[zone]);label(c,"FAMILY TOWN",W()/2,dp(92),17,0xFFFFD45A);String[] jobs={"INN • heal party","SMITH • upgrade weapon","BREEDER • raise bonds","QUEST • advance story","SHOP • buy supplies"};
            for(int i=0;i<5;i++){float y=dp(112)+i*dp(54);box(c,W()*.06f,y,W()*.94f,y+dp(44),0xFF1A3045);left(c,npcs[(zone+i)%npcs.length],W()*.10f,y+dp(19),12,Color.WHITE);left(c,jobs[i],W()*.10f,y+dp(36),9,0xFF9DB6CE);}
            box(c,W()*.06f,H()*.54f,W()*.94f,H()*.69f,0xE8172B40);label(c,married?"Married to "+spouse:"Not married yet",W()/2,H()*.59f,14,0xFFFFD45A);label(c,children?"Your children are ready to join the quest.":"The next generation awaits.",W()/2,H()*.635f,10,Color.WHITE);nav(c);
        }
        void party(Canvas c){
            header(c,"PARTY • FAMILY & MONSTERS");label(c,"Hero  Lv."+level+"   HP "+playerHp+"/"+maxHp+"   Gold "+gold,W()/2,dp(91),12,Color.WHITE);if(heroWalk!=null)bmp(c,heroWalk,W()*.18f,dp(142),dp(60),dp(75));label(c,married?"Spouse • "+spouse:"Childhood Hero",W()*.18f,dp(190),10,0xFFFFD45A);
            for(int i=0;i<4;i++){float y=dp(225)+i*dp(76);box(c,W()*.07f,y,W()*.93f,y+dp(62),0xFF182D42);if(i<party.size()){int id=party.get(i);Bitmap bm=monsterFor(id);if(bm!=null)bmp(c,bm,W()*.20f,y+dp(30),dp(48),dp(48));left(c,monsterDisplayName(id),W()*.32f,y+dp(25),13,Color.WHITE);left(c,"Lv."+monsterLevel[id]+" • Bond "+(monsterLevel[id]*12)+"%",W()*.32f,y+dp(46),10,0xFF9FC1DA);}else left(c,"Empty slot",W()*.32f,y+dp(35),12,0xFF687B8F);}nav(c);
        }
        void story(Canvas c){
            header(c,"CHAPTER "+chapter+" • "+chapters[chapter-1]);label(c,"YEAR "+year+"   •   GENERATION "+generation,W()/2,dp(90),11,0xFFFFD45A);box(c,W()*.07f,dp(110),W()*.93f,H()*.58f,0xFF13263A);label(c,chapters[chapter-1],W()/2,dp(145),19,Color.WHITE);wrap(c,chapterText[chapter-1],W()*.13f,dp(190),W()*.87f,22,13,0xFFD6E5F2);
            if(chapter==4&&!married)button(c,W()*.12f,H()*.64f,W()*.88f,H()*.72f,"MARRY LYRA • BEGIN A FAMILY",0xFF70404E);else if(chapter>=6&&!children)button(c,W()*.12f,H()*.64f,W()*.88f,H()*.72f,"RAISE THE NEXT GENERATION",0xFF4E6640);else button(c,W()*.12f,H()*.64f,W()*.88f,H()*.72f,"ADVANCE CHAPTER",0xFF24536C);
            button(c,W()*.12f,H()*.76f,W()*.88f,H()*.84f,"RETURN TO MAP",0xFF263E55);
        }
        void wrap(Canvas c,String s,float l,float y,float r,float lh,float z,int col){text.setTextSize(dp(z));text.setColor(col);text.setTextAlign(Paint.Align.LEFT);String[] words=s.split(" ");String line="";for(String w:words){String test=line.length()==0?w:line+" "+w;if(text.measureText(test)>r-l){c.drawText(line,l,y,text);y+=dp(lh);line=w;}else line=test;}if(line.length()>0)c.drawText(line,l,y,text);}
        void breed(Canvas c){
            header(c,"BREEDING • THE FAMILY BOND");label(c,"Combine two bonded monsters to raise a stronger heir.",W()/2,dp(94),11,0xFFD6E5F2);box(c,W()*.08f,dp(120),W()*.92f,dp(270),0xFF172B40);int a=party.size()>0?party.get(0):0,b=party.size()>1?party.get(1):0;if(monsterFor(a)!=null)bmp(c,monsterFor(a),W()*.28f,dp(190),dp(75),dp(75));if(monsterFor(b)!=null)bmp(c,monsterFor(b),W()*.72f,dp(190),dp(75),dp(75));label(c,monsterDisplayName(a),W()*.28f,dp(242),11,Color.WHITE);label(c,monsterDisplayName(b),W()*.72f,dp(242),11,Color.WHITE);label(c,"✦",W()/2,dp(198),28,0xFFFFD45A);
            button(c,W()*.18f,dp(300),W()*.82f,dp(360),"BREED • 40 GOLD",0xFF5C4930);box(c,W()*.08f,dp(390),W()*.92f,dp(500),0xFF13263A);wrap(c,"Breeding creates a new bonded monster, increases its starting level, and can unlock rare evolution paths.",W()*.12f,dp(425),W()*.88f,21,12,Color.WHITE);nav(c);
        }
        void startEncounter(boolean finalBoss){boss=finalBoss;enemy=finalBoss?11:rng.nextInt(18);enemyHp=enemyMax();battleTurn=0;defending=false;screen=2;beep(true);invalidate();}
        void winBattle(){wins++;gold+=18+chapter*8;xp+=28+chapter*14;while(xp>=level*80){xp-=level*80;level++;maxHp=100+level*24;playerHp=maxHp;}for(int id:party)monsterLevel[id]=Math.min(30,monsterLevel[id]+1);if(chapter<8&&wins%3==0)chapter=Math.min(8,chapter+1);playerHp=Math.min(maxHp,playerHp+20);persist();}
        void attack(){int dmg=18+level*3+weaponPower[weapon]+rng.nextInt(10);enemyHp-=dmg;battleTurn++;beep(true);if(enemyHp<=0){winBattle();screen=1;return;}enemyTurn();}
        void skill(){int dmg=28+level*4+weapon*5+rng.nextInt(16);enemyHp-=dmg;battleTurn++;flash=8;beep(true);if(enemyHp<=0){winBattle();screen=1;return;}enemyTurn();}
        void enemyTurn(){int dmg=8+chapter*3+rng.nextInt(12);if(defending)dmg/=2;playerHp-=dmg;defending=false;if(playerHp<=0){playerHp=maxHp/2;gold=Math.max(0,gold-25);persist();screen=1;}}
        void tame(){if(boss){beep(false);return;}int chance=25+(enemyMax()-enemyHp)*55/enemyMax()+Math.min(15,chapter*2);if(rng.nextInt(100)<chance){int id=enemy%120;if(!caught[id]){caught[id]=true;tames++;if(party.size()<4)party.add(id);}monsterLevel[id]=Math.max(1,level);gold+=8;persist();beep(true);screen=1;}else{beep(false);enemyTurn();}}
        void nextChapter(){if(chapter<8){chapter++;year+=3;zone=Math.min(7,chapter-1);}persist();screen=1;beep(true);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();if(screen==0){screen=1;invalidate();return true;}
            if(screen==1){if(y>H()*.71f&&y<H()*.84f){if(x<W()*.46f)startEncounter(false);else if(x<W()*.74f){screen=6;}else nextChapter();}else if(y>H()-dp(70)){if(x<W()*.2f)screen=1;else if(x<W()*.4f)screen=3;else if(x<W()*.6f)screen=4;else if(x<W()*.8f)screen=5;else screen=7;}}
            else if(screen==2){if(y>dp(465)&&y<dp(625)){int col=x<W()/2?0:1,row=(int)((y-dp(470))/dp(48)),idx=row*2+col;if(idx==0)attack();else if(idx==1)skill();else if(idx==2)tame();else if(idx==3){defending=true;enemyTurn();}else if(idx==4){playerHp=Math.min(maxHp,playerHp+35);enemyTurn();}else if(idx==5){screen=1;persist();}}}
            else if(screen==3){if(y>dp(80)&&y<H()-dp(70)){if(x<W()/2)selected=Math.max(0,selected-12);else selected=Math.min(108,selected+12);}else if(y>H()-dp(70)){if(x<W()*.2f)screen=1;else if(x<W()*.4f)screen=3;else if(x<W()*.6f)screen=4;else if(x<W()*.8f)screen=5;else screen=7;}}
            else if(screen==4||screen==5||screen==7){if(y>H()-dp(70)){if(x<W()*.2f)screen=1;else if(x<W()*.4f)screen=3;else if(x<W()*.6f)screen=4;else if(x<W()*.8f)screen=5;else screen=7;}else if(screen==4&&y>dp(110)&&y<dp(420)){if(y<dp(170)){playerHp=maxHp;persist();beep(true);}else if(y<dp(225)){if(weapon<4&&gold>=40+weapon*40){gold-=40+weapon*40;weapon++;persist();beep(true);}}else if(y<dp(280)){screen=7;}else if(y<dp(335)){screen=6;}}else if(screen==7&&y>dp(290)&&y<dp(375)){if(party.size()>=2&&gold>=40){gold-=40;int id=Math.min(119,Math.max(0,party.get(0)+party.get(1)+rng.nextInt(6)));caught[id]=true;tames++;monsterLevel[id]=Math.min(30,Math.max(2,level+1));if(party.size()<4)party.add(id);persist();beep(true);}else beep(false);}}
            else if(screen==6){if(y>dp(600)&&y<dp(750)){if(chapter==4&&!married){married=true;spouse="Lyra";year+=2;generation=2;persist();beep(true);}else if(chapter>=6&&!children){children=true;generation=3;year+=4;persist();beep(true);}else nextChapter();}else if(y>dp(750)&&y<dp(850)screen=1;}
            invalidate();return true;}
    }
}

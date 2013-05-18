import java.awt.*;

public class Sniper extends Character{//--------------------------------スナイパークラス
	CharaSet cs;//位置や何やら
	int[] gumx = new int[3], gumy = new int[3];//パチンコのゴム描画用
	int muzzleX, muzzleY;//発射口（弾を吐き出す位置）
	static final int maxhp = 10;//最大耐久力
	static int hp, damage;//現在耐久力,累積ダメージ
	
	static void damage(){//敵が侵入してきたらダメージを受ける
		hp--;
		damage++;
		if(hp <= 0)//耐久力がなくなるとゲームオーバー
			Catapult.GAMEOVER = true;
	}
	static void recover(){
		hp++;
		if (hp > maxhp)
			hp = maxhp;
	}
	static void sethp(int hp){
		Sniper.hp = hp;
		damage = 0;
	}
	Sniper(Image img){//画像登録とキャラセット初期化
		super(img);
		cs = new CharaSet();
	}
	
	void setting(int hp){//初期設定
		sethp(hp);
		locate(0, 440);//初期位置
	}
	void locate(int x, int y){//各所位置設定
		cs.x = x;           cs.y = y;
		gumx[0] = x;        gumy[0] = y;
		gumx[2] = x + w -1; gumy[2] = y;
		muzzleX = x + w/2;  muzzleY = y;
		gumx[1] = muzzleX;  gumy[1] = muzzleY;
	}
	void setTama(int h){//弾をセットするとゴムが伸びる
		gumy[1] = muzzleY + h/2;
	}
	void takeAim(int x, int y){//ゴムがもっと伸びる
		gumx[1] = x; gumy[1] = y;
	}
	void shot(){//打ち出した後はゴムが戻る
		gumx[1] = muzzleX;  gumy[1] = muzzleY;
	}
	void paint(Catapult c){//画像、ゴム、残り耐久力描画
		c.mg.drawImage(image, cs.x, cs.y, w, h, c);
		c.mg.setColor(new Color(0xAFEEEE));
		c.mg.drawPolyline(gumx, gumy, 3);//パチンコのゴム
		c.mg.fillRect(280, 480, 10 * maxhp + 2, 19);//耐久力
		c.mg.setColor(new Color(0x800000));
		c.mg.fillRect(281, 481, 10 * hp ,17);//残り耐久力表示
	}
	void paintExplain(Catapult c, int slide){//操作説明用
		if(slide < 20)
			slide = 20;
		c.mg.drawImage(image, slide+40, 440, w, h, c);
		c.mg.setColor(new Color(0xEEEEEE));
		c.mg.drawString("パチンコ。これがマウスと連動して動きます。" , slide+w+50, 450);
		c.mg.drawString("Z、X、C、V、Bキーを押すと弾が補充され、" , slide+60, 470);
		c.mg.drawString("弾をドラッグすることで打ち出すことが出来ます。" , slide+60, 490);
	}
	void paintExplain(Catapult c){//操作説明用2
		c.mg.setColor(new Color(0xAFEEEE));
		c.mg.fillRect(280, 480, 10 * maxhp + 2, 19);//耐久力
		c.mg.setColor(new Color(0x800000));
		c.mg.fillRect(281, 481, 10 * hp ,17);//残り耐久力表示
	}
	int muzzleX(){//発射口を返す
		return muzzleX;
	}
	int muzzleY(){//発射口
		return muzzleY;
	}
}
class Tama extends Character{//-------------------------------------------弾クラス
	static int Shoten = 0;//打ち出された弾の総数
	static int Hits = 0;//敵に当たった弾の総数
	//残り弾数、連続射撃可能弾数、貫通性、滞空時間、打ち出された弾の個別数
	int rest, snum, hp, stay, shoten;
	CharaSet[] cs;//打ち出す弾
	int id, leadx, leady;//打ち出す弾の整理番号、誘導弾用（開始点座標保存）
	CharaSet restview;//残り弾数表示用座標
	boolean SILVER = false, GOLD = false;//特殊弾用
	
	Tama(Image img){//画像設定とキャラセット初期化
		super(img);
		restview = new CharaSet();
	}
	void setting(int rest, int snum, int hp, int stay){//初期化
		shoten = 0;
		this.rest = rest;
		this.snum = snum;
		this.hp = hp;
		this.stay = stay;
		id = 0;
		cs = new CharaSet[snum+1];
		for(int i=0; i<cs.length; i++)
			cs[i] = new CharaSet(hp, 0, stay);
	}
	void setting(int rest, int snum, int hp, int stay, int sp){//初期化
		setting(rest, snum, hp, stay);
		if(sp == 1)
			SILVER = true;
		else if(sp == 2)
			GOLD = true;
	}
	void restLocate(int x, int y){//残り弾数表示用座標位置設定
		restview.x = x;  restview.y = y;
	}
	void addRest(){//弾補充
		rest++;
		if (rest > 99)
			rest = 99;
	}
	void addRest(int n){//弾補充
		rest += n;
		if (rest > 99)
			rest = 99;
	}
	boolean canAim(){//弾をセットできるかどうか
		if (rest > 0){
			int flynum = 0;
			for (int i=0; i < cs.length; i++)
				if(cs[i].FLY)
					flynum++;
			if (flynum < snum)
				return true;
		}
		return false;
	}
	void takeAim(int x, int y){//狙いをつける
		cs[id].AIM = true;
		cs[id].x = x - w/2;   cs[id].y = y - h/2;
	}
	void setVelocity(int x, int y){//速度設定
		cs[id].setVelocity((x-(cs[id].x+w/2))/3, (y-(cs[id].y+h/2))/3);
	}
	void setLead(int x, int y){//ドラッグの初期座標（誘導弾）
		leadx = x; leady = y;
	}
	void leading(int x, int y){//ドラッグ中の座標（誘導弾）
		int preid;
		if(id == 0)
			preid = snum;
		else
			preid = id - 1;//誘導できるのは１つ前の弾
		cs[preid].setVelocity((x - leadx)/4, (y - leady)/4);
	}
	void notAim(){//狙わない(弾セット解除)
		cs[id].AIM = false;
	}
	void shot(){//弾を打ち出す
		Shoten++;
		shoten++;
		if(cs[id].AIM){
			cs[id].AIM = false;
			cs[id].FLY = true;
			int v = (cs[id].vx * cs[id].vx) + (cs[id].vy * cs[id].vy);
			if (v < 200)//弾のスピードによって鳴らす音を変える
				Sound.play(Sound.shot01);
			else if (v < 400)
				Sound.play(Sound.shot02);
			else
				Sound.play(Sound.shot03);
			rest--;//残り弾数減らす
			cs[id].setSize(w, h);//大きさ設定
			if(id < cs.length - 1)//次の弾へ
				id++;
			else
				id=0;
			cs[id] = new CharaSet(hp, 0, stay);//次の弾を得る
		}
	}
	void division(int i){
		cs[id].x = cs[i].x;
		cs[id].y = cs[i].y;
		cs[id].stay = cs[i].stay;
		cs[id].FLY = true;
		cs[id].ORI = false;
		cs[id].setSize(w, h);//大きさ設定
		if(id < cs.length - 1)//次の弾へ
			id++;
		else
			id=0;
		cs[id] = new CharaSet(hp, 0, stay);//次の弾を得る
	}
	void divisionG(int i){
		cs[id].vx = cs[i].vx;
		cs[id].vy = cs[i].vy;
		division(i);
		cs[id].vx = -cs[i].vx;
		cs[id].vy = -cs[i].vy;
		division(i);
		cs[id].vx = cs[i].vy;
		cs[id].vy = -cs[i].vx;
		division(i);
		cs[id].vx = -cs[i].vy;
		cs[id].vy = cs[i].vx;
		division(i);
	}
	boolean isOn(int x, int y){//当たり判定
		cs[id].setSize(w+10, h+20);//ドラッグ判定用に余裕を持たせる
		return cs[id].isOn(x, y);
	}
	void paint(Catapult c){//各種画像描画
		//残り弾数表示
		restview.y +=7;//位置調整
		c.mg.drawImage(image, restview.x, restview.y, w, h, c);
		restview.y -=7;//位置調整
		restview.paintNum(c, rest);//残り弾数表示
		
		for(int i=0; i<cs.length; i++){
			if(cs[i].FLY){//飛行中の弾表示
				c.mg.drawImage(image, cs[i].x, cs[i].y, w, h, c);
				boolean temp = cs[i].moveA(left, right, up, down);//次状態へ遷移
				if(SILVER && temp){
					if(cs[i].vx > 0)
						cs[id].vx = cs[i].vx - 1;
					else
						cs[id].vx = cs[i].vx + 1;
					division(i);
				}
			}else if(cs[i].AIM){//狙いをつけいている弾表示
				c.mg.drawImage(image, cs[i].x, cs[i].y, w, h, c);
			}
		}
	}
	void paintExplain(Catapult c){//操作説明用
		//残り弾数表示
		restview.y +=7;//位置調整
		c.mg.drawImage(image, restview.x, restview.y, w, h, c);
		restview.y -=7;//位置調整
		restview.paintNum(c, rest);//残り弾数表示
	}
}
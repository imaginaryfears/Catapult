import java.awt.*;

public class CharaSet{//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%CharaSetクラス
	//数字画像、爆発画像、それぞれの画像の大きさ
	static Image[] num = new Image[10], bomb = new Image[3];
	static int num_w, num_h, bomb_w, bomb_h;
	//速度、位置、画像の大きさ、耐久力、滞空時間、爆発を表示用変数
	int vx, vy, x, y, w, h, hp, stay, bID;
	//狙っている状態、飛行中、爆発中、進入成功、1弾目
	boolean AIM, FLY, BOMB, COME, ORI;
	//点数（貫通数）、
	int point;

	static void setImage(Catapult c){//数字画像取得
		for(int i=0; i < num.length; i++){
			num[i] = c.getImage(c.getCodeBase(), 
								"img/" + Integer.toString(i) + ".gif");
			c.met.addImage(num[i], c.mid++);
		}
		//爆発画像取得
		bomb[0] = c.getImage(c.getCodeBase(), "img/bomb01.gif");
		bomb[1] = c.getImage(c.getCodeBase(), "img/bomb02.gif");
		bomb[2] = c.getImage(c.getCodeBase(), "img/bomb03.gif");
		c.met.addImage(bomb[0], c.mid++);
		c.met.addImage(bomb[1], c.mid++);
		c.met.addImage(bomb[2], c.mid++);
	}
	static void getImageSize(Catapult c){//各画像の大きさ取得
		num_w = num[0].getWidth(c);
		num_h = num[0].getHeight(c);
		bomb_w = bomb[0].getWidth(c);
		bomb_h = bomb[0].getHeight(c);
	}
	CharaSet(){//初期化
		vx = 0;   vy = 0;   stay = -1;   bID = 0;
		AIM = false;   FLY = false;   BOMB = false;   COME = false;  ORI = true;
	}
	CharaSet(int hp, int point, int stay){//耐久力設定用+点数（貫通数）+滞空時間
		this();
		this.hp = hp;  this.point = point;  this.stay = stay;
	}
	void setSize(int w, int h){//大きさ取得
		this.w = w;    this.h = h;
	}
	void setVelocity(int x, int y){//速度設定
		this.vx = x;   this.vy = y;
	}
	void move(int left, int right, int up, int down){//次の位置に動く
		x += vx;       y += vy;
		if(x < left){//左境界で反射
			x = left;  vx = -vx;
		}else if(x > right){//右境界で反射
			x = right; vx = -vx;
		}
		if(y < up)//上の境界
			FLY = false;
		if(y > down){//下の境界
			FLY  = false;
			BOMB = true;
			COME = true;
		}
	}
	//動作＋反射音付き＋滞空時間考慮
	boolean moveA(int left, int right, int up, int down){
		x += vx;    y += vy;  stay--;
		if(stay == 0){//滞空時間終了,
			FLY = false;
			AIM = false;
			return false;
		}
		if(y < up || y > down){//上下境界線を越えると飛行終了
			FLY = false;
			AIM = false;
			return false;
		}
		if(x < left){//左境界で反射で音鳴らす
			Sound.play(Sound.reflect);
			x = left;  vx = -vx;
			return true;
		}else if(x > right){//右境界で反射で音鳴らす
			Sound.play(Sound.reflect);
			x = right; vx = -vx;
			return true;
		}
			return false;
	}
	boolean isOn(int x, int y){//当たり判定
		if(this.x <= x && x <= this.x+w && this.y <= y && y <= this.y+h){
			return true;
		}else{
			return false;
		}
	}
	CharaSet isBomb(CharaSet cs){//爆発判定
		if(isOn(cs.x + cs.w/2, cs.y + cs.h/2)){
			cs.hp--;//当たったほうの体力減少
			if(cs.ORI) Tama.Hits++;//最初の一撃だけ換算
			cs.point++;//tamaの貫通数
			cs.ORI = false;
			if(cs.hp <= 0){
				cs.FLY = false;
				cs.AIM = false;
			}
			hp--;//当てられたほうの体力減少
			if(hp <= 0){
				point *= cs.point;
				BOMB= true;
				FLY = false;
			}
		}
		return cs;
	}
	void paintNum(Catapult c, int n){//数字画像描画
		int place = x+20;
		if(n == 0)
			c.mg.drawImage(num[n%10], place, y, num_w, num_h, c);
		else{
			while(n > 0){//一桁ごと考慮
				c.mg.drawImage(num[n%10], place, y, num_w, num_h, c);
				n /= 10;
				place -= 10;
			}
		}
	}
	void paintBomb(Catapult c){//爆発画像を描く（遅延でアニメ）
		int delay = 7;
		//FLY = false;
		if(bID == 0)//初めの1回だけ音を鳴らす
			Sound.play(Sound.bomb01);
		if(bID < bomb.length * delay + 10){
			if(bID < bomb.length * delay){
				c.mg.drawImage(bomb[(bID++)/delay], x, y, bomb_w, bomb_h, c);
			}else if(!COME){
				paintNum(c, point);
				c.score += point/delay; 
				bID++;
			}
		}else//爆発アニメ終了
			BOMB = false;
	}
}

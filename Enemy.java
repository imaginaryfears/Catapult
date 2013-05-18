import java.awt.*;
import java.util.Random;

public class Enemy extends Character{//----------------------------------------敵クラス
	static int Killed = 0;//倒された敵の数
	CharaSet cs[] = new CharaSet[10];//同じ種類の敵の数
	int hp, point, id;//耐久力、点数、敵のID
	int vx, vy, cvx, cvy;//速度（縦横）、変化
	
	Enemy(Image img){//画像設定、下限界地点設定
		super(img);
		down = 430;
		id = 0;
	}
	void setting(int hp, int point, int vx, int vy, int cvx, int cvy){//初期設定
		this.hp = hp;
		this.point = point;
		this.vx  = vx;   this.vy  = vy;
		this.cvx = cvx;  this.cvy = cvy;
		for(int i=0; i<cs.length; i++)
			cs[i] = new CharaSet(hp, point, -1);
	}
	void paint(Catapult c, Tama[] t){//画像描画と次状態への遷移
		ene : for (int i=0; i < cs.length; i++){
			if(cs[i].BOMB){
				cs[i].paintBomb(c);//爆発イメージ
			}else if(cs[i].FLY){
				c.mg.drawImage(image, cs[i].x, cs[i].y, w, h, c);//描画
				cs[i].move(left, right, up, down);//次への動き
				change(cs[i]);//速度変更
				if(cs[i].COME){//進入でダメージを与える
					c.BackColor = Color.gray;//背景色
					Sniper.damage();
				}
				//弾の当たり判定
				for (int j = 0; j < t.length; j++)//j:弾の種類（緑、青、・・・）
					for (int k = 0; k < t[j].cs.length; k++)//k:同種類内の弾
						if (t[j].cs[k].FLY){
							t[j].cs[k] = cs[i].isBomb(t[j].cs[k]);//当たり判定
							if (t[j].GOLD && !(t[j].cs[k].FLY))//金弾の場合
								t[j].divisionG(k);
							if (cs[i].BOMB){
								Enemy.Killed++;
								continue ene;
							}
						}
			}
		}
	}
	boolean isFLY(){
		for(int i=0; i<cs.length; i++)
			if (cs[i].FLY)
				return true;
		return false;
	}
	void readyGo(){//攻撃前の位置、速度設定
		cs[id].setSize(w+2, h+2);//画像の大きさを設定しておく
		cs[id].y  = up;//上の画面外
		cs[id].x  = (int)(Math.random() * (right-left)) + left;
		cs[id].vx = (int)(Math.random() * (vx*2+1)) - vx;
		cs[id].vy = vy;
		cs[id].FLY = true;
		id++;
		if(id >= cs.length)
			id = 0;
		cs[id] = new CharaSet(hp, point, -1);//次の初期化
	}
	void change(CharaSet ch){//飛行中の速度変化
	//x方向
		Random ran = new Random();
		if((ran.nextInt(2)) == 0)
			ch.vx += (ran.nextInt(cvx*2+1)) - cvx;
		if(ch.vx > vx)
			ch.vx = vx;
		else if(ch.vx < -vx)
			ch.vx = -vx;
	//y方向
		if((int)(ran.nextInt(2)) == 0)
			ch.vy += (ran.nextInt(cvy*2+1)) - cvy;
		if(ch.vy > vy)
			ch.vy = vy;
		else if(ch.vy <= 0)
			ch.vy = 0;
	}
}
class Item extends Character{//----------------------------------------アイテムクラス
	String dir = "img/";
	String[] fileName = {"item_b.gif" , "item_g.gif" , "item_r.gif" ,
						 "item_s.gif" , "item_k.gif" , "item_h.gif"};
	Image image[] = new Image[fileName.length];
	int id;//アイテムのID
	CharaSet cs;
	
	Item(Catapult c){//画像設定、下限界地点設定
		for (int i=0; i < image.length; i++){
			image[i] = c.getImage(c.getCodeBase(), dir+fileName[i]);
			c.met.addImage(image[i], c.mid++);
		}
		down = 430;
	}
	void setting(){
		id = 0;
		cs = new CharaSet();
	}
	void getImageSize(Catapult c){//画像のオリジナルの大きさ取得
		w = image[0].getWidth(c);  h = image[0].getHeight(c);
		right -= w;//跳ね返り用
		up -= h;   //一部でも描画
		down -= h; //進入したら終了
	}

	void paint(Catapult c, Tama[] t){//画像描画と次状態への遷移
		if(cs.FLY){
			c.mg.drawImage(image[id], cs.x, cs.y, w, h, c);//描画
			cs.move(left, right, up, down);//次への動き
			if(cs.COME){//進入でアイテムを与える
				Sound.play(Sound.itemGet);//音鳴らす
				switch(id){
				case 0: t[0].addRest(15); break;
				case 1: t[1].addRest( 8); break;
				case 2: t[2].addRest( 3); break;
				case 3: t[3].addRest( 2); break;
				case 4: t[4].addRest( 3); break;
				case 5: Sniper.recover(); break;
				}
				cs.COME = false;
				return;
			}
			//弾の当たり判定
			for (int j = 0; j < t.length; j++)//j:弾の種類（緑、青、・・・）
				for (int k = 0; k < t[j].cs.length; k++)//k:同種類内の弾
					if (t[j].cs[k].FLY){
						t[j].cs[k] = cs.isBomb(t[j].cs[k]);//当たり判定
						if (cs.BOMB){
							Sound.play(Sound.itemChange);//音鳴らす
							if(id < image.length-1)
								id++;
							else
								id = 0;
							cs.FLY = true;
							cs.BOMB = false;
							return;
						}
			}
		}
	}
	void readyGo(){//攻撃前の位置、速度設定
		if(!cs.FLY){
			id = 0;
			cs = new CharaSet();
			cs.setSize(w+2, h+2);//画像の大きさを設定しておく
			cs.y  = up;//上の画面外
			cs.x  = (int)(Math.random() * (right-left)) + left;
			cs.vy = (int)(Math.random() * 2) + 1;
			cs.FLY = true;
		}
	}

}

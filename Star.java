import java.awt.*;
import java.util.Random;

public class Star{//------------------------------------------星表示クラス
	Image iconON, iconOFF;//アイコン
	int w, h, sx, sy;//アイコンの大きさと設置位置
	boolean ON = true;//星を表示するかどうか
	CharaSet[] cs;//数々の星
	int left=3, right=396, up=0, down=440;//表示領域

	void setImage(Catapult c){//アイコン読み込み
		iconON = c.getImage(c.getCodeBase(), "img/star.gif");
		iconOFF = c.getImage(c.getCodeBase(), "img/star_s.gif");
		c.met.addImage(iconON, c.mid++);
		c.met.addImage(iconOFF, c.mid++);
	}
	Star(int num){
		cs = new CharaSet[num];
		Random random = new Random();
		for(int i=0; i < cs.length; i++){
			cs[i] = new CharaSet();
			cs[i].x = random.nextInt(right-left) + left;
			cs[i].y = random.nextInt(down-up) + up;
			cs[i].vy = random.nextInt(3) + 1;
			cs[i].FLY = true;
		}
	}
	void locateIcon(int x, int y){//位置取得
		sx = x;    sy = y;
	}
	void getImageSize(Catapult c){//画像のオリジナルの大きさ取得
		w = iconON.getWidth(c);  h = iconON.getHeight(c);
	}
	void paint(Catapult c){//星描画
		Random random = new Random();
		if(ON){
			c.mg.setColor(new Color(0xffffcc));
			for(int i=0; i < cs.length; i++){
				if(cs[i].FLY){
					c.mg.drawLine(cs[i].x, cs[i].y, cs[i].x, cs[i].y);
					cs[i].move(left, right, up, down);
					if(cs[i].COME){
						cs[i].x = random.nextInt(right-left) + left;
						cs[i].y = 1;
						cs[i].vy = random.nextInt(3) + 1;
						cs[i].FLY = true;
						cs[i].COME = false;
					}
				}
			}
		}
	}
	void paintIcon(Catapult c){//アイコン描画
		if(ON){
			c.mg.drawImage(iconON, sx, sy, w, h, c);//アイコン
		}else
			c.mg.drawImage(iconOFF, sx, sy, w, h, c);
	}
	void clickIcon(int x, int y){//アイコンをクリックしたとき
		if(sx < x && x < sx+w && sy < y && y < sy+h){
			if(ON)
				ON = false;
			else
				ON = true;
		}
	}
}

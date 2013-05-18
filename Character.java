import java.awt.*;


public class Character{//##############################################キャラクタークラス
	//キャラの画像とその大きさの情報
	Image image;
	int w, h;
	//移動範囲
	int left=3, right=396, up=0, down=500;
	
	Character(){
	}
	Character(Image i){
		image = i;
	}
	void setSize(int w, int h){//大きさ取得
		this.w = w;    this.h = h;
	}
	void getImageSize(Catapult c){//画像のオリジナルの大きさ取得
		w = image.getWidth(c);  h = image.getHeight(c);
		right -= w;//跳ね返り用
		up -= h;   //一部でも描画
		down -= h; //進入したら終了
	}
}
class ClickImage{//-------------------------------クリック判定用の画像
	//表示する画像、マウスをのせたときの画像
	Image image, subimage = null;
	int sx, sy, w, h;//位置と大きさ
	boolean FOCUS = false;//マウスをのせているか
	
	ClickImage(Image i){
		image = i;
	}
	ClickImage(Catapult c, String dir, String main, String sub){
		image = c.getImage(c.getCodeBase(), dir + main);
		c.met.addImage(image, c.mid++);
		subimage = c.getImage(c.getCodeBase(), dir + sub);
		c.met.addImage(subimage, c.mid++);
	}
	ClickImage(Catapult c, String dir, String main){
		image = c.getImage(c.getCodeBase(), dir + main);
		c.met.addImage(image, c.mid++);
	}
	void setSubImage(Image i){
		subimage = i;
	}
	void getImageSize(Catapult c){//画像のオリジナルの大きさ取得
		w = image.getWidth(c);  h = image.getHeight(c);
	}
	void locate(int x, int y){//位置設定
		sx = x; sy=y;
	}
	boolean isOn(int x, int y){//当たり判定
		boolean preFOCUS = FOCUS;
		if(sx < x && x < sx+w && sy < y && y < sy+h){
			if(!preFOCUS){//マウスがのると音を出す
				FOCUS = true;
				Sound.play(Sound.set);
			}
			return true;
		}else{
			FOCUS = false;
			return false;
		}
	}
	void paint(Catapult c){//画像描画
		if(FOCUS && subimage !=null){//サブ画像を読み込んでいるなら
			c.mg.drawImage(subimage, sx, sy, c);
		}else{
			c.mg.drawImage(image, sx, sy, c);
		}
	}

}

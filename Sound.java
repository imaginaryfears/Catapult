import java.applet.*;
import java.awt.*;

public class Sound{//------------------------------------------サウンドクラス
	static Image iconON, iconOFF;//アイコン
	static int w, h, sx, sy;//アイコンの大きさと設置位置
	static boolean ON = true;//音を鳴らすかどうか
	//音ファイル
	static String dir = "sound/";
	static String[] fileName = {"reflect.wav",
	 "shot01.wav", "shot02.wav", "shot03.wav", "bomb01.wav",
	 "itemchange.wav", "itemget.wav", "set.wav", "open.wav"};
	static AudioClip ac[] = new AudioClip[9];
	static final int reflect    = 0;//通し番号
	static final int shot01     = 1;
	static final int shot02     = 2;
	static final int shot03     = 3;
	static final int bomb01     = 4;
	static final int itemChange = 5;
	static final int itemGet    = 6;
	static final int set        = 7;
	static final int open       = 8;

	static void setImage(Catapult c){//アイコン用画像読み込み
		iconON = c.getImage(c.getCodeBase(), "img/sound.gif");
		iconOFF = c.getImage(c.getCodeBase(), "img/soundoff.gif");
		c.met.addImage(iconON, c.mid++);
		c.met.addImage(iconOFF, c.mid++);
	}
	Sound(){
	}
	static void locateIcon(int x, int y){//位置取得
		sx = x;    sy = y;
	}
	static void getImageSize(Catapult c){//画像のオリジナルの大きさ取得
		w = iconON.getWidth(c);  h = iconON.getHeight(c);
	}
	static void setAudio(){//効果音設定
		for(int i=0; i < ac.length; i++)
			ac[i] = Catapult.newAudioClip
					(Catapult.class.getResource(dir+fileName[i]));
	}
	static void paintIcon(Catapult c){//アイコン描画
		if(ON)
			c.mg.drawImage(iconON, sx, sy, w, h, c);
		else
			c.mg.drawImage(iconOFF, sx, sy, w, h, c);
	}
	static void clickIcon(int x, int y){//アイコンをクリックしたとき
		if(sx < x && x < sx+w && sy < y && y < sy+h){
			if(ON)
				ON = false;
			else
				ON = true;
		}
	}
	static void play(int x){//通し番号で指定した音を鳴らす
		if(ON)
			ac[x].play();
	}

}

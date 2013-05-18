import java.awt.Color;
import java.io.*;
import java.net.*;


public class ScoreData{
	String data;
	String score, name, 
		   shoten, t0shoten, t1shoten, t2shoten, t3shoten, t4shoten,
		   killed, rate, damage, com;
	int rank;
	boolean REGIST = false, PASS = false, SENT = false;
	
	public ScoreData(){

	}
	void makeData(String n, String c){//送信するデータ文字列を生成
		PASS = false;
		name = n;
		com = c;
		data = score + "," + name + "," + shoten + ","
		+ t0shoten + "," + t1shoten + "," + t2shoten + "," + t3shoten + ","+ t4shoten + ","
		+ killed + "," + rate + "," + damage + "," + com;
	}
	void getScore(int score, Tama[] tama){
		SENT = false;//データ送信前
		this.score = Integer.toString(score);
		shoten = Integer.toString(Tama.Shoten);
		t0shoten = Integer.toString(tama[0].shoten);
		t1shoten = Integer.toString(tama[1].shoten);
		t2shoten = Integer.toString(tama[2].shoten);
		t3shoten = Integer.toString(tama[3].shoten);
		t4shoten = Integer.toString(tama[4].shoten);
		killed = Integer.toString(Enemy.Killed);
		damage = Integer.toString(Sniper.damage);
		if(Tama.Shoten ==0){
			rate = "---";
		}else{
			int temp = Tama.Hits *100 / Tama.Shoten;
			rate = Integer.toString(temp);
		}
	}
	void passPaint(Catapult p){
		p.mg.setColor(new Color(50, 100, 100, 200));
		p.mg.fillRect(50, 200, 300, 150);
		p.mg.setColor(new Color(0xffff88));		
		p.mg.drawString("パスワードを入力してください", 110, 250);						
	}
	void paint(Catapult p){//スコア描画or入力画面
		p.mg.setColor(new Color(100, 100, 200, 50));
		p.mg.fillRect(50, 50, 300, 300);
		p.mg.setColor(new Color(0xeeeeee));
		if (REGIST){
			p.mg.drawString("登録用の名前を入力してください！", 110, 110);
			p.mg.drawString("名前：", 130, 153);
			p.mg.drawString("↓コメントも登録できます↓", 120, 200);
		} else{
			p.mg.drawString("スコア　 :  " + score, 80, 100);
			p.mg.drawString("発射弾数 :  " + shoten, 80, 130);
			p.mg.drawString("　青 : " + t0shoten, 80, 150);
			p.mg.drawString("　緑 : " + t1shoten, 130, 150);
			p.mg.drawString("　赤 : " + t2shoten, 180, 150);
			p.mg.drawString("　銀 : " + t3shoten, 230, 150);
			p.mg.drawString("　金 : " + t4shoten, 280, 150);
			p.mg.drawString("倒した敵 :   " + killed, 80, 180);
			p.mg.drawString("狙撃率　 :  " + rate+ "%", 80, 210);
			p.mg.drawString("敵の侵入回数 :   " + damage, 80, 240);
			p.mg.setColor(new Color(0xffff88));
			if (PASS){
				p.mg.drawString("↓次のステージへのパスワード↓", 110, 265);				
			}else if (SENT){
				if(rank == 0)
					p.mg.drawString("このスコアは残念ながらランク外でした。", 80, 280);
				else
					p.mg.drawString("このスコアは" + rank + "位にランクインしました！！", 80, 280);
			}else
				p.mg.drawString("このスコアをランキングに送信できます！！", 80, 280);
		}
	}

	void sendScore(){//データを送信する
        try {
            // CGIを表すURLオブジェクト
            URL cgiURL = new URL("http://www48.tok2.com/home/knt/catapult/ranking/scorewrite.cgi");
            // 接続
            URLConnection uc = cgiURL.openConnection();
            uc.setDoOutput(true);
            uc.setUseCaches(false);
            // CGIへの書き込み用ストリームを開く
            PrintWriter pw = new PrintWriter(uc.getOutputStream());
            // CGIにデータを送信する
            pw.print(data);
            // ストリームを閉じる
            pw.close();
            
            // CGIからの読み込み用ストリームを開く
            BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            // CGIの出力を読み込んでコンソール画面に表示
            String line = "";
            if ((line = br.readLine()) != null) {
            	//System.out.print(line);
            	rank = Integer.parseInt(line);//順位情報を受け取る
            }
            SENT = true;//データ送信しました。
            // ストリームを閉じる
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

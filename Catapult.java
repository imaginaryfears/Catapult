import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Catapult extends Applet implements Runnable{
	//アプレット仕様
	static final int MainScreen = 400;//アプレットの幅
	static final int ScreenHeight = 500;//アプレットの高さ
	Color BackColor = new Color(0x000000);//背景色
	int TIME = 50;//遅延時間
	
	//キャラクター関係
	Sniper sniper;//パチンコ
	Item item;//アイテム	
	Tama tama[] = new Tama[5];//弾
	String Idir = "img/";
	String tamaFile[] = {"tama_b.gif", "tama_g.gif", "tama_r.gif",
		"tama_s.gif", "tama_k.gif"};
	Enemy enemy[] = new Enemy[5];//敵キャラ
	int tid = 0;//弾の種類
	//各支援クラス
	Star star = new Star(50);//背景の星	
	Stage stage = new Stage();//ステージデータ読み込み用
	ScoreData sd = new ScoreData();//スコア送信用	
	Sound sound = new Sound();//効果音

	//動作状況判定、画面表示状況
	boolean DRAG, SETAIM, AIM, TITLE, SCORE;
	//ゲーム進行状況（一時停止、ゲームオーバー、クリア）
	static boolean POSE, GAMEOVER, CLEAR, COMPLETE;
	
	//画面表示用、画像管理
	Image rogo;//タイトルロゴ
	ClickImage hajime, tuduki, explain, next, pre;//タイトルメニュー
	int slide, explainPage;//説明スライド表示用、操作説明用
	Button close, regist, send, nextstage, back;//ウィンドウを閉じる、データ転送
	TextField name, com, pass;//名前入力、コメント入力、パスワード入出力
	MediaTracker met;
	int mid = 0;//メディアトラッカーのID
	
	//ターン数、獲得点数、スコア基準、処理遅延時間
	int turn, score, score500, extraTime;
	
	//スレッドとダブルバッファ処理
	Thread th;
	Image main;
	Graphics mg;
	public void init(){//----------------------------------------------init
		//背景色の設定
		setBackground(BackColor);
		//各リスナーの追加
		addKeyListener(new MyKey());
		addMouseListener(new MyMouse());
		addMouseMotionListener(new MyMouseMotion());
		//ダブルバッファ処理
		main = createImage(MainScreen, ScreenHeight);
		mg = main.getGraphics();
		
		//効果音読み込み
		Sound.setAudio();
		//画像読み込み処理　各クラス初期化
		met = new MediaTracker(this);
		mid = 0;
		//タイトル関連画像
		rogo = getImage(getCodeBase(), Idir + "title.gif");//タイトルロゴ
		met.addImage(rogo, mid++);
		tuduki = new ClickImage(this, Idir, "tuduki.gif", "tuduki_s.gif");//つづき
		hajime = new ClickImage(this, Idir, "hajime.gif", "hajime_s.gif");//はじめ
		explain = new ClickImage(this, Idir, "explain.gif", "explain_s.gif");//操作説明
		next = new ClickImage(this, Idir, "next.gif");//→表示
		pre = new ClickImage(this, Idir, "pre.gif");//→表示
		//各キャラ画像
		sniper = new Sniper(getImage(getCodeBase(), Idir + "cata.gif"));
		met.addImage(sniper.image, mid++);
		for(int i=0; i < tama.length; i++){//各種弾読み込み
			tama[i] = new Tama(getImage(getCodeBase(), Idir + tamaFile[i]));
			met.addImage(tama[i].image, mid++);
			tama[i].restLocate(5+i*50, 480);//下部の残り弾表示用
		}
		for(int i=0; i < enemy.length; i++){//敵
			enemy[i] = new Enemy(getImage(getCodeBase(), Idir + "enemy0"+(i+1)+".gif"));
			met.addImage(enemy[i].image, mid++);
		}
		item = new Item(this);//アイテム初期化
		Sound.setImage(this);//音アイコン画像読み込み
		star.setImage(this);//星アイコン画像読み込み
		CharaSet.setImage(this);//数字画像、爆発画像読み込み
		
		try{//一枚ずつ画像が読み込まれるのを待つ
			mg.drawString("画像読み込み中", 200, 200);
			if(mid >0){
				for(int i=0; i<mid; i++){
					met.waitForID(i);
					mg.fillRect(100, 200, 202, 22);
					mg.drawRect(101, 201, i*200/(mid), 20);
				}
			}
			Graphics g = getGraphics();
			g.drawImage(main, 0, 0, this);
			g.dispose();
		}catch(InterruptedException e){
			showStatus(" " + e);
		}
		if(met.checkAll()){//各画像の大きさ取得
			hajime.getImageSize(this);//「はじめから」の画像
			tuduki.getImageSize(this);//「つづきから」の画像
			explain.getImageSize(this);//「操作説明」の画像
			next.getImageSize(this);//「→」の画像
			pre.getImageSize(this);//「←」の画像
			sniper.getImageSize(this);//スナイパー
			for(int i=0; i < enemy.length; i++)//敵
				enemy[i].getImageSize(this);
			for(int i=0; i<tama.length; i++)//弾
				tama[i].getImageSize(this);
			item.getImageSize(this);//アイテム
			Sound.getImageSize(this);//サウンドアイコン
			star.getImageSize(this);//星（表示選択アイコン）の画像
			CharaSet.getImageSize(this);//数字と爆発の画像
		}
		setting();//（初期）設定関数
	}
	void setting(){//---------------------------ゲーム中変更される(初期)設定
		GAMEOVER = false; CLEAR = false; POSE = false;
		DRAG = false; SETAIM = false; AIM = false;
		TITLE = true; SCORE = false;
		turn = -50;
		score500 = 500;
		extraTime = 100;
		explainPage = 0;
		sniper.setting(3);//
		Tama.Shoten = 0;//打ち出された弾の数
		Tama.Hits = 0;//敵に当たった弾の数
		tama[0].setting(20,  5, 1, 150);//青
		tama[1].setting( 5,  3, 3, 150);//緑、やや貫通
		tama[2].setting( 3,  1, 2, 600);//赤、誘導弾
		tama[3].setting( 2, 10, 1, 500, 1);//銀、特殊（シールド分裂）
		tama[4].setting( 3, 20, 1, 700, 2);//金、特殊（敵分裂）
		enemy[0].setting(1, 10, 3, 1, 0, 0);//敵
		enemy[1].setting(1, 20, 0, 2, 0, 0);//敵
		enemy[2].setting(1, 20, 3, 1, 1, 0);//敵
		enemy[3].setting(1, 30, 4, 1, 2, 0);//敵
		enemy[4].setting(1, 10, 5, 1, 2, 1);//敵
		Enemy.Killed = 0;//倒された敵の数
		item.setting();//アイテム初期化
		stage.setting();//ステージデータ初期化
		close = new Button("登録しない");//ボタン初期設定
		close.addActionListener(new MyAction());
		close.setBounds(210, 300, 80, 20);
	}
	public void start(){//-------------ゲーム中変わることのない初期設定など
		th = new Thread(this);
		th.start();
		//画像位置設定
		Sound.locateIcon(370, 0);//音アイコンの位置決め
		star.locateIcon(340, 0);//星アイコンの位置決め
		hajime.locate(200-hajime.w/2, 250);//「はじめから」の位置決め
		tuduki.locate(200-tuduki.w/2, 300);//「つづきから」の位置決め
		explain.locate(200-explain.w/2, 350);//「操作説明」の位置決め
		next.locate(380, 445);//「→」の位置決め
		pre.locate(0, 445);//「←」の位置決め
		score = 0;//獲得点数
		
		//ボタン設定
		setLayout(null);//レイアウト初期化（これでボタンの位置を自由に設定）
		regist = new Button("登録する");//ボタン初期設定
		regist.addActionListener(new MyAction());
		regist.setBounds(110, 300, 80, 20);
		send = new Button("送信する");//ボタン初期設定
		send.addActionListener(new MyAction());
		send.setBounds(110, 300, 80, 20);
		nextstage = new Button("続ける");
		nextstage.addActionListener(new MyAction());
		nextstage.setBounds(110, 300, 80, 20);
		back = new Button("終了する");//ボタン初期設定
		back.addActionListener(new MyAction());
		back.setBounds(210, 300, 80, 20);

		//テキストフィールド設定
		name = new TextField();//名前入力
		name.addTextListener(new MyAction());
		name.setBounds(170,140,100,18);
		com = new TextField();//コメント入力
		com.setBounds(80,210,240,18);
		pass = new TextField();//パスワード入出力
		pass.addActionListener(new MyAction());
		pass.setBounds(100, 275, 200, 18);
	}
	public void run(){//------------------------------------------------run
		for(;;){
			try{
				Thread.sleep(TIME);//遅延時間
			}catch(InterruptedException e){
			}
			if(TITLE || SCORE || POSE || CLEAR){
				//何もしない
			}else{
				if (stage.ENTRY)
					stage.event(this, turn);//敵、アイテムが出現
				else{
					boolean NOTFLY = true;
					for(int i=0; i < enemy.length; i++)
						if(enemy[i].isFLY()){
							NOTFLY = false;
							break;
						}
					if(NOTFLY){
						if(stage.next())
							COMPLETE = true;
						else
							CLEAR = true;
					}
				}
				if(turn%90==0)//90ターン毎で、弾（青）が増える
					tama[0].addRest();
				if(turn%160==0)//160ターン毎で、弾（緑）が増える
					tama[1].addRest();
				if(score >= score500){//500スコア毎で、弾（赤）が増える
					tama[2].addRest();
					score500 += 500;
				}
			turn++;
			}
			repaint();
		}
	}
	public void titlePaint(){//---------------------------------タイトル画面
			mg.drawImage(rogo, 36, 100, this);//タイトルロゴ
			hajime.paint(this);//はじめから
			tuduki.paint(this);//つづGraphics gきから
			explain.paint(this);//操作説明
			mg.setColor(new Color(0xEEEEEE));
			if(slide > 20)//説明文をスライド表示するため
				slide -= 30;
			if(explainPage > 0){//操作説明状態
				switch(explainPage){//操作説明のページ数で表示するものを決める
				case 1: sniper.paintExplain(this, slide); break;
				case 2:
					mg.drawString("ゲーム中は左下にそれぞれの弾の残り数が表示されます。"
						, slide+40, 440);
					mg.drawString("Z、X、C、V、Bのキーがそれぞれの弾に対応しています。"
						, slide+40, 456);
					mg.drawString("青は通常弾。緑は貫通弾。赤は誘導弾。銀と金は分裂弾です。"
						, slide+30, 474);
					if(slide <=20){//スライド後残り弾数表示
						for (int i=0; i<tama.length; i++)
							tama[i].paintExplain(this);
					}
					break;
				case 3:
					mg.drawString("ドラッグした長さで弾のスピードが決まります。"
						, slide+60, 440);
					mg.drawString("弾は両サイドにある壁で跳ね返ります。"
						, slide+80, 457);
					mg.drawString("誘導弾は撃った後、任意の箇所をドラッグすることで誘導できます。"
						, slide+10, 474);
					mg.drawString("銀弾は両サイドの壁に、金弾は敵に当てると分裂します。"
						, slide+40, 491);
					if(slide <=20){//スライド後壁表示
						mg.setColor(new Color(0xAFEEEE));
						mg.fillRect(0, 0, 2, 500);//左の壁
						mg.fillRect(397, 0, 2, 500);//右の壁
						mg.setColor(new Color(0x00CED1));
						mg.drawLine(2, 0, 2, 500);//左の壁
						mg.drawLine(399, 0, 399, 500);//右の壁
					}
					break;
				case 4:
					mg.drawString("敵に下側の紫のエリアまで進入されるとダメージを受けます。"
						, slide+20, 440);
					mg.drawString("ゲーム中右下に表示されるのがHPゲージです。", slide+80, 460);
					mg.drawString("ゲージが無くなるとゲームオーバーです。", slide+50, 480);
					if(slide <=20)//スライド後HPゲージ表示
						sniper.paintExplain(this);
					break;
				case 5:
					mg.drawString("右上の星のアイコンは、クリックで背景に表示されている"
						, slide+40, 440);
					mg.drawString("星のアニメーションの表示／非表示を切り替えられます。"
						, slide+40, 460);
					mg.drawString("スピーカのアイコンは、効果音の有無を切り替えられます。"
						, slide+40, 480);
					break;
				case 6:
					mg.drawString("あらかたそんなところです。", slide+120, 440);
					mg.drawString("さて、実際にやってみてください。", slide+100, 460);
					mg.drawString("幸運をお祈りします。", slide+140, 480);
					break;
				default: explainPage = 0; break;
				}
				if(slide <= 20){//スライド後ページ移動用ボタン表示
					next.paint(this);
					pre.paint(this);
				}
			}else if(hajime.FOCUS){//「はじめから」にマウスポインタをのせたとき
				mg.drawString("ゲームを stage 1 からはじめます。", slide+100, 440);
			}else if(tuduki.FOCUS){//「つづきから」にマウスポインタをのせたとき
				mg.drawString("各ステージのパスワードを入力してはじめます。", slide+80, 440);
				mg.drawString("スコアは０、弾の数は初期状態から始まります。", slide+80, 460);
			}else if(explain.FOCUS){//「操作説明」にマウスポインタをのせたとき
				mg.drawString("クリックするとここ説明が表示されます。", slide+100, 440);
				mg.drawString("説明と同時に、" +
						"左右に点滅する三角のボタンが表示されます。", slide+30, 460);
				mg.drawString("それをクリックするとこで、" +
						"説明の切り替えが出来ます。", slide+40, 480);
			}else
				slide = 400;
	}
	public void paint(Graphics g){//--------------------------------------paint
		mg.setColor(BackColor);//背景色塗りつぶし
		BackColor = Color.black;//背景色再設定
		mg.fillRect(0, 0, MainScreen, ScreenHeight);//画面塗りつぶし
		star.paint(this);//背景の星
			mg.setColor(new Color(0x990066));//紫
			mg.fillRect(3, 430, 394, 70);//自機
		if(TITLE){
			titlePaint();
			if (SCORE)
				sd.passPaint(this);
		}else if(SCORE){
			sd.paint(this);
		}else{
			mg.setColor(new Color(0xAFEEEE));
			mg.fillRect(0, 0, 2, 500);//左の壁
			mg.fillRect(397, 0, 2, 500);//右の壁
			mg.setColor(new Color(0x00CED1));
			mg.drawLine(2, 0, 2, 500);//左の壁
			mg.drawLine(399, 0, 399, 500);//右の壁
			if(POSE){//ポーズ
				mg.setColor(new Color(0xeeeeee));
				mg.drawString("- pose -", 180, 240);
			}else{
				sniper.paint(this);//パチンコ描画
				for(int i = 0; i < enemy.length; i++)//敵描画
					enemy[i].paint(this, tama);
				for(int i = 0; i < tama.length; i++)//弾描画
					tama[i].paint(this);
				item.paint(this, tama);//アイテム
			}
			mg.setColor(new Color(0xeeeeee));
			mg.drawString(Integer.toString(turn),20,10);//ターン数描画
			if(turn < 0)//初めにステージ数を表示
				mg.drawString("- " + stage.name + " -", 170, 250);
			if(GAMEOVER){
				mg.setColor(new Color(0xeeeeee));
				mg.drawString("- GAME OVER -", 160, 250);
				extraTime--;//クリアより短くする
			}else if(CLEAR){
				mg.setColor(new Color(0xFFD700));
				mg.setFont(new Font("Serif", Font.PLAIN, 30));
				if(extraTime % 4 < 2)
					mg.drawString("☆★☆ CLEAR ☆★☆", 50, 240);
				else
					mg.drawString("★☆★ CLEAR ★☆★", 50, 240);
				mg.setFont(new Font("Dialog", Font.PLAIN, 12));
				mg.drawString("おめでとうございます！！", 130, 270);
				mg.drawString("次のステージ追加をご期待ください", 100, 290);
			}
			if(GAMEOVER || CLEAR){
				extraTime--;//しばらく現状表示
				if(extraTime < 0){//一定時間後スコア表示画面へ
					Sound.play(Sound.open);
					SCORE = true;
					sd.getScore(score, tama);
					add(close);
					add(regist);
				}
			}else if (COMPLETE){
				extraTime -= 2;
				mg.drawString("- " + stage.name + " complete ! -", 150, 250);
				if (extraTime < 0){
					SCORE = true;
					COMPLETE = false;
					sd.getScore(score, tama);
					sd.PASS = true;
					add(nextstage);
					add(back);
					add(pass);
				}
			}
		}
		//mg.setColor(new Color(0x999999));
		//mg.drawString("by  tanaka tomoyuki", 100,15);//名前
		mg.setColor(new Color(0xeeeeee));
		mg.drawString("score:"+Integer.toString(score), 250,15);//スコア
		star.paintIcon(this);//星アイコン
		Sound.paintIcon(this);//サウンドアイコン
		g.drawImage(main, 0, 0, this);//アプレットに描画
	}

	public void update(Graphics g){
		paint(g);
	}
	public void stop(){
		th = null;
	}
	class MyMouse extends MouseAdapter{//--------------------------MouseAdapter
		public void mouseClicked(MouseEvent e){//それぞれをクリックしたとき
			requestFocus();//キー処理できるように
			if(TITLE){
				if(hajime.isOn(e.getX(),e.getY())){//はじめから
					Sound.play(Sound.itemGet);
					score = 0;//獲得点数
					TITLE = false;
				}else if (tuduki.isOn(e.getX(), e.getY())){//つづきから
					Sound.play(Sound.itemChange);
					SCORE = true;
					sd.PASS = true;
					add(nextstage);
					add(back);
					add(pass);
					pass.requestFocus();
				}else if (explain.isOn(e.getX(),e.getY())){//操作説明
					Sound.play(Sound.itemChange);
					slide = 400;
					explainPage = 1;
				}else if(explainPage > 0 && slide <= 20){//操作説明表示中かつスライド後
					if(next.isOn(e.getX(),e.getY())){//→（次のページ）
						slide = 400;
						next.FOCUS = false;
						explainPage++;
					}else if(pre.isOn(e.getX(),e.getY())){//←（前のページ）
						slide = 400;
						pre.FOCUS = false;
						explainPage--;
					}
				}
			}
			Sound.clickIcon(e.getX(), e.getY());//効果音アイコン
			star.clickIcon(e.getX(), e.getY());//背景の星アイコン
		}
		public void mousePressed(MouseEvent e){
			if(TITLE || SCORE || POSE){
				//何もしない
			}else{
				if(tama[tid].isOn(e.getX(), e.getY()))//セットされた弾の上か
					AIM = true;//ドラッグ開始処理
				else if(tid ==2)
					tama[2].setLead(e.getX(), e.getY());//誘導弾処理
			}
		}
		public void mouseReleased(MouseEvent e){
			if(DRAG){//ドラッグ終了処理
				tama[tid].shot();//弾発射処理
				sniper.shot();//スナイパー発射処理
			}else tama[tid].notAim();//リセット
			DRAG = false;
			AIM = false;
		}
	}
	
	class MyMouseMotion extends MouseMotionAdapter{//--------MouseMotionAdapter
		public void mouseMoved(MouseEvent e){//マウスポインタがどこにあるか
			if(TITLE){//タイトル画面表示時
				hajime.isOn(e.getX(), e.getY());//「はじめから」の上
				tuduki.isOn(e.getX(), e.getY());//「つづきから」の上
				explain.isOn(e.getX(), e.getY());//「操作説明」の上
			}else if(SCORE || POSE){
				//何もしない
			}else{
				sniper.locate(e.getX()-10, 440);//マウスと連動して動く
				if(SETAIM){//弾をセットした状態
					tama[tid].takeAim(sniper.muzzleX(), sniper.muzzleY());
					sniper.setTama(tama[tid].h);
				}
			}
		}
		public void mouseDragged(MouseEvent e){//ドラッグの処理
			int mx = e.getX();
			int my = e.getY();
			if(AIM){
				if(my > 430 && my < 510){//下部限定
					SETAIM = false;
					DRAG = true;
					//ドラッグで引っ張られ、弾の位置、初速度が変わる
					tama[tid].takeAim(mx, my);
					tama[tid].setVelocity(sniper.muzzleX(), sniper.muzzleY());
					sniper.takeAim(mx, my);
				}else DRAG = false;
			}else if(tid == 2){//誘導弾の速度変更
				tama[tid].leading(mx, my);
			}
		}
	}
	class MyKey extends KeyAdapter{//------------------------------KeyAdapter
		public void keyPressed(KeyEvent e){
			if(TITLE || SCORE){
				//何もしない
			}else{
				int code = e.getKeyCode();
				if(code == KeyEvent.VK_SPACE){
					if(POSE) POSE = false;
					else     POSE = true;
				}
				if(!AIM && !POSE){
					boolean SET = false;
					SETAIM = false;
					tama[tid].notAim();//リセット
					switch (code){//各キーの処理
						case KeyEvent.VK_Z : tid = 0; SET = true; break;
						case KeyEvent.VK_X : tid = 1; SET = true; break;
						case KeyEvent.VK_C : tid = 2; SET = true; break;
						case KeyEvent.VK_V : tid = 3; SET = true; break;
						case KeyEvent.VK_B : tid = 4; SET = true; break;
						case KeyEvent.VK_P : Sniper.damage(); break;
						//case KeyEvent.VK_Q : CLEAR = true; break;
						default : break;
					}
					if(SET && tama[tid].canAim())//弾をセットしていいか
						SETAIM = true;
					if(SETAIM){
						Sound.play(Sound.set);
						tama[tid].takeAim(sniper.muzzleX(), sniper.muzzleY());
						sniper.setTama(tama[tid].h);
					}
				}
			}
		}
	}
	class MyAction implements ActionListener, TextListener{
		public void actionPerformed(ActionEvent e){
			if (e.getSource() == close){//タイトル画面へ
				//ボタンとテキストフィールドを取り除く
				remove(close); remove(regist); remove(send);
				remove(name);  remove(com);
				sd.REGIST = false;
				setting();//初期設定
			}else if (e.getSource() == regist){//登録画面へ
				remove(regist);
				add(send);
				add(name); add(com);//テキストフィールド追加
				sd.REGIST = true;//登録画面へ
				System.out.print(name.getText());
				if ((name.getText()).length() == 0)
					send.setEnabled(false);//送信ボタン利用不可
			}else if (e.getSource() == send){//データ送信
				//ボタンとテキストフィールドを取り除く
				remove(send);
				remove(name); remove(com);
				sd.REGIST = false;
				sd.makeData(name.getText(), com.getText());
				sd.sendScore();//データ送信
				close.setLabel("タイトル画面へ戻る");
				close.setBounds(120, 300, 160, 20);
			}else if (e.getSource() == nextstage || e.getSource() == pass){//続きへ
				remove(nextstage);
				remove(back);
				remove(pass);
				extraTime = 100;
				turn = -50;
				sd.PASS = false;
				SCORE = false;
				if (TITLE && stage.passCheck(pass.getText())){//パスの示すステージから始まる。
					Sound.play(Sound.itemGet);
					score = 0;//スコアリセット
					TITLE = false;
				}
				stage.setData();
				pass.setText("");
				requestFocus();//キー処理できるように
			}else if (e.getSource() == back){//戻る
				sd.PASS = false;
				remove(nextstage);
				remove(back);
				remove(pass);
				pass.setText("");
				setting();
			}
			
		}
		public void textValueChanged(TextEvent e){//テキスト入力に対応する処理
			if ((name.getText()).length() == 0)
				send.setEnabled(false);
			else
				send.setEnabled(true);			
		}
	}

}

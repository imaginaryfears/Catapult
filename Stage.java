import java.io.*;
import java.net.URL;

public class Stage{
	String dir = "data/";//ディレクトリ名とファイル名
	String[] fileName = {"stage1.d", "stage2.d", "stage3.d"};
	String[] stagePass = {"hihen", "kihen", "sannzui"};
	int fid;
	String name;//ステージ名
	int[] turn, type;//ターン数と敵タイプ
	boolean ENTRY, NEXT;
	int num, id;
	
	void setting(){
		fid = 0;
		ENTRY = false;
		setData();
	}
	boolean passCheck(String s){
		for (int i = 0; i < stagePass.length; i++)
			if (s.equalsIgnoreCase(stagePass[i])){
				fid = i;
				return true;
			}
		return false;
	}
	boolean next(){
		return NEXT;
	}
	void event(Catapult c, int n){
		if (ENTRY && turn[id] == n){
			if (type[id] == -1)
				c.item.readyGo();
			else if (type[id] == 999){//敵終了
				fid++;
				ENTRY = false;
				if (fid < fileName.length){//次のステージあり
					c.pass.setText(stagePass[fid]);
					NEXT = true;
				}else{//次のステージなし
					NEXT = false;
					fid = 0;
				}
			}else if (0 <= type[id] && type[id] < c.enemy.length)
				c.enemy[type[id]].readyGo();
			id++;
			if (id >= num)
				ENTRY = false;
		}else if (ENTRY && turn[id] < n){
			System.out.print("StageDataError!");
			id++;
		}
	}
	void setData(){
		id = 0;
		num = 0;
		BufferedReader br = null;
	 	try {
			//File file = new File((Catapult.class.getResource(dir+fileName[fid])).getFile());
			URL data = (Catapult.class.getResource(dir + fileName[fid]));
			InputStream ins = data.openStream();
			br = new BufferedReader(new InputStreamReader(ins));

			String line;
 			if ((line = br.readLine()) != null){//１行目（名前）
				name = line;
				//System.out.println(name);
			}
			if ((line = br.readLine()) != null){//２行目（データ数）
				num = Integer.parseInt(line);
				turn = new int [num];
				type = new int [num];
				if (num > 0)
					ENTRY = true;
				//System.out.println(num);
			}
			if (ENTRY){
				for (int i = 0; i < num; i++){//３行目以降（データ）
					if ((line = br.readLine()) == null)
						return;
					int p = line.indexOf(",");
					turn[i] = Integer.parseInt(line.substring(0, p));
					type[i] = Integer.parseInt(line.substring(p+1));
					//System.out.println(turn[i] + "[+]" + type[i]);
				}
			}
		} catch (IOException e) {
		   e.printStackTrace(); 
		}finally{
			try{
				if(br !=null)
					br.close();
			}catch(IOException e){
			     e.printStackTrace();
			}
		}
	}
}

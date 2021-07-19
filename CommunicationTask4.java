package ieexp3.id190441148;

import java.net.*;
import java.io.*;
import javafx.concurrent.Task;

public class CommunicationTask4 extends Task<Void> {
	public static String cmd = "nothing";
	public String msg;


	private String server;
	private int port;

	private String[] holeNo = new String[3];

	public CommunicationTask4(String server,int port,String[] holeNo) {
		this.server = server;
		this.port = port;
		this.holeNo = holeNo;
	}

	/* バックグラウンド・スレッドで呼び出されるメソッド*/
	@Override
	protected Void call() throws Exception{
		Socket socket = null;
			try {
				// ソケットを作成
				socket = new Socket();
				// 指定されたホスト名（IPアドレス）とポート番号でサーバに接続する
				socket.connect(new InetSocketAddress(this.server, this.port));

				// 接続されたソケットの入力ストリームを取得し，データ入力ストリームを連結
				InputStream is = socket.getInputStream();
				DataInputStream dis = new DataInputStream(is);

				//　接続されたソケットの出力ストリームを取得し，データ出力ストリームを連結
				OutputStream os =  socket.getOutputStream();
				DataOutputStream dos = new DataOutputStream(os);

				while(true){
					Thread.sleep(1000);
					//Connect処理
					if(cmd.equals("connect")) {
						dos.writeUTF("connect");
						//ソケットを通じてデータ送信
						dos.flush();
						cmd = "nothing";
					}

					//DisConnect処理
					if(cmd.equals("disConnect")) {
						dos.writeUTF("disConnect");
						//ソケットを通じてデータ送信
						dos.flush();
						cmd = "nothing";
					}

					//RUN処理
					if(cmd.equals("run")) {
						String message = "run," + holeNo[0] + "," +holeNo[1] + "," + holeNo[2];
						dos.writeUTF(message);
						//ソケットを通じてデータ送信
						dos.flush();
						cmd = "nothing";
					}

				}

			}catch(Exception e) {
				e.printStackTrace();
			}finally {

				// ソケットをクローズする
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						System.out.println(e);
					}
				}
			}
		return null;
	}
}

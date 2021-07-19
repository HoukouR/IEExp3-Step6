package ieexp3.id190441148;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import ieexp3.library.CaoAPI;

public class BasicTask4Server {


	/** サーバソケット */
	private static ServerSocket serverSocket;
	/** ソケット */
	private static Socket socket;
	/** データ入力用ストリーム */
	private static DataInputStream dis;
	/** データ出力用ストリーム */
	private static DataOutputStream dos;

	/**
	 * メインメソッド
	 *
	 * @param args
	 *            [0]:リッスンポート番号
	 */
	public static void main(String[] args) {
		// コマンドライン引数の数が一致しない場合は使い方を表示して終了
		if (args.length != 1) {
			System.out.println("invalid argument :(");
			System.out.println("usage: TcpServer port");
			System.exit(1);
		}

		// コマンドライン引数からポート番号を取得
		int port = Integer.parseInt(args[0]);

		// Ctrl+Cでプログラム終了時に呼び出す処理を設定
		setShutdownHook();

		try {
			// サーバソケットの生成
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);

			// 指定ポート番号にバインド
			serverSocket.bind(new InetSocketAddress(port));

			// クライアントからの接続要求待ちループ
			while (true) {
				// クライアントからの接続要求を待機（ブロッキング）
				System.out.println("\nlistening on port " + port + "...");
				socket = serverSocket.accept();

				// データ入出力ストリームを連結
				dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

				// コネクションが確立したらクライアントのIPアドレスとポート番号を表示
				System.out.println("establised a connection with "+ socket.getInetAddress().toString() + ":"+ socket.getPort() + "!");

				// クライアントからのメッセージ待ちループ
				while (true) {
					boolean exception = false;
					try {
						// クライアントからのメッセージを待機（ブロッキング）
						String message = dis.readUTF();
						// クライアントからのメッセージを分割
						String[] splitMessage = message.split(",",0);

						if(splitMessage[0].equals("connect")) {
							try {
								// CAOエンジンの初期化
								CaoAPI.init("TestWorkspace");
								System.out.println("CAO engine is initialized.");

								// コントローラに接続
								CaoAPI.connect("RC8", "VE026A");
								System.out.println("Controller and Robot are connected.");

								// 自動モードに設定
								CaoAPI.setControllerMode(CaoAPI.CONTROLLER_MODE_AUTO);
								System.out.println("Operation mode is set to Auto mode.");

								// モータを起動
								CaoAPI.turnOnMotor();
								System.out.println("Motor is turned on.");

								// ロボットの外部速度/加速度/減速度を設定
								float speed = 50.0f, accel = 25.0f, decel = 25.0f;
								CaoAPI.setExtSpeed(speed, accel, decel);
								System.out.println("External speed/acceleration/deceleration is set to "+ speed + "/" + accel + "/" + decel + ".");

								splitMessage[0]="null";


							} catch (Exception e) {
								e.printStackTrace();
								dos.writeUTF("State: error.");
								dos.flush();
							}
						}

						if(splitMessage[0].equals("run")) {
							try {
								// TakeArm Keep = 0
								CaoAPI.takeArm(0L, 0L);

								// Speed 100
								CaoAPI.speed(-1L, 100.0f);

								// Move P, @0 P1
								CaoAPI.move(1L, "@0 P0", "");

								CaoAPI.approach(1L, "P"+splitMessage[1], "@0 60", "");
								CaoAPI.driveAEx("(7, 5)", "");
								CaoAPI.move(2L, "@0 P"+splitMessage[1], "S = 50");
								CaoAPI.driveAEx("(7, -45)", "");
								CaoAPI.depart(2L, "@P 60", "");

								CaoAPI.approach(1L, "P"+splitMessage[2], "@0 60", "");
								CaoAPI.move(2L, "@0 P"+splitMessage[2], "S = 50");
								CaoAPI.driveAEx("(7, 5)", "");
								CaoAPI.driveAEx("(7, -45)", "");
								CaoAPI.depart(2L, "@P 60", "");

								// Move P, @0 P1
								CaoAPI.move(1L, "@0 P0", "");
								// GiveArm
								CaoAPI.giveArm();
							}catch(Exception e) {
								e.printStackTrace();
							}
						}


						if(splitMessage[0].equals("disConnect")) {
							try {
								// モータを停止
								CaoAPI.turnOffMotor();
								System.out.println("Moter is turned off.");

								// コントローラから切断
								CaoAPI.disconnect();
								System.out.println("Controller and Robot is disconnected.");

								dos.writeUTF("State: disconnected.");
								dos.flush();

							} catch (Exception e) {
								e.printStackTrace();
								dos.writeUTF("State: error.");
								dos.flush();
							}
							break;
						}

					} catch (EOFException e) {
						// クライアントがソケットを閉じたときに送信するFINを受信した場合の例外処理
						System.out.println("client disconnected.");
						exception = true;
					} catch (IOException e) {
						e.printStackTrace();
						exception = true;
					} finally {
						// 例外が発生した場合はソケットを閉じてメッセージ待ちループを脱出
						if (exception) {
							close();
							break;
						}
					}
				}
			}

		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			/* setShutdownHook()メソッドでサーバソケットが閉じられるため，ここでは何も処理しない */
		}
	}

	private static void close() {
		// ソケットを閉じる
		if (socket != null && !socket.isClosed())
			try {
				socket.close();
				System.out.println("socket is closed.");
			} catch (IOException e) {
				e.printStackTrace();
			}

		// データ入力ストリームを閉じる
		if (dis != null)
			try {
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		// データ出力ストリームを閉じる
		if (dos != null)
			try {
				dos.flush();	// 出力ストリームはcloseする前にflushした方がよい
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * プログラムが終了する場合に呼び出させるメソッド
	 */
	private static void setShutdownHook() {
		// シャットダウンフックを登録
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println();
				// ソケットを閉じる
				close();
				// サーバソケットを閉じる
				if (serverSocket != null && !serverSocket.isClosed())
					try {
						serverSocket.close();
						System.out.println("server socket is closed.");
					} catch (IOException e) {
						e.printStackTrace();
					}

				System.out.println("bye");
				System.out.flush();
			}
		});
	}

}

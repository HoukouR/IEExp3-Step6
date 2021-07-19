package ieexp3.id190441148.ieexp3_step6;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Switch;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CommunicationTask6 extends AsyncTask<Void, String, Void> {
    /** サーバのIPアドレス */
    private String address;
    /** サーバのポート番号 */
    private int port;
    /** ソケット */
    private Socket socket;
    /** データ入力用ストリーム */
    private DataInputStream dis;
    /** データ出力用ストリーム */
    private DataOutputStream dos;
    /** ソケットの受信ループフラグ */
    private boolean isLoop;


    private CommunicationTask6Callback callback;

    public CommunicationTask6(String address, int port, CommunicationTask6Callback callback) {
        this.address = address;
        this.port = port;
        this.callback = callback;
        this.isLoop = true;
    }

    /**
     * バックグラウンド処理
     * （アクティビティのUI操作は不可能）
     * @param params    未使用（Void型の配列）
     * @return  null
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            // サーバへ接続（3秒でタイムアウト）
            socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 3000);


            try {
                // コネクションが確立したらソケットの入出力ストリームにバッファ付ストリームと
                // データ入出力ストリームを連結
                dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                sendMessage("connect");

                // サーバからの応答受信ループ（ノンブロッキング）
                while (isLoop) {
                    // データ入力ストリームに読み込み可能なデータがあるか確認
                    if (dis.available() > 0) {
                        //今回はサーバからデータを受信しないため何もしない
                    } else {
                        // 100ms待機（過負荷対策）
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            Log.e("IEExp3-Step5", "error", ie);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("IEExp3-Step5", "error", e);
                publishProgress("[ERROR] " + e.getMessage());
            } finally {
                // ソケットを閉じる
                close();
            }
        } catch (Exception e) {
            Log.e("IEExp3-Step5", "error", e);
            publishProgress("[ERROR] " + e.getMessage());
        }

        return null;
    }

    /**
     * バックグラウンド処理を行う前の事前処理
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (callback != null)
            callback.onPreExecute();
    }

    /**
     * doInBackground()の処理が終了したときに呼び出されるメソッド
     * @param aVoid doInBackground()の戻り値
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (callback != null)
            callback.onPostExecute(aVoid);
    }

    /**
     * doInBackground()内でpublishProgress()が呼ばれたときに呼び出されるメソッド
     * @param values   Logに出力するメッセージ
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (callback != null)
            callback.onProgressUpdate(values);
    }

    /**
     * メッセージをサーバへ送信するメソッド
     * @param message   メッセージ
     */
    public void sendMessage(String message) {
        if (message != null) {
            try {
                // メッセージをサーバへ送信
                dos.writeUTF(message);
                dos.flush();

                publishProgress("send the message '" + message + "' to the server.");
                String[] splitMessage = message.split(",",0);
                if(splitMessage[0].equals("run")){
                    publishProgress("The ball is in the holeNo." + splitMessage[2]);
                }
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }
        }
    }

    /**
     * 受信処理ループを停止するメソッド
     */
    public void stop() {
        this.isLoop = false;
    }

    /**
     * ソケット及びストリームを閉じるメソッド
     */
    private void close() {
        // ソケットを閉じる
        if (socket != null && socket.isConnected())
            try {
                socket.close();
                publishProgress("socket is closed.");
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }

        // データ入力ストリームを閉じる
        if (dis != null)
            try {
                dis.close();
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }

        // データ出力ストリームを閉じる
        if (dos != null)
            try {
                dos.flush();
                dos.close();
            } catch (IOException e) {
                Log.e("Sample5", "error", e);
            }
    }

}

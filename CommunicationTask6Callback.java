package ieexp3.id190441148.ieexp3_step6;

/**
 * TCPクライアントコールバックインタフェース
 *
 * @author Riku Hinata
 */
public interface CommunicationTask6Callback {
    /**
     * doInBackground()の処理を開始する前に呼び出されるコールバックメソッド
     */
    void onPreExecute();

    /**
     * doInBackground()内でpublishProgress()が呼ばれたときに呼び出されるコールバックメソッド
     * @param values   Logに出力するメッセージ
     */
    void onProgressUpdate(String... values);

    /**
     * doInBackground()の処理が終了したときに呼び出されるコールバックメソッド
     * @param aVoid doInBackground()の戻り値
     */
    void onPostExecute(Void aVoid);
}

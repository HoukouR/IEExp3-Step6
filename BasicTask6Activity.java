package ieexp3.id190441148.ieexp3_step6;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BasicTask6Activity extends AppCompatActivity implements CommunicationTask6Callback, View.OnTouchListener {
    /** IP addressを入力するエディットテキスト */
    protected EditText editAddress;
    /** Port numberを入力するエディットテキスト */
    protected EditText editPort;
    /** ボールの初期位置を入力するエディットテキスト */
    protected EditText indexPosition;
    /** 1つ目の穴を表すイメージビュー */
    protected ImageView firstHoleView;
    /** 2つ目の穴を表すイメージビュー */
    protected ImageView secondHoleView;
    /** 3つ目の穴を表すイメージビュー */
    protected ImageView thirdHoleView;
    /** 4つ目の穴を表すイメージビュー */
    protected ImageView fourthHoleView;
    /** 5つ目の穴を表すイメージビュー */
    protected ImageView fifthHoleView;
    /** 6つ目の穴を表すイメージビュー */
    protected ImageView sixthHoleView;
    /** Logを表示するエディットテキスト */
    protected EditText editLog;
    /** サーバに接続するボタン */
    protected Button buttonConnect;
    /** サーバから切断するボタン */
    protected Button buttonDisconnect;
    /**ロボット制御を開始するボタン */
    protected Button buttonRun;
    /**ボールの初期位置を設定するボタン */
    protected Button buttonSet;
    /** Logをクリアするボタン */
    protected Button buttonClear;
    /**デバッグモードをオン、オフするスイッチ*/
    protected Switch debugSwitch;
    /**ボールを表すカスタムイメージビュー*/
    protected CustomImageView ballView;
    /** TCPクライアントタスク */
    private CommunicationTask6 task;
    /**UIスレッドへRunnableを渡すためのHandler*/
    private Handler handler;
    /**以前にタッチした座標(古い座標)*/
    private int preDx,preDy;
    /**各穴の座標*/
    private int[][] holePosition = new int [6][4];
    /**基準となる穴の番号*/
    private String indexHoleNo = "";
    /**移動先の穴の番号*/
    private String destinationHoleNo = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_task6);

        // GUIコンポーネントを取得してインスタンス変数に設定
        editAddress = (EditText)findViewById(R.id.editAddress);
        editPort = (EditText)findViewById(R.id.editPort);
        editLog = (EditText)findViewById(R.id.editLog);
        indexPosition = (EditText)findViewById(R.id.indexPosition);
        buttonConnect = (Button)findViewById(R.id.buttonConnect);
        buttonDisconnect = (Button)findViewById(R.id.buttonDisconnect);
        buttonRun = (Button)findViewById(R.id.buttonRun);
        buttonSet = (Button)findViewById(R.id.buttonSet);
        buttonClear = (Button)findViewById(R.id.buttonClear);
        debugSwitch = (Switch)findViewById(R.id.debugSwitch);
        firstHoleView = (ImageView)findViewById(R.id.firstHoleView);
        secondHoleView = (ImageView)findViewById(R.id.secondHoleView);
        thirdHoleView = (ImageView)findViewById(R.id.thirdHoleView);
        fourthHoleView = (ImageView)findViewById(R.id.fourthHoleView);
        fifthHoleView = (ImageView)findViewById(R.id.fifthHoleView);
        sixthHoleView = (ImageView)findViewById(R.id.sixthHoleView);
        ballView = (CustomImageView) findViewById(R.id.ballView);
        ballView.setOnTouchListener(this);

        // ボタンの有効/無効を設定
        buttonConnect.setEnabled(true);
        buttonDisconnect.setEnabled(false);
        buttonSet.setEnabled(false);
        buttonRun.setEnabled(false);

        //editLogを編集不可に設定
        editLog.setFocusable(false);

        //Switchに関する設定
        debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            //Switchがオン、またはオフにされた場合
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
                if(isChecked == true) onProgressUpdate("\"DebugMode\" is ON");
                else  onProgressUpdate("\"DebugMode\" is OFF");
            }
        });

        //初期位置を指定するまでボールは非表示に設定
        ballView.setVisibility(View.INVISIBLE);

        // StrictModeを設定
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
    }

    /**
     * ボールの画像がタッチされた場合の処理を行うメソッド
     * @param v
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent event){
        //タッチした時のx,yの座標を取得
        int newDx = (int)event.getRawX();
        int newDy = (int)event.getRawY();

        //タッチした時の動作を取得
        int action = event.getAction();

        switch (action) {
            //タッチを押された瞬間の処理
            case (MotionEvent.ACTION_DOWN) :
                //何もしない
                break;

            //タッチされていて、その移動中の処理
            case (MotionEvent.ACTION_MOVE) :
                //ボールの現在位置にボールをタッチして動かした時の移動量を加算
                int dx = ballView.getLeft() + (newDx - preDx);
                int dy = ballView.getTop() + (newDy - preDy);
                int imgW = dx + ballView.getWidth();
                int imgH = dy + ballView.getHeight();

                //ボールの位置を設定する
                ballView.layout(dx, dy, imgW, imgH);
                break;

            //タッチを離した瞬間の処理
            case (MotionEvent.ACTION_UP) :
                //移動先の穴の番号を取得
                destinationHoleNo = setDestination(ballView.getLeft(),ballView.getTop());
                //ボールの位置を設定する
                int tmp = Integer.parseInt(destinationHoleNo);
                ballView.layout(holePosition[tmp-1][0],holePosition[tmp-1][1],holePosition[tmp-1][2],holePosition[tmp-1][3]);
                //デバッグモードがオンの時はLogにメッセージを出力
                if(debugSwitch.isChecked()) onProgressUpdate("The ball will go to the holeNo." + tmp);
                break;
            //それ以外の処理
            default:
                //何もしない
                break;
        }

        //タッチした座標をを古い座標とする
        preDx = newDx;
        preDy = newDy;

        return true;
    }


    /**
     * 移動先の穴を決定するメソッド
     * @param dx 現在のボールのx座標
     * @param dy 現在のボールのy座標
     * @return 移動先の穴の番号
     */
    public String setDestination(int dx, int dy){
        int [] distance = new int[6];
        String holeNo = "1";
        //現在のボールと各穴の距離を計算
        for(int i = 0;i < 6;i++){
            distance[i] = getDistance((double)holePosition[i][0], (double)holePosition[i][1], dx, dy);
            //デバッグモードがオンの時はLogにメッセージを出力
            if(debugSwitch.isChecked()) onProgressUpdate("Distance to holeNo." + (i + 1) + " is " + distance[i]);
        }
        //ボールから一番近い穴を移動先の穴とする
        int minDistance = distance[0];
        for(int i = 1;i < 6;i++){
            if(minDistance > distance[i]){
                minDistance = distance[i];
                holeNo = String.valueOf(i + 1);
            }
        }
        //デバッグモードがオンの時はLogにメッセージを出力
        if(debugSwitch.isChecked()) onProgressUpdate("Closest to the ball is holeNo." + holeNo );
        return holeNo;
    }


    /**
     * ボールと穴の距離を計算するメソッド
     * @param x 穴のx座標
     * @param y 穴のy座標
     * @param x2 ボールのx座標
     * @param y2 ボールのy座標
     * @return 距離
     */
    public int getDistance(double x,double y,double x2,double y2){
        double distance = Math.sqrt(Math.pow(x2 - x, 2)  + Math.pow(y2 - y, 2));
        return (int) distance;
    }

    /**
     * Setボタンをクリックした時に呼び出すイベントハンドラ
     * @param view
     */
    public void handleButtonSet(View view) {
        //各穴の座標を取得
        holePosition[0][0] = firstHoleView.getLeft();
        holePosition[0][1] = firstHoleView.getTop();
        holePosition[0][2] = holePosition[0][0] + firstHoleView.getWidth();
        holePosition[0][3] = holePosition[0][1] + firstHoleView.getHeight();

        holePosition[1][0] = secondHoleView.getLeft();
        holePosition[1][1] = secondHoleView.getTop();
        holePosition[1][2] = holePosition[1][0] + secondHoleView.getWidth();
        holePosition[1][3] = holePosition[1][1] + secondHoleView.getHeight();

        holePosition[2][0] = thirdHoleView.getLeft();
        holePosition[2][1] = thirdHoleView.getTop();
        holePosition[2][2] = holePosition[2][0] + thirdHoleView.getWidth();
        holePosition[2][3] = holePosition[2][1] + thirdHoleView.getHeight();

        holePosition[3][0] = fourthHoleView.getLeft();
        holePosition[3][1] = fourthHoleView.getTop();
        holePosition[3][2] = holePosition[3][0] + fourthHoleView.getWidth();
        holePosition[3][3] = holePosition[3][1] + fourthHoleView.getHeight();

        holePosition[4][0] = fifthHoleView.getLeft();
        holePosition[4][1] = fifthHoleView.getTop();
        holePosition[4][2] = holePosition[4][0] + fifthHoleView.getWidth();
        holePosition[4][3] = holePosition[4][1] + fifthHoleView.getHeight();

        holePosition[5][0] = sixthHoleView.getLeft();
        holePosition[5][1] = sixthHoleView.getTop();
        holePosition[5][2] = holePosition[5][0] + sixthHoleView.getWidth();
        holePosition[5][3] = holePosition[5][1] + sixthHoleView.getHeight();

        //エディットテキストから基準となる穴の番号を取得
        indexHoleNo = indexPosition.getText().toString();
        //何も入力されていない場合は警告ダイアログを表示
        if (indexHoleNo.equals("")) {
            showAlertDialog("基準となる穴の番号を入力してください！");
            return;
        }
        int index = Integer.parseInt(indexHoleNo);
        //入力された数字が範囲外の場合は警告ダイアログを表示
        if(index < 1 || index > 6) {
            showAlertDialog("エディットテキストには1-6の数字を入力してください！");
            return;
        }
        //ボールを指定された穴に設置
        ballView.layout(holePosition[index - 1][0],holePosition[index - 1][1],holePosition[index - 1][2],holePosition[index - 1][3]);
        ballView.setVisibility(View.VISIBLE);
        //デバッグモードがオンの時はLogにメッセージを出力
        if(debugSwitch.isChecked()) onProgressUpdate("The ball is in the holeNo." + index);
        //Runボタンを有効にする
        buttonRun.setEnabled(true);
    }


    /**
     * Connectボタンをクリックした時に呼び出すイベントハンドラ
     * @param view
     */
    public void handleButtonConnect(View view) {
        // サーバのIPアドレスを取得
        String address = editAddress.getText().toString();
        // 入力されていない場合は警告ダイアログを表示
        if (address.equals("")) {
            showAlertDialog("IPアドレスを入力してください！");
            return;
        }

        // 入力されていない場合は警告ダイアログを表示
        if (editPort.getText().toString().equals("")) {
            showAlertDialog("ポート番号を入力してください！");
            return;
        }
        //サーバのポート番号を取得
        int port = Integer.parseInt(editPort.getText().toString());

        // TCPクライアントタスクを生成してバックグラウンドで実行（非同期処理）
        task = new CommunicationTask6(address, port, this);
        task.execute();
    }


    /**
     * Disconnectボタンをクリックした時に呼び出すイベントハンドラ
     * @param view
     */
    public void handleButtonDisconnect(View view) {
        task.sendMessage("disconnect");
        if (task != null)
            task.stop();
    }


    /**
     * Runボタンをクリックした時に呼び出すイベントハンドラ
     * @param view
     */
    public void handleButtonRun(View view) {
        //移動先の穴の番号を指定していない場合は警告ダイアログを表示
        if(destinationHoleNo.equals("")){
            showAlertDialog("ボールの画像を移動させて、移動先の穴を指定してください！");
            return;
        }

        int destination = Integer.parseInt(destinationHoleNo);
        int index = Integer.parseInt(indexHoleNo);
        //移動先の穴と基準となる穴が同じだった場合は警告ダイアログを表示
        if(index == destination){
            showAlertDialog("ボールの画像を移動させて、移動先の穴を指定してください！" +
                    "\n(現在の穴と同じ穴は指定できません。)");
            return;
        }

        //メッセージをサーバへ送信
        task.sendMessage("run," + indexHoleNo + "," + destinationHoleNo);
        //移動先の穴を基準となる穴とする
        indexHoleNo = destinationHoleNo;
        //ボールを基準となる穴に設置
        index = Integer.parseInt(indexHoleNo);
        ballView.layout(holePosition[index - 1][0],holePosition[index - 1][1],holePosition[index - 1][2],holePosition[index - 1][3]);
        //Runボタンを一度無効にし、指定した秒数後にもう一度有効にする
        buttonRun.setEnabled(false);
        waitingProcess();
    }


    /**
     * 指定された秒数後に決められた処理を行うメソッド
     */
    public void waitingProcess(){
        //もしすでにhandlerがあった場合は一度取り消す
        if(handler != null) handler.removeCallbacksAndMessages(null);

        handler = new Handler();

        //指定された秒数後にRunボタンを有効にする
        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                buttonRun.setEnabled(true);
            }
        }, 7000l);

    }


    /**
     * Clearボタンをクリックした時に呼び出すイベントハンドラ
     * @param view
     */
    public void handleButtonClear(View view) {
        editLog.setText("");
    }


    /**
     * CommunicationTask5側のonPreExecute()からコールバックされるメソッド
     */
    @Override
    public void onPreExecute() {
        //ボタンの有効/無効を設定
        buttonConnect.setEnabled(false);
        buttonDisconnect.setEnabled(true);
        buttonSet.setEnabled(true);

        //移動先の穴の番号を初期化
        destinationHoleNo = "";

        //トーストの表示
        Toast.makeText(this, "CommunicationTask5 is started!.", Toast.LENGTH_SHORT).show();
    }


    /**
     * TcpClientTask側のonProgressUpdate()からコールバックされるメソッド
     * @param values   Logに出力するメッセージ
     */
    @Override
    public void onProgressUpdate(String... values) {
        // メインアクティビティのLogにメッセージを設定または追記
        if (editLog.length() == 0)
            editLog.setText(values[0]);
        else
            editLog.append("\n" + values[0]);
    }


    /**
     * CommunicationTask5側のonPostExecute()からコールバックされるメソッド
     * @param aVoid doInBackground()の戻り値
     */
    @Override
    public void onPostExecute(Void aVoid) {
        //トーストの表示
        Toast.makeText(this, "CommunicationTask5 is finished.", Toast.LENGTH_SHORT).show();

        //ボタンの有効/無効を設定
        buttonConnect.setEnabled(true);
        buttonDisconnect.setEnabled(false);
        buttonSet.setEnabled(false);
        buttonRun.setEnabled(false);

        //初期位置を指定するまでボールは非表示に設定
        ballView.setVisibility(View.INVISIBLE);
    }


    /**
     * アラートを表示するメソッド
     * @param message 表示するメッセージ
     */
    private void showAlertDialog(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Error!");			// ダイアログタイトルの設定
        dialog.setMessage(message);			// ダイアログメッセージの設定
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }
}

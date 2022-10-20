package keti.ccrc.actionrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import keti.ccrc.libai.ActionRecognition;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1001;

    private Timer timerCall;
    private int nCnt;
    TimerTask timerTask;

    private TextView outputTextView;

    ActionRecognition actionRecognition;

    // sw emulator 사용 시: true, device 사용 시: false
    private boolean emulation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputTextView = findViewById(R.id.textView);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            // --- KETI: o1. 초기화
            // --- activity_main.xml에서 CameraSourcePreview와 GraphicOverlay는 반드시 있어야 함
            actionRecognition.init(MainActivity.this, emulation);
        }

        nCnt = 0;
        timerCall = new Timer();
    }

    public void onStartRecognition(View view) {
        // --- KETI: o2. 행동인식 시작
        actionRecognition.start();
        outputTextView.setText("시작");

        if (timerTask != null) {
            timerTask.cancel();
        }

        nCnt = 0;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateResult();
                    }
                });
            }
        };

        // 1초 간격으로 행동인식 결과 호출
        timerCall.schedule(timerTask, 0, 1000);
    }

    public void onStopRecognition(View view) {
        // --- KETI: o3. 행동인식 중지
        actionRecognition.stop();
        outputTextView.setText("중지");

        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    private void updateResult() {
        //outputTextView.setText(String.valueOf(nCnt));

        // --- KETI: o4. 행동인식 결과값 가져오기
        outputTextView.setText(String.valueOf(actionRecognition.getCurrentAction()));

        nCnt++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        actionRecognition.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            actionRecognition.init(MainActivity.this, emulation);
        }
    }
}
package jp.techacademy.hiroko.ando.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button returnB;
    Button nextB;
    Button autoB;
    Cursor cursor;
    Timer timer;
    double timersec;
    Handler mHandler = new Handler();
    View v;

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        returnB = (Button) findViewById(R.id.returnB);
        nextB = (Button) findViewById(R.id.nextB);
        autoB = (Button) findViewById(R.id.autoB);
        v=(View) findViewById(R.id.parent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                slideFunction();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            slideFunction();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    slideFunction();
                }
                break;
            default:
                Snackbar.make(v,"ファイルアクセスが許可されていません", Snackbar.LENGTH_LONG)
                        .show();

                break;
        }
    }

    private void slideFunction(){
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            setImageView();
        }

        nextB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveView();
            }
        });

        returnB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.isFirst()){
                    cursor.moveToLast();
                }else{
                    cursor.moveToPrevious();
                }
                setImageView();
            }
        });

        autoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timer==null){
                    autoB.setText("停止");
                    nextB.setEnabled(false);
                    returnB.setEnabled(false);
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            timersec += 0.1;

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    moveView();
                                }
                            });
                        }
                    }, 2000, 2000);

                }else{
                    timer.cancel();
                    timer=null;
                    autoB.setText("再生");
                    nextB.setEnabled(true);
                    returnB.setEnabled(true);
                }

            }
        });



    }

    private void moveView() {
        if(cursor.isLast()){
            cursor.moveToFirst();
        }else{
            cursor.moveToNext();
        }

        setImageView();
    }

    private void setImageView() {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        imageView.setImageURI(imageUri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cursor!=null){
            cursor.close();
        }
    }
}

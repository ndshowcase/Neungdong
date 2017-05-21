package kr.ms.neungdong.showcase.neungdong;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
//이건바로 로고액티비티!!!!
public class Logo extends AppCompatActivity {

    private Handler handler; //변수정의 하는부분

    //여기는 액티비티 시작 시 애니메이션 주기 코드여
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(Logo.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        init();

        handler.postDelayed(runnable,1000);
        //이게 바로 얼마나 이 액티비티에 있을 것인지를 정하는 코드지

    }

    public void init() {
        handler = new Handler();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        handler.removeCallbacks(runnable);
        //음 이건 뭐냐
    }
}

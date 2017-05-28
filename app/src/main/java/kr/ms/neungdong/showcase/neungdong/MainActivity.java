package kr.ms.neungdong.showcase.neungdong;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sdsmdg.tastytoast.TastyToast;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private FragmentManager fragmentManager;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {




        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    return true;

                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }

    };

        /**
         * 리스트뷰와 어뎁터
         */
        ListView mListView;
        BapListAdapter mAdapter;

        /**
         * ProcessTask를 상속한 BapDownloadTask class
         */
        BapDownloadTask mProcessTask;

        /**
         * 진행 상황을 표시하기 위한 Dialog
         */
        ProgressDialog mDialog;

        /**
         * 오늘 날짜를 알아오기 위한 Calendar
         */
        Calendar mCalendar;

        /**
         * 2번이상 BapDownloadTask를 실행하지 않도록 도와주는 boolean
         */
        boolean isUpdating = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main2);
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            /**
             * 지금 날짜를 가져오기 위한 Calendar 생성
             */
            mCalendar = Calendar.getInstance();

            /**
             * 리스트뷰를 findViewById하고 mAdapter를 생성합니다.
             */
            mListView = (ListView) findViewById(R.id.mListView);
            mAdapter = new BapListAdapter(getApplicationContext());
            mListView.setAdapter(mAdapter);

            /**
             * 급식 리스트를 얻습니다.
             * isUpdate=true로 설정하여 급식이 없을경우 BapDownloadTask를 실행합니다.
             */
            getBapList(true);
        }

        private void getBapList(boolean isUpdate) {
            /**
             * 기존 데이터를 초기화 합니다.
             */
            mAdapter.clearData();
            mAdapter.notifyDataSetChanged();

            /**
             * mCalendar가 null이면 새로 생성합니다.
             */
            if (mCalendar == null)
                mCalendar = Calendar.getInstance();

            /**
             * 월요일 부터 급식을 표시하므로
             * 이번주 월요일 날짜로 설정합니다.
             */
            mCalendar.add(Calendar.DATE, 2 - mCalendar.get(Calendar.DAY_OF_WEEK));

            /**
             * for 반복문을 5번 돌면서 월요일부터 금요일까지의 급식 데이터를 가져옵니다.
             */
            for (int i = 0; i < 5; i++) {
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);

                /**
                 * BapTool을 이용해서 저장된 급식 데이터를 가져옵니다.
                 */
                BapTool.restoreBapDateClass mData =
                        BapTool.restoreBapData(getApplicationContext(), year, month, day);

                /**
                 * isBlankDay가 true이면 급식 데이터가 저장되지 않은 날입니다.
                 */
                if (mData.isBlankDay) {
                    if (Tools.isNetwork(getApplicationContext())) {
                        /**
                         * 네트워크가 켜져있으면
                         */
                        if (!isUpdating && isUpdate) {
                            /**
                             * mProcessTask가 실행중이지 않고 : !isUpdating
                             * 업데이트를 실행하라고 하면 : isUpdate
                             *
                             * TODO 작업중 표시를 커스텀하려면 이곳을 수정하세요
                             */
                            mDialog = new ProgressDialog(this);
                            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            mDialog.setMax(100);
                            mDialog.setTitle(R.string.loading_title);
                            mDialog.setCancelable(false);
                            mDialog.show();

                            /**
                             * 급식을 업데이트 합니다.
                             */
                            mProcessTask = new BapDownloadTask(getApplicationContext());
                            mProcessTask.execute(year, month, day);
                        }
                    } else {
                        /**
                         * 네트워크가 꺼져있으면 오류 메세지를 표시합니다.
                         * TODO 원하는 오류 처리방식으로 수정하세요
                         */
                        TastyToast.makeText(getApplicationContext(), "인터넷 연결이 안되서 데이터를 다운로드 할 수 없습니다", TastyToast.LENGTH_SHORT,TastyToast.ERROR).show();
                    }

                    return;
                }

                /**
                 * 급식 데이터가 저장되어 있으면 : mData.isBlankDay가 false이면
                 * mAdapter에 급식을 추가합니다.
                 */

                mAdapter.addItem(mData.Calender, mData.DayOfTheWeek, mData.Morning, mData.Lunch, mData.Dinner);
                mCalendar.add(Calendar.DATE, 1);
            }

            /**
             * TODO for문이 실행되고 나면 mCalendar의 날짜가 이번주 금요일을 설정되므로 mCalendar의 날짜를 다시 설정해주어야 합니다.
             */

            mAdapter.notifyDataSetChanged();
            setCurrentItem();
        }

        /**
         * 오늘 날짜에 맞는 급식을 바로 보여주기 위한 메소드
         * 월~금까지는 각자의 요일이 바로 위에 뜨며 토, 일은 월요일이 맨앞에 뜨게 됩니다.
         */
        private void setCurrentItem() {
            int DAY_OF_WEEK = mCalendar.get(Calendar.DAY_OF_WEEK);

            if (DAY_OF_WEEK > 1 && DAY_OF_WEEK < 7) {
                mListView.setSelection(DAY_OF_WEEK - 2);
            } else {
                mListView.setSelection(0);
            }
        }

        /**
         * ProcessTask를 상속받아 만든 BapDownloadTask
         */
        public class BapDownloadTask extends ProcessTask {
            public BapDownloadTask(Context mContext) {
                super(mContext);
            }

            @Override
            public void onPreDownload() {
                isUpdating = true;
            }

            @Override
            public void onUpdate(int progress) {
                /**
                 * TODO 작업 현황을 표시하는 방법을 커스텀 하세요
                 */
                mDialog.setProgress(progress);
            }

            @Override
            public void onFinish(long result) {
                if (mDialog != null)
                    mDialog.dismiss();

                isUpdating = false;

                if (result == -1) {
                    /**
                     * TODO 에러가 발생하면 어떻게 처리할건지 이곳에 작성하세요
                     */
                    return;
                }

                /**
                 * 무한 반복 업데이트를 막기 위해 isUpdate=false로 getBapList()을 호출합니다.
                 */
                getBapList(false);
            }
        }

        @Override
        protected void onPause() {
            super.onPause();

            /**
             * 앱을 일시중지 할경우 Dialog를 닫습니다.
             */
            if (mDialog != null)
                mDialog.dismiss();

            mCalendar = null;
        }
    private boolean isSecond = false;
    private Timer timer;

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if ((keycode == KeyEvent.KEYCODE_BACK)) {
            if (isSecond == false) {
                TastyToast.makeText(this, "진짜 종료하실 거예요?", TastyToast.LENGTH_LONG, TastyToast.WARNING).show();
                isSecond = true;
                TimerTask second = new TimerTask() {
                    @Override
                    public void run() {
                        timer.cancel();
                        timer = null;
                        isSecond = false;
                    }
                };
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                timer = new Timer();
                timer.schedule(second, 2000);
            } else {
                super.onBackPressed();
            }
        }
        return true;
        //이게 백버튼 두번 누르고 종료하게 하는 버튼이다
        //원래는 Toast.makeText(쏼라쏼라).show(); 이건데
        //내가 TastyToast 라이브러리를 추가했다
        //그러니까 이거 작업하려면 인터넷 연결하라는 소리야
    }
    }


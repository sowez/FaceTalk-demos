package com.example.opentok_tutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.OpentokError;
import android.support.annotation.NonNull;
import android.Manifest;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import android.widget.FrameLayout;


public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY = "46329592";
    private static String SESSION_ID = "2_MX40NjMyOTU5Mn5-MTU1NzkyODM0Njg4NH5PQlJ3VEN2bE02dzV3cHRkL3RLYjRXUDd-UH4";

    // 1:1 대화 가능하게 realy token 생성함
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjMyOTU5MiZzaWc9NTk5MWI5ZDA3Y2IyNTg5YTBkMjU5YzgwYmY5NjBkYjQzY2QwMTk0ODpzZXNzaW9uX2lkPTJfTVg0ME5qTXlPVFU1TW41LU1UVTFOemt5T0RNME5qZzROSDVQUWxKM1ZFTjJiRTAyZHpWM2NIUmtMM1JMWWpSWFVEZC1VSDQmY3JlYXRlX3RpbWU9MTU1NzkyODM3NiZub25jZT0wLjcxOTUwNjIwMDIyNzk5Njgmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU2MDUyMDM3NiZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
    private Publisher mPublisher;  // 방만든사람
    private Subscriber mSubscriber; // 방에 초대? 된사람
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // initialize view objects from your layout
            mPublisherViewContainer = findViewById(R.id.publisher_container);
            mSubscriberViewContainer = findViewById(R.id.subscriber_container);

            // initialize and connect to the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(this);
            mSession.connect(TOKEN);


        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    // Session.SessionListener override method
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build(); // publisher 객체를 instance화
        mPublisher.setPublisherListener(this); // Publisher listener interface구현하는 object

        mPublisherViewContainer.addView(mPublisher.getView()); //장치의 카메라에서 캡처 한 비디오를 표시
        mSession.publish(mPublisher); // publisher객체를 전달하여 opentok session에 publish
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber); // 수신된 스크림에 등록한다.
            mSubscriberViewContainer.addView(mSubscriber.getView()); // 새 구독 스트림 뷰를 화면에 배치
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        // 스트림이 삭제되면 구독자의 뷰를 제거
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }

    // PublisherKit.PublisherListener override method
    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
    }
}

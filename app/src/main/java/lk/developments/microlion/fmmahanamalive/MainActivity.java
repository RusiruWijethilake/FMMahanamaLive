package lk.developments.microlion.fmmahanamalive;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.MediaMetaData;

public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener, CurrentSessionCallback {

    private Context context = this;

    private boolean onairNow = false;
    private String nowplaying = "Nothing";
    private String streamby = "Developer";
    private String playUrl = "";
    private String coverImgUrl = "";

    private FirebaseFirestore firedb;
    private FirebaseStorage firestorage;
    private DocumentReference publicData;
    private FirebaseMessaging fireMessage;
    private AudioStreamingManager streamingManager;
    private MediaMetaData mediaObj;
    private NetworkStateReceiver networkStateReceiver;
    private FirebaseAnalytics mFirebaseAnalytics;

    private AdView fmAd1;
    private InterstitialAd mInterstitialAd;
    private ViewGroup rootView, offlineview, onlineview;
    private ImageButton btnPlay, btnTheme, btnScore, btnShare, btnInfo;
    private TextView txtNowPlaying, txtBy;
    private SimpleDraweeView imgCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //---init classes---//
        FirebaseApp.initializeApp(context);
        Fresco.initialize(this);

        //---class---//
        fmmToolKit.setDefaultTheme(context);
        this.setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---init Variables---//
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firedb = FirebaseFirestore.getInstance();
        firestorage = FirebaseStorage.getInstance();
        fireMessage = FirebaseMessaging.getInstance();
        networkStateReceiver = new NetworkStateReceiver();
        streamingManager = AudioStreamingManager.getInstance(context);

        publicData = firedb.collection("public").document("stream");
        rootView = findViewById(R.id.view_home_root);
        btnTheme = findViewById(R.id.btn_theme);
        btnScore = findViewById(R.id.btn_score);
        btnPlay = findViewById(R.id.btn_play);
        btnShare = findViewById(R.id.btn_share);
        btnInfo = findViewById(R.id.btn_info);
        offlineview = findViewById(R.id.viewOffline);
        onlineview = findViewById(R.id.viewOnline);
        txtNowPlaying = findViewById(R.id.txtNowPlaying);
        txtBy = findViewById(R.id.txtBy);
        imgCover = findViewById(R.id.img_cover);

        //---set actions---//
        onlineview.setVisibility(View.GONE);
        offlineview.setVisibility(View.VISIBLE);
        networkStateReceiver.addListener(this);
        registerReceiver(this.networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        loadCloudData();
        streamingManager.setPlayMultiple(false);
        mediaObj = new MediaMetaData();
        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fmmToolKit.darkModeToggle(context);
            }
        });
        btnScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                Intent scoreint = new Intent(context, ScoreboardActivity.class);
                startActivity(scoreint);
            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onairNow) {
                    radioStreamPlayerManager();
                } else {
                    new MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.txt_com_dialog_offair_title)
                            .setMessage(R.string.txt_com_dialog_offair_desc)
                            .setPositiveButton(R.string.txt_com_dialog_btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.txt_com_dialog_btn_moreinfo, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    fmmToolKit.openFacebookPage(context, getString(R.string.txt_com_open_web_mcrcfb));
                                }
                            })
                            .show();
                }
            }
        });
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(context)
                        .setView(R.layout.layout_about)
                        .setPositiveButton(R.string.txt_com_dialog_btn_gotit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.txt_com_dialog_btn_moreinfo, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                fmmToolKit.openLink(context, getString(R.string.txt_com_open_web_ml));
                            }
                        })
                        .show();
            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareMessage = getString(R.string.txt_share_message);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.txt_com_dialog_share_via)));
            }
        });

        //---Init Ads---//
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_id_com_openact));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
        fmAd1 = findViewById(R.id.fmad1);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        fmAd1.loadAd(adRequest);

        //checkForFirstTime
        if (fmmToolKit.isThisFirstTime(context)) {
            Rect startUp = new Rect();
            new TapTargetSequence(this)
                    .targets(
                            TapTarget.forBounds(startUp, "Welcome to FM Mahanama", "Listen to FM Mahanama easily with advanced features. Let's make a quick introduction")
                                    .id(1)
                                    .cancelable(true)
                                    .targetRadius(60),
                            TapTarget.forView(btnPlay, "Play live stream when it's onair")
                                    .id(2)
                                    .cancelable(true),
                            TapTarget.forView(btnTheme, "Introducing dark and light mode", "Toggle between dark and light mode or set it to happen auto")
                                    .id(3)
                                    .cancelable(true),
                            TapTarget.forView(btnScore, "Introducing new scoreboard", "Live scoreboard with more additional details")
                                    .id(4)
                                    .cancelable(true),
                            TapTarget.forBounds(startUp, "And much more stuff", "FM Mahanama is now lightweight and automatically configure audio quality with the type of your connection.")
                                    .id(5)
                                    .cancelable(true)
                    )
                    .listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            fmmToolKit.setNotFirstTime(context);
                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {

                        }
                    })
                    .considerOuterCircleCanceled(true)
                    .continueOnCancel(true)
                    .start();
        }
    }

    private void radioStreamPlayerManager() {
        if (streamingManager.isPlaying()) {
            btnPlay.setImageResource(R.drawable.ic_play_circle);
            streamingManager.onStop();
        } else {
            if (isNetworkAvaliable(context)) {
                btnPlay.setImageResource(R.drawable.ic_pause_circle);
                mediaObj.setMediaId(getString(R.string.txt_radio_id));
                mediaObj.setMediaTitle(nowplaying);
                mediaObj.setMediaArtist(streamby);
                mediaObj.setMediaUrl(playUrl);
                mediaObj.setMediaArt(coverImgUrl);
                streamingManager.setShowPlayerNotification(true);
                streamingManager.setPendingIntentAct(getNotificationPendingIntent());
                streamingManager.onPlay(mediaObj);
            } else {
                Snackbar.make(rootView, R.string.txt_com_no_internet, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        if (streamingManager != null) {
            streamingManager.subscribesCallBack(this);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (streamingManager != null) {
            streamingManager.unSubscribeCallBack();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (streamingManager != null) {
            streamingManager.unSubscribeCallBack();
        }
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (streamingManager.isPlaying()) {
            btnPlay.setImageResource(R.drawable.ic_pause_circle);
        } else {
            btnPlay.setImageResource(R.drawable.ic_play_circle);
        }
    }

    @Override
    public void setTheme(int resId) {
        super.setTheme(resId);
    }

    private void loadCloudData() {
        publicData.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                } else {
                    onairNow = snapshot.getBoolean("onair");
                }
                if (snapshot != null && snapshot.exists()) {
                    TransitionManager.beginDelayedTransition(rootView, new Slide());
                    if (onairNow) {
                        onlineview.setVisibility(View.VISIBLE);
                        offlineview.setVisibility(View.GONE);
                        if (!(snapshot.getString("link").equals(playUrl))) {
                            Snackbar.make(rootView, getString(R.string.txt_snack_stream_restart), Snackbar.LENGTH_SHORT).show();
                            playUrl = snapshot.getString("link");
                            if (streamingManager.isPlaying()) {
                                radioStreamPlayerManager();
                                btnPlay.setImageResource(R.drawable.ic_play_circle);
                                radioStreamPlayerManager();
                            }
                        }
                        playUrl = snapshot.getString("link");
                        String now = snapshot.getString("nowplaying");
                        String by = snapshot.getString("by");
                        nowplaying = now;
                        streamby = by;
                        if (!now.isEmpty()) {
                            txtNowPlaying.setText(now);
                        } else {
                            txtNowPlaying.setText(R.string.txt_nothing_placeholder);
                            mediaObj.setMediaTitle("FM Mahanama");
                        }
                        if (!by.isEmpty()) {
                            txtBy.setText(by);
                            mediaObj.setMediaArtist(streamby);
                        } else {
                            txtBy.setText(R.string.txt_nothing_placeholder);
                            mediaObj.setMediaArtist("MCRC");
                        }
                        final String imgurl = snapshot.getString("cover_img");
                        if (imgurl.isEmpty()) {
                            imgCover.setImageResource(R.drawable.ic_product_logo);
                            coverImgUrl = "";
                            mediaObj.setMediaArt("");
                        } else {
                            if (imgurl.startsWith("gs")) {
                                final StorageReference imageRef = firestorage.getReferenceFromUrl(imgurl);
                                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        coverImgUrl = uri.toString();
                                        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                                                .setProgressiveRenderingEnabled(true)
                                                .build();
                                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                                .setImageRequest(request)
                                                .setOldController(imgCover.getController())
                                                .build();
                                        imgCover.setController(controller);
                                        mediaObj.setMediaArt(uri.toString());
                                    }
                                });
                            } else {
                                coverImgUrl = imgurl;
                                imgCover.setImageURI(imgurl);
                                mediaObj.setMediaArt(imgurl);
                            }
                        }
                    } else {
                        onlineview.setVisibility(View.GONE);
                        offlineview.setVisibility(View.VISIBLE);
                        imgCover.setImageURI("");
                        streamingManager.onStop();
                        btnPlay.setImageResource(R.drawable.ic_play_circle);
                    }
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    private boolean isNetworkAvaliable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED);
    }

    @Override
    public void networkAvailable() {
        onlineview.setVisibility(View.VISIBLE);
        offlineview.setVisibility(View.GONE);
        Snackbar.make(rootView, "Internet available", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void networkUnavailable() {
        onlineview.setVisibility(View.GONE);
        offlineview.setVisibility(View.VISIBLE);
        if (streamingManager.isPlaying()) {
            streamingManager.onStop();
        }
    }

    private PendingIntent getNotificationPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction("openplayer");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return mPendingIntent;
    }

    @Override
    public void updatePlaybackState(int state) {
        switch (state) {
            case PlaybackState.STATE_PLAYING:
                break;
            case PlaybackState.STATE_PAUSED:
                break;
            case PlaybackState.STATE_NONE:
                break;
            case PlaybackState.STATE_STOPPED:
                break;
            case PlaybackState.STATE_BUFFERING:
                break;
        }
    }

    @Override
    public void playSongComplete() {

    }

    @Override
    public void currentSeekBarPosition(int progress) {

    }

    @Override
    public void playCurrent(int indexP, MediaMetaData currentAudio) {

    }

    @Override
    public void playNext(int indexP, MediaMetaData currentAudio) {

    }

    @Override
    public void playPrevious(int indexP, MediaMetaData currentAudio) {

    }
}

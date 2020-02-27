package lk.developments.microlion.fmmahanamalive;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ScoreboardActivity extends AppCompatActivity {

    private Context context = this;
    private String tvLink = "";
    private int currentUpdateList = 0;
    private List<UpdateItem> updateItemList = new ArrayList<>();
    private Map<String, Object> scoreMap;

    private FirebaseFirestore firestore;
    private UpdateItemAdapter uiAdapter;

    private ViewGroup rootView, viewOn, viewOff, viewTVOn;
    private AdView fmAd2;
    private InterstitialAd mInterstitialAd;
    private ImageButton btnGoHome, btnShareScore;
    private TextView txtScoreName, txtScoreDesc, txtScore1name, txtScore2name, txtScore1score, txtScore2score, txtScore1stat, txtScore2stat, txtScoreTime;
    private MaterialButton btnOpenTV;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        firestore = FirebaseFirestore.getInstance();

        context = this;
        rootView = findViewById(R.id.view_score_root);
        viewOn = findViewById(R.id.layoutOngoingMatch);
        viewOff = findViewById(R.id.layoutNoMatch);
        viewTVOn = findViewById(R.id.txtTVOn);
        btnGoHome = findViewById(R.id.btnGoHome);
        recyclerView = findViewById(R.id.view_score_updatelist);
        btnShareScore = findViewById(R.id.btnShareScore);
        txtScoreName = findViewById(R.id.txt_score_name);
        txtScoreDesc = findViewById(R.id.txt_score_desc);
        txtScore1name = findViewById(R.id.txt_score_one_name);
        txtScore2name = findViewById(R.id.txt_score_two_name);
        txtScore1score = findViewById(R.id.txt_score_one_score);
        txtScore2score = findViewById(R.id.txt_score_two_score);
        txtScore1stat = findViewById(R.id.txt_score_one_status);
        txtScore2stat = findViewById(R.id.txt_score_two_status);
        txtScore2stat = findViewById(R.id.txt_score_two_status);
        txtScoreTime = findViewById(R.id.txt_score_time);
        btnOpenTV = findViewById(R.id.btn_score_openTVM);

        viewOn.setVisibility(View.GONE);
        viewOff.setVisibility(View.VISIBLE);
        viewOn.setVisibility(View.GONE);
        viewOff.setVisibility(View.VISIBLE);
        uiAdapter = new UpdateItemAdapter(updateItemList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        recyclerView.setAdapter(uiAdapter);
        initActions();

        fmAd2 = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        fmAd2.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_id_com_openact));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void initActions() {
        btnGoHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScoreboardActivity.this.finish();
            }
        });
        btnShareScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareMessage = getString(R.string.txt_score_share_msg);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.txt_com_dialog_share_via)));
            }
        });
        firestore.collection("public").document("stream")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable DocumentSnapshot doc, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                        if (doc.getBoolean("tvonair")) {
                            TransitionManager.beginDelayedTransition(rootView, new Slide());
                            viewTVOn.setVisibility(View.VISIBLE);
                            tvLink = doc.getString("tvlink");
                        } else {
                            TransitionManager.beginDelayedTransition(rootView, new Slide());
                            viewTVOn.setVisibility(View.GONE);
                        }
                    }
                });
        firestore.collection("scoreboard")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            viewOn.setVisibility(View.GONE);
                            viewOff.setVisibility(View.VISIBLE);
                            return;
                        }
                        for (QueryDocumentSnapshot doc : value) {
                            if (value.isEmpty()) {
                                viewOn.setVisibility(View.GONE);
                                viewOff.setVisibility(View.VISIBLE);
                            } else {
                                if (doc.getBoolean("ongoing") == false) {
                                    viewOn.setVisibility(View.GONE);
                                    viewOff.setVisibility(View.VISIBLE);
                                } else {
                                    scoreMap = doc.getData();
                                    viewOn.setVisibility(View.VISIBLE);
                                    viewOff.setVisibility(View.GONE);
                                    txtScoreName.setText(doc.getString("match_name"));
                                    txtScoreDesc.setText(doc.getString("match_desc"));
                                    txtScore1name.setText(doc.getString("one_name"));
                                    txtScore1score.setText(doc.getString("one_score"));
                                    txtScore1stat.setText(doc.getString("one_stats"));
                                    txtScore2name.setText(doc.getString("two_name"));
                                    txtScore2score.setText(doc.getString("two_score"));
                                    txtScore2stat.setText(doc.getString("two_stats"));
                                    Date date = doc.getTimestamp("timestamp").toDate();
                                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                                    DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
                                    String dateTime = "Created : " + timeFormat.format(date) + " " + dateFormat.format(date);
                                    txtScoreTime.setText(dateTime);
                                    ArrayList list = (ArrayList) doc.getData().get("match_del_col");
                                    addToUpdateList(list);
                                }
                                //Show Old Scores
                            }
                        }
                    }
                });
        btnOpenTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvLink.equals("")) {
                    fmmToolKit.openLink(context, tvLink);
                }
            }
        });

    }

    private void addToUpdateList(ArrayList list) {
        int newsize = list.size();
        if (newsize < currentUpdateList) {
            currentUpdateList = 0;
            updateItemList.clear();
        }
        for (int i = currentUpdateList; i < newsize; i++) {
            Map map = (Map) list.get(i);
            UpdateItem updateItem = new UpdateItem(map.get("title").toString(), map.get("desc").toString());
            updateItemList.add(updateItem);
        }
        uiAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(newsize - 1);
        currentUpdateList = newsize;
    }
}

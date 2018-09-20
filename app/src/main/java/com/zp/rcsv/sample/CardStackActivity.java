package com.zp.rcsv.sample;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.devilist.cardstackview.CardManager;
import com.devilist.cardstackview.CardRecyclerView;
import com.devilist.cardstackview.PokerCardManager;
import com.devilist.cardstackview.TowerCardManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengp on 2017/12/5.
 */

public class CardStackActivity extends AppCompatActivity implements RCSAdapter.OnItemClickListener,
        CardManager.OnCardDragListener {

    private CardRecyclerView crv_list;

    private List<AppInfo> appInfolist = new ArrayList<>();

    public static void start(Context context, String type) {
        Intent starter = new Intent(context, CardStackActivity.class);
        starter.putExtra("type", type);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_stack);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initView();
                    }
                });
            }
        }).start();

    }

    private void initView() {
        findViewById(R.id.tv_load).setVisibility(View.GONE);
        crv_list = findViewById(R.id.crv_list);
        RCSAdapter adapter = new RCSAdapter(this);
        adapter.addData(appInfolist);
        adapter.setOnItemClickListener(this);
        crv_list.setAdapter(adapter);
        String type = getIntent().getStringExtra("type");
        if (type.equals("tower")) {
            TowerCardManager towerCardManager = new TowerCardManager(this, crv_list);
            towerCardManager.setCardOffset(25);
            towerCardManager.setCardElevation(50);
            towerCardManager.setOnCardDragListener(this);
            crv_list.setLayoutManager(towerCardManager);
        } else if (type.equals("poker")) {
            PokerCardManager pokerCardManager = new PokerCardManager(this, crv_list);
            pokerCardManager.setCardOffset(25);
            pokerCardManager.setCardElevation(50);
            pokerCardManager.setOnCardDragListener(this);
            crv_list.setLayoutManager(pokerCardManager);
        }
    }

    private void initData() {

        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);

        for (int i = 0; i < packages.size(); i++) {

            PackageInfo packageInfo = packages.get(i);
            String appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            Drawable appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            String packageName = packageInfo.packageName;

            AppInfo appInfo = new AppInfo();
            appInfo.setAppName(appName);
            appInfo.setAppIcon(appIcon);
            appInfo.setVersionCode(versionCode);
            appInfo.setVersionName(versionName);
            appInfo.setPackageName(packageName);

            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appInfolist.add(appInfo);
            }
        }
    }

    @Override
    public void onDraggingStateChanged(View view, boolean isDragging, boolean isDropped, float offsetX, float offsetY) {

    }

    @Override
    public void onCardDragging(View view, float offsetX, float offsetY) {

    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "onItemClick", Toast.LENGTH_SHORT).show();
    }
}

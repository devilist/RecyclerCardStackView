package com.zp.rcsv.sample;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.devilist.cardstackview.CardRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengpu on 2016/10/28.
 */

public class RCSAdapter extends CardRecyclerView.CardAdapter<RecyclerView.ViewHolder>  {

    private Context context;
    private List<AppInfo> appInfolist = new ArrayList<>();
    private List<AppInfo> tempData = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;


    public RCSAdapter(Context mContext) {
        this.context = mContext;
    }

    public void addData(List<AppInfo> data) {
        this.appInfolist = data;
        notifyDataSetChanged();
    }

    @Override
    public int getVisibleCardCount() {
        return 3;
    }

    @Override
    protected void delItem(int position) {
        if (null != appInfolist && appInfolist.size() > 0) {
            appInfolist.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    protected void recycleData() {
        if (isEnableDataRecycle()) {
            if (appInfolist.size() > getVisibleCardCount() + 1) {
                tempData.add(appInfolist.get(0));
                delItem(0);
            } else {
                tempData.add(appInfolist.get(0));
                appInfolist.remove(0);
                notifyItemRemoved(0);
                int start = appInfolist.size();
                appInfolist.addAll(tempData);
                notifyItemRangeInserted(start, tempData.size());
//                notifyDataSetChanged();
                tempData.clear();
            }
        }
    }

    @Override
    protected boolean isEnableDataRecycle() {
        return true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.rcs_activity_item, parent, false);
        return new ViewHolder(v, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AppInfo appInfo = appInfolist.get(position);
        ViewHolder mViewHolder = (ViewHolder) holder;
        mViewHolder.appNameTv.setText(appInfo.getAppName());
        mViewHolder.appIconIv.setImageDrawable(appInfo.getAppIcon());
        mViewHolder.appVNameTv.setText("v" + appInfo.getVersionName());
        mViewHolder.appPnameTv.setText(appInfo.getPackageName());
    }

    @Override
    public int getItemCount() {
        return appInfolist.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CardView rootCv;
        private ImageView appIconIv;
        private TextView appNameTv;
        private TextView appVCodeTv;
        private TextView appVNameTv;
        private TextView appPnameTv;
        private OnItemClickListener mListener;

        public ViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mListener = listener;

            rootCv = (CardView) itemView.findViewById(R.id.cv_view);
            appIconIv = (ImageView) itemView.findViewById(R.id.iv_app_icon);
            appNameTv = (TextView) itemView.findViewById(R.id.tv_app_name);
            appVNameTv = (TextView) itemView.findViewById(R.id.tv_app_version_name);
            appPnameTv = (TextView) itemView.findViewById(R.id.tv_app_package_name);

            rootCv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onItemClick(v, getLayoutPosition());
        }
    }
}


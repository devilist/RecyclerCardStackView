/*
 * Copyright  2017  zengpu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.devilist.cardstackview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


/**
 * a helper to handle the drag event of the {@link CardRecyclerView}'s child views;
 * Created by zengp on 2017/9/30.
 */

public abstract class CardManager extends RecyclerView.LayoutManager implements View.OnTouchListener {

    protected int mScreenWidth;
    protected int mScreenHeight;
    protected CardRecyclerView mRecyclerView;

    protected OnCardDragListener mListener;

    public CardManager(Context context, CardRecyclerView recyclerView) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = wm.getDefaultDisplay().getWidth();
        mScreenHeight = wm.getDefaultDisplay().getHeight();
        mRecyclerView = recyclerView;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    public abstract void onLayoutCards(RecyclerView.Recycler recycler, RecyclerView.State state);

    public void dropCardNoTouch(int orientation) {
    }

    @Override
    public final void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        if (getItemCount() <= 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }

        // remove all attached child views
        detachAndScrapAttachedViews(recycler);

        // re-layout
        onLayoutCards(recycler, state);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public void setOnCardDragListener(OnCardDragListener listener) {
        this.mListener = listener;
    }

    public interface OnCardDragListener {

        void onDraggingStateChanged(View view, boolean isDragging, boolean isDropped, float offsetX, float offsetY);

        void onCardDragging(View view, float offsetX, float offsetY);
    }

}

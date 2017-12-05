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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


/**
 * Created by zengp on 2017/09/30.
 */

public class CardRecyclerView extends RecyclerView {

    public CardRecyclerView(Context context) {
        this(context, null);
    }

    public CardRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getVisibleCardCount() {
        if (getAdapter().getVisibleCardCount() <= 0)
            throw new IllegalArgumentException("visibleCardCount must be over zero !!!");
        return getAdapter().getVisibleCardCount();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof CardAdapter)
            super.setAdapter(adapter);
        else
            throw new IllegalArgumentException("");
    }

    @Override
    public CardAdapter getAdapter() {
        return (CardAdapter) super.getAdapter();
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof CardManager)
            super.setLayoutManager(layout);
        else
            throw new IllegalArgumentException("");
    }

    @Override
    public CardManager getLayoutManager() {
        return (CardManager) super.getLayoutManager();
    }

    public void dropCard(int orientation) {
        if (getChildCount() > 0)
            getLayoutManager().dropCardNoTouch(orientation);
    }

    protected void dropCard() {
        if (getAdapter().isEnableDataRecycle()) {
            getAdapter().recycleData();
        } else
            getAdapter().delItem(0);
    }

    public static abstract class CardAdapter<VH extends ViewHolder> extends Adapter<VH> {

        protected abstract void delItem(int position);

        protected abstract void recycleData();

        public int getVisibleCardCount() {
            return 3;
        }

        protected boolean isEnableDataRecycle() {
            return false;
        }

    }
}

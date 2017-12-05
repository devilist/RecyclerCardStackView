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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;


/**
 * Created by zengp on 2017/09/30.
 */
public class PokerCardManager extends CardManager {

    private float mTouchDownX = 0, mTouchDownY = 0;
    private boolean mIsTouchUp = false;
    // the dragged border position to differentiate whether the dragged card be drop or reset
    private float mDragThresholdX, mDragThresholdY;
    private int mMinVelocityThreshold = 2000;
    private int mMaxVelocityThreshold = 4500;

    private VelocityTracker mVelocityTracker = null;

    private int mCardOffset = 10;
    private int mCardElevation = 10;

    public PokerCardManager(Context context, CardRecyclerView recyclerView) {
        super(context, recyclerView);
        // forbidden the default item animator
        mRecyclerView.setItemAnimator(null);
        mDragThresholdX = mScreenWidth / 3;
        mDragThresholdY = mScreenHeight / 3;
        ViewCompat.setTranslationZ(mRecyclerView, -1);
    }

    public void setCardOffset(int cardOffset) {
        if (cardOffset < 0)
            throw new IllegalArgumentException("cardOffset must be over zero !!!");
        this.mCardOffset = cardOffset;
    }

    public void setCardElevation(int cardElevation) {
        if (cardElevation < 0)
            throw new IllegalArgumentException("cardElevation must be over zero !!!");
        this.mCardElevation = cardElevation;
    }


    @Override
    public void onLayoutCards(RecyclerView.Recycler recycler, RecyclerView.State state) {
        onLayoutCards(recycler, state, mRecyclerView.getVisibleCardCount(), mCardOffset, mCardElevation);
        setTargetDragCard();
    }

    private void onLayoutCards(RecyclerView.Recycler recycler, RecyclerView.State state,
                               int visibleCardCount, int cardOffset, int cardElevation) {
        if (getItemCount() > 0) {
            // calculate the validate areas that all the visible cards cover.
            int left = 0;
            int top = 0;
            // in order to compat lower version, card must be added from last to first,
            // and if all the cards are added at every time this method invoked, there may
            // be some performance problems happened, such as OOM crash, long time wasted to
            // attached cards,and less smoothly when scroll etc.So to avoid these potential
            // problems, only necessary cards will be attached to improve performance
            int maxAttachChildrenCount = Math.min(mRecyclerView.getVisibleCardCount(), getItemCount() - 1);
            for (int i = maxAttachChildrenCount; i >= 0; i--) {
                View child_i = recycler.getViewForPosition(i);
                addView(child_i);
                measureChildWithMargins(child_i, 0, 0);
                int childWidth = getDecoratedMeasuredWidth(child_i);
                int childHeight = getDecoratedMeasuredHeight(child_i);
                if (i == maxAttachChildrenCount) {
                    int childWidthWithTotalOffset = childWidth + cardOffset * (visibleCardCount - 1);
                    int childHeightWithTotalOffset = childHeight + cardOffset * (visibleCardCount - 1);
                    int parentWExcludePadding = getWidth() - getPaddingLeft() - getPaddingRight();
                    int parentHExcludePadding = getHeight() - getPaddingTop() - getPaddingBottom();
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child_i.getLayoutParams();
                    int childMarginHorizOffset = params.leftMargin - params.rightMargin;
                    int childMarginVertOffset = params.topMargin - params.bottomMargin;
                    left = (parentWExcludePadding - childWidthWithTotalOffset) / 2 + getPaddingLeft() + childMarginHorizOffset;
                    top = (parentHExcludePadding - childHeightWithTotalOffset) / 2 + getPaddingTop() + childMarginVertOffset;
                }
                // remove decorator area
                Rect childRect = new Rect();
                calculateItemDecorationsForChild(child_i, childRect);
                int left_i, top_i;
                if (i <= visibleCardCount - 1) {
                    left_i = left + cardOffset * i;
                    top_i = top + cardOffset * (visibleCardCount - 1 - i);
                    // set elevations for all the visible children
                    ViewCompat.setTranslationZ(child_i, cardElevation * (visibleCardCount - i));
                } else {
                    left_i = left + cardOffset * (visibleCardCount - 1);
                    top_i = top;
                    ViewCompat.setTranslationZ(child_i, 0);
                }
                // reset card
                child_i.setTranslationY(0);
                child_i.setTranslationX(0);
                child_i.setAlpha(1f);
                child_i.setRotation(0);
                child_i.setScaleX(1f);
                child_i.setScaleY(1f);
                layoutDecorated(child_i, left_i, top_i, left_i + childWidth, top_i + childHeight);
            }
        }
    }

    private void setTargetDragCard() {
        // count of all the attached cards
        int maxAttachChildrenCount = Math.min(mRecyclerView.getVisibleCardCount(), getItemCount() - 1);
        if (maxAttachChildrenCount >= 0) {
            for (int i = 0; i <= maxAttachChildrenCount; i++) {
                getChildAt(i).setOnTouchListener(this);
                if (i == maxAttachChildrenCount) {
                    dispatchOnDragEvent(getChildAt(i), false, false, 0, 0);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int maxAttachChildrenCount = Math.min(mRecyclerView.getVisibleCardCount(), getItemCount() - 1);
        if (v != getChildAt(maxAttachChildrenCount))
            return true;
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int velocityX = 0;
        int velocityY = 0;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ViewCompat.setTranslationZ(mRecyclerView, 1);
            mTouchDownX = event.getRawX();
            mTouchDownY = event.getRawY();
            mIsTouchUp = false;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mIsTouchUp) {
                mTouchDownX = event.getRawX();
                mTouchDownY = event.getRawY();
                mIsTouchUp = false;
            }
            ViewCompat.setTranslationZ(mRecyclerView, 1);
            if (event.getEventTime() - event.getDownTime() >= 100) {
                dragCard(v, event.getRawX() - mTouchDownX, event.getRawY() - mTouchDownY,
                        event.getRawX() - mTouchDownX);
                dispatchOnDragEvent(v, true, false, event.getRawX() - mTouchDownX, event.getRawY() - mTouchDownY);
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mIsTouchUp = true;
            if (mVelocityTracker != null) {
                mVelocityTracker.computeCurrentVelocity(1000);
                velocityX = (int) mVelocityTracker.getXVelocity();
                velocityY = (int) mVelocityTracker.getYVelocity();
            }
            int velocity = (int) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (velocity > mMaxVelocityThreshold && event.getEventTime() - event.getDownTime() < 100) {
                ViewCompat.setTranslationZ(mRecyclerView, -1);
                return true;
            } else if (velocity >= mMinVelocityThreshold
                    || event.getEventTime() - event.getDownTime() >= 100) {
                releaseCard(v, event.getRawX() - mTouchDownX, event.getRawY() - mTouchDownY, velocity);
                return true;
            } else
                ViewCompat.setTranslationZ(mRecyclerView, -1);
        }
        return false;
    }

    // hold the card just dragging as free as you can
    private void dragCard(View card, float offset_x, float offset_y, float touchX) {
        // trans
        card.setTranslationX(offset_x);
        card.setTranslationY(offset_y);
        // factor
        float distance = (float) Math.sqrt(offset_x * offset_x + offset_y * offset_y);
        float maxDistance = (float) Math.sqrt(mScreenWidth * mScreenWidth + mScreenHeight * mScreenHeight);
        float factor = Math.min(1, distance / maxDistance);
        // tansZ
        float ori_elevation = mRecyclerView.getVisibleCardCount() * mCardElevation;
        ViewCompat.setTranslationZ(card, (float) (ori_elevation * (1 + Math.sqrt(factor))));
        //scale
        card.setScaleX(1 - factor);
        card.setScaleY(1 - factor);
        // alpha
        card.setAlpha(1 - factor * factor);
        // rotate
        float rotateDegree = offset_x == 0 ? 0 : (float) Math.asin(touchX / mScreenWidth);
        rotateDegree = (float) (rotateDegree * 180 / Math.PI);
        // deg factor to make the rotate more smooth
        // rotateDegree = rotateDegree * factor;
        card.setRotation(rotateDegree);
        refreshOtherVisibleCardsPosition(offset_x, offset_y);
    }

    private void releaseCard(View card, float offset_x, float offset_y, int velocity) {
        // check card status to decide next action
        if (Math.abs(offset_x) >= mDragThresholdX
                || Math.abs(offset_y) >= mDragThresholdY
                || (velocity >= mMinVelocityThreshold && velocity <= mMaxVelocityThreshold)) {
            dropCard(card);
        } else {
            resetDragCard(card);
        }
    }

    // drop card without ontouch event drop from left if orientation is minus otherwise right
    @Override
    public void dropCardNoTouch(int orientation) {
        if (getChildCount() <= 0)
            return;
        ViewCompat.setTranslationZ(mRecyclerView, 1);
        final View card = getChildAt(getChildCount() - 1);
        orientation = orientation < 0 ? -1 : 1;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1.1f);
        final int finalOrientation = orientation;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float offset = (float) animation.getAnimatedValue();
                dragCard(card, finalOrientation * mDragThresholdX * offset, mDragThresholdY * offset / 5,
                        finalOrientation * mScreenWidth / 2 * offset);
                dispatchOnDragEvent(card, true, false, card.getTranslationX(), card.getTranslationY());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dropCard(card);
            }
        });
        animator.setDuration(150);
        animator.start();
    }

    // del the current card or recycle it
    private void dropCard(final View card) {
        final float oriTransX = card.getTranslationX();
        final float oriTransY = card.getTranslationY();
        final float oriRotateDeg = card.getRotation();
        final float oriScaleX = card.getScaleX();
        final float oriScaleY = card.getScaleY();
        final float oriAlpha = card.getAlpha();

        ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float offset = (float) animation.getAnimatedValue();
                card.setTranslationX(3 * oriTransX - 2 * oriTransX * offset);
                card.setTranslationY(3 * oriTransY - 2 * oriTransY * offset);
                card.setRotation(3 * oriRotateDeg - 2 * oriRotateDeg * offset);
                card.setScaleX(oriScaleX * offset);
                card.setScaleY(oriScaleY * offset);
                card.setAlpha(oriAlpha * offset);
                refreshOtherVisibleCardsPosition(mDragThresholdX + (Math.abs(oriTransX) - mDragThresholdX) * offset,
                        mDragThresholdY + (Math.abs(oriTransY) - mDragThresholdY) * offset);
                dispatchOnDragEvent(card, false, false, oriTransX, oriTransY);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != mRecyclerView && null != mRecyclerView.getAdapter()) {
                    mRecyclerView.dropCard();
                    dispatchOnDragEvent(card, false, true, oriTransX, oriTransY);
                    ViewCompat.setTranslationZ(mRecyclerView, -1);
                }
            }
        });
        animator.setDuration(300);
        animator.start();
    }

    // let the card go back to its ori position
    private void resetDragCard(final View card) {
        final float oriTransX = card.getTranslationX();
        final float oriTransY = card.getTranslationY();
        final float oriRotateDeg = card.getRotation();
        final float oriScaleX = card.getScaleX();
        final float oriScaleY = card.getScaleY();
        final float oriAlpha = card.getAlpha();

        final float oriTransZ = ViewCompat.getTranslationZ(card);
        final float targetTransZ = mRecyclerView.getVisibleCardCount() * mCardElevation;

        ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float offset = (float) animation.getAnimatedValue();
                card.setTranslationX(oriTransX * offset);
                card.setTranslationY(oriTransY * offset);
                ViewCompat.setTranslationZ(card, targetTransZ + (oriTransZ - targetTransZ) * offset);
                card.setRotation(oriRotateDeg * offset);
                card.setScaleX((oriScaleX - 1) * offset + 1);
                card.setScaleY((oriScaleY - 1) * offset + 1);
                card.setAlpha((oriAlpha - 1) * offset + 1);
                refreshOtherVisibleCardsPosition(oriTransX * offset, oriTransY * offset);
                dispatchOnDragEvent(card, false, false, oriTransX * offset, oriTransY * offset);

            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewCompat.setTranslationZ(mRecyclerView, -1);
            }
        });
        animator.setDuration(500);
        animator.start();
    }

    // refresh other visible cards' positions when dragging
    private void refreshOtherVisibleCardsPosition(float offset_x, float offset_y) {
        float factor = (float) (Math.sqrt(offset_x * offset_x + offset_y * offset_y)
                / Math.sqrt(mDragThresholdX * mDragThresholdX + mDragThresholdY * mDragThresholdY));
        factor = Math.min(factor, 1);
        if (getItemCount() > 1) {
            int cardOffset = mCardOffset;
            // count of all the attached cards
            int maxAttachChildrenCount = Math.min(mRecyclerView.getVisibleCardCount(), getItemCount() - 1);
            // count of all the cards required refreshing
            int totalRefreshingCount = Math.min(getItemCount() - 1, mRecyclerView.getVisibleCardCount());
            for (int i = 1; i <= totalRefreshingCount; i++) {
                int childPosition = maxAttachChildrenCount - i;
                // trans x y. if the visible cards count is three, for example,
                // here we only need to handle translation x and y for the other two visible cards at position 1 and 2
                if (i < totalRefreshingCount) {
                    getChildAt(childPosition).setTranslationX(-cardOffset * factor);
                    getChildAt(childPosition).setTranslationY(cardOffset * factor);
                }

                // it is different for handling transZ compared to transX Y.
                // we just need to handle more than one card.

                // calculate the current card ori elevation
                int current = mRecyclerView.getVisibleCardCount() - i;
                int oriElevation = mCardElevation * current;
                // update
                int currentElevation = (int) (oriElevation + mCardElevation * factor);
                ViewCompat.setTranslationZ(getChildAt(childPosition), currentElevation);
            }
        }
    }

    private void dispatchOnDragEvent(View view, boolean isDragging, boolean isDropped,
                                     float offsetX, float offsetY) {
        if (null != mListener) {
            mListener.onDraggingStateChanged(view, isDragging, isDropped, offsetX, offsetY);
            if (isDragging)
                mListener.onCardDragging(view, offsetX, offsetY);
        }
    }
}

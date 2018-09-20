# RecyclerCardStackView

这是一个用Recyclerview实现的模仿探探的CardStackView。

目前有两种card组织形式。

![image](https://github.com/devilist/RecyclerCardStackView/raw/master/images/image.gif)

# 调用方法

和正常使用recyclerview步骤一样：

'''

    CardRecyclerView crv_list = findViewById(R.id.crv_list);
    RCSAdapter adapter = new RCSAdapter(this);
    adapter.addData(appInfolist);
    crv_list.setAdapter(adapter);
    TowerCardManager towerCardManager = new TowerCardManager(this, crv_list);
    towerCardManager.setCardOffset(25);                // 设置card和card之间的偏移
    towerCardManager.setCardElevation(50);             // 设置card和card之间的elevation
    towerCardManager.setOnCardDragListener(this);      //  card拖动监听
    crv_list.setLayoutManager(towerCardManager);
        
'''


提供了手动drop card的方法：

'''

    public void dropCardNoTouch(int orientation) // orientation > 0 向右丢弃，反之向左丢弃


'''

拖拽card的监听：CardManager.OnCardDragListener

'''

    @Override
    public void onDraggingStateChanged(View view, boolean isDragging, boolean isDropped, float offsetX, float offsetY) {
        // 这个方法监听card状态变化
        isDragging: 当前card是否正在拖拽中（MotionEvent.ACTION_MOVE）
        isDropped:  当前card是否已经被丢弃
        offsetX，offsetY: 一次完整的拖动释放生命周期过程中card的偏移
        
        isDragging = false && isDropped == false 代表card处于释放状态
    }

    @Override
    public void onCardDragging(View view, float offsetX, float offsetY) {
        // 这个方法监听card拖动过程中的偏移 isDragging = true

    }


'''

布局中使用：

'''

        <com.devilist.cardstackview.CardRecyclerView
            android:id="@+id/crv_list"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

'''


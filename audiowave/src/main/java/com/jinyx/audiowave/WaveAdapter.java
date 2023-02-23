package com.jinyx.audiowave;

import android.graphics.Color;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.jinyx.audiowave.charting.charts.LineChart;
import com.jinyx.audiowave.charting.components.YAxis;
import com.jinyx.audiowave.charting.data.Entry;
import com.jinyx.audiowave.charting.data.LineData;
import com.jinyx.audiowave.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

class WaveAdapter extends BaseQuickAdapter<WaveItemModel, BaseViewHolder> {
    private final float DEFAULT_SCALE = 0F;
    private final float DEFAULT_MAX_VALUE = -1F;

    private final int mScreenWidth;
    private final boolean mWaveAutoScale;
    private final int maxWaveScaleValue;
    private final int mMaxWaveCount;
    private final int mLineColor;

    private int mMaxHeight = 0;

    private float mScale = DEFAULT_SCALE;
    private float mMaxValue = DEFAULT_MAX_VALUE;

    protected WaveAdapter(int screenWidth, boolean autoScale, int maxWaveScaleValue, int waveCount, int waveColor) {
        super(R.layout.item_wave);
        mScreenWidth = screenWidth;
        this.mWaveAutoScale = autoScale;
        this.maxWaveScaleValue = maxWaveScaleValue;
        this.mLineColor = waveColor;
        this.mMaxWaveCount = waveCount;
    }

    @Override
    public void setNewData(@Nullable List<WaveItemModel> data) {
        super.setNewData(data);
        mScale = DEFAULT_SCALE;
        mMaxValue = DEFAULT_MAX_VALUE;
        if (getRecyclerView() != null) {
            disableLoadMoreIfNotFullPage();
        }
    }

    private int lastCount = 0;

    /**
     * 每一组波形数量 + 1 时触发，需要判断其最大值是不是变了，变了刷新
     */
    @Override
    public void setData(int index, @NonNull WaveItemModel data) {
        LinearLayoutManager layoutManager = ((LinearLayoutManager)getRecyclerView().getLayoutManager());
        int firstItem = layoutManager.findFirstVisibleItemPosition();
        int lastItem = layoutManager.findLastVisibleItemPosition();
        if (index != lastCount) {
            lastCount = index;
            notifyItemInserted(index);  // 正常 前面 mAdapter.addData() 应该会执行他，不懂，直接调用 setData() 会崩溃
        } else {
            // 用于刷新屏幕中课件的 item，避免滚动时波形断开接不上；是滚动效果不符合预期
            notifyItemRangeChanged(firstItem, lastItem - firstItem);
        }
        // 拿到添加的最后一个波长数据
        float waveValue = data.mCharList.get(data.mCharList.size() - 1);
        if (waveValue > mMaxValue) {
            if (mMaxHeight > 0) {
                mMaxValue = mWaveAutoScale ? waveValue : maxWaveScaleValue * 0.5F;
                reSetScale(mMaxValue);
                notifyDataSetChanged();
            } else {
                mMaxHeight = getRecyclerView().getHeight() / 2;
                if (mMaxHeight > 0) {
                    mMaxValue = mWaveAutoScale ? waveValue : maxWaveScaleValue * 0.5F;
                    reSetScale(mMaxValue);
                    notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void convert(BaseViewHolder helper, WaveItemModel model) {
        LineChart chartWave = helper.itemView.findViewById(R.id.chartWave);
//        LineChart chartWave = (LineChart) helper.itemView;
        setChart(chartWave);
        List<Entry> entryList = new ArrayList<>();
        for (int index = 0; index < model.mCharList.size(); index++) {
            float value = model.mCharList.get(index);
            float yValue = value / mScale > 0 ? value / mScale : 0F;
            entryList.add(new Entry(index, yValue));
            entryList.add(new Entry(index + 0.5F, -yValue));
        }
        setChartData(entryList, chartWave, model);
    }

    protected boolean needToNext() {
        if (getData().isEmpty()) {
            return false;
        }
        WaveItemModel lastItem = getData().get(getData().size() - 1);
        float screenCount = 2F;    // 一个 item 绘制多少个屏幕宽度
        return lastItem.mCharList.size() >= screenCount * mMaxWaveCount && lastItem.mWaveWidth > screenCount * mScreenWidth;
    }

    private void reSetScale(float value) {
        float scale = value / mMaxHeight;
        if (scale > mScale) {
            mScale = scale;
        }
    }

    private void setChart(LineChart chartWave) {
        chartWave.getDescription().setEnabled(false);   // 不显示描述文字
        chartWave.setTouchEnabled(false);   // 禁用 touch 事件（不能拖拽，不能缩放）
        chartWave.setDrawGridBackground(false);  // 禁用平移
        chartWave.setDoubleTapToZoomEnabled(false);  // 禁止双击缩放
        chartWave.setHighlightPerDragEnabled(false);
        chartWave.setBackgroundColor(Color.TRANSPARENT);
        chartWave.setViewPortOffsets(0f, 0f, 0f, 0f);
        chartWave.getLegend().setEnabled(false);

        // 不要轴线（后续可能需要绘制 x 轴线，但是 ItemView 之间刻度值如何连贯的过渡是问题）
        chartWave.getXAxis().setEnabled(false);
        chartWave.getAxisLeft().setAxisMaximum(mMaxHeight);
        chartWave.getAxisLeft().setAxisMinimum(-mMaxHeight);
        chartWave.getAxisLeft().setEnabled(false);
        chartWave.getAxisRight().setEnabled(false);
    }

    private synchronized void setChartData(List<Entry> chartList, LineChart chartWave, WaveItemModel model) {
        LineDataSet lineDataSet = new LineDataSet(chartList, "");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setColor(mLineColor);
        lineDataSet.setValueTextColor(mLineColor);
        lineDataSet.setLineWidth(1.0f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setFillAlpha(25);
        lineDataSet.setFillColor(mLineColor);
        lineDataSet.setHighLightColor(mLineColor);
        lineDataSet.setDrawCircleHole(false);

        LineData lineData = new LineData(lineDataSet);
        lineData.setValueTextColor(mLineColor);
        lineData.setValueTextSize(9f);
        lineData.setDrawValues(false);

        chartWave.setData(lineData);
        model.mWaveWidth = chartWave.getData().getEntryCount() * mScreenWidth / mMaxWaveCount / 2;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(model.mWaveWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        chartWave.setLayoutParams(layoutParams);

        chartWave.invalidate();
    }

}

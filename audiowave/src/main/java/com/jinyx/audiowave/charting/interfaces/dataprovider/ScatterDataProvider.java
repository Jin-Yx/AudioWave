package com.jinyx.audiowave.charting.interfaces.dataprovider;

import com.jinyx.audiowave.charting.data.ScatterData;

public interface ScatterDataProvider extends BarLineScatterCandleBubbleDataProvider {

    ScatterData getScatterData();
}

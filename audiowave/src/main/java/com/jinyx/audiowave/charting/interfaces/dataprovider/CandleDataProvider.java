package com.jinyx.audiowave.charting.interfaces.dataprovider;

import com.jinyx.audiowave.charting.data.CandleData;

public interface CandleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    CandleData getCandleData();
}

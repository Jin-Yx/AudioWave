package com.jinyx.audiowave.charting.interfaces.dataprovider;

import com.jinyx.audiowave.charting.data.BubbleData;

public interface BubbleDataProvider extends BarLineScatterCandleBubbleDataProvider {

    BubbleData getBubbleData();
}

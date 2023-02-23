package com.jinyx.audiowave.charting.interfaces.dataprovider;

import com.jinyx.audiowave.charting.components.YAxis.AxisDependency;
import com.jinyx.audiowave.charting.data.BarLineScatterCandleBubbleData;
import com.jinyx.audiowave.charting.utils.Transformer;

public interface BarLineScatterCandleBubbleDataProvider extends ChartInterface {

    Transformer getTransformer(AxisDependency axis);
    boolean isInverted(AxisDependency axis);
    
    float getLowestVisibleX();
    float getHighestVisibleX();

    BarLineScatterCandleBubbleData getData();
}

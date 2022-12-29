package com.moko.bxp.button.d.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.moko.bxp.button.d.R;
import com.moko.support.d.entity.ExportData;

public class ExportDataListAdapter extends BaseQuickAdapter<ExportData, BaseViewHolder> {
    public ExportDataListAdapter() {
        super(R.layout.item_export_data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ExportData item) {
        helper.setText(R.id.tv_timestamp, item.timestamp);
        helper.setText(R.id.tv_trigger_mode, item.triggerMode);

    }
}

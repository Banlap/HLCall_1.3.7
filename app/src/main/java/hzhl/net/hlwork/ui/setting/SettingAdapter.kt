package hzhl.net.hlwork.ui.setting

import hzhl.net.hlcall.R
import hzhl.net.hlcall.utils.MyLog
import hzhl.net.hlwork.base.BaseAdapter
import kotlinx.android.synthetic.main.item_setting.*
import org.linphone.core.PayloadType

/**
 * @email   770138859@qq.com
 * @author: wen
 * @date:   2020/5/13
 * @time:   11:34
 */
class SettingAdapter : BaseAdapter<PayloadType>(R.layout.item_setting) {
    override fun onBind(t: PayloadType) {
        tv_name.text = t.mimeType
        switch_choose.isChecked = t.enabled()
        switch_choose.setOnCheckedChangeListener { _, isChecked ->
            t.enable(isChecked)
            onClick(t)
        }
    }
}
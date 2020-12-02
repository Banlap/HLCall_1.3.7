package hzhl.net.hlwork.untils
import android.app.Activity
import android.view.View
import android.widget.PopupWindow
import hzhl.net.hlcall.R
import kotlinx.android.extensions.LayoutContainer

/**
 * @email   770138859@qq.com
 * @author: wen
 * @date:   2020/4/17
 * @time:   9:06
 */

class PopUtil(private var activity : Activity) : PopupWindow.OnDismissListener,LayoutContainer{
    override var containerView: View? = null
    private var onDismiss: (()-> Unit)? = null
    var popupWindow: PopupWindow? = null

    fun inflaterLayout(layoutId: Int, width: Int, height: Int): PopUtil {
        //ActionBar.LayoutParams.WRAP_CONTENT,
        //ActionBar.LayoutParams.WRAP_CONTENT
        val vPopupWindow = activity.layoutInflater.inflate(layoutId, null, false) //引入弹窗布局
        popupWindow = PopupWindow(vPopupWindow, width, height, true)
        popupWindow?.animationStyle = R.style.PopupWindowAnimation
        return this
    }

    fun addBackground(): PopUtil { // 设置背景颜色变暗
        val lp = activity.window.attributes
        lp.alpha = 0.7f //调节透明度
        activity.window.attributes = lp
        //dismiss时恢复原样
        return this
    }

    fun create(onCreateView:(v:View)->Unit): PopUtil {
        popupWindow?.contentView?.let {
            containerView = it
            onCreateView(it)
        }
        return this
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
        onDismiss?.invoke()
        popupWindow = null
        onDismiss = null
    }

    fun onDismiss(onDismiss: ()-> Unit): PopUtil {
        this.onDismiss = onDismiss
        return this
    }

    fun show(parent: View, gravity: Int, x: Int, y: Int) {
        popupWindow?.showAtLocation(parent, gravity, x, y)
        popupWindow?.setOnDismissListener(this)
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

}
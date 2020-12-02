package hzhl.net.hlwork.ui.view

import android.app.ActionBar
import android.app.Activity
import android.util.ArrayMap
import android.view.Gravity
import hzhl.net.hlcall.R
import hzhl.net.hlcall.utils.MyLog
import hzhl.net.hlwork.untils.PopUtil
import kotlinx.android.synthetic.main.popup_bottom_menu.*
import kotlinx.android.synthetic.main.popup_bottom_menu_item.view.*

/**
 * @email   770138859@qq.com
 * @author: wen
 * @date:   2020/5/8
 * @time:   15:13
 */
class PopBottomMenu(val activity: Activity) {
    private val menuMap = arrayListOf<Menu>()

    fun show(){
        PopUtil(activity).apply {
            inflaterLayout(R.layout.popup_bottom_menu, ActionBar.LayoutParams.WRAP_CONTENT,ActionBar.LayoutParams.WRAP_CONTENT)
            create{
                ll_menu.apply {
                    menuMap.forEach {menu->
                        activity.layoutInflater
                                .inflate(R.layout.popup_bottom_menu_item,this,false)
                                .let {
                                    it.title.text = menu.name
                                    it.setOnClickListener {
                                        dismiss()
                                        menu.action(menu.name)
                                    }
                                    addView(it)
                                }
                    }
                }
                tv_dismiss.setOnClickListener { dismiss() }
            }
            addBackground()
            //containerView!!.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            show(activity.window.decorView, Gravity.BOTTOM, 0, 0)
        }

    }
    fun addMenu(name:String,action:(name:String)->Unit) = menuMap.add(Menu(name, action))
    fun addMenu(menus:List<Menu>) = menuMap.addAll(menus)
    class Menu(val name: String,val action: (name: String) -> Unit)
}


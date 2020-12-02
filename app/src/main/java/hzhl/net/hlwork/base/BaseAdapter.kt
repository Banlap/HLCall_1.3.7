package hzhl.net.hlwork.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*


abstract class BaseAdapter<T>(private val rid:Int,list: List<T> = arrayListOf())
    : RecyclerView.Adapter<ViewHolder>() ,LayoutContainer{
    var list = arrayListOf<T>()
        set(value) {
            field.apply {
                clear()
                addAll(value)
                notifyDataSetChanged()
            }
        }
    init {
        this.list.apply {
            clear()
            addAll(list)
        }
    }

    var onClick:((t:T)-> Unit) = {}
    var onLongClick:((t:T)-> Unit) = {}
    override var containerView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(rid,parent,false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        containerView = holder.itemView
        onBind(list[position])
        onBind(list[position],holder.adapterPosition)
        clearFindViewByIdCache()
    }


    abstract fun onBind(t: T)
    open fun onBind(t: T,position: Int){

    }

    override fun getItemCount() = list.size

    fun addData(list: List<T>){
        this.list.addAll(list)
        notifyDataSetChanged()
    }
    fun addData(t: T):Int{
        this.list.add(t)
        notifyItemChanged(itemCount -1)
        return itemCount - 1
    }
    fun setData(list: List<T>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

}
class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

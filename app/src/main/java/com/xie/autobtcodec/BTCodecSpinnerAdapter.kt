package com.xie.autobtcodec

import android.bluetooth.BluetoothCodecConfig
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.Collections
import java.util.Comparator
import java.util.stream.Collectors

/**
 * @Author XIE
 * @Date 2024/11/11
 * @Description
 */
class BTCodecSpinnerAdapter : BaseAdapter() {
    private val listInfo by lazy { ArrayList<BluetoothCodecConfig>() }

    fun setItems(itemInfos: List<BluetoothCodecConfig>) {
        listInfo.clear()
        listInfo.addAll(itemInfos)
        listInfo.sortWith { o1, o2 -> o2.codecType - o1.codecType }
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return listInfo.size
    }

    override fun getItem(position: Int): Any {
        return listInfo[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val context = parent.context
        val resultView: View
        if (convertView == null) {
            resultView = LayoutInflater.from(context).inflate(R.layout.item_spinner, null)
            holder = ViewHolder(resultView)
            resultView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            resultView = convertView
        }
        val itemInfo = listInfo[position]
        holder.textView.text = Tools.getCodecName(itemInfo.codecType)
        return resultView
    }

    class ViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.tv_content)
    }
}
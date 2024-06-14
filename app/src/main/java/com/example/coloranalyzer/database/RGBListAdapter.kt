package com.example.coloranalyzer.database

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coloranalyzer.R
import java.text.SimpleDateFormat
import java.util.Locale

class RGBListAdapter: ListAdapter<RGB, RGBListAdapter.RGBViewHolder>(RGB_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RGBViewHolder {
        return RGBViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RGBViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    // ViewHolder
    class RGBViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val item : TextView = itemView.findViewById(R.id.item)
        private val averageColor: ImageView = itemView.findViewById(R.id.color_hint)

        @SuppressLint("SetTextI18n")
        fun bind(rgb: RGB){

            // show the first 3 decimals
            val red = String.format("%.3f", rgb.data.red())
            val green = String.format("%.3f", rgb.data.green())
            val blue = String.format("%.3f", rgb.data.blue())

            // set the text
            item.text =
                "Average Red:   $red\n" +
                "Average Green:   $green\n" +
                "Average Blue:   $blue\n" +
                SimpleDateFormat(DATA_FORMAT,Locale.ITALIAN).format(rgb.timestamp)

            // set the average color in a square block
            averageColor.setBackgroundColor(rgb.data.toArgb())
        }


        companion object {

            const val DATA_FORMAT = "dd-MM-yyyy   HH:mm:ss"
            fun create(parent: ViewGroup): RGBViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return RGBViewHolder(view)
            }
        }

    }

    companion object {
        val RGB_COMPARATOR = object : DiffUtil.ItemCallback<RGB>() {

            override fun areContentsTheSame(oldItem: RGB, newItem: RGB): Boolean {
                return oldItem.timestamp == newItem.timestamp
            }

            override fun areItemsTheSame(oldItem: RGB, newItem: RGB): Boolean {
                return oldItem.timestamp == newItem.timestamp && oldItem.data == newItem.data
            }
        }
    }
}
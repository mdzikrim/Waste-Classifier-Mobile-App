package com.example.tes.ui.profil

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tes.R
import com.example.tes.data.SampahEntity
import java.io.File

class HistoryAdapter(private var items: List<SampahEntity>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun submitList(data: List<SampahEntity>) {
        items = data
        notifyDataSetChanged()
    }

    // ViewHolder untuk setiap item dalam RecyclerView
    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageThumb: ImageView = view.findViewById(R.id.imageThumb)
        val labelLevelView: TextView = view.findViewById(R.id.labelLevel2)
        val timestampView: TextView = view.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.labelLevelView.text = item.labelLevel2
        holder.timestampView.text = item.timestamp

        // Load foto asli dari imageUri, fallback ke gambar default jika gagal
        if (!item.imageUri.isNullOrEmpty()) {
            val imgFile = File(item.imageUri)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                holder.imageThumb.setImageBitmap(bitmap)
            } else {
                holder.imageThumb.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            holder.imageThumb.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    override fun getItemCount(): Int = items.size
}

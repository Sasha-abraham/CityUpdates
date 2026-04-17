package com.sasha.cityupdates.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sasha.cityupdates.R
import com.sasha.cityupdates.models.Update

class UpdateAdapter(private val updates: MutableList<Update>) :
    RecyclerView.Adapter<UpdateAdapter.UpdateViewHolder>() {

    class UpdateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvArea: TextView = itemView.findViewById(R.id.tvArea)
        val tvPostedBy: TextView = itemView.findViewById(R.id.tvPostedBy)
    }

    private var onItemClick: ((Update) -> Unit)? = null

    fun setOnItemClickListener(listener: (Update) -> Unit) {
        onItemClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_update, parent, false)
        return UpdateViewHolder(view)
    }

    override fun onBindViewHolder(holder: UpdateViewHolder, position: Int) {
        val update = updates[position]
        holder.tvCategory.text = update.category
        holder.tvTitle.text = update.title
        holder.tvDescription.text = update.description
        holder.tvArea.text = "📍 ${update.area}"
        holder.tvPostedBy.text = "by ${update.postedBy}"
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(update)
        }
    }

    override fun getItemCount() = updates.size

    fun setUpdates(newUpdates: List<Update>) {
        updates.clear()
        updates.addAll(newUpdates)
        notifyDataSetChanged()
    }
}
package com.sasha.cityupdates.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sasha.cityupdates.R
import com.sasha.cityupdates.models.Update

class UpdateAdapter(private val updates: MutableList<Update>) :
    RecyclerView.Adapter<UpdateAdapter.UpdateViewHolder>() {

    private var onItemClick: ((Update) -> Unit)? = null

    fun setOnItemClickListener(listener: (Update) -> Unit) {
        onItemClick = listener
    }

    class UpdateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvUrgency: TextView = itemView.findViewById(R.id.tvUrgency)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvArea: TextView = itemView.findViewById(R.id.tvArea)
        val tvPostedBy: TextView = itemView.findViewById(R.id.tvPostedBy)
        val tvFlagCount: TextView = itemView.findViewById(R.id.tvFlagCount)
        val tvUpvoteCount: TextView = itemView.findViewById(R.id.tvUpvoteCount)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tvCommentCount)
        val tvResolved: TextView = itemView.findViewById(R.id.tvResolved)
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
        holder.tvUpvoteCount.text = "👍 ${update.upvoteCount}"
        holder.tvCommentCount.text = "💬 ${update.commentCount}"

        // Resolved badge
        holder.tvResolved.visibility = if (update.isResolved) View.VISIBLE else View.GONE

        // Urgency color
        val (urgencyColor, urgencyLabel) = when (update.urgency) {
            "Critical" -> Pair("#F44336", "🔴 Critical")
            "High"     -> Pair("#FF9800", "🟠 High")
            "Medium"   -> Pair("#FFC107", "🟡 Medium")
            else       -> Pair("#4CAF50", "🟢 Low")
        }
        holder.tvUrgency.text = urgencyLabel
        holder.tvUrgency.setBackgroundColor(Color.parseColor(urgencyColor))

        // Flag count
        if (update.flagCount > 0) {
            holder.tvFlagCount.text = "🚩 ${update.flagCount}"
            holder.tvFlagCount.visibility = View.VISIBLE
        } else {
            holder.tvFlagCount.visibility = View.GONE
        }

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
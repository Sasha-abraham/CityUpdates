package com.sasha.cityupdates.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sasha.cityupdates.R
import com.sasha.cityupdates.models.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(private val comments: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
        val tvCommentBy: TextView = itemView.findViewById(R.id.tvCommentBy)
        val tvCommentTime: TextView = itemView.findViewById(R.id.tvCommentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvCommentText.text = comment.text
        holder.tvCommentBy.text = "👤 ${comment.postedBy}"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.tvCommentTime.text = sdf.format(Date(comment.timestamp))
    }

    override fun getItemCount() = comments.size

    fun setComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }
}
package com.hereliesaz.julesapisdk.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// *** MODIFIED: Inherit from ListAdapter, remove 'logs' from constructor ***
class LogcatAdapter : ListAdapter<String, LogcatAdapter.LogcatViewHolder>(LogDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogcatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.logcat_item, parent, false)
        return LogcatViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogcatViewHolder, position: Int) {
        // *** MODIFIED: Get item from ListAdapter's internal list ***
        val log = getItem(position)
        holder.bind(log)
    }

    // *** REMOVED: getItemCount() is handled by ListAdapter ***

    class LogcatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(log: String) {
            textView.text = log
            // This is where we set this, so it only applies to logs
            textView.setTextIsSelectable(true)
        }
    }

    // *** ADDED: Required DiffUtil for ListAdapter ***
    object LogDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            // Since logs are strings, we can just check content.
            // If they were objects, we'd check a unique ID.
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
package com.hereliesaz.julesapisdk.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hereliesaz.julesapisdk.PartialSession

class SessionsAdapter(
    private val onSessionClicked: (PartialSession) -> Unit
) : ListAdapter<PartialSession, SessionsAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = getItem(position)
        holder.bind(session, onSessionClicked)
    }

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val nameTextView: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(session: PartialSession, onSessionClicked: (PartialSession) -> Unit) {
            // *** MODIFIED: We only have 'name' from PartialSession ***
            titleTextView.text = session.name.substringAfterLast('/') // Show ID as title
            nameTextView.text = session.name // Show full path as subtitle

            itemView.setOnClickListener {
                onSessionClicked(session)
            }
        }
    }

    private class SessionDiffCallback : DiffUtil.ItemCallback<PartialSession>() {
        override fun areItemsTheSame(oldItem: PartialSession, newItem: PartialSession): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: PartialSession, newItem: PartialSession): Boolean {
            return oldItem == newItem
        }
    }
}
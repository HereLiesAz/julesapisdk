package com.hereliesaz.julesapisdk.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hereliesaz.julesapisdk.Session

class SessionsAdapter(private val onItemClicked: (Session) -> Unit) :
    ListAdapter<Session, SessionsAdapter.SessionViewHolder>(SessionDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        // Use the custom logcat_item layout for white text
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.logcat_item, parent, false)
        return SessionViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = getItem(position)
        holder.bind(session)
    }

    class SessionViewHolder(itemView: View, private val onItemClicked: (Session) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        // Use the ID from logcat_item.xml
        private val textView: TextView = itemView.findViewById(android.R.id.text1)
        private var currentSession: Session? = null

        init {
            itemView.setOnClickListener {
                currentSession?.let { onItemClicked(it) }
            }
        }

        fun bind(session: Session) {
            currentSession = session
            // Display a human-readable summary
            val shortName = session.name.split('/').lastOrNull() ?: session.name

            // Handle the nullable state field
            val stateText = session.state ?: "UNKNOWN"
            val displayText = "$shortName ($stateText)"

            textView.text = displayText
        }
    }

    object SessionDiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem == newItem
        }
    }
}
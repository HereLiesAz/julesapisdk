package com.hereliesaz.julesapisdk.testapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hereliesaz.julesapisdk.GithubRepoSource
import com.hereliesaz.julesapisdk.Source

class SourcesAdapter(
    private val onSourceSelected: (Source) -> Unit
) : ListAdapter<Source, SourcesAdapter.SourceViewHolder>(SourceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        // *** MODIFIED: Use logcat_item for white text and consistency ***
        val view = LayoutInflater.from(parent.context).inflate(R.layout.logcat_item, parent, false)
        return SourceViewHolder(view, onSourceSelected)
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        val source = getItem(position)
        holder.bind(source)
    }

    class SourceViewHolder(itemView: View, private val onSourceSelected: (Source) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        // *** MODIFIED: Use the ID from logcat_item.xml ***
        private val textView: TextView = itemView.findViewById(android.R.id.text1)
        private var currentSource: Source? = null

        init {
            itemView.setOnClickListener {
                currentSource?.let { onSourceSelected(it) }
            }
        }

        fun bind(source: Source) {
            currentSource = source
            // Display a more useful name
            if (source is GithubRepoSource) {
                textView.text = "${source.githubRepo.owner}/${source.githubRepo.repo}"
            } else {
                textView.text = source.name
            }
        }
    }

    class SourceDiffCallback : DiffUtil.ItemCallback<Source>() {
        override fun areItemsTheSame(oldItem: Source, newItem: Source): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Source, newItem: Source): Boolean {
            return oldItem == newItem
        }
    }
}
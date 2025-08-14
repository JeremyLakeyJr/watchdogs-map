package com.jeremylakeyjr.watchdogsmap

import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchResultsAdapter(private val onSearchResultClicked: (Address) -> Unit) :
    RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private var results: List<Address> = emptyList()

    fun setResults(results: List<Address>) {
        this.results = results
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.textView.text = result.getAddressLine(0)
        holder.itemView.setOnClickListener { onSearchResultClicked(result) }
    }

    override fun getItemCount(): Int {
        return results.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.search_result_text)
    }
}
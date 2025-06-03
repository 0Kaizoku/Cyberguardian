package com.cyberguardianapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cyberguardianapp.model.AppInfo

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {
    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName)
        val riskLevel: TextView = view.findViewById(R.id.riskLevel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.appName.text = app.appName
        val riskDisplay = if (app.riskLevel.displayName == "Safe") "Safe" else app.riskLevel.displayName
        holder.riskLevel.text = riskDisplay
        holder.itemView.setOnClickListener { onAppClick(app) }
    }

    override fun getItemCount() = apps.size
} 
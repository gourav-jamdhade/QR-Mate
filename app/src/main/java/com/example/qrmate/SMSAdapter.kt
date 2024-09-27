package com.example.qrmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SMSAdapter(private val smsList: List<String>, private val listener: OnSmsClickListener) :
    RecyclerView.Adapter<SMSAdapter.SmsViewHolder>() {

    inner class SmsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val smsTextView: TextView = view.findViewById(R.id.tvSmsMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_list_item, parent, false)
        return SmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {
        val smsMessage = smsList[position]
        holder.smsTextView.text = smsMessage

        holder.itemView.setOnClickListener {
            listener.onSmsClick(smsMessage)
        }
    }

    override fun getItemCount(): Int {
        return smsList.size
    }

    interface OnSmsClickListener {
        fun onSmsClick(smsMessage: String)
    }
}

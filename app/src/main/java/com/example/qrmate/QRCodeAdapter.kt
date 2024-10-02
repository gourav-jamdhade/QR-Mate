package com.example.qrmate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QRCodeAdapter(private val qrCodeList: List<QRCode>) : RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder>() {

    class QRCodeViewHolder(view: View):RecyclerView.ViewHolder(view){
        val qrImage:ImageView = view.findViewById(R.id.ivQRImage)
        val qrName:TextView = view.findViewById(R.id.tvQRName)
        //val options:ImageView = view.findViewById(R.id.ivOptions)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):QRCodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.qr_rv_item,parent,false)
        return QRCodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: QRCodeAdapter.QRCodeViewHolder, position: Int) {
        val qrCode = qrCodeList[position]

        CoroutineScope(Dispatchers.Main).launch {
            holder.qrName.text = qrCode.name

            Glide.with(holder.qrImage.context).load(qrCode.imageUrl).into(holder.qrImage)
        }


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context,QRCodeActivity::class.java).apply{
                putExtra("QR_NAME",qrCode.name)
                putExtra("QR_IMAGE_URL",qrCode.imageUrl)
            }
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return qrCodeList.size
    }
}
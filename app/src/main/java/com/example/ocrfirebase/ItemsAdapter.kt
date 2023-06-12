package com.example.ocrfirebase

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ItemsAdapter(private var items: MutableList<Item>, private val context: Context, val clickListener: ItemClickListener):
    ListAdapter<Item, RecyclerView.ViewHolder>(ColumnDiffCallback()) {

    fun getContext() : Context {
        return context
    }

    fun updateList(modelList: MutableList<Item>){
        items = modelList
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = items[position]
                holder.itemView.setOnClickListener { clickListener.onClick(item) }
                holder.bind(item, position, ItemRemoveListener{
                    items.remove(it)
                    notifyDataSetChanged()
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val etProduct: EditText = itemView.findViewById(R.id.etProduct)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)
        private val etQtd: EditText = itemView.findViewById(R.id.etQuantity)
        private val spUm: Spinner = itemView.findViewById(R.id.spUm)
        private val ivRemove: ImageView = itemView.findViewById(R.id.ivRemove)
        private val llBackground: LinearLayout = itemView.findViewById(R.id.llBackground)

        fun bind(item: Item, position: Int, removeListener: ItemRemoveListener) {
            val context = itemView.context
            spUm.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, item.units)
            etProduct.setText(item.id)
            tvDesc.text = item.description
            etQtd.setText(item.qnt)
            ivRemove.setOnClickListener { removeListener.onRemove(item) }

            if (item.qnt == "0"){
                llBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.red ))
            } else {
                llBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent ))
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.list_item, parent, false)
                return ViewHolder(view)
            }
        }
    }
}

class ItemRemoveListener(val removeListener: (item: Item) -> Unit) {
    fun onRemove(item: Item) = removeListener(item)
}

class ItemClickListener(val clickListener: (item: Item) -> Unit) {
    fun onClick(item: Item) = clickListener(item)
}

class ColumnDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }
}

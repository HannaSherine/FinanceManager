package com.example.financemanager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanager.R
import com.example.financemanager.data.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    // Set in MainActivity to handle swipe-to-delete
    var onDelete: (Transaction) -> Unit = {}

    // Set in MainActivity to handle tap-to-edit
    var onEdit: (Transaction) -> Unit = {}

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private val categoryIcons = mapOf(
        "Food"      to "🍽️",
        "Travel"    to "🚌",
        "Shopping"  to "🛍️",
        "Health"    to "💊",
        "Salary"    to "💰",
        "Education" to "🎓",
        "Rent"      to "🏠",
        "Utilities" to "⚡",
        "income"    to "💰",
        "expense"   to "💳"
    )

    private val categoryColors = mapOf(
        "Food"      to 0xFFFEF3C7.toInt(),
        "Travel"    to 0xFFE0E7FF.toInt(),
        "Shopping"  to 0xFFFCE7F3.toInt(),
        "Health"    to 0xFFCFFAFE.toInt(),
        "Salary"    to 0xFFD1FAE5.toInt(),
        "Education" to 0xFFEDE9FE.toInt(),
        "Rent"      to 0xFFFFEDD5.toInt(),
        "Utilities" to 0xFFFEF9C3.toInt(),
        "income"    to 0xFFD1FAE5.toInt(),
        "expense"   to 0xFFFEE2E2.toInt()
    )

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView     = itemView.findViewById(R.id.tvCategoryIcon)
        val title: TextView    = itemView.findViewById(R.id.tvTitle)
        val date: TextView     = itemView.findViewById(R.id.tvDate)
        val amount: TextView   = itemView.findViewById(R.id.tvAmount)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        val ctx = holder.itemView.context

        holder.title.text    = transaction.description.ifEmpty { transaction.category }
        holder.date.text     = dateFormat.format(Date(transaction.date))
        holder.category.text = transaction.category

        val icon = categoryIcons[transaction.category]
            ?: if (transaction.type == "income") "💰" else "💳"
        holder.icon.text = icon

        val bgColor = categoryColors[transaction.category]
            ?: if (transaction.type == "income") 0xFFD1FAE5.toInt() else 0xFFFEE2E2.toInt()
        holder.icon.setBackgroundColor(bgColor)

        if (transaction.type == "income") {
            holder.amount.text = "+ ₹${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
            holder.amount.setTextColor(ctx.getColor(R.color.income_green))
        } else {
            holder.amount.text = "- ₹${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
            holder.amount.setTextColor(ctx.getColor(R.color.expense_red))
        }

        // Tap item to edit
        holder.itemView.setOnClickListener {
            onEdit(transaction)
        }
    }

    // Called by MainActivity's ItemTouchHelper on left swipe
    fun deleteItemAt(position: Int) {
        onDelete(getItem(position))
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) =
            oldItem == newItem
    }
}
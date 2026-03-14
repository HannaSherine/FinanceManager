package com.example.financemanager

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanager.adapter.TransactionAdapter
import com.example.financemanager.database.AppDataBase
import com.example.financemanager.repository.TransactionRepository
import com.example.financemanager.viewmodel.TransactionViewModel
import com.example.financemanager.viewmodel.TransactionViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBalance    = findViewById(R.id.tvBalance)
        tvIncome     = findViewById(R.id.tvIncome)
        tvExpense    = findViewById(R.id.tvExpense)
        recyclerView = findViewById(R.id.rvTransactions)
        val fabAdd   = findViewById<FloatingActionButton>(R.id.btnAddTransaction)

        val database   = AppDataBase.getDataBase(this)
        val repository = TransactionRepository(database.transactionDao())
        val factory    = TransactionViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        adapter = TransactionAdapter()

        // Tap to edit — pass transaction id to EditTransactionActivity
        adapter.onEdit = { transaction ->
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_ID",          transaction.id)
            intent.putExtra("TRANSACTION_AMOUNT",      transaction.amount)
            intent.putExtra("TRANSACTION_DESCRIPTION", transaction.description)
            intent.putExtra("TRANSACTION_CATEGORY",    transaction.category)
            intent.putExtra("TRANSACTION_TYPE",        transaction.type)
            intent.putExtra("TRANSACTION_DATE",        transaction.date)
            startActivity(intent)
        }

        // Swipe left to delete with undo
        adapter.onDelete = { transaction ->
            viewModel.deleteTransaction(transaction)
            Snackbar.make(recyclerView, "Transaction deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO") { viewModel.addTransaction(transaction) }
                .setActionTextColor(Color.parseColor("#6EE7B7"))
                .setBackgroundTint(Color.parseColor("#1A1A2E"))
                .setTextColor(Color.WHITE)
                .show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Swipe-to-delete gesture
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private val bgPaint   = Paint().apply { color = Color.parseColor("#DC2626") }
            private val textPaint = Paint().apply {
                color       = Color.WHITE
                textSize    = 36f
                isAntiAlias = true
            }

            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.deleteItemAt(viewHolder.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val bg = RectF(
                    itemView.right + dX, itemView.top.toFloat() + 4,
                    itemView.right.toFloat(), itemView.bottom.toFloat() - 4
                )
                c.drawRoundRect(bg, 24f, 24f, bgPaint)
                val label = "Delete"
                val textX = itemView.right - textPaint.measureText(label) - 48f
                val textY = itemView.top + (itemView.height / 2f) + (textPaint.textSize / 3)
                c.drawText(label, textX, textY, textPaint)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allTransactions.collectLatest { adapter.submitList(it) }
                }
                launch {
                    viewModel.totalIncome.collect { tvIncome.text = formatAmount(it) }
                }
                launch {
                    viewModel.totalExpense.collect { tvExpense.text = formatAmount(it) }
                }
                launch {
                    combine(viewModel.totalIncome, viewModel.totalExpense) { inc: Double?, exp: Double? ->
                        (inc ?: 0.0) - (exp ?: 0.0)
                    }.collect { tvBalance.text = formatAmount(it) }
                }
            }
        }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }

    private fun formatAmount(value: Double?): String =
        String.format(Locale.getDefault(), "₹%.2f", value ?: 0.0)
}

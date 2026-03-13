package com.example.financemanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanager.adapter.TransactionAdapter
import com.example.financemanager.database.AppDataBase
import com.example.financemanager.repository.TransactionRepository
import com.example.financemanager.viewmodel.TransactionViewModel
import com.example.financemanager.viewmodel.TransactionViewModelFactory
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

        // Initialize Views
        tvBalance = findViewById(R.id.tvBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        recyclerView = findViewById(R.id.rvTransactions)
        val btnAdd = findViewById<Button>(R.id.btnAddTransaction)

        // Setup Repository and ViewModel
        val database = AppDataBase.getDataBase(this)
        val repository = TransactionRepository(database.transactionDao())
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        // Setup RecyclerView
        adapter = TransactionAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Lifecycle-aware collection
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Transactions
                launch {
                    viewModel.allTransactions.collectLatest { transactionList ->
                        adapter.submitList(transactionList)
                    }
                }

                // Collect Income
                launch {
                    viewModel.totalIncome.collect { income ->
                        tvIncome.text = String.format(Locale.getDefault(), "Income: ₹%.2f", income ?: 0.0)
                    }
                }

                // Collect Expense
                launch {
                    viewModel.totalExpense.collect { expense ->
                        tvExpense.text = String.format(Locale.getDefault(), "Expense: ₹%.2f", expense ?: 0.0)
                    }
                }

                // Collect and Calculate Balance
                launch {
                    combine(viewModel.totalIncome, viewModel.totalExpense) { income: Double?, expense: Double? ->
                        (income ?: 0.0) - (expense ?: 0.0)
                    }.collect { balance ->
                        tvBalance.text = String.format(Locale.getDefault(), "Balance: ₹%.2f", balance)
                    }
                }
            }
        }

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }
}

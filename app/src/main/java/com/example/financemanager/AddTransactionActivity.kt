package com.example.financemanager

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.financemanager.data.Transaction
import com.example.financemanager.database.AppDataBase
import com.example.financemanager.repository.TransactionRepository
import com.example.financemanager.viewmodel.TransactionViewModel
import com.example.financemanager.viewmodel.TransactionViewModelFactory

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var etCategory: EditText
    private lateinit var spType: Spinner
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        val database = AppDataBase.getDataBase(this)
        val dao = database.transactionDao()
        val repository = TransactionRepository(dao)
        val factory = TransactionViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        etCategory = findViewById(R.id.etCategory)
        spType = findViewById(R.id.spType)
        btnSave = findViewById(R.id.btnSave)

        val types = arrayOf("income", "expense")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        spType.adapter = adapter

        btnSave.setOnClickListener {

            val amount = etAmount.text.toString().toDouble()
            val description = etDescription.text.toString()
            val category = etCategory.text.toString()
            val type = spType.selectedItem.toString()

            val transaction = Transaction(
                amount = amount,
                description = description,
                category = category,
                type = type,
                date = System.currentTimeMillis()
            )

            viewModel.addTransaction(transaction)

            finish()
        }
    }
}
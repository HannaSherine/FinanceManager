package com.example.financemanager

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.financemanager.data.Transaction
import com.example.financemanager.database.AppDataBase
import com.example.financemanager.repository.TransactionRepository
import com.example.financemanager.viewmodel.TransactionViewModel
import com.example.financemanager.viewmodel.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var btnIncome: Button
    private lateinit var btnExpense: Button
    private lateinit var btnSave: Button

    private var transactionId: Int = 0
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private var selectedType: String = "income"
    private var selectedCategory: String = "Food"

    private val categoryMap = mapOf(
        R.id.catFood      to "Food",
        R.id.catTravel    to "Travel",
        R.id.catShopping  to "Shopping",
        R.id.catHealth    to "Health",
        R.id.catSalary    to "Salary",
        R.id.catEducation to "Education",
        R.id.catRent      to "Rent",
        R.id.catUtilities to "Utilities"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_transaction)

        val database   = AppDataBase.getDataBase(this)
        val repository = TransactionRepository(database.transactionDao())
        val factory    = TransactionViewModelFactory(repository)
        viewModel      = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        etAmount      = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        etDate        = findViewById(R.id.etDate)
        btnIncome     = findViewById(R.id.btnIncome)
        btnExpense    = findViewById(R.id.btnExpense)
        btnSave       = findViewById(R.id.btnSave)

        // ── Pre-fill from Intent extras ──────────────────────────────────────
        transactionId     = intent.getIntExtra("TRANSACTION_ID", 0)
        val amount        = intent.getDoubleExtra("TRANSACTION_AMOUNT", 0.0)
        val description   = intent.getStringExtra("TRANSACTION_DESCRIPTION") ?: ""
        val category      = intent.getStringExtra("TRANSACTION_CATEGORY") ?: "Food"
        val type          = intent.getStringExtra("TRANSACTION_TYPE") ?: "income"
        val date          = intent.getLongExtra("TRANSACTION_DATE", System.currentTimeMillis())

        etAmount.setText(amount.toString())
        etDescription.setText(description)
        selectedDateMillis = date
        etDate.setText(formatDate(selectedDateMillis))

        selectedType     = type
        selectedCategory = category

        setTypeSelected(type)
        setCategorySelected(categoryMap.entries.firstOrNull { it.value == category }?.key, category)

        // ── Listeners ────────────────────────────────────────────────────────
        etDate.setOnClickListener { showDatePicker() }
        btnIncome.setOnClickListener  { setTypeSelected("income") }
        btnExpense.setOnClickListener { setTypeSelected("expense") }

        categoryMap.forEach { (viewId, label) ->
            findViewById<LinearLayout>(viewId).setOnClickListener {
                setCategorySelected(viewId, label)
            }
        }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            if (amountText.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val parsedAmount = amountText.toDoubleOrNull()
            if (parsedAmount == null || parsedAmount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = Transaction(
                id          = transactionId,        // preserve original id for @Update
                amount      = parsedAmount,
                description = etDescription.text.toString().trim().ifEmpty { selectedCategory },
                category    = selectedCategory,
                type        = selectedType,
                date        = selectedDateMillis
            )

            viewModel.updateTransaction(updated)
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // ── Date picker ──────────────────────────────────────────────────────────

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedDateMillis = cal.timeInMillis
                etDate.setText(formatDate(selectedDateMillis))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(millis)

    // ── Type toggle ──────────────────────────────────────────────────────────

    private fun setTypeSelected(type: String) {
        selectedType = type
        if (type == "income") {
            btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_income_bg))
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.toggle_income_text))
            btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_inactive_bg))
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        } else {
            btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_expense_bg))
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.toggle_expense_text))
            btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_inactive_bg))
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        }
    }

    // ── Category grid ────────────────────────────────────────────────────────

    private fun setCategorySelected(selectedId: Int?, label: String) {
        selectedCategory = label
        categoryMap.keys.forEach { viewId ->
            val tile = findViewById<LinearLayout>(viewId)
            if (viewId == selectedId) {
                tile.setBackgroundColor(ContextCompat.getColor(this, R.color.cat_selected_bg))
                (tile.getChildAt(1) as? TextView)?.setTextColor(
                    ContextCompat.getColor(this, R.color.cat_selected_text)
                )
            } else {
                tile.setBackgroundColor(ContextCompat.getColor(this, R.color.cat_unselected_bg))
                (tile.getChildAt(1) as? TextView)?.setTextColor(
                    ContextCompat.getColor(this, R.color.cat_unselected_text)
                )
            }
        }
    }
}
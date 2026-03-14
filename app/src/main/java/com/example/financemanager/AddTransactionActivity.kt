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

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var viewModel: TransactionViewModel

    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var btnIncome: Button
    private lateinit var btnExpense: Button
    private lateinit var btnSave: Button

    // Holds the user-picked date as millis; defaults to today
    private var selectedDateMillis: Long = System.currentTimeMillis()

    // Track selected values
    private var selectedType: String = "income"
    private var selectedCategory: String = "Food"

    // Category tile IDs mapped to their labels
    private val categoryMap = mapOf(
        R.id.catFood       to "Food",
        R.id.catTravel     to "Travel",
        R.id.catShopping   to "Shopping",
        R.id.catHealth     to "Health",
        R.id.catSalary     to "Salary",
        R.id.catEducation  to "Education",
        R.id.catRent       to "Rent",
        R.id.catUtilities  to "Utilities"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_transaction)

        // ViewModel setup
        val database = AppDataBase.getDataBase(this)
        val repository = TransactionRepository(database.transactionDao())
        val factory = TransactionViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[TransactionViewModel::class.java]

        // Bind views
        etAmount      = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        etDate        = findViewById(R.id.etDate)
        btnIncome     = findViewById(R.id.btnIncome)
        btnExpense    = findViewById(R.id.btnExpense)
        btnSave       = findViewById(R.id.btnSave)

        // Show today's date immediately in the field
        etDate.setText(formatDate(selectedDateMillis))

        // Tap anywhere on the date field → open DatePickerDialog
        etDate.setOnClickListener { showDatePicker() }

        // Set initial type state
        setTypeSelected("income")

        // Type toggle listeners
        btnIncome.setOnClickListener  { setTypeSelected("income") }
        btnExpense.setOnClickListener { setTypeSelected("expense") }

        // Category tile listeners
        categoryMap.forEach { (viewId, label) ->
            findViewById<LinearLayout>(viewId).setOnClickListener {
                setCategorySelected(viewId, label)
            }
        }

        // Save
        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amountText.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                amount      = amount,
                description = description.ifEmpty { selectedCategory },
                category    = selectedCategory,
                type        = selectedType,
                date        = selectedDateMillis       // ← user-picked date
            )

            viewModel.addTransaction(transaction)
            finish()
        }
    }

    // ── Date picker ──────────────────────────────────────────────────────────

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
        }
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                selectedDateMillis = cal.timeInMillis
                etDate.setText(formatDate(selectedDateMillis))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Prevent selecting future dates
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun formatDate(millis: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(millis)

    // ── Type toggle ──────────────────────────────────────────────────────────

    private fun setTypeSelected(type: String) {
        selectedType = type
        if (type == "income") {
            // Income button — active (green)
            btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_income_bg))
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.toggle_income_text))
            // Expense button — inactive
            btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_inactive_bg))
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        } else {
            // Expense button — active (red)
            btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_expense_bg))
            btnExpense.setTextColor(ContextCompat.getColor(this, R.color.toggle_expense_text))
            // Income button — inactive
            btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.toggle_inactive_bg))
            btnIncome.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        }
    }

    // ── Category grid ────────────────────────────────────────────────────────

    private fun setCategorySelected(selectedId: Int, label: String) {
        selectedCategory = label
        categoryMap.keys.forEach { viewId ->
            val tile = findViewById<LinearLayout>(viewId)
            if (viewId == selectedId) {
                tile.setBackgroundColor(ContextCompat.getColor(this, R.color.cat_selected_bg))
                // Update the label TextView (second child) color
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
package com.example.financemanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanager.data.Transaction
import com.example.financemanager.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {
    val allTransactions: Flow<List<Transaction>> = repository.allTransaction

    val totalIncome: Flow<Double?> = repository.totalIncome

    val totalExpense: Flow<Double?> = repository.totalExpense

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
}
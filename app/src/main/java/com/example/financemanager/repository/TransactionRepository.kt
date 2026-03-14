package com.example.financemanager.repository

import com.example.financemanager.data.Transaction
import com.example.financemanager.database.TransactionDao
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransaction: Flow<List<Transaction>> =
        transactionDao.getAllTransactions()

    val totalIncome: Flow<Double?> =
        transactionDao.getTotalIncome()

    val totalExpense: Flow<Double?> =
        transactionDao.getTotalExpense()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
}
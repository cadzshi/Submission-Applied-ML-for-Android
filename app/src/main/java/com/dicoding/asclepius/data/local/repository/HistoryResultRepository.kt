package com.dicoding.asclepius.data.local.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.dicoding.asclepius.data.local.db.HistoryResult
import com.dicoding.asclepius.data.local.db.HistoryResultDao
import com.dicoding.asclepius.data.local.db.HistoryResultRoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HistoryResultRepository (application: Application){

    private val mHistoryResultDao: HistoryResultDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        val db= HistoryResultRoomDatabase.getDatabase(application)
        mHistoryResultDao = db.HistoryResultDao()
    }

    fun insert(historyResult: HistoryResult){
        executorService.execute {
            mHistoryResultDao.insert(historyResult)
        }
    }
    fun getAllHistoryResult(): LiveData<List<HistoryResult>> = mHistoryResultDao.getAllHistoryResult()

}
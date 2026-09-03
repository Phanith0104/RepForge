package com.repforge.data.repository

import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.entities.StepEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: StepDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayDate(): String = dateFormat.format(Date())

    fun getTodaySteps(): Flow<StepEntity?> {
        return stepDao.getLatestSteps() // Simplification: get latest entry
    }

    suspend fun updateSteps(newCount: Int) {
        val today = getTodayDate()
        val currentEntry = stepDao.getStepsForDate(today)
        
        val newStreak = if (newCount >= 10000) {
            // Logic for streak: if yesterday had 10k, increment.
            // For simplicity, let's just mark if today reached 10k.
            val yesterday = getYesterdayDate()
            val yesterdayEntry = stepDao.getStepsForDate(yesterday)
            if (yesterdayEntry != null && yesterdayEntry.count >= 10000) {
                yesterdayEntry.streak + 1
            } else {
                1
            }
        } else {
            0
        }

        stepDao.insertOrUpdateSteps(
            StepEntity(date = today, count = newCount, streak = newStreak)
        )
    }

    private fun getYesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return dateFormat.format(cal.time)
    }
}

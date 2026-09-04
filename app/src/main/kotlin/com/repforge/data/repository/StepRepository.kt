package com.repforge.data.repository

import android.content.Context
import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.entities.StepEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: StepDao,
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val prefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

    fun getTodayDate(): String = dateFormat.format(Date())

    fun getTodaySteps(): Flow<StepEntity?> {
        return stepDao.getLatestSteps()
    }

    suspend fun updateSteps(sensorSteps: Int) {
        val today = getTodayDate()
        val lastUpdateDate = prefs.getString("last_update_date", "")

        // Handle Daily Reset for Sensor Steps
        if (lastUpdateDate != today) {
            // New day detected. Store the current sensor value as the base for today.
            prefs.edit().apply {
                putInt("sensor_base_steps", sensorSteps)
                putString("last_update_date", today)
                apply()
            }
        }

        val baseSteps = prefs.getInt("sensor_base_steps", sensorSteps)
        val todayCount = (sensorSteps - baseSteps).coerceAtLeast(0)

        // Achievement Logic: Check if today's steps cross Personal Best
        val personalBest = stepDao.getPersonalBestExcludingToday(today) ?: 0
        val isAchievement = todayCount > personalBest && todayCount > 0

        // Streak Logic
        val newStreak = if (todayCount >= 10000) {
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
            StepEntity(
                date = today,
                count = todayCount,
                streak = newStreak,
                isAchievement = isAchievement
            )
        )
    }

    private fun getYesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return dateFormat.format(cal.time)
    }

    fun getAllStepHistory(): Flow<List<StepEntity>> = stepDao.getAllSteps()
}

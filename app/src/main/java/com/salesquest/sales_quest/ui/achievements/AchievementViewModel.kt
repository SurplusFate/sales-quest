package com.salesquest.sales_quest.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppAchievements
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.entity.AchievementEntity
import com.salesquest.sales_quest.services.AchievementStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 成就 ViewModel - 组合定义与解锁状态 */
class AchievementViewModel : ViewModel() {

    val statuses: StateFlow<List<AchievementStatus>> =
        AppContainer.db.achievementDao().watchAll().map { unlocked: List<AchievementEntity> ->
            val unlockedById = unlocked.associateBy { it.achievementId }
            AppAchievements.definitions.map { def ->
                val entity = unlockedById[def.id]
                AchievementStatus(
                    def = def,
                    unlocked = entity != null,
                    unlockedAt = entity?.unlockedAt
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

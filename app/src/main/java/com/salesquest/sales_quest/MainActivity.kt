package com.salesquest.sales_quest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.ui.navigation.SalesQuestApp
import com.salesquest.sales_quest.ui.theme.SalesQuestTheme
import com.salesquest.sales_quest.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.info("MainActivity", "onCreate")
        setContent {
            val mode by ThemeManager.mode.collectAsState()
            val theme by ThemeManager.theme.collectAsState()
            // 读取 mode/theme 触发重组, SalesQuestTheme 内部根据最新值选择配色
            SalesQuestTheme {
                SalesQuestApp()
            }
        }
    }
}

package com.salesquest.sales_quest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.ui.navigation.SalesQuestApp
import com.salesquest.sales_quest.ui.theme.SalesQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.info("MainActivity", "onCreate")
        setContent {
            SalesQuestTheme {
                SalesQuestApp()
            }
        }
    }
}

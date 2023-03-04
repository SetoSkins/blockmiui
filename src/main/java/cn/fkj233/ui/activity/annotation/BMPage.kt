package cn.fkj233.ui.activity.annotation

import androidx.annotation.Keep

@Keep
annotation class BMPage(
    val key: String,
    val title: String = "",
    val titleId: Int = 0,
    val hideMenu: Boolean = true
)

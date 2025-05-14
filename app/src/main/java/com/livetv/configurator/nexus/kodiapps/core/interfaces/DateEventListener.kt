package com.livetv.configurator.nexus.kodiapps.core.interfaces

import java.util.Date

interface DateEventListener {
    fun onDateSelected(date: Date, hourOfDay: Int, minute: Int)
}
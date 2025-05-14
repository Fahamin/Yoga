package com.livetv.configurator.nexus.kodiapps.core.interfaces

interface CallbackListener {

    fun onSuccess()

    fun onCancel()

    fun onRetry()
}
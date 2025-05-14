package com.livetv.configurator.nexus.kodiapps.core.interfaces

interface PermissionListener {

    fun onPermissionClick()

    fun onPermissionAllow(isAllow: Boolean)
}
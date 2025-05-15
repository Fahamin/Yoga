package com.livetv.configurator.nexus.kodiapps.core

open class BaseException() : Exception() {

    open lateinit var errMessage : String
    open lateinit var title : String


}
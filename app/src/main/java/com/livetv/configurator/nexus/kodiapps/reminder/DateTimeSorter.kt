package com.livetv.configurator.nexus.kodiapps.reminder

// Class to create DateTime objects for easy sorting
class DateTimeSorter {
    var index = 0
    var dateTime: String? = null

    constructor(index: Int, DateTime: String?) {
        this.index = index
        dateTime = DateTime
    }

    constructor() {}

}
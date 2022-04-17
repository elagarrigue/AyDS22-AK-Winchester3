package ayds.winchester.songinfo.home.view

import ayds.winchester.songinfo.home.model.entities.DatePrecision
import ayds.winchester.songinfo.home.model.entities.EmptySong
import ayds.winchester.songinfo.home.model.entities.Song

interface ResultReleaseDate {
    fun getDescription(song: Song = EmptySong): String
}

internal class ResultReleaseDateImpl() : ResultReleaseDate {

    override fun getDescription(song: Song): String {
        return when (song.releaseDatePrecision) {
            DatePrecision.YEAR -> getDescriptionByYear(song)
            DatePrecision.MONTH -> getDescriptionByMonth(song)
            DatePrecision.DAY -> getDescriptionByDay(song)
            else -> "Invalid Precision"
        }
    }

    private fun getDescriptionByYear(song: Song): String {
        var year = (song.releaseDate.split("-").first())
        return if (!isLeapYear(year.toInt())) {
            "$year (not a leap year)"
        } else "$year (leap year)"
    }

    private fun getDescriptionByMonth(song: Song): String {
        var year = song.releaseDate.split("-").first()
        var month = song.releaseDate.split("-").component2()
        var fecha = fromNumberToMonth(month)
        return "$fecha, $year"
    }

    private fun fromNumberToMonth(month: String): String {
        return when (month) {
            "01" ->  "January"
            "02" ->  "February"
            "03" ->  "March"
            "04" ->  "April"
            "05" ->  "May"
            "06" ->  "June"
            "07" ->  "July"
            "08" ->  "August"
            "09" ->  "September"
            "10" ->  "October"
            "11" ->  "November"
            "12" ->  "December"
            else -> "Invalid month"
        }
    }

    private fun getDescriptionByDay(song: Song): String {
        var day = song.releaseDate.split("-").component3()
        var month = song.releaseDate.split("-").component2()
        var year = song.releaseDate.split("-").first()
        return ("$day/$month/$year")
    }

    private fun isLeapYear(n: Int) = (n % 4 == 0) && (n % 100 != 0 || n % 400 == 0)
}
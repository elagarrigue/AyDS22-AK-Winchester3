package ayds.winchester.songinfo.home.view

import ayds.winchester.songinfo.home.model.entities.Song

class ResultReleaseDate {

    public fun getDescription(song: Song): String {
        return when (song.releaseDatePrecision) {
            "year" -> getDescriptionByYear(song)
            "month" -> getDescriptionByMonth(song)
            "day" -> getDescriptionByDay(song)
            else -> "Invalid Precision"
        }
    }
    private fun getDescriptionByYear(song: Song): String {
        var fecha = song.releaseDate.split("-").first()
        return if (!isLeapYear(fecha.toInt())) {
            "$fecha (not a leap year)"
        } else "$fecha (leap year)"
    }

    private fun getDescriptionByMonth(song: Song): String {
        var fecha = ""
        if (song.releaseDate.split("-").component2() != "") {
            fromNumberToMonth(song.releaseDate.split("-").component2() )
        }
        return "$fecha, " + song.releaseDate.split("-").first()
    }

    private fun fromNumberToMonth(month:String):String{
        var fecha = ""
        when (month) {
            "01" ->  fecha = "January"
            "02" ->  fecha ="February"
            "03" ->  fecha ="March"
            "04" ->  fecha ="April"
            "05" ->  fecha ="May"
            "06" ->  fecha ="June"
            "07" ->  fecha ="July"
            "08" ->  fecha ="August"
            "09" ->  fecha ="September"
            "10" ->  fecha ="October"
            "11" ->  fecha ="November"
            "12" ->  fecha ="December"
        }
        return fecha
    }

    private fun getDescriptionByDay(song: Song): String {

        return if (song.releaseDate.split("-").component3() != "") {
            (song.releaseDate.split("-").component3()) +
                    "/" + (song.releaseDate.split("-").component2()) +
                    "/" + (song.releaseDate.split("-").first())
        } else {
            ""
        }
    }

    private fun isLeapYear(n: Int) = (n % 4 == 0) && (n % 100 != 0 || n % 400 == 0)
}
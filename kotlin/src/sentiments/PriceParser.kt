package sentiments

import com.beust.klaxon.*
import java.text.SimpleDateFormat
import java.util.*

fun parsePrices(rawPrices: String): Array<Pair<Long, Double>> {
    val parser: Parser = Parser()
    val stringBuilder: StringBuilder = StringBuilder(rawPrices)
    val json: JsonObject = parser.parse(stringBuilder) as JsonObject
    val data: JsonArray<JsonObject> = json.array("data") ?: throw RuntimeException("Invalid data format.")

    val prices = data.map { v ->
        val time = v.string("time")
        val price = v.getValue("usd").toString()

        if (time == null) {
            throw RuntimeException("Invalid data input.")
        }
        Pair(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).parse(time.substringBefore("Z", "")).time, java.lang.Double.parseDouble(price))
    }
    return prices.toTypedArray()
}

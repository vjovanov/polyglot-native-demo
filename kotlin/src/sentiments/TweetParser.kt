package sentiments

import com.beust.klaxon.*

fun parseTweet(tweet: String): Pair<Long, String> {
    val parser: Parser = Parser()
    val stringBuilder: StringBuilder = StringBuilder(tweet)
    val json: JsonObject = parser.parse(stringBuilder) as JsonObject
    val text = json.string("text")
    val time = json.long("time")
    if (time == null || text == null) {
        throw RuntimeException("Invalid input format.")
    }
    return Pair(time, text)
}


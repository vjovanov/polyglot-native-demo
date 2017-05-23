package sentiments

import java.util.{Calendar, Date}

object Sentiments {


  val cutoffDate: Long = {
    val calendar = Calendar.getInstance()
    calendar.set(2017, 4, 12, 0, 0, 0)
    calendar.getTimeInMillis
  }

  def correlateTweetsWithPrices(prices: Array[(Long, Double)], tweetSentiments: Array[(Long, Boolean)]): Double = {
    val dayTweets = tweetSentiments
      .filter(v => v._1 > cutoffDate)
      .map(v => (dayOfMonth(v._1), v._2))

    val tweetsOnDay = dayTweets
      .groupBy(x => x._1).mapValues(_.map(_._2))
      .mapValues(v => (v.count(x => x), v.count(x => !x), v.length))

    println("Day            Positive\tNegative")
    tweetsOnDay.toSeq.sortBy(_._1).foreach {
      case (day, (positive, negative, _)) =>
        print(s"  May ${day}th 2017  $positive\t$negative\n")
    }

    val dayTweetsDifference = tweetsOnDay
      .mapValues(v => (v._1 - v._2).toDouble / v._3)

    val pricesPerDay = prices
      .filter(v => v._1 > cutoffDate)
      .map(v => (dayOfMonth(v._1), v._2))
      .groupBy(x => x._1) mapValues (_.map(_._2).sorted)

    val priceDeltaPerDay = pricesPerDay.mapValues(x => x(x.length - 1) - x(0))

    println("Price changes: ")
    priceDeltaPerDay.toSeq.sortBy(x => x._1).foreach {
      case (day, priceDelta) =>
        print(s"  May ${day}th 2017 -> $priceDelta\n")
    }

    correlate(priceDeltaPerDay.values.toArray, dayTweetsDifference.values.toArray)
  }

  def dayOfMonth(time: Long): Int = {
    val c = Calendar.getInstance()
    c.setTime(new Date(time))
    c.get(Calendar.DAY_OF_MONTH)
  }

  def normalize(vec: Array[Double]): Array[Double] = vec

  def correlate(a: Array[Double], b: Array[Double]): Double = {
    val n = a.length

    val (amean, avar) = (mean(a), variance(a))
    val (bmean, bvar) = (mean(a), variance(b))
    val astddev = math.sqrt(avar)
    val bstddev = math.sqrt(bvar)

    val coef = (a zip b).map({
      case (a, b) => ((a - amean) / astddev) * (b - bmean / bstddev)
    }).sum
    1.0 / (n - 1.0) * coef
  }

  def mean(arr: Array[Double]): Double = arr.sum / arr.length

  def variance(arr: Array[Double]) = {
    val meanValue = mean(arr)
    var temp = 0.0D

    arr.foreach { v =>
      temp += (v - meanValue) * (v - meanValue)
    }

    temp / arr.length
  }
}

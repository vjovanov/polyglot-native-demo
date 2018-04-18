package sentiments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.graalvm.nativeimage.c.function.CEntryPoint;;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import org.graalvm.nativeimage.Isolate;

import kotlin.Pair;
import scala.Tuple2;

public final class CInterface {

    public static void main(String[] args) throws IOException {
        String pricesString = new String(Files.readAllBytes(Paths.get("data/eth-price.csv")));
        String tweetsString = new String(Files.readAllBytes(Paths.get("data/ether-tweets")));
        double correlation = correlateTweetsWithMarket(pricesString, tweetsString);
        System.out.println("Price correlation: " + correlation);
    }

    @CEntryPoint(name = "correlate_tweets_with_market")
    public static double correlateTweetsWithMarket(Isolate isolate, CCharPointer prices, CCharPointer tweets) {
        return correlateTweetsWithMarket(CTypeConversion.toJavaString(prices), CTypeConversion.toJavaString(tweets));
    }

    public static double correlateTweetsWithMarket(String pricesString, String tweetsString) {
        // parse tweets
        List<Pair<Long, String>> tweets = Arrays.stream(tweetsString.split("\n"))
                .map(TweetParserKt::parseTweet)
                .collect(Collectors.toList());
        Pair<Long, Double>[] prices = PriceParserKt.parsePrices(pricesString);

        // parse tuples
        List<Tuple2<Long, Double>> priceTuples =
                Arrays.stream(prices).map(v -> new Tuple2<>(v.component1(), v.component2())).collect(Collectors.toList());

        // sentiment analysis
        List<Tuple2<Long, Boolean>> sentiments = tweets.stream()
                .map(t -> new Tuple2<>(t.component1(), SentimentAnalysis.isPositiveTweet(t.component2())))
                .collect(Collectors.toList());

        // correlated tweets with sentiments
        return Sentiments.correlateTweetsWithPrices(
                priceTuples.toArray(new Tuple2[priceTuples.size()]),
                sentiments.toArray(new Tuple2[sentiments.size()])
        );
    }

}

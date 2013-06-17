/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.datatorrent.demos.rollingtopwords;

import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.DAG;
import com.datatorrent.demos.twitter.TwitterSampleInput;
import com.datatorrent.lib.algo.UniqueCounter;
import com.datatorrent.lib.algo.WindowedTopCounter;
import com.datatorrent.lib.io.ConsoleOutputOperator;

import org.apache.hadoop.conf.Configuration;

/**
 * This application is same as other twitter demo
 * {@link com.datatorrent.demos.twitter.TwitterTopCounterApplication} <br>
 * Run Sample :
 * 
 * <pre>
 * 2013-06-17 16:50:34,911 [Twitter Stream consumer-1[Establishing connection]] INFO  twitter4j.TwitterStreamImpl info - Connection established.
 * 2013-06-17 16:50:34,912 [Twitter Stream consumer-1[Establishing connection]] INFO  twitter4j.TwitterStreamImpl info - Receiving status stream.
 * topWords: {}
 * topWords: {love=1, ate=1, catch=1, calma=1, Phillies=1, ela=1, from=1, running=1, <3=2, Zimmerman=1}
 * topWords: {،=4, الله=5, eu=3, da=2, e=2, من=5, pra=3, mas=2, não=4, <3=3}
 * topWords: {،=4, الله=5, eu=3, da=2, e=2, من=5, pra=3, mas=2, não=4, <3=3}
 * </pre>
 * 
 * @author Zhongjian Wang<zhongjian@malhar-inc.com>
 */
public class Application implements StreamingApplication
{
  private static final boolean inline = true;
  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
		TwitterSampleInput twitterFeed = new TwitterSampleInput();
		twitterFeed.setConsumerKey("uPLCFoMJUGrnTevKcYNPQ");
		twitterFeed
				.setConsumerSecret("0LWZ4fL3eivCbkQcvReFL4Pbcg2yx6KN1f5jRrso");
		twitterFeed
				.setAccessToken("1525255297-6HksiqL4B0J9lTQfzfWjTjr7VKTDKA6rQON6otd");
		twitterFeed
				.setAccessTokenSecret("ya76sQEFjz5rcWThNOOoViNNfAT1vMlD6xkqpswQ");
		twitterFeed = dag.addOperator("TweetSampler", twitterFeed);

    TwitterStatusWordExtractor wordExtractor = dag.addOperator("WordExtractor", TwitterStatusWordExtractor.class);

    UniqueCounter<String> uniqueCounter = dag.addOperator("UniqueWordCounter", new UniqueCounter<String>());

    WindowedTopCounter<String> topCounts = dag.addOperator("TopCounter", new WindowedTopCounter<String>());
    topCounts.setTopCount(10);
    topCounts.setSlidingWindowWidth(120, 1);

    dag.addStream("TweetStream", twitterFeed.text, wordExtractor.input).setInline(inline);
    dag.addStream("TwittedWords", wordExtractor.output, uniqueCounter.data).setInline(inline);

    dag.addStream("UniqueWordCounts", uniqueCounter.count, topCounts.input).setInline(inline);

    ConsoleOutputOperator consoleOperator = dag.addOperator("topWords", new ConsoleOutputOperator());
    consoleOperator.setStringFormat("topWords: %s");

    dag.addStream("TopWords", topCounts.output, consoleOperator.input).setInline(inline);
  }
}

/**
 * Copyright (c) 2012-2012 Malhar, Inc. All rights reserved.
 */
package com.malhartech.lib.math;

import com.malhartech.engine.TestCountAndLastTupleSink;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Performance tests for {@link com.malhartech.lib.math.MarginValue}<p>
 *
 */
public class MarginValueBenchmark
{
  private static Logger log = LoggerFactory.getLogger(MarginValueBenchmark.class);

  /**
   * Test node logic emits correct results
   */
  @Test
  @SuppressWarnings("SleepWhileInLoop")
  @Category(com.malhartech.annotation.PerformanceTestCategory.class)
  public void testNodeProcessing() throws Exception
  {
    testNodeProcessingSchema(new MarginValue<Double>());
  }

  public void testNodeProcessingSchema(MarginValue oper)
  {
    TestCountAndLastTupleSink marginSink = new TestCountAndLastTupleSink();

    oper.margin.setSink(marginSink);
    oper.setup(new com.malhartech.engine.OperatorContext("irrelevant", null, null));
    oper.setPercent(true);
    oper.beginWindow(0);

    int numTuples = 100000000;
    for (int i = 0; i < numTuples; i++) {
      oper.numerator.process(2);
      oper.numerator.process(20);
      oper.numerator.process(1000);
      oper.denominator.process(4);
      oper.denominator.process(40);
      oper.denominator.process(2000);
    }
    oper.endWindow();
    log.debug(String.format("\nBenchmarked %d tuples", numTuples * 6));
    Assert.assertEquals("number emitted tuples", 1, marginSink.count);
    Assert.assertEquals("margin was ", 50, ((Number) marginSink.tuple).intValue());
  }
}
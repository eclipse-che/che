/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.util;

import java.util.concurrent.TimeUnit;

/**
 * Rate exceed detector. It used to detect exceeding of rate of some operation. If rate is exceeded
 * then method {@link #updateAndCheckRate()} returns {@code true}. A {@code RateExceedDetector} gets
 * max allowed permits per seconds in constructor, then it checks that required time have elapsed
 * between two calls of method {@link #updateAndCheckRate()}.
 *
 * <p>Implementation is not threadsafe and required external synchronization if it's used in
 * multi-thread environment. An example that prints on stdout if rate exceed 5 per seconds limit:
 *
 * <pre>{@code
 * final RateExceedDetector r = new RateExceedDetector(5);
 *
 * void doSomething() {
 *     if (r.updateAndCheckRate()) {
 *         // do something useful
 *     } else {
 *         System.out.printf("Max rate exceeded, rate: %f 1/s%n", r.getRate());
 *     }
 * }
 *
 * }</pre>
 */
public class RateExceedDetector {
  private final long intervalMicros;
  private final long reportTimeMicros;
  private final long rateTime;

  private long count;
  private long lastMicros;
  private double rate;

  private long threshold;

  public RateExceedDetector(double permits) {
    intervalMicros = (long) (TimeUnit.SECONDS.toMicros(1L) / permits);
    reportTimeMicros = TimeUnit.SECONDS.toMicros(1);
    lastMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
    rateTime = 1L;
  }

  /**
   * Update rate and check is max allowed rate exceeded.
   *
   * @return {@code true} if max allowed rate is exceeded and {@code false} otherwise. If this
   *     method return {@code true} typically need check average rate with method {@link
   *     #getRate()}. In some cases it is possible to have smaller then expected interval between
   *     two calls of this method but average rate may be under allowed limit.
   */
  public boolean updateAndCheckRate() {
    final long nowTimeMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
    final boolean exceed = (nowTimeMicros - threshold) < 0;
    threshold = nowTimeMicros + intervalMicros;
    countRate(nowTimeMicros);
    return exceed;
  }

  private void countRate(long nowTimeMicros) {
    if (lastMicros + reportTimeMicros < nowTimeMicros) {
      rate = count / rateTime;
      lastMicros = nowTimeMicros;
      count = 0;
    }
    count++;
  }

  /** Get average rate of calls method {@link #updateAndCheckRate()}. */
  public double getRate() {
    return rate;
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.test.mockito.answer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Answer class that helps to lock execution of test in separate threads for testing purposes.
 *
 * <p>It can hold waiting thread until this answer is called.<br>
 * It can hold waiting thread that uses mock until answering to mock call is allowed.
 *
 * <p>Here is an example of complex test that ensures that (for example) locked area is not called
 * when other thread try to access it.
 *
 * <pre class="code"><code class="java">
 *     // given
 *     WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
 *     doAnswer(waitingAnswer).when(someClassUsedInTestedClass).someMethod(eq(param1), eq(param2));
 *
 *     // start doing something in a separate thread
 *     executor.execute(() -> testedClass.doSomething());
 *     // wait until separate thread call answer to find the moment when critical area is occupied
 *     // to make test fast wait not more that provided timeout
 *     waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);
 *
 *     // when
 *     try {
 *         // start doing something in current thread
 *         testedClass.doSomething()
 *         // this area should not be reachable until answer is completed!
 *         fail("Error message");
 *     } finally {
 *         // In this case exception can be suppressed
 *         // Test is simplified to provide clean example
 *
 *         // then
 *         // complete waiting answer
 *         waitingAnswer.completeAnswer();
 *         // ensure that someMethod is called only once to confirm that testedClass.doSomething()
 *         // doesn't call it this particular test
 *         verify(someClassUsedInTestedClass, timeout(100).times(1)).someMethod(any(), any());
 *     }
 * </code></pre>
 *
 * @author Alexander Garagatyi
 */
public class WaitingAnswer<T> implements Answer<T> {

  private final CountDownLatch answerIsCalledLatch;
  private final CountDownLatch answerResultIsUnlockedLatch;

  private long maxWaitingTime;
  private TimeUnit maxWaitingUnit;
  private T result;

  private volatile String error;

  public WaitingAnswer() {
    this.maxWaitingTime = 1;
    this.maxWaitingUnit = TimeUnit.SECONDS;
    this.result = null;
    this.answerIsCalledLatch = new CountDownLatch(1);
    this.answerResultIsUnlockedLatch = new CountDownLatch(1);
    this.error = null;
  }

  public WaitingAnswer(long maxWaitingTime, TimeUnit maxWaitingUnit) {
    this();
    this.maxWaitingTime = maxWaitingTime;
    this.maxWaitingUnit = maxWaitingUnit;
  }

  public WaitingAnswer(T result) {
    this();
    this.result = result;
  }

  public WaitingAnswer(T result, long maxWaitingTime, TimeUnit maxWaitingUnit) {
    this();
    this.result = result;
    this.maxWaitingTime = maxWaitingTime;
    this.maxWaitingUnit = maxWaitingUnit;
  }

  /**
   * Waits until answer is called in method {@link #answer(InvocationOnMock)}.
   *
   * @param maxWaitingTime max time to wait
   * @param maxWaitingUnit time unit of the max waiting time argument
   * @throws Exception if the waiting time elapsed before this answer is called
   * @see #answer(InvocationOnMock)
   */
  public void waitAnswerCall(long maxWaitingTime, TimeUnit maxWaitingUnit) throws Exception {
    if (!answerIsCalledLatch.await(maxWaitingTime, maxWaitingUnit)) {
      error = "Waiting time elapsed but answer is not called";
      throw new Exception(error);
    }
  }

  /**
   * Stops process of waiting returning result of answer in method {@link
   * #answer(InvocationOnMock)}.
   *
   * @throws Exception if this answer waiting time elapsed before this method is called
   * @see #answer(InvocationOnMock)
   */
  public void completeAnswer() throws Exception {
    answerResultIsUnlockedLatch.countDown();
    if (error != null) {
      throw new Exception(error);
    }
  }

  /**
   * Stops waiting until answer is called in method {@link #waitAnswerCall(long, TimeUnit)} and then
   * waits until method {@link #completeAnswer()} is called.
   *
   * @param invocationOnMock see {@link Answer#answer(InvocationOnMock)}
   * @return returns answer result if provided in constructor or null otherwise
   * @throws Exception if answer call or answer result waiting time is elapsed
   * @throws Throwable in the same cases as in {@link Answer#answer(InvocationOnMock)}
   * @see #waitAnswerCall(long, TimeUnit)
   * @see #completeAnswer()
   * @see Answer#answer(InvocationOnMock)
   */
  @Override
  public T answer(InvocationOnMock invocationOnMock) throws Throwable {
    // report start of answer call
    answerIsCalledLatch.countDown();
    if (error != null) {
      throw new Exception(error);
    }
    // wait until another thread unlocks returning of answer
    if (!answerResultIsUnlockedLatch.await(maxWaitingTime, maxWaitingUnit)) {
      error = "Waiting time elapsed but completeAnswer is not called";
      throw new Exception(error);
    }
    return result;
  }
}

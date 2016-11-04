/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.util.lineconsumer;

import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.commons.test.mockito.answer.WaitingAnswer;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.invocation.Invocation;
import org.mockito.testng.MockitoTestNGListener;
import org.mockito.verification.VerificationMode;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

/**
 * @author Mykola Morhun
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ConcurrentCompositeLineConsumerTest  {

    @Mock
    private LineConsumer lineConsumer1;
    @Mock
    private LineConsumer lineConsumer2;
    @Mock
    private LineConsumer lineConsumer3;

    private ConcurrentCompositeLineConsumer concurrentCompositeLineConsumer;

    private LineConsumer    subConsumers[];
    private ExecutorService executor;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        subConsumers = new LineConsumer[] { lineConsumer1, lineConsumer2, lineConsumer3 };
        concurrentCompositeLineConsumer = new ConcurrentCompositeLineConsumer(subConsumers);

        executor = Executors.newFixedThreadPool(3);
    }

    @AfterMethod
    public void afterMethod() {
        executor.shutdownNow();
    }

    @Test
    public void shouldWriteMessageIntoEachConsumer() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentCompositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : subConsumers) {
            verify(subConsumer).writeLine(eq(message));
        }
    }

    @Test
    public void shouldNotWriteIntoSubConsumersAfterClosingCompositeConsumer() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentCompositeLineConsumer.close();
        concurrentCompositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : subConsumers) {
            verify(subConsumer, never()).writeLine(anyString());
        }
    }

    @DataProvider(name = "subConsumersExceptions")
    public Object[][] subConsumersExceptions() {
        return new Throwable[][] {
                {new ConsumerAlreadyClosedException("Error")},
                {new ClosedByInterruptException()}
        };
    }

    @Test(dataProvider = "subConsumersExceptions")
    public void shouldCloseSubConsumerOnException(Throwable exception) throws Exception {
        // given
        final String message = "Test line";
        final String message2 = "Test line2";

        LineConsumer closedConsumer = mock(LineConsumer.class);
        doThrow(exception).when(closedConsumer).writeLine(anyString());

        concurrentCompositeLineConsumer = new ConcurrentCompositeLineConsumer(appendTo(subConsumers, closedConsumer));

        // when
        concurrentCompositeLineConsumer.writeLine(message);
        concurrentCompositeLineConsumer.writeLine(message2);

        // then
        verify(closedConsumer, never()).writeLine(eq(message2));
        for (LineConsumer consumer : subConsumers) {
            verify(consumer).writeLine(eq(message2));
        }
    }

    @Test
    public void shouldDoNothingOnWriteLineIfAllSubConsumersAreClosed() throws Exception {
        // given
        final String message = "Test line";
        LineConsumer[] closedConsumers = subConsumers;
        for (LineConsumer consumer : closedConsumers) {
            doThrow(ConsumerAlreadyClosedException.class).when(consumer).writeLine(anyString());
        }
        concurrentCompositeLineConsumer = new ConcurrentCompositeLineConsumer(closedConsumers);

        // when
        concurrentCompositeLineConsumer.writeLine("Error");
        concurrentCompositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : closedConsumers) {
            verify(subConsumer, never()).writeLine(eq(message));
        }
    }

    @Test
    public void shouldBeAbleToWriteIntoSubConsumersSimultaneously() throws Exception {
        // given
        final String message1 = "Message 1";
        final String message2 = "Message 2";

        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
        doAnswer(waitingAnswer).when(lineConsumer2).writeLine(eq(message1));

        executor.execute(() -> concurrentCompositeLineConsumer.writeLine(message1));
        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);

        // when
        concurrentCompositeLineConsumer.writeLine(message2);
        waitingAnswer.completeAnswer();

        // then
        awaitFinalization();

        for (LineConsumer consumer : subConsumers) {
            verify(consumer).writeLine(eq(message1));
            verify(consumer).writeLine(eq(message2));
        }
    }

    @Test
    public void closeOperationShouldWaitUntilAllCurrentOperationsWillBeFinished() throws Exception {
        // given
        final String message1 = "Message 1";
        final String message2 = "Message 2";

        WaitingAnswer<Void> waitingAnswer1 = waitOnWrite(lineConsumer2, message1);
        WaitingAnswer<Void> waitingAnswer2 = waitOnWrite(lineConsumer2, message2);

        // when
        executor.execute(concurrentCompositeLineConsumer::close);

        waitingAnswer1.completeAnswer();
        waitingAnswer2.completeAnswer();

        // then
        awaitFinalization();

        assertFalse(concurrentCompositeLineConsumer.isOpen());

        for (LineConsumer consumer : subConsumers) {
            verify(consumer).writeLine(eq(message1));
            verify(consumer).writeLine(eq(message2));
            verify(consumer, last()).close();
        }
    }

    @Test
    public void shouldIgnoreWriteToSubConsumersAfterCloseWasCalled() throws Exception {
        // given
        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
        doAnswer(waitingAnswer).when(lineConsumer2).close();

        executor.execute(() -> concurrentCompositeLineConsumer.close());
        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);

        // when
        concurrentCompositeLineConsumer.writeLine("Test line");
        waitingAnswer.completeAnswer();

        // then
        awaitFinalization();

        for (LineConsumer consumer : subConsumers) {
            verify(consumer, never()).writeLine(anyString());
        }
    }

    /**
     * Executes write line into file in separate thread and waits on writing to file operation until this thread released.
     *
     * @param consumer
     *         subconsumer on which thread should be freezed
     * @param message
     *         message to write
     * @return waiting answer to release this thread later using {@link WaitingAnswer#completeAnswer()}
     * @throws Exception
     */
    private WaitingAnswer<Void> waitOnWrite(LineConsumer consumer, String message) throws Exception {
        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
        doAnswer(waitingAnswer).when(consumer).writeLine(eq(message));

        executor.execute(() -> concurrentCompositeLineConsumer.writeLine(message));
        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);

        return waitingAnswer;
    }

    private void awaitFinalization() throws Exception {
        executor.shutdown();
        if (!executor.awaitTermination(1_000, TimeUnit.MILLISECONDS)) {
            fail("Operation is hanged up. Terminated.");
        }
    }

    private LineConsumer[] appendTo(LineConsumer[] base, LineConsumer... toAppend ) {
        List<LineConsumer> allElements = new ArrayList<>();
        allElements.addAll(Arrays.asList(base));
        allElements.addAll(Arrays.asList(toAppend));
        return allElements.toArray(new LineConsumer[allElements.size()]);
    }

    /**
     * Checks whether interaction with given mock is <i>the last one so far</i>.
     * Typical using:
     * <pre class="code"><code class="java">
     * verify(someMock, last()).someMethod();
     * </code></pre>
     */
    private static VerificationMode last() {
        return (verificationData) -> {
            List<Invocation> invocations = verificationData.getAllInvocations();
            InvocationMatcher invocationMatcher = verificationData.getWanted();

            if (invocations == null || invocations.isEmpty()) {
                throw new MockitoException("\nNo interactions with " + invocationMatcher.getInvocation().getMock() + " mock so far");
            }
            Invocation invocation = invocations.get(invocations.size() - 1);

            if (!invocationMatcher.matches(invocation)) {
                throw new MockitoException("\nWanted but not invoked:\n" + invocationMatcher);
            }
        };
    }

}

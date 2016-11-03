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

import org.eclipse.che.commons.test.mockito.answer.WaitingAnswer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

/**
 * @author Mykola Morhun
 */
@Listeners(value = {MockitoTestNGListener.class})
public class ConcurrentFileLineConsumerTest {
    private static final Logger LOG = getLogger(ConcurrentFileLineConsumer.class);

    @Mock
    private Writer writer;

    private ConcurrentFileLineConsumer concurrentFileLineConsumer;

    private ExecutorService executor;
    private File            file;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        file = File.createTempFile("file", ".tmp");
        concurrentFileLineConsumer = new ConcurrentFileLineConsumer(file);
        injectWriterMock(concurrentFileLineConsumer, writer);

        executor = Executors.newFixedThreadPool(3);
    }

    @AfterMethod
    public void afterMethod() {
        executor.shutdownNow();
    }

    @AfterClass
    public void afterClass() {
        if (!file.delete()) {
            LOG.warn("Failed to remove temporary file: '{}'.", file);
        }
    }

    @Test
    public void shouldBeAbleToWriteIntoFile() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentFileLineConsumer.writeLine(message);

        // then
        verify(writer).write(eq(message));
    }

    @Test
    public void shouldNotWriteIntoFileAfterConsumerClosing() throws Exception {
        // given
        final String message = "Test line";

        // when
        concurrentFileLineConsumer.close();
        concurrentFileLineConsumer.writeLine(message);

        // then
        verify(writer, never()).write(anyString());
    }

    @Test
    public void shouldBeAbleToWriteIntoFileSimultaneously() throws Exception {
        // given
        final String message1 = "Message 1";
        final String message2 = "Message 2";

        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
        doAnswer(waitingAnswer).when(writer).write(eq(message1));

        executor.execute(() -> writeIntoConsumer(message1));
        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);

        // when
        writeIntoConsumer(message2);
        waitingAnswer.completeAnswer();

        // then
        awaitFinalization();

        verify(writer).write(eq(message1));
        verify(writer).write(eq(message2));
    }

    @Test
    public void closeOperationShouldWaitUntilAllCurrentWriteOperationsWillBeFinished() throws Exception {
        // given
        final String message1 = "Message 1";
        final String message2 = "Message 2";

        WaitingAnswer<Void> waitingAnswer1 = waitOnWrite(message1);
        WaitingAnswer<Void> waitingAnswer2 = waitOnWrite(message2);

        // when
        executor.execute(this::closeConsumer);

        waitingAnswer1.completeAnswer();
        waitingAnswer2.completeAnswer();

        // then
        awaitFinalization();

        assertFalse(concurrentFileLineConsumer.isOpen());

        verify(writer).write(eq(message1));
        verify(writer).write(eq(message2));
    }

    @Test
    public void shouldNotWriteIntoSubConsumersWhenLockForCloseIsLocked() throws Exception {
        // given
        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
        doAnswer(waitingAnswer).when(writer).close();

        executor.execute(this::closeConsumer);
        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);

        // when
        writeIntoConsumer("Test line");
        waitingAnswer.completeAnswer();

        // then
        awaitFinalization();

        verify(writer, never()).write(anyString());
    }

    /**
     * Inject Writer mock into FileLineConsumer class.
     * This allow to test the FileLineConsumer operations.
     *
     * @param concurrentFileLineConsumer
     *         instance in which mock will be injected
     * @param writerMock
     *         mock to inject
     * @throws Exception
     */
    private void injectWriterMock(ConcurrentFileLineConsumer concurrentFileLineConsumer, Writer writerMock) throws Exception {
        Field writerField = concurrentFileLineConsumer.getClass().getDeclaredField("writer");
        writerField.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(writerField, writerField.getModifiers() & ~Modifier.FINAL);

        writerField.set(concurrentFileLineConsumer, writerMock);
    }

    private void writeIntoConsumer(String message) {
        try {
            concurrentFileLineConsumer.writeLine(message);
        } catch (IOException ignore) {
        }
    }

    private void closeConsumer() {
        try {
            concurrentFileLineConsumer.close();
        } catch (IOException ignore) {
        }
    }

    /**
     * Executes write line into file in separate thread and waits on writing to file operation until this thread released.
     *
     * @param message
     *         message to write
     * @return waiting answer to release this thread later using {@link WaitingAnswer#completeAnswer()}
     * @throws Exception
     */
    private WaitingAnswer<Void> waitOnWrite(String message) throws Exception {
        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
        doAnswer(waitingAnswer).when(writer).write(eq(message));

        executor.execute(() -> writeIntoConsumer(message));
        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);

        return waitingAnswer;
    }

    private void awaitFinalization() throws Exception {
        executor.shutdown();
        if (!executor.awaitTermination(1_000, TimeUnit.MILLISECONDS)) {
            fail("Operation is hanged up. Terminated.");
        }
    }

}

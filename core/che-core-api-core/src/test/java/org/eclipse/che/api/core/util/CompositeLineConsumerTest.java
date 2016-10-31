package org.eclipse.che.api.core.util;

import org.eclipse.che.api.core.util.lineconsumer.ConsumerAlreadyClosedException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.channels.ClosedByInterruptException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Mykola Morhun
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CompositeLineConsumerTest {

    @Mock
    private LineConsumer lineConsumer1;
    @Mock
    private LineConsumer lineConsumer2;
    @Mock
    private LineConsumer lineConsumer3;

    private CompositeLineConsumer compositeLineConsumer;

    private LineConsumer subConsumers[];

    @BeforeMethod
    public void beforeMethod() throws Exception {
        subConsumers = new LineConsumer[] { lineConsumer1, lineConsumer2 };

        for (LineConsumer lineConsumer : subConsumers) {
            doNothing().when(lineConsumer).writeLine(anyString());
        }
    }

    @Test
    public void shouldWriteMessageIntoEachConsumer() throws Exception {
        // given
        final String message = "Test line";
        compositeLineConsumer = new CompositeLineConsumer(subConsumers);

        // when
        compositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : subConsumers) {
            verify(subConsumer).writeLine(eq(message));
        }
    }

    @Test
    public void shouldNotWriteIntoSubConsumersAfterClosingCompositeConsumer() throws Exception {
        // given
        final String message = "Test line";
        compositeLineConsumer = new CompositeLineConsumer(subConsumers);

        // when
        compositeLineConsumer.close();
        compositeLineConsumer.writeLine(message);

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
    public void shouldCloseSubConsumerOnConsumerAlreadyClosedException(Throwable exception) throws Exception {
        // given
        final String message = "Test line";
        final String message2 = "Test line2";
        final int closedConsumerIndex = 1;

        LineConsumer closedConsumer = mock(LineConsumer.class);
        doThrow(exception.getClass()).when(closedConsumer).writeLine(anyString());
        subConsumers[closedConsumerIndex] = closedConsumer;

        compositeLineConsumer = new CompositeLineConsumer(subConsumers);

        // when
        compositeLineConsumer.writeLine(message);
        compositeLineConsumer.writeLine(message2);

        // then
        for (int i = 0; i < subConsumers.length; i++) {
            if (i != closedConsumerIndex) {
                verify(subConsumers[i]).writeLine(eq(message2));
            } else {
                verify(subConsumers[closedConsumerIndex], never()).writeLine(eq(message2));
            }
        }
    }

    @Test
    public void shouldDoNothingOnWriteLineIfAllSubConsumersAreClosed() throws Exception {
        // given
        final String message = "Test line";
        for (int i = 0; i < subConsumers.length; i++) {
            LineConsumer closedConsumer = mock(LineConsumer.class);
            doThrow(ConsumerAlreadyClosedException.class).when(closedConsumer).writeLine(anyString());
            subConsumers[i] = closedConsumer;
        }
        compositeLineConsumer = new CompositeLineConsumer(subConsumers);

        // when
        compositeLineConsumer.writeLine("Error");
        compositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : subConsumers) {
            verify(subConsumer, never()).writeLine(eq(message));
        }
    }

}

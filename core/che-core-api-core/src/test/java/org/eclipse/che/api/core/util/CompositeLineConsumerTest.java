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
package org.eclipse.che.api.core.util;

import org.eclipse.che.api.core.util.lineconsumer.ConsumerAlreadyClosedException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
        subConsumers = new LineConsumer[] { lineConsumer1, lineConsumer2, lineConsumer3 };
        compositeLineConsumer = new CompositeLineConsumer(subConsumers);
    }

    @Test
    public void shouldWriteMessageIntoEachConsumer() throws Exception {
        // given
        final String message = "Test line";

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
    public void shouldCloseSubConsumerOnException(Throwable exception) throws Exception {
        // given
        final String message = "Test line";
        final String message2 = "Test line2";

        LineConsumer closedConsumer = mock(LineConsumer.class);
        doThrow(exception).when(closedConsumer).writeLine(anyString());

        compositeLineConsumer = new CompositeLineConsumer(appendTo(subConsumers, closedConsumer));

        // when
        compositeLineConsumer.writeLine(message);
        compositeLineConsumer.writeLine(message2);

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
        compositeLineConsumer = new CompositeLineConsumer(closedConsumers);

        // when
        compositeLineConsumer.writeLine("Error");
        compositeLineConsumer.writeLine(message);

        // then
        for (LineConsumer subConsumer : closedConsumers) {
            verify(subConsumer, never()).writeLine(eq(message));
        }
    }

    private LineConsumer[] appendTo(LineConsumer[] base, LineConsumer... toAppend ) {
        List<LineConsumer> allElements = new ArrayList<>();
        allElements.addAll(Arrays.asList(base));
        allElements.addAll(Arrays.asList(toAppend));
        return allElements.toArray(new LineConsumer[allElements.size()]);
    }

}

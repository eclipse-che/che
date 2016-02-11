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
package org.eclipse.che.api.project.server;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testing {@link ProjectImportOutputWSLineConsumer}. To avoid output overflow of sent messages, this line consumer should not send all the
 * lines but skip some if needed. This test is checking that for a delay of 300ms between each messages, the messages would be delayed if
 * needed or overridden if newer messages are sent before the delay count down.
 */
public class ProjectImportOutputWSLineConsumerTest {


    protected String lastSentLine;

    @Test
    public void testSkippingLinesInLineConsumer() throws IOException, InterruptedException {
        ProjectImportOutputWSLineConsumer lineConsumer = new ProjectImportOutputWSLineConsumer("test", "test", 300) {

            @Override
            protected void sendMessageToWS(ChannelBroadcastMessage bm) {
                Matcher m = Pattern.compile("^.*line\":\"(.+)\".*").matcher(bm.getBody());
                if (m.find()) {
                    lastSentLine = m.group(1);
                }
            }
        };
        try {
            lineConsumer.writeLine("line1");
            Thread.sleep(30); // let some time to the first thread to do his job ...
            lineConsumer.writeLine("line1.1");
            lineConsumer.writeLine("line1.2");
            // at this point should send line1 because it's the first message
            Assert.assertEquals("after 0ms, sent line", "line1", lastSentLine);
            Thread.sleep(200);
            lineConsumer.writeLine("line2");
            Assert.assertEquals("after 200ms, sent line", "line1", lastSentLine);
            Thread.sleep(100);
            // at this point, sending message line2: after 300ms
            Assert.assertEquals("after 300ms, sent line", "line2", lastSentLine);
            Thread.sleep(100);
            lineConsumer.writeLine("line1");
            Assert.assertEquals("after 400ms since the last sent message, sent line", "line2", lastSentLine);
            Thread.sleep(200);
            // at this point, should sent the last message: line1
            Assert.assertEquals("after 600ms, sent line", "line1", lastSentLine);
            Thread.sleep(800);

            // if last message has been sent > 300, reset the counter, redoing the previous tests should work from now
            lineConsumer.writeLine("line21");
            Thread.sleep(100);
            // at this point should send line1 because it's the first message
            Assert.assertEquals("after 0ms, sent line", "line21", lastSentLine);
            // sleep 230 to let 30ms to write the message
            Thread.sleep(200);
            lineConsumer.writeLine("line22");
            Assert.assertEquals("after 200ms, sent line", "line21", lastSentLine);
            Thread.sleep(100);
            // at this point, sending message line2: after 300ms
            Assert.assertEquals("after 300ms, sent line", "line22", lastSentLine);
            Thread.sleep(100);
            lineConsumer.writeLine("line21");
            Assert.assertEquals("after 400ms since the last sent message, sent line", "line22", lastSentLine);
            Thread.sleep(200);
            // at this point, should sent the last message: line1
            Assert.assertEquals("after 600ms, sent line", "line21", lastSentLine);
        } finally {
            lineConsumer.close();
        }
    }
}

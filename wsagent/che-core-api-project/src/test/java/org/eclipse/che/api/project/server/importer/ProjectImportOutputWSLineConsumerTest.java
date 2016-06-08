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
package org.eclipse.che.api.project.server.importer;

import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 *  @author Igor Vinokur
 */
public class ProjectImportOutputWSLineConsumerTest {

    @Test
    public void shouldSendMessage() {
        //given
        ArgumentCaptor<ChannelBroadcastMessage> argumentCaptor = ArgumentCaptor.forClass(ChannelBroadcastMessage.class);
        ProjectImportOutputWSLineConsumer consumer = spy(new ProjectImportOutputWSLineConsumer("project", "workspace", 300));

        //when
        consumer.sendMessage("message");

        //then
        verify(consumer).sendMessageToWS(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getChannel(), "importProject:output:workspace:project");
        assertEquals(argumentCaptor.getValue().getBody(), "{\"num\":1, \"line\":\"message\"}");
    }
}

package org.eclipse.che.plugin.languageserver.server.lsapi;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.EncodeException;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.typefox.lsapi.PublishDiagnosticsParams;
import io.typefox.lsapi.PublishDiagnosticsParamsImpl;

@Singleton
public class PublishDiagnosticsParamsMessenger implements EventSubscriber<PublishDiagnosticsParams> {
    private final static Logger LOG = LoggerFactory.getLogger(PublishDiagnosticsParamsMessenger.class);

    private EventService eventService;

    @Inject
    public PublishDiagnosticsParamsMessenger(final EventService eventService) {
        this.eventService = eventService;
    }

    public void onEvent(final PublishDiagnosticsParams event) {
        try {
            if (event instanceof PublishDiagnosticsParamsImpl && event.getUri().startsWith("file:///projects")) {
                ((PublishDiagnosticsParamsImpl)event).setUri(event.getUri().substring(16));
            }
            final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
            bm.setChannel("languageserver/textDocument/publishDiagnostics");
            bm.setBody(new Gson().toJson(event));
            WSConnectionContext.sendMessage(bm);
        } catch (EncodeException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @PostConstruct
    public void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    public void unsubscribe() {
        eventService.unsubscribe(this);
    }
}

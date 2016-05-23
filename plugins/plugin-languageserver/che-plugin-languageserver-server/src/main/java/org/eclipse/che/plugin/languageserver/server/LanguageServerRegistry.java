package org.eclipse.che.plugin.languageserver.server;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.eclipse.che.plugin.languageserver.server.lsapi.PublishDiagnosticsParamsMessenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.typefox.lsapi.InitializeParamsImpl;
import io.typefox.lsapi.LanguageServer;
import io.typefox.lsapi.NotificationCallback;
import io.typefox.lsapi.PublishDiagnosticsParams;

@Singleton
public class LanguageServerRegistry {

    private Map<String, LanguageServer> extensionToServer = newHashMap();
    private final PublishDiagnosticsParamsMessenger publishDiagnosticsMessenger;
    
    @Inject
    public LanguageServerRegistry(PublishDiagnosticsParamsMessenger publishDiagnosticsMessenger) {
        this.publishDiagnosticsMessenger = publishDiagnosticsMessenger;
    }
    
    public LanguageServer findServer(String uri) {
        int lastIndexOf = uri.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return null;
        }
        String extension = uri.substring(lastIndexOf + 1);
        return extensionToServer.get(extension);
    }

    public void registerForExtension(String extension, LanguageServer server) {
        this.extensionToServer.put(extension, server);
        server.initialize(new InitializeParamsImpl() {
            {
                //HACK hard coded properties
                setProcessId(4711);
                setRootPath("/projects/");
            }
        });
        server.getTextDocumentService().onPublishDiagnostics(new NotificationCallback<PublishDiagnosticsParams>() {
            @Override
            public void call(PublishDiagnosticsParams param) {
                publishDiagnosticsMessenger.onEvent(param);
            }
        });
    }
}

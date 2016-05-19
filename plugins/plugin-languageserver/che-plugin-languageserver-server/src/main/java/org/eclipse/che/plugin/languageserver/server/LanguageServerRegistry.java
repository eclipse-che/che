package org.eclipse.che.plugin.languageserver.server;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.google.inject.Singleton;

import io.typefox.lsapi.LanguageServer;

@Singleton
public class LanguageServerRegistry {

    private Map<String, LanguageServer> extensionToServer = newHashMap();

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
    }
}

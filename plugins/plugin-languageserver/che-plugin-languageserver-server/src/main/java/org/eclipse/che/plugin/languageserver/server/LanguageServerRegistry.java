package org.eclipse.che.plugin.languageserver.server;

import io.typefox.lsapi.InitializeParamsImpl;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.services.LanguageServer;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.lsapi.PublishDiagnosticsParamsMessenger;
import org.eclipse.che.plugin.languageserver.shared.lsapi.LanguageDescriptionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

@Singleton
public class LanguageServerRegistry {
	
	private final static Logger LOG = LoggerFactory.getLogger(LanguageServerRegistry.class);

	private Map<String, LanguageServer> extensionToServer = newHashMap();
	private Map<LanguageServer, List<? extends LanguageDescription>> server2InitResult = newHashMap();
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

	public List<LanguageDescription> getSupportedLanguages() {
		List<LanguageDescription> result = newArrayList();
		synchronized (server2InitResult) {
			for (List<? extends LanguageDescription> supportedLanguages : server2InitResult.values()) {
				result.addAll(supportedLanguages);
			}
		}
		return result;
	}

	private static int getProcessId() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		int prefixEnd = name.indexOf('@');
		if (prefixEnd != -1) {
			String prefix = name.substring(0, prefixEnd);
			try {
				return Integer.parseInt(prefix);
			} catch (NumberFormatException e) {
			}
		}
		return -1;
	}

	private final static int PROCESS_ID = getProcessId();

	public void register(final LanguageServer server) {
		register(server, null);
	}

	public void register(final LanguageServer server, final List<LanguageDescriptionDTO> languages) {
		InitializeParamsImpl initializeParams = new InitializeParamsImpl();
		initializeParams.setProcessId(PROCESS_ID);
        //TODO remove if it's only for demo
        if (languages != null && "csharp".equals(languages.get(0).getLanguageId())) {
            initializeParams.setRootPath("/projects/test/");
        }else {
            initializeParams.setRootPath("/projects/");
        }
		initializeParams.setClientName("EclipseChe");
		connect(server);
		CompletableFuture<InitializeResult> result = server.initialize(initializeParams);
		try {
			InitializeResult initializeResult = result.get();
			internalRegisterInitialized(server, languages==null ? initializeResult.getSupportedLanguages():languages);
		} catch (InterruptedException | ExecutionException e) {
			LOG.error("Error registering language : "+e.getMessage(), e);
		}
	}

	private void internalRegisterInitialized(final LanguageServer server, List<? extends LanguageDescription> supportedLanguages) {
		synchronized (server2InitResult) {
			Set<LanguageServer> toBeReplaced = newHashSet();
			for (Entry<LanguageServer, List<? extends LanguageDescription>> entry : server2InitResult.entrySet()) {
				for (LanguageDescription description : entry.getValue()) {
					for (LanguageDescription newLang : supportedLanguages) {
						if (newLang.getLanguageId().equals(description.getLanguageId())) {
							toBeReplaced.add(entry.getKey());
							LOG.info("Removing running language server for '"+newLang.getLanguageId()+"'.");
						}
					}
				}
			}
			for (LanguageServer toDie : toBeReplaced) {
				toDie.shutdown();
				toDie.exit();
				server2InitResult.remove(toDie);
			}
			LOG.info("Adding language server for '"+Joiner.on(',').join(supportedLanguages.stream().map((lang)->lang.getLanguageId()).iterator())+"'.");
			server2InitResult.put(server, supportedLanguages);
			for (LanguageDescription lang : supportedLanguages) {
				for (String extension : lang.getFileExtensions()) {
					extensionToServer.put(extension, server);
				}
			}
		}
	}

	private void connect(LanguageServer server) {
		server.getTextDocumentService().onPublishDiagnostics(publishDiagnosticsMessenger::onEvent);
        //TODO do we need to send this log messages ot client(browser)?
		server.getWindowService().onLogMessage(messageParams -> LOG.error(messageParams.getType() + " " + messageParams.getMessage()));
	}
}

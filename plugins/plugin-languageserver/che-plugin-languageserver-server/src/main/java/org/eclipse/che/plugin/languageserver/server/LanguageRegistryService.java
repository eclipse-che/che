package org.eclipse.che.plugin.languageserver.server;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.typefox.lsapi.LanguageDescription;

@Singleton
@Path("languageserver")
public class LanguageRegistryService {
	
	private LanguageServerRegistry registry;
	private FatJarBasedLanguageServerRegistrant registrant;

	@Inject
	public LanguageRegistryService(LanguageServerRegistry reg, FatJarBasedLanguageServerRegistrant registrant) {
		this.registry = reg;
		this.registrant = registrant;
	}
	
	@GET
	@Path("supportedLanguages")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LanguageDescription> getSupportedLanguages() {
		registrant.registerLanguageServer(registry);
		return registry.getSupportedLanguages();
	}
	
	@GET
	@Path("scanForNewLanguages")
	@Produces(MediaType.APPLICATION_JSON)
	public void scanForNewLanguages() {
		registrant.registerLanguageServer(registry);
	}
}

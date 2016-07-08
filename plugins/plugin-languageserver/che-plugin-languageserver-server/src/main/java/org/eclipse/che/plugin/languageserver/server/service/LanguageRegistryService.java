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
package org.eclipse.che.plugin.languageserver.server.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.DtoConverter;
import org.eclipse.che.plugin.languageserver.server.registry.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.shared.lsapi.InitializeResultDTO;
import org.eclipse.che.plugin.languageserver.shared.lsapi.LanguageDescriptionDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Path("languageserver")
public class LanguageRegistryService {

	private final LanguageServerRegistry registry;

	@Inject
	public LanguageRegistryService(LanguageServerRegistry registry) {
		this.registry = registry;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("supported")
	public List<LanguageDescriptionDTO> getSupportedLanguages() {
		return registry.getSupportedLanguages()
					   .stream()
					   .map(DtoConverter::asDto)
					   .collect(Collectors.toList());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("registered")
	public List<InitializeResultDTO> getRegisteredLanguages() {
		return registry.getRegisteredLanguages()
					   .stream()
					   .map(DtoConverter::asDto)
					   .collect(Collectors.toList());

	}
}

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
package org.eclipse.che.plugin.languageserver.server;

import io.typefox.lsapi.LanguageDescription;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Singleton
@Path("languageserver")
public class LanguageRegistryService {

	private LanguageServerRegistry registry;

	@Inject
	public LanguageRegistryService(LanguageServerRegistry registry) {
		this.registry = registry;
	}

	@GET
	@Path("supportedLanguages")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LanguageDescription> getSupportedLanguages() {
		return registry.getSupportedLanguages();
	}
	
	@GET
	@Path("scanForNewLanguages")
	@Produces(MediaType.APPLICATION_JSON)
	public void scanForNewLanguages() { }
}

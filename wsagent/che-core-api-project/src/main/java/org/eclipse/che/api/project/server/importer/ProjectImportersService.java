/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server.importer;

import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.project.server.DtoConverter;
import org.eclipse.che.api.project.shared.dto.ProjectImporterData;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Provide information about registered ProjectImporter's via REST.
 *
 * @author Vitaly Parfonov
 */
@Path("project-importers")
public class ProjectImportersService extends Service {

    private final Map<String, String>     configuration;
    private final ProjectImporterRegistry importersRegistry;

    @Inject
    public ProjectImportersService(ProjectImporterRegistry importersRegistry,
                                   @Named("project.importer.default_importer_id") String defaultProjectImporter) {
        this.configuration = new HashMap<>();
        this.importersRegistry = importersRegistry;
        this.configuration.put("default-importer", defaultProjectImporter);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ProjectImporterData getImportersData() {
        final List<ProjectImporterDescriptor> importers = importersRegistry.getImporters()
                                                                           .stream()
                                                                           .map(DtoConverter::asDto)
                                                                           .collect(Collectors.toList());
        return newDto(ProjectImporterData.class).withImporters(importers).withConfiguration(configuration);
    }
}

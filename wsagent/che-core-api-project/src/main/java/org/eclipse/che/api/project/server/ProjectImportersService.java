/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.project.server.impl.ProjectDtoConverter;
import org.eclipse.che.api.project.server.impl.ProjectImporterRegistry;
import org.eclipse.che.api.project.shared.dto.ProjectImporterData;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;

/**
 * Provide information about registered ProjectImporter's via REST.
 *
 * @author Vitaly Parfonov
 */
@Path("project-importers")
public class ProjectImportersService extends Service {

  private final Map<String, String> configuration;
  private final ProjectImporterRegistry projectImporterRegistry;

  @Inject
  public ProjectImportersService(
      ProjectImporterRegistry projectImporterRegistry,
      @Named("project.importer.default_importer_id") String defaultProjectImporter) {
    this.configuration = new HashMap<>();
    this.projectImporterRegistry = projectImporterRegistry;
    this.configuration.put("default-importer", defaultProjectImporter);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ProjectImporterData getImportersData() {
    final List<ProjectImporterDescriptor> importers =
        projectImporterRegistry.getAll().stream().map(ProjectDtoConverter::asDto).collect(toList());
    return newDto(ProjectImporterData.class)
        .withImporters(importers)
        .withConfiguration(configuration);
  }
}

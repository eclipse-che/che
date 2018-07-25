/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.template;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;

/**
 * Provide information about registered ProjectTemplates via REST.
 *
 * @author Vitaly Parfonov
 */
@Path("project-template")
public class ProjectTemplateService extends Service {

  private ProjectTemplateRegistry templateRegistry;

  @Inject
  public ProjectTemplateService(ProjectTemplateRegistry templateRegistry) {
    this.templateRegistry = templateRegistry;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProjectTemplateDescriptor> getProjectTemplates(@QueryParam("tag") List<String> tags) {
    return templateRegistry.getTemplates(tags);
  }

  @GET
  @Path("/all")
  @Produces(MediaType.APPLICATION_JSON)
  public List<ProjectTemplateDescriptor> getProjectTemplates() {
    return templateRegistry.getAllTemplates();
  }
}

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
package org.eclipse.che.plugin.java.server.rest;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.plugin.java.server.JavaNavigation;
import org.eclipse.jdt.core.JavaModelException;

/** @author Evgen Vidolob */
@Path("java/navigation")
public class JavaNavigationService {
  @Inject private JavaNavigation navigation;

  @GET
  @Path("get/projects/and/packages")
  @Produces("application/json")
  public List<JavaProject> getProjectsAndPackages(
      @QueryParam("includepackages") boolean includePackages) throws JavaModelException {
    return navigation.getAllProjectsAndPackages(includePackages);
  }
}

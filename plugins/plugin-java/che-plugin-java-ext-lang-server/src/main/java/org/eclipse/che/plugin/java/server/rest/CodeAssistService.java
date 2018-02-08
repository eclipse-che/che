/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.rest;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.plugin.java.server.CodeAssist;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jface.text.BadLocationException;

/** @author Evgen Vidolob */
@Path("java/code-assist")
public class CodeAssistService {
  private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
  @Inject private CodeAssist codeAssist;

  /**
   * Organizes the imports of a compilation unit.
   *
   * @param projectPath path to the project
   * @param fqn fully qualified name of the java file
   * @return list of imports which have conflicts
   */
  @POST
  @Path("/organize-imports")
  @Produces({MediaType.APPLICATION_JSON})
  public OrganizeImportResult organizeImports(
      @QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn)
      throws NotFoundException, CoreException, BadLocationException {
    IJavaProject project = model.getJavaProject(projectPath);
    return codeAssist.organizeImports(project, fqn);
  }

  /**
   * Applies chosen imports after resolving conflicts.
   *
   * @param projectPath path to the project
   * @param fqn fully qualified name of the java file
   * @param chosen
   * @return list of document changes
   */
  @POST
  @Path("/apply-imports")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<Change> applyChosenImports(
      @QueryParam("projectpath") String projectPath,
      @QueryParam("fqn") String fqn,
      ConflictImportDTO chosen)
      throws NotFoundException, CoreException, BadLocationException {
    IJavaProject project = model.getJavaProject(projectPath);
    return codeAssist.applyChosenImports(project, fqn, chosen.getTypeMatches());
  }
}

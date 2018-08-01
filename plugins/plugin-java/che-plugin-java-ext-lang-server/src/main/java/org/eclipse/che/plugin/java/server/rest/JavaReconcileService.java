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

import com.google.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.jdt.javaeditor.JavaReconciler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/** @author Evgen Vidolob */
@Path("java/reconcile")
public class JavaReconcileService {

  private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();

  @Inject private JavaReconciler reconciler;

  @GET
  @Produces("application/json")
  public ReconcileResult reconcile(
      @QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn)
      throws JavaModelException {
    IJavaProject javaProject = model.getJavaProject(projectPath);
    return reconciler.reconcile(javaProject, fqn);
  }
}

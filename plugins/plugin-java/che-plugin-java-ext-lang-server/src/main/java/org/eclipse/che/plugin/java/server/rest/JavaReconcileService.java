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

package org.eclipse.che.plugin.java.server.rest;

import com.google.inject.Inject;

import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.jdt.javaeditor.JavaReconciler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * @author Evgen Vidolob
 */
@Path("java/reconcile")
public class JavaReconcileService {

    private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();

    @Inject
    private JavaReconciler reconciler;

    @GET
    @Produces("application/json")
    public ReconcileResult reconcile(@QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn)
            throws JavaModelException {
        IJavaProject javaProject = model.getJavaProject(projectPath);
        return reconciler.reconcile(javaProject, fqn);
    }
}

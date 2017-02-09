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
package org.eclipse.che.plugin.java.server.rest;

import com.google.inject.Inject;

import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.plugin.java.server.search.SearchException;
import org.eclipse.che.plugin.java.server.search.SearchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * REST service for all java project related searches.
 *
 * @author Evgen Vidolob
 */
@Path("java/search")
public class SearchService {

    @Inject
    private SearchManager manager;

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Path("find/usages")
    public FindUsagesResponse findUsages(FindUsagesRequest request) throws SearchException {
        JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
        IJavaProject javaProject = javaModel.getJavaProject(request.getProjectPath());
        return manager.findUsage(javaProject, request.getFQN(), request.getOffset());
    }
}

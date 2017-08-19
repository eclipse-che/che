/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.project;

import javax.inject.Inject;
import org.eclipse.che.api.project.shared.dto.ProjectImporterData;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.HTTPHeader;

/** @author Vitaly Parfonov */
public class ProjectImportersServiceClientImpl implements ProjectImportersServiceClient {

  private final AsyncRequestFactory asyncRequestFactory;

  @Inject
  public ProjectImportersServiceClientImpl(AsyncRequestFactory asyncRequestFactory) {
    this.asyncRequestFactory = asyncRequestFactory;
  }

  @Override
  public void getProjectImporters(
      DevMachine devMachine, AsyncRequestCallback<ProjectImporterData> callback) {
    asyncRequestFactory
        .createGetRequest(devMachine.getWsAgentBaseUrl() + "/project-importers")
        .header(HTTPHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON)
        .send(callback);
  }
}

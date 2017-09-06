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
package org.eclipse.che.selenium.core.utils;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceImpl;

/** @author Mihail Kuznyetsov */
public class WorkspaceDtoDeserializer {

  public WorkspaceConfigDto deserializeWorkspaceTemplate(String templateName) {
    requireNonNull(templateName);

    String pathToTemplate =
        TestWorkspaceImpl.class.getResource("/templates/workspace/" + templateName).getPath();
    String json;
    try {
      json = FileUtils.readFileToString(new File(pathToTemplate), Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException("Could not read template " + templateName);
    }
    return DtoFactory.getInstance().createDtoFromJson(json, WorkspaceConfigDto.class);
  }
}

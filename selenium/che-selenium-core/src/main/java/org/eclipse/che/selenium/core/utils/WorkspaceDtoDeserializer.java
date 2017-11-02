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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceImpl;

/**
 * Creates workspace config from JSON stored in resources. Takes into account current infrastructure
 * implementation set by property.
 *
 * @author Mihail Kuznyetsov
 * @author Alexander Garagatyi
 */
public class WorkspaceDtoDeserializer {
  @Inject
  @Named("che.selenium.infrastructure")
  private String infrastructure;

  public WorkspaceConfigDto deserializeWorkspaceTemplate(String templateName) {
    requireNonNull(templateName);

    String pathToTemplate =
        TestWorkspaceImpl.class
            .getResource(format("/templates/workspace/%s/%s", infrastructure, templateName))
            .getPath();

    File templateFile = new File(pathToTemplate);
    if (!templateFile.isFile()) {
      throw new RuntimeException(
          format(
              "Workspace template %s file %s doesn't exist or not a file",
              templateName, templateFile));
    }

    String json;
    try {
      json = FileUtils.readFileToString(templateFile, Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException("Could not read template " + templateName);
    }
    return DtoFactory.getInstance().createDtoFromJson(json, WorkspaceConfigDto.class);
  }
}

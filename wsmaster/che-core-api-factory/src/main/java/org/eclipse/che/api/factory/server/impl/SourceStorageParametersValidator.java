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
package org.eclipse.che.api.factory.server.impl;

import static java.lang.String.format;
import static org.eclipse.che.api.factory.server.FactoryConstants.PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE;
import static org.eclipse.che.api.factory.server.FactoryConstants.PARAMETRIZED_INVALID_PARAMETER_MESSAGE;

import java.util.Map;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.factory.server.FactoryParameterValidator;

/**
 * @author Alexander Garagatyi
 * @author Valeriy Svydenko
 */
public class SourceStorageParametersValidator implements FactoryParameterValidator<SourceStorage> {
  @Override
  public void validate(SourceStorage source, FactoryParameter.Version version)
      throws ConflictException {
    for (Map.Entry<String, String> entry : source.getParameters().entrySet()) {
      switch (entry.getKey()) {
        case "keepVcs":
          final String keepVcs = entry.getValue();
          if (!"true".equals(keepVcs) && !"false".equals(keepVcs)) {
            throw new ConflictException(
                format(
                    PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE,
                    "source.project.parameters.keepVcs",
                    entry.getValue()));
          }
          break;
        case "skipFirstLevel":
          final String skipFirstLevel = entry.getValue();
          if (!"true".equals(skipFirstLevel) && !"false".equals(skipFirstLevel)) {
            throw new ConflictException(
                format(
                    PARAMETRIZED_ILLEGAL_PARAMETER_VALUE_MESSAGE,
                    "project.source.parameters.skipFirstLevel",
                    entry.getValue()));
          }
          break;
        case "branch":
        case "startPoint":
        case "commitId":
        case "keepDir":
        case "fetch":
        case "branchMerge":
          break;
        default:
          throw new ConflictException(
              format(
                  PARAMETRIZED_INVALID_PARAMETER_MESSAGE,
                  "source.project.parameters." + entry.getKey(),
                  version));
      }
    }
  }
}

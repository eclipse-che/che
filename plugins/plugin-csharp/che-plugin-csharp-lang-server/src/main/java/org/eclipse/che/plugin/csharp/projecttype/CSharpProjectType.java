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
package org.eclipse.che.plugin.csharp.projecttype;

import com.google.inject.Inject;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.csharp.shared.Constants;

/**
 * C# project type
 *
 * @author Anatolii Bazko
 */
public class CSharpProjectType extends ProjectTypeDef {
  @Inject
  public CSharpProjectType() {
    super(Constants.CSHARP_PROJECT_TYPE_ID, "C# .NET Core", true, false, true);
    addConstantDefinition(Constants.LANGUAGE, "language", Constants.CSHARP_LANG);
  }
}

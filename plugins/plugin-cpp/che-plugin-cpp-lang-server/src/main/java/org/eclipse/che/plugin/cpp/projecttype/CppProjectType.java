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
package org.eclipse.che.plugin.cpp.projecttype;

import static org.eclipse.che.plugin.cpp.shared.Constants.CPP_LANG;
import static org.eclipse.che.plugin.cpp.shared.Constants.CPP_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.cpp.shared.Constants.LANGUAGE;

import com.google.inject.Inject;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;

/**
 * C++ project type
 *
 * @author Vitalii Parfonov
 */
public class CppProjectType extends ProjectTypeDef {
  @Inject
  public CppProjectType() {
    super(CPP_PROJECT_TYPE_ID, "C++", true, false, true);
    addConstantDefinition(LANGUAGE, "language", CPP_LANG);
  }
}

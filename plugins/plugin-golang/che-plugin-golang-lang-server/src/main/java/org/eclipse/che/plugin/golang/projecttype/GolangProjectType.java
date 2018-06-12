/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.golang.projecttype;

import static org.eclipse.che.plugin.golang.shared.Constants.GOLANG_PROJECT_TYPE_ID;
import static org.eclipse.che.plugin.golang.shared.Constants.LANGUAGE;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;

/** @author Eugene Ivantsov */
public class GolangProjectType extends ProjectTypeDef {

  public GolangProjectType() {
    super(GOLANG_PROJECT_TYPE_ID, "Golang", true, false, true);
    addConstantDefinition(LANGUAGE, LANGUAGE, GOLANG_PROJECT_TYPE_ID);
  }
}

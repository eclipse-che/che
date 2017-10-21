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
package org.eclipse.che.api.core.model.workspace.config;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.project.ProjectProblem;

/**
 * @author gazarenkov
 * @author Dmitry Shnurenko
 */
public interface ProjectConfig {
  String getName();

  String getPath();

  String getDescription();

  String getType();

  List<String> getMixins();

  Map<String, List<String>> getAttributes();

  SourceStorage getSource();

  List<? extends ProjectProblem> getProblems();
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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

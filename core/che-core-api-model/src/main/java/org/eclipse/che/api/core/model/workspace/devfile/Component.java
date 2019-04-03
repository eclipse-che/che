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
package org.eclipse.che.api.core.model.workspace.devfile;

import java.util.List;
import java.util.Map;

public interface Component {

  String getName();

  String getType();

  // openshift/kubernetes
  String getReference();

  String getReferenceContent();

  List<? extends Entrypoint> getEntrypoints();

  Map<String, String> getSelector();

  // dockerimage

  String getImage();

  String getMemoryLimit();

  boolean getMountSources();

  List<String> getCommand();

  List<String> getArgs();

  List<? extends Volume> getVolumes();

  List<? extends Env> getEnv();

  List<? extends Endpoint> getEndpoints();
}

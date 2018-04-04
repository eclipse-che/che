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
package org.eclipse.che.plugin.github.shared;

import org.eclipse.che.dto.shared.DTO;

/** @author Vladyslav Zhukovskii */
@DTO
public interface GitHubKey {
  int getId();

  void setId(int id);

  String getKey();

  void setKey(String key);

  String getUrl();

  void setUrl(String url);

  String getTitle();

  void setTitle(String title);
}

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
package org.eclipse.che.api.git.shared;

import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/**
 * Info received from push response
 *
 * @author Igor Vinokur
 */
@DTO
public interface PushResponse {

  /** set output message */
  void setCommandOutput(String commandOutput);

  /** @return output message */
  String getCommandOutput();

  PushResponse withCommandOutput(String commandOutput);

  /** set list of push updates */
  void setUpdates(List<Map<String, String>> updates);

  /** @return list of push updates */
  List<Map<String, String>> getUpdates();

  PushResponse withUpdates(List<Map<String, String>> updates);
}

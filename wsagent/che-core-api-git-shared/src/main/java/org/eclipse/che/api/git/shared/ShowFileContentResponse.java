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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Response of the show file content command.
 *
 * @author Igor Vinokur
 */
@DTO
public interface ShowFileContentResponse extends Log {
  /** @return content of the file */
  String getContent();

  /** set content of the file */
  void setContent(String content);

  /** @return response with established file content */
  ShowFileContentResponse withContent(String content);
}

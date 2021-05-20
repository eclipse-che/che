/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.workspace;

import java.util.Map;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Should be thrown when limit of some resource is exceeded e.g. the ram per workspace.
 *
 * @author Yevhenii Voevodin
 * @see LimitsCheckingWorkspaceManager
 */
public class LimitExceededException extends ServerException {

  public LimitExceededException(String message) {
    super(message);
  }

  public LimitExceededException(String message, Map<String, String> attributes) {
    super(
        DtoFactory.newDto(ExtendedError.class)
            .withMessage(message)
            .withAttributes(attributes)
            .withErrorCode(ErrorCodes.LIMIT_EXCEEDED));
  }
}

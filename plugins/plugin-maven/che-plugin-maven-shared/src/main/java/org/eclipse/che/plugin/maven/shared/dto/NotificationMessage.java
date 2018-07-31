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
package org.eclipse.che.plugin.maven.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Message for translating notification from <b>MavenServerNotifier</b>
 *
 * @author Evgen Vidolob
 */
@DTO
@Deprecated
public interface NotificationMessage {

  String getText();

  void setText(String text);

  double getPercent();

  void setPercent(double percent);

  boolean isPercentUndefined();

  void setPercentUndefined(boolean percentUndefined);
}

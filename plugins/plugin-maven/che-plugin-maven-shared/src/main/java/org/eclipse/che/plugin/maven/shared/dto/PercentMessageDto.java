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
package org.eclipse.che.plugin.maven.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/** Message for translating value of percent of resolving from <b>MavenServerNotifier</b>. */
@DTO
public interface PercentMessageDto extends MavenOutputEventDto {

  double getPercent();

  void setPercent(double percent);

  PercentMessageDto withPercent(double percent);
}

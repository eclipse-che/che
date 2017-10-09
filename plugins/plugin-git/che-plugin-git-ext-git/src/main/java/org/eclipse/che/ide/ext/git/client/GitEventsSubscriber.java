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
package org.eclipse.che.ide.ext.git.client;

import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;

/**
 * Describes git events.
 *
 * @author Mykola Morhun
 */
public interface GitEventsSubscriber {

  /** Invoked when a file which is added to git was changed */
  void onFileUnderGitChanged(String endpointId, FileChangedEventDto dto);

  /** Invoked when git status of project was changed */
  void onGitStatusChanged(String endpointId, StatusChangedEventDto dto);

  /** Invoked when git checkout was performed */
  void onGitCheckout(String endpointId, GitCheckoutEventDto dto);
}

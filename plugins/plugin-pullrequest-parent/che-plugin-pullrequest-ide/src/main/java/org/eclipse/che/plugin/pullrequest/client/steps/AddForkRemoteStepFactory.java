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
package org.eclipse.che.plugin.pullrequest.client.steps;

import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.plugin.pullrequest.client.workflow.Step;

/** @author Mihail Kuznyetsov */
public interface AddForkRemoteStepFactory {
  AddForkRemoteStep create(
      @Assisted("delegate") Step delegate, @Assisted("remoteUrl") String remoteUrl);
}

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
package org.eclipse.che.selenium.core.constant;

import com.google.inject.Singleton;

/**
 * Reflects values of environment variable CHE_INFRASTRUCTURE
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public enum Infrastructure {
  DOCKER,
  OPENSHIFT,
  K8S,
  OSIO
}

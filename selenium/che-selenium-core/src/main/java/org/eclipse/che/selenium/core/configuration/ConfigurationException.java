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
package org.eclipse.che.selenium.core.configuration;

/**
 * Exception happen in case of some problems with configuration.
 *
 * @author Sergii Kabashniuk
 */
public class ConfigurationException extends RuntimeException {
  public ConfigurationException(String message) {
    super(message);
  }
}

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
package org.eclipse.che.ide.ext.help.client;

import com.google.gwt.i18n.client.Constants;

/**
 * Represents application's build information.
 *
 * @author Ann Shumilova
 */
public interface BuildInfo extends Constants {

  @Key("revision")
  @DefaultStringValue("xxx")
  String revision();

  @Key("buildTime")
  @DefaultStringValue("just now")
  String buildTime();

  @Key("version")
  @DefaultStringValue("zzz")
  String version();
}

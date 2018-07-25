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
package org.eclipse.che.ide.ext.help.client.about;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization for About Codenvy dialog.
 *
 * @author Ann Shumilova
 */
public interface AboutLocalizationConstant extends Messages {
  @Key("about.version")
  String aboutVersion();

  @Key("about.revision")
  String aboutRevision();

  @Key("about.buildtime")
  String aboutBuildTime();

  @Key("about.control.title")
  String aboutControlTitle();
}

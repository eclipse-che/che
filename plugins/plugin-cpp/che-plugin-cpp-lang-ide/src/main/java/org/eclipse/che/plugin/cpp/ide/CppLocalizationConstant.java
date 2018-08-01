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
package org.eclipse.che.plugin.cpp.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants. Interface to represent the constants defined in resource bundle:
 * 'CppLocalizationConstant.properties'.
 *
 * @author Vitalii PArfonov
 */
public interface CppLocalizationConstant extends Messages {

  @Key("cpp.action.create.c.file.title")
  @DefaultMessage("New C File")
  String createCFileActionTitle();

  @Key("cpp.action.create.c.file.description")
  @DefaultMessage("Create C File")
  String createCFileActionDescription();

  @Key("cpp.action.create.h.file.title")
  @DefaultMessage("New H File")
  String createCHeaderFileActionTitle();

  @Key("cpp.action.create.c.file.description")
  @DefaultMessage("Create C Header File")
  String createCHeaderFileActionDescription();

  @Key("cpp.action.create.cpp.file.title")
  @DefaultMessage("New C++ File")
  String createCppFileActionTitle();

  @Key("cpp.action.create.cpp.file.description")
  @DefaultMessage("Create C++ File")
  String createCppFileActionDescription();
}

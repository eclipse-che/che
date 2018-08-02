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
package org.eclipse.che.ide.ext.java.client;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaFile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;

/**
 * Provides FQN of the Java-class which is opened in active editor.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CurrentClassFQN_Macro implements Macro {

  private static final String KEY = "${current.class.fqn}";
  private final AppContext appContext;
  private final JavaLocalizationConstant localizationConstants;

  @Inject
  public CurrentClassFQN_Macro(
      AppContext appContext, JavaLocalizationConstant localizationConstants) {
    this.appContext = appContext;
    this.localizationConstants = localizationConstants;
  }

  @Override
  public String getName() {
    return KEY;
  }

  @Override
  public String getDescription() {
    return localizationConstants.macroCurrentClassFQN_Description();
  }

  @Override
  public Promise<String> expand() {
    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length != 1) {
      return Promises.resolve("");
    }

    final Resource resource = resources[0];
    final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

    if (resource.getResourceType() == FILE && isJavaFile(resource) && srcFolder.isPresent()) {
      return Promises.resolve(JavaUtil.resolveFQN((Container) srcFolder.get(), resource));
    } else {
      return Promises.resolve("");
    }
  }
}

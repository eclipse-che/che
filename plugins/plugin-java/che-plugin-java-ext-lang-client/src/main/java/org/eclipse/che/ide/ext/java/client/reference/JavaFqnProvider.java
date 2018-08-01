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
package org.eclipse.che.ide.ext.java.client.reference;

import com.google.inject.Singleton;
import org.eclipse.che.ide.api.reference.FqnProvider;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;

/**
 * The class contains business logic which allows extract fqn for given resource.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class JavaFqnProvider implements FqnProvider {

  @Override
  public String getFqn(Object object) {

    if (object instanceof Resource) {
      return JavaUtil.resolveFQN((Resource) object);
    }

    return "";
  }
}

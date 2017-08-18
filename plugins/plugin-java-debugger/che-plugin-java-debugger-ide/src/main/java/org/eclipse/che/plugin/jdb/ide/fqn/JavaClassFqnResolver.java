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
package org.eclipse.che.plugin.jdb.ide.fqn;

import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.plugin.debugger.ide.fqn.FqnResolver;

/**
 * FQN resolver for {@link org.eclipse.che.ide.MimeType#APPLICATION_JAVA_CLASS} nodes.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class JavaClassFqnResolver implements FqnResolver {

  @NotNull
  @Override
  public String resolveFqn(@NotNull final VirtualFile file) {
    return file.getLocation().toString();
  }
}

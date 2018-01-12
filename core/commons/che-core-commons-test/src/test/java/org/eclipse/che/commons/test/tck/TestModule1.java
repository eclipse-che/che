/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.test.tck;

import com.google.inject.TypeLiteral;
import java.util.Collection;
import org.eclipse.che.commons.test.tck.TckComponentsTest.Entity;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;

/** @author Yevhenii Voevodin */
public class TestModule1 extends TckModule {

  @Override
  public void configure() {
    bind(new TypeLiteral<TckRepository<Entity>>() {})
        .toInstance(
            new TckRepository<Entity>() {
              @Override
              public void createAll(Collection<? extends Entity> entities)
                  throws TckRepositoryException {}

              @Override
              public void removeAll() throws TckRepositoryException {}
            });
  }
}

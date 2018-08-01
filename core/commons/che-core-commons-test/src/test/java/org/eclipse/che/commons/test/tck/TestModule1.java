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

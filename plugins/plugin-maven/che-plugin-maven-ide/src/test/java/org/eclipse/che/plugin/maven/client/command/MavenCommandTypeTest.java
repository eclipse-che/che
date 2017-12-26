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
package org.eclipse.che.plugin.maven.client.command;

import static org.junit.Assert.assertTrue;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Collection;
import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.java.client.MavenResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Artem Zatsarynnyi */
@RunWith(GwtMockitoTestRunner.class)
public class MavenCommandTypeTest {

  @Mock private MavenResources mavenResources;
  @Mock private MavenCommandPagePresenter mavenCommandPagePresenter;
  @Mock private IconRegistry iconRegistry;

  @InjectMocks private MavenCommandType mavenCommandType;

  @Test
  public void shouldReturnPages() throws Exception {
    Collection<CommandPage> pages = mavenCommandType.getPages();

    assertTrue(pages.contains(mavenCommandPagePresenter));
  }
}

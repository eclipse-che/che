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
package org.eclipse.che.ide.reference;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.reference.FqnProvider;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Dmitry Shnurenko */
@RunWith(MockitoJUnitRunner.class)
public class ShowReferencePresenterTest {

  private static final Path PATH = Path.valueOf("path");
  private static final String PROJECT_TYPE = "type";

  // constructor mocks
  @Mock private ShowReferenceView view;

  // additional mocks
  @Mock private FqnProvider provider;

  @Mock private Resource resource;
  @Mock private Project project;

  private ShowReferencePresenter presenter;

  @Before
  public void setUp() {
    Map<String, FqnProvider> providers = new HashMap<>();
    providers.put(PROJECT_TYPE, provider);

    presenter = new ShowReferencePresenter(view, providers);

    when(resource.getLocation()).thenReturn(PATH);
    when(resource.getRelatedProject()).thenReturn(Optional.of(project));
    when(project.getType()).thenReturn(PROJECT_TYPE);
  }

  @Test
  public void pathShouldBeShownForNodeWhichDoesNotHaveFqn() {

    Map<String, FqnProvider> providers = new HashMap<>();
    presenter = new ShowReferencePresenter(view, providers);

    presenter.show(resource);

    verify(provider, never()).getFqn(resource);
    verify(view).show("", PATH);
  }

  @Test
  public void pathAndFqnShouldBeShownForNode() {
    when(provider.getFqn(resource)).thenReturn("fqn");

    presenter.show(resource);

    verify(provider).getFqn(resource);
    verify(view).show("fqn", PATH);
  }
}

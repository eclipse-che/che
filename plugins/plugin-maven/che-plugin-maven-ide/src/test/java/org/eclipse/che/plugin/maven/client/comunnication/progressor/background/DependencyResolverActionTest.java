/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.client.comunnication.progressor.background;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.plugin.maven.client.MavenLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class DependencyResolverActionTest {
    @Mock
    private BackgroundLoaderPresenter dependencyResolver;
    @Mock
    private MavenLocalizationConstant locale;
    @Mock
    private Presentation              presentation;

    private DependencyResolverAction action;

    @Before
    public void setUp() throws Exception {
        action = new DependencyResolverAction(dependencyResolver, locale);
    }

    @Test
    public void constructorShouldBePerformed() throws Exception {
        verify(locale).loaderActionName();
        verify(locale).loaderActionDescription();

        verify(dependencyResolver).hide();
    }

    @Test
    public void customComponentShouldBeCreated() throws Exception {
        action.createCustomComponent(presentation);

        verify(dependencyResolver).getCustomComponent();
    }
}

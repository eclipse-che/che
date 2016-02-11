/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.settings.common;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.NotSupportedException;

import static org.eclipse.che.ide.settings.common.AbstractSettingsPagePresenter.DEFAULT_CATEGORY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractSettingsPagePresenterTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private ImageResource resource;

    private DummySettingsPagePresenter presenter;

    @Before
    public void setUp() {
        presenter = new DummySettingsPagePresenter(SOME_TEXT, SOME_TEXT, resource);
    }

    @Test
    public void titleShouldBeReturned() {
        String title = presenter.getTitle();

        assertThat(title, equalTo(SOME_TEXT));
    }

    @Test
    public void categoryShouldBeReturned() {
        String category = presenter.getCategory();

        assertThat(category, equalTo(SOME_TEXT));
    }

    @Test
    public void defaultCategoryAndNullIconShouldBeSet() {
        presenter = new DummySettingsPagePresenter(SOME_TEXT);

        String category = presenter.getCategory();
        ImageResource imageResource = presenter.getIcon();

        assertThat(category, equalTo(DEFAULT_CATEGORY));
        assertThat(imageResource, nullValue());
    }

    @Test
    public void iconShouldBeReturned() {
        ImageResource imageResource = presenter.getIcon();

        assertThat(imageResource, equalTo(resource));
    }

    private class DummySettingsPagePresenter extends AbstractSettingsPagePresenter {

        public DummySettingsPagePresenter(String title) {
            super(title);
        }

        public DummySettingsPagePresenter(String title, String category, ImageResource icon) {
            super(title, category, icon);
        }

        @Override
        public boolean isDirty() {
            throw new NotSupportedException(getClass() + "Method isn't supported in current class...");
        }

        @Override
        public void storeChanges() {
            throw new NotSupportedException(getClass() + "Method isn't supported in current class...");
        }

        @Override
        public void revertChanges() {
            throw new NotSupportedException(getClass() + "Method isn't supported in current class...");
        }

        @Override
        public void go(AcceptsOneWidget container) {
            throw new NotSupportedException(getClass() + "Method isn't supported in current class...");
        }
    }
}
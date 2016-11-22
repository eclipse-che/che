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
package org.eclipse.che.ide.editor.preferences;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.editor.preferences.editorproperties.propertiessection.EditorPropertiesSectionPresenter;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.EditorPreferenceSectionFactory;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.EditorPropertiesSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.LanguageToolsPropertiesSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.RulersPropertiesSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.TabsPropertiesSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.TypingPropertiesSection;
import org.eclipse.che.ide.editor.preferences.editorproperties.sections.WhiteSpacesPropertiesSection;
import org.eclipse.che.ide.editor.preferences.keymaps.KeyMapsPreferencePresenter;
import org.eclipse.che.ide.editor.preferences.keymaps.KeymapsPreferenceView;
import org.eclipse.che.ide.editor.preferences.keymaps.KeymapsPreferenceViewImpl;

/**
 * GIN module for configuring editor preferences.
 *
 * @author Artem Zatsarynnyi
 */
public class EditorPreferencesModule extends AbstractGinModule {

    @Override
    protected void configure() {
        GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class).addBinding().to(EditorPreferencePresenter.class);

        bind(EditorPreferenceView.class).to(EditorPreferenceViewImpl.class);
        bind(KeymapsPreferenceView.class).to(KeymapsPreferenceViewImpl.class);
        bind(KeyMapsPreferencePresenter.class);

        install(new GinFactoryModuleBuilder()
                        .implement(EditorPreferenceSection.class, EditorPropertiesSectionPresenter.class)
                        .build(EditorPreferenceSectionFactory.class));

        GinMultibinder<EditorPropertiesSection> editorPropertiesSectionBinder =
                GinMultibinder.newSetBinder(binder(), EditorPropertiesSection.class);

        editorPropertiesSectionBinder.addBinding().to(TabsPropertiesSection.class);
        editorPropertiesSectionBinder.addBinding().to(LanguageToolsPropertiesSection.class);
        editorPropertiesSectionBinder.addBinding().to(TypingPropertiesSection.class);
        editorPropertiesSectionBinder.addBinding().to(WhiteSpacesPropertiesSection.class);
        editorPropertiesSectionBinder.addBinding().to(RulersPropertiesSection.class);
    }
}

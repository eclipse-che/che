package org.eclipse.che.plugin.languageserver.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

import org.vectomatic.dom.svg.ui.SVGResource;

public interface LanguageServerResources extends ClientBundle {
    LanguageServerResources INSTANCE = GWT.create(LanguageServerResources.class);

    @Source("svg/file.svg")
    SVGResource file();

    @Source("svg/category.svg")
    SVGResource category();
}

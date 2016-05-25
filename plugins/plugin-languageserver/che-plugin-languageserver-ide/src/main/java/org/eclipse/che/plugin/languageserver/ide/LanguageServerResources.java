package org.eclipse.che.plugin.languageserver.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

public interface LanguageServerResources extends ClientBundle {

    LanguageServerResources INSTANCE = GWT.create(LanguageServerResources.class);

    @Source("languageserver.css")
    LSCss css();

    @Source("svg/file.svg")
    SVGResource file();

    @Source("svg/category.svg")
    SVGResource category();

    @Source("svg/taskmrk.svg")
    SVGResource taskMark();

    @Source("svg/mark-error.svg")
    SVGResource markError();

    @Source("svg/mark-warning.svg")
    SVGResource markWarning();

    @Source("svg/import.svg")
    SVGResource importItem();


    interface LSCss extends CssResource {

        @ClassName("overview-mark-warning")
        String overviewMarkWarning();

        @ClassName("overview-mark-error")
        String overviewMarkError();

        @ClassName("overview-mark-task")
        String overviewMarkTask();

        @ClassName("mark-element")
        String markElement();
    }
}

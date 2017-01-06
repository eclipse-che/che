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
package org.eclipse.che.ide.navigation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.window.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of {@link NavigateToFileView} view.
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NavigateToFileViewImpl extends Window implements NavigateToFileView {

    interface NavigateToFileViewImplUiBinder extends UiBinder<Widget, NavigateToFileViewImpl> {
    }

    @UiField
    Label errLabel;

    @UiField(provided = true)
    SuggestBox files;

    @UiField(provided = true)
    CoreLocalizationConstant locale;

    private ActionDelegate      delegate;
    private HandlerRegistration handlerRegistration;

    @Inject
    public NavigateToFileViewImpl(CoreLocalizationConstant locale, NavigateToFileViewImplUiBinder uiBinder) {
        this.locale = locale;
        setTitle(locale.navigateToFileViewTitle());
        files = new SuggestBox(new MySuggestOracle());

        files.getValueBox().addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (KeyCodes.KEY_ESCAPE == event.getNativeKeyCode()) {
                    close();
                }
            }
        });

        files.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                delegate.onFileSelected(Path.valueOf(event.getSelectedItem().getReplacementString()));
            }
        });

        Widget widget = uiBinder.createAndBindUi(this);
        setWidget(widget);
        getFooter().setVisible(false);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() {
        files.setEnabled(false);
        hide();
    }

    @Override
    public void showDialog() {
        errLabel.setText("");

        files.setEnabled(true);
        new Timer() {
            @Override
            public void run() {
                String warning = locale.navigateToFileSearchIsCaseSensitive();
                files.setText(warning);
                files.getValueBox().selectAll();

                files.setFocus(true);
            }
        }.schedule(300);

        handlerRegistration = files.getValueBox().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                String value = files.getText();
                String warning = locale.navigateToFileSearchIsCaseSensitive();

                if (value.equals(warning)) {
                    files.setText("");
                }
                handlerRegistration.removeHandler();
            }
        });
        super.show();
    }

    @Override
    public void clearInput() {
        files.getValueBox().setValue("");
    }

    private class MySuggestOracle extends SuggestOracle {
        @Override
        public boolean isDisplayStringHTML() {
            return true;
        }

        @Override
        public void requestSuggestions(final Request request, final Callback callback) {
            delegate.onRequestSuggestions(request.getQuery(), new AsyncCallback<List<Path>>() {
                @Override
                public void onSuccess(List<Path> result) {
                    errLabel.setText("");

                    final List<SuggestOracle.Suggestion> suggestions = new ArrayList<>(result.size());
                    for (final Path item : result) {
                        suggestions.add(new SuggestOracle.Suggestion() {
                            @Override
                            public String getDisplayString() {
                                return item.lastSegment() + " (" + item.removeLastSegments(1) + ")";
                            }

                            @Override
                            public String getReplacementString() {
                                return item.toString();
                            }
                        });
                    }

                    callback.onSuggestionsReady(request, new Response(suggestions));
                }

                @Override
                public void onFailure(Throwable caught) {
                    errLabel.setText(caught.getMessage());
                    // hide previous suggestion list in case any error
                    callback.onSuggestionsReady(request, new Response(new ArrayList<Suggestion>(0)));
                }
            });
        }
    }
}

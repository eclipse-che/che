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
package org.eclipse.che.ide.jseditor.client.preference.dataprovider;

import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

import java.util.List;

public class RefreshableListDataProvider<T> extends ListDataProvider<T> implements RefreshableDataProvider {

    public RefreshableListDataProvider() {
    }

    public RefreshableListDataProvider(final List<T> listToWrap) {
        super(listToWrap);
    }

    public RefreshableListDataProvider(final ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }

    public RefreshableListDataProvider(final List<T> listToWrap, final ProvidesKey<T> keyProvider) {
        super(listToWrap, keyProvider);
    }

}

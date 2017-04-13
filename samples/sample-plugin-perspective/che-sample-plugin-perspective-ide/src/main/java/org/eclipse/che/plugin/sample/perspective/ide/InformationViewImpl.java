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
package org.eclipse.che.plugin.sample.perspective.ide;

import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;

/**
 */
public class InformationViewImpl extends BaseView<InformationView.ActionDelegate> implements InformationView {

    /**
     * Constructor.
     *
     * @param resources the {@link PartStackUIResources}
     */
    @Inject
    public InformationViewImpl(PartStackUIResources resources){
        super(resources);
        Label label = new Label("Information Part :: Hello World!");
        setContentWidget(label);
    }
}

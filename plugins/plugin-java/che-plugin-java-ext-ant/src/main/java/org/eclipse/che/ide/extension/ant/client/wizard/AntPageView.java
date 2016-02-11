/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.ant.client.wizard;

import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

/** @author Vladyslav Zhukovskii */
@ImplementedBy(AntPageViewImpl.class)
public interface AntPageView extends View<AntPageView.ActionDelegate> {
    public interface ActionDelegate {
    }
}

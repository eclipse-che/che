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
package org.eclipse.che.plugin.pullrequest.client.dialogs.commit;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Widget;

/**
 * {@link com.google.gwt.uibinder.client.UiBinder} interface for the commit dialog.
 *
 * @author Kevin Pollet
 */
@UiTemplate("CommitViewImpl.ui.xml")
interface CommitViewUiBinder extends UiBinder<Widget, CommitViewImpl> {
}

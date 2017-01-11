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
package org.eclipse.che.ide.extension.machine.client;

import com.google.gwt.user.cellview.client.CellTable;

/**
 * Class contains references to resources which need to correct displaying of dialog box's table.
 *
 * @author Dmitry Shnurenko
 */
public interface TableResources extends CellTable.Resources {

    interface TableStyles extends CellTable.Style {

    }

    @Source("table.css")
    TableStyles cellTableStyle();
}

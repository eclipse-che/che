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
package org.eclipse.che.ide.ui.cellview;

import com.google.gwt.user.cellview.client.CellTable;

/**
 * @author Evgen Vidolob
 */
public interface CellTableResources extends CellTable.Resources {
    public interface CellTableStyle extends CellTable.Style {
    }


    @Override
    @Source({"cellTable.css", "org/eclipse/che/ide/api/ui/style.css"})
    CellTableStyle cellTableStyle();

}

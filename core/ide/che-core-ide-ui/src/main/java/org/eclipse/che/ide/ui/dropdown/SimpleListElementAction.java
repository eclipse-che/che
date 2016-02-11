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
package org.eclipse.che.ide.ui.dropdown;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;

import javax.validation.constraints.NotNull;

/**
 * The action which describes simple element of the custom drop down list.
 *
 * @author Valeriy Svydenko
 * @author Oleksii Orel
 */
public class SimpleListElementAction extends Action {
    private final String               id;
    private final String               name;
    private final DropDownHeaderWidget header;

    @Inject
    public SimpleListElementAction(@NotNull @Assisted("id") String id,
                                   @NotNull @Assisted("name") String name,
                                   @Assisted DropDownHeaderWidget header) {
        super(name);
        this.id = id;
        this.name = name;
        this.header = header;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (header != null) {
            header.selectElement(id);
        }
    }

    /** @return id of the element */
    @NotNull
    public String getId() {
        return id;
    }

    /** @return title of the element */
    @NotNull
    public String getName() {
        return name;
    }
}

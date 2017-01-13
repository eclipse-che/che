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
package org.eclipse.che.plugin.languageserver.ide.location;

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.languageserver.shared.lsapi.LocationDTO;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
@ImplementedBy(OpenLocationViewImpl.class)
public interface OpenLocationView extends View<OpenLocationView.ActionDelegate> {

    void setLocations(List<LocationDTO> locations);

    void setTitle(String title);

    interface ActionDelegate extends BaseActionDelegate{

        void onLocationSelected(LocationDTO location);
    }
}

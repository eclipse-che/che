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
package org.eclipse.che.ide.extension.maven.client.module;

import org.eclipse.che.ide.ui.Styles;
import com.google.gwt.resources.client.ClientBundle;

/**
 * @author Evgen Vidolob
 */
public interface CreateMavenModuleResources extends ClientBundle {

    @Source({"org/eclipse/che/ide/api/ui/style.css","org/eclipse/che/ide/ui/Styles.css"})
    Css css();

    public interface Css extends Styles{

    }
}

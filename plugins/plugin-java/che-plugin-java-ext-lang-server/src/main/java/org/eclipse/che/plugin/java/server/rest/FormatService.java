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
package org.eclipse.che.plugin.java.server.rest;

import com.google.inject.Inject;

import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.jdt.internal.corext.format.Formatter;
import org.eclipse.jface.text.BadLocationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Roman Nikitenko
 */
@Path("java/code-formatting")
public class FormatService {
    private Formatter formatter;

    @Inject
    public FormatService(Formatter formatter) {
        this.formatter = formatter;
    }

    @POST
    @Path("/format")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces({MediaType.APPLICATION_JSON})

    public List<Change> getFormatChanges(@QueryParam("offset") int offset,
                                         @QueryParam("length") int length,
                                         final String content) throws BadLocationException, IllegalArgumentException {
        return formatter.getFormatChanges(content, offset, length);
    }
}

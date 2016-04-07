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
package org.eclipse.che.ide.gdb.server.parser;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.debugger.shared.Breakpoint;
import org.eclipse.che.ide.ext.debugger.shared.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'run' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbContinue {

    private static final Pattern GDB_BREAKPOINT = Pattern.compile("Breakpoint .* at (.*):([0-9]*).*");

    private final Breakpoint breakpoint;

    public GdbContinue(Breakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }

    @Nullable
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    /**
     * Factory method.
     */
    public static GdbContinue parse(GdbOutput gdbOutput) throws GdbParseException {
        String output = gdbOutput.getOutput();

        for (String line : output.split("\n")) {
            Matcher matcher = GDB_BREAKPOINT.matcher(line);
            if (matcher.find()) {
                String file = matcher.group(1);
                String lineNumber = matcher.group(2);

                Location location = DtoFactory.newDto(Location.class);
                location.setClassName(file);
                location.setLineNumber(Integer.parseInt(lineNumber));

                Breakpoint breakpoint = DtoFactory.newDto(Breakpoint.class);
                breakpoint.setLocation(location);

                return new GdbContinue(breakpoint);
            }
        }

        return new GdbContinue(null);
    }
}

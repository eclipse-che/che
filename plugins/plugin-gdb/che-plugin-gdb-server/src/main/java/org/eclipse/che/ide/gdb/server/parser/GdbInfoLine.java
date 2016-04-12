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

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.debugger.shared.Location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 'info line' command parser.
 *
 * @author Anatoliy Bazko
 */
public class GdbInfoLine {

    private static final Pattern GDB_INFO_LINE = Pattern.compile("Line ([0-9]*) of \"(.*)\"\\s*starts at .*");

    private final Location location;

    public GdbInfoLine(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Factory method.
     */
    public static GdbInfoLine parse(GdbOutput gdbOutput) throws GdbParseException {
        String output = gdbOutput.getOutput();

        Matcher matcher = GDB_INFO_LINE.matcher(output);
        if (matcher.find()) {
            String lineNumber = matcher.group(1);
            String file = matcher.group(2);

            Location location = DtoFactory.newDto(Location.class);
            location.setClassName(file);
            location.setLineNumber(Integer.parseInt(lineNumber));

            return new GdbInfoLine(location);
        }

        throw new GdbParseException(GdbInfoLine.class, output);
    }
}

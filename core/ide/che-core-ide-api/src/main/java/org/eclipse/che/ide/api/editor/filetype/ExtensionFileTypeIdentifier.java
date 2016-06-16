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
package org.eclipse.che.ide.api.editor.filetype;

import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionFileTypeIdentifier implements FileTypeIdentifier {

    /** The known extensions registry. */
    Map<String, List<String>> mappings = new HashMap<>();

    public ExtensionFileTypeIdentifier() {
        init();
    }

    @Override
    public List<String> identifyType(final VirtualFile file) {
        final String filename = file.getName();
        if (filename != null) {
            final int dotPos = filename.lastIndexOf('.');
            if (dotPos < 1) { // either -1 (not found) or 0 (first position, for example .project or .codenvy etc.
                Log.debug(ExtensionFileTypeIdentifier.class, "File name has no suffix ");
                return null;
            }
            final String suffix = filename.substring(dotPos + 1);
            Log.debug(ExtensionFileTypeIdentifier.class, "Looking for a type for suffix " + suffix);
            List<String> result = mappings.get(suffix);
            Log.debug(ExtensionFileTypeIdentifier.class, "Found mime-types: " + printList(result));
            return result;
        }
        return null;
    }
    
    public void registerNewExtension(String extension, List<String> contentTypes) {
        if (mappings.containsKey(extension)) {
            Log.warn(ExtensionFileTypeIdentifier.class, "Replacing content types for extension '" + extension +"'.");
        }
        mappings.put(extension, contentTypes);
    }

    /** Prepares the know extension registry. */
    public void init() {
        this.mappings.put("c", Collections.singletonList("text/x-csrc"));
        this.mappings.put("C", Collections.singletonList("text/x-c++src"));
        this.mappings.put("cc", Collections.singletonList("text/x-c++src"));
        this.mappings.put("cpp", Collections.singletonList("text/x-c++src"));
        this.mappings.put("h", Collections.singletonList("text/x-chdr"));
        this.mappings.put("hh", Collections.singletonList("text/x-c++hdr"));
        this.mappings.put("c++", Collections.singletonList("text/x-c++src"));
        this.mappings.put("cs", Collections.singletonList("text/x-csharp"));
        this.mappings.put("m", Collections.singletonList("text/x-objective-c")); // conflict with octave/matlab

        this.mappings.put("java", Collections.singletonList("text/x-java"));
        this.mappings.put("class", Collections.singletonList("text/x-java"));
        this.mappings.put("scala", Collections.singletonList("text/x-scala"));
        this.mappings.put("sbt", Collections.singletonList("text/x-scala"));// scala build definition
        this.mappings.put("clj", Collections.singletonList("text/x-clojure"));
        this.mappings.put("groovy", Collections.singletonList("text/x-groovy"));
        this.mappings.put("gvy", Collections.singletonList("text/x-groovy"));
        this.mappings.put("gy", Collections.singletonList("text/x-groovy"));
        this.mappings.put("gradle", Collections.singletonList("text/x-groovy"));// gradle conf are groovy files
        this.mappings.put("kt", Collections.singletonList("text/x-kotlin"));

        this.mappings.put("js", Arrays.asList("application/javascript", "text/javascript"));
        this.mappings.put("coffee", Collections.singletonList("text/x-coffeescript"));
        this.mappings.put("json", Collections.singletonList("application/json"));
        this.mappings.put("ts", Arrays.asList("application/javascript", "application/typescript"));
        this.mappings.put("es6", Arrays.asList("application/javascript", "text/javascript"));
        this.mappings.put("jsx", Arrays.asList("application/javascript", "text/javascript"));

        this.mappings.put("css", Collections.singletonList("text/css"));
        this.mappings.put("scss", Collections.singletonList("text/x-scss"));
        this.mappings.put("less", Collections.singletonList("text/x-less"));
        this.mappings.put("sass", Collections.singletonList("text/x-sass"));

        this.mappings.put("xml", Collections.singletonList("application/xml"));
        this.mappings.put("xml", Collections.singletonList("application/xml"));
        this.mappings.put("html", Collections.singletonList("text/html"));
        this.mappings.put("xhtml", Arrays.asList("application/xml+xhtml", "text/html"));
        this.mappings.put("htm", Collections.singletonList("text/html"));
        this.mappings.put("dtd", Collections.singletonList("application/xml-dtd"));

        this.mappings.put("yaml", Collections.singletonList("text/x-yaml"));
        this.mappings.put("yml", Collections.singletonList("text/x-yaml"));

        this.mappings.put("markdown", Collections.singletonList("text/x-markdown"));
        this.mappings.put("mdown", Collections.singletonList("text/x-markdown"));
        this.mappings.put("mkdn", Collections.singletonList("text/x-markdown"));
        this.mappings.put("mkd", Collections.singletonList("text/x-markdown"));
        this.mappings.put("md", Collections.singletonList("text/x-markdown"));
        this.mappings.put("mdwn", Collections.singletonList("text/x-markdown"));
        this.mappings.put("rest", Collections.singletonList("text/x-rst"));// although most are suffixed with .txt
        this.mappings.put("rst", Collections.singletonList("text/x-rst"));
        this.mappings.put("tex", Collections.singletonList("text/x-latex"));
        this.mappings.put("cls", Collections.singletonList("text/x-latex"));
        this.mappings.put("sty", Collections.singletonList("text/x-latex"));

        this.mappings.put("py", Collections.singletonList("text/x-python"));
        this.mappings.put("pyx", Collections.singletonList("text/x-cython"));
        this.mappings.put("rb", Collections.singletonList("text/x-ruby"));
        this.mappings.put("gemspec", Collections.singletonList("text/x-ruby"));
        this.mappings.put("go", Collections.singletonList("text/x-go"));
        this.mappings.put("rs", Collections.singletonList("text/x-rustsrc"));
        this.mappings.put("erl", Collections.singletonList("text/x-erlang"));
        this.mappings.put("lua", Collections.singletonList("text/x-lua"));
        this.mappings.put("tcl", Collections.singletonList("text/x-tcl"));
        this.mappings.put("pl", Collections.singletonList("text/x-perl"));
        this.mappings.put("pm", Collections.singletonList("text/x-perl"));// perl module

        this.mappings.put("php", Collections.singletonList("text/x-php"));
        this.mappings.put("ejs", Collections.singletonList("application/x-ejs"));
        this.mappings.put("jsp", Collections.singletonList("application/x-jsp"));
        this.mappings.put("asp", Collections.singletonList("application/x-aspx"));
        this.mappings.put("aspx", Collections.singletonList("application/x-aspx"));
        this.mappings.put("slim", Collections.singletonList("text/x-slim"));

        this.mappings.put("ml", Collections.singletonList("text/x-ocaml"));
        this.mappings.put("fs", Collections.singletonList("text/x-fsharp"));
        this.mappings.put("lisp", Collections.singletonList("text/x-commonlisp"));
        this.mappings.put("cl", Collections.singletonList("text/x-commonlisp"));
        this.mappings.put("hs", Collections.singletonList("text/x-haskell"));
        this.mappings.put("scm", Collections.singletonList("text/x-scheme"));

        this.mappings.put("sql", Collections.singletonList("text/x-sql"));
        this.mappings.put("properties", Collections.singletonList("text/x-properties"));
        this.mappings.put("diff", Collections.singletonList("text/x-diff"));
        this.mappings.put("r", Collections.singletonList("text/x-rsrc"));
        this.mappings.put("R", Collections.singletonList("text/x-rsrc"));
        this.mappings.put("csv", Collections.singletonList("text/csv"));
        this.mappings.put("sh", Collections.singletonList("text/x-sh"));// many are not suffixed at all !

        this.mappings.put("pas", Collections.singletonList("text/x-pascal"));
        this.mappings.put("p", Collections.singletonList("text/x-pascal"));
        this.mappings.put("fpm", Collections.singletonList("text/x-pascal"));// turbo pascal modules
        this.mappings.put("st", Collections.singletonList("text/x-stsrc"));// smalltalk

        this.mappings.put("cob", Collections.singletonList("text/x-cobol"));
        this.mappings.put("cbl", Collections.singletonList("text/x-cobol"));
        this.mappings.put("cpy", Collections.singletonList("text/x-cobol"));
        this.mappings.put("f", Collections.singletonList("text/x-fortran"));
        this.mappings.put("for", Collections.singletonList("text/x-fortran"));
        this.mappings.put("f90", Collections.singletonList("text/x-fortran"));
        this.mappings.put("f95", Collections.singletonList("text/x-fortran"));
        this.mappings.put("f03", Collections.singletonList("text/x-fortran"));
        this.mappings.put("vb", Collections.singletonList("text/x-vb"));
        this.mappings.put("vbs", Collections.singletonList("text/vbscript"));

        this.mappings.put("pp", Collections.singletonList("text/x-puppet"));
        this.mappings.put("docker", Collections.singletonList("text/x-dockerfile"));
        this.mappings.put("jag", Collections.singletonList("text/jaggery"));
    }

    /**
     * Format a list of String for logging.
     *
     * @param strings
     *         le list to display
     * @return a representation of the list
     */
    private String printList(final List<String> strings) {
        final StringBuilder sb = new StringBuilder("[");
        if (strings != null && !strings.isEmpty()) {
            sb.append(strings.get(0));

            if (strings.size() > 1) {
                for (String string : strings.subList(1, strings.size())) {
                    sb.append(", ");
                    sb.append(string);
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

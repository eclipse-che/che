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
package org.eclipse.che.ide.api.editor.filetype;

import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
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
            if (dotPos < 1) { // either -1 (not found) or 0 (first position, for example .project or .che etc.
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
        this.mappings.put("c", makeList("text/x-csrc"));
        this.mappings.put("C", makeList("text/x-c++src"));
        this.mappings.put("cc", makeList("text/x-c++src"));
        this.mappings.put("cpp", makeList("text/x-c++src"));
        this.mappings.put("ino", makeList("text/x-c++src"));
        this.mappings.put("h", makeList("text/x-chdr"));
        this.mappings.put("hh", makeList("text/x-c++hdr"));
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

        this.mappings.put("js", makeList("application/javascript", "text/javascript"));
        this.mappings.put("coffee", makeList("text/x-coffeescript"));
        this.mappings.put("json", makeList("application/json"));
        this.mappings.put("ts", makeList("application/javascript", "application/typescript"));
        this.mappings.put("es6", makeList("application/javascript", "text/javascript"));
        this.mappings.put("jsx", makeList("application/javascript", "text/javascript"));

        this.mappings.put("css", makeList("text/css"));
        this.mappings.put("scss", makeList("text/x-scss"));
        this.mappings.put("less", makeList("text/x-less"));
        this.mappings.put("sass", makeList("text/x-sass"));

        this.mappings.put("xml", makeList("application/xml"));
        this.mappings.put("xml", makeList("application/xml"));
        this.mappings.put("html", makeList("text/html"));
        this.mappings.put("xhtml", makeList("application/xml+xhtml", "text/html"));
        this.mappings.put("htm", makeList("text/html"));
        this.mappings.put("dtd", makeList("application/xml-dtd"));

        this.mappings.put("yaml", makeList("text/x-yaml"));
        this.mappings.put("yml", makeList("text/x-yaml"));

        this.mappings.put("markdown", makeList("text/x-markdown"));
        this.mappings.put("mdown", makeList("text/x-markdown"));
        this.mappings.put("mkdn", makeList("text/x-markdown"));
        this.mappings.put("mkd", makeList("text/x-markdown"));
        this.mappings.put("md", makeList("text/x-markdown"));
        this.mappings.put("mdwn", makeList("text/x-markdown"));
        this.mappings.put("rest", makeList("text/x-rst"));// although most are suffixed with .txt
        this.mappings.put("rst", makeList("text/x-rst"));
        this.mappings.put("tex", makeList("text/x-latex"));
        this.mappings.put("cls", makeList("text/x-latex"));
        this.mappings.put("sty", makeList("text/x-latex"));

        this.mappings.put("py", makeList("text/x-python"));
        this.mappings.put("pyx", makeList("text/x-cython"));
        this.mappings.put("rb", makeList("text/x-ruby"));
        this.mappings.put("erb", makeList("text/html"));//templates with embedded ruby
        this.mappings.put("gemspec", makeList("text/x-ruby"));
        this.mappings.put("go", makeList("text/x-go"));
        this.mappings.put("rs", makeList("text/x-rustsrc"));
        this.mappings.put("erl", makeList("text/x-erlang"));
        this.mappings.put("lua", makeList("text/x-lua"));
        this.mappings.put("tcl", makeList("text/x-tcl"));
        this.mappings.put("pl", makeList("text/x-perl"));
        this.mappings.put("pm", makeList("text/x-perl"));// perl module

        this.mappings.put("php", makeList("text/x-php"));
        this.mappings.put("phtml", makeList("text/x-php"));
        this.mappings.put("ejs", makeList("application/x-ejs"));
        this.mappings.put("jsp", makeList("application/x-jsp"));
        this.mappings.put("asp", makeList("application/x-aspx"));
        this.mappings.put("aspx", makeList("application/x-aspx"));
        this.mappings.put("slim", makeList("text/x-slim"));

        this.mappings.put("ml", makeList("text/x-ocaml"));
        this.mappings.put("fs", makeList("text/x-fsharp"));
        this.mappings.put("lisp", makeList("text/x-commonlisp"));
        this.mappings.put("cl", makeList("text/x-commonlisp"));
        this.mappings.put("hs", makeList("text/x-haskell"));
        this.mappings.put("scm", makeList("text/x-scheme"));

        this.mappings.put("sql", makeList("text/x-sql"));
        this.mappings.put("properties", makeList("text/x-properties"));
        this.mappings.put("diff", makeList("text/x-diff"));
        this.mappings.put("r", makeList("text/x-rsrc"));
        this.mappings.put("R", makeList("text/x-rsrc"));
        this.mappings.put("csv", makeList("text/csv"));
        this.mappings.put("sh", makeList("text/x-sh"));// many are not suffixed at all !

        this.mappings.put("pas", makeList("text/x-pascal"));
        this.mappings.put("p", makeList("text/x-pascal"));
        this.mappings.put("fpm", makeList("text/x-pascal"));// turbo pascal modules
        this.mappings.put("st", makeList("text/x-stsrc"));// smalltalk

        this.mappings.put("cob", makeList("text/x-cobol"));
        this.mappings.put("cbl", makeList("text/x-cobol"));
        this.mappings.put("cpy", makeList("text/x-cobol"));
        this.mappings.put("f", makeList("text/x-fortran"));
        this.mappings.put("for", makeList("text/x-fortran"));
        this.mappings.put("f90", makeList("text/x-fortran"));
        this.mappings.put("f95", makeList("text/x-fortran"));
        this.mappings.put("f03", makeList("text/x-fortran"));
        this.mappings.put("vb", makeList("text/x-vb"));
        this.mappings.put("vbs", makeList("text/vbscript"));

        this.mappings.put("pp", makeList("text/x-puppet"));
        this.mappings.put("docker", makeList("text/x-dockerfile"));
        this.mappings.put("jag", makeList("text/jaggery"));
    }

    /**
     * Builds a list from the parameters.
     *
     * @param strings
     *         the elements of the list
     * @return the list
     */
    private List<String> makeList(final String... strings) {
        final List<String> result = new ArrayList<>();
        for (String string : strings) {
            result.add(string);
        }
        return result;
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

            for (String string : strings.subList(1, strings.size())) {
                sb.append(", ");
                sb.append(string);
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

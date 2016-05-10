Use antlr 3.3 to generate parser and lexer for expressions.

java -cp ${PATH_TO_ANTLR}/antlr-3.3-complete.jar:$JAVA_HOME/lib/tools.jar org.antlr.Tool -o ../java org/eclipse/che/ide/ext/java/jdi/server/expression/Java.g org/eclipse/che/ide/ext/java/jdi/server/expression/JavaTreeParser.g
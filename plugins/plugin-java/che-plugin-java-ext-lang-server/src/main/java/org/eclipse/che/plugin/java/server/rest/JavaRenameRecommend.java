package org.eclipse.che.plugin.java.server.rest;

import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;
import org.eclipse.che.plugin.java.server.rest.recommend.model.ResultModel;
import org.eclipse.che.plugin.java.server.rest.recommend.parser.Parser;

public class JavaRenameRecommend {

    public static ResultModel result;
    private static IJavaElement javaElement;

    public JavaRenameRecommend() {
        result=null;
    }

    public void recommend(DataModel renameData){
        result=new ResultModel();
        Parser parser = new Parser();
        parser.parse(renameData);
        result = parser.result;
    }
}

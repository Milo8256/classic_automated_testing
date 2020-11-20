public class CreateGraph {
    /**
    * @Description 此方法用来生成调用图,主要是调用Selection.getDot()
    * @return void
    **/
    public static void getGraph() {
        String[] tasks = {"0-CMD","1-ALU","2-DataLog","3-BinaryHeap","4-NextDay","5-MoreTriangle"};
        String path = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\";
        for (String task : tasks) {
            String projectPath = path + task;
            String targetFile = ".\\class-"+task+".dot";
            Selection selection = Util.createSelection(projectPath,"","-c");
            if(selection==null){
                return ;
            }
            selection.getDot(targetFile);
            selection = Util.createSelection(projectPath,"","-m");
            targetFile = ".\\method-"+task+".dot";
            selection.getDot(targetFile);
        }

    }
}

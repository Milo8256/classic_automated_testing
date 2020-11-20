public class CreateGraph {
    public static void getGraph() {
        String[] tasks = {"0-CMD","1-ALU","2-DataLog","3-BinaryHeap","4-NextDay","5-MoreTriangle"};
        String path = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\";
        for (String task : tasks) {
            String projectPath = path + task;
            String targetFile = ".\\class-"+task+".dot";
            Selection selection = Until.createSelection(projectPath,"","-c");
            if(selection==null){
                return ;
            }
            selection.getDot(targetFile);
            selection = Until.createSelection(projectPath,"","-m");
            targetFile = ".\\method-"+task+".dot";
            selection.getDot(targetFile);
        }

    }
}

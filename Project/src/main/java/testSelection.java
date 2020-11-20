public class testSelection {

    /**
    * @Description 此函数为主要方法,调用Util.getSelction()获取不同粒度的文件
     * @Param: projectPath 项目的绝对路径
    * @Param: changeInfoPath changinfo的绝对路径
    * @Param: level 选择方式的粒度,分为-m(方法级)-c(类级)
    * @return void
    **/

    public void run(String projectPath,String changeInfoPath,String level) {
        Selection selection = Util.createSelection(projectPath,changeInfoPath,level);
        if(selection==null){
            return ;
        }
        selection.getSelection();

    }


    public static void main(String[] args) {
        testSelection test = new testSelection();
        if(args.length!=3){
            System.out.println("Something wrong with command ");
            return;
        }
        //args[0] : -m/-c
        //args[1] : project path
        //args[2] : changeInfo.txt path

        if(!"-c".equals(args[0])&&!"-m".equals(args[0])){
            System.out.println("can't solve option : " + args[0] + "\n" +
                                "please use -c or -m \n"+
                                "-c: class level test case selection .\n" +
                                "-m: method level test case selection .\n");
            return ;
        }

        test.run(args[1],args[2],args[0]);

        /************************* create graph for 6 tasks **********************************/

//        CreateGraph.getGraph();

    }


}
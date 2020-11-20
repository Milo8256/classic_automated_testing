import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrikeCT.InvalidClassFileException;

import java.util.*;

public class SelectionClass implements Selection{
    //分析域
    private AnalysisScope scope;
    //生产class绝对路径
    private String srcClassPath;
    //测试class的绝对路径
    private String testPath;
    //调用图
    private CallGraph callGraph;
    //合法方法邻接矩阵图
    private Map<String,Set<String>> validClassGraph;
    //改变的方法
    private Map<String,Set<String>> changeInfoMap;
    //改变的生产方法
    private Set<String> changedClass;
    //选择的测试方法,保存的是签名
    private Set<String> selectedClass;


    public SelectionClass(String projectPath,String changeInfoPath) {
        this.srcClassPath = projectPath+"\\target\\classes\\net\\mooctest";
        this.testPath = projectPath+"\\target\\test-classes\\net\\mooctest";
        scope = Util.getScope(srcClassPath);
        if(!"".equals(changeInfoPath)) changeInfoMap = Util.loadChangeInfo(changeInfoPath);
    }

    /**
     * @Description 获取类级的测试用例
     *              1. 获取相应生产代码的scope
     *              2. 获取生产代码的调用图,使用邻接矩阵存储
     *              3. 使用dfs算法,获取需要测试的方法
     *              4. 添加测试用例到scope
     *              5. 获取所有代码的调用图,使用邻接矩阵存储
     *              6. 通过dfs算法,选择测试类
     *              7. 将测试类保存到相应的文件中
     * @return void
     **/
    @Override
    public void getSelection() {
        System.out.println("start analyse in class level .");
        callGraph = Util.getCallGraph(scope);
        validClassGraph = getValidClassGraph();
        changedClass = getChangedClass();
        Util.addScope(testPath,scope);
        callGraph = Util.getCallGraph(scope);
        validClassGraph = getValidClassGraph();
        selectedClass = getSelectedTest();
        Set<String> selected = getSelected();
        System.out.println("analyse in class level done ! ");
        Util.store(selected,".\\selection-class.txt");
    }


    @Override
    public void getDot(String targetFile){
        System.out.println("start analyse in class level .");
//        callGraph = Until.getCallGraph(scope);
        Util.addScope(testPath,scope);
        callGraph = Util.getCallGraph(scope);
        validClassGraph = getValidClassGraph();
        StringBuilder res = new StringBuilder();
        res.append("digraph {\n");
        for(String callee:validClassGraph.keySet()){
            for(String caller:validClassGraph.get(callee)){
                res.append("\""+callee+"\" -> \""+caller+"\";\n");
            }
        }
        res.append("}");
        Util.store(res.toString(),targetFile);
    }

    /**
     * @Description 已经获得相应的测试类,将测试类中的测试方法全部选择出来
     * @Param:
     * @return Set<String>
     **/
    private Set<String> getSelected() {
        HashSet<String> res = new HashSet<>();
        for(CGNode node: callGraph) {
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if(node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                String className = method.getDeclaringClass().getName().toString();
                if(selectedClass.contains(className)&&!method.getSignature().contains("init")){
                    res.add(className + " " + method.getSignature());
                }

            }
        }
        return res;
    }

    private Set<String> getSelectedTest() {
        HashSet<String> res = new HashSet<>();
        for(String changed:changedClass){
            getChangedClassHelper(validClassGraph,changed,res);
        }
        //挑选出的类中包含生产用例,此处消除生产用例,只保留测试用例
        res.removeAll(changedClass);
        return res;
    }

    private Set<String> getChangedClass() {
        System.out.println("get changed class ");
        HashSet<String> res = new HashSet<>();
        for(String changedClass:changeInfoMap.keySet()){
            getChangedClassHelper(validClassGraph,changedClass,res);
        }
        System.out.println("get changed class done");
        return res;
    }

    private void getChangedClassHelper(Map<String, Set<String>> validClassGraph, String changedClass, HashSet<String> res) {
        if(!validClassGraph.containsKey(changedClass)){
            res.add(changedClass);
            return;
        }
        for (String callerClass : validClassGraph.get(changedClass)) {
            if(res.contains(callerClass))continue;
            res.add(callerClass);
            getChangedClassHelper(validClassGraph,callerClass,res);
        }
    }

    /**
     * @Description 选取callgraph中,有关生产代码的节点,使用邻接矩阵存储
     * @return Map<String,Set<String>> 保存相关调用的邻接矩阵
     **/
    private Map<String,Set<String>>  getValidClassGraph(){
//        System.out.println("get valid class graph");
        HashMap<String, Set<String>> callMap = new HashMap<String, Set<String>>();
        for(CGNode node: callGraph) {
            if(node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if(!"Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    continue;
                }
                Collection<CallSiteReference> callSites = null;
                try {
                    callSites = method.getCallSites();
                    if(callSites==null)continue;
                    String callerName = method.getDeclaringClass().getName().toString();
                    if(callerName.contains("$"))continue;
                    for(CallSiteReference callee: callSites){
                        String calleeName = callee.getDeclaredTarget().getDeclaringClass().getName().toString();
                        if(!calleeName.contains("mooctest"))continue;
                        if(calleeName.contains("$"))continue;
                        if(callMap.containsKey(calleeName)){
                            callMap.get(calleeName).add(callerName);
                        }else{
                            HashSet<String> callers = new HashSet<String>();
                            callers.add(callerName);
                            callMap.put(calleeName,callers);
                        }
                    }
                } catch (InvalidClassFileException e) {
                    e.printStackTrace();
                }
            }
        }
        return callMap;
    }




}

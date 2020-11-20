import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrikeCT.InvalidClassFileException;

import java.util.*;

public class SelectionClass implements Selection{
//    private String projectPath;
//    private String changeInfoPath;
    private AnalysisScope scope;
    private String srcClassPath;
    private String testPath;
    private CallGraph callGraph;
    private Map<String,Set<String>> validClassGraph;
    private Map<String,Set<String>> changeInfoMap;
    private Set<String> changedClass;
    private Set<String> selectedClass;


    public SelectionClass(String projectPath,String changeInfoPath) {
//        this.projectPath = projectPath;
//        this.changeInfoPath = changeInfoPath;
        this.srcClassPath = projectPath+"\\target\\classes\\net\\mooctest";
        this.testPath = projectPath+"\\target\\test-classes\\net\\mooctest";
        scope = Until.getScope(srcClassPath);
        if(!"".equals(changeInfoPath)) changeInfoMap = Until.loadChangeInfo(changeInfoPath);
    }


    @Override
    public void getSelection() {
        System.out.println("start analyse in class level .");
        callGraph = Until.getCallGraph(scope);
        validClassGraph = getValidClassGraph();
        changedClass = getChangedClass();
        Until.addScope(testPath,scope);
        callGraph = Until.getCallGraph(scope);
        validClassGraph = getValidClassGraph();
        selectedClass = getSelectedTest();
        Set<String> selected = getSelected();
        System.out.println("analyse in class level done ! ");
        Until.store(selected,".\\selection-class.txt");
    }

    @Override
    public void getDot(String targetFile){
        System.out.println("start analyse in class level .");
//        callGraph = Until.getCallGraph(scope);
        Until.addScope(testPath,scope);
        callGraph = Until.getCallGraph(scope);
        validClassGraph = getValidClassGraph();
        StringBuilder res = new StringBuilder();
        res.append("digraph {\n");
        for(String callee:validClassGraph.keySet()){
            for(String caller:validClassGraph.get(callee)){
                res.append("\""+callee+"\" -> \""+caller+"\";\n");
            }
        }
        res.append("}");
        Until.store(res.toString(),targetFile);
    }

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

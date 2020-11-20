import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrikeCT.InvalidClassFileException;

import java.util.*;

public class SelectionMethod implements Selection{
//    String projectPath;
//    String changeInfoPath;
    private AnalysisScope scope;
    private String srcClassPath;
    private String testPath;
    private CallGraph callGraph;
    private Map<String,Set<String>> validMethodGraph;
    private Map<String,Set<String>> changeInfoMap;
    private Set<String> changedMethod;
    private Set<String> selectedMethod;

    public SelectionMethod(String projectPath,String changeInfoPath) {
        //        this.projectPath = projectPath;
//        this.changeInfoPath = changeInfoPath;
        this.srcClassPath = projectPath+"\\target\\classes\\net\\mooctest";
        this.testPath = projectPath+"\\target\\test-classes\\net\\mooctest";
        scope = Until.getScope(srcClassPath);
        if(!"".equals(changeInfoPath))
        changeInfoMap = Until.loadChangeInfo(changeInfoPath);
    }

    @Override
    public void getDot(String targetFile) {
        callGraph = Until.getCallGraph(scope);
//        validMethodGraph = getValidMethodGraph();
        Until.addScope(testPath,scope);
        callGraph = Until.getCallGraph(scope);
        validMethodGraph = getValidMethodGraph();
        StringBuilder res = new StringBuilder();
        res.append("digraph {\n");
        for(String callee:validMethodGraph.keySet()){
            for(String caller:validMethodGraph.get(callee)){
                res.append("\""+callee+"\" -> \""+caller+"\";\n");
            }
        }
        res.append("}");
        Until.store(res.toString(),targetFile);
    }

    @Override
    public void getSelection() {
        System.out.println("start analyse in method level .");
        callGraph = Until.getCallGraph(scope);
        validMethodGraph = getValidMethodGraph();
        changedMethod = getChangedMethod();
        Until.addScope(testPath,scope);
        callGraph = Until.getCallGraph(scope);
        validMethodGraph = getValidMethodGraph();
        selectedMethod = getSelectedMethod();
        Set<String> selected = getSelected();
        System.out.println("analyse in method level done ! ");
        Until.store(selected,".\\selection-method.txt");
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
                String methodSignature = method.getSignature();
                if(selectedMethod.contains(methodSignature)&&!methodSignature.contains("init")){
//                if(selectedMethod.contains(methodSignature)){
                    res.add(className + " " + methodSignature);
                }

            }
        }
        return res;
    }

    private Set<String> getSelectedMethod() {
        HashSet<String> res = new HashSet<>();
        for(String changed:changedMethod){
            getChangedMethodHelper(validMethodGraph,changed,res);
        }
        res.removeAll(changedMethod);
        return res;
    }

    private Set<String> getChangedMethod() {
        HashSet<String> res = new HashSet<>();
        for(String changedClass:changeInfoMap.keySet()){
            for(String changedMethod:changeInfoMap.get(changedClass)){
                getChangedMethodHelper(validMethodGraph,changedMethod,res);
            }
        }
        return res;
    }

    private void getChangedMethodHelper(Map<String, Set<String>> validMethodGraph, String changedMethod, HashSet<String> res) {
        if(!validMethodGraph.containsKey(changedMethod)){
            res.add(changedMethod);
            return;
        }
        res.add(changedMethod);
        for (String callerMethod : validMethodGraph.get(changedMethod)) {
            if(res.contains(callerMethod))continue;
            getChangedMethodHelper(validMethodGraph,callerMethod,res);
        }
    }

    private Map<String, Set<String>> getValidMethodGraph() {
        Map<String, Set<String>> callMap = new HashMap<String, Set<String>>();
        for(CGNode node: callGraph) {
            if(node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if(!"Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    continue;
                }
                Collection<CallSiteReference> callSites = null;
                try {
                    callSites = method.getCallSites();
                    if(callSites==null)continue;
                    String callerSignature = method.getSignature();
                    for(CallSiteReference callee: callSites){
                        String calleeName = callee.getDeclaredTarget().getDeclaringClass().getName().toString();
                        if(!calleeName.contains("mooctest"))continue;
                        String calleeSignature = callee.getDeclaredTarget().getSignature();
                        String className = calleeSignature.split("\\(")[0];
                        if(className.contains("$"))continue;
                        if(calleeSignature.contains("init"))continue;
                        if(callMap.containsKey(calleeSignature)){
                            callMap.get(calleeSignature).add(callerSignature);
                        }else{
                            HashSet<String> callers = new HashSet<String>();
                            callers.add(callerSignature);
                            callMap.put(calleeSignature,callers);
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

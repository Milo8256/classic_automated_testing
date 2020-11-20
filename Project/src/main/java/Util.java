import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Util {
    /**
    * @Description  此方法是一个工厂方法,按照参数的粒度,分别返回相应的类级
     *              -m 返回方法级选择,
     *              -c 返回类级选择
     *              其他 报错
     * @Param: projectPath 项目的绝对路径
    * @Param: changeInfoPath  changeInfo.txt的绝对路径
    * @Param: level 粒度选择,合法的为-c或者-m
    * @return Selection 返回相应的参数
    **/
    public static Selection createSelection(String projectPath, String changeInfoPath, String level){
        switch (level){
            case "-m":
                System.out.println("create selectionMethod class");
                return new SelectionMethod(projectPath,changeInfoPath);
            case "-c":
                System.out.println("create selectionClass class");
                return new SelectionClass(projectPath,changeInfoPath);
            default:
                System.out.println("ERROR : something wrong in get Selection");
                return null;
        }
    }

    /**
    * @Description 获取scope对象,调用readJavaScope方法,读取scope.txt,exclusion.txt
     * @Param: path  需要添加进scope的class文件的绝对路径
    * @return com.ibm.wala.ipa.callgraph.AnalysisScope
    **/
    public static AnalysisScope getScope(String path){
        ClassLoader classloader = Util.class.getClassLoader();
        try {
            AnalysisScope scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), classloader);
            addScope(path,scope);
            return scope;
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("something wrong while creating scope .");
        return null;
    }
    
    /**
    * @Description 添加新的class文件进入已经创建好的scope中
     * @Param: path class文件的绝对路径
    * @Param: scope 需要添加class的scope分析域
    * @return void
    **/

    public static void addScope(String path,AnalysisScope scope){
        try {
            File project = new File(path);
            File[] classFileList = project.listFiles();
            for (File file : classFileList) {
                System.out.println("load " + file.getName() + " to scope .");
                scope.addClassFileToScope(ClassLoaderReference.Application,file);
            }
            return ;
        }catch (InvalidClassFileException e){
            e.printStackTrace();
        }
        System.out.println("something wrong while creating scope .");
        return;
    }
    
    /**
    * @Description 获取scope的callGraph
     * @Param: scope
    * @return com.ibm.wala.ipa.callgraph.CallGraph
    **/
    public static CallGraph getCallGraph(AnalysisScope scope) {
//        System.out.println("try to get call graph");
        try{
            // 1.生成类层次关系对象
            ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
            // 2.生成进入点
            Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
            // 3.利用CHA算法构建调用图
            CHACallGraph cg = new CHACallGraph(cha);
            cg.init(eps);
//            System.out.println("get call graph done");
            return cg;
        }catch (ClassHierarchyException e){
            e.printStackTrace();
        }catch (CancelException e){
            e.printStackTrace();
        }
        System.out.println("something wrong while creating call graph . ");
        return null;
    }
    
    /**
    * @Description 加载changeInfo文件到Map<String,Set<String>>中
     *              key : class
     *              value : signature
     * @Param: changeInfoPath changeInfo文件的绝对路径
    * @return java.util.Map<java.lang.String,java.util.Set<java.lang.String>>
    **/

    public static Map<String, Set<String>> loadChangeInfo(String changeInfoPath){
        Map<String, Set<String>> resMap = new HashMap<String, Set<String>>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(changeInfoPath));
            String str = null;
            while ((str = in.readLine()) != null) {
                String className = str.split(" ")[0];
                String classMethod = str.split(" ")[1];
                if(resMap.containsKey(className)){
                    resMap.get(className).add(classMethod);
                }else{
                    Set<String> methodSet = new HashSet<String>();
                    methodSet.add(classMethod);
                    resMap.put(className,methodSet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resMap;
    }

    /**
    * @Description 将res的内容存入targetFile文件中
     * @Param: res 保存相应的testcase
    * @Param: targetFile 将内容保存到相应的文件中
    * @return void
    **/
    public static void store(Set<String> res,String targetFile){
        System.out.println("save result into "+ targetFile+" ! ");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(targetFile));
            for (String line : res) {
                out.write(line+"\n");
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("save into file done ! ");
    }

    /**
     * 同store(Set<String> res,String targetFile)
     * @param res
     * @param targetFile
     */
    public static void store(String res,String targetFile){
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(targetFile));
            out.write(res);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

# classic_automated_testing
## 经典自动化测试CODE

### 项目依赖

```xml
    <dependencies>
        <dependency>
            <groupId>com.ibm.wala</groupId>
            <artifactId>com.ibm.wala.util</artifactId>
            <version>1.5.5</version>
        </dependency>

        <dependency>
            <groupId>com.ibm.wala</groupId>
            <artifactId>com.ibm.wala.shrike</artifactId>
            <version>1.5.5</version>
        </dependency>

        <dependency>
            <groupId>com.ibm.wala</groupId>
            <artifactId>com.ibm.wala.core</artifactId>
            <version>1.5.5</version>
        </dependency>

        <dependency>
            <groupId>com.ibm.wala</groupId>
            <artifactId>com.ibm.wala.cast.java</artifactId>
            <version>1.5.5</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>RELEASE</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>RELEASE</version>
            <scope>test</scope>
        </dependency>

    </dependencies>
```

### 程序入口

#### testSelection

```java
public class testSelection {

    /**
    * @Description 此函数为主要方法,调用Util.getSelction()获取不同粒度的文件
     * @Param: projectPath 项目的绝对路径
    * @Param: changeInfoPath changinfo的绝对路径
    * @Param: level 选择方式的粒度,分为-m(方法级)-c(类级)
    * @return void
    * @author wrl
    * @date 2020/11/20 2:33
    **/

    public void run(String projectPath,String changeInfoPath,String level) {
        Selection selection = Until.createSelection(projectPath,changeInfoPath,level);
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
```

### 其余类

#### Selection

接口,定义的两个方法,分别是选取测试用例并保存,以及生成dot文件

```java
public void getDot(String targetFile);
public void getSelection();
```

#### SelectionClass

类级别的测试用例选择,实现了Selection接口

```java
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
```

最为重要的是getValidClassGraph()方法,通过遍历所有CGNode,剪枝,使用邻接矩阵的形式存储起来

```java
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
```

其中最为重要的是getSelectedTest()方法,借助dfs算法,标记需要选择的测试用例

```
private Set<String> getSelectedTest() {
    HashSet<String> res = new HashSet<>();
    for(String changed:changedClass){
        getChangedClassHelper(validClassGraph,changed,res);
    }
    //挑选出的类中包含生产用例,此处消除生产用例,只保留测试用例
    res.removeAll(changedClass);
    return res;
}
```

#### SelectionMethod

实现了接口Selection

```java
/**
* @Description 获取方法级的测试用例
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
    System.out.println("start analyse in method level .");
    callGraph = Util.getCallGraph(scope);
    validMethodGraph = getValidMethodGraph();
    changedMethod = getChangedMethod();
    Util.addScope(testPath,scope);
    callGraph = Util.getCallGraph(scope);
    validMethodGraph = getValidMethodGraph();
    selectedMethod = getSelectedMethod();
    Set<String> selected = getSelected();
    System.out.println("analyse in method level done ! ");
    Util.store(selected,".\\selection-method.txt");
}
```

其他的和selectionClass有相似之处,只是调用图更加细致

#### CreateGraph

用于生成所有class的调用图,调用Seletion的getDot方法

#### Util

工具类,主要为wala框架的使用方法,以及文件存储方法等

其中比较重要的是createSelection方法,这是一个工厂方法,生成相应粒度的class文件.

```java
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
```

### 问题

目前对于方法级别的测试用例选择,存在精度不高的情况,可能的原因是使用了cha生成调用图,从而在某些情况(如改变了接口,改变了父类等多态,泛型问题),测试用例方法的选取会缺少几个

所想到的改进的方式:细化生成调用图,即修改Util.getCallGraph()方法即可


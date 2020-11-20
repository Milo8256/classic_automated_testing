import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class testSelectionTest {
    testSelection test = new testSelection();
    final static String CLASSLEVEL = "-c";
    final static String METHODLEVEL = "-m";
    final static String SELECTION_CLASS = ".\\selection-class.txt";
    final static String SELECTION_METHOD = ".\\selection-method.txt";
    final static String CMD = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\0-CMD";
    final static String ALU = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\1-ALU";
    final static String DATALOG = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\2-DataLog";
    final static String BINARYHEAP = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\3-BinaryHeap";
    final static String NEXTDAY = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\4-NextDay";
    final static String MORETRIANGLE = "G:\\南大\\3-1自动化测试\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\5-MoreTriangle";




    public Set<String> loadFileHelp(String file){
        HashSet<String> res = new HashSet<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str = null;
            while ((str = in.readLine()) != null) {
                if("".equals(str.trim()))continue;
                res.add(str.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public String getExpectedClassPath(String projectPath){
        return projectPath+"\\data\\selection-class.txt";
    }

    public String getExpectedMethodPath(String projectPath){
        return projectPath+"\\data\\selection-method.txt";
    }
    public String getChangeInfoPath(String projectPath){
        return projectPath+"\\data\\change_info.txt";
    }

    @Test
    public void ClassTestCMD() {
        test.run(CMD,getChangeInfoPath(CMD),CLASSLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_CLASS);
        Set<String> expected = loadFileHelp(getExpectedClassPath(CMD));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }
    @Test
    public void MethodTestCMD() {
        test.run(CMD,getChangeInfoPath(CMD),METHODLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_METHOD);
        Set<String> expected = loadFileHelp(getExpectedMethodPath(CMD));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }



    @Test
    public void ClassTestALU() {
        test.run(ALU,getChangeInfoPath(ALU),CLASSLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_CLASS);
        Set<String> expected = loadFileHelp(getExpectedClassPath(ALU));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

    @Test
    public void MethodTestALU() {
        test.run(ALU,getChangeInfoPath(ALU),METHODLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_METHOD);
        Set<String> expected = loadFileHelp(getExpectedMethodPath(ALU));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }
    @Test
    public void ClassTestDatalog() {
        test.run(DATALOG,getChangeInfoPath(DATALOG),CLASSLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_CLASS);
        Set<String> expected = loadFileHelp(getExpectedClassPath(DATALOG));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

    @Test
    public void MethodTestDatalog() {
        test.run(DATALOG,getChangeInfoPath(DATALOG),METHODLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_METHOD);
        Set<String> expected = loadFileHelp(getExpectedMethodPath(DATALOG));
//        assertEquals(expected.size(),actual.size());
        for (String s : expected) {
            System.out.println(s);
            assert(actual.contains(s));
        }
        System.out.println();
        for(String s : actual){
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

    @Test
    public void ClassTestBinaryheap() {
        test.run(BINARYHEAP,getChangeInfoPath(BINARYHEAP),CLASSLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_CLASS);
        Set<String> expected = loadFileHelp(getExpectedClassPath(BINARYHEAP));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

    @Test
    public void MethodTestBINARYHEAP() {
        test.run(BINARYHEAP,getChangeInfoPath(BINARYHEAP),METHODLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_METHOD);
        Set<String> expected = loadFileHelp(getExpectedMethodPath(BINARYHEAP));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

    @Test
    public void ClassTestNextDay() {
        test.run(NEXTDAY,getChangeInfoPath(NEXTDAY),CLASSLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_CLASS);
        Set<String> expected = loadFileHelp(getExpectedClassPath(NEXTDAY));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

    @Test
    public void MethodTestNEXTDAY() {
        test.run(NEXTDAY,getChangeInfoPath(NEXTDAY),METHODLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_METHOD);
        Set<String> expected = loadFileHelp(getExpectedMethodPath(NEXTDAY));
//        assertEquals(expected.size(),actual.size());
        for (String s : expected) {
            System.out.println(s);
            assert(actual.contains(s));
        }
    }

    @Test
    public void ClassTestMoretriangle() {
        test.run(MORETRIANGLE,getChangeInfoPath(MORETRIANGLE),CLASSLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_CLASS);
        Set<String> expected = loadFileHelp(getExpectedClassPath(MORETRIANGLE));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

    @Test
    public void MethodTestMORETRIANGLE() {
        test.run(MORETRIANGLE,getChangeInfoPath(MORETRIANGLE),METHODLEVEL);
        Set<String> actual = loadFileHelp(SELECTION_METHOD);
        Set<String> expected = loadFileHelp(getExpectedMethodPath(MORETRIANGLE));
        assertEquals(expected.size(),actual.size());
        for (String s : actual) {
            System.out.println(s);
            assert(expected.contains(s));
        }
    }

}
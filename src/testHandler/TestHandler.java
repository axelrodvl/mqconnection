package testHandler;

import java.util.ArrayList;

public class TestHandler {
    public ArrayList<Test> suite;
    
    public void addTest(Test test) {
        suite.add(test);
    }
    
    private boolean startTest(Test test) {
        try {
            test.init();
            test.action();
            test.end();
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            return false;
        }
    }
    
    public boolean startSuite() {
        boolean result = true;
        for (Test test : suite) {
            result = result && startTest(test);
        }
        if (result) {
            System.out.println("Suite: PASSED");
        } else {
            System.out.println("Suite: FAILED");
        }
        
        
        return result;
    }
}

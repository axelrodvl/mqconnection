    package testHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TestHandler {
    public ArrayList<Test> suite = new ArrayList<Test>();
    public DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    Calendar timeStart = null;
    Calendar timeStop = null;
    Calendar suiteTimeStart = null;
    Calendar suiteTimeStop = null;
    
    public static long getDiffInMillis(Calendar c1, Calendar c2)
    {
        // учитываем перевод времени
        long time1 = c1.getTimeInMillis() + c1.getTimeZone().getOffset(c1.getTimeInMillis());
        long time2 = c2.getTimeInMillis() + c2.getTimeZone().getOffset(c2.getTimeInMillis());
        return Math.abs(time1 - time2);
    }
    
    public void addTest(Test test) {
        suite.add(test);
        System.out.println("TestHandler. Test " + test.testName + " added");
    }
    
    private boolean startTest(Test test) {
        System.out.println("TestHandler. Test " + test.testName + " started");
        timeStart = Calendar.getInstance();
        System.out.println("Time start: " + timeStart.getTime());
        
        try {
            test.init();
            test.action();
            test.end();
            
            timeStop = Calendar.getInstance();
            System.out.println("Time stop: " + timeStop.getTime());
            System.out.println("Time elapsed:" + getDiffInMillis(timeStart, timeStop) + " ms");
            System.out.println("TestHandler. Test " + test.testName + " completed");
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            
            
            return false;
        }
    }
    
    public boolean startSuite() {
        System.out.println("TestHandler. Suite: started");
        suiteTimeStart = Calendar.getInstance();
        System.out.println("Suite. Time start: " + suiteTimeStart.getTime());
        
        boolean result = true;
        for (Test test : suite) {
            result = result && startTest(test);
        }
        if (result) {
            System.out.println("TestHandler. Suite: PASSED");
        } else {
            System.out.println("TestHandler. Suite: FAILED");
        }
        
        suiteTimeStop = Calendar.getInstance();
        System.out.println("Suite. Time stop: " + suiteTimeStop.getTime());
        System.out.println("Suite. Time elapsed:" + getDiffInMillis(suiteTimeStart, suiteTimeStop) + " ms");
        System.out.println("Suite. Average time: " + (getDiffInMillis(suiteTimeStart, suiteTimeStop) / suite.size()) + " ms");
        
        return result;
    }
}

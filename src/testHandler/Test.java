package testHandler;

import java.util.concurrent.TimeUnit;

public abstract class Test {
    public String testName = null;
    public String testDescription = null;
    
    public static String randomValue(int length) {
        String result = "";
        int temp;
        for (int i = 0; i < length; ++i) {
            temp = (int) (Math.random() * 1000 % 36) + 97;
            result += (temp < 123) ? ((char) temp) : ((char) (temp - 75));
        }
        return result;
    }
    public static String randomUUID() {
        return java.util.UUID.randomUUID().toString();
    }  
    public static void sleep(int sleepTimeInMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
        }
        catch (Exception ex) {}
    }
    
    public abstract void init() throws Exception;
    public abstract void action() throws Exception;
    public abstract void end() throws Exception;
}

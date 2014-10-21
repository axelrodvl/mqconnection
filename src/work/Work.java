package work;

import testHandler.*;
import testPackage.*;

public class Work {
    public static void main(String[] args) {
        TestHandler th = new TestHandler();
        th.addTest(new SPM());
        th.addTest(new SPM());
        th.addTest(new SPM());
        th.startSuite();
    }
}

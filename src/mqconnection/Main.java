package mqconnection;

public class Main {

    public static void main(String[] args) {  
        MQConnection mqc = new MQConnection("WS084.TEST.QM", "localhost", 1420, "SYSTEM.DEF.SVRCONN");
        
        mqc.clearQueue("MQSTUB.OUT"); 
    }   
}
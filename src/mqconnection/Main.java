package mqconnection;

public class Main {

    public static void main(String[] args) {  
        MQConnection mqc = new MQConnection("WS084.TEST.QM", "localhost", 1420, "SYSTEM.DEF.SVRCONN");
        
        System.out.println(mqc.createVariable());
        System.out.println(mqc.createVariable());
       
        mqc.clearQueue("MQSTUB.IN"); 
        mqc.clearQueue("MQSTUB.OUT");
    }   
}
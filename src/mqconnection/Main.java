package mqconnection;

public class Main {

    public static void main(String[] args) {  
        MQConnection mqc = new MQConnection();
        
        System.out.println(mqc.createVariable());
        System.out.println(mqc.createVariable());
       
        mqc.clearQueue("MQSTUB.IN"); 
        mqc.clearQueue("MQSTUB.OUT");
    }   
}
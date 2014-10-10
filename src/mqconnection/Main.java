package mqconnection;

public class Main {

    public static void main(String[] args) {  
        
        RandomVariableCreator rvc = new RandomVariableCreator();
        
        String abc = new String(rvc.createVariable());
        System.out.println(abc);

        System.out.println(rvc.createVariable());
        System.out.println(rvc.createVariable());
        
        JMeterMQTools mqt = new JMeterMQTools();
       
        mqt.clearQueue("MQSTUB.IN"); 
        mqt.clearQueue("MQSTUB.OUT");
    }   
}
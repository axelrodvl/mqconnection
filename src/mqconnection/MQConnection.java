package mqconnection;

import javax.xml.*;

import javax.jms.*;
import com.ibm.jms.*;
import com.ibm.mq.*;

import com.ibm.msg.client.wmq.WMQConstants;


public class MQConnection {
    public void MQConnection() {
        
    }
    
    public String createVariable() {
	    String outID = new String("");
            int temp;
	    for (int i = 0; i < 24; ++i) {
                temp = ((int)(Math.random() * 1000) % 36) + 97;
                outID += (temp < 123) ? (char)temp : (char)(temp - 75);
	    }
	    return outID;
    }
    
    public void clearQueue(String mqQueue) {
        int depth = 0;  
        MQQueueManager qMgr; // define a queue manager object  
        String mqHost = "localhost";  
        String mqPort = "1420";  
        String mqChannel = "SYSTEM.DEF.SVRCONN";  
        String mqQMgr = "WS084.TEST.QM";  
        try {  
            // Set up MQSeries environment  
           MQEnvironment.hostname = mqHost;  
           MQEnvironment.port = Integer.valueOf(mqPort).intValue();  
           MQEnvironment.channel = mqChannel;  
           MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY,  
           MQC.TRANSPORT_MQSERIES);  
           qMgr = new MQQueueManager(mqQMgr);  
         
           int openOptions = MQC.MQOO_INQUIRE;  
           
           MQQueue destQueue = qMgr.accessQueue(mqQueue, openOptions);  
           depth = destQueue.getCurrentDepth();  
           
           destQueue.close();
           
           openOptions = MQC.MQOO_INPUT_AS_Q_DEF;
           destQueue = qMgr.accessQueue(mqQueue, openOptions);  
           
           MQMessage message = new MQMessage();
           
           for (int i = 0; i < depth; ++i) {
               destQueue.get(message);
               message = null;
               message = new MQMessage();
           }
           
           destQueue.close();  
           qMgr.disconnect();  
           
           System.out.println("Queue " + mqQueue + ": cleared");
        } 
        
        catch (Exception err) {  
           System.out.println("Some error while clearing queue.");
           err.printStackTrace();  
        }
    }
}
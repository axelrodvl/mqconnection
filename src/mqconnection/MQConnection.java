package mqconnection;

import com.ibm.mq.*;

public class MQConnection {
    // MQEnvironment init variables
    String queueMgrName = null;
    String queueMgrHostname = null;
    int queueMgrPort = 0;
    String queueMgrChannel = null; 
    String putQueueName = null;
    String getQueueName = null;
    
    // MQEnvironment Tools
    MQQueueManager queueMgr = null;
    MQQueue putQueue = null;
    MQQueue getQueue = null;
    MQPutMessageOptions pmo = new MQPutMessageOptions();
    MQGetMessageOptions gmo = new MQGetMessageOptions();
    MQMessage requestMsg = new MQMessage();
    MQMessage responseMsg = new MQMessage();
    String msgBody = null;
    String requestXML = null;
    byte[] responseMsgData = null;
    String msg = null;
    
    public MQConnection(String queueMgrName, String queueMgrHostname, int queueMgrPort, String queueMgrChannel) {
        this.queueMgrName = queueMgrName;
        this.queueMgrHostname = queueMgrHostname;
        this.queueMgrPort = queueMgrPort;
        this.queueMgrChannel = queueMgrChannel;
        
        MQEnvironment.hostname = this.queueMgrHostname;
        MQEnvironment.port = this.queueMgrPort;
        MQEnvironment.channel = this.queueMgrChannel;
        
        try {
            queueMgr = new MQQueueManager(queueMgrName);
        }
        catch (MQException ex) {
            System.out.println("Error connecting to queue manager. Reason: " + ex.reasonCode);
            System.out.println(ex.getMessage());
        }
        
        try {
            putQueue = queueMgr.accessQueue(putQueueName, MQC.MQOO_BIND_NOT_FIXED | MQC.MQOO_OUTPUT);
            getQueue= queueMgr.accessQueue(getQueueName, MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT);
            System.out.println("Successful connection to " + this.queueMgrName);
        }
        catch (MQException ex) {
            System.out.println("Error accessing queues. Reason: " + ex.reasonCode);
            System.out.println(ex.getMessage());
        }
    }
    
    public MQConnection() {
        System.out.println("MQConnection. Empty constructor.");
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
    
    
    
    @Deprecated
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
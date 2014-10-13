package mqconnection;

import com.ibm.mq.*;
import java.io.IOException;

public class MQConnection {
    String queueMgrName = null;
    String queueMgrHostname = null;
    int queueMgrPort = 0;
    String queueMgrChannel = null; 
    MQQueueManager queueMgr = null;
    
    public class Queue {
        
    }
    
    public class Message {
        
    }
    
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
    }
    
    public MQConnection() {
        System.out.println("MQConnection. Empty constructor.");
    }
    
    public boolean clearQueue(String putQueueName) {
        int depth = 0;
        
        try {
           int openOptions = MQC.MQOO_INQUIRE;  
           MQQueue queue = queueMgr.accessQueue(putQueueName, openOptions);  
           depth = queue.getCurrentDepth();  
           queue.close();
           
           openOptions = MQC.MQOO_INPUT_AS_Q_DEF;
           queue = queueMgr.accessQueue(putQueueName, openOptions);  
           MQMessage message = new MQMessage();
           
           for (int i = 0; i < depth; ++i) {
               queue.get(message);
               //message.clearMessage();
               message = null;
               message = new MQMessage();
           }
           queue.close();  
           System.out.println("Queue " + putQueueName + ": cleared");
           return true;
        } 
        catch (MQException ex) {  
            System.out.println("clearQueue(" + putQueueName + "): error");
            System.out.println(ex.toString());
            return false;
        }
    }
    
    public boolean sendMessageSingle(String putQueueName, String replytToQueueName, String msgBody) {
        MQQueue putQueue = null;
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        MQMessage requestMsg = new MQMessage();
        try {
            putQueue = queueMgr.accessQueue(putQueueName, MQC.MQOO_BIND_NOT_FIXED | MQC.MQOO_OUTPUT);
            pmo.options = MQC.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.
            requestMsg.replyToQueueName = replytToQueueName; // the response should be put on this queue
            requestMsg.report=MQC.MQRO_PASS_MSG_ID; //If a report or reply is generated as a result of this message, the MsgId of this message is copied to the MsgId of the report or reply message.
            requestMsg.format = MQC.MQFMT_STRING; // Set message format. The application message data can be either an SBCS string (single-byte character set), or a DBCS string (double-byte character set). 
            requestMsg.messageType=MQC.MQMT_REQUEST; // The message is one that requires a reply.
            requestMsg.writeString(msgBody); // message payload
            putQueue.put(requestMsg, pmo);
            
            return true;
        } 
        catch(MQException | IOException ex) {
            System.out.println("sendMessageSingle: error");
            System.out.println(ex.toString());
            return false;
        }
    }
}
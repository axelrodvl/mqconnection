package mqconnection;

import com.ibm.mq.*;
import com.ibm.mq.headers.MQRFH2;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MQConnection {
    String queueMgrName = null;
    String queueMgrHostname = null;
    int queueMgrPort = 0;
    String queueMgrChannel = null; 
    MQQueueManager queueMgr = null;
    
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
    
    public boolean sendMessage(String putQueueName, MQMessage message) {
        MQQueue putQueue = null;
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        try {
            putQueue = queueMgr.accessQueue(putQueueName, MQC.MQOO_BIND_NOT_FIXED | MQC.MQOO_OUTPUT);
            pmo.options = MQC.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.            
            putQueue.put(message, pmo);
            putQueue.close();
            
            System.out.println("sendMessageSingle: message sent to " + putQueueName);
            return true;
        } 
        // For JDK 1.7: catch(MQException | IOException ex) {
        
        // For JDK 1.5
        catch(Exception ex) {
            System.out.println("sendMessageSingle: error");
            System.out.println(ex.toString());
            return false;
        }
    }
    
    public MQMessage browseMessage(String getQueueName) {
        MQQueue getQueue = null;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;  
        
        try {
            getQueue = queueMgr.accessQueue(getQueueName, MQC.MQOO_BROWSE | MQC.MQOO_INPUT_SHARED);
            gmo.options = MQC.MQGMO_WAIT | MQC.MQGMO_BROWSE_NEXT ;
            getQueue.get(responseMsg, gmo);
            getQueue.close();

            System.out.println("browseMessage: message browsed from " + getQueueName);

            return responseMsg;
        } catch (Exception ex) {
            return null;
        }
    }
    public MQMessage browseMessage(String getQueueName, MQMessage request) {
        MQQueue getQueue = null;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;  
        
        try {
            getQueue = queueMgr.accessQueue(getQueueName, MQC.MQOO_BROWSE | MQC.MQOO_INPUT_SHARED);
            gmo.options = MQC.MQGMO_WAIT | MQC.MQGMO_BROWSE_NEXT ;
            
            responseMsg.messageId = request.messageId;
            gmo.matchOptions=MQC.MQMO_MATCH_MSG_ID;
            
            getQueue.get(responseMsg, gmo);
            getQueue.close();

            System.out.println("browseMessage: message browsed from " + getQueueName);

            return responseMsg;
        } catch (Exception ex) {
            return null;
        }
    }
    
    public MQMessage getMessage(String getQueueName) {
        MQQueue getQueue = null;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;  
        
        try {
            getQueue = queueMgr.accessQueue(getQueueName, MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT);
            
            getQueue.get(responseMsg, gmo);
            getQueue.close();

            System.out.println("getMessageSimple: message recieved from " + getQueueName);

            return responseMsg;
        } catch (Exception ex) {
            return null;
        }
    }
    public MQMessage getMessage(String getQueueName, MQMessage request) {
        MQQueue getQueue = null;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;  
        
        try {
            getQueue = queueMgr.accessQueue(getQueueName, MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT);

            responseMsg.messageId = request.messageId;
            gmo.matchOptions=MQC.MQMO_MATCH_MSG_ID;

            getQueue.get(responseMsg, gmo);
            getQueue.close();
            System.out.println("getMessageSimple: message recieved from " + getQueueName);
            
            return responseMsg;
        } catch (Exception ex) {
            return null;
        }
    }
    
    public MQMessage newMessage(String messageString) {
        try {
            MQMessage message = new MQMessage();
            message.replyToQueueName = "SOMEQUEUE";
            message.report=MQC.MQRO_PASS_MSG_ID;
            message.format = MQC.MQFMT_STRING;
            message.messageType=MQC.MQMT_REQUEST;
            message.writeString(messageString);
            return message;
        } catch(Exception ex) {
            return null;
        }
    }
    
    public void closeConnection() {
        try {
            queueMgr.close();
        }
        catch (MQException ex) {
            System.out.println("Error while closing connection");
        }
        queueMgr = null;
    }
        
    @Override
    protected void finalize()
    { 
        try {
            queueMgr.close();
            
            queueMgrName = null;
            queueMgrHostname = null;
            queueMgrPort = 0;
            queueMgrChannel = null; 
            queueMgr = null;
            
            
        }
        catch (MQException ex) {
        }
        
        try {
            super.finalize();
        }
        catch (Throwable ex) {
            Logger.getLogger(MQConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
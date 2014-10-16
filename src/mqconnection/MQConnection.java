package mqconnection;

import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MQConnection {
    private String queueMgrName = null;
    private String queueMgrHostname = null;
    private int queueMgrPort = 0;
    private String queueMgrChannel = null; 
    public MQQueueManager queueMgr = null;
    
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
            int openOptions = MQConstants.MQOO_INQUIRE;  
            MQQueue queue = queueMgr.accessQueue(putQueueName, openOptions);  
            depth = queue.getCurrentDepth();  
            queue.close();
           
            openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF;
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
            putQueue = queueMgr.accessQueue(putQueueName, MQConstants.MQOO_BIND_NOT_FIXED | MQConstants.MQOO_OUTPUT);
            pmo.options = MQConstants.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.            
            putQueue.put(message, pmo);
            putQueue.close();
            
            System.out.println("sendMessageSingle: message sent to " + putQueueName);
            return true;
        } 
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
            getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED);
            gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT ;
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
            getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED);
            gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT ;
            
            responseMsg.messageId = request.messageId;
            gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;
            
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
            getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
            
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
            getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);

            responseMsg.messageId = request.messageId;
            gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;

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
            message.report = MQConstants.MQRO_PASS_MSG_ID;
            message.format = MQConstants.MQFMT_STRING;
            message.messageType = MQConstants.MQMT_REQUEST;
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
}
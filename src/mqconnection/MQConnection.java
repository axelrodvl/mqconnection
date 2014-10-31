package mqconnection;

import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;

/**
 * IBM Message Queue toolkit
 * Provides ability to connect to queue manager; send, browse and get messages from MQ
 * @author vakselrod
 */
public class MQConnection {
    private boolean loggingEnabled = false;
    private String queueMgrName = null;
    private String queueMgrHostname = null;
    private int queueMgrPort = 0;
    private String queueMgrChannel = null; 
    public MQQueueManager queueMgr = null;
    
    /**
     * Enable logging to console through System.out.println()
     */
    public void enableLogging() {
        this.loggingEnabled = true;
    }
    
    /**
     * Printing message to standard output (System.out.println())
     * @param message Message to print
     */
    private void printLog(String message) {
        if (loggingEnabled) {
            System.out.println(message);
        }
    }
    
    /**
     * Creates connection to MQ manager
     * @param queueMgrName
     * @param queueMgrHostname
     * @param queueMgrPort
     * @param queueMgrChannel 
     */
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
            printLog("Successfull connection to " + queueMgrName);
        }
        catch (MQException ex) {
            printLog("Error connecting to queue manager. Reason: " + ex.reasonCode);
            printLog(ex.getMessage());
        }
    }
    
    /**
     * Clear chosen queue at connected queue manager
     * @param putQueueName
     * @return Returns flag of success
     */
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
                message = null;
                message = new MQMessage();
            }
            queue.close();  
            printLog("Queue " + putQueueName + ": cleared");
            return true;
        } 
        catch (MQException ex) {  
            printLog("clearQueue(" + putQueueName + "): error");
            printLog(ex.toString());
            return false;
        }
    }
    
    /** 
     * Sending single message to chosen queue
     * @param putQueueName
     * @param message 
     * @return Returns flag of success
     */
    public boolean sendMessage(String putQueueName, MQMessage message) {
        MQQueue putQueue = null;
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        try {
            putQueue = queueMgr.accessQueue(putQueueName, MQConstants.MQOO_BIND_NOT_FIXED | MQConstants.MQOO_OUTPUT);
            pmo.options = MQConstants.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.            
            putQueue.put(message, pmo);
            putQueue.close();
            
            printLog("sendMessageSingle: message sent to " + putQueueName);
            return true;
        } 
        catch(Exception ex) {
            printLog("sendMessageSingle: error");
            printLog(ex.toString());
            return false;
        }
    }
    
    /** 
     * Sending single message to chosen queue with certain parameters
     * @param putQueueName
     * @param message 
     * @param pmo MQPut message options
     * @return Returns flag of success
     */
    public boolean sendMessage(String putQueueName, MQMessage message, MQPutMessageOptions pmo) {
        MQQueue putQueue = null;
        try {
            putQueue = queueMgr.accessQueue(putQueueName, MQConstants.MQOO_BIND_NOT_FIXED | MQConstants.MQOO_OUTPUT);
            putQueue.put(message, pmo);
            putQueue.close();
            
            printLog("sendMessageSingle: message sent to " + putQueueName);
            return true;
        } 
        catch(Exception ex) {
            printLog("sendMessageSingle: error");
            printLog(ex.toString());
            return false;
        }
    }
    
    /**
     * Browsing first message in queue
     * @param getQueueName
     * @return Returns flag of success
     */
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

            printLog("browseMessage: message browsed from " + getQueueName);

            return responseMsg;
        } catch (Exception ex) {
            printLog("Error while browseMessage");
            return null;
        }
    }
    
    /**
     * Browsing first message in queue with request messageId 
     * @param getQueueName
     * @param request
     * @return Returns flag of success
     */
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

            printLog("browseMessage: message browsed from " + getQueueName);

            return responseMsg;
        } catch (Exception ex) {
            printLog("Error while browseMessage");
            return null;
        }
    }
    
    /**
     * Getting first message in queue
     * @param getQueueName
     * @return Returns flag of success
     */
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

            printLog("getMessage: message recieved from " + getQueueName);

            return responseMsg;
        } catch (Exception ex) {
            printLog("Error while getMessage");
            return null;
        }
    }
    
    /**
     * Getting first message in queue with chosen timeout
     * @param getQueueName
     * @param timeout
     * @return Returns flag of success
     */
    public MQMessage getMessage(String getQueueName, int timeout) {
        MQQueue getQueue = null;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;  
        
        try {
            gmo.options = MQConstants.MQGMO_WAIT;
            gmo.waitInterval = timeout;
            getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
            
            getQueue.get(responseMsg, gmo);
            getQueue.close();

            printLog("getMessage: message recieved from " + getQueueName + " with timeout = " + new Integer(timeout).toString());

            return responseMsg;
        } catch (Exception ex) {
            printLog("Error while getMessage");
            return null;
        }
    }
    
    /**
     * Getting message in qeueue with request messageId
     * @param getQueueName
     * @param request
     * @return 
     */
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
            
            printLog("getMessage: message recieved from " + getQueueName + " with settings from request");
            
            return responseMsg;
        } catch (Exception ex) {
            printLog("Error while getMessage");
            return null;
        }
    }
    
    /**
     * Getting message in queue with request messageId and chosen timeout
     * @param getQueueName
     * @param request
     * @param timeout
     * @return 
     */
    public MQMessage getMessage(String getQueueName, MQMessage request, int timeout) {
        MQQueue getQueue = null;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;  
        
        try {
            gmo.options = MQConstants.MQGMO_WAIT;
            gmo.waitInterval = timeout;
            getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);

            responseMsg.messageId = request.messageId;
            gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;

            getQueue.get(responseMsg, gmo);
            getQueue.close();
            
            printLog("getMessage: message recieved from " + getQueueName + " with settings from request");
            
            return responseMsg;
        } catch (Exception ex) {
            printLog("Error while getMessage");
            return null;
        }
    }
    
    /**
     * Returns new MQMessage with basic parameters and body from messageString
     * @param messageString
     * @return Returns flag of success
     */
    public MQMessage newMessage(String messageString) {
        try {
            MQMessage message = new MQMessage();
            message.replyToQueueName = "SOMEQUEUE";
            message.report = MQConstants.MQRO_PASS_MSG_ID;
            message.format = MQConstants.MQFMT_STRING;
            message.messageType = MQConstants.MQMT_REQUEST;
            message.writeString(messageString);
            
            printLog("New MQMessage created from string");
            
            return message;
        } catch(Exception ex) {
            printLog("Error while newMessage");
            return null;
        }
    }
    
    /**
     * Closing connection to queue manager
     * @return Returns flag of success
     */
    public boolean closeConnection() {
        try {
            queueMgr.close();
            printLog("Connection with " + queueMgrName + " closed");
            queueMgr = null;
            return true;
        }
        catch (MQException ex) {
            printLog("Error while closeConnection");
            return false;
        }
    }
}
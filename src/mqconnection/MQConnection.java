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
     * Returns queue, opened to put messages
     * @param putQueueName
     * @return MQQueue object
     */
    public MQQueue openPutQueue(String putQueueName) throws Exception {
        return queueMgr.accessQueue(putQueueName, MQConstants.MQOO_BIND_NOT_FIXED | MQConstants.MQOO_OUTPUT);
    }
    
    /**
     * Returns queue, opened to get messages
     * @param getQueueName
     * @return MQQueue object
     */
    public MQQueue openGetQueue(String getQueueName) throws Exception {
        return queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
    }
    
    /**
     * Returns queue, opened to browse messages
     * @param getQueueName
     * @return MQQueue object
     */
    public MQQueue openBrowseQueue(String getQueueName) throws Exception {
        return queueMgr.accessQueue(getQueueName, MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED);
    }
    
    /**
     * Creates connection to MQ manager
     * @param queueMgrName
     * @param queueMgrHostname
     * @param queueMgrPort
     * @param queueMgrChannel 
     */
    public MQConnection(String queueMgrName, String queueMgrHostname, int queueMgrPort, String queueMgrChannel) throws Exception {
        MQEnvironment.hostname = queueMgrHostname;
        MQEnvironment.port = queueMgrPort;
        MQEnvironment.channel = queueMgrChannel;
        queueMgr = new MQQueueManager(queueMgrName);
        printLog("Successfull connection to " + queueMgrName);
    }
    
    /**
     * Clear chosen queue at connected queue manager
     * @param putQueueName
     * @return Returns flag of success
     */
    public void clearQueue(String putQueueName) throws Exception {
        int depth = 0;
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
    }
    
    /** 
     * Sending single message to chosen queue
     * @param putQueueName
     * @param message 
     * @return Returns flag of success
     */
    public void sendMessage(String putQueueName, MQMessage message) throws Exception {
        MQQueue putQueue = queueMgr.accessQueue(putQueueName, MQConstants.MQOO_BIND_NOT_FIXED | MQConstants.MQOO_OUTPUT);
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        pmo.options = MQConstants.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.            
        putQueue.put(message, pmo);
        putQueue.close();
        printLog("sendMessageSingle: message sent to " + putQueueName);
    }
    
    /** 
     * Sending single message to open queue
     * @param putQueueName
     * @param message 
     * @return Returns flag of success
     */
    public void sendMessage(MQQueue putQueue, MQMessage message) throws Exception {
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        pmo.options = MQConstants.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.            
        putQueue.put(message, pmo);    
        printLog("sendMessageSingle: message sent to " + putQueue.name);
    }
    
    /** 
     * Sending single message to chosen queue with certain parameters
     * @param putQueueName
     * @param message 
     * @param pmo MQPut message options
     * @return Returns flag of success
     */
    public void sendMessage(String putQueueName, MQMessage message, MQPutMessageOptions pmo) throws Exception {
        MQQueue putQueue = queueMgr.accessQueue(putQueueName, MQConstants.MQOO_BIND_NOT_FIXED | MQConstants.MQOO_OUTPUT);
        putQueue.put(message, pmo);
        putQueue.close();
        printLog("sendMessageSingle: message sent to " + putQueueName);
    }
    
    /** 
     * Sending single message to chosen queue with certain parameters
     * @param putQueueName
     * @param message 
     * @param pmo MQPut message options
     * @return Returns flag of success
     */
    public void sendMessage(MQQueue putQueue, MQMessage message, MQPutMessageOptions pmo) throws Exception {
        putQueue.put(message, pmo);
        printLog("sendMessageSingle: message sent to " + putQueue.name);
    }
    
    /**
     * Browsing first message in queue
     * @param getQueueName
     * @return Returns flag of success
     */
    public MQMessage browseMessage(String getQueueName) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        MQQueue getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED);
        gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT ;
        getQueue.get(responseMsg, gmo);
        getQueue.close();
        printLog("browseMessage: message browsed from " + getQueueName);
        return responseMsg;
    }
    
    /**
     * Browsing first message in queue
     * @param getQueueName
     * @return Returns flag of success
     */
    public MQMessage browseMessage(MQQueue browseQueue) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT ;
        browseQueue.get(responseMsg, gmo);
        printLog("browseMessage: message browsed from " + browseQueue.name);
        return responseMsg;
    }
    
    /**
     * Browsing first message in queue with request messageId 
     * @param getQueueName
     * @param request
     * @return Returns flag of success
     */
    public MQMessage browseMessage(String getQueueName, MQMessage request) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        MQQueue getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED);
        gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT ;
        responseMsg.messageId = request.messageId;
        gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;
        getQueue.get(responseMsg, gmo);
        getQueue.close();
        printLog("browseMessage: message browsed from " + getQueueName);
        return responseMsg;
    }
    
    /**
     * Browsing first message in queue with request messageId 
     * @param getQueueName
     * @param request
     * @return Returns flag of success
     */
    public MQMessage browseMessage(MQQueue browseQueue, MQMessage request) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT ;
        responseMsg.messageId = request.messageId;
        gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;
        browseQueue.get(responseMsg, gmo);
        printLog("browseMessage: message browsed from " + browseQueue.name);
        return responseMsg;
    }
    
    /**
     * Getting first message in queue
     * @param getQueueName
     * @return Returns flag of success
     */
    public MQMessage getMessage(String getQueueName) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        MQQueue getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
        getQueue.get(responseMsg, gmo);
        getQueue.close();
        printLog("getMessage: message recieved from " + getQueueName);
        return responseMsg;
    }
    
    /**
     * Getting first message in queue
     * @param getQueueName
     * @return Returns flag of success
     */
    public MQMessage getMessage(MQQueue getQueue) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        getQueue.get(responseMsg, gmo);
        printLog("getMessage: message recieved from " + getQueue.name);
        return responseMsg;
    }
    
    /**
     * Getting first message in queue with chosen timeout
     * @param getQueueName
     * @param timeout
     * @return Returns flag of success
     */
    public MQMessage getMessage(String getQueueName, int timeout) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        gmo.options = MQConstants.MQGMO_WAIT;
        gmo.waitInterval = timeout;
        MQQueue getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
        getQueue.get(responseMsg, gmo);
        getQueue.close();
        printLog("getMessage: message recieved from " + getQueueName + " with timeout = " + new Integer(timeout).toString());
        return responseMsg;
    }
    
    /**
     * Getting first message in queue with chosen timeout
     * @param getQueueName
     * @param timeout
     * @return Returns flag of success
     */
    public MQMessage getMessage(MQQueue getQueue, int timeout) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        gmo.options = MQConstants.MQGMO_WAIT;
        gmo.waitInterval = timeout;
        getQueue.get(responseMsg, gmo);
        printLog("getMessage: message recieved from " + getQueue.name + " with timeout = " + new Integer(timeout).toString());
        return responseMsg;
    }
    
    /**
     * Getting message in qeueue with request messageId
     * @param getQueueName
     * @param request
     * @return 
     */
    public MQMessage getMessage(String getQueueName, MQMessage request) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        MQQueue getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
        responseMsg.messageId = request.messageId;
        gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;
        getQueue.get(responseMsg, gmo);
        getQueue.close();            
        printLog("getMessage: message recieved from " + getQueueName + " with settings from request");
        return responseMsg;
    }
    
    /**
     * Getting message in qeueue with request messageId
     * @param getQueueName
     * @param request
     * @return 
     */
    public MQMessage getMessage(MQQueue getQueue, MQMessage request) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        responseMsg.messageId = request.messageId;
        gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;
        getQueue.get(responseMsg, gmo);
        printLog("getMessage: message recieved from " + getQueue.name + " with settings from request");
        return responseMsg;
    }
    
    /**
     * Getting message in queue with request messageId and chosen timeout
     * @param getQueueName
     * @param request
     * @param timeout
     * @return 
     */
    public MQMessage getMessage(String getQueueName, MQMessage request, int timeout) throws Exception {
        MQQueue getQueue = null;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        gmo.options = MQConstants.MQGMO_WAIT;
        gmo.waitInterval = timeout;
        getQueue = queueMgr.accessQueue(getQueueName, MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT);
        responseMsg.messageId = request.messageId;
        gmo.matchOptions=MQConstants.MQMO_MATCH_MSG_ID;
        getQueue.get(responseMsg, gmo);
        getQueue.close();
        printLog("getMessage: message recieved from " + getQueueName + " with settings from request");    
        return responseMsg;
    }
    
    /**
     * Getting message in queue with request messageId and chosen timeout
     * @param getQueueName
     * @param request
     * @param timeout
     * @return 
     */
    public MQMessage getMessage(MQQueue getQueue, MQMessage request, int timeout) throws Exception {
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage responseMsg = new MQMessage();
        gmo.options = MQConstants.MQGMO_WAIT;
        gmo.waitInterval = timeout;
        responseMsg.messageId = request.messageId;
        gmo.matchOptions = MQConstants.MQMO_MATCH_MSG_ID;
        getQueue.get(responseMsg, gmo);          
        printLog("getMessage: message recieved from " + getQueue.name + " with settings from request");            
        return responseMsg;
    }
    
    /**
     * Getting message in queue with request messageId and chosen timeout
     * @param getQueueName
     * @param request
     * @param timeout
     * @return 
     */
    public MQMessage getMessage(MQQueue getQueue, MQMessage request, MQGetMessageOptions gmo) throws Exception {
        MQMessage responseMsg = new MQMessage();
        responseMsg.messageId = request.messageId;
        getQueue.get(responseMsg, gmo);          
        printLog("getMessage: message recieved from " + getQueue.name + " with settings from request");            
        return responseMsg;
    }
    
    /**
     * Getting message in queue with request messageId and chosen timeout
     * @param getQueueName
     * @param request
     * @param timeout
     * @return 
     */
    public MQMessage getMessage(MQQueue getQueue, MQGetMessageOptions gmo) throws Exception {
        MQMessage responseMsg = new MQMessage();
        getQueue.get(responseMsg, gmo);          
        printLog("getMessage: message recieved from " + getQueue.name + " with settings from request");            
        return responseMsg;
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
            printLog(ex.toString());
            return null;
        }
    }
    
    /**
     * Closing connection to queue manager
     * @return Returns flag of success
     */
    public boolean closeConnection() throws Exception {
        try {
            queueMgr.close();
            printLog("Connection to Queue Manager closed");
            queueMgr = null;
            return true;
        }
        catch (MQException ex) {
            printLog("Error while closeConnection");
            printLog(ex.toString());
            return false;
        }
    }
}
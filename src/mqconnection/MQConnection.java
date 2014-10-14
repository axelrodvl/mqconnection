package mqconnection;

import xmlmessage.XMLMessage;
import com.ibm.mq.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MQConnection {
    public class MQQueueConnection {
        public MQQueue queue = null;
        public String name = null;
        public int openOptions = 0;
        
        public MQQueueConnection(String queueName, int openOptions) {
            try {
                this.name = queueName;
                this.openOptions = openOptions;
                queue = queueMgr.accessQueue(queueName, openOptions);  
            }
            catch (MQException ex) {
                System.out.println("MQQueueConnection: error while starting connection");
            }
        }
        
        public void closeConnection() {
            try {
                queue.close();
                queue = null;
            }
            catch (MQException ex) {
                System.out.println("MQQueueConnection: error while closing connection");
            }
        }
    }
    
    String queueMgrName = null;
    String queueMgrHostname = null;
    int queueMgrPort = 0;
    String queueMgrChannel = null; 
    MQQueueManager queueMgr = null;
    MQQueueConnection getQueueOnce = null;
    MQQueueConnection putQueueOnce = null;
    
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
    
    public boolean sendMessageSimple(String putQueueName, String replytToQueueName, XMLMessage xmlMessage) {
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
            requestMsg.writeString(xmlMessage.toString()); // message payload
            putQueue.put(requestMsg, pmo);
            
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
    
    public XMLMessage getResponse(String putQueueName, String getQueueName, XMLMessage requestXmlMessage) {
        MQQueue putQueue = null;
        MQQueue getQueue = null;
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage requestMsg = new MQMessage();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;        
        
        try {
            putQueue = queueMgr.accessQueue(putQueueName, MQC.MQOO_BIND_NOT_FIXED | MQC.MQOO_OUTPUT);
            pmo.options = MQC.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.
            requestMsg.replyToQueueName = getQueueName; // the response should be put on this queue
            requestMsg.report=MQC.MQRO_PASS_MSG_ID; //If a report or reply is generated as a result of this message, the MsgId of this message is copied to the MsgId of the report or reply message.
            requestMsg.format = MQC.MQFMT_STRING; // Set message format. The application message data can be either an SBCS string (single-byte character set), or a DBCS string (double-byte character set). 
            requestMsg.messageType=MQC.MQMT_REQUEST; // The message is one that requires a reply.
            requestMsg.writeString(requestXmlMessage.toString()); // message payload
            putQueue.put(requestMsg, pmo);
            
            putQueue.close();
            
            System.out.println("newPutGetTransaction: request message sent to " + putQueueName);
        }
        catch (Exception ex) {
            System.out.println("newPutGetTransaction: error while sending request");
            System.out.println(ex.toString());
            return null;
        }
        
        try {    
            getQueue = queueMgr.accessQueue(getQueueName, MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT);
            responseMsg.messageId = requestMsg.messageId; // The Id to be matched against when getting a message from a queue
            //gmo.matchOptions=MQC.MQMO_MATCH_CORREL_ID; // The message to be retrieved must have a correlation identifier that matches the value of the CorrelId field in the MsgDesc parameter of the MQGET call.
            gmo.matchOptions=MQC.MQMO_MATCH_MSG_ID; // The message to be retrieved must have a correlation identifier that matches the value of the CorrelId field in the MsgDesc parameter of the MQGET call.
            gmo.options=MQC.MQGMO_WAIT; // The application waits until a suitable message arrives.
            gmo.waitInterval=60000; // timeout in ms
            getQueue.get(responseMsg, gmo);
            
            // Check the message content
            responseMsgData = responseMsg.readStringOfByteLength(responseMsg.getTotalMessageLength()).getBytes();
            
            getQueue.close();
            
            System.out.println("newPutGetTransaction: response message got from " + getQueueName);
            
            return new XMLMessage(new String(responseMsgData));
        } 
        // For JDK 1.7: catch(MQException | IOException ex) {
        
        // For JDK 1.5
        catch(Exception ex) {
            System.out.println("newPutGetTransaction: error while getting response");
            System.out.println(ex.toString());
            return null;
        }
    }
    
    public void initGetResponseStaticConnection(String putQueueName, String getQueueName) {
        putQueueOnce = new MQQueueConnection(putQueueName, MQC.MQOO_BIND_NOT_FIXED | MQC.MQOO_OUTPUT);
        getQueueOnce = new MQQueueConnection(getQueueName, MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT);
    }
    
    public XMLMessage getResponseStaticConnection(XMLMessage requestXmlMessage) {
        MQPutMessageOptions pmo = new MQPutMessageOptions();
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        MQMessage requestMsg = new MQMessage();
        MQMessage responseMsg = new MQMessage();
        byte[] responseMsgData = null;
        String msg = null;        
        
        try {
            pmo.options = MQC.MQPMO_NEW_MSG_ID; // The queue manager replaces the contents of the MsgId field in MQMD with a new message identifier.
            requestMsg.replyToQueueName = getQueueOnce.name; // the response should be put on this queue
            requestMsg.report=MQC.MQRO_PASS_MSG_ID; //If a report or reply is generated as a result of this message, the MsgId of this message is copied to the MsgId of the report or reply message.
            requestMsg.format = MQC.MQFMT_STRING; // Set message format. The application message data can be either an SBCS string (single-byte character set), or a DBCS string (double-byte character set). 
            requestMsg.messageType=MQC.MQMT_REQUEST; // The message is one that requires a reply.
            requestMsg.writeString(requestXmlMessage.toString()); // message payload
            putQueueOnce.queue.put(requestMsg, pmo);
            
            System.out.println("getResponseStaticConnection: request message sent to " + putQueueOnce.name);
        }
        catch (Exception ex) {
            System.out.println("getResponseStaticConnection: error while sending request");
            System.out.println(ex.toString());
            return null;
        }
        
        try {    
            responseMsg.messageId = requestMsg.messageId; // The Id to be matched against when getting a message from a queue
            //gmo.matchOptions=MQC.MQMO_MATCH_CORREL_ID; // The message to be retrieved must have a correlation identifier that matches the value of the CorrelId field in the MsgDesc parameter of the MQGET call.
            gmo.matchOptions=MQC.MQMO_MATCH_MSG_ID; // The message to be retrieved must have a correlation identifier that matches the value of the CorrelId field in the MsgDesc parameter of the MQGET call.
            gmo.options=MQC.MQGMO_WAIT; // The application waits until a suitable message arrives.
            gmo.waitInterval=60000; // timeout in ms
            getQueueOnce.queue.get(responseMsg, gmo);
            
            // Check the message content
            responseMsgData = responseMsg.readStringOfByteLength(responseMsg.getTotalMessageLength()).getBytes();
            
            System.out.println("getResponseStaticConnection: response message got from " + getQueueOnce.name);
            
            return new XMLMessage(new String(responseMsgData));
        } 
        // For JDK 1.7: catch(MQException | IOException ex) {
        
        // For JDK 1.5
        catch(Exception ex) {
            System.out.println("getResponseStaticConnection: error while getting response");
            System.out.println(ex.toString());
            return null;
        }
    }
    
    public void finalizeGetResponseStaticConnection() {
        getQueueOnce.closeConnection();
        putQueueOnce.closeConnection();
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
package testPackage;

import com.ibm.mq.MQMessage;
import java.io.File;
import mqconnection.MQConnection;
import testHandler.*;
import static testHandler.Test.randomUUID;
import static testHandler.Test.randomValue;
import static testHandler.Test.sleep;
import xmlmessage.XMLMessage;

public class SPM_LR extends Test {
    MQConnection mqc = null;
    XMLMessage SPMrequest_GXSD = null;
    String moduleCode = null;
    String SPRProcessId = null;
    String processCode = null;
    String messageId = null;
    String senderroremail = null;
    String sendtobackout = null;
    String msgtype = null;
    MQMessage request = null;
    MQMessage response = null;
    XMLMessage responseXML = null;
    String correlId = null;
    MQMessage log1 = null;
    MQMessage log2 = null;
    MQMessage log3 = null;
    MQMessage log4 = null;
    MQMessage log5 = null;
    MQMessage correlMessage = null;
    XMLMessage SPMresponse = null;
    MQMessage responseToSPM = null;
    MQMessage responseToSystem = null;
    
    public SPM_LR() {
        testName = "SPMTest";
        testDescription = "SPM Test Description";
    }
    
    public void init() throws Exception {
        mqc = new MQConnection("SPR2.QM", "vm-spr-01", 1420, "SYSTEM.DEF.SVRCONN");
        
        mqc.clearQueue("RU.CMX.MBRD.ADAPTER.SIEBEL.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.FACADE.SIEBEL.PROCESSING.OUT");
        mqc.clearQueue("RU.CMX.MBRD.FACADE.SIEBEL.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.FACADE.UFO.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.UTIL.MSGROUTER.IN");
        mqc.clearQueue("LOG.TO.DB");
        mqc.clearQueue("EMAIL.TO.ADMIN");
        mqc.clearQueue("RU.CMX.MBRD.ADAPTER.SCORING.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.ADAPTER.RBO.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.FACADE.RBO.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.FACADE.RBO.PROCESSING.OUT");
        mqc.clearQueue("RU.CMX.MBRD.BCKQ");
        mqc.clearQueue("RU.CMX.MBRD.UTIL.CORRELATIONQUEUE");
        mqc.clearQueue("RU.CMX.MBRD.UTIL.ERROR");
        mqc.clearQueue("RU.CMX.MBRD.UTIL.ERROR.ASYNCREQUEST.TIMEOUT");
        mqc.clearQueue("RU.CMX.MBRD.UTIL.ERROR.NOCORRELATION.RESPONSE");
        mqc.clearQueue("RU.CMX.MBRD.UTIL.ERROR.WS.BADANSWER");
        mqc.clearQueue("RU.CMX.MBRD.UTIL.ROUTES.INFO");
        mqc.clearQueue("RU.CMX.MBRD.FACADE.SPM.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.ADAPTER.SPM.PROCESSING.IN");
        mqc.clearQueue("RU.CMX.MBRD.FACADE.SPM.PROCESSING.OUT");
        mqc.clearQueue("RU.MBRD.ROUTER.IN");
        
        SPMrequest_GXSD = new XMLMessage(new File("C:\\testFiles\\GXSD_MainToSPM.xml"));
        
        moduleCode = "SPM";
        SPRProcessId = randomUUID();
        processCode = randomValue(10);
        messageId = randomValue(10);
        senderroremail = "true";
        sendtobackout = "false";
        msgtype = processCode + "#" + moduleCode;
        
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='systemSPRInfo']/*[local-name()='moduleCode']", moduleCode);
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='request']/*[local-name()='serviceData']/*[local-name()='processCode']", processCode);
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='systemSPRInfo']/*[local-name()='SPRProcessId']", SPRProcessId);
        
        request = mqc.newMessage(SPMrequest_GXSD.toString());
        
        request.setStringProperty("msgtype", msgtype);
        request.setStringProperty("msgid", messageId);
        request.setStringProperty("procid", messageId);
        request.setStringProperty("senderroremail", senderroremail);
        request.setStringProperty("sendtobackout", sendtobackout);
        
        SPMresponse = new XMLMessage(new File("C:\\testFiles\\SPMresponse.xml"));
    }
    
    public void action() throws Exception {
        mqc.sendMessage("RU.CMX.MBRD.ADAPTER.SPM.PROCESSING.IN", request);
        sleep(1000);
        response = mqc.getMessage("RU.CMX.MBRD.FACADE.SPM.PROCESSING.IN");
        responseXML = new XMLMessage(response);
        correlId = responseXML.getXpathValue("/*[local-name()='afsRequest']/*[local-name()='correlationId']");
        
        log1 = mqc.getMessage("LOG.TO.DB");
        log2 = mqc.getMessage("LOG.TO.DB");
        log3 = mqc.getMessage("LOG.TO.DB");
        correlMessage = mqc.browseMessage("RU.CMX.MBRD.UTIL.CORRELATIONQUEUE");
        
        SPMresponse.replaceXpathValue("/*[local-name()='afsResponse']/*[local-name()='correlationId']", correlId);        
        
        responseToSPM = mqc.newMessage(SPMresponse.toString());
        mqc.sendMessage("RU.CMX.MBRD.FACADE.SPM.PROCESSING.OUT", responseToSPM);
        
        sleep(1000);
        
        log4 = mqc.getMessage("LOG.TO.DB");
        log5 = mqc.getMessage("LOG.TO.DB");
        
        responseToSystem = mqc.getMessage("RU.CMX.MBRD.UTIL.MSGROUTER.IN");
    } 
    public void end() throws Exception {
        mqc.closeConnection();
        
        mqc = null;
        SPMrequest_GXSD = null;
        moduleCode = null;
        SPRProcessId = null;
        processCode = null;
        messageId = null;
        senderroremail = null;
        sendtobackout = null;
        msgtype = null;
        request = null;
        response = null;
        responseXML = null;
        correlId = null;
        log1 = null;
        log2 = null;
        log3 = null;
        log4 = null;
        log5 = null;
        correlMessage = null;
        SPMresponse = null;
        responseToSPM = null;
        responseToSystem = null;
    }
}

package testPackage;

import com.ibm.mq.MQMessage;
import java.io.File;
import mqconnection.MQConnection;
import testHandler.*;
import static testHandler.Test.randomUUID;
import static testHandler.Test.randomValue;
import static testHandler.Test.sleep;
import xmlmessage.XMLMessage;

public class SPM extends Test {
    public SPM() {
        testName = "SPMTest";
        testDescription = "SPM Test Description";
    }
    
    public void init() throws Exception {
        
    }
    public void action() throws Exception {
        MQConnection mqc = new MQConnection("SPR2.QM", "vm-spr-01", 1420, "SYSTEM.DEF.SVRCONN");
        
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
        
        XMLMessage SPMrequest_GXSD = new XMLMessage(new File("C:\\testFiles\\GXSD_MainToSPM.xml"));
        
        String moduleCode = "SPM";
        String SPRProcessId = randomUUID();
        String processCode = randomValue(10);
        String messageId = randomValue(10);
        String senderroremail = "true";
        String sendtobackout = "false";
        String msgtype = processCode + "#" + moduleCode;
        
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='systemSPRInfo']/*[local-name()='moduleCode']", moduleCode);
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='request']/*[local-name()='serviceData']/*[local-name()='processCode']", processCode);
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='systemSPRInfo']/*[local-name()='SPRProcessId']", SPRProcessId);
        
        //System.out.println("Request: ");
        //System.out.println(SPMrequest_GXSD.toString());
        
        MQMessage request = mqc.newMessage(SPMrequest_GXSD.toString());
        
        request.setStringProperty("msgtype", msgtype);
        request.setStringProperty("msgid", messageId);
        request.setStringProperty("procid", messageId);
        request.setStringProperty("senderroremail", senderroremail);
        request.setStringProperty("sendtobackout", sendtobackout);
        
        mqc.sendMessage("RU.CMX.MBRD.ADAPTER.SPM.PROCESSING.IN", request);
        
        sleep(1000);
        
        MQMessage response = mqc.getMessage("RU.CMX.MBRD.FACADE.SPM.PROCESSING.IN");
        
        //System.out.println(response.getStringProperty("msgid"));
        
        XMLMessage responseXML = new XMLMessage(response);
        
        //System.out.println(responseXML.toString());
        
        String correlId = responseXML.getXpathValue("/*[local-name()='afsRequest']/*[local-name()='correlationId']");
        
        //System.out.println("correlId = " + correlId);
        
        MQMessage log1 = mqc.getMessage("LOG.TO.DB");
        MQMessage log2 = mqc.getMessage("LOG.TO.DB");
        MQMessage log3 = mqc.getMessage("LOG.TO.DB");
        
        MQMessage correlMessage = mqc.browseMessage("RU.CMX.MBRD.UTIL.CORRELATIONQUEUE");
        
        //System.out.println("msgtype = " + correlMessage.getStringProperty("msgtype"));
        //System.out.println("msgid = " + correlMessage.getStringProperty("msgid"));
        //System.out.println("procid = " + correlMessage.getStringProperty("procid"));
        //System.out.println("senderroremail = " + correlMessage.getStringProperty("senderroremail"));
        //System.out.println("sendtobackout = " + correlMessage.getStringProperty("sendtobackout"));
        
        XMLMessage SPMresponse = new XMLMessage(new File("C:\\testFiles\\SPMresponse.xml"));
        SPMresponse.replaceXpathValue("/*[local-name()='afsResponse']/*[local-name()='correlationId']", correlId);        
        
        //System.out.println("Response to SPM:");
        //System.out.println(SPMresponse.toString());
        
        MQMessage responseToSPM = mqc.newMessage(SPMresponse.toString());
        mqc.sendMessage("RU.CMX.MBRD.FACADE.SPM.PROCESSING.OUT", responseToSPM);
        
        sleep(1000);
        
        MQMessage log4 = mqc.getMessage("LOG.TO.DB");
        MQMessage log5 = mqc.getMessage("LOG.TO.DB");
        
        MQMessage responseToSystem = mqc.getMessage("RU.CMX.MBRD.UTIL.MSGROUTER.IN");
                
        //System.out.println(responseToSystem.getStringProperty("msgid"));
        
        //System.out.println("Total");
        //System.out.println(new XMLMessage(responseToSystem).toString());
        
        mqc.closeConnection();
    } 
    public void end() throws Exception {
    }
}

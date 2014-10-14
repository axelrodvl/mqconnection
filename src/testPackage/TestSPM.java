package testPackage;

import com.ibm.mq.MQMessage;
import java.io.File;
import java.util.concurrent.TimeUnit;
import mqconnection.*;
import xmlmessage.*;



public class TestSPM {
    public String randomValue(int length) {
        String result = "";
        int temp;
        for (int i = 0; i < length; ++i) {
            temp = (int) (Math.random() * 1000 % 36) + 97;
            result += (temp < 123) ? ((char) temp) : ((char) (temp - 75));
        }
        return result;
    }
    
    public void sleep(int sleepTimeInMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTimeInMs);
        }
        catch (Exception ex) {}
    }
    
    public TestSPM() {
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
        String SPRProcessId = java.util.UUID.randomUUID().toString();
        String processCode = randomValue(10);
        String messageId = randomValue(10);
        String senderroremail = "true";
        String sendtobackout = "false";
        String msgtype = processCode + "#" + moduleCode;
        
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='systemSPRInfo']/*[local-name()='moduleCode']", moduleCode);
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='request']/*[local-name()='serviceData']/*[local-name()='processCode']", processCode);
        SPMrequest_GXSD.replaceXpathValue("/*[local-name()='Execute']/*[local-name()='systemSPRInfo']/*[local-name()='SPRProcessId']", SPRProcessId);
        
        MQMessage request = mqc.newMessage(SPMrequest_GXSD);
        try {
            request.setStringProperty("msgtype", msgtype);
            request.setStringProperty("msgid", messageId);
            request.setStringProperty("procid", messageId);
            request.setStringProperty("senderroremail", senderroremail);
            request.setStringProperty("sendtobackout", sendtobackout);
        } catch (Exception ex) {}
        /*
        mqc.sendMessage("RU.CMX.MBRD.ADAPTER.SPM.PROCESSING.IN", request);
        
        sleep(1000);
        
        //MQMessage response = mqc.getMessageSimple("RU.CMX.MBRD.FACADE.SPM.PROCESSING.IN");
        MQMessage response = mqc.getMessageSimple("LOG.TO.DB");
        
        System.out.println("Marcellus");
        
        try {
            byte[] responseMsgData = response.readStringOfByteLength(response.getTotalMessageLength()).getBytes();
            System.out.println(new String(responseMsgData));
        } catch (Exception ex) {}
        */
        
        mqc.sendMessage("LOG.TO.DB", request);
        mqc.sendMessage("LOG.TO.DB", request);
        mqc.sendMessage("LOG.TO.DB", request);
        mqc.sendMessage("LOG.TO.DB", request);
        mqc.sendMessage("LOG.TO.DB", request);
        
        MQMessage response = mqc.getMessageSimple("LOG.TO.DB");
        
        try {
            System.out.println(response.getTotalMessageLength());
            byte[] responseMsgData = response.readStringOfByteLength(response.getTotalMessageLength()).getBytes();
            System.out.println(new String(responseMsgData));
        } catch (Exception ex) {
            System.out.println("fuck");
        }
        
        response = mqc.getMessageSimple("LOG.TO.DB");
        response = mqc.getMessageSimple("LOG.TO.DB");
        
        //System.out.println(response.toString());
        
        /*
        try {
            System.out.println(request.getStringProperty("messageId"));
            System.out.println(response.getStringProperty("messageId"));
        } catch(Exception ex) {}
                */
    }
}

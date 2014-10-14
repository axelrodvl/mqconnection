package testPackage;

import mqconnection.*;
import xmlmessage.*;

public class Main {
    public static void main(String[] args) {
        MQConnection mqc = new MQConnection("WS084.TEST.QM", "localhost", 1420, "SYSTEM.DEF.SVRCONN");
        //mqc.clearQueue("MQSTUB.OUT"); 
        
        String requestXMLString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestingMQStubInput xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"InputFormat.xsd\">" +
 		"<ID>This is my ID</ID> <DateTime>2014-09-17T16:16:47Z</DateTime>  <Description>Some description from NetBeans</Description>  <MQStubVersion>333</MQStubVersion>" + 
 		"<Converting>  <FormatToConvert>TestingMQStubOutput</FormatToConvert>  <IsNeededToConvert>true</IsNeededToConvert> </Converting>" +
 		"<Data>  <ElemString>Element from String</ElemString>  <ElemInt>888</ElemInt>  <ElemDateTime>2022-12-17T09:30:47Z</ElemDateTime>" +
 		"<ElemBoolean>false</ElemBoolean> </Data></TestingMQStubInput>";
        
        XMLMessage xmlMessage = new XMLMessage(requestXMLString);
        
        System.out.println(xmlMessage.getXpathValue("/TestingMQStubInput/ID"));
        xmlMessage.replaceXpathValue("/TestingMQStubInput/ID", "XPATH");
        System.out.println(xmlMessage.getXpathValue("/TestingMQStubInput/ID"));

        //System.out.println(xmlMessage);
        
        //mqc.sendMessageSimple("MQSTUB.OUT", "MQSTUB.OUT", requestXmlMessage);
        
        
        XMLMessage responseXmlMessage = mqc.getResponse("MQSTUB.IN", "MQSTUB.OUT", xmlMessage);
        System.out.println(responseXmlMessage.toString());
        
        /*
        XMLMessage responseXmlMessage = null;
        
        mqc.initGetResponseStaticConnection("MQSTUB.IN", "MQSTUB.OUT");
        
        responseXmlMessage = mqc.getResponseStaticConnection(requestXmlMessage);
        System.out.println("1 - " + responseXmlMessage.toString());
        
        responseXmlMessage = mqc.getResponseStaticConnection(requestXmlMessage);
        System.out.println("2 - " + responseXmlMessage.toString());
        
        responseXmlMessage = mqc.getResponseStaticConnection(requestXmlMessage);
        System.out.println("3 - " + responseXmlMessage.toString());
        
        mqc.finalizeGetResponseStaticConnection();
        */
        mqc.closeConnection();
    }
}

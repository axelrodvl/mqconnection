package mqconnection;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;

public class XMLMessage {
    private Document document = null;
    private String msgBody = null;
    
    public XMLMessage() {
        System.out.println("XMLMessage: empty constructor");
    }
    
    public XMLMessage(String msgBody) {
        try {
            this.msgBody = msgBody;
            InputSource source = new InputSource(new StringReader(msgBody));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(source);
            System.out.println("XMLMessage: new message has successfully parsed from string");
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println("XMLMessage: error");
            System.out.println(ex.toString());
        }
    }
    
    private void updateMsgBody() throws Exception {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            msgBody = writer.getBuffer().toString().replaceAll("\n|\r", "");
        }
        catch (IllegalArgumentException | TransformerException ex) {
            System.out.println("XMLDocument.updateMsgBody(): error");
            System.out.println("WARNING! msgBody has not updated!");
            System.out.println(ex.toString());
            throw new Exception("XMLDocument.updateMsgBody(): error. WARNING! msgBody has not updated!");
        }
    }
    
    public String getXpathValue(String xpathToValue) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            String value = xpath.evaluate(xpathToValue, document);
            System.out.println("Value (" + xpathToValue + "): " + value);
            return value;
        }
        catch (XPathExpressionException ex) {
            System.out.println("getXpathValue: error");
            System.out.println(ex.toString());
            return null;
        }
    }
    
    public boolean replaceXpathValue(String xpathToValue, String value) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPathExpression xp = XPathFactory.newInstance().newXPath().compile(xpathToValue);
            String href = xp.evaluate(document);
            
            
            
            /*
            //NodeList links = (NodeList) xp.evaluate(document, XPathConstants.NODESET);
            
            Node node 
            
            //href.item(idx).setTextContent(textToReplace);
            href.setTextContent(textToReplace);
            
            
            document.
            
            document.renameNode(document, msgBody, xpathToValue)
            
            String value = xpath.evaluate(xpathToValue, document);
            System.out.println("Value (" + xpathToValue + "): " + value);
            return value;*/
            
            //updateMsgBody();
            return true;
        }
        catch (Exception ex) {
            System.out.println("getXpathValue: error");
            System.out.println(ex.toString());
            return false;
        }
    }
    
    @Override
    public String toString() {
        return msgBody;
    }
}
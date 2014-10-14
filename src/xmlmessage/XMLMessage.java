package xmlmessage;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class XMLMessage {
    private Document document = null;
    private String msgBody = null;
    
    public XMLMessage(String msgBody) {
        try {
            this.msgBody = msgBody;
            InputSource source = new InputSource(new StringReader(msgBody));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(source);
        } catch (Exception ex) {
            System.out.println("XMLMessage: error");
            System.out.println(ex.toString());
        }
    }
    
    public XMLMessage(File fXmlFile) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(fXmlFile);
            document.getDocumentElement().normalize();
            updateMsgBody();
        } catch (Exception ex) {
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
            msgBody = writer.getBuffer().toString();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
            throw new Exception("XMLDocument.updateMsgBody(): error. WARNING! msgBody has not updated!");
        }
    }
    
    public String getXpathValue(String xpathToValue) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            String value = xpath.evaluate(xpathToValue, document);
            return value;
        }
        catch (XPathExpressionException ex) {
            System.out.println("getXpathValue: error");
            System.out.println(ex.toString());
            return null;
        }
    }
    
    public boolean replaceXpathValue(String xPathExpression, String xPathValue) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            NodeList nodes = (NodeList) xPath.evaluate(xPathExpression, document, XPathConstants.NODESET);
            
            for (int k = 0; k < nodes.getLength(); k++)
            {
                //System.out.println(nodes.item(k).getTextContent());  // Prints original value
                nodes.item(k).setTextContent(xPathValue);
                //System.out.println(nodes.item(k).getTextContent());  // Prints 111 after
            }
            
            updateMsgBody();
            return true;
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
            return false;
        }
    }
    
    @Override
    public String toString() {
        return msgBody;
    }
}
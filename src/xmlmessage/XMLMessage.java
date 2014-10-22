package xmlmessage;

import com.ibm.mq.MQMessage;
import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class XMLMessage {
    private Document document = null;
    private String msgBody = null;

    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private XPath xpath = xpathFactory.newXPath();
    
    private void updateMsgBody() throws Exception {
        try {
            Transformer transformer = transformerFactory.newTransformer();
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
    
    private void XMLMessageFetchString(String msgBody) {
        try {
            InputSource source = new InputSource(new StringReader(msgBody));
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            document = db.parse(source);
            this.msgBody = msgBody;
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    public XMLMessage(String msgBody) {
        XMLMessageFetchString(msgBody);
    }
    
    public XMLMessage(MQMessage message) {
        try {
            byte[] data = new byte[message.getDataLength()];
            message.readFully(data, 0, message.getDataLength());
            XMLMessageFetchString(new String(data));
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    public XMLMessage(File fXmlFile) {
        try {
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            document = db.parse(fXmlFile);
            document.getDocumentElement().normalize();
            updateMsgBody();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    public String getXpathValue(String xPathExpression) {
        try {
            return xpath.evaluate(xPathExpression, document);
        }
        catch (XPathExpressionException ex) {
            System.out.println(ex.toString());
            return null;
        }
    }   
    
    public boolean replaceXpathValue(String xPathExpression, String xPathValue) {
        try {
            NodeList nodes = (NodeList) xpath.evaluate(xPathExpression, document, XPathConstants.NODESET);
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
    
    public boolean validate(File fXsdFile) {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = schemaFactory.newSchema(fXsdFile);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
            System.out.println("Document is valid");
        } catch (Exception ex) {
            System.out.println("Document is NOT valid");
            System.out.println("Reason: " + ex.toString());
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return msgBody;
    }
}
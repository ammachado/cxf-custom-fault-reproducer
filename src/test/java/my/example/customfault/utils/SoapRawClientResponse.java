package my.example.customfault.utils;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import lombok.Getter;
import lombok.Setter;
import my.example.customfault.common.InternalBusinessException;
import my.example.customfault.common.XmlUtils;

@Setter
@Getter
public class SoapRawClientResponse {

    private int httpStatusCode;
    private Document httpResponseBody;

    public Node getElementByNameWithNamespace(String namespaceUrl, String elementName) {
    	
        return httpResponseBody.getElementsByTagNameNS(namespaceUrl, elementName).item(0);
    }

    public String getElementValueByName(String elementName) {
        Node node = httpResponseBody.getElementsByTagName(elementName).item(0);
        return node.getNodeValue();
    }

    public String getBodyStringValue() {
        Node body = getElementByNameWithNamespace("http://schemas.xmlsoap.org/soap/envelope/", "Body");
       
        Node node = body.getChildNodes().item(0);
       
        return node != null ? nodeAsXml(node) : null;
    }

    public String getFaultStringValue() {
        Node fault = getElementByNameWithNamespace("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
        // The second Node (with List-Nr. 1) is the <faultstring>
        Node node = fault.getChildNodes().item(1);
        return node != null ? node.getTextContent() : null;
    }

    public <T> T getUnmarshalledObjectFromSoapMessage(Class<T> jaxbClass) throws InternalBusinessException {
        return XmlUtils.getUnmarshalledObjectFromSoapMessage(httpResponseBody, jaxbClass);
    }
    
    public String nodeAsXml(Node node) {
    	String s = null;
    	try {
    	  StringWriter writer = new StringWriter();
          Transformer transformer = TransformerFactory.newInstance().newTransformer();
          transformer.transform(new DOMSource(node), new StreamResult(writer));
           s = writer.toString();
    	}catch(Exception e) {
    		
    	}
    	return s;
    }
}

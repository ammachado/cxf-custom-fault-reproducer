package my.example.customfault.common;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;


public class SoapUtils {

	static Logger LOG = LoggerFactory.getLogger(SoapUtils.class);

	private static Document getDocument(Object obj) throws JAXBException, ParserConfigurationException {

		// EAP apps use Apache Chain to run operations
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.newDocument();

		

		

		JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(obj, document);

		return document;
	}

	
	
	public  static String replaceFaultNodeAndGetSoapString(InputStream stream,Object obj) {
		StringWriter writer = new StringWriter();
		Document mainDocument;
		try {
			mainDocument = XmlUtils.parseFileStream2Document(stream);
			
			
			Document replacementDocument = getDocument(obj);

			// Identify the node to be replaced in the main document
			NodeList nodeList = mainDocument.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/","Fault");
			System.out.println(nodeList.getLength());

			Node nodeToReplace = nodeList.item(0);
			System.out.println(nodeToReplace);

			// Get the replacement node from the replacement document
			Node replacementNode = replacementDocument.getDocumentElement();

			// Import the replacement node into the main document
			Node importedNode = mainDocument.importNode(replacementNode, true);

			// Replace the node in the main document
			nodeToReplace.getParentNode().replaceChild(importedNode, nodeToReplace);

			// Save the modified main document
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource source = new DOMSource(mainDocument);
			writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);

			LOG.debug("Node replaced successfully {}", writer);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return writer.toString();
			
	}
}

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

import de.codecentric.namespace.weatherservice.datatypes.MessageDetailType;
import de.codecentric.namespace.weatherservice.datatypes.TechnicalSeverityCodeType;
import my.example.customfault.configuration.customsoapfaults.internal.InvocationOutcomeRecorder;
import my.example.customfault.configuration.customsoapfaults.internal.MsgDtlLocatorImpl;
import my.example.customfault.configuration.customsoapfaults.internal.OutcomeLocatorImpl;
import org.apache.commons.chain.impl.ContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.codecentric.namespace.weatherservice.exception.WeatherException;
import de.codecentric.namespace.weatherservice.general.GetCityWeatherByZIPResponse;
import de.codecentric.namespace.weatherservice.general.WeatherReturn;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class SoapUtils {

	static Logger LOG = LoggerFactory.getLogger(SoapUtils.class);

	private static Document getDocument() throws JAXBException, ParserConfigurationException {

		// EAP apps use Apache Chain to run operations
		ContextBase context = new ContextBase();
		InvocationOutcomeRecorder invocationOutcomeRecorder = new InvocationOutcomeRecorder();
		invocationOutcomeRecorder.setOutcomeLocator(new OutcomeLocatorImpl());
		invocationOutcomeRecorder.setMsgDtlLocator(new MsgDtlLocatorImpl());
		invocationOutcomeRecorder.outcomeFailure(context, new MessageDetailType("id", TechnicalSeverityCodeType.F, 100200, "error"));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.newDocument();

		GetCityWeatherByZIPResponse resp = new GetCityWeatherByZIPResponse();
		WeatherReturn getCityWeatherByZIPResult = new WeatherReturn();
		getCityWeatherByZIPResult.setCity("LONDON");
		getCityWeatherByZIPResult.setResponseText("Cold");
		resp.setGetCityWeatherByZIPResult(getCityWeatherByZIPResult);

		WeatherException exception = new WeatherException();
		exception.setExceptionDetails("error");
		exception.setBusinessErrorId("id");
		exception.setBigBusinessErrorCausingMoneyLoss(true);
		exception.setUuid("BigUUID");

		JAXBContext jaxbContext = JAXBContext.newInstance(WeatherException.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(exception, document);

		return document;
	}

	public  static String replaceFaultNodeAndGetSoapString(InputStream stream) {
		StringWriter writer = new StringWriter();
		Document mainDocument;
		try {
			mainDocument = XmlUtils.parseFileStream2Document(stream);
			
			Document replacementDocument = getDocument();

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

package my.example.customfault.common;

import java.io.InputStream;
import java.io.OutputStreamWriter;
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
import org.w3c.dom.Element;
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

    public static String replaceFaultNodeAndGetSoapString(InputStream stream) {
        StringWriter writer = new StringWriter();
        Document mainDocument;
        try {
            mainDocument = XmlUtils.parseFileStream2Document(stream);

            Document replacementDocument = getDocument();

            // Identify the node to be replaced in the main document
            NodeList nodeList = mainDocument.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
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

    public static void printXML(Node node) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(node),
                new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
    }

    /**
     * Sets the specified element as the BODY of a new SOAP document
     * @param soapBodyElement   Element to set in the body of the SOAP message
     * @return  SOAP document
     * @throws Exception
     */
    public static Document setElementAsSOAPDocumentBody(Element soapBodyElement) throws Exception {
        // Create a new document
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.newDocument();

        // SOAP namespace and elements
        final String soapNamespaceUrl = "http://schemas.xmlsoap.org/soap/envelope/";
        final Element envelopeElem = document.createElementNS(soapNamespaceUrl, "soap:Envelope");
        document.appendChild(envelopeElem);
        final Element bodyElem = document.createElementNS(soapNamespaceUrl, "soap:Body");
        envelopeElem.appendChild(bodyElem);

        // Set weather exception in soap:Body
        final Node weatherNode = soapBodyElement.getFirstChild();
        soapBodyElement.removeChild(weatherNode);
        final Node importedWeatherNode = document.importNode(weatherNode, true);
        bodyElem.appendChild(importedWeatherNode);

        // Return
        return document;
    }

    public static String replaceFaultNodeAndGetSoapString(InputStream stream, Element exBodyElement) throws Exception {
        // SOAP XML string
        String soapXml = null;

        try {
            final Document soapDocument = XmlUtils.parseFileStream2Document(stream);

            // SOAP namespace and elements
            final Element soapEnvelopeElem = soapDocument.getDocumentElement();
            final NodeList soapEnvelopeNodeList = soapEnvelopeElem.getChildNodes();
            for (int i = 0; i < soapEnvelopeNodeList.getLength(); i++) {
                final Node soapEnvelopeChildNode = soapEnvelopeNodeList.item(i);
                if (soapEnvelopeChildNode.getLocalName().equals("Body")) {
                    final NodeList soapBodyNodeList = soapEnvelopeChildNode.getChildNodes();

                    // Remove FAULT node
                    for (int j = 0; j < soapBodyNodeList.getLength(); j++) {
                        final Node soapBodyChildNode = soapBodyNodeList.item(j);
                        if (soapBodyChildNode.getLocalName().equals("Fault")) {
                            soapEnvelopeChildNode.removeChild(soapBodyChildNode);
                        }
                    }

                    // Add custom exception node
                    final Node weatherNode = exBodyElement.getFirstChild();
                    exBodyElement.removeChild(weatherNode);
                    final Node importedWeatherNode = soapDocument.importNode(weatherNode, true);
                    soapEnvelopeChildNode.appendChild(importedWeatherNode);
                }
            }

            // Generate SOAP XML from document
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            final StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(soapDocument), new StreamResult(stringWriter));
            soapXml = stringWriter.toString();
        } catch (Exception ex) {
            LOG.error("Error generating SOAP XML for custom fault message", ex);
        }

        // Return
        return soapXml;
    }
}

package org.apache.cxf.jaxb;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.jaxb.JAXBUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.service.model.MessagePartInfo;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CignaDataReaderImpl<T> extends JAXBDataBase implements DataReader<T>  {
	
	 private static final Logger LOG = LogUtils.getLogger(CignaDataReaderImpl.class);
	    CignaJaxBDataBinding databinding;
	    boolean unwrapJAXBElement;
	    ValidationEventHandler veventHandler;
	    boolean setEventHandler = true;


    public CignaDataReaderImpl(CignaJaxBDataBinding myDataBinding, boolean unwrapJAXBElement2) {
    	
    	   super(myDataBinding.getContext());
    	   log.info("\n\n\nMy data binding ................{}",myDataBinding.getContext());
           unwrapJAXBElement = unwrapJAXBElement2;
           databinding = myDataBinding;
	}


	public Object read(T input) {
        return read(null, input);
    }

    private static class WSUIDValidationHandler implements ValidationEventHandler {
        ValidationEventHandler origHandler;
        WSUIDValidationHandler(ValidationEventHandler o) {
            origHandler = o;
        }

        public boolean handleEvent(ValidationEvent event) {
            // if the original handler has already handled the event, no need for us
            // to do anything, otherwise if not yet handled, then do this 'hack'
            if (origHandler != null && origHandler.handleEvent(event)) {
                return true;
            }
            // hack for CXF-3453
            String msg = event.getMessage();
            return msg != null
                && msg.contains(":Id")
                && (msg.startsWith("cvc-type.3.1.1")
                    || msg.startsWith("cvc-type.3.2.2")
                    || msg.startsWith("cvc-complex-type.3.1.1")
                    || msg.startsWith("cvc-complex-type.3.2.2"));
        }
    }

    public void setProperty(String prop, Object value) {
        if (prop.equals(JAXBDataBinding.UNWRAP_JAXB_ELEMENT)) {
            unwrapJAXBElement = Boolean.TRUE.equals(value);
        } else if (prop.equals(org.apache.cxf.message.Message.class.getName())) {
            org.apache.cxf.message.Message m = (org.apache.cxf.message.Message)value;
            veventHandler = getValidationEventHandler(m, JAXBDataBinding.READER_VALIDATION_EVENT_HANDLER);
            log.info("Event Handler set is {}",veventHandler);
            if (veventHandler == null) {
                veventHandler = databinding.getValidationEventHandler();
            }
            setEventHandler = MessageUtils.getContextualBoolean(m,
                    JAXBDataBinding.SET_VALIDATION_EVENT_HANDLER, true);

            Object unwrapProperty = m.get(JAXBDataBinding.UNWRAP_JAXB_ELEMENT);
            if (unwrapProperty == null) {
                unwrapProperty = m.getExchange().get(JAXBDataBinding.UNWRAP_JAXB_ELEMENT);
            }
            if (unwrapProperty != null) {
                unwrapJAXBElement = Boolean.TRUE.equals(unwrapProperty);
            }
        }
    }

    private Unmarshaller createUnmarshaller() {
    	log.info("Context is {}",context);
        try {
            Unmarshaller um = context.createUnmarshaller();
            if (databinding.getUnmarshallerListener() != null) {
                um.setListener(databinding.getUnmarshallerListener());
            }
            log.info("if {} Event Handler set is {}",setEventHandler,veventHandler);
            if (setEventHandler) {
                um.setEventHandler(new WSUIDValidationHandler(veventHandler));
            }
            if (databinding.getUnmarshallerProperties() != null) {
                for (Map.Entry<String, Object> propEntry
                    : databinding.getUnmarshallerProperties().entrySet()) {
                    try {
                        um.setProperty(propEntry.getKey(), propEntry.getValue());
                    } catch (PropertyException pe) {
                        LOG.log(Level.INFO, "PropertyException setting Marshaller properties", pe);
                    }
                }
            }
            um.setSchema(schema);
            um.setAttachmentUnmarshaller(getAttachmentUnmarshaller());
            for (XmlAdapter<?, ?> adapter : databinding.getConfiguredXmlAdapters()) {
                um.setAdapter(adapter);
            }
            return um;
        } catch (jakarta.xml.bind.UnmarshalException ex) {
            throw new Fault(new Message("UNMARSHAL_ERROR", LOG, ex.getLinkedException()
                .getMessage()), ex);
        } catch (JAXBException ex) {
            throw new Fault(new Message("UNMARSHAL_ERROR", LOG, ex.getMessage()), ex);
        }
    }

    public Object read(MessagePartInfo part, T reader) {
    	log.info("Read method called");
    	System.out.println("Read Method Called with Part info");
        boolean honorJaxbAnnotation = honorJAXBAnnotations(part);
		/*
		 * T copy = reader; try { if(copy instanceof XMLStreamReader) { Transformer
		 * transformer = TransformerFactory.newInstance().newTransformer(); StringWriter
		 * stringWriter = new StringWriter(); transformer.transform(new
		 * StAXSource((XMLStreamReader)copy), new StreamResult(stringWriter));
		 * System.out.println(stringWriter.toString()); }}catch(Exception e) {
		 * e.printStackTrace(); }
		 */
        System.out.println("honorJaxbAnnotation?: "+reader.getClass().getCanonicalName());
        if (honorJaxbAnnotation) {
            Annotation[] anns = getJAXBAnnotation(part);
            if (anns.length > 0) {
                // RpcLit will use the JAXB Bridge to unmarshall part message when it is
                // annotated with @XmlList,@XmlAttachmentRef,@XmlJavaTypeAdapter
                // TODO:Cache the JAXBRIContext
                QName qname = new QName(null, part.getConcreteName().getLocalPart());
                ((CignaJaxbCustomValidator)veventHandler).setErrors();
                Object obj = JAXBEncoderDecoder.unmarshalWithBridge(qname,
                                                              part.getTypeClass(),
                                                              anns,
                                                              databinding.getContextClasses(),
                                                              reader,
                                                              getAttachmentUnmarshaller());
               

                onCompleteUnmarshalling();

                return obj;
            }
        }

        Unmarshaller um = createUnmarshaller();
        try {
        	System.out.println("Before error handler......."+veventHandler.getClass().getCanonicalName());
        	if(veventHandler instanceof org.apache.cxf.jaxb.CignaJaxbCustomValidator) {
         	   List<String> errors = ((org.apache.cxf.jaxb.CignaJaxbCustomValidator)veventHandler).getErrors();
         	   System.out.println("Errors is "+errors);
         	 // ((org.apache.cxf.jaxb.CignaJaxbCustomValidator)veventHandler).getErrors().clear();
            }
        	((org.apache.cxf.jaxb.CignaJaxbCustomValidator)veventHandler).setErrors();
            Object obj = JAXBEncoderDecoder.unmarshall(um, reader, part,
                                                 unwrapJAXBElement);
           if(veventHandler instanceof org.apache.cxf.jaxb.CignaJaxbCustomValidator) {
        	   List<String> errors = ((org.apache.cxf.jaxb.CignaJaxbCustomValidator)veventHandler).getErrors();
        	   
        	   for(String error:errors) {
        		   System.out.println(error);
        	   }
           }
            onCompleteUnmarshalling();

            return obj;
        } finally {
            JAXBUtils.closeUnmarshaller(um);
        }
    }

    public Object read(QName name, T input, Class<?> type) {
    	log.info("Read Method Called QName");
    	System.out.println("Read Method Called with Qname");
        Unmarshaller um = createUnmarshaller();

        try {
            Object obj = JAXBEncoderDecoder.unmarshall(um, input,
                                             name, type,
                                             unwrapJAXBElement);
            
            if(veventHandler instanceof org.apache.cxf.jaxb.CignaJaxbCustomValidator) {
         	   List<String> errors = ((org.apache.cxf.jaxb.CignaJaxbCustomValidator)veventHandler).getErrors();
         	   log.info("Errors list is {}",errors);
            }
            onCompleteUnmarshalling();

            return obj;
        } finally {
            JAXBUtils.closeUnmarshaller(um);
        }

    }

    private void onCompleteUnmarshalling() {
        if (setEventHandler && veventHandler instanceof UnmarshallerEventHandler) {
            try {
                ((UnmarshallerEventHandler) veventHandler).onUnmarshalComplete();
            } catch (UnmarshalException e) {
                if (e.getLinkedException() != null) {
                    throw new Fault(new Message("UNMARSHAL_ERROR", LOG,
                            e.getLinkedException().getMessage()), e);
                }
                throw new Fault(new Message("UNMARSHAL_ERROR", LOG, e.getMessage()), e);
            }
        }
        if(veventHandler instanceof CignaJaxbCustomValidator) {
        	List<String> errors = ((org.apache.cxf.jaxb.CignaJaxbCustomValidator)veventHandler).getErrors();
        	if(!errors.isEmpty()) {
        		System.out.println("Hello hello ---"+String.join("--", errors));
        		//((org.apache.cxf.jaxb.CignaJaxbCustomValidator)veventHandler).getErrors().clear();
        		 throw new Fault(new Message("UNMARSHAL_ERROR", LOG,
                         String.join( "-",errors), new Exception()));
        		 
        	}
        }
    }

}

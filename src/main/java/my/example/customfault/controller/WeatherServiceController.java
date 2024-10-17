package my.example.customfault.controller;

import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailType;
import de.codecentric.namespace.weatherservice.datatypes1.MessageDetailsType;
import de.codecentric.namespace.weatherservice.datatypes1.TechnicalSeverityCodeType;
import de.codecentric.namespace.weatherservice.general.ForecastRequest;
import de.codecentric.namespace.weatherservice.general.ForecastReturn;
import my.example.customfault.configuration.customsoapfaults.internal.StandardMessages;
import my.example.customfault.configuration.customsoapfaults.internal.beans.TechnicalSeverityCodeEnum;
import my.example.customfault.transformation.GetCityForecastByZIPOutMapper;

/*
 *  Example-Controller:
 *  This Class would be responsible for Mapping from Request to internal Datamodel (and backwards),
 *  for calling Backend-Services and handling Backend-Exceptions
 *  So it decouples the WSDL-generated Classes from the internal Classes - for when the former changes,
 *  nothing or only the mapping has to be changed
 */ 

public class WeatherServiceController {
 
    public ForecastReturn getCityForecastByZIP(ForecastRequest forecastRequest) {
	    /*
	     * We leave out inbound transformation, plausibility-checking, logging, backend-calls e.g.
	     * for the moment
	     * 
	     * 
	     */
    	
    	
    	
		MessageDetailsType details =  new MessageDetailsType();
		
    	
    	if(forecastRequest.getForecastCustomer().getAge() == 0) {
    		MessageDetailType messageDetail = new MessageDetailType();
    		messageDetail.setId("1");
    		messageDetail.setTechnicalReturnCode(StandardMessages.DATA_TYPE_ERR_CODE);
    		messageDetail.setTechnicalReturnMessage("Age is not defined");
    		messageDetail.setTechnicalSeverityCode(TechnicalSeverityCodeType.F);
    		details.getMessageDetail().add(messageDetail);
    	}
    	if(forecastRequest.getProductName() == null) {
    		MessageDetailType messageDetail = new MessageDetailType();
    		messageDetail.setId("1");
    		messageDetail.setTechnicalReturnCode(StandardMessages.DATA_TYPE_ERR_CODE);
    		messageDetail.setTechnicalReturnMessage("ProductName is null");
    		messageDetail.setTechnicalSeverityCode(TechnicalSeverityCodeType.F);
    		details.getMessageDetail().add(messageDetail);
    	}
        return GetCityForecastByZIPOutMapper.mapGeneralOutlook2Forecast(details);
	}
	
	/*
	 * Other Methods would follow here...
	 */
	//public WeatherReturn getCityWeatherByZIP(ForecastRequest forecastRequest) throws Exception {}

	//public WeatherInformationReturn getWeatherInformation(String zip) throws BusinessException {}
}

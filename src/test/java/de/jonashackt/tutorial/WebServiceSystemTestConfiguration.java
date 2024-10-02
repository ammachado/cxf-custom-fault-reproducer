package de.jonashackt.tutorial;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.codecentric.namespace.weatherservice.WeatherService;
import de.jonashackt.tutorial.common.InternalBusinessException;
import de.jonashackt.tutorial.configuration.WebServiceConfiguration;
import de.jonashackt.tutorial.utils.SoapRawClient;





@Configuration
public class WebServiceSystemTestConfiguration {



//    @LocalManagementPort
//    private Integer managementPort;

    private String webServiceUrl = "http://localhost:8080"+ WebServiceConfiguration.BASE_URL + WebServiceConfiguration.SERVICE_URL;
    
    @Bean
    public WeatherService weatherServiceSystemTestClient() {
       // System.out.println("Port value is "+ port);
        JaxWsProxyFactoryBean jaxWsProxyFactory = new JaxWsProxyFactoryBean();
        jaxWsProxyFactory.setServiceClass(WeatherService.class);
        jaxWsProxyFactory.setAddress(webServiceUrl);
        return (WeatherService) jaxWsProxyFactory.create();
    }
    
    @Bean
    public SoapRawClient soapRawClient() throws InternalBusinessException {
        return new SoapRawClient(webServiceUrl, WeatherService.class);
    }
}

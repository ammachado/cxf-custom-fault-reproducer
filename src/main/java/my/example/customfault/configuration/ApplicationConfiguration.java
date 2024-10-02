package my.example.customfault.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import my.example.customfault.controller.WeatherServiceController;


@Configuration
public class ApplicationConfiguration {

	@Bean
	public WeatherServiceController weatherServiceController() {
		return new WeatherServiceController();
	}
}

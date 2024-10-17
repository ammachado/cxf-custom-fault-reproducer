package my.example.customfault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource(locations = {"cxf-beans.xml"})
public class SimpleBootCxfApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleBootCxfApplication.class, args);
    }
}

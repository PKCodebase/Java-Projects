package com.mcd.plantation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;



@SpringBootApplication
@EnableCaching
@OpenAPIDefinition(
    info = @Info(
        title       = "MCD Green Plantation Portal API",
        version     = "2.0.0",
        description = "REST API for MCD Green Plantation Drive — slot-level tree inventory, citizen bookings, payment, plantation records and certificate generation.",
        contact     = @Contact(name = "MCD Horticulture Dept.", email = "horticulture@mcd.gov.in")
    )
)
@SecurityScheme(
    name   = "bearerAuth",
    type   = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class PlantationPortalApplication extends SpringBootServletInitializer{
    
	public static void main(String[] args) {
        SpringApplication.run(PlantationPortalApplication.class, args);
    }
    
	/** Used when run as WAR **/
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    	//configure();
        return builder.sources(PlantationPortalApplication.class);
    }
}

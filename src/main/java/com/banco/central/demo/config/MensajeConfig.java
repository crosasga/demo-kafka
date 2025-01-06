package com.banco.central.demo.config;


import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "configuration.exceptions")
@Configuration
@Getter
@Setter
@Generated
public class MensajeConfig {
}

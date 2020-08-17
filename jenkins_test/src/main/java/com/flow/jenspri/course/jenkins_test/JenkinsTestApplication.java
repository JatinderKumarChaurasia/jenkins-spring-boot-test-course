package com.flow.jenspri.course.jenkins_test;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.ContainerInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Collections;

@SpringBootApplication
public class JenkinsTestApplication {

    @Bean
    public ServletRegistrationBean<AtmosphereServlet> atmosphereServlet() {
        ServletRegistrationBean<AtmosphereServlet> registration = new ServletRegistrationBean<>(
                new AtmosphereServlet(), "/chat/*");
        registration.addInitParameter("org.atmosphere.cpr.packages", "jenkins_test");
        registration.addInitParameter("org.atmosphere.interceptor.HeartbeatInterceptor"
                + ".clientHeartbeatFrequencyInSeconds", "10");
        registration.setLoadOnStartup(0);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public EmbeddedAtmosphereInitializer embeddedAtmosphereInitializer() {
        return new EmbeddedAtmosphereInitializer();
    }

    public static void main(String[] args) {
        SpringApplication.run(JenkinsTestApplication.class, args);
    }

    @Configuration
    static class MVCConfiguration implements WebMvcConfigurer {

        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            registry.addViewController("/").setViewName("forward:/home/home.html");
        }
    }

    private static class EmbeddedAtmosphereInitializer extends ContainerInitializer implements ServletContextInitializer {

        @Override
        public void onStartup(ServletContext servletContext) throws ServletException {
            onStartup(Collections.<Class<?>>emptySet(), servletContext);
        }
    }

}

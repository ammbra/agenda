package org.eu.todo.init;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

   @Bean
   public AcceptHeaderLocaleResolver localeResolver() {
      final AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
      resolver.setDefaultLocale(Locale.US);
      return resolver;
   }

}
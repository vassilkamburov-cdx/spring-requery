package bg.codexio.springframework.data.jpa.requery.config;

import bg.codexio.springframework.data.jpa.requery.resolver.FilterJsonSpecificationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterJsonSpecificationConverterConfiguration {
    @Bean
    public FilterJsonSpecificationConverter filterJsonSpecificationConverter(FilterJsonTypeConverter converter) {
        return new FilterJsonSpecificationConverter(converter);
    }
}

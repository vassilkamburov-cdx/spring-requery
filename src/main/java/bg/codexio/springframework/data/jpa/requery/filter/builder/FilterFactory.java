package bg.codexio.springframework.data.jpa.requery.filter.builder;

import bg.codexio.springframework.data.jpa.requery.payload.FilterRequestWrapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.net.URI;

public interface FilterFactory {
    Specification<?> toSpecification(
            FilterRequestWrapper<Specification<?>> filterRequestWrapper,
            Class<?> clazz
    );

    public <T> FilterRequestWrapper<Specification<T>> toDto(
            Specification<T> specification,
            Class<T> clazz
    );

    <T> RequestEntity<T> toRequestEntity(
            T body,
            MultiValueMap<String, String> headers,
            HttpMethod method,
            URI url,
            Type type
    );
}

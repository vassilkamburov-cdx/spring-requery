package bg.codexio.springframework.data.jpa.requery.filter.builder;

import bg.codexio.springframework.data.jpa.requery.config.ReversibleSpecificationFactory;
import bg.codexio.springframework.data.jpa.requery.payload.FilterRequestWrapper;
import bg.codexio.springframework.data.jpa.requery.resolver.FilterJsonSpecificationConverter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

@Component
public class DefaultFilterFactory
        implements FilterFactory {
    private final ReversibleSpecificationFactory reversibleSpecificationFactory;
    private final FilterJsonSpecificationConverter specificationConverter;

    public DefaultFilterFactory(
            ReversibleSpecificationFactory reversibleSpecificationFactory,
            FilterJsonSpecificationConverter specificationConverter
    ) {
        this.reversibleSpecificationFactory = reversibleSpecificationFactory;
        this.specificationConverter = specificationConverter;
    }

    @Override
    public Specification<?> toSpecification(
            FilterRequestWrapper<Specification<?>> filterRequestWrapper,
            Class<?> clazz
    ) {
        return filterRequestWrapper.isSimple(simpleFilter -> this.specificationConverter.getSimpleFilterSpecification(
                                           simpleFilter,
                                           clazz
                                   ))
                                   .orComplex(complexFilter -> this.specificationConverter.getComplexFilterSpecification(
                                           complexFilter,
                                           clazz
                                   ))
                                   .or(this.specificationConverter::noFilterSpecification);
    }

    @Override
    public <T> FilterRequestWrapper<Specification<T>> toDto(
            Specification<T> specification,
            Class<T> clazz
    ) {
        var reverseSpec = reversibleSpecificationFactory.create(
                                                                specification,
                                                                clazz
                                                        )
                                                        .toRequest();
        if (Objects.isNull(reverseSpec.rightSideOperands())
                && Objects.isNull(reverseSpec.nonPriorityGroupOperators())) {
            return new FilterRequestWrapper<>(Arrays.asList(reverseSpec.groupOperations()));
        } else {
            return new FilterRequestWrapper<>(reverseSpec);
        }
    }

    @Override
    public <T> RequestEntity<T> toRequestEntity(
            T body,
            MultiValueMap<String, String> headers,
            HttpMethod method,
            URI url,
            Type type
    ) {
        return new RequestEntity<T>(
                body,
                headers,
                method,
                url,
                type
        );
    }
}

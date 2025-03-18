package bg.codexio.springframework.data.jpa.requery.resolver;

import bg.codexio.springframework.data.jpa.requery.adapter.HttpFilterAdapter;
import bg.codexio.springframework.data.jpa.requery.payload.FilterRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * A Spring MVC argument resolver for converting JSON-encoded filter criteria
 * into {@link Specification} objects. This resolver allows for complex
 * filtering strategies to be applied to JPA entity queries based on JSON
 * input from web requests, supporting both simple and complex structured
 * filters.
 */
@Component
public class FilterJsonArgumentResolver
        implements HandlerMethodArgumentResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<HttpFilterAdapter> activeAdapters;
    private final FilterJsonSpecificationConverter specificationConverter;

    public FilterJsonArgumentResolver(
            List<HttpFilterAdapter> activeAdapters,
            FilterJsonSpecificationConverter specificationConverter
    ) {
        this.activeAdapters = activeAdapters;
        this.specificationConverter = specificationConverter;
    }

    /**
     * Determines if this resolver is applicable for the method parameter,
     * specifically checking if the parameter is of type {@link Specification}.
     *
     * @param parameter the method parameter to check
     * @return true if the parameter is a {@link Specification}, false otherwise
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameter()
                        .getType()
                        .equals(Specification.class);
    }

    /**
     * Resolves a method parameter into an argument value from a given web
     * request.
     *
     * @param parameter  the method parameter to resolve
     * @param webRequest the {@link NativeWebRequest} being handled
     * @return the resolved {@link Specification} object, or {@code null} if
     * no filters are provided
     * @throws Exception if an error occurs during argument resolution
     */
    @Override
    public Object resolveArgument(
            @NotNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @NotNull NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        var request = webRequest.getNativeRequest(HttpServletRequest.class);
        var genericType =
                (Class<?>) ((ParameterizedType) parameter.getGenericParameterType()).getActualTypeArguments()[0];
        this.logger.debug(
                "{} active adapters will be tested against the request",
                this.activeAdapters.size()
        );
        return this.activeAdapters.stream()
                                  .peek(adapter -> this.logger.debug(
                                          "Invoking {}'s supports method",
                                          adapter.getClass()
                                                 .getSimpleName()
                                  ))
                                  .filter(adapter -> adapter.supports(request))
                                  .peek(adapter -> this.logger.debug(
                                          "{} supports this request and will "
                                                  + "attempt to adapt it.",
                                          adapter.getClass()
                                                 .getSimpleName()
                                  ))
                                  .findFirst()
                                  .map(httpFilterAdapter -> httpFilterAdapter.adapt(request))
                                  .orElse(new FilterRequestWrapper<>())
                                  .isSimple(simpleFilter -> this.specificationConverter.getSimpleFilterSpecification(
                                          simpleFilter,
                                          genericType
                                  ))
                                  .orComplex(complexFilter -> this.specificationConverter.getComplexFilterSpecification(
                                          complexFilter,
                                          genericType
                                  ))
                                  .or(this.specificationConverter::noFilterSpecification);
    }
}
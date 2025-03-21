package bg.codexio.springframework.data.jpa.requery.adapter;

import bg.codexio.springframework.data.jpa.requery.payload.FilterGroupRequest;
import bg.codexio.springframework.data.jpa.requery.payload.FilterRequest;
import bg.codexio.springframework.data.jpa.requery.payload.FilterRequestWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The default filter adapter that processes JSON filters from
 * {@link HttpServletRequest}.
 * It adapts JSON-based filter requests or complex filter requests into
 * {@link FilterRequestWrapper}.
 * This implementation uses {@link ObjectMapper} for JSON deserialization.
 */
@Component
public class JsonHttpFilterAdapter
        implements HttpFilterAdapter {
    private final Logger logger =
            LoggerFactory.getLogger(JsonHttpFilterAdapter.class);
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new {@code JsonHttpFilterAdapter} with the given {@code
     * ObjectMapper}.
     *
     * @param objectMapper the {@code ObjectMapper} used for JSON
     *                     deserialization
     */
    public JsonHttpFilterAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Determines whether the given {@link HttpServletRequest} supports
     * filtering operations.
     * <p>
     * This method checks if the request contains any filter parameters,
     * specifically
     * the "filter" or "complexFilter" parameters, which indicates that the
     * request
     * is related to filtering operations.
     * </p>
     *
     * @param req the {@link HttpServletRequest} to evaluate
     * @return {@code true} if the request contains either "filter" or
     * "complexFilter" parameters,
     * {@code false} otherwise
     */
    @Override
    public boolean supports(HttpServletRequest req) {
        return req.getParameter("filter") != null
                || req.getParameter("complexFilter") != null;
    }

    /**
     * Adapts the filter parameters from the given {@link HttpServletRequest}
     * into a {@link FilterRequestWrapper}.
     * It reads the "filter" and "complexFilter" parameters from the request
     * and attempts to parse them.
     *
     * @param webRequest the HTTP servlet request containing filter parameters
     * @param <T>        the type of the result in the
     *                   {@link FilterRequestWrapper}
     * @return a {@link FilterRequestWrapper} containing the parsed filter
     * requests or an empty wrapper if parsing fails
     */
    @Override
    public <T> FilterRequestWrapper<T> adapt(HttpServletRequest webRequest) {
        var filterJson = webRequest.getParameter("filter");
        var complexFilterJson = webRequest.getParameter("complexFilter");

        try {
            if (filterJson != null) {
                return constructSimpleFilterWrapper(filterJson);
            } else if (complexFilterJson != null) {
                return constructComplexFilterWrapper(complexFilterJson);
            } else {
                return new FilterRequestWrapper<>();
            }
        } catch (JsonProcessingException e) {
            this.logger.error(
                    e.getMessage(),
                    e
            );

            return new FilterRequestWrapper<>();
        }
    }

    /**
     * Constructs a {@link FilterRequestWrapper} from a simple filter JSON
     * string.
     * If the filter JSON represents a single filter, it is wrapped in a
     * list; otherwise, an array of filters is expected.
     *
     * @param filterJson the JSON string representing the filter(s)
     * @param <T>        the type of the result in the
     *                   {@link FilterRequestWrapper}
     * @return a {@link FilterRequestWrapper} containing the parsed filter
     * request(s)
     * @throws JsonProcessingException if JSON parsing fails
     */
    private <T> FilterRequestWrapper<T> constructSimpleFilterWrapper(String filterJson)
            throws JsonProcessingException {
        if (!filterJson.startsWith("[")) {
            var filterRequest = this.objectMapper.readValue(
                    filterJson,
                    FilterRequest.class
            );
            if (filterRequest == null) {
                return new FilterRequestWrapper<>();
            }

            return new FilterRequestWrapper<>(List.of(filterRequest));
        }
        List<FilterRequest> filterList = List.of(this.objectMapper.readValue(
                filterJson,
                FilterRequest[].class
        ));
        return new FilterRequestWrapper<>(filterList);
    }

    /**
     * Constructs a {@link FilterRequestWrapper} from a complex filter JSON
     * string.
     * The JSON string is expected to represent a {@link FilterGroupRequest}.
     *
     * @param complexFilterJson the JSON string representing the complex filter
     * @param <T>               the type of the result in the
     *                          {@link FilterRequestWrapper}
     * @return a {@link FilterRequestWrapper} containing the parsed
     * {@link FilterGroupRequest}
     * @throws JsonProcessingException if JSON parsing fails
     */
    private <T> FilterRequestWrapper<T> constructComplexFilterWrapper(String complexFilterJson)
            throws JsonProcessingException {
        var filterGroupRequest = this.objectMapper.readValue(
                complexFilterJson,
                FilterGroupRequest.class
        );

        return new FilterRequestWrapper<>(filterGroupRequest);
    }
}

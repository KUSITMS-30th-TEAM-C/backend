package spring.backend.core.configuration.argumentresolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.stream.Stream;

@Component
@Log4j2
public class ClientIpArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
    };

    private static final String LOCAL_HOST_IPV6 = "0:0:0:0:0:0:0:1";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ClientIp.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = ((ServletWebRequest)webRequest).getRequest();
        return extractIpFromServlet(request);
    }

    private String extractIpFromServlet(HttpServletRequest request) {
        String ipList = Stream.of( IP_HEADER_CANDIDATES)
                .map(request::getHeader)
                .filter(header -> header != null && !header.isEmpty())
                .findFirst()
                .orElseGet(request::getRemoteAddr);
        String ip =  getIpFromIpList(ipList);

        return ip;
    }

    private String getIpFromIpList(String ipList) {
        if (ipList.contains(",")) {
            return Stream.of(ipList.split(","))
                    .map(String::trim)
                    .filter(ip -> !ip.equalsIgnoreCase("unknown"))
                    .findFirst()
                    .orElse("");

        }

        if (ipList.equals(LOCAL_HOST_IPV6)) {
            return "127.0.0.1";
        }

        return ipList;
    }
}


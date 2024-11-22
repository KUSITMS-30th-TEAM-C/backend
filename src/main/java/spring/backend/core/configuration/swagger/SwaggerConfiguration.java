package spring.backend.core.configuration.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import spring.backend.core.exception.error.BaseErrorCode;
import spring.backend.core.presentation.ErrorResponse;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "조각조각 API", description = "조각조각 : API 명세서", version = "v1.0.0"),
        servers = {@Server(url = "${springdoc.server-url}", description = "Https Server URL")}
)
public class SwaggerConfiguration {

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme cookieAuth = new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.COOKIE)
            .name("access_token");

    SecurityRequirement securityRequirement = new SecurityRequirement().addList("cookieAuth");

    Parameter accessTokenParam = new Parameter()
            .in("cookie")
            .name("access_token")
            .schema(new StringSchema())
            .required(false);

    return new OpenAPI()
            .components(new Components()
                    .addSecuritySchemes("cookieAuth", cookieAuth)
                    .addParameters("accessToken", accessTokenParam))
            .security(Arrays.asList(securityRequirement));
  }

  @Bean
  public OperationCustomizer operationCustomizer() {
    return (Operation operation, HandlerMethod handlerMethod) -> {
      ApiErrorCode apiErrorCode = handlerMethod.getMethodAnnotation(ApiErrorCode.class);
      if (apiErrorCode != null) {
        generateErrorCodeResponseExample(operation, apiErrorCode.value());
      }
      return operation;
    };
  }

  private void generateErrorCodeResponseExample(Operation operation, Class<? extends BaseErrorCode>[] types) {
    ApiResponses responses = operation.getResponses();
    List<ExampleHolder> exampleHolders = new ArrayList<>();

    for (Class<? extends BaseErrorCode> type : types) {
      BaseErrorCode[] errorCodes = type.getEnumConstants();
      Arrays.stream(errorCodes).map(
              baseErrorCode -> ExampleHolder.builder()
                      .holder(getSwaggerExample(baseErrorCode))
                      .code(baseErrorCode.getHttpStatus().value())
                      .name(baseErrorCode.name())
                      .build()
      ).forEach(exampleHolders::add);
    }

    Map<Integer, List<ExampleHolder>> statusWithExampleHolders = new HashMap<>(
            exampleHolders.stream()
                    .collect(Collectors.groupingBy(ExampleHolder::getCode)));

    addExamplesToResponses(responses, statusWithExampleHolders);
  }

  private Example getSwaggerExample(BaseErrorCode baseErrorCode) {
    ErrorResponse errorResponse = ErrorResponse.createSwaggerErrorResponse()
            .baseErrorCode(baseErrorCode)
            .build();
    Example example = new Example();
    example.setValue(errorResponse);
    return example;
  }

  private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
    statusWithExampleHolders.forEach(
            (status, value) -> {
              Content content = new Content();
              MediaType mediaType = new MediaType();
              ApiResponse apiResponse = new ApiResponse();
              value.forEach(exampleHolder -> mediaType.addExamples(exampleHolder.getName(),
                      exampleHolder.getHolder()));
              content.addMediaType("application/json", mediaType);
              apiResponse.setContent(content);
              responses.addApiResponse(status.toString(), apiResponse);
            }
    );
  }
}

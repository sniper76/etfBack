package com.etf.config;

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

//	  @Bean
//	  public OpenAPI openAPI(@Value("${springdoc.version}") String springdocVersion) {
//	    Info info = new Info()
//	        .title("타이틀 입력")
//	        .version(springdocVersion)
//	        .description("API에 대한 설명 부분");
//
//	    return new OpenAPI()
//	        .components(new Components())
//	        .info(info);
//	  }
//	}

	@Bean
	public GroupedOpenApi customTestOpenAPi() {
		String[] paths = { "/api/**", "/file/**" };

		return GroupedOpenApi.builder().group("테스트 관련 API").pathsToMatch(paths)
				.addOpenApiCustomiser(buildSecurityOpenApi()).build();
	}

	public OpenApiCustomiser buildSecurityOpenApi() {
		// jwt token 을 한번 설정하면 header 에 값을 넣어주는 코드, 자세한건 아래에 추가적으로 설명할 예정
		return OpenApi -> OpenApi.addSecurityItem(new SecurityRequirement().addList("jwt token")).getComponents()
				.addSecuritySchemes("jwt token",
						new SecurityScheme().name("Authorization").type(SecurityScheme.Type.HTTP)
								.in(SecurityScheme.In.HEADER).bearerFormat("JWT").scheme("bearer"));
	}
}
package com.github.jambeez.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    fun postsApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(ApiInfoBuilder().title("JamBeez").version("0.1").build())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.github.jambeez.server"))
            .paths(PathSelectors.any())
            .build();
    }
}
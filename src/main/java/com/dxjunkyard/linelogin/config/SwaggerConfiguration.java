package com.dxjunkyard.linelogin.config;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket swaggerSpringMvcPlugin() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("micro-volunteer-api")    // APIドキュメントをグルーピングするための識別名
                .select()
                .paths(paths())
                .build()
                .apiInfo(apiInfo());
    }

    private Predicate<String> paths() {
        return Predicates.or(Predicates.containsPattern("/v1/api*"));
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo("micro-volunteer API", "micro-volunteer platformを利用するためのAPIです。",
                "v1", "", "http://micro-volunteer-supporter.com", "", "");
        return apiInfo;
    }
}

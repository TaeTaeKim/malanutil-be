package taeyun.malanalter.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI{
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(SecurityScheme.In.HEADER)
            .name("Authorization");

        // Security Requirement 정의
        val securityRequirement = SecurityRequirement().addList("BearerAuth");

        return OpenAPI()
            .info(Info().title("Malangg Alerter API")
                .description("메랜지지 알람 API")
                .version("v1.0")
            )
            .addSecurityItem(securityRequirement)  // Security Requirement 추가
            .schemaRequirement("BearerAuth", securityScheme);
    }
}
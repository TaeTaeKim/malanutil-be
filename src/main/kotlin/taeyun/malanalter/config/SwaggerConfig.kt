package taeyun.malanalter.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI{
        return OpenAPI().components(Components()).info(configurationInfo())
    }

    private fun configurationInfo(): Info {
        return Info()
            .title("MalanGG Alerter")
            .description("매렌지지 알람 서버 API DOC")
            .version("0.0.1")
    }
}
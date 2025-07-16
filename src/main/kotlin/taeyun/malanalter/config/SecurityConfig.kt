package taeyun.malanalter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import taeyun.malanalter.auth.AlerterCookieOauth2AuthRequestRepository
import taeyun.malanalter.auth.JwtAuthExceptionFilter
import taeyun.malanalter.auth.JwtAuthenticationFilter
import taeyun.malanalter.auth.discord.DiscordOAuth2UserService
import taeyun.malanalter.auth.discord.OAuth2SuccessHandler

@Configuration
@EnableWebSecurity
class SecurityConfig(
    val discordOAuth2UserService: DiscordOAuth2UserService,
    val oAuth2SuccessHandler: OAuth2SuccessHandler,
    val jwtAuthenticationFilter: JwtAuthenticationFilter,
    val jwtAuthExceptionFilter: JwtAuthExceptionFilter,
    val alerterCookieOauth2AuthRequestRepository: AlerterCookieOauth2AuthRequestRepository
) {
    companion object {
        private val openedUrlMatcher = arrayOf(
            // swagger API
            "/swagger-ui/**",
            "/favicon.ico",
            "/.well-known/**",
            "/v3/api-docs/**",
            // login, sing up
            "/alerter/auth/**",
            "/timer/image"
        )

        fun getOpenUrlMatchers(): Array<AntPathRequestMatcher> {
            return openedUrlMatcher.map { AntPathRequestMatcher(it) }.toTypedArray()
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf(CsrfConfigurer<HttpSecurity>::disable)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint{
                        it.authorizationRequestRepository(alerterCookieOauth2AuthRequestRepository)
                    }
                    .userInfoEndpoint { it.userService(discordOAuth2UserService) }
                    .successHandler(oAuth2SuccessHandler)
            }
            .authorizeHttpRequests {
                it.requestMatchers(*getOpenUrlMatchers())
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .formLogin(FormLoginConfigurer<HttpSecurity>::disable)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthExceptionFilter, jwtAuthenticationFilter::class.java)
            .build()
    }
}
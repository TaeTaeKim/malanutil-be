package taeyun.malanalter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import taeyun.malanalter.auth.JwtAuthenticationFilter

@Configuration
class SecurityConfig (
    val jwtAuthenticationFilter: JwtAuthenticationFilter
){
    companion object{
        private val openedUrlMatcher =  arrayOf(
            "/api/**",
            "/api-docs/**",
            "/swagger-ui/**",
            "/malan-alter/auth/login",
            "/malan-alter/auth/register"
        )

        fun getOpenUrlMatchers(): Array<AntPathRequestMatcher> {
             return openedUrlMatcher.map { AntPathRequestMatcher(it) }.toTypedArray()
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity) = http
        .csrf(CsrfConfigurer<HttpSecurity>::disable)
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.requestMatchers(*getOpenUrlMatchers())
                .permitAll()
                .anyRequest()
                .authenticated()
        }
        .formLogin(FormLoginConfigurer<HttpSecurity>::disable)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()
}
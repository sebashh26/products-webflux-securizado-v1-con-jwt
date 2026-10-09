package security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import service.UserDetailService;

import java.util.List;

@Configuration// si pongo @configuration muy problablemente dentro tengo @beans
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    //3., el 4 ya es el controlador
    private final AuthManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserDetailService userDetailsService;



    //1:generar un map de usuarios
    @Bean
    public ReactiveUserDetailsService users() {
        return userDetailsService::findByUsername; //username -> userDetailsService.findByUsername(username);

    }

    //2. genearar un objecto de secutiry
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf( csrfSpec -> csrfSpec.disable())
                //.authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange( authorizeRequests ->
                    authorizeRequests.pathMatchers(HttpMethod.POST, "/alta").hasAnyRole("ADMIN")
                            .pathMatchers(HttpMethod.DELETE, "/eliminar/**").hasAnyRole("ADMIN", "OPERATOR")
                            .pathMatchers("productos/**").authenticated()
                            .anyExchange().permitAll()
                ).build();
                //.httpBasic(Customizer.withDefaults());//como se realiza la presentacion de credenciales mediante auth basica, usuario presneta credenciales en un encabezad;

        //return http.build();
    }

}

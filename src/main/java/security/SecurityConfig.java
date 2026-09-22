package security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import java.util.List;

@Configuration// si pongo @configuration muy problablemente dentro tengo @beans
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    //3., el 4 ya es el controlador
    private AuthManager authenticationManager;
    private SecurityContextRepository securityContextRepository;

    public SecurityConfig(AuthManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    //1:generar un map de usuarios
    @Bean
    public MapReactiveUserDetailsService mapReactiveUserDetailsService() {
        List<UserDetails> users = List.of(
                User.withUsername("user1")
                        .password("user1")
                        .roles("USERS").build(),
                User.withUsername("admin")
                        .password("admin")
                        .roles("USERS", "ADMIN").build(),
                User.withUsername("user2")
                        .password("user2")
                        .roles("OPERATOR").build()
        );

        return new MapReactiveUserDetailsService(users);
    }

    //2. genearar un objecto de secutiry
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf( csrfSpec -> csrfSpec.disable())
                .authenticationManager(authenticationManager)
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

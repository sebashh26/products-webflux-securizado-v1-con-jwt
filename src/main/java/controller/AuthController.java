package controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import model.Credentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.stream.Collectors;

//4.
@RestController
public class AuthController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXPIRATION_TIME = 180_000L;

    //obj q hace la autentication
    ReactiveUserDetailsService userDetailsService;

    public AuthController(ReactiveUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

//    Línea de tiempo
//    ARRANQUE (una vez)
//    Spring ejecuta users()
//  └── crea el puente y lo guarda
//            (no se busca ningún usuario, no hay "admin" todavía)
//
//        ... la aplicación queda corriendo ...
//
//    LOGIN de "admin" (cada vez que alguien entra)
//            puente.findByUsername("admin")
//            └── el puente llama a userDetailsService.findByUsername("admin")
//            └── se ejecuta TU método: BD, flatMap, map, UserDetails
    @PostMapping(value="login", consumes = MediaType.APPLICATION_JSON_VALUE,produces=MediaType.TEXT_PLAIN_VALUE)
    public Mono<ResponseEntity<String>> login(@RequestBody Credentials credentials){
        //si el usuario es válido genera un token con su información y se la envía al cliente
        //para que éste la utilice en las llamadas a los recursos
        return userDetailsService.findByUsername(credentials.getUser())
                .filter(details -> credentials.getPwd().equals(details.getPassword()))
                .map(details -> new ResponseEntity<>(getToken(details), HttpStatus.OK))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }
    //genera el token y lo envía al cliente
    private String getToken(UserDetails details) {
        //en el body del token se incluye el usuario
        //y los roles a los que pertenece, además
        //de la fecha de caducidad y los datos de la firma
        return Jwts.builder()
                .subject(details.getUsername()) //usuario
                .issuedAt(new Date())
                .claim("authorities",details.getAuthorities().stream() //roles
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) //fecha caducidad
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))//clave y algoritmo para firma
                .compact(); //generación del token

    }



}

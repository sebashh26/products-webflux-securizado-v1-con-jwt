package service;

import lombok.RequiredArgsConstructor;
import model.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import repository.RolRepository;
import repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {

    private final RolRepository rolRepository;
    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {

//        Paso a paso:
//
//        1. Tengo:                       📦[ ["USERS","ADMIN"] ]
//        2. map abre la caja, saca:      ["USERS","ADMIN"]
//        3. La lambda devuelve:          UserDetails{admin, admin123, ROLE_USERS, ROLE_ADMIN}   ← valor normal
//        4. map lo mete en una caja:     📦[ UserDetails ]
//        Resultado: Mono<UserDetails> ✅


        return userRepository.findByUser(username) //Mono<Usuario>
                .flatMap((Usuario us) -> rolRepository.findByIdUser(username)   //Flux<Rol>
                        .map(r -> r.getId().getRol()) //Flux<String>
                        .collectList()//Mono<List<String>>
                        .map(roles -> User.withUsername(us.getUser())
                                .password(us.getPwd())
                                .roles(roles.toArray(new String[0]))
                                .build())) //Mono<UserDetails>
                .switchIfEmpty(Mono.empty());
    }
}

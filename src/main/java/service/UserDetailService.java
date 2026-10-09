package service;

import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface UserDetailService {

    Mono<UserDetails> findByUsername(String username);
}

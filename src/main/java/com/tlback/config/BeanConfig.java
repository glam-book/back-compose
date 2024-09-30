package com.tlback.config;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tlback.domain.Role;
import com.tlback.domain.User;
import com.tlback.repos.UserRepository;

@Configuration
public class BeanConfig {

    @Bean
    User mockUser(UserRepository userRepository) {
        var u = new User();
        u.setName("test");
        u.setLastName("test");
        u.setMiddleName("test");
        u.setLogin("test");

        var role = new Role();
        role.setName("USER");

        u.setRoles(Set.of(role));
    
        return userRepository.save(u);
    }
}

package org.example.forsapidev;


import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.Role;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DefaultUserConfig {

    @Bean
    public CommandLineRunner createDefaultUser(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {


            // Vérifier si la table des utilisateurs est vide
            if (userRepository.count() == 0) {
                // Ajouter l'utilisateur par défaut
                User user = new User();
                user.setUsername("admin");
                user.setEmail("admin@forsa.com");
                user.setIsActive(true);
                user.setPasswordHash(passwordEncoder.encode("admin@2025"));
                if(roleRepository.count() == 0){
                    Role role = new Role();
                    role.setName(ERole.ADMIN);
                    Role role1 = new Role();
                    role1.setName(ERole.CLIENT);
                    Role role2 = new Role();
                    role2.setName(ERole.AGENT);
                    roleRepository.save(role);
                    roleRepository.save(role1);
                    roleRepository.save(role2);
                }
                Role role = roleRepository.findByName(ERole.ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                user.setRole(role);
                userRepository.save(user);
            }



        };
    }
}
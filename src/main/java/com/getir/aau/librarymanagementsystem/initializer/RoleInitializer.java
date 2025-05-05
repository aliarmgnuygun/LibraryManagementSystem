package com.getir.aau.librarymanagementsystem.initializer;

import com.getir.aau.librarymanagementsystem.model.entity.ERole;
import com.getir.aau.librarymanagementsystem.model.entity.Role;
import com.getir.aau.librarymanagementsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initRoles();
    }

    private void initRoles() {
        createRoleIfNotExists(ERole.ROLE_USER,
                "Regular user: can view available books, borrow/return books, and manage own profile");

        createRoleIfNotExists(ERole.ROLE_LIBRARIAN,
                "Librarian: can manage all books and all users");
    }

    private void createRoleIfNotExists(ERole roleEnum, String description) {
        roleRepository.findByName(roleEnum).orElseGet(() -> {
            Role role = Role.builder()
                    .name(roleEnum)
                    .description(description)
                    .build();
            return roleRepository.save(role);
        });
    }
}

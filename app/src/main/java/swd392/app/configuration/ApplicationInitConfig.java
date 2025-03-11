package swd392.app.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import swd392.app.entity.Role;
import swd392.app.entity.User;
import swd392.app.entity.Warehouse;
import swd392.app.enums.UserStatus;
import swd392.app.repository.RoleRepository;
import swd392.app.repository.WarehouseRepository;
import swd392.app.repository.UserRepository;

import java.util.List;



@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository, WarehouseRepository warehouseRepository) {
        return args -> {
            if (roleRepository.count() == 0 ) {
                // Lấy Role ADMIN từ database
                List<Role> roles = List.of(
                        new Role("AD", "ADMIN","admin"),
                        new Role("MA", "MANAGER", "manager"),
                        new Role("ST", "STAFF","staff")

                );
                roleRepository.saveAll(roles);
                log.warn("All roles have been created");
            }

            if (warehouseRepository.count() == 0 ) {
                // Lấy Role ADMIN từ database
                List<Warehouse> warehouses = List.of(
                        new Warehouse("1","W1", "Ware house 1","Ha Noi"),
                        new Warehouse("2","W2", "Ware house 2", "Ho Chi Minh"),
                        new Warehouse("3","W3", "Ware house 3","Can Tho")

                );
                warehouseRepository.saveAll(warehouses);
                log.warn("All warehouses have been created");
            }

            if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {

                // Lấy Role ADMIN từ database
                Role adminRole = roleRepository.findByRoleType("ADMIN")
                        .orElseThrow(() ->  new RuntimeException("Role ADMIN not found!"));

                Warehouse warehouse = warehouseRepository.findByWarehouseCode("W1")
                        .orElseThrow(() -> new RuntimeException("warehouse W1 not found"));

                User user = User.builder()
                        .userCode("USR001")
                        .userName("admin")
                        .fullName("admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin123"))
                        .status(UserStatus.active)
                        .role(adminRole)  // Gán Role entity
                        .warehouse(warehouse)
                        .build();

                userRepository.save(user);
                log.warn("Admin has been created with default password admin123, please change it");
            }
        };
    }
}
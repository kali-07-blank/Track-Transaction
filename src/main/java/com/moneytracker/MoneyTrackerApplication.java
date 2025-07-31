package com.moneytracker;

import com.moneytracker.entity.User;
import com.moneytracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MoneyTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoneyTrackerApplication.class, args);
    }

    @Bean
    public CommandLineRunner createDefaultUser(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("1234"); // 🔑 plain text (for demo only!)
                userRepository.save(admin);
                System.out.println("✅ Default admin user created: admin / 1234");
            } else {
                System.out.println("ℹ️ Default admin user already exists.");
            }
        };
    }
}

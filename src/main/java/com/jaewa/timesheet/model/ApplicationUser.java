package com.jaewa.timesheet.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "application_user")
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="username", length=50, nullable=false, unique=true)
    String username;

    @Column(name="first_name", length=50, nullable=false)
    String firstName;

    @Column(name="last_name", length=50, nullable=false)
    String lastName;

    @Column(name="email", length=50, nullable=false, unique=true)
    String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

}

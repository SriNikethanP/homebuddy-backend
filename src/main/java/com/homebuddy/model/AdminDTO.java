package com.homebuddy.model;

import lombok.Data;

@Data
public class AdminDTO {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private boolean enabled = true;
} 
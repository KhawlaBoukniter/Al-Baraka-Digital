package org.albarakadigital.service;


import org.albarakadigital.entity.User;

public interface ClientService {
    User createClient(String email, String password, String fullName);
    User getUserByEmail(String email);
}

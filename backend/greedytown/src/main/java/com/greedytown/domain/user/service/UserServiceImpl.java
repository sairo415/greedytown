package com.greedytown.domain.user.service;

import com.greedytown.domain.user.dto.UserDto;
import com.greedytown.domain.user.model.User;
import com.greedytown.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public boolean insertUser(UserDto userDto) {
        userDto.setUserPassword(bCryptPasswordEncoder.encode(userDto.getUserPassword()));
        User user = User.builder()
                .userNickname(userDto.getUserNickname())
                .userEmail(userDto.getUserEmail())
                .userPassword(userDto.getUserPassword())
                .build();
        try {
            userRepository.save(user);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

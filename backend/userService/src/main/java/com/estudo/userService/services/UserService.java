package com.estudo.userService.services;

import com.estudo.userService.dtos.PageResponse;
import com.estudo.userService.dtos.UserResponse;
import com.estudo.userService.dtos.UserUpdateRequest;
import com.estudo.userService.entities.User;
import com.estudo.userService.repository.UserRepository;
import com.estudo.userService.specifications.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getMe(User user){
        return mapToUserResponse(user);
    }

    public List<UserResponse> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    public PageResponse<UserResponse> getAllUsersPaginated(Pageable pageable){
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> users = userPage.getContent()
                .stream()
                .map(this::mapToUserResponse)
                .toList();

        return new PageResponse<>(
                users,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize(),
                userPage.hasNext(),
                userPage.hasPrevious(),
                userPage.isFirst(),
                userPage.isLast()
        );
    }

    public PageResponse<UserResponse> searchUsers(String name, String email, String cpf, Pageable pageable){
        Specification<User> spec = UserSpecification.withFilters(name, email, cpf);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> users = userPage.getContent()
                .stream()
                .map(this::mapToUserResponse)
                .toList();

        return new PageResponse<>(
                users,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.getSize(),
                userPage.hasNext(),
                userPage.hasPrevious(),
                userPage.isFirst(),
                userPage.isLast()
        );
    }

    public UserResponse updateMe(User user, UserUpdateRequest request) {
        user.setName(request.name());
        user.setPhone(request.phone());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return mapToUserResponse(user);
    }

    public void toggleUserStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        user.setActive(!user.isActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}

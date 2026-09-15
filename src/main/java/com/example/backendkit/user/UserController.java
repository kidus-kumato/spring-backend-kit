package com.example.backendkit.user;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }

    @GetMapping("/users/me")
    public UserDtos.MeResponse me(@AuthenticationPrincipal AppUser user) { return UserDtos.MeResponse.from(user); }

    @PutMapping("/users/me")
    public UserDtos.MeResponse update(@AuthenticationPrincipal AppUser user,
                                      @Valid @RequestBody UserDtos.UpdateProfileRequest request) {
        user.setFirstName(request.firstName()); user.setLastName(request.lastName());
        return UserDtos.MeResponse.from(users.save(user));
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AppUser user) {
        user.setStatus(UserStatus.INACTIVE); users.save(user); return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDtos.PageResponse<UserDtos.AdminUserResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt,desc") String sort) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid pagination parameters");
        String[] parts = sort.split(",", 2); List<String> allowed = List.of("createdAt", "email", "status");
        String property = allowed.contains(parts[0]) ? parts[0] : "createdAt";
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<AppUser> result = users.findAll(PageRequest.of(page, size, Sort.by(direction, property)));
        return new UserDtos.PageResponse<>(result.getContent().stream().map(UserDtos.AdminUserResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}

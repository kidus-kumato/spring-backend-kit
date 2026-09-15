package com.example.backendkit.user;

import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class UserDtos {
    private UserDtos() {}
    public record MeResponse(UUID id, String email, String firstName, String lastName, Set<RoleName> roles,
                             UserStatus status, Instant createdAt, Instant updatedAt) {
        public static MeResponse from(AppUser user) { return new MeResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                Set.copyOf(user.getRoles()), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt()); }
    }
    public record UpdateProfileRequest(@Size(max = 100) String firstName, @Size(max = 100) String lastName) {}
    public record AdminUserResponse(UUID id, String email, Set<RoleName> roles, UserStatus status, Instant createdAt) {
        public static AdminUserResponse from(AppUser user) { return new AdminUserResponse(user.getId(), user.getEmail(), Set.copyOf(user.getRoles()), user.getStatus(), user.getCreatedAt()); }
    }
    public record PageResponse<T>(java.util.List<T> content, int page, int size, long totalElements, int totalPages) {}
}

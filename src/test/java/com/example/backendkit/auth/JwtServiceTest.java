package com.example.backendkit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.example.backendkit.user.AppUser;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private final JwtService service=new JwtService("01234567890123456789012345678901",900);
    @Test void createsAndParsesAccessToken() {
        AppUser user=new AppUser("test@example.com","hash");
        try {
            Field id=AppUser.class.getDeclaredField("id"); id.setAccessible(true); id.set(user, UUID.randomUUID());
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
        String token=service.createAccessToken(user);
        assertThat(service.isValid(token)).isTrue();
    }
    @Test void rejectsMalformedToken() { assertThat(service.isValid("not-a-token")).isFalse(); }
    @Test void rejectsShortSecret() { assertThatThrownBy(() -> new JwtService("short",900)).isInstanceOf(IllegalArgumentException.class); }
}

package com.example.backendkit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class TokenHasherTest {
    @Test void hashesDeterministicallyWithoutReturningPlaintext() {
        TokenHasher hasher=new TokenHasher();
        assertThat(hasher.hash("secret")).isEqualTo(hasher.hash("secret"));
        assertThat(hasher.hash("secret")).hasSize(64).doesNotContain("secret");
    }
}

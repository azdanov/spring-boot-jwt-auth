package org.js.azdanov.springbootjwtauth.repository;

import org.js.azdanov.springbootjwtauth.models.RefreshToken;
import org.js.azdanov.springbootjwtauth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    int deleteByUser(User user);
}

package org.js.azdanov.springbootjwtauth.repository;

import org.js.azdanov.springbootjwtauth.models.Role;
import org.js.azdanov.springbootjwtauth.models.enums.RoleEnum;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    @EntityGraph(attributePaths = "users")
    Optional<Role> findByName(RoleEnum name);
}

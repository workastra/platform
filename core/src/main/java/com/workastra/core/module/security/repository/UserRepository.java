package com.workastra.core.module.security.repository;

import com.workastra.core.module.security.model.User;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    @Nullable
    User findByUsername(String username);

    @Nullable
    User findByUsernameAndDeletedAtIsNull(String username);
}

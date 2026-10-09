package org.example.demo4.store;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByOwnerId(Long ownerId);

    void deleteByOwnerId(Long ownerId);
}

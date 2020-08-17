package ru.megadevelopers.equality.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.megadevelopers.equality.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}

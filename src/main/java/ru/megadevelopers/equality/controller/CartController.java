package ru.megadevelopers.equality.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.megadevelopers.equality.model.Cart;
import ru.megadevelopers.equality.model.Items;
import ru.megadevelopers.equality.repository.CartRepository;

@RestController

public class CartController {

    private final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired CartRepository cartRepository;

    @GetMapping("/add")
    public Cart add() {
        Cart cart = new Cart("cart");
        Items item = new Items("item", cart);
        cart.getItems().add(item);


        logger.info("Item is inside the cart before save: {}", cart.getItems().contains(item));
        cart = cartRepository.save(cart);
        logger.info("Item is inside the cart after save: {}", cart.getItems().contains(item));

        return cart;
    }

    @GetMapping("/cart/{cardId}")
    public Cart get(Long cartId) {
        return cartRepository.getOne(cartId);
    }
}

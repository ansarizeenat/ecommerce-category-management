package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.dto.AddToCartRequest;
import com.ecommerce.ecommerce.dto.CartDTO;
import com.ecommerce.ecommerce.dto.UpdateCartQuantityRequest;
import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartDTO> getAllCarts(@RequestParam(required = false) Long customerId) {
        return cartService.getAllCarts(customerId);
    }

    @GetMapping("/stats")
    public Map<String, Object> getCartStats() {
        return cartService.getCartStats();
    }

    @GetMapping("/{id}")
    public CartDTO getCartById(@PathVariable Long id) {
        return cartService.toDTO(cartService.getCartById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerCart(@PathVariable Long customerId) {
        try {
            List<CartDTO> items = cartService.getCustomerCart(customerId);
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}/summary")
    public ResponseEntity<?> getCustomerCartSummary(@PathVariable Long customerId) {
        try {
            return ResponseEntity.ok(cartService.getCustomerCartSummary(customerId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        try {
            CartDTO dto = cartService.toDTO(cartService.addToCart(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not add to cart: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<?> updateQuantity(@PathVariable Long id,
                                            @RequestBody UpdateCartQuantityRequest request) {
        try {
            Integer qty = (request != null) ? request.getQuantity() : null;
            if (qty == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Quantity is required."));
            }
            Cart updated = cartService.updateQuantity(id, qty);
            return ResponseEntity.ok(cartService.toDTO(updated));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not update cart: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        try {
            cartService.removeFromCart(id);
            return ResponseEntity.ok(Map.of("message", "Cart item #" + id + " removed successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<?> clearCustomerCart(@PathVariable Long customerId) {
        try {
            cartService.clearCustomerCart(customerId);
            return ResponseEntity.ok(Map.of("message", "Cart cleared for customer #" + customerId + "."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

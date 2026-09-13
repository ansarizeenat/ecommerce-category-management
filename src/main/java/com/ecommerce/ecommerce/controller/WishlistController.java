package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.model.Wishlist;
import com.ecommerce.ecommerce.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@CrossOrigin(origins = "*")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping
    public ResponseEntity<?> addToWishlist(@RequestParam Long customerId,
                                           @RequestParam Long productId) {
        try {
            Wishlist wishlist = wishlistService.addToWishlist(customerId, productId);
            return ResponseEntity.status(HttpStatus.CREATED).body(wishlist);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerWishlist(@PathVariable Long customerId) {
        try {
            List<Wishlist> wishlist = wishlistService.getCustomerWishlist(customerId);
            return ResponseEntity.ok(wishlist);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{wishlistId}")
    public ResponseEntity<?> getWishlistItem(@PathVariable Long wishlistId) {
        try {
            return ResponseEntity.ok(wishlistService.getWishlistItem(wishlistId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long wishlistId) {
        try {
            wishlistService.removeFromWishlist(wishlistId);
            return ResponseEntity.ok("Product removed from wishlist");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/customer/{customerId}/product/{productId}")
    public ResponseEntity<?> removeByCustomerAndProduct(@PathVariable Long customerId,
                                                        @PathVariable Long productId) {
        try {
            wishlistService.removeByCustomerAndProduct(customerId, productId);
            return ResponseEntity.ok("Product removed from wishlist");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/product/{productId}/exists")
    public ResponseEntity<Boolean> isInWishlist(@PathVariable Long customerId,
                                                @PathVariable Long productId) {
        return ResponseEntity.ok(
                wishlistService.isInWishlist(customerId, productId)
        );
    }

    @DeleteMapping("/customer/{customerId}")
    public ResponseEntity<?> clearWishlist(@PathVariable Long customerId) {
        try {
            wishlistService.clearWishlist(customerId);
            return ResponseEntity.ok("Wishlist cleared successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{wishlistId}/move-to-cart")
    public ResponseEntity<?> moveToCart(@PathVariable Long wishlistId) {
        try {
            Cart cartItem = wishlistService.moveToCart(wishlistId);
            return ResponseEntity.ok(cartItem);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
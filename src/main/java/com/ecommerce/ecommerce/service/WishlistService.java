package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.AddToCartRequest;
import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.User;
import com.ecommerce.ecommerce.model.Wishlist;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public WishlistService(WishlistRepository wishlistRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           CartService cartService) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    // Add product to wishlist
    public Wishlist addToWishlist(Long customerId, Long productId) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.isStatus()) {
            throw new IllegalStateException("Product is inactive");
        }

        if (wishlistRepository.existsByCustomerUserIdAndProductId(customerId, productId)) {
            throw new IllegalStateException("Product is already in wishlist");
        }

        Wishlist wishlist = new Wishlist(customer, product);

        return wishlistRepository.save(wishlist);
    }

    // View customer's wishlist
    @Transactional(readOnly = true)
    public List<Wishlist> getCustomerWishlist(Long customerId) {

        if (!userRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found");
        }

        return wishlistRepository
                .findByCustomerUserIdOrderByCreatedAtDesc(customerId);
    }

    // Get one wishlist item
    @Transactional(readOnly = true)
    public Wishlist getWishlistItem(Long wishlistId) {

        return wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new IllegalArgumentException("Wishlist item not found"));
    }

    // Remove product from wishlist
    public void removeFromWishlist(Long wishlistId) {

        if (!wishlistRepository.existsById(wishlistId)) {
            throw new IllegalArgumentException("Wishlist item not found");
        }

        wishlistRepository.deleteById(wishlistId);
    }

    // Remove product using customer and product ID
    public void removeByCustomerAndProduct(Long customerId, Long productId) {

        Wishlist wishlist = wishlistRepository
                .findByCustomerUserIdAndProductId(customerId, productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found in wishlist"));

        wishlistRepository.delete(wishlist);
    }

    // Check whether product is in wishlist
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long customerId, Long productId) {

        return wishlistRepository
                .existsByCustomerUserIdAndProductId(customerId, productId);
    }

    // Clear customer's complete wishlist
    public void clearWishlist(Long customerId) {

        if (!userRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found");
        }

        wishlistRepository.deleteByCustomerUserId(customerId);
    }

    // Move product from wishlist to cart
    public Cart moveToCart(Long wishlistId) {

        Wishlist wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Wishlist item not found"));

        Long customerId = wishlist.getCustomer().getUserId();
        Long productId = wishlist.getProduct().getId();

        AddToCartRequest request = new AddToCartRequest();
        request.setCustomerId(customerId);
        request.setProductId(productId);
        request.setQuantity(1);

        Cart cartItem = cartService.addToCart(request);

        wishlistRepository.delete(wishlist);

        return cartItem;
    }
}
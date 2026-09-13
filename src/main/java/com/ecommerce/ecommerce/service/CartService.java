package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.AddToCartRequest;
import com.ecommerce.ecommerce.dto.CartDTO;
import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.User;
import com.ecommerce.ecommerce.repository.CartRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ============ ADMIN: All carts (for cart-abandonment dashboard) ============
    public List<CartDTO> getAllCarts(Long customerIdFilter) {
        List<Cart> carts;
        if (customerIdFilter != null) {
            carts = cartRepository.findByCustomerIdOrderByCreatedAtDesc(customerIdFilter);
        } else {
            carts = cartRepository.findAllByOrderByCreatedAtDesc();
        }
        List<CartDTO> dtos = new ArrayList<>();
        for (Cart c : carts) dtos.add(toDTO(c));
        return dtos;
    }

    // ============ CUSTOMER: Get my cart ============
    public List<CartDTO> getCustomerCart(Long customerId) {
        validateCustomerExists(customerId);
        List<Cart> carts = cartRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<CartDTO> dtos = new ArrayList<>();
        for (Cart c : carts) dtos.add(toDTO(c));
        return dtos;
    }

    // ============ Single cart item ============
    public Cart getCartById(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with ID: " + cartId));
    }

    // ============ 1. Add to Cart ============
    @Transactional
    public Cart addToCart(AddToCartRequest request) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required.");
        }
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required.");
        }
        int qty = (request.getQuantity() != null && request.getQuantity() > 0) ? request.getQuantity() : 1;

        User customer = validateCustomerExists(request.getCustomerId());
        Product product = validateProductExists(request.getProductId());

        if (!product.isStatus()) {
            throw new IllegalStateException("Product '" + product.getName() + "' is inactive and cannot be added to cart.");
        }

        Optional<Cart> existing = cartRepository.findByCustomerIdAndProductId(request.getCustomerId(), request.getProductId());
        if (existing.isPresent()) {
            Cart cart = existing.get();
            int newQty = cart.getQuantity() + qty;
            validateInventory(product, newQty);
            cart.setQuantity(newQty);
            cart.setTotalPrice(calcLineTotal(product, newQty));
            cart.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(cart);
        } else {
            validateInventory(product, qty);
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cart.setProduct(product);
            cart.setQuantity(qty);
            cart.setTotalPrice(calcLineTotal(product, qty));
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(cart);
        }
    }

    // ============ 2. Update Cart Quantity ============
    @Transactional
    public Cart updateQuantity(Long cartId, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1. To remove an item, use Remove.");
        }
        Cart cart = getCartById(cartId);
        Product product = cart.getProduct();
        validateInventory(product, newQuantity);
        cart.setQuantity(newQuantity);
        cart.setTotalPrice(calcLineTotal(product, newQuantity));
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    // ============ 3. Remove from Cart ============
    @Transactional
    public void removeFromCart(Long cartId) {
        Cart cart = getCartById(cartId);
        cartRepository.delete(cart);
    }

    // ============ Clear Customer Cart ============
    @Transactional
    public void clearCustomerCart(Long customerId) {
        validateCustomerExists(customerId);
        cartRepository.deleteByCustomerId(customerId);
    }

    // ============ Per-customer cart summary (grand total, unique items, total qty) ============
    public Map<String, Object> getCustomerCartSummary(Long customerId) {
        List<Cart> items = cartRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        BigDecimal grandTotal = BigDecimal.ZERO;
        int uniqueItems = items.size();
        int totalQty = 0;
        for (Cart c : items) {
            grandTotal = grandTotal.add(c.getTotalPrice() == null ? BigDecimal.ZERO : c.getTotalPrice());
            totalQty += (c.getQuantity() == null ? 0 : c.getQuantity());
        }
        Map<String, Object> s = new HashMap<>();
        s.put("customerId", customerId);
        s.put("uniqueItems", uniqueItems);
        s.put("totalQuantity", totalQty);
        s.put("grandTotal", grandTotal);
        return s;
    }

    // ============ Admin: Aggregate stats (abandonment analysis, totals, unique customers) ============
    public Map<String, Object> getCartStats() {
        List<Cart> all = cartRepository.findAll();
        BigDecimal totalValue = BigDecimal.ZERO;
        long totalQty = 0;
        Set<Long> uniqueCustIds = new HashSet<>();
        Set<Long> uniqueProdIds = new HashSet<>();

        for (Cart c : all) {
            if (c.getTotalPrice() != null) totalValue = totalValue.add(c.getTotalPrice());
            if (c.getQuantity() != null) totalQty += c.getQuantity();
            if (c.getCustomerId() != null) uniqueCustIds.add(c.getCustomerId());
            if (c.getProductId() != null) uniqueProdIds.add(c.getProductId());
        }

        long uniqueCustomers = uniqueCustIds.size();
        long uniqueProducts = uniqueProdIds.size();
        long cartRows = all.size();

        Map<String, Object> s = new HashMap<>();
        s.put("totalCartValue", totalValue);
        s.put("totalItemsQuantity", totalQty);
        s.put("uniqueCustomersWithCarts", uniqueCustomers);
        s.put("uniqueProductsInCarts", uniqueProducts);
        s.put("totalCartRows", cartRows);
        double avgItemsPerCustomer = uniqueCustomers == 0 ? 0.0 : (double) cartRows / (double) uniqueCustomers;
        s.put("avgItemsPerCustomer", Math.round(avgItemsPerCustomer * 100.0) / 100.0);
        return s;
    }

    // ============ Helpers ============
    private User validateCustomerExists(Long customerId) {
        return userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
    }

    private Product validateProductExists(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    }

    private void validateInventory(Product product, int requestedQty) {
        int available = (product.getInventoryCount() > 0) ? product.getInventoryCount() : 0;
        if (requestedQty > available) {
            throw new IllegalStateException(
                    "Insufficient stock for '" + product.getName() + "'. " +
                    "Requested: " + requestedQty + ", Available in inventory: " + available + "."
            );
        }
    }

    private BigDecimal calcLineTotal(Product product, int qty) {
        double price = product.getPrice() > 0 ? product.getPrice() : 0.0;
        return BigDecimal.valueOf(price)
                .multiply(BigDecimal.valueOf(qty))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public CartDTO toDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getCartId());
        dto.setCustomerId(cart.getCustomerId());
        dto.setCustomerName(cart.getCustomer() != null ? cart.getCustomer().getName() : "N/A");
        dto.setCustomerEmail(cart.getCustomer() != null ? cart.getCustomer().getEmail() : null);
        dto.setProductId(cart.getProductId());
        dto.setProductName(cart.getProduct() != null ? cart.getProduct().getName() : "N/A");
        dto.setProductSku(cart.getProduct() != null ? cart.getProduct().getSku() : null);
        dto.setUnitPrice(cart.getProduct() != null
                ? BigDecimal.valueOf(cart.getProduct().getPrice()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        dto.setQuantity(cart.getQuantity());
        dto.setTotalPrice(cart.getTotalPrice());
        dto.setAvailableInventory(cart.getProduct() != null ? cart.getProduct().getInventoryCount() : null);
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        return dto;
    }
}

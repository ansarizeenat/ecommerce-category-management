package com.ecommerce.ecommerce;

import com.ecommerce.ecommerce.dto.AddToCartRequest;
import com.ecommerce.ecommerce.dto.CartDTO;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.User;
import com.ecommerce.ecommerce.repository.CartRepository;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired private CartService cartService;
    @Autowired private CartRepository cartRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;

    private User testCustomer;
    private Product testProduct;      // $50.00, inventory 10
    private Product testProductExpensive; // $125.00, inventory 5

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFirstName("Rahul");
        user.setLastName("Sharma");
        user.setEmail("rahul" + System.currentTimeMillis() + "@example.com");
        user.setPhone("+91 9000012345");
        user.setStatus(true);
        testCustomer = userRepository.save(user);

        Category cat = new Category();
        cat.setCategoryName("Gadgets");
        cat.setDescription("Electronic gadgets");
        cat = categoryRepository.save(cat);

        Product p1 = new Product();
        p1.setName("Smartphone Cover");
        p1.setPrice(50.00);
        p1.setSku("COV-001");
        p1.setInventoryCount(10);
        p1.setCategory(cat);
        p1.setStatus(true);
        testProduct = productRepository.save(p1);

        Product p2 = new Product();
        p2.setName("Wireless Headphones");
        p2.setPrice(125.00);
        p2.setSku("HP-005");
        p2.setInventoryCount(5);
        p2.setCategory(cat);
        p2.setStatus(true);
        testProductExpensive = productRepository.save(p2);
    }

    // --- 1. Add to cart success ---
    @Test
    void testAddToCart_Success() {
        AddToCartRequest req = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 2);
        Cart cart = cartService.addToCart(req);

        assertNotNull(cart.getCartId());
        assertEquals(testCustomer.getUserId(), cart.getCustomerId());
        assertEquals(testProduct.getId(), cart.getProductId());
        assertEquals(2, cart.getQuantity());
        // 50 * 2 = 100
        assertEquals(0, cart.getTotalPrice().compareTo(new BigDecimal("100.00")));
        assertNotNull(cart.getCreatedAt());
    }

    // --- 2. Add same product again increments qty (merge) ---
    @Test
    void testAddToCart_SameProductMergesQuantity() {
        AddToCartRequest r1 = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 2);
        cartService.addToCart(r1);
        AddToCartRequest r2 = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 3);
        Cart merged = cartService.addToCart(r2);

        // Only one cart row should exist for this customer+product
        List<Cart> customerCarts = cartRepository.findByCustomerIdOrderByCreatedAtDesc(testCustomer.getUserId());
        long sameProductCount = customerCarts.stream().filter(c -> c.getProductId().equals(testProduct.getId())).count();
        assertEquals(1, sameProductCount);

        assertEquals(5, merged.getQuantity());
        // 50 * 5 = 250
        assertEquals(0, merged.getTotalPrice().compareTo(new BigDecimal("250.00")));
    }

    // --- 3. Add fails when out of stock ---
    @Test
    void testAddToCart_InsufficientInventoryThrowsException() {
        // inventory 10; try adding 15
        AddToCartRequest req = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 15);
        assertThrows(IllegalStateException.class, () -> cartService.addToCart(req));
    }

    // --- 4. Merge add fails when combined qty exceeds inventory ---
    @Test
    void testAddToCart_MergedExceedsInventoryThrows() {
        AddToCartRequest r1 = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 8);
        cartService.addToCart(r1);
        AddToCartRequest r2 = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 5);
        assertThrows(IllegalStateException.class, () -> cartService.addToCart(r2));
    }

    // --- 5. Update quantity success + recalc ---
    @Test
    void testUpdateQuantity_SuccessAndPriceRecalculates() {
        AddToCartRequest r1 = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 1);
        Cart cart = cartService.addToCart(r1);

        Cart updated = cartService.updateQuantity(cart.getCartId(), 4);
        assertEquals(4, updated.getQuantity());
        assertEquals(0, updated.getTotalPrice().compareTo(new BigDecimal("200.00")));
    }

    // --- 6. Update quantity exceeds stock throws ---
    @Test
    void testUpdateQuantity_ExceedsInventoryThrows() {
        AddToCartRequest r1 = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 1);
        Cart cart = cartService.addToCart(r1);
        assertThrows(IllegalStateException.class, () -> cartService.updateQuantity(cart.getCartId(), 500));
    }

    // --- 7. Remove from cart ---
    @Test
    void testRemoveFromCart_Success() {
        AddToCartRequest r1 = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 2);
        Cart c1 = cartService.addToCart(r1);
        AddToCartRequest r2 = new AddToCartRequest(testCustomer.getUserId(), testProductExpensive.getId(), 1);
        Cart c2 = cartService.addToCart(r2);

        cartService.removeFromCart(c1.getCartId());

        assertFalse(cartRepository.findById(c1.getCartId()).isPresent());
        assertTrue(cartRepository.findById(c2.getCartId()).isPresent());

        List<CartDTO> remaining = cartService.getCustomerCart(testCustomer.getUserId());
        assertEquals(1, remaining.size());
    }

    // --- 8. Admin stats, customer summary, and DTO mapping ---
    @Test
    void testCartStatsAndCustomerSummary() {
        // Customer 1: 2 products -> total 2*50 + 1*125 = $225
        cartService.addToCart(new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 2));
        cartService.addToCart(new AddToCartRequest(testCustomer.getUserId(), testProductExpensive.getId(), 1));

        Map<String, Object> custSum = cartService.getCustomerCartSummary(testCustomer.getUserId());
        assertEquals(2, custSum.get("uniqueItems"));
        assertEquals(3, custSum.get("totalQuantity"));
        assertEquals(0, ((BigDecimal) custSum.get("grandTotal")).compareTo(new BigDecimal("225.00")));

        Map<String, Object> stats = cartService.getCartStats();
        assertTrue(((BigDecimal) stats.get("totalCartValue")).compareTo(BigDecimal.ZERO) > 0);
        assertTrue((long) stats.get("uniqueCustomersWithCarts") >= 1);
        assertTrue((long) stats.get("uniqueProductsInCarts") >= 2);
        assertTrue((long) stats.get("totalItemsQuantity") >= 3);
    }

    // --- 9. Clear customer cart ---
    @Test
    void testClearCustomerCart() {
        cartService.addToCart(new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 1));
        cartService.addToCart(new AddToCartRequest(testCustomer.getUserId(), testProductExpensive.getId(), 1));

        assertEquals(2, cartService.getCustomerCart(testCustomer.getUserId()).size());
        cartService.clearCustomerCart(testCustomer.getUserId());
        assertEquals(0, cartService.getCustomerCart(testCustomer.getUserId()).size());
    }

    // --- 10. Inactive product cannot be added ---
    @Test
    void testAddToCart_InactiveProductThrows() {
        testProduct.setStatus(false);
        productRepository.save(testProduct);
        AddToCartRequest req = new AddToCartRequest(testCustomer.getUserId(), testProduct.getId(), 1);
        assertThrows(IllegalStateException.class, () -> cartService.addToCart(req));
    }
}

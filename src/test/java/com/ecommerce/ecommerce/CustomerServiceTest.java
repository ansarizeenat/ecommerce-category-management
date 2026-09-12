package com.ecommerce.ecommerce;

import com.ecommerce.ecommerce.dto.CustomerDTO;
import com.ecommerce.ecommerce.dto.CustomerRequest;
import com.ecommerce.ecommerce.dto.OrderItemRequest;
import com.ecommerce.ecommerce.dto.OrderRequest;
import com.ecommerce.ecommerce.entity.Category;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.model.User;
import com.ecommerce.ecommerce.repository.CategoryRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import com.ecommerce.ecommerce.service.CustomerService;
import com.ecommerce.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setCategoryName("Accessories");
        category.setDescription("Tech accessories");
        category = categoryRepository.save(category);

        testProduct = new Product();
        testProduct.setName("USB-C Hub");
        testProduct.setPrice(40.00);
        testProduct.setSku("HUB-01");
        testProduct.setInventoryCount(50);
        testProduct.setCategory(category);
        testProduct.setStatus(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void testCreateCustomer_Success() {
        CustomerRequest req = new CustomerRequest("Rahul", "Sharma", "rahul" + System.currentTimeMillis() + "@example.com", "9876543210");
        User customer = customerService.createCustomer(req);

        assertNotNull(customer.getUserId());
        assertEquals("Rahul", customer.getFirstName());
        assertEquals("Sharma", customer.getLastName());
        assertEquals("Rahul Sharma", customer.getName());
        assertTrue(customer.getStatus());
        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void testCreateCustomer_DuplicateEmailThrowsException() {
        String email = "duplicate" + System.currentTimeMillis() + "@example.com";
        CustomerRequest req1 = new CustomerRequest("Aman", "Gupta", email, "9876500000");
        customerService.createCustomer(req1);

        CustomerRequest req2 = new CustomerRequest("Aman", "Kumar", email, "9876511111");
        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(req2));
    }

    @Test
    void testUpdateCustomer_Success() {
        CustomerRequest req = new CustomerRequest("Sara", "Khan", "sara" + System.currentTimeMillis() + "@example.com", "9876522222");
        User customer = customerService.createCustomer(req);

        CustomerRequest updateReq = new CustomerRequest("Sarah", "Ali", "sarah" + System.currentTimeMillis() + "@example.com", "9876599999");
        User updated = customerService.updateCustomer(customer.getUserId(), updateReq);

        assertEquals("Sarah", updated.getFirstName());
        assertEquals("Ali", updated.getLastName());
        assertEquals("9876599999", updated.getPhone());
    }

    @Test
    void testDeactivateCustomer_SoftDelete() {
        CustomerRequest req = new CustomerRequest("Vijay", "Verma", "vijay" + System.currentTimeMillis() + "@example.com", "9876533333");
        User customer = customerService.createCustomer(req);
        assertTrue(customer.getStatus());

        // Soft delete: status becomes false
        User deactivated = customerService.deactivateCustomer(customer.getUserId());
        assertFalse(deactivated.getStatus());

        // Customer still exists in database
        User found = customerService.getCustomerById(customer.getUserId());
        assertNotNull(found);
        assertFalse(found.getStatus());
    }

    @Test
    void testActivateCustomer_Reactivation() {
        CustomerRequest req = new CustomerRequest("Pooja", "Patel", "pooja" + System.currentTimeMillis() + "@example.com", "9876544444");
        User customer = customerService.createCustomer(req);

        customerService.deactivateCustomer(customer.getUserId());
        assertFalse(customerService.getCustomerById(customer.getUserId()).getStatus());

        customerService.activateCustomer(customer.getUserId());
        assertTrue(customerService.getCustomerById(customer.getUserId()).getStatus());
    }

    @Test
    void testGetCustomerWithOrders_OrderHistory() {
        CustomerRequest req = new CustomerRequest("Sneha", "Reddy", "sneha" + System.currentTimeMillis() + "@example.com", "9876555555");
        User customer = customerService.createCustomer(req);

        // Place an order for this customer
        OrderRequest orderReq = new OrderRequest();
        orderReq.setUserId(customer.getUserId());
        orderReq.setCustomerName(customer.getName());
        orderReq.setCustomerEmail(customer.getEmail());
        orderReq.setShippingAddress("Hyderabad, Telangana");
        orderReq.setItems(List.of(new OrderItemRequest(testProduct.getId(), 2)));

        Order order = orderService.placeOrder(orderReq);
        assertNotNull(order);

        CustomerDTO customerDTO = customerService.getCustomerWithOrders(customer.getUserId());
        assertEquals(1, customerDTO.getOrderCount());
        assertNotNull(customerDTO.getOrders());
        assertEquals(1, customerDTO.getOrders().size());
        assertEquals(order.getId(), customerDTO.getOrders().get(0).getId());
    }
}

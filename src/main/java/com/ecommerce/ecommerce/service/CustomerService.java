package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.dto.CustomerDTO;
import com.ecommerce.ecommerce.dto.CustomerRequest;
import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.User;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CustomerService(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public List<CustomerDTO> getAllCustomers(Boolean statusFilter) {
        List<User> users = (statusFilter != null)
                ? userRepository.findByStatusOrderByCreatedAtDesc(statusFilter)
                : userRepository.findAllByOrderByCreatedAtDesc();

        List<CustomerDTO> dtos = new ArrayList<>();
        for (User u : users) {
            dtos.add(toDTO(u, false));
        }
        return dtos;
    }

    public User getCustomerById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    }

    public CustomerDTO getCustomerWithOrders(Long id) {
        User user = getCustomerById(id);
        return toDTO(user, true);
    }

    @Transactional
    public User createCustomer(CustomerRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A customer with email '" + email + "' already exists.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
        user.setEmail(email);
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setStatus(request.getStatus() != null ? request.getStatus() : true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public User updateCustomer(Long id, CustomerRequest request) {
        User existing = getCustomerById(id);

        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            existing.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            existing.setLastName(request.getLastName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmailAndUserIdNot(newEmail, id)) {
                throw new IllegalArgumentException("Another customer already uses email '" + newEmail + "'.");
            }
            existing.setEmail(newEmail);
        }
        if (request.getPhone() != null) {
            existing.setPhone(request.getPhone().trim());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existing);
    }

    @Transactional
    public User deactivateCustomer(Long id) {
        User existing = getCustomerById(id);
        // Soft delete: status = false
        existing.setStatus(false);
        existing.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existing);
    }

    @Transactional
    public User activateCustomer(Long id) {
        User existing = getCustomerById(id);
        existing.setStatus(true);
        existing.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existing);
    }

    private CustomerDTO toDTO(User user, boolean includeOrders) {
        CustomerDTO dto = new CustomerDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        List<Order> orders = orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
        dto.setOrderCount(orders.size());

        BigDecimal total = BigDecimal.ZERO;
        for (Order o : orders) {
            if (Boolean.TRUE.equals(o.getStatus())) {
                total = total.add(o.getTotalAmount());
            }
        }
        dto.setTotalSpent(total);

        if (includeOrders) {
            dto.setOrders(orders);
        }
        return dto;
    }
}

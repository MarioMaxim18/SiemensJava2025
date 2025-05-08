package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doNothing;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    public void testGetAllItems_returnsList() throws Exception {
        List<Item> items = List.of(
                new Item(1L, "Test Item", "Test Description", "NEW", "test@mail.com"),
                new Item(2L, "Test Item 2", "Test Description 2", "NEW", "test2@mail.com")
        );
        when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk()); // This test checks if the GET endpoint returns a successful response when retrieving all items
    }

    @Test
    public void testGetItemById_validId_returnsItem() throws Exception {
        Item item = new Item(1L, "Test Item", "Test Description", "NEW", "test@mail.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk()); // This test checks if the GET endpoint returns a successful response when retrieving an existing item
    }

    @Test
    public void testGetItemById_invalidId_returns404() throws Exception {
        when(itemService.findById(3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/3"))
                .andExpect(status().isNotFound()); // This test checks if the GET endpoint returns a 404 status when the requested item doesn't exist
    }

    @Test
    public void testCreateItem_valid_returns201() throws Exception {
        Item item = new Item(null, "Test Item", "Test Description", "NEW", "test@mail.com");
        when(itemService.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(item)))
                .andExpect(status().isCreated()); // This test checks if the POST endpoint successfully creates a new item and returns a 201 status
    }

    @Test
    public void testCreateItem_invalidEmail_returns400() throws Exception {
        Item item = new Item(null, "Test Item", "Test Description", "NEW", "invalid");

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(item)))
                .andExpect(status().isBadRequest()); // This test checks if the POST endpoint returns a 400 status when creating an item with invalid email
    }

    @Test
    public void testUpdateItem_valid_updatesItem() throws Exception {
        Item existingItem =new Item(1L, "Test Item", "Test Description", "NEW", "invalid");
        Item updatedItem = new Item(1L, "Updated Name", "Updated Description", "PROCESSED", "new@mail.com");

        when(itemService.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemService.save(any(Item.class))).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("PROCESSED"))
                .andExpect(jsonPath("$.email").value("new@mail.com")); // This test verifies that all item fields are properly updated
    }

    @Test
    public void testUpdateItem_invalidEmail_returns400() throws Exception {
        Item existingItem = new Item(1L, "Test Item", "Test Description", "NEW", "test@mail.com");
        Item invalidItem = new Item(1L, "Test Item", "Test Description", "NEW", "invalid");
        when(itemService.findById(1L)).thenReturn(Optional.of(existingItem));

        mockMvc.perform(put("/api/items/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());// This test checks if the PUT endpoint returns 400 when updating with invalid email
    }

    @Test
    public void testDeleteItem_existingItem_returnsNoContent() throws Exception {
        Item item = new Item(1L, "Test Item", "Test Description", "NEW", "test@mail.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemService).deleteById(1L);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent()); // This test checks if the DELETE endpoint returns 204 status when item exists
    }

    @Test
    public void testDeleteItem_nonExistingItem_returns404() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNotFound()); // This test checks if the DELETE endpoint returns 404 when item doesn't exist
    }

    @Test
    public void testProcessItems_returnsProcessedItems() throws Exception {
        List<Item> processed = List.of(
                new Item(1L, "Test Item", "Test Description", "NEW", "test@mail.com")
        );
        when(itemService.processItemsAsync()).thenReturn(CompletableFuture.completedFuture(processed));

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk()); // This test checks if the process endpoint successfully processes items and returns a successful response
    }
}
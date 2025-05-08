package com.siemens.internship.controller;

import com.siemens.internship.service.ItemService;
import com.siemens.internship.model.Item;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // return a bad request if validation fails
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED); // return a created response
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // return NOT_FOUND if the item does not exist
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id); // set the ID of the item to be updated
            Item newItem = itemService.save(item); // save the updated item
            return new ResponseEntity<>(newItem, HttpStatus.OK); // return the updated item with OK status
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // return NOT_FOUND if the item does not exist

        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> item = itemService.findById(id);
        if (item.isPresent()) { // check if the item exists
            itemService.deleteById(id); // delete the item
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // return NO_CONTENT status
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // return NOT_FOUND if the item does not exist
        }
    }

    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync()
                .thenApply(items -> new ResponseEntity<>(items, HttpStatus.OK)) // return the processed items with OK status
                .exceptionally(ex -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)); // handle exceptions and return INTERNAL_SERVER_ERROR status
    }
}

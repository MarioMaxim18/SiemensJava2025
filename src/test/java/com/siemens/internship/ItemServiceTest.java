package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ItemServiceTest {

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemService itemService;

	private Item testItem;
	private List<Item> testItems;

	@BeforeEach
	public void setUp() {
		testItem = new Item(1L, "Test Item", "Test Description", "NEW", "test@example.com");
		testItems = Arrays.asList(
				testItem,
				new Item(2L, "Test Item 2", "Test Description 2", "NEW", "test2@example.com")
		);
	} // This method is called before each test to set up the test data

	@Test
	public void testFindAll_returnsAllItems() {
		when(itemRepository.findAll()).thenReturn(testItems);

		List<Item> result = itemService.findAll();

		Assertions.assertEquals(2, result.size()); // This test checks if the findAll method returns the correct number of items
		Assertions.assertEquals("Test Item", result.get(0).getName()); // This test checks if the findAll method returns the first item correctly
		Assertions.assertEquals("Test Item 2", result.get(1).getName()); // This test checks if the findAll method returns the second item correctly
	}

	@Test
	public void testFindById_existingId_returnsCorrectItem() {
		when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

		Optional<Item> result = itemService.findById(1L);

		Assertions.assertEquals("Test Item", result.get().getName()); // This test checks if the findById method returns the correct item
	}

	@Test
	public void testFindById_nonExistingId_returnsEmptyOptional() {
		when(itemRepository.findById(3L)).thenReturn(Optional.empty());

		Optional<Item> result = itemService.findById(3L);

		Assertions.assertFalse(result.isPresent()); // This test checks if the findById method returns an empty optional for a non-existing item
	}

	@Test
	public void testDeleteById_executesSuccessfully() {
		doNothing().when(itemRepository).deleteById(1L);

		itemService.deleteById(1L);

		verify(itemRepository).deleteById(1L); // This test checks if the deleteById method deletes the item correctly
	}

	@Test
	public void testProcessItemsAsync_successfullyProcessesItems() throws Exception {
		when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));
		when(itemRepository.findById(1L)).thenReturn(Optional.of(testItems.get(0)));
		when(itemRepository.findById(2L)).thenReturn(Optional.of(testItems.get(1)));
		when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
			Item item = inv.getArgument(0);
			item.setStatus("PROCESSED");
			return item;
		});

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get(5, TimeUnit.SECONDS);

		Assertions.assertEquals(2, result.size()); // This test checks if the processItemsAsync method processes all items correctly
		for (Item item : result) {
			Assertions.assertEquals("PROCESSED", item.getStatus()); // This test checks if the status of each processed item is set to "PROCESSED"
		}
	}

	@Test
	public void testProcessItemsAsync_skipsNonExistentItem() throws Exception {
		when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 3L));
		when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
		when(itemRepository.findById(3L)).thenReturn(Optional.empty());
		when(itemRepository.save(any(Item.class))).thenReturn(testItem);

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get(5, TimeUnit.SECONDS);

		Assertions.assertEquals(1, result.size());  // This test checks if the processItemsAsync method skips non-existent items correctly
	}

	@Test
	public void testProcessItemsAsync_emptyList_returnsEmptyList() throws Exception {
		when(itemRepository.findAllIds()).thenReturn(List.of());

		CompletableFuture<List<Item>> future = itemService.processItemsAsync();
		List<Item> result = future.get(5, TimeUnit.SECONDS);

		Assertions.assertTrue(result.isEmpty()); // This test checks if the processItemsAsync method returns an empty list when there are no items to process
	}
}
package com.example.fortunecookie.controller;

import com.example.fortunecookie.entity.Fortune;
import com.example.fortunecookie.repository.FortuneRepository;
import com.example.fortunecookie.util.MessageReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fortunes")
public class FortuneController {

    @Autowired
    private FortuneRepository fortuneRepository;

    private static final Logger logger = LoggerFactory.getLogger(FortuneController.class);

    // Create
    @PostMapping
    public ResponseEntity<Fortune> createFortune(@RequestBody Fortune fortune) {
        try {
            Fortune savedFortune = fortuneRepository.save(fortune);
            logger.info("Created new Fortune: {}", savedFortune);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFortune);
        } catch (Exception e) {
            logger.error("Error creating Fortune", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Read (All)
    @GetMapping("/all")
    public ResponseEntity<List<Fortune>> getAllFortunes() {
        try {
            List<Fortune> fortunes = fortuneRepository.findAll();
            logger.info("Fetched {} Fortunes", fortunes.size());
            return ResponseEntity.ok(fortunes);
        } catch (Exception e) {
            logger.error("Error fetching all Fortunes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Read (Single)
    @GetMapping("/{id}")
    public ResponseEntity<Fortune> getFortuneById(@PathVariable String id) {
        try {
            Optional<Fortune> fortune = fortuneRepository.findById(id);
            if (fortune.isPresent()) {
                logger.info("Fetched Fortune: {}", fortune.get());
                return ResponseEntity.ok(fortune.get());
            } else {
                logger.info("Fortune not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching Fortune with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Fortune> updateFortune(@PathVariable String id, @RequestBody Fortune fortune) {
        try {
            Optional<Fortune> existingFortune = fortuneRepository.findById(id);
            if (existingFortune.isPresent()) {
                fortune.setId(id);
                Fortune updatedFortune = fortuneRepository.save(fortune);
                logger.info("Updated Fortune: {}", updatedFortune);
                return ResponseEntity.ok(updatedFortune);
            } else {
                logger.info("Fortune not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating Fortune with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFortune(@PathVariable String id) {
        try {
            if (fortuneRepository.existsById(id)) {
                fortuneRepository.deleteById(id);
                logger.info("Deleted Fortune with id: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.info("Fortune not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error deleting Fortune with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete (All)
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllFortunes() {
        try {
            long deletedCount = fortuneRepository.count();
            fortuneRepository.deleteAll();
            logger.info("Deleted all fortunes. Total deleted: {}", deletedCount);
            return ResponseEntity.ok("All fortunes have been deleted. Total deleted: " + deletedCount);
        } catch (Exception e) {
            logger.error("Error occurred while deleting all fortunes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting all fortunes: " + e.getMessage());
        }
    }

    @GetMapping("/random")
    public ResponseEntity<Fortune> getRandomFortune() {

        try {
            List<Fortune> fortunes = fortuneRepository.findAll();
            if (fortunes.isEmpty()) {
                return ResponseEntity.ok(new Fortune("No fortunes available."));
            }
            Random random = new Random();
            Fortune randomFortune = fortunes.get(random.nextInt(fortunes.size()));
            randomFortune.setLuckyNumbers(generateLuckyNumbers());

            logger.info("--> Fetched random Fortune: "+randomFortune.toString());

            return ResponseEntity.ok(randomFortune);
        } catch (Exception e) {
            // Return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Fortune("An error occurred while fetching a random fortune."));
        }
    }

    // GET /search?keyword=luck
    @GetMapping("/search")
    public ResponseEntity<?> searchFortunes(@RequestParam String keyword) {
        try {
            List<Fortune> fortunes = fortuneRepository.findByMessageContainingIgnoreCase(keyword);
            if (fortunes.isEmpty()) {
                return ResponseEntity.ok("No fortunes found containing the keyword: " + keyword);
            }
            return ResponseEntity.ok(fortunes);
        } catch (Exception e) {
            logger.error("Error occurred while searching fortunes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while searching fortunes: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    public long getFortuneCount() {
        return fortuneRepository.count();
    }

    // Store the messages by reading file.
    @PostMapping("/bulkupdate")
    @Transactional
    public ResponseEntity<String> storeFortuneMessages() {
        try {

            MessageReader messageReader = new MessageReader();
            List<String> messages = messageReader.readMessages("../../resources/templates/messages.txt");

            List<Fortune> fortunes = messages.stream()
                    .map(Fortune::new)
                    .collect(Collectors.toList());

            fortuneRepository.saveAll(fortunes);

            String resultMessage = String.format("Bulk update completed. %d fortunes loaded.", fortunes.size());
            logger.info(resultMessage);
            return ResponseEntity.ok(resultMessage);
        } catch (IOException e) {
            logger.error("Error during bulk update of fortunes", e);
            return ResponseEntity.internalServerError().body("Error during bulk update: " + e.getMessage());
        }
    }


    /**
     * Generates a list of 6 unique lucky numbers.
     *
     * This method creates a list of 6 distinct random integers between 1 and 49 (inclusive).
     * These numbers are intended to represent lottery numbers or other forms of "lucky numbers"
     * that often accompany fortune cookie messages.
     *
     * The method uses Java's Random class to generate the numbers and ensures that:
     * 1. All numbers are between 1 and 49
     * 2. There are no duplicate numbers in the list
     * 3. Exactly 6 numbers are generated
     *
     * @return A List<Integer> containing 6 unique random numbers between 1 and 49.
     */
    private List<Integer> generateLuckyNumbers() {
        Random random = new Random();
        List<Integer> luckyNumbers = random.ints(1, 49)
                .distinct()
                .limit(6)
                .boxed()
                .collect(Collectors.toList());

        Collections.sort(luckyNumbers); // Sort the numbers in ascending order
        return luckyNumbers;
    }

}

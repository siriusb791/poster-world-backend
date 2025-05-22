package com.sirius.posterworld.controllers;

import com.sirius.posterworld.models.Poster;
import com.sirius.posterworld.services.PosterService;
import com.sirius.posterworld.services.S3ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/posters")
public class PosterController {

    private static final Logger logger = LoggerFactory.getLogger(PosterController.class);

    private final PosterService posterService;

    private final S3ImageService s3ImageService;

    @Autowired
    public PosterController(PosterService posterService, S3ImageService s3ImageService) {
        this.posterService = posterService;
        this.s3ImageService = s3ImageService;
    }


    @PostMapping(consumes = {"multipart/form-data"}) // Specify the media type for file uploads
    public ResponseEntity<Poster> addPoster(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("category") String category,
            @RequestParam("artist") String artist,
            @RequestParam("dimensions") String dimensions,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("image") MultipartFile image) { // Receive the image file

        try {
            String imageUrl = s3ImageService.uploadImage(image);

            Poster newPoster = new Poster();
            newPoster.setTitle(title);
            newPoster.setDescription(description);
            newPoster.setPrice(price);
            newPoster.setCategory(category);
            newPoster.setArtist(artist);
            newPoster.setDimensions(dimensions);
            newPoster.setStockQuantity(stockQuantity);
            newPoster.setImageUrl(imageUrl); // Set the S3 image URL
            newPoster.setUploadDate(LocalDateTime.now());

            Poster savedPoster = posterService.addPoster(newPoster);
            return new ResponseEntity<>(savedPoster, HttpStatus.CREATED);

        } catch (IOException e) {
            // Handle the image upload error appropriately
            logger.error("Error uploading image to S3: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Poster>> getAllPosters(@RequestParam(required = false) String category) {

        logger.info("getall poster with catogory controller running");
        List<Poster> posters = posterService.getAllPosters(category);
        return new ResponseEntity<>(posters, HttpStatus.OK);
    }

    @GetMapping("/{posterId}")
    public ResponseEntity<Poster> getPosterById(@PathVariable String posterId) {
        Poster poster = posterService.getPosterById(posterId);
        if (poster != null) {
            return new ResponseEntity<>(poster, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping(value = "/{posterId}", consumes = {"multipart/form-data"})
    public ResponseEntity<Poster> updatePoster(
            @PathVariable String posterId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "dimensions", required = false) String dimensions,
            @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Poster existingPoster = posterService.getPosterById(posterId);
        if (existingPoster == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            if (image != null && !image.isEmpty()) {
                // Delete the old image from S3 if it exists
                if (existingPoster.getImageUrl() != null && !existingPoster.getImageUrl().isEmpty()) {
                    s3ImageService.deleteImage(existingPoster.getImageUrl());
                }
                // Upload the new image
                String newImageUrl = s3ImageService.uploadImage(image);
                existingPoster.setImageUrl(newImageUrl);
            }

            // Update other poster details if provided
            if (title != null) existingPoster.setTitle(title);
            if (description != null) existingPoster.setDescription(description);
            if (price != null) existingPoster.setPrice(price);
            if (category != null) existingPoster.setCategory(category);
            if (artist != null) existingPoster.setArtist(artist);
            if (dimensions != null) existingPoster.setDimensions(dimensions);
            if (stockQuantity != null) existingPoster.setStockQuantity(stockQuantity);

            Poster updatedPoster = posterService.updatePoster(existingPoster);
            return new ResponseEntity<>(updatedPoster, HttpStatus.OK);

        } catch (IOException e) {
            logger.error("Error updating image for poster {}: {}", posterId, e.getMessage(), e);
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{posterId}")
    public ResponseEntity<Void> deletePoster(@PathVariable String posterId) {
        Poster existingPoster = posterService.getPosterById(posterId);
        if (existingPoster != null) {
            // Optionally, delete the image from S3 when deleting the poster
            if (existingPoster.getImageUrl() != null && !existingPoster.getImageUrl().isEmpty()) {
                s3ImageService.deleteImage(existingPoster.getImageUrl());
            }
            posterService.deletePoster(posterId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    }

    // You can add more endpoints for listing, updating, and deleting posters later


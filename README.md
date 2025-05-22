Here's a comprehensive README for your "Poster-World" Spring Boot application. This README is designed to be clear, informative, and suitable for a resume portfolio, highlighting your skills and the application's capabilities.

---

# Poster-World: A Dynamic Poster E-commerce Platform

## 🌟 Project Overview

**Poster-World** is a robust backend e-commerce application built with Spring Boot, designed to manage and serve a rich catalog of artistic posters. It provides a comprehensive set of RESTful APIs for managing poster data, handling image uploads to AWS S3, and facilitating basic shopping cart functionalities. This application demonstrates scalable backend development, cloud integration, and efficient data management.

## ✨ Features

* **Poster Management (CRUD):** Full Create, Read, Update, and Delete operations for poster entities, including details like title, description, price, category, artist, and dimensions.
* **Dynamic Image Hosting:** Seamless integration with AWS S3 for secure and scalable storage of poster images. Images are uploaded to and retrieved from a private S3 bucket.
* **Pre-Signed URL Generation:** Images are served to the frontend via **AWS S3 Pre-Signed URLs**, ensuring secure, time-limited access to private assets without exposing AWS credentials to the client.
* **Category-Based Filtering:** Efficiently retrieve posters filtered by specific categories using DynamoDB Global Secondary Indexes.
* **Shopping Cart Functionality:**
    * Add items to a shopping cart (initial quantity of 1).
    * Update item quantities in the cart.
    * Remove items from the cart.
    * View current cart contents.
* **No-SQL Database Integration:** Utilizes AWS DynamoDB for flexible and highly available data storage.
* **Robust Backend Architecture:** Developed with Spring Boot for rapid development, dependency injection, and easy configuration.
* **RESTful APIs:** Provides clean and well-defined API endpoints for frontend consumption.

## 🚀 Technologies Used

* **Backend:** Java 17+
* **Framework:** Spring Boot 3.x
* **Database:** AWS DynamoDB
* **Cloud Storage:** AWS S3 (for image hosting)
* **AWS SDK:** AWS SDK for Java 2.x
* **Build Tool:** Maven
* **API Testing:** Postman
* **Logging:** SLF4J with Logback

---

## 🛠️ Setup and Local Installation

Follow these steps to get Poster-World up and running on your local machine.

### Prerequisites

* Java Development Kit (JDK) 17 or higher
* Apache Maven (or Gradle)
* AWS CLI (configured with appropriate credentials)
* An AWS Account

### AWS Configuration

1.  **S3 Bucket Creation:**
    * Create an S3 bucket in the `ap-south-1` (Mumbai) region.
    * **Crucially, keep this bucket private** (default settings, ensure "Block all public access" is enabled). The application will use pre-signed URLs to grant temporary access.
    * Note down your **Bucket Name** (e.g., `sirius-poster-world-images`).

2.  **DynamoDB Table Creation:**
    * Create a DynamoDB table named `Posters`.
    * Set the **Primary Key** to `posterId` (String).
    * Create a **Global Secondary Index (GSI)**:
        * **Index Name:** `CategoryIndex`
        * **Partition Key:** `category` (String)
        * **Projection:** `ALL` (or `KEYS_ONLY` if you prefer, but `ALL` makes loading easier)
    * Ensure your AWS account has permissions to read/write to this table.

3.  **IAM User/Role Permissions:**
    * Create an IAM User (for local development) or an IAM Role (for deployment on AWS services like EC2/Lambda).
    * Attach policies that grant:
        * `AmazonS3FullAccess` (or more restrictively: `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, `s3:GeneratePresignedUrl` on your specific S3 bucket `arn:aws:s3:::your-bucket-name/*`).
        * `AmazonDynamoDBFullAccess` (or more restrictively: `dynamodb:GetItem`, `dynamodb:PutItem`, `dynamodb:UpdateItem`, `dynamodb:DeleteItem`, `dynamodb:Scan`, `dynamodb:Query` on your specific DynamoDB table `arn:aws:dynamodb:ap-south-1:YOUR_ACCOUNT_ID:table/Posters`).
    * If using an IAM User, configure your AWS CLI or environment variables with the Access Key ID and Secret Access Key.

### Application Configuration (`src/main/resources/application.properties`)

Update your `application.properties` file with your AWS credentials and bucket name:

```properties
# AWS Configuration
aws.accessKeyId=YOUR_AWS_ACCESS_KEY_ID
aws.secretKey=YOUR_AWS_SECRET_ACCESS_KEY
aws.region=ap-south-1 # This is crucial for connecting to the Mumbai region

# S3 Bucket
aws.s3.bucketName=sirius-poster-world-images # Replace with your actual S3 bucket name

# DynamoDB Configuration
dynamodb.endpoint=
dynamodb.region=ap-south-1 # Ensure this matches your S3 region
```

* **Note:** For production deployments, it's highly recommended to use IAM Roles assigned to your AWS services (EC2, ECS, Lambda) instead of hardcoding `accessKeyId` and `secretKey` in `application.properties`.

### Build and Run

1.  **Clone the Repository:**
    ```bash
    git clone [Your Repository URL]
    cd poster-world
    ```
2.  **Build the Project:**
    ```bash
    mvn clean install
    ```
3.  **Run the Application:**
    ```bash
    java -jar target/poster-world-0.0.1-SNAPSHOT.jar
    ```
    The application will start on `http://localhost:8080`.

---

## 💡 API Endpoints

The Poster-World backend exposes the following RESTful API endpoints:

### Posters

* `POST /api/posters`
    * **Description:** Create a new poster entry.
    * **Request Body:** `Poster` object (JSON)
    * **Response:** Created `Poster` object (JSON) with a 201 Created status.
* `GET /api/posters`
    * **Description:** Retrieve all posters, with images served via pre-signed URLs.
    * **Response:** List of `Poster` objects (JSON).
* `GET /api/posters?category={categoryName}`
    * **Description:** Retrieve posters filtered by a specific category, with images served via pre-signed URLs.
    * **Response:** List of `Poster` objects (JSON).
* `GET /api/posters/{posterId}`
    * **Description:** Retrieve a single poster by its ID.
    * **Response:** `Poster` object (JSON).
* `PUT /api/posters/{posterId}`
    * **Description:** Update an existing poster by its ID.
    * **Request Body:** `Poster` object (JSON) with updated details.
    * **Response:** Updated `Poster` object (JSON).
* `DELETE /api/posters/{posterId}`
    * **Description:** Delete a poster by its ID.
    * **Response:** 204 No Content.

### Image Upload

* `POST /api/posters/uploadImage`
    * **Description:** Upload a poster image to AWS S3 and associate it with a poster ID.
    * **Request Body:** `multipart/form-data` with `file` (the image) and `posterId`.
    * **Response:** Confirmation message and image URL.

### Shopping Cart

* `POST /cart/add?posterId={id}&quantity={num}`
    * **Description:** Add an item to the shopping cart. If the item exists, quantity is incremented.
    * **Response:** "Item added to cart" (String).
* `GET /cart`
    * **Description:** View the current contents of the shopping cart.
    * **Response:** `ShoppingCart` object (JSON).
* `PUT /cart/update?posterId={id}&quantity={num}`
    * **Description:** Update the quantity of an item in the cart.
    * **Response:** "Cart updated" (String).
* `DELETE /cart/remove?posterId={id}`
    * **Description:** Remove an item from the cart.
    * **Response:** "Item removed from cart" (String).

---

## 🤝 Contributing

Feel free to fork the repository and contribute. Any enhancements, bug fixes, or new features are welcome!

## 📄 License

[Specify your license here, e.g., MIT License]

---
**Note:** This README was generated for a project developed with a focus on users in the Guwahati region, emphasizing efficient cloud resource utilization in nearby AWS regions like `ap-south-1` for optimal latency.

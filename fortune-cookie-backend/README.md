# Fortune Cookie Backend API Server

This is a Spring Boot application that serves as the backend for a Fortune Cookie service. It provides various API endpoints to manage and retrieve fortune messages.

- Spring Boot 3.x
- Java 17
- MongoDB
- Maven

![](./doc/backend-diagram.png)

## API Endpoints

1. **Create a Fortune**
    - POST `/fortunes`
    - Creates a new fortune message

2. **Get All Fortunes**
    - GET `/fortunes/all`
    - Retrieves all fortune messages

3. **Get Fortune by ID**
    - GET `/fortunes/{id}`
    - Retrieves a specific fortune by its ID

4. **Update Fortune**
    - PUT `/fortunes/{id}`
    - Updates an existing fortune

5. **Delete Fortune**
    - DELETE `/fortunes/{id}`
    - Deletes a specific fortune

6. **Delete All Fortunes**
    - DELETE `/fortunes/all`
    - Deletes all fortune messages

7. **Get Random Fortune**
    - GET `/fortunes/random`
    - Retrieves a random fortune message, the user can get a message from this API.

8. **Search Fortunes**
    - GET `/fortunes/search?keyword={keyword}`
    - Searches for fortunes containing the specified keyword

9. **Get Fortune Count**
    - GET `/fortunes/count`
    - Returns the total number of fortunes

10. **Bulk Update Fortunes**
    - POST `/fortunes/bulkupdate`
    - Loads fortune cookie messages from a file



## MongoDB Configuration

### Installation in Kubernetes cluster

To install MongoDB in a Kubernetes cluster using Helm, you can follow these steps. This guide will walk you through adding the Bitnami Helm repository, configuring a values.yaml file for custom settings, and deploying MongoDB with Helm.

1. Add the Bitnami Helm Repository
   First, you need to add the Bitnami Helm repository to your Helm configuration. Bitnami provides a well-maintained MongoDB Helm chart.
   ```bash
   helm repo add bitnami https://charts.bitnami.com/bitnami
   helm repo update
   ```

2. Create a Custom values.yaml File
   Create a values.yaml file to customize the MongoDB installation. This file allows you to set the number of replicas, authentication details, and other configurations.
   ```bash
   helm install my-mongodb bitnami/mongodb -f values.yaml
   ```

   This is the example of the `values.yaml` file for the database default setting. It will create a database named 'dev', user 'dev', password 'dev-password'.

   ```yaml
   # values.yaml
   replicaCount: 2
   auth:
     enabled: true
     rootPassword: root-password
     username: dev
     password: dev-password
     database: dev
   ```
   - replicaCount: Number of MongoDB replicas to deploy. Setting this to 2 provides high availability.
   - auth.enabled: Enables authentication for the MongoDB instance.
   - auth.rootPassword: Password for the root user.
   - auth.username: Username for a non-root user.
   - auth.password: Password for the non-root user.
   - auth.database: Default database to create for the non-root user.

3. Install MongoDB Using Helm
   With the values.yaml file configured, you can now install MongoDB using Helm. This command will deploy MongoDB with the settings specified in your values.yaml file.
   ```bash
   helm install my-mongodb bitnami/mongodb -f values.yaml
   ```

   - my-mongodb: This is the release name for your MongoDB deployment. You can choose any name you prefer.
   - bitnami/mongodb: This specifies the MongoDB chart from the Bitnami repository.
   - -f values.yaml: This flag tells Helm to use the custom values.yaml file for configuration.

4. Verify the Installation
   After running the Helm install command, you can verify that MongoDB is running in your Kubernetes cluster.
   ```bash
   kubectl get pods
   ```
   This command will list all the pods in your cluster, including the MongoDB pods. You should see pods with names like my-mongodb-0, my-mongodb-1, etc., depending on the replica count.

5. Access MongoDB
   To access MongoDB from within your Kubernetes cluster, you can use the service name my-mongodb (or whatever release name you chose) and the port specified in the service configuration.
   For external access, you might need to set up a Kubernetes service of type LoadBalancer or use port forwarding.

6. Clean Up
   If you need to uninstall MongoDB, you can do so with the following command:
   ```bash
   helm uninstall my-mongodb
   ```
   This will remove the MongoDB deployment and all associated resources from your Kubernetes cluster.

#### Additional Considerations
- Persistence: By default, the Bitnami MongoDB chart uses Persistent Volume Claims (PVCs) to store data. Ensure your cluster has a suitable StorageClass configured.
- Scaling: You can scale the MongoDB deployment by adjusting the replicaCount in the values.yaml file and running a helm upgrade.
- Monitoring: Consider integrating monitoring tools like Prometheus and Grafana to monitor the health and performance of your MongoDB deployment.
  By following these steps, you can deploy a robust and scalable MongoDB instance in your Kubernetes cluster using Helm.

### Database Configuration in the spring boot.

To configure MongoDB for the application database, add the following properties to your `application.properties` file:

```properties
spring.data.mongodb.uri=mongodb://${USER_NAME}:${USER_PW}@${MONGODB_HOST}:${MONGODB_PORT}/${DATABASE_NAME}
# e.g.) spring.data.mongodb.uri=mongodb://dev:dev-password@localhost:27017/dev
```
Replace with your MongoDB host, port, user, password and database name respectively.

## Building the Application
To build the application, run the following Maven command:
```bash
mvn clean package
```
This will create a JAR file in the target directory.



## Running the Application
To run the packaged application, use the following command:
```bash
java -jar target/fortunecookie-0.0.1-SNAPSHOT.jar
```
Replace fortunecookie-0.0.1-SNAPSHOT.jar with the actual name of the generated JAR file.

## Development
For development, you can run the application using Maven:

```bash
mvn spring-boot:run
```
## Testing
To run the tests, use the following Maven command:
```bash
mvn test
```
## API Documentation
For detailed API documentation, consider using Swagger or Spring REST Docs. You can access the Swagger UI at http://localhost:8080/swagger-ui.html when the application is running (if Swagger is configured).
Environment Variables
The application can be configured using environment variables. For example:
```bash
export SPRING_DATA_MONGODB_URI=mongodb://username:password@host:port/database
java -jar fortunecookie-0.0.1-SNAPSHOT.jar
```
## Docker Support
A Dockerfile is provided to containerize the application. Build the Docker image using:
```bash
docker build -t fortunecookie-backend .
```

### Run the container using:
```bash
docker run -p 8080:8080 fortunecookie-backend
```

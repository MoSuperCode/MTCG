# Protocol: Monster Trading Cards Game

## Project Overview

The Monster Trading Cards Game (MTCG) is a REST-based card trading platform where users can register, acquire card packages, battle against other players, and trade cards with others. The project was built using Java without any HTTP framework, implementing the server and REST API from scratch.

## Project Structure and Design

### Architecture

The project follows a classic layered architecture:

1. **HTTP Server Layer**: Handles HTTP requests and responses, routing
2. **Controller Layer**: Processes specific route requests, validates data, returns appropriate responses
3. **Service Layer**: Contains the business logic
4. **Data Layer**: Handles database interactions

### Class Diagram

```
+-------------------------+         +------------------------+          +----------------------+
|  HTTP Server Components |         |      Controllers       |          |      Services        |
+-------------------------+         +------------------------+          +----------------------+
| - Server                |<------->| - BattleController     |<-------->| - BattleService     |
| - Request               |         | - CardController       |<-------->| - CardService       |
| - Response              |         | - DeckController       |<-------->| - DeckService       |
| - Router                |         | - PackageController    |<-------->| - PackageService    |
| - RequestHandler        |         | - ScoreboardController |<-------->| - ScoreboardService |
| - Service (Interface)   |         | - StatsController      |<-------->| - StatsService      |
+-------------------------+         | - TradingController    |<-------->| - TradingService    |
                                    | - TransactionController|<-------->| - TransactionService|
                                    | - UserController       |<-------->| - UserService       |
                                    | - UserProfileController|<-------->| - UserProfileService|
                                    +------------------------+          +----------------------+
                                             |                                    |
                                             v                                    v
                                    +------------------------+          +----------------------+
                                    |        Models          |          |     Database         |
                                    +------------------------+          +----------------------+
                                    | - Card                 |<-------->| - Database          |
                                    | - User                 |          |   (PostgreSQL)      |
                                    | - BattleResult         |          +----------------------+
                                    +------------------------+
```

### Key Design Decisions

1. **Custom HTTP Server Implementation**
   - Built a complete HTTP server without relying on frameworks
   - Implemented request parsing, response generation, and routing from scratch
   - Used multithreading with ExecutorService for handling multiple client connections

2. **RESTful API Design**
   - Designed the API to follow REST principles
   - Used appropriate HTTP methods (GET, POST, PUT, DELETE) for CRUD operations
   - Used appropriate HTTP status codes for responses

3. **PostgreSQL Database**
   - Used PostgreSQL for data persistence
   - Designed a relational schema for users, cards, packages, trades, etc.
   - Implemented transaction handling for atomicity in critical operations

4. **Battle System Implementation**
   - Created a queue-based matching system for battles
   - Implemented special battle rules and element effectiveness
   - Added random card selection for battle rounds
   - Implemented card exchange on battle win

5. **Trading System**
   - Created a complete trading marketplace
   - Implemented trade requirements validation
   - Added protection against trading cards that are in a deck
   - Added ownership validation and transfer mechanism

## Lessons Learned

### Technical Lessons

1. **HTTP Protocol Understanding**
   - Gained deep understanding of HTTP request and response formats
   - Learned about proper header formatting and content negotiation
   - Discovered the importance of proper status code usage

2. **Database Transaction Management**
   - Learned the importance of transactions for maintaining data integrity
   - Implemented proper error handling and rollback mechanisms
   - Applied connection pooling for better performance

3. **Concurrency Handling**
   - Implemented thread-safe code for battle queue management
   - Used synchronization for critical sections
   - Learned about race conditions and how to avoid them

4. **JSON Handling**
   - Used Jackson for JSON serialization and deserialization
   - Learned about proper error handling for malformed JSON
   - Implemented validation for JSON input

### Project Management Lessons

1. **Importance of Testing**
   - Unit tests caught many bugs before they reached production
   - Learned about mocking for isolating components during testing
   - Discovered the value of test-driven development

2. **API Design Challenges**
   - Learned that clear API design simplifies implementation
   - Discovered the importance of consistency in API responses
   - Found that good error messages help troubleshooting

3. **Time Management**
   - Learned to prioritize core functionality first
   - Discovered that clean code takes longer but saves time in debugging
   - Found that documenting decisions helps maintain project focus

## Unit Testing Decisions

The project includes over 20 unit tests focusing on critical components:

1. **Model Tests**
   - `CardTest`: Tests card creation, property setting, and string representation
   - `UserTest`: Tests user creation, property setting, and token handling

2. **Controller Tests**
   - `BattleControllerTest`: Tests battle request handling, token validation, and response generation
   - `CardControllerTest`: Tests card retrieval and error handling

3. **Service Tests**
   - Tests for battle logic including the special rules for different card combinations
   - Tests for trading validation and card ownership verification
   - Tests for deck creation and validation

These components were chosen for testing because:

1. **Core Functionality**: They represent the most critical parts of the application
2. **Complex Logic**: They contain complex business logic that's prone to bugs
3. **Security**: Authentication and authorization are critical for security
4. **Data Integrity**: Operations affecting multiple database tables need careful verification

Mock objects were used to isolate components during testing, allowing each component to be tested independently without relying on external dependencies like the database.

## Unique Feature: Enhanced Battle System

The unique feature implemented is an enhanced battle system with detailed battle logs and strategic special rules. The battle system includes:

1. **Detailed Battle Logs**: Each battle generates a comprehensive log detailing:
   - Cards played in each round
   - Special rule effects that were triggered
   - Damage calculations with element effectiveness
   - Round outcomes and card exchanges
   - Final battle result

2. **Special Rule Implementation**: All special rules from the specification were implemented:
   - Goblins are afraid of Dragons
   - Wizards can control Orks
   - Knights drown against WaterSpells due to heavy armor
   - Kraken is immune to spells
   - FireElves can evade Dragon attacks

3. **Random Card Selection**: Instead of simply drawing from the top of the deck, cards are randomly selected for each round, adding strategic variety to the battles.

4. **Element Type Effectiveness**: Implemented the full elemental type system:
   - Water is effective against Fire (double damage)
   - Fire is effective against Normal (double damage)
   - Normal is effective against Water (double damage)
   - And the reverse relationships (half damage)

5. **Statistical Tracking**: The system tracks wins, losses, and ELO ratings, updating them after each battle.

## Time Tracking

| Task                               | Hours |
|------------------------------------|-------|
| Project Planning & Setup           | 5     |
| HTTP Server Implementation         | 15    |
| Database Design & Implementation   | 10    |
| User System (Register, Login)      | 8     |
| Card & Package System              | 12    |
| Deck Management                    | 6     |
| Battle System                      | 18    |
| Trading System                     | 10    |
| User Profiles & Stats              | 5     |
| Testing                            | 12    |
| Debugging                          | 8     |
| Documentation                      | 5     |
| **Total**                          | **114** |

## Git Repository

[Link to Git Repository](https://github.com/yourusername/MTCG)

## Technical Steps and Solutions

### HTTP Server Implementation

The HTTP server was implemented from scratch, parsing HTTP requests and generating HTTP responses according to the specification. The server uses a thread pool to handle multiple concurrent connections, with each client connection handled in a separate thread.

### Database Schema

The database schema includes tables for:
- users: User accounts with credentials and stats
- cards: Card information including properties and ownership
- packages: Available card packages
- package_cards: Links packages to their cards
- user_cards: Links users to their cards
- user_deck: Stores the user's configured deck
- trades: Available trade offers
- user_profiles: User profile information

### Authentication

Token-based authentication was implemented, with tokens generated at login and stored in the database. Each protected endpoint verifies the token before processing the request.

### Package Acquisition

The package acquisition system uses database transactions to ensure atomicity when:
- Checking if the user has enough coins
- Deducting coins from the user
- Assigning cards to the user
- Removing the package from available packages

### Battle System

The battle system uses a queue to match players for battles. When two players are in the queue, a battle is started with their currently configured decks. Special rules and element effectiveness are applied according to the specification.

### Trading System

The trading system allows users to create trade offers with specific requirements (card type and minimum damage). Other users can accept these offers if they have a card that meets the requirements.

## Challenges and Solutions

### Challenge 1: HTTP Parsing

Parsing HTTP requests correctly was challenging, especially handling headers and the request body.

**Solution**: Implemented a robust parsing system with careful handling of edge cases. Used manual testing with curl to verify correct parsing.

### Challenge 2: Concurrency in Battle System

The battle queue needed to be thread-safe to prevent race conditions when multiple users join battles simultaneously.

**Solution**: Used synchronized methods and a thread-safe queue implementation. Carefully managed the battle matching logic to ensure correct player pairing.

### Challenge 3: Card Type Detection

Automatically detecting if a card is a monster or spell based on its name was challenging.

**Solution**: Implemented a simple but effective rule: if the name contains "Spell", it's a spell card. For element types, checked for keywords like "Water", "Fire", etc.

### Challenge 4: Error Handling

Providing meaningful error messages while maintaining security was challenging.

**Solution**: Implemented a consistent error response format with appropriate HTTP status codes. Created specific error messages for common issues without exposing sensitive information.

## Conclusion

The Monster Trading Cards Game project was successfully implemented with all required features. The custom HTTP server handles requests correctly, the database stores all necessary data, and the business logic implements the game rules according to the specification. The testing suite ensures the correctness of critical components, and the enhanced battle system adds excitement to the game.

The project demonstrates how to build a complete client-server application from scratch, including networking, database integration, and business logic implementation.

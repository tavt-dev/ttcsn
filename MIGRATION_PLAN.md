# Friendify Modular Monolith Migration Plan

This plan is based on the repository state inspected under `c:\Users\Admin\Desktop\friendify`.
No source code should be moved until each migration slice below is reviewed and tested.

## 1. Current Services And Modules

| Module | Main package | Main application class | Build file | Config files |
|---|---|---|---|---|
| `api-gateway` | `com.tien.apigateway` | `api-gateway/src/main/java/com/tien/apigateway/ApiGatewayApplication.java` | `api-gateway/pom.xml` | `api-gateway/src/main/resources/application.yaml`, `config-server/src/main/resources/config/api-gateway.yaml` |
| `config-server` | `com.tien.configserver` | `config-server/src/main/java/com/tien/configserver/ConfigServerApplication.java` | `config-server/pom.xml` | `config-server/src/main/resources/application.yaml`, `config-server/src/main/resources/config/*.yaml` |
| `identity-service` | `com.tien.identityservice`; also `com.tien.event.dto` | `identity-service/src/main/java/com/tien/identityservice/IdentityServiceApplication.java` | `identity-service/pom.xml` | `identity-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/identity-service.yaml` |
| `profile-service` | `com.tien.profileservice` | `profile-service/src/main/java/com/tien/profileservice/ProfileServiceApplication.java` | `profile-service/pom.xml` | `profile-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/profile-service.yaml` |
| `post-service` | `com.tien.postservice` | `post-service/src/main/java/com/tien/postservice/PostServiceApplication.java` | `post-service/pom.xml` | `post-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/post-service.yaml` |
| `interaction-service` | `com.tien.interactionservice` | `interaction-service/src/main/java/com/tien/interactionservice/InteractionServiceApplication.java` | `interaction-service/pom.xml` | `interaction-service/src/main/resources/application.yaml`, `interaction-service/src/main/resources/application.properties`, `config-server/src/main/resources/config/interaction-service.yaml` |
| `social-service` | `com.tien.socialservice` | `social-service/src/main/java/com/tien/socialservice/SocialServiceApplication.java` | `social-service/pom.xml` | `social-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/social-service.yaml` |
| `group-service` | `com.tien.groupservice` | `group-service/src/main/java/com/tien/groupservice/GroupServiceApplication.java` | `group-service/pom.xml` | `group-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/group-service.yaml` |
| `chat-service` | `com.tien.chatservice` | `chat-service/src/main/java/com/tien/chatservice/ChatServiceApplication.java` | `chat-service/pom.xml` | `chat-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/chat-service.yaml` |
| `file-service` | `com.tien.fileservice` | `file-service/src/main/java/com/tien/fileservice/FileServiceApplication.java` | `file-service/pom.xml` | `file-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/file-service.yaml` |
| `notification-service` | `com.tien.notificationservice`; also `com.tien.event.dto` | `notification-service/src/main/java/com/tien/notificationservice/NotificationServiceApplication.java` | `notification-service/pom.xml` | `notification-service/src/main/resources/application.yaml`, `config-server/src/main/resources/config/notification-service.yaml` |
| `shared-common` | `com.tien.sharedcommon` | none | `shared-common/pom.xml` | none found |
| `shared-contacts` | `com.tien.sharedcontacts` | none | `shared-contacts/pom.xml` | none found |
| `monolith` | `com.friendify.app` | `monolith/src/main/java/com/friendify/app/MonolithApplication.java` | `monolith/pom.xml` | `monolith/src/main/resources/application.properties` |

Version inventory: every current `pom.xml` inspected uses Spring Boot `3.5.5` and `java.version` `17`. `spring-cloud.version` is `2025.0.0` in services that use Spring Cloud. `AGENTS.md` currently says Java 21 and Spring Boot 4.0.6; this conflicts with the checked build files and needs manual review before changing `monolith/pom.xml`.

## 2. Service Responsibilities

### `api-gateway`

- Domain: gateway routing, token introspection filter, CORS/WebClient setup.
- Important files:
  - `api-gateway/src/main/java/com/tien/apigateway/configuration/AuthenticationFilter.java`
  - `api-gateway/src/main/java/com/tien/apigateway/configuration/WebClientConfiguration.java`
  - `api-gateway/src/main/java/com/tien/apigateway/repository/IdentityClient.java`
  - `api-gateway/src/main/java/com/tien/apigateway/service/IdentityService.java`
- Gateway routes in `config-server/src/main/resources/config/api-gateway.yaml` expose:
  - `/api/v1/identity/** -> http://localhost:8081`, `StripPrefix=2`
  - `/api/v1/profile/** -> http://localhost:8082`, `StripPrefix=2`
  - `/profile/internal/** -> http://localhost:8082`, `StripPrefix=1`
  - `/api/v1/notification/** -> http://localhost:8083`, `StripPrefix=2`
  - `/api/v1/post/** -> http://localhost:8084`, `StripPrefix=2`
  - `/api/v1/file/** -> http://localhost:8085`, `StripPrefix=2`
  - `/api/v1/chat/** -> http://localhost:8086`, `StripPrefix=2`
  - `/api/v1/social/** -> http://localhost:8087`, `StripPrefix=2`
  - `/api/v1/interaction/** -> http://localhost:8088`, `StripPrefix=2`
  - `/api/v1/group/** -> http://localhost:8089`, `StripPrefix=2`

### `identity-service`

- Domain: registration, login, JWT lifecycle, OAuth2 Google login, roles, permissions, password reset OTP, profile creation trigger, notification email trigger.
- Important classes:
  - Controllers: `AuthenticationController`, `UserController`, `RoleController`, `PermissionController`
  - Services: `AuthenticationService`, `UserService`, `JwtService`, `OtpService`, `OAuth2Service`, `ProfileService`, `NotificationService`, `RoleService`, `PermissionService`
  - Repositories: `UserRepository`, `RoleRepository`, `PermissionRepository`, `InvalidatedTokenRepository`, `UserOtpRepository`
  - Security/config: `SecurityConfig`, `CustomJwtDecoder`, `JwtAuthenticationEntryPoint`, OAuth2 handlers, `ApplicationInitConfig`

### `profile-service`

- Domain: user profile CRUD/search and avatar/background update.
- Important classes:
  - Controllers: `ProfileController`, `InternalProfileController`
  - Service: `ProfileService`, `ImageUploadKafkaService`
  - Repository: `ProfileRepository`
  - Mapper/config: `ProfileMapper`, `SecurityConfig`, `FeignConfiguration`

### `post-service`

- Domain: posts, feeds, saved posts, shared posts, group posts, multipart image upload orchestration.
- Important classes:
  - Controllers: `PostController`, `InternalPostController`
  - Service: `PostService`, `ImageUploadKafkaService`, `DateTimeFormatter`
  - Repositories: `PostRepository`, `SavedPostRepository`, `SharedPostRepository`
  - HTTP clients: `ProfileClient`, `SocialClient`, `InteractionClient`, `GroupClient`

### `interaction-service`

- Domain: likes and comments for posts/comments, counters, cleanup on post delete events.
- Important classes:
  - Controllers: `LikeController`, `CommentController`, `InternalInteractionController`
  - Services: `LikeService`, `CommentService`
  - Repositories: `LikeRepository`, `CommentRepository`
  - Listeners/events: `PostEventListener`, `UserEventListener`, `LikeEvent`, `CommentEvent`, `PostEvent`, `UserEvent`
  - HTTP clients: `PostClient`, `ProfileClient`

### `social-service`

- Domain: friendships, follows, blocks, social counts, suggested/mutual friend queries.
- Important classes:
  - Controllers: `FriendshipController`, `FollowController`, `UserBlockController`, `InternalSocialController`
  - Services: `FriendshipService`, `FollowService`, `UserBlockService`
  - Repositories: `FriendshipRepository`, `FollowRepository`, `UserBlockRepository`
  - HTTP client: `ProfileClient`

### `group-service`

- Domain: group CRUD, membership, join requests, role checks, group avatar/cover upload orchestration.
- Important classes:
  - Controllers: `GroupController`, `InternalGroupController`
  - Service: `GroupService`, `ImageUploadKafkaService`, `DateTimeFormatter`
  - Repositories: `GroupRepository`, `GroupMemberRepository`, `JoinRequestRepository`
  - HTTP client: `ProfileClient`

### `file-service`

- Domain: Cloudinary image upload, image metadata persistence, upload response contracts.
- Important classes:
  - Controller: `CloudMediaController`
  - Service/listener: `ImageService`, `ImageUploadListener`
  - Repository: `ImageRepository`
  - Config: `CloudinaryConfig`, `MongoConfig`
  - Shared contracts used: `ImageUploadEvent`, `ImageUploadedEvent`, `ImageType`, `ImageTopics`

### `notification-service`

- Domain: user notifications, email delivery through Brevo, Kafka notification delivery consumer.
- Important classes:
  - Controllers/components: `UserNotificationController`, `EmailController`, `NotificationController`
  - Services: `NotificationService`, `EmailService`
  - Repository/client: `NotificationRepository`, `EmailClient`
  - Event: `notification-service/src/main/java/com/tien/event/dto/NotificationEvent.java`

### `chat-service`

- Domain: conversations, messages, read receipts, STOMP/WebSocket chat events.
- Important classes:
  - Controllers: `ConversationController`, `ChatMessageController`, `WebSocketController`
  - Services: `ConversationService`, `ChatMessageService`, `ReadReceiptService`
  - Repositories: `ConversationRepository`, `ChatMessageRepository`, `ReadReceiptRepository`
  - WebSocket/security: `WebSocketConfig`, `WebSocketAuthInterceptor`, `SecurityConfig`
  - HTTP client: `ProfileClient`

## 3. REST And WebSocket APIs

Gateway-visible public paths should be preserved as `/api/v1/{domain}` plus each service's local controller path, because gateway strips `/api/v1/{domain}` before forwarding.

Additional framework/gateway endpoints and review notes:

- `api-gateway/src/main/java/com/tien/apigateway/configuration/AuthenticationFilter.java` has no business controller, but it permits public gateway paths matching `/api/v1/identity/auth/.*`, `/api/v1/identity/oauth2/.*`, `/api/v1/identity/login/oauth2/.*`, `/api/v1/notification/email/send`, and `/api/v1/file/media/download/.*`. Needs manual review: no active `/file/media/download/**` endpoint was found in `file-service`.
- `identity-service/src/main/java/com/tien/identityservice/configuration/SecurityConfig.java` permits Spring Security OAuth2 paths `/oauth2/**` and `/login/oauth2/**`. These are framework-generated, not controller methods, but must be preserved if Google OAuth2 login stays in the monolith.
- Each service config under `config-server/src/main/resources/config/*-service.yaml` sets Swagger/OpenAPI paths `/v3/api-docs` and `/swagger-ui.html`; preserve or intentionally replace them in the monolith.
- `config-server` exposes Spring Cloud Config server endpoints and actuator/management endpoints from `config-server/src/main/resources/application.yaml`; these are infrastructure endpoints, not Friendify business APIs. Retire only after all service config has moved into monolith config/environment variables.

### Identity APIs, local context path `/identity`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| POST | `/auth/registration` | `/api/v1/identity/auth/registration` | `AuthenticationController` | `UserCreationRequest` | `ApiResponse<UserResponse>` |
| POST | `/auth/verify-user` | `/api/v1/identity/auth/verify-user` | `AuthenticationController` | `VerifyUserRequest` | `ApiResponse<Void>` |
| POST | `/auth/resend-verification` | `/api/v1/identity/auth/resend-verification` | `AuthenticationController` | `ResendOtpRequest` | `ApiResponse<Void>` |
| POST | `/auth/token` | `/api/v1/identity/auth/token` | `AuthenticationController` | `AuthenticationRequest` | `ApiResponse<AuthenticationResponse>` |
| POST | `/auth/introspect` | `/api/v1/identity/auth/introspect` | `AuthenticationController` | `IntrospectRequest` | `ApiResponse<IntrospectResponse>` |
| POST | `/auth/refresh` | `/api/v1/identity/auth/refresh` | `AuthenticationController` | `RefreshTokenRequest` | `ApiResponse<AuthenticationResponse>` |
| POST | `/auth/logout` | `/api/v1/identity/auth/logout` | `AuthenticationController` | `LogoutRequest` | `ApiResponse<Void>` |
| POST | `/auth/forgot-password` | `/api/v1/identity/auth/forgot-password` | `AuthenticationController` | `ForgotPasswordRequest` | `ApiResponse<Void>` |
| POST | `/auth/reset-password` | `/api/v1/identity/auth/reset-password` | `AuthenticationController` | `ResetPasswordRequest` | `ApiResponse<Void>` |
| GET | `/users` | `/api/v1/identity/users` | `UserController` | none | `ApiResponse<List<UserResponse>>` |
| GET | `/users/{userId}` | `/api/v1/identity/users/{userId}` | `UserController` | none | `ApiResponse<UserResponse>` |
| GET | `/users/myInfo` | `/api/v1/identity/users/myInfo` | `UserController` | none | `ApiResponse<UserResponse>` |
| PUT | `/users/{userId}` | `/api/v1/identity/users/{userId}` | `UserController` | `UserUpdateRequest` | `ApiResponse<UserResponse>` |
| DELETE | `/users/{userId}` | `/api/v1/identity/users/{userId}` | `UserController` | none | `ApiResponse<String>` |
| PUT | `/users/change-password` | `/api/v1/identity/users/change-password` | `UserController` | `ChangePasswordRequest` | `ApiResponse<Void>` |
| POST | `/roles` | `/api/v1/identity/roles` | `RoleController` | `RoleRequest` | `ApiResponse<RoleResponse>` |
| GET | `/roles` | `/api/v1/identity/roles` | `RoleController` | none | `ApiResponse<List<RoleResponse>>` |
| DELETE | `/roles/{role}` | `/api/v1/identity/roles/{role}` | `RoleController` | none | `ApiResponse<Void>` |
| POST | `/permissions` | `/api/v1/identity/permissions` | `PermissionController` | `PermissionRequest` | `ApiResponse<PermissionResponse>` |
| GET | `/permissions` | `/api/v1/identity/permissions` | `PermissionController` | none | `ApiResponse<List<PermissionResponse>>` |
| DELETE | `/permissions/{permission}` | `/api/v1/identity/permissions/{permission}` | `PermissionController` | none | `ApiResponse<Void>` |

### Profile APIs, local context path `/profile`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| GET | `/users/{profileId}` | `/api/v1/profile/users/{profileId}` | `ProfileController` | none | `ApiResponse<ProfileResponse>` |
| GET | `/users` | `/api/v1/profile/users` | `ProfileController` | none | `ApiResponse<List<ProfileResponse>>` |
| GET | `/users/my-profile` | `/api/v1/profile/users/my-profile` | `ProfileController` | none | `ApiResponse<ProfileResponse>` |
| PUT | `/users/my-profile` | `/api/v1/profile/users/my-profile` | `ProfileController` | `UpdateProfileRequest` | `ApiResponse<ProfileResponse>` |
| POST | `/users/search` | `/api/v1/profile/users/search` | `ProfileController` | `SearchUserRequest` | `ApiResponse<List<ProfileResponse>>` |
| PUT | `/users/avatar` | `/api/v1/profile/users/avatar` | `ProfileController` | multipart `file` | `ApiResponse<ProfileResponse>` |
| PUT | `/users/background` | `/api/v1/profile/users/background` | `ProfileController` | multipart `file` | `ApiResponse<ProfileResponse>` |
| POST | `/internal/users` | internal only | `InternalProfileController` | `ProfileCreationRequest` | `ApiResponse<ProfileResponse>` |
| GET | `/internal/users/{userId}` | internal only | `InternalProfileController` | none | `ApiResponse<ProfileResponse>` |
| GET | `/internal/users/batch?userIds=...` | internal only | `InternalProfileController` | query `List<String>` | `ApiResponse<List<ProfileResponse>>` |

### Post APIs, local context path `/post`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| POST | `/create` | `/api/v1/post/create` | `PostController` | multipart params `content`, `images`, `privacy`, `groupId` | `ApiResponse<PostResponse>` |
| POST | `/json` | `/api/v1/post/json` | `PostController` | `PostRequest` | `ApiResponse<PostResponse>` |
| GET | `/my-posts` | `/api/v1/post/my-posts` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| POST | `/save/{postId}` | `/api/v1/post/save/{postId}` | `PostController` | none | `ApiResponse<Void>` |
| DELETE | `/unsave/{postId}` | `/api/v1/post/unsave/{postId}` | `PostController` | none | `ApiResponse<Void>` |
| GET | `/saved-posts` | `/api/v1/post/saved-posts` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| POST | `/share/{postId}` | `/api/v1/post/share/{postId}` | `PostController` | query `content` | `ApiResponse<PostResponse>` |
| GET | `/shared-posts/{postId}` | `/api/v1/post/shared-posts/{postId}` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| GET | `/share-count/{postId}` | `/api/v1/post/share-count/{postId}` | `PostController` | none | `ApiResponse<Long>` |
| GET | `/is-saved/{postId}` | `/api/v1/post/is-saved/{postId}` | `PostController` | none | `ApiResponse<Boolean>` |
| GET | `/user/{userId}` | `/api/v1/post/user/{userId}` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| GET | `/my-shared-posts` | `/api/v1/post/my-shared-posts` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| GET | `/saved-count` | `/api/v1/post/saved-count` | `PostController` | none | `ApiResponse<Long>` |
| GET | `/search?keyword=...` | `/api/v1/post/search` | `PostController` | query `keyword`, `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| GET | `/{postId}` | `/api/v1/post/{postId}` | `PostController` | none | `ApiResponse<PostResponse>` |
| PUT | `/{postId}` | `/api/v1/post/{postId}` | `PostController` | multipart `content`, `images`, `privacy` | `ApiResponse<PostResponse>` |
| PUT | `/{postId}/json` | `/api/v1/post/{postId}/json` | `PostController` | `UpdatePostRequest` | `ApiResponse<PostResponse>` |
| DELETE | `/{postId}` | `/api/v1/post/{postId}` | `PostController` | none | `ApiResponse<Void>` |
| GET | `/public` | `/api/v1/post/public` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| GET | `/feed` | `/api/v1/post/feed` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| GET | `/group/{groupId}` | `/api/v1/post/group/{groupId}` | `PostController` | query `page`, `size` | `ApiResponse<PageResponse<PostResponse>>` |
| GET | `/internal/posts/{postId}/exists` | internal only | `InternalPostController` | none | `ApiResponse<Boolean>` |

### Interaction APIs, local context path `/interaction`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| POST | `/likes` | `/api/v1/interaction/likes` | `LikeController` | `CreateLikeRequest` | `ApiResponse<LikeResponse>` |
| DELETE | `/likes/{id}` | `/api/v1/interaction/likes/{id}` | `LikeController` | none | `ApiResponse<Void>` |
| DELETE | `/likes/post/{postId}` | `/api/v1/interaction/likes/post/{postId}` | `LikeController` | none | `ApiResponse<Void>` |
| DELETE | `/likes/comment/{commentId}` | `/api/v1/interaction/likes/comment/{commentId}` | `LikeController` | none | `ApiResponse<Void>` |
| GET | `/likes/post/{postId}` | `/api/v1/interaction/likes/post/{postId}` | `LikeController` | query `page`, `size` | `ApiResponse<PageResponse<LikeResponse>>` |
| POST | `/comments` | `/api/v1/interaction/comments` | `CommentController` | `CreateCommentRequest` | `ApiResponse<CommentResponse>` |
| GET | `/comments/post/{postId}` | `/api/v1/interaction/comments/post/{postId}` | `CommentController` | query `page`, `size` | `ApiResponse<PageResponse<CommentResponse>>` |
| GET | `/comments/{id}` | `/api/v1/interaction/comments/{id}` | `CommentController` | none | `ApiResponse<CommentResponse>` |
| GET | `/comments/{id}/replies` | `/api/v1/interaction/comments/{id}/replies` | `CommentController` | query `page`, `size` | `ApiResponse<PageResponse<CommentResponse>>` |
| PUT | `/comments/{id}` | `/api/v1/interaction/comments/{id}` | `CommentController` | `UpdateCommentRequest` | `ApiResponse<CommentResponse>` |
| DELETE | `/comments/{id}` | `/api/v1/interaction/comments/{id}` | `CommentController` | none | `ApiResponse<Void>` |
| GET | `/internal/likes/post/{postId}/count` | internal only | `InternalInteractionController` | none | `ApiResponse<Long>` |
| GET | `/internal/likes/post/{postId}/is-liked` | internal only | `InternalInteractionController` | none | `ApiResponse<Boolean>` |
| GET | `/internal/comments/post/{postId}/count` | internal only | `InternalInteractionController` | none | `ApiResponse<Long>` |

### Social APIs, local context path `/social`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| POST | `/friendships/{friendId}` | `/api/v1/social/friendships/{friendId}` | `FriendshipController` | none | `ApiResponse<FriendshipResponse>` |
| POST | `/friendships/{friendId}/accept` | `/api/v1/social/friendships/{friendId}/accept` | `FriendshipController` | none | `ApiResponse<FriendshipResponse>` |
| POST | `/friendships/{friendId}/reject` | `/api/v1/social/friendships/{friendId}/reject` | `FriendshipController` | none | `ApiResponse<Void>` |
| DELETE | `/friendships/{friendId}` | `/api/v1/social/friendships/{friendId}` | `FriendshipController` | none | `ApiResponse<Void>` |
| GET | `/friendships/friends` | `/api/v1/social/friendships/friends` | `FriendshipController` | query `page`, `size` | `ApiResponse<PageResponse<FriendshipResponse>>` |
| GET | `/friendships/sent-requests` | `/api/v1/social/friendships/sent-requests` | `FriendshipController` | query `page`, `size` | `ApiResponse<PageResponse<FriendshipResponse>>` |
| GET | `/friendships/received-requests` | `/api/v1/social/friendships/received-requests` | `FriendshipController` | query `page`, `size` | `ApiResponse<PageResponse<FriendshipResponse>>` |
| GET | `/friendships/search?keyword=...` | `/api/v1/social/friendships/search` | `FriendshipController` | query `keyword` | `ApiResponse<List<ProfileResponse>>` |
| GET | `/friendships/status/{friendId}` | `/api/v1/social/friendships/status/{friendId}` | `FriendshipController` | none | `ApiResponse<String>` |
| GET | `/friendships/mutual/{friendId}` | `/api/v1/social/friendships/mutual/{friendId}` | `FriendshipController` | none | `ApiResponse<List<ProfileResponse>>` |
| GET | `/friendships/suggested` | `/api/v1/social/friendships/suggested` | `FriendshipController` | query `limit` | `ApiResponse<List<ProfileResponse>>` |
| GET | `/friendships/pending-requests/count` | `/api/v1/social/friendships/pending-requests/count` | `FriendshipController` | none | `ApiResponse<Long>` |
| DELETE | `/friendships/{friendId}/cancel` | `/api/v1/social/friendships/{friendId}/cancel` | `FriendshipController` | none | `ApiResponse<Void>` |
| GET | `/friendships/sent-requests/count` | `/api/v1/social/friendships/sent-requests/count` | `FriendshipController` | none | `ApiResponse<Long>` |
| POST | `/friendships/batch-status` | `/api/v1/social/friendships/batch-status` | `FriendshipController` | `List<String>` | `ApiResponse<FriendshipStatusResponse>` |
| GET | `/friendships/counts` | `/api/v1/social/friendships/counts` | `FriendshipController` | none | `ApiResponse<SocialCountsResponse>` |
| POST | `/follows/{followingId}` | `/api/v1/social/follows/{followingId}` | `FollowController` | none | `ApiResponse<FollowResponse>` |
| DELETE | `/follows/{followingId}` | `/api/v1/social/follows/{followingId}` | `FollowController` | none | `ApiResponse<Void>` |
| GET | `/follows/following/{userId}` | `/api/v1/social/follows/following/{userId}` | `FollowController` | query `page`, `size` | `ApiResponse<PageResponse<FollowResponse>>` |
| GET | `/follows/followers/{userId}` | `/api/v1/social/follows/followers/{userId}` | `FollowController` | query `page`, `size` | `ApiResponse<PageResponse<FollowResponse>>` |
| GET | `/follows/info/{userId}` | `/api/v1/social/follows/info/{userId}` | `FollowController` | none | `ApiResponse<UserSocialInfoResponse>` |
| POST | `/blocks/{blockedId}` | `/api/v1/social/blocks/{blockedId}` | `UserBlockController` | none | `ApiResponse<UserBlockResponse>` |
| DELETE | `/blocks/{blockedId}` | `/api/v1/social/blocks/{blockedId}` | `UserBlockController` | none | `ApiResponse<Void>` |
| GET | `/blocks` | `/api/v1/social/blocks` | `UserBlockController` | query `page`, `size` | `ApiResponse<PageResponse<UserBlockResponse>>` |
| GET | `/blocks/check/{blockedId}` | `/api/v1/social/blocks/check/{blockedId}` | `UserBlockController` | none | `ApiResponse<Boolean>` |
| GET | `/internal/friend-ids` | internal only | `InternalSocialController` | none | `ApiResponse<List<String>>` |
| GET | `/internal/following-ids` | internal only | `InternalSocialController` | none | `ApiResponse<List<String>>` |
| GET | `/internal/blocks/ids` | internal only | `InternalSocialController` | none | `ApiResponse<List<String>>` |

### Group APIs, local context path `/group`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| POST | `/groups` | `/api/v1/group/groups` | `GroupController` | `CreateGroupRequest` | `ApiResponse<GroupResponse>` |
| PUT | `/groups/{groupId}` | `/api/v1/group/groups/{groupId}` | `GroupController` | `UpdateGroupRequest` | `ApiResponse<GroupResponse>` |
| DELETE | `/groups/{groupId}` | `/api/v1/group/groups/{groupId}` | `GroupController` | none | `ApiResponse<Void>` |
| PUT | `/groups/{groupId}/avatar` | `/api/v1/group/groups/{groupId}/avatar` | `GroupController` | multipart `file` | `ApiResponse<GroupResponse>` |
| PUT | `/groups/{groupId}/cover` | `/api/v1/group/groups/{groupId}/cover` | `GroupController` | multipart `file` | `ApiResponse<GroupResponse>` |
| GET | `/groups` | `/api/v1/group/groups` | `GroupController` | query `privacy`, `page`, `size` | `ApiResponse<PageResponse<GroupResponse>>` |
| GET | `/groups/{groupId}` | `/api/v1/group/groups/{groupId}` | `GroupController` | none | `ApiResponse<GroupResponse>` |
| POST | `/groups/{groupId}/members/{userId}` | `/api/v1/group/groups/{groupId}/members/{userId}` | `GroupController` | none | `ApiResponse<Void>` |
| DELETE | `/groups/{groupId}/members/{userId}` | `/api/v1/group/groups/{groupId}/members/{userId}` | `GroupController` | none | `ApiResponse<Void>` |
| PUT | `/groups/{groupId}/members/{userId}/role` | `/api/v1/group/groups/{groupId}/members/{userId}/role` | `GroupController` | `UpdateMemberRoleRequest` | `ApiResponse<Void>` |
| GET | `/groups/{groupId}/members` | `/api/v1/group/groups/{groupId}/members` | `GroupController` | query `role`, `page`, `size` | `ApiResponse<PageResponse<GroupMemberResponse>>` |
| POST | `/groups/{groupId}/join` | `/api/v1/group/groups/{groupId}/join` | `GroupController` | optional `JoinGroupRequest` | `ApiResponse<Void>` |
| POST | `/groups/{groupId}/leave` | `/api/v1/group/groups/{groupId}/leave` | `GroupController` | none | `ApiResponse<Void>` |
| POST | `/groups/{groupId}/join-requests/{requestId}/process` | `/api/v1/group/groups/{groupId}/join-requests/{requestId}/process` | `GroupController` | `ProcessJoinRequest` | `ApiResponse<Void>` |
| GET | `/groups/{groupId}/join-requests` | `/api/v1/group/groups/{groupId}/join-requests` | `GroupController` | query `page`, `size` | `ApiResponse<PageResponse<JoinRequestResponse>>` |
| DELETE | `/groups/{groupId}/join-requests/{requestId}` | `/api/v1/group/groups/{groupId}/join-requests/{requestId}` | `GroupController` | none | `ApiResponse<Void>` |
| GET | `/groups/my-join-requests` | `/api/v1/group/groups/my-join-requests` | `GroupController` | query `page`, `size` | `ApiResponse<PageResponse<JoinRequestResponse>>` |
| GET | `/groups/my-groups` | `/api/v1/group/groups/my-groups` | `GroupController` | query `page`, `size` | `ApiResponse<PageResponse<GroupResponse>>` |
| GET | `/groups/joined-groups` | `/api/v1/group/groups/joined-groups` | `GroupController` | query `page`, `size` | `ApiResponse<PageResponse<GroupResponse>>` |
| GET | `/groups/search` | `/api/v1/group/groups/search` | `GroupController` | query `keyword`, `page`, `size` | `ApiResponse<PageResponse<GroupResponse>>` |
| GET | `/internal/groups/{groupId}/exists` | internal only | `InternalGroupController` | none | `ApiResponse<Boolean>` |
| GET | `/internal/groups/{groupId}` | internal only | `InternalGroupController` | none | `ApiResponse<GroupResponse>` |
| GET | `/internal/groups/{groupId}/can-post` | internal only | `InternalGroupController` | none | `ApiResponse<Boolean>` |
| GET | `/internal/groups/{groupId}/can-view` | internal only | `InternalGroupController` | none | `ApiResponse<Boolean>` |
| GET | `/internal/groups/{groupId}/can-view/{userId}` | internal only | `InternalGroupController` | none | `ApiResponse<Boolean>` |

### File APIs, local context path `/file`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| POST | `/images/upload` | `/api/v1/file/images/upload` | `CloudMediaController` | `ImageUploadEvent` | `ResponseEntity<ImageUploadedEvent>` |
| POST | `/images/upload-form-data` | `/api/v1/file/images/upload-form-data` | `CloudMediaController` | multipart `file`, `type`, `ownerId`, optional `postId` | `ResponseEntity<UploadResponse>` |
| POST | `/images/upload-multiple-form-data` | `/api/v1/file/images/upload-multiple-form-data` | `CloudMediaController` | multipart `files`, `type`, `ownerId`, optional `postId` | `ResponseEntity<List<UploadResponse>>` |

Commented-out endpoints exist in `CloudMediaController` for image info/download/delete/batch delete; treat them as inactive unless re-enabled explicitly.

### Notification APIs, local context path `/notification`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| GET | `/notifications` | `/api/v1/notification/notifications` | `UserNotificationController` | query `page`, `size` | `ApiResponse<PageResponse<NotificationResponse>>` |
| PUT | `/notifications/{id}/read` | `/api/v1/notification/notifications/{id}/read` | `UserNotificationController` | none | `ApiResponse<NotificationResponse>` |
| PUT | `/notifications/read-all` | `/api/v1/notification/notifications/read-all` | `UserNotificationController` | none | `ApiResponse<Void>` |
| GET | `/notifications/unread-count` | `/api/v1/notification/notifications/unread-count` | `UserNotificationController` | none | `ApiResponse<Long>` |
| POST | `/email/send` | `/api/v1/notification/email/send` | `EmailController` | `SendEmailRequest` | `ApiResponse<EmailResponse>` |

`NotificationController` is a Kafka component, not a REST controller.

### Chat APIs And WebSocket, local context path `/chat`

| Method | Local path | Gateway path | Controller | Request DTO | Response |
|---|---|---|---|---|---|
| POST | `/conversations` | `/api/v1/chat/conversations` | `ConversationController` | `ConversationRequest` | `ApiResponse<ConversationResponse>` |
| GET | `/conversations/my-conversations` | `/api/v1/chat/conversations/my-conversations` | `ConversationController` | none | `ApiResponse<List<ConversationResponse>>` |
| GET | `/conversations/{id}` | `/api/v1/chat/conversations/{id}` | `ConversationController` | none | `ApiResponse<ConversationResponse>` |
| PUT | `/conversations/{id}` | `/api/v1/chat/conversations/{id}` | `ConversationController` | `UpdateConversationRequest` | `ApiResponse<ConversationResponse>` |
| DELETE | `/conversations/{id}` | `/api/v1/chat/conversations/{id}` | `ConversationController` | none | `ApiResponse<Void>` |
| POST | `/conversations/{id}/participants` | `/api/v1/chat/conversations/{id}/participants` | `ConversationController` | `AddParticipantRequest` | `ApiResponse<ConversationResponse>` |
| DELETE | `/conversations/{id}/participants/{participantId}` | `/api/v1/chat/conversations/{id}/participants/{participantId}` | `ConversationController` | none | `ApiResponse<ConversationResponse>` |
| POST | `/conversations/{id}/leave` | `/api/v1/chat/conversations/{id}/leave` | `ConversationController` | none | `ApiResponse<Void>` |
| POST | `/conversations/{id}/admins` | `/api/v1/chat/conversations/{id}/admins` | `ConversationController` | `AddAdminRequest` | `ApiResponse<ConversationResponse>` |
| DELETE | `/conversations/{id}/admins/{participantId}` | `/api/v1/chat/conversations/{id}/admins/{participantId}` | `ConversationController` | none | `ApiResponse<ConversationResponse>` |
| POST | `/messages` | `/api/v1/chat/messages` | `ChatMessageController` | `ChatMessageRequest` | `ApiResponse<ChatMessageResponse>` |
| GET | `/messages?conversationId=...` | `/api/v1/chat/messages` | `ChatMessageController` | query `conversationId` | `ApiResponse<List<ChatMessageResponse>>` |
| GET | `/messages/paginated` | `/api/v1/chat/messages/paginated` | `ChatMessageController` | query `conversationId`, `page`, `size` | `ApiResponse<PageResponse<ChatMessageResponse>>` |
| GET | `/messages/{id}` | `/api/v1/chat/messages/{id}` | `ChatMessageController` | none | `ApiResponse<ChatMessageResponse>` |
| PUT | `/messages/{id}` | `/api/v1/chat/messages/{id}` | `ChatMessageController` | `UpdateMessageRequest` | `ApiResponse<ChatMessageResponse>` |
| DELETE | `/messages/{id}` | `/api/v1/chat/messages/{id}` | `ChatMessageController` | none | `ApiResponse<Void>` |
| POST | `/messages/{id}/read` | `/api/v1/chat/messages/{id}/read` | `ChatMessageController` | none | `ApiResponse<ReadReceiptResponse>` |
| GET | `/messages/{id}/read-receipts` | `/api/v1/chat/messages/{id}/read-receipts` | `ChatMessageController` | none | `ApiResponse<List<ReadReceiptResponse>>` |
| GET | `/messages/unread-count` | `/api/v1/chat/messages/unread-count` | `ChatMessageController` | query `conversationId` | `ApiResponse<Long>` |

WebSocket/STOMP endpoints in `WebSocketController`:

- STOMP handshake endpoint: `/ws` with SockJS, registered in `chat-service/src/main/java/com/tien/chatservice/configuration/WebSocketConfig.java`.
- Application destination prefix: `/app`.
- Simple broker prefixes: `/topic`, `/queue`, `/user`.
- User destination prefix: `/user`.
- `@MessageMapping("/chat.sendMessage")` with `ChatMessageRequest`, publishes to `/topic/conversation/{conversationId}`.
- `@MessageMapping("/chat.typing")` with `TypingNotification`, publishes to `/topic/conversation/{conversationId}/typing`.
- `@MessageMapping("/chat.addUser")` with `ChatNotification`, publishes to `/topic/conversation/{conversationId}`.
- `@MessageMapping("/chat.removeUser")` with `ChatNotification`, publishes to `/topic/conversation/{conversationId}`.

Needs manual review: `WebSocketConfig` allows all origins with `setAllowedOriginPatterns("*")`; decide whether to keep that behavior or restrict it before production.

## 4. Database Entities, Tables, Collections

### MySQL/JPA modules

| Service | Config source | Entity | Table/collection | Important relationships |
|---|---|---|---|---|
| `identity-service` | `config-server/src/main/resources/config/identity-service.yaml`, DB `identity_service` | `User` | default JPA table `user` unless naming strategy changes it | `@ManyToMany(fetch = LAZY) Set<Role> roles`; unique `username`, `email` |
| `identity-service` | same | `Role` | default JPA table `role` | `@ManyToMany Set<Permission> permissions` |
| `identity-service` | same | `Permission` | default JPA table `permission` | used by `Role.permissions` |
| `identity-service` | same | `InvalidatedToken` | default JPA table `invalidated_token` or naming-strategy equivalent | JWT blacklist/revocation by token id |
| `identity-service` | same | `UserOtp` | `user_otp` | `@ManyToOne(fetch = LAZY)` to `User` via `user_id`; OTP type `OtpType` |
| `profile-service` | `config-server/src/main/resources/config/profile-service.yaml`, DB `profile_service` | `Profile` | `user_profile` | unique `userId`; no JPA relation to identity user |
| `social-service` | `config-server/src/main/resources/config/social-service.yaml`, DB `social_service` | `Friendship` | `friendships` | unique `(user_id, friend_id)`; status `FriendshipStatus` |
| `social-service` | same | `Follow` | `follows` | unique `(follower_id, following_id)` |
| `social-service` | same | `UserBlock` | `user_blocks` | unique `(blocker_id, blocked_id)`; block removes follows/friendships in service logic |
| `interaction-service` | `config-server/src/main/resources/config/interaction-service.yaml`, DB `interaction_service` | `Comment` | `comments` | references `postId`, `userId`, optional `parentCommentId`; no DB FK |
| `interaction-service` | same | `Like` | `likes` | references `userId`, optional `post_id`, optional `comment_id`; no DB FK |

### MongoDB modules

| Service | Config source | Entity/document | Collection | Important relationships |
|---|---|---|---|---|
| `post-service` | `config-server/src/main/resources/config/post-service.yaml`, Mongo DB `post-service` | `Post` | `post` | fields `userId`, `groupId`, `originalPostId`, `imageUrls`; cross-module references only |
| `post-service` | same | `SavedPost` | `saved_posts` | fields `userId`, `postId` |
| `post-service` | same | `SharedPost` | `shared_posts` | fields `userId`, `postId`, `originalPostUserId` |
| `group-service` | `config-server/src/main/resources/config/group-service.yaml`, Mongo DB `group-service` | `Group` | `group` | owner by `ownerId`; policy flags for posting/viewing |
| `group-service` | same | `GroupMember` | `group_member` | fields `groupId`, `userId`, `MemberRole` |
| `group-service` | same | `JoinRequest` | `join_request` | fields `groupId`, `userId`, `RequestStatus`, reviewed metadata |
| `file-service` | `config-server/src/main/resources/config/file-service.yaml`, Mongo DB `file-service` | `Image` | `file` | fields `ownerId`, `postId`, `ImageType`, `secureUrl`, `publicId`, `ImageVersions` |
| `notification-service` | `config-server/src/main/resources/config/notification-service.yaml`, Mongo DB `notification-service` | `Notification` | `notifications` | fields `userId`, type/title/content, related entity metadata, `isRead` |
| `chat-service` | `config-server/src/main/resources/config/chat-service.yaml`, Mongo DB `chat-service` | `Conversation` | `conversation` | embedded `List<ParticipantInfo>`; unique indexed `participantsHash` |
| `chat-service` | same | `ChatMessage` | `chat_message` | indexed `conversationId`, indexed `createdDate`, embedded sender `ParticipantInfo` |
| `chat-service` | same | `ReadReceipt` | `read_receipt` | indexed `messageId`, `conversationId`, `userId` |

Needs manual review: the first monolith can keep MySQL and MongoDB side-by-side. Do not merge Mongo documents into MySQL in this migration pass.

## 5. Inter-Service Communication And Integrations

### HTTP, Feign, WebClient

| Source | Client class | Target | Calls |
|---|---|---|---|
| `api-gateway` | `IdentityClient` | `identity-service` | `POST /auth/introspect` via WebClient HTTP interface |
| `identity-service` | `ProfileClient` | `profile-service` | `POST /internal/users` |
| `interaction-service` | `ProfileClient` | `profile-service` | `GET /internal/users/{userId}`, `GET /internal/users/batch` |
| `interaction-service` | `PostClient` | `post-service` | `GET /internal/posts/{postId}/exists` |
| `post-service` | `ProfileClient` | `profile-service` | `GET /internal/users/{userId}` |
| `post-service` | `SocialClient` | `social-service` | `GET /internal/blocks/ids`, `/internal/friend-ids`, `/internal/following-ids` |
| `post-service` | `InteractionClient` | `interaction-service` | like/comment count and is-liked internal endpoints |
| `post-service` | `GroupClient` | `group-service` | group exists, can-post, can-view, get group |
| `social-service` | `ProfileClient` | `profile-service` | `POST /users/search`, `GET /internal/users/{userId}` |
| `group-service` | `ProfileClient` | `profile-service` | `GET /internal/users/{userId}` |
| `chat-service` | `ProfileClient` | `profile-service` | `GET /internal/users/{userId}`, `GET /internal/users/batch` |
| `notification-service` | `EmailClient` | Brevo external API | `POST https://api.brevo.com/v3/smtp/email` |

Migration rule: replace service-to-service HTTP clients with direct module ports, for example `PostModule` depends on `ProfileQueryPort`, `SocialGraphQueryPort`, `InteractionQueryPort`, and `GroupAccessPort`, not Feign clients.

### Kafka

| Flow | Producer | Consumer | Topic | Current behavior | Monolith strategy |
|---|---|---|---|---|---|
| Notification delivery | `identity-service/src/main/java/com/tien/identityservice/service/NotificationService.java` | `notification-service/src/main/java/com/tien/notificationservice/controller/NotificationController.java` | `notification-delivery` | Identity sends email events; notification service sends Brevo email and may save notification if `param.userId` exists | Replace with direct `NotificationApplicationService.sendEmail(...)` or `NotificationApplicationService.deliver(NotificationEvent)` call. Add `@Async` or outbox only if reliability requirements need it. |
| Image upload request/reply | `profile-service`, `post-service`, `group-service` `ImageUploadKafkaService` classes | `file-service/src/main/java/com/tien/fileservice/listener/ImageUploadListener.java`; replies consumed by each caller | `image.upload`, `image.uploaded` from `ImageTopics` | Synchronous request/reply over Kafka with `CompletableFuture` and 30-second timeout | Replace with direct `FileApplicationService.uploadImage(...)` and `uploadImages(...)`. Preserve validation and return `ImageUploadedEvent` or URL list. |
| Post deletion cleanup | Producer not found in repo | `interaction-service/src/main/java/com/tien/interactionservice/listener/PostEventListener.java` | `post.events` | Listener deletes comments and likes when event type is `DELETED` | Needs manual review because no producer was found. In monolith, call `InteractionCleanupPort.deleteByPostId(postId)` directly from post delete transaction/workflow. |
| User event | Producer not found in repo | `interaction-service/src/main/java/com/tien/interactionservice/listener/UserEventListener.java` | `user.events` | Listener logs `CREATED`; no domain write | Remove unless an external producer exists. Needs manual review. |
| Like events | `LikeService.publishLikeEvent(...)` | no consumer found in repo | `like.events` | Produced on create/delete | Remove Kafka publishing unless external consumers exist. Needs manual review. |
| Comment events | `CommentService.publishCommentEvent(...)` | no consumer found in repo | `comment.events` | Produced on create/update/delete | Remove Kafka publishing unless external consumers exist. Needs manual review. |
| Notification events | constant only in `notification-service/NotificationService.java` | no producer call found | `notification.events` | `KafkaTemplate` injected but no send found | Remove after confirming not planned for external delivery. |

Kafka dependencies/configs found in poms/config for `identity-service`, `profile-service`, `post-service`, `interaction-service`, `file-service`, `group-service`, `social-service`, and `notification-service`. `social-service` has Kafka config/dependency but no Kafka Java usage found. No Kafka usage was found in `chat-service`.

### Redis

No real Redis usage was found by repository search for Redis classes/config (`RedisTemplate`, `StringRedisTemplate`, `spring.redis`, `spring.data.redis`, cache annotations). Do not add Redis during migration unless a new explicit requirement appears.

### External integrations

- Cloudinary: `file-service/src/main/java/com/tien/fileservice/configuration/CloudinaryConfig.java` and `ImageService`; properties in `config-server/src/main/resources/config/file-service.yaml` under `cloudinary.cloud-name`, `api-key`, `api-secret`.
- Brevo email API: `notification-service/src/main/java/com/tien/notificationservice/repository/httpclient/EmailClient.java`, `EmailService`; properties in `config-server/src/main/resources/config/notification-service.yaml` under `notification.email.brevo-url` and `brevo-apikey`.
- Google OAuth2: `identity-service` config in `config-server/src/main/resources/config/identity-service.yaml`, handlers in `OAuth2AuthenticationSuccessHandler` and `OAuth2AuthenticationFailureHandler`.

## 6. Shared Code To Move Into `com.friendify.app.shared`

Move shared contracts/utilities before domain migration:

- API wrappers:
  - duplicate `ApiResponse` classes in most services, for example `identity-service/src/main/java/com/tien/identityservice/dto/ApiResponse.java`, `post-service/.../dto/ApiResponse.java`, `chat-service/.../dto/ApiResponse.java`.
  - duplicate `PageResponse` classes in `post-service`, `interaction-service`, `social-service`, `group-service`, `chat-service`, `notification-service`.
- Exception model:
  - `AppException`, `ErrorCode`, `GlobalExceptionHandler` exist per service. Unify the handler shape while keeping module-specific error codes if needed.
- Security/OpenAPI:
  - Repeated `SecurityConfig`, `CustomJwtDecoder`, `JwtAuthenticationEntryPoint`, `OpenApiConfig`, and Feign auth interceptors across services.
  - Keep a single JWT resource server config in monolith and module-level authorization rules.
- Media contracts/utilities:
  - `shared-contacts/src/main/java/com/tien/sharedcontacts/media/ImageUploadEvent.java`
  - `ImageUploadedEvent.java`, `MultipleImageResponse.java`, `ImageResponse.java`, `ImageDeleteEvent.java`, `ImageTopics.java`
  - `shared-contacts/src/main/java/com/tien/sharedcontacts/media/entity/ImageType.java`
  - `shared-common/src/main/java/com/tien/sharedcommon/converter/MediaConverter.java`
- Notification contract:
  - Duplicate `NotificationEvent` under `identity-service/src/main/java/com/tien/event/dto/NotificationEvent.java` and `notification-service/src/main/java/com/tien/event/dto/NotificationEvent.java`; move one canonical contract to shared.
- Profile summary DTOs:
  - Similar `ProfileResponse`/`UserProfileResponse` DTOs exist in post, social, group, chat, interaction, identity. Prefer a shared read model such as `shared.profile.ProfileSummary`.
- Common constants/enums:
  - Consider moving only truly shared enums such as `ImageType`. Keep domain-specific enums (`FriendshipStatus`, `GroupPrivacy`, `MemberRole`, `PrivacyType`) in their modules unless used across module boundaries.

## 7. Recommended Modular Monolith Structure

Use the existing monolith root:

```text
monolith/src/main/java/com/friendify/app
  MonolithApplication.java
  /auth
  /profile
  /post
  /interaction
  /social
  /group
  /chat
  /file
  /notification
  /shared
  /config
```

Recommended module contents:

- `auth`: identity controllers/services/entities/repositories, JWT, OAuth2, OTP, roles/permissions.
- `profile`: profile controllers/services/entities/repositories and profile read ports.
- `file`: Cloudinary configuration, image upload service, image metadata document/repository, media DTOs if not fully shared.
- `social`: friendship/follow/block domain and social graph query ports.
- `group`: group membership/access checks and group documents.
- `interaction`: likes/comments and cleanup port.
- `post`: post/feed/saved/shared post domain and Mongo repositories.
- `notification`: notification documents, Brevo email client, email/notification application service.
- `chat`: conversation/message/read receipt/WebSocket domain.
- `shared`: `ApiResponse`, `PageResponse`, canonical exception base types, current-user helper, media contracts, notification event contract, shared DTOs.
- `config`: security, OpenAPI, persistence, WebSocket, Cloudinary, external clients.

Allowed dependencies:

- Controllers depend only on their module application services and shared response/security utilities.
- Domain modules may call other modules only through explicit ports/interfaces, for example:
  - `auth -> profile` via `ProfileCreationPort`
  - `auth -> notification` via `NotificationDeliveryPort`
  - `post -> profile/social/interaction/group/file` via query/upload ports
  - `interaction -> post/profile` via query ports
  - `group -> profile/file` via query/upload ports
  - `chat -> profile` via profile query port
- `shared` must not depend on domain modules.

Dependencies to remove:

- Spring Cloud Gateway from the target runtime after API parity is verified.
- Spring Cloud Config client/server after monolith config is local/environment-driven.
- OpenFeign for internal calls after ports are wired directly. Keep Feign only for true external clients if useful; Brevo can remain Feign or become a Spring HTTP interface.
- Spring Kafka after all flows above are replaced or confirmed unnecessary.

## 8. Kafka Migration Strategy

1. Replace image upload request/reply first:
   - Move `ImageService` to `com.friendify.app.file`.
   - Add a direct upload port, for example `FileUploadPort.uploadImage(MultipartFile file, ImageType type, String ownerId, String postId)`.
   - Update profile/post/group migration slices to call that port directly instead of `ImageUploadKafkaService`.
   - Remove `ImageUploadListener`, `ImageUploadKafkaService` classes, `ImageTopics` usage, and Kafka config only after tests pass.

2. Replace notification delivery:
   - Move canonical `NotificationEvent` to shared.
   - Convert identity notification sends to direct `NotificationDeliveryPort.deliver(NotificationEvent)` or `sendEmail(recipient, subject, body)`.
   - If email delivery must not block registration/password reset, use Spring `@Async` or a local outbox. This is a reliability decision and needs manual review.

3. Replace post deletion cleanup:
   - In `PostService.deletePost`, call an `InteractionCleanupPort.deleteByPostId(postId)` after confirming expected transaction/failure behavior.
   - Current `PostEventListener` already deletes comments and likes; preserve that behavior.
   - Needs manual review because no `post.events` producer was found.

4. Remove dead Kafka flows:
   - `user.events`: listener logs only, no write. Remove if no external producer.
   - `like.events` and `comment.events`: producers found, no consumers found. Remove if no external consumers.
   - `notification.events`: unused constant/injected template in notification service. Remove.
   - `social-service` Kafka dependency/config: no Java usage found. Remove during social module migration.

5. Remove Kafka dependencies/config after all modules build:
   - `spring-kafka` from migrated monolith dependencies if no remaining external Kafka integration exists.
   - `spring.kafka.*` blocks from migrated configuration.
   - Shared `ImageTopics` can be deleted after no code references topics.

## 9. Safe Step-By-Step Migration Order

Each step should end with `cd monolith && mvn test` or `mvn verify` once the monolith has the required dependencies.

1. [x] Stabilize monolith shell. DONE
   - Review version conflict: current repo uses Java 17/Spring Boot 3.5.5, `AGENTS.md` says Java 21/Spring Boot 4.0.6. Choose explicitly before editing `monolith/pom.xml`.
   - Add required Spring starters only for the first slice.
   - Preserve root package `com.friendify.app`.
   - Checkpoint: `MonolithApplicationTests.contextLoads`.

   Completed details:
   - Updated `monolith/pom.xml` to Spring Boot `4.0.6` and Java `21`.
   - Kept package root `com.friendify.app`.
   - Kept dependencies minimal: `spring-boot-starter` and `spring-boot-starter-test`.
   - Did not add Kafka or Redis.
   - Did not migrate business code.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`.

2. [x] Move shared code. DONE
   - Add `com.friendify.app.shared.dto.ApiResponse`, `PageResponse`.
   - Add shared media contracts from `shared-contacts` and `MediaConverter` from `shared-common`.
   - Add canonical `NotificationEvent`.
   - Add shared exception base and security helpers.
   - Checkpoint: context loads and shared unit tests for media conversion.

   Completed details:
   - Added `monolith/src/main/java/com/friendify/app/shared/dto/ApiResponse.java`.
   - Added `monolith/src/main/java/com/friendify/app/shared/dto/PageResponse.java`.
   - Added shared exception classes under `monolith/src/main/java/com/friendify/app/shared/exception/`:
     `AppException.java`, `ErrorCode.java`, `GlobalExceptionHandler.java`.
   - Added media contracts/util under `monolith/src/main/java/com/friendify/app/shared/media/`:
     `ImageDeleteEvent.java`, `ImageResponse.java`, `ImageType.java`,
     `ImageUploadedEvent.java`, `ImageUploadEvent.java`, `MediaConverter.java`,
     `MultipleImageResponse.java`.
   - Added canonical notification event:
     `monolith/src/main/java/com/friendify/app/shared/notification/NotificationEvent.java`.
   - Added focused test:
     `monolith/src/test/java/com/friendify/app/shared/media/MediaConverterTests.java`.
   - Added minimal `spring-web` dependency to `monolith/pom.xml` for `MultipartFile`
     and shared web exception types.
   - Did not migrate business modules, controllers, services, entities, Kafka, or Redis.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`.

3. [x] DONE: Migrate `auth` and `profile` together, but split the work into small sub-steps.
   - 3a. [x] DONE: Move only shared auth/profile DTOs, entities, repositories, mappers, and config needed for a context load.
     Completed details:
     - Added auth foundation packages under `monolith/src/main/java/com/friendify/app/auth/`:
       `dto/request`, `dto/response`, `entity`, `enums`, `mapper`, `repository`, and `validation`.
     - Added auth entities: `User`, `Role`, `Permission`, `InvalidatedToken`, `UserOtp`.
     - Added auth repositories: `UserRepository`, `RoleRepository`, `PermissionRepository`,
       `InvalidatedTokenRepository`, `UserOtpRepository`.
     - Added auth DTOs for user/auth/token/OTP/password/role/permission contracts and
       enums/constants `OtpType`, `PredefinedRole`, `SignInProvider`.
     - Added auth mappers: `UserMapper`, `RoleMapper`, `PermissionMapper`.
     - Added profile foundation packages under `monolith/src/main/java/com/friendify/app/profile/`:
       `dto/request`, `dto/response`, `entity`, `mapper`, `repository`, and `port`.
     - Added profile entity/repository/mapper/DTOs: `Profile`, `ProfileRepository`,
       `ProfileMapper`, `ProfileCreationRequest`, `UpdateProfileRequest`,
       `SearchUserRequest`, `ProfileResponse`.
     - Added initial profile ports only as interfaces:
       `ProfileCreationPort`, `ProfileQueryPort`.
     - Updated `monolith/pom.xml` with minimal foundation dependencies:
       Spring Data JPA, Bean Validation, MySQL runtime driver, Lombok, MapStruct,
       and annotation processors for Lombok/MapStruct.
     - Updated `monolith/src/main/resources/application.properties` with temporary
       Spring Boot 4 datasource/JPA auto-configuration exclusions so the shell can
       context-load before database configuration is migrated.
     - Did not add Kafka, Redis, Feign, internal HTTP clients, controllers, services,
       avatar/background upload, `ImageUploadKafkaService`, OTP/email behavior, or
       auth/profile business flows.
     - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
       `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`.
   - 3b. [x] DONE: Move profile core service/query behavior and internal profile creation/query ports: create profile, get profile, update profile text fields, search. Do not remove the legacy profile service yet because avatar/background upload still depends on the file module.
     Completed details:
     - Added profile service implementation:
       `monolith/src/main/java/com/friendify/app/profile/service/ProfileService.java`.
     - `ProfileService` implements `ProfileCreationPort` and `ProfileQueryPort` directly;
       no Feign, WebClient, RestTemplate, Kafka, Redis, or internal HTTP calls were added.
     - Completed profile port methods for direct module calls:
       `createProfile`, `getProfileById`, `getProfileByUserId`,
       `getCurrentUserProfile`, `getAllProfiles`, `getProfilesByUserIds`, and `search`.
     - Added public non-media profile controller:
       `monolith/src/main/java/com/friendify/app/profile/controller/ProfileController.java`.
     - Migrated profile endpoints:
       `GET /api/v1/profile/users/{profileId}`,
       `GET /api/v1/profile/users`,
       `GET /api/v1/profile/users/my-profile`,
       `PUT /api/v1/profile/users/my-profile`,
       `POST /api/v1/profile/users/search`.
     - Added temporary compatibility internal controller:
       `monolith/src/main/java/com/friendify/app/profile/controller/InternalProfileController.java`
       with `POST /internal/users`, `GET /internal/users/{userId}`, and
       `GET /internal/users/batch?userIds=...`.
     - Added small shared current-user helper:
       `monolith/src/main/java/com/friendify/app/shared/security/CurrentUserProvider.java`.
     - Updated shared `ErrorCode` with `USER_NOT_EXISTED` for profile lookup failures.
     - Updated `monolith/pom.xml`: replaced raw `spring-web` with
       `spring-boot-starter-web`, added `spring-security-core` for the current-user helper,
       and added test-scoped H2 for context-load tests without a local MySQL dependency.
     - Updated configuration:
       `monolith/src/main/resources/application.properties` now has MySQL datasource
       placeholders for the profile schema; `monolith/src/test/resources/application.properties`
       uses H2 in MySQL mode for tests.
     - Added profile service tests:
       `monolith/src/test/java/com/friendify/app/profile/service/ProfileServiceTests.java`.
     - Deferred media endpoints to Step 4 and did not expose or stub:
       `PUT /api/v1/profile/users/avatar`,
       `PUT /api/v1/profile/users/background`.
     - Needs manual review: the old `ProfileController` route variable was named
       `profileId` but delegated to a service method that looked up by `userId`.
       Step 3b follows the migration plan/task wording: public
       `/api/v1/profile/users/{profileId}` resolves by profile id, while
       `/internal/users/{userId}` resolves by user id.
     - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
       `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`.
   - 3c. [x] DONE: Move identity registration/login/token behavior and replace `identity-service` Feign `ProfileClient.createProfile` with `ProfileCreationPort`.
     Completed details:
     - Added auth services under `monolith/src/main/java/com/friendify/app/auth/service/`:
       `AuthenticationService`, `UserService`, `JwtService`, `OtpService`,
       `RoleService`, and `PermissionService`.
     - Added auth controllers under `monolith/src/main/java/com/friendify/app/auth/controller/`:
       `AuthenticationController`, `UserController`, `RoleController`, and
       `PermissionController`.
     - Migrated auth endpoints:
       `POST /api/v1/identity/auth/registration`,
       `POST /api/v1/identity/auth/verify-user`,
       `POST /api/v1/identity/auth/resend-verification`,
       `POST /api/v1/identity/auth/token`,
       `POST /api/v1/identity/auth/introspect`,
       `POST /api/v1/identity/auth/refresh`,
       `POST /api/v1/identity/auth/logout`,
       `POST /api/v1/identity/auth/forgot-password`,
       `POST /api/v1/identity/auth/reset-password`,
       `GET /api/v1/identity/users`,
       `GET /api/v1/identity/users/{userId}`,
       `GET /api/v1/identity/users/myInfo`,
       `PUT /api/v1/identity/users/{userId}`,
       `DELETE /api/v1/identity/users/{userId}`,
       `PUT /api/v1/identity/users/change-password`,
       `POST /api/v1/identity/roles`,
       `GET /api/v1/identity/roles`,
       `DELETE /api/v1/identity/roles/{role}`,
       `POST /api/v1/identity/permissions`,
       `GET /api/v1/identity/permissions`,
       `DELETE /api/v1/identity/permissions/{permission}`.
     - Registration now saves `User`, calls `ProfileCreationPort.createProfile(...)`
       directly, creates OTP, then calls `NotificationDeliveryPort.sendEmail(...)`.
       No `ProfileClient`, Feign, WebClient, RestTemplate, Kafka, Redis, or internal
       profile HTTP call was added.
     - Added auth notification port:
       `monolith/src/main/java/com/friendify/app/auth/port/NotificationDeliveryPort.java`.
     - Added temporary notification bridge:
       `monolith/src/main/java/com/friendify/app/auth/adapter/ExistingNotificationServiceEmailAdapter.java`.
       It calls the existing notification-service `POST /email/send` endpoint only when
       `friendify.auth.notification-service-url` is configured. If not configured, email
       flows fail with `NOTIFICATION_DELIVERY_NOT_CONFIGURED`; they do not log-and-succeed
       or silently no-op.
     - Added non-OAuth2 security/JWT classes:
       `SecurityConfig`, `CustomJwtDecoder`, and `JwtAuthenticationEntryPoint`.
       OAuth2 login handlers remain deferred to Step 3d.
     - Added small HTTP client config:
       `monolith/src/main/java/com/friendify/app/config/HttpClientConfig.java`
       for `RestClient.Builder` used by the temporary external notification bridge.
     - Updated `monolith/pom.xml` with `spring-boot-starter-security` and
       `spring-boot-starter-oauth2-resource-server`. `nimbus-jose-jwt` is supplied
       transitively by the resource-server stack.
     - Updated `monolith/src/main/resources/application.properties` with JWT settings,
       one monolith datasource placeholder, and
       `friendify.auth.notification-service-url`. Test properties use H2 and test JWT
       settings.
     - Added tests:
       `monolith/src/test/java/com/friendify/app/auth/service/AuthenticationServiceTests.java`
       verifies registration uses `ProfileCreationPort` and sends verification email via
       `NotificationDeliveryPort`;
       `monolith/src/test/java/com/friendify/app/auth/adapter/ExistingNotificationServiceEmailAdapterTests.java`
       verifies missing notification URL fails clearly instead of silently succeeding.
     - Needs manual review: Step 3c uses one monolith datasource placeholder
       `FRIENDIFY_DATASOURCE_URL` with default `friendify_monolith`. Existing service data
       currently lives in separate `identity_service` and `profile_service` schemas; a real
       data migration/schema decision is still required before production use.
     - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
       `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`.
   - 3d. [x] DONE: Move OAuth2 handlers and verify `/oauth2/**` and `/login/oauth2/**` behavior separately.
     Completed details:
     - Inspected OAuth2 implementation in `identity-service`:
       `OAuth2Service`, `OAuth2AuthenticationSuccessHandler`,
       `OAuth2AuthenticationFailureHandler`, `SecurityConfig`, and OAuth2 properties
       in `config-server/src/main/resources/config/identity-service.yaml`.
       No custom OAuth2 user service or provider-specific user-info class was found.
     - Added OAuth2 package:
       `monolith/src/main/java/com/friendify/app/auth/oauth2/`.
     - Migrated OAuth2 classes:
       `OAuth2Service`,
       `OAuth2AuthenticationSuccessHandler`,
       `OAuth2AuthenticationFailureHandler`.
     - Added compatibility controller:
       `OAuth2CompatibilityController`.
     - OAuth2 user creation now uses `ProfileCreationPort.createProfile(...)`
       directly when a Google user is first created. No profile Feign, WebClient,
       RestTemplate, Kafka, Redis, or internal profile HTTP call was added.
     - Updated `SecurityConfig` to wire `.oauth2Login(...)` with the migrated success
       and failure handlers. Public access is limited to auth public endpoints,
       OAuth2 framework/compatibility paths, internal profile compatibility paths,
       and Swagger paths; other endpoints remain authenticated.
     - Final OAuth2 paths:
       canonical Spring Security paths are
       `GET /oauth2/authorization/google` and
       `GET /login/oauth2/code/google`.
       gateway-compatible aliases are also supported:
       `GET /api/v1/identity/oauth2/authorization/google` redirects to the canonical
       authorization path, and
       `GET /api/v1/identity/login/oauth2/code/google` redirects to the canonical
       callback while preserving the query string.
     - Added OAuth2 config properties to `monolith/src/main/resources/application.properties`:
       `spring.security.oauth2.client.registration.google.client-id`,
       `spring.security.oauth2.client.registration.google.client-secret`,
       `spring.security.oauth2.client.registration.google.scope`,
       `spring.security.oauth2.client.registration.google.redirect-uri`,
       `spring.security.oauth2.client.registration.google.client-name`,
       Google provider authorization/token/user-info/user-name properties, and
       `app.oauth2.authorized-redirect-uri`.
       Later review updated the success redirect default from
       `http://localhost:5173/oauth2/redirect` to
       `http://localhost:5173/oauth2/success`.
     - OAuth2 success now sets the generated JWT in an HttpOnly cookie instead of
       appending it to the frontend redirect URL. Added cookie configuration:
       `app.oauth2.cookie-name`, `app.oauth2.cookie-secure`,
       `app.oauth2.cookie-same-site`, and `app.oauth2.cookie-path`, backed by
       `FRIENDIFY_OAUTH2_COOKIE_*` environment variables. For production HTTPS,
       set `FRIENDIFY_OAUTH2_COOKIE_SECURE=true`.
     - Updated `SecurityConfig` with a custom `BearerTokenResolver` so authenticated
       requests can still use the normal `Authorization: Bearer ...` header, and can
       also authenticate from the OAuth2 HttpOnly access-token cookie when no bearer
       header is present.
       Test properties provide non-secret placeholder values.
     - Added `spring-boot-starter-oauth2-client` to `monolith/pom.xml`.
       Kafka, Redis, Feign, gateway, and config-server dependencies were not added.
     - Added tests:
       `monolith/src/test/java/com/friendify/app/auth/oauth2/OAuth2ServiceTests.java`
       verifies OAuth2 new-user creation uses `ProfileCreationPort`;
       `monolith/src/test/java/com/friendify/app/auth/oauth2/OAuth2AuthenticationSuccessHandlerTests.java`
       verifies OAuth2 success creates/loads user, generates JWT, sets an HttpOnly
       access-token cookie, and redirects without a JWT query parameter.
       `monolith/src/test/java/com/friendify/app/auth/configuration/SecurityConfigTests.java`
       verifies bearer-token resolution prefers the header and falls back to the
       OAuth2 cookie.
     - Needs manual review: production Google `GOOGLE_CLIENT_ID`,
       `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI`, and
       `FRIENDIFY_OAUTH2_AUTHORIZED_REDIRECT_URI` must be configured with real values.
       Defaults are placeholders only and are not valid production credentials.
     - Verification after cookie-based OAuth2 redirect update:
       `cd monolith && mvn test` passed with `BUILD SUCCESS`;
       `Tests run: 16, Failures: 0, Errors: 0, Skipped: 0`.
   - 3e. [x] DONE: Do not stub away real OTP/verification email behavior. Either add a temporary `NotificationDeliveryPort` adapter that still reaches the existing notification path, or migrate the minimal Brevo email sender into `notification` before enabling monolith auth flows.
     Completed details:
     - Inspected current `notification-service` email implementation:
       `EmailService`, `EmailClient`, `EmailController`, `SendEmailRequest`,
       `EmailRequest`, `Recipient`, `Sender`, `EmailResponse`, and config keys
       `notification.email.brevo-url` / `notification.email.brevo-apikey`.
     - Added minimal monolith email package:
       `monolith/src/main/java/com/friendify/app/notification/email/`.
     - Added email classes:
       `EmailDeliveryService`, `BrevoEmailClient`, `RestClientBrevoEmailClient`,
       `AuthEmailNotificationAdapter`, and DTO records under
       `monolith/src/main/java/com/friendify/app/notification/email/dto/`.
     - Replaced the temporary Step 3c bridge
       `ExistingNotificationServiceEmailAdapter` with
       `AuthEmailNotificationAdapter`, which implements
       `NotificationDeliveryPort` and sends through the monolith Brevo email service.
     - Auth flows still call only `NotificationDeliveryPort`; registration
       verification, resend verification, forgot password, and welcome email now go
       through `EmailDeliveryService` and `RestClientBrevoEmailClient`.
     - If `notification.email.brevo-apikey` / `BREVO_API_KEY` is missing, email
       sending fails fast with `NOTIFICATION_DELIVERY_NOT_CONFIGURED`. If Brevo
       returns an HTTP/client error, sending fails with `NOTIFICATION_DELIVERY_FAILED`.
       There is no no-op/log-only success path.
     - Added Brevo config to `monolith/src/main/resources/application.properties`:
       `notification.email.brevo-url`,
       `notification.email.brevo-apikey`,
       `notification.email.sender-name`,
       `notification.email.sender-email`.
       Test properties keep the API key blank to verify fail-fast behavior.
     - No new dependencies were required; the implementation uses the existing
       Spring `RestClient` from `spring-boot-starter-web`.
     - Did not add Kafka, Redis, Feign, gateway, config-server, or notification
       service-to-service HTTP calls.
     - Full notification module remains deferred to Step 9. Not migrated here:
       `UserNotificationController`, notification CRUD/read/unread APIs,
       `NotificationRepository`, `Notification` document/entity, or Kafka
       `NotificationController` listener.
     - Added tests:
       `monolith/src/test/java/com/friendify/app/notification/email/EmailDeliveryServiceTests.java`
       verifies fail-fast missing API key and Brevo request mapping;
       `monolith/src/test/java/com/friendify/app/notification/email/NotificationDeliveryPortBeanTests.java`
       verifies `NotificationDeliveryPort` is backed by `AuthEmailNotificationAdapter`.
     - Needs manual review: production must set `BREVO_API_KEY` and should verify
       `BREVO_SENDER_EMAIL` is an approved Brevo sender.
     - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
       `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`.
   - Phase 3 completion review. [x] DONE.
     Completed details:
     - Verified auth/profile code uses `com.friendify.app.*` packages only. No old
       `com.tien.*` imports were found in `monolith/src/main/java` or tests.
     - Verified no Kafka, Redis, Feign, WebClient, RestTemplate, or internal profile
       HTTP calls were added for Phase 3 monolith communication.
     - Verified OAuth2 success no longer exposes JWT in a query string; it now sets
       `FRIENDIFY_ACCESS_TOKEN` as an HttpOnly cookie and redirects to the configured
       frontend success URL. `SecurityConfig` can resolve the token from that cookie
       on follow-up API requests.
     - Added default auth role seed config:
       `monolith/src/main/java/com/friendify/app/auth/configuration/ApplicationInitConfig.java`.
       It ensures `USER` and `ADMIN` roles exist so registration/OAuth2-created users
       can receive the default `USER` role when the DB is new.
     - Admin account seeding is available but disabled by default with
       `FRIENDIFY_SEED_ADMIN_ENABLED=false`; enable it only with explicit
       `FRIENDIFY_SEED_ADMIN_USERNAME`, `FRIENDIFY_SEED_ADMIN_PASSWORD`, and
       `FRIENDIFY_SEED_ADMIN_EMAIL`.
     - Added seed config properties to `monolith/src/main/resources/application.properties`,
       `monolith/.env`, and `monolith/.env.example`. Tests disable the application runner
       with `friendify.seed.enabled=false`.
     - Added tests:
       `monolith/src/test/java/com/friendify/app/auth/configuration/ApplicationInitConfigTests.java`
       verifies default role seeding without creating an admin by default;
       `monolith/src/test/java/com/friendify/app/auth/service/JwtServiceTests.java`
       verifies generated tokens validate and can be revoked.
     - Preserved `/api/v1/identity/**` and `/api/v1/profile/**` paths migrated in
       Phase 3. Profile avatar/background upload remains intentionally deferred to
       Step 4 with the file module.
     - Needs manual review: production should decide whether to enable admin seeding
       or create the first admin through a controlled DB/admin procedure.
     - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
       `Tests run: 16, Failures: 0, Errors: 0, Skipped: 0`.

4. [ ] Migrate `file` and remove image-upload Kafka request/reply.
   - Move Cloudinary config, `Image`, `ImageVersions`, `ImageRepository`, `ImageService`, `CloudMediaController`.
   - Expose direct upload port for profile/post/group.
   - Rewire migrated profile avatar/background endpoints from the temporary upload boundary to the direct file upload port.
   - Checkpoint tests: image upload service with mocked Cloudinary; profile avatar/background direct call.

5. [ ] Migrate `social`.
   - Move friendship/follow/block JPA entities/repositories/services/controllers.
   - Replace profile Feign calls with profile query port.
   - Remove unused Kafka config/dependency for this module.
   - Checkpoint tests: block/friend/follow queries used by post feed.

6. [ ] Migrate `group`.
   - Move group Mongo documents/repositories/services/controllers.
   - Replace profile Feign with profile query port.
   - Replace group avatar/cover Kafka upload with direct file upload port.
   - Checkpoint tests: can-view/can-post checks; join request workflows.

7. [ ] Migrate `interaction`.
   - Move comment/like JPA entities/repositories/services/controllers.
   - Replace post/profile Feign clients with ports.
   - Remove `like.events`/`comment.events` Kafka publishing after external consumer review.
   - Keep cleanup port for post deletion.
   - Checkpoint tests: comment/reply/like flows; count/is-liked; cleanup by post id.

8. [ ] Migrate `post`.
   - Move post Mongo documents/repositories/services/controllers.
   - Replace profile/social/interaction/group Feign with direct ports.
   - Replace post image upload Kafka with file upload port.
   - Wire post delete to interaction cleanup.
   - Checkpoint tests: feed visibility; group post permissions; post delete cleans comments/likes.

9. [ ] Migrate `notification`.
   - Move notification Mongo document/repository/service/controllers and Brevo client.
   - Replace `notification-delivery` Kafka listener with direct application service.
   - Decide sync vs async email delivery.
   - Checkpoint tests: email dispatch with mocked Brevo; notification persistence/read/unread count.

10. [ ] Migrate `chat`.
   - Move chat Mongo documents/repositories/services/controllers and WebSocket config.
   - Replace profile Feign with profile query port.
   - Preserve REST and STOMP paths.
   - Checkpoint tests: WebSocket auth, conversation membership validation, unread count.

11. [ ] Retire gateway/config-server.
   - Only after monolith endpoint parity is verified.
   - Keep compatibility routing inside monolith so clients still use `/api/v1/{domain}/...`.
   - Remove `api-gateway` and `config-server` from deployment, not from source, until a release rollback plan exists.

## 10. Risks And Manual Review Items

- Version conflict: checked build files use Java 17/Spring Boot 3.5.5, while `AGENTS.md` currently says Java 21/Spring Boot 4.0.6. Needs manual review before migration implementation.
- Config-server port/config risk: services import `optional:configserver:http://localhost:8888`, while `config-server/src/main/resources/application.yaml` sets `management.server.port: 8888` but no explicit `server.port`. Confirm how config-server is actually run before relying on its current runtime behavior.
- Gateway path compatibility: current services rely on servlet context paths and gateway `StripPrefix=2`. The monolith must expose `/api/v1/identity`, `/api/v1/profile`, `/api/v1/post`, etc. directly or via controller base mappings.
- Gateway public allowlist risk: `AuthenticationFilter` permits `/api/v1/file/media/download/.*`, but only commented-out file download code was found in `CloudMediaController`. Needs manual review before copying this allowlist.
- OAuth2 framework endpoint risk: `/oauth2/**` and `/login/oauth2/**` are not visible in controller scans but are part of Spring Security Google login. Preserve and test them if OAuth2 remains enabled.
- Profile media sequencing risk: `ProfileController.updateAvatar` and `updateBackgroundImage` call profile media logic that currently depends on `profile-service/src/main/java/com/tien/profileservice/service/ImageUploadKafkaService.java`. Migrate profile core first, then complete these media endpoints immediately after the file module is available.
- Security duplication: every service has its own `SecurityConfig`, `CustomJwtDecoder`, and `JwtAuthenticationEntryPoint`. Consolidation can accidentally open or block endpoints.
- Internal endpoints: `/internal/**` endpoints are currently used by Feign clients. In monolith they should become ports, but keep temporary compatibility endpoints only if external clients still call them.
- Data ownership: identity `User.id`, profile `Profile.userId`, social ids, post `userId`, group `ownerId/member.userId`, chat participant `userId`, and notification `userId` are string references without DB FKs. Cross-module consistency must be tested.
- Cross-store workflows: post deletion touches MongoDB posts and MySQL interactions. Decide whether direct cleanup is same transaction, best-effort, or async/outbox.
- Kafka external dependencies: no consumers/producers were found for several topics in this repo, but external consumers may exist. Needs manual review before deleting Kafka topics or deployments.
- File upload failure timing: Kafka request/reply currently waits up to 30 seconds. Direct Cloudinary calls fail immediately in the request path; preserve error mapping.
- Notification reliability: Kafka currently decouples identity from Brevo email. Direct calls may make registration/password reset depend on Brevo availability unless async/outbox is used.
- Auth email sequencing risk: registration verification, resend verification, forgot password, and reset password depend on email delivery. The auth slice is not safe to release with a no-op notification stub.
- OAuth2 redirect: `identity-service` uses `app.oauth2.authorizedRedirectUri` and Google OAuth config. Verify callback paths after monolith route changes.
- OAuth2 cookie security: OAuth2 success now uses an HttpOnly access-token cookie instead of a query token. Production must run HTTPS with `FRIENDIFY_OAUTH2_COOKIE_SECURE=true`, verify CORS credentials behavior with the frontend, and review CSRF protection before relying on cookie-authenticated state-changing APIs.
- Duplicate DTOs: `ProfileResponse`, `UserProfileResponse`, `ApiResponse`, `PageResponse`, and exception classes are duplicated with possibly different fields. Compare fields before unifying.
- Mongo collection names: collections use singular names such as `post`, `group`, `file`, `conversation`; preserve them during first migration.
- Tests are thin: current tests are mostly `contextLoads` classes. Add focused tests before deleting or disabling old services.

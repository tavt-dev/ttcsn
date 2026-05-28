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

Version inventory: legacy service `pom.xml` files use Spring Boot `3.5.5` and
`java.version` `17`. The monolith was explicitly upgraded during migration to
Java `21` and Spring Boot `4.0.6` to match the current `AGENTS.md` policy and
the local Java runtime.

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

Phase 12 update: monolith WebSocket origins are no longer hard-coded as `*`.
They are controlled by `FRIENDIFY_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`. Production
still needs manual review to set exact frontend origins.

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

Needs manual review: the original legacy services use MongoDB for post, group,
file, notification, and chat. The current monolith target has been clarified as
MySQL-only for migrated modules. Steps 4, 6, 8, 9, and 10 map file, group,
post, notification, and chat data to MySQL/JPA. Production needs data migration
scripts if existing MongoDB data must be preserved.

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
- `post`: post/feed/saved/shared post domain and JPA repositories.
- `notification`: notification entity/repository, Brevo email client, email/notification application service.
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
   - Version policy resolved for the monolith: use Java 21 and Spring Boot
     4.0.6 per current `AGENTS.md` and the local runtime. Legacy services stay
     unchanged for rollback.
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
     - Historical Step 3c note: this slice originally used a temporary
       external notification bridge so auth email flows would not silently
       succeed. That temporary bridge was removed in Step 3e/Step 9 and is not
       present in the final monolith path. Auth email now uses the notification
       module's `AuthEmailNotificationAdapter` and Brevo email service.
     - Added non-OAuth2 security/JWT classes:
       `SecurityConfig`, `CustomJwtDecoder`, and `JwtAuthenticationEntryPoint`.
       OAuth2 login handlers remain deferred to Step 3d.
     - Added small HTTP client config:
       `monolith/src/main/java/com/friendify/app/config/HttpClientConfig.java`
       for `RestClient.Builder`, now used only for the external Brevo email
       integration.
     - Updated `monolith/pom.xml` with `spring-boot-starter-security` and
       `spring-boot-starter-oauth2-resource-server`. `nimbus-jose-jwt` is supplied
       transitively by the resource-server stack.
     - Updated `monolith/src/main/resources/application.properties` with JWT
       settings and one monolith datasource placeholder. Test properties use H2
       and test JWT settings.
     - Added tests:
       `monolith/src/test/java/com/friendify/app/auth/service/AuthenticationServiceTests.java`
       verifies registration uses `ProfileCreationPort` and sends verification email via
       `NotificationDeliveryPort`.
       The old temporary notification bridge tests were removed after the real
       notification email adapter was introduced.
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
   - 3e. [x] DONE: Finalize real auth email delivery through the monolith Brevo email adapter.
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

4. [x] DONE: Migrate `file` and remove image-upload Kafka request/reply.
   - Move Cloudinary config, `Image`, `ImageVersions`, `ImageRepository`, `ImageService`, `CloudMediaController`.
   - Expose direct upload port for profile/post/group.
   - Rewire migrated profile avatar/background endpoints from the temporary upload boundary to the direct file upload port.
   - Checkpoint tests: image upload service with mocked Cloudinary; profile avatar/background direct call.
   Completed details:
   - Added file module packages under `monolith/src/main/java/com/friendify/app/file/`:
     `controller`, `service`, `repository`, `entity`, `dto`, `config`, `mapper`, and `port`.
   - Migrated file classes:
     `ImageService`, `ImageRepository`, `Image`, `ImageVersions`,
     `CloudinaryConfig`, `CloudMediaController`, `UploadResponse`,
     `ImageResponse`, and `ImageMapper`.
   - Adjusted file image metadata storage to MySQL/JPA for the monolith target:
     `Image` is now a JPA `@Entity` mapped to table `file`,
     `ImageVersions` is an `@Embeddable`, and `ImageRepository` extends
     `JpaRepository`. This intentionally differs from the legacy `file-service`,
     where `Image` was a MongoDB document.
   - Added direct upload port:
     `monolith/src/main/java/com/friendify/app/file/port/FileUploadPort.java`.
     Methods:
     `uploadImage(MultipartFile file, ImageType imageType, String ownerId, String postId)`
     and
     `uploadImages(List<MultipartFile> files, ImageType imageType, String ownerId, String postId)`.
     Both return file-module `UploadResponse` contracts to avoid duplicating response
     mapping in profile/post/group callers.
   - `ImageService` implements `FileUploadPort` directly. It still supports the JSON
     `ImageUploadEvent -> ImageUploadedEvent` API for compatibility, but no Kafka
     listener, `KafkaTemplate`, `ImageTopics`, or `spring-kafka` dependency was added.
   - Preserved file endpoints directly in the monolith:
     `POST /api/v1/file/images/upload`,
     `POST /api/v1/file/images/upload-form-data`,
     `POST /api/v1/file/images/upload-multiple-form-data`.
   - Rewired profile media endpoints:
     `PUT /api/v1/profile/users/avatar` and
     `PUT /api/v1/profile/users/background`.
     `ProfileService` now calls `FileUploadPort` directly with `ImageType.AVATAR`
     and `ImageType.BACKGROUND_IMAGE`; it does not call file through HTTP and does
     not use Kafka request/reply.
   - Added configuration properties:
     `spring.servlet.multipart.max-file-size`,
     `spring.servlet.multipart.max-request-size`,
     `cloudinary.cloud-name`,
     `cloudinary.api-key`,
     `cloudinary.api-secret`.
     `.env` / `.env.example` now include multipart limits and `CLOUDINARY_*`
     placeholders. Cloudinary config fails fast at startup with a clear error if
     required Cloudinary settings are missing.
   - Updated `monolith/pom.xml` with only required dependencies:
     `com.cloudinary:cloudinary-http44`. The existing JPA/MySQL dependencies are
     reused for file metadata. No MongoDB, Kafka, Redis, Feign, gateway, or
     config-server dependency was added.
   - Added file/profile tests:
     `CloudinaryConfigTests` verifies missing Cloudinary config fails clearly;
     `FileUploadPortBeanTests` verifies the port bean exists;
     `ImageServiceTests` verifies unsupported content types fail before upload;
     `ProfileServiceTests` verifies avatar/background use `FileUploadPort`.
   - Post/group image upload remains deferred:
     post image upload will be wired to `FileUploadPort` in Step 8;
     group avatar/cover upload will be wired to `FileUploadPort` in Step 6.
   - Needs manual review: tests do not perform a real Cloudinary upload. Production/local
     runtime must set real `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and
     `CLOUDINARY_API_SECRET`. Because file metadata was changed from MongoDB to
     MySQL, production migration must include a data migration for existing
     `file-service.file` documents if old uploaded-image metadata must be preserved.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 21, Failures: 0, Errors: 0, Skipped: 0`.

5. [x] DONE: Migrate `social`.
   - Move friendship/follow/block JPA entities/repositories/services/controllers.
   - Replace profile Feign calls with profile query port.
   - Remove unused Kafka config/dependency for this module.
   - Checkpoint tests: block/friend/follow queries used by post feed.
   Completed details:
   - Added social module packages under
     `monolith/src/main/java/com/friendify/app/social/`:
     `controller`, `service`, `repository`, `entity`, `dto`, `mapper`,
     `enums`, and `port`.
   - Migrated social JPA entities and enum:
     `Friendship`, `Follow`, `UserBlock`, and `FriendshipStatus`.
     Existing table names/constraints from `social-service` were preserved:
     `friendships`, `follows`, and `user_blocks`.
   - Migrated repositories:
     `FriendshipRepository`, `FollowRepository`, and `UserBlockRepository`.
   - Migrated response DTOs and mappers:
     `FriendshipResponse`, `FollowResponse`, `UserBlockResponse`,
     `FriendshipStatusResponse`, `SocialCountsResponse`,
     `UserSocialInfoResponse`, `FriendshipMapper`, `FollowMapper`,
     and `UserBlockMapper`.
   - Migrated services:
     `FriendshipService`, `FollowService`, and `UserBlockService`.
     `FriendshipService` now uses
     `com.friendify.app.profile.port.ProfileQueryPort` directly for profile
     search and profile lookup. No `ProfileClient`, Feign, WebClient,
     RestTemplate, or internal HTTP call was added.
   - Added direct social port for later post/feed migration:
     `monolith/src/main/java/com/friendify/app/social/port/SocialGraphQueryPort.java`.
     Implemented methods:
     `getFriendIds(currentUserId)`,
     `getFollowingIds(currentUserId)`,
     `getBlockedUserIds(currentUserId)`, and
     `isBlockedBetween(userId1, userId2)`.
     `UserBlockService` implements this port.
   - Preserved public social endpoints directly in the monolith:
     `POST /api/v1/social/friendships/{friendId}`,
     `POST /api/v1/social/friendships/{friendId}/accept`,
     `POST /api/v1/social/friendships/{friendId}/reject`,
     `DELETE /api/v1/social/friendships/{friendId}`,
     `GET /api/v1/social/friendships/friends`,
     `GET /api/v1/social/friendships/sent-requests`,
     `GET /api/v1/social/friendships/received-requests`,
     `GET /api/v1/social/friendships/search`,
     `GET /api/v1/social/friendships/status/{friendId}`,
     `GET /api/v1/social/friendships/mutual/{friendId}`,
     `GET /api/v1/social/friendships/suggested`,
     `GET /api/v1/social/friendships/pending-requests/count`,
     `DELETE /api/v1/social/friendships/{friendId}/cancel`,
     `GET /api/v1/social/friendships/sent-requests/count`,
     `POST /api/v1/social/friendships/batch-status`,
     `GET /api/v1/social/friendships/counts`,
     `POST /api/v1/social/follows/{followingId}`,
     `DELETE /api/v1/social/follows/{followingId}`,
     `GET /api/v1/social/follows/following/{userId}`,
     `GET /api/v1/social/follows/followers/{userId}`,
     `GET /api/v1/social/follows/info/{userId}`,
     `POST /api/v1/social/blocks/{blockedId}`,
     `DELETE /api/v1/social/blocks/{blockedId}`,
     `GET /api/v1/social/blocks`,
     and `GET /api/v1/social/blocks/check/{blockedId}`.
   - Preserved temporary internal compatibility endpoints:
     `GET /internal/friend-ids`,
     `GET /internal/following-ids`,
     and `GET /internal/blocks/ids`.
     Later monolith modules should use `SocialGraphQueryPort` instead of
     these internal endpoints.
   - Added social error codes to shared exception handling:
     `INVALID_KEYWORD`, `FOLLOW_ALREADY_EXISTS`, `FOLLOW_NOT_FOUND`,
     `CANNOT_FOLLOW_SELF`, `FRIENDSHIP_ALREADY_EXISTS`,
     `FRIENDSHIP_NOT_FOUND`, `CANNOT_FRIEND_SELF`,
     `FRIEND_REQUEST_ALREADY_SENT`, `FRIEND_REQUEST_NOT_PENDING`,
     `USER_ALREADY_BLOCKED`, `USER_NOT_BLOCKED`, and `CANNOT_BLOCK_SELF`.
   - No dependency changes were required. Existing JPA, validation, MySQL,
     Lombok, and MapStruct dependencies were reused. No Kafka, Redis, Feign,
     gateway, config-server, or social-service Kafka config was added.
   - Added tests:
     `monolith/src/test/java/com/friendify/app/social/port/SocialGraphQueryPortBeanTests.java`
     verifies the direct social query port bean exists;
     `monolith/src/test/java/com/friendify/app/social/service/FriendshipServiceTests.java`
     verifies friend search uses `ProfileQueryPort` and filters blocked/current
     users, and verifies self-friend requests fail before repository writes.
   - Needs manual review: legacy `social-service` used its own MySQL schema
     (`social_service`). The monolith currently uses one datasource, so
     production must decide whether to migrate these tables into the monolith
     schema or point the monolith datasource at an existing combined schema.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 24, Failures: 0, Errors: 0, Skipped: 0`.

6. [x] DONE: Migrate `group`.
   - Move group persistence/repositories/services/controllers into MySQL/JPA for the monolith.
   - Replace profile Feign with profile query port.
   - Replace group avatar/cover Kafka upload with direct file upload port.
   - Checkpoint tests: can-view/can-post checks; join request workflows.
   Completed details:
   - Added group module packages under
     `monolith/src/main/java/com/friendify/app/group/`:
     `controller`, `service`, `repository`, `entity`, `dto`, `mapper`,
     `enums`, and `port`.
   - Migrated group persistence as MySQL/JPA in the monolith per the explicit
     MySQL-only decision for this migration slice. This intentionally differs
     from the legacy `group-service`, where `Group`, `GroupMember`, and
     `JoinRequest` were Mongo documents.
   - Added JPA entities:
     `Group`, `GroupMember`, and `JoinRequest`.
     Table mappings:
     quoted table ``group`` for `Group`,
     `group_member` for `GroupMember`,
     and `join_request` for `JoinRequest`.
     `GroupMember` and `JoinRequest` preserve unique `(group_id, user_id)`
     constraints for membership/request identity.
   - Added repositories:
     `GroupRepository`, `GroupMemberRepository`, and `JoinRequestRepository`,
     all extending JPA repositories. No Mongo repository or Mongo dependency was
     added.
   - Added enums:
     `GroupPrivacy`, `MemberRole`, and `RequestStatus`.
   - Added request/response DTOs:
     `CreateGroupRequest`, `UpdateGroupRequest`, `JoinGroupRequest`,
     `ProcessJoinRequest`, `UpdateMemberRoleRequest`, `GroupResponse`,
     `GroupMemberResponse`, `JoinRequestResponse`, and `MemberRoleResponse`.
   - Added mappers and formatter:
     `GroupMapper`, `GroupMemberMapper`, `JoinRequestMapper`, and
     `DateTimeFormatter`.
   - Migrated `GroupService` behavior for group CRUD, membership management,
     join/leave/request workflows, group search/listing, member listing,
     and group access checks.
   - `GroupService` now injects
     `com.friendify.app.profile.port.ProfileQueryPort` for owner/member/join
     request profile display data. It does not use `ProfileClient`, Feign,
     WebClient, RestTemplate, or internal HTTP calls.
   - Group avatar/cover upload now injects
     `com.friendify.app.file.port.FileUploadPort` directly and calls it with
     `ImageType.GROUP_AVATAR` and `ImageType.GROUP_COVER`.
     `ImageUploadKafkaService`, `@KafkaListener`, `KafkaTemplate`, and
     `ImageTopics` were not migrated.
   - Added direct group access port for later post migration:
     `monolith/src/main/java/com/friendify/app/group/port/GroupAccessPort.java`.
     Implemented methods:
     `exists(groupId)`,
     `canPost(groupId, userId)`,
     `canView(groupId, userId)`, and
     `getGroup(groupId)`.
     `GroupService` implements this port.
   - Preserved public group endpoints directly in the monolith:
     `POST /api/v1/group/groups`,
     `PUT /api/v1/group/groups/{groupId}`,
     `DELETE /api/v1/group/groups/{groupId}`,
     `GET /api/v1/group/groups`,
     `GET /api/v1/group/groups/{groupId}`,
     `PUT /api/v1/group/groups/{groupId}/avatar`,
     `PUT /api/v1/group/groups/{groupId}/cover`,
     `POST /api/v1/group/groups/{groupId}/members/{userId}`,
     `DELETE /api/v1/group/groups/{groupId}/members/{userId}`,
     `PUT /api/v1/group/groups/{groupId}/members/{userId}/role`,
     `GET /api/v1/group/groups/{groupId}/members`,
     `POST /api/v1/group/groups/{groupId}/join`,
     `POST /api/v1/group/groups/{groupId}/leave`,
     `POST /api/v1/group/groups/{groupId}/join-requests/{requestId}/process`,
     `GET /api/v1/group/groups/{groupId}/join-requests`,
     `DELETE /api/v1/group/groups/{groupId}/join-requests/{requestId}`,
     `GET /api/v1/group/groups/my-join-requests`,
     `GET /api/v1/group/groups/my-groups`,
     `GET /api/v1/group/groups/joined-groups`,
     and `GET /api/v1/group/groups/search`.
   - Preserved temporary internal compatibility endpoints:
     `GET /internal/groups/{groupId}/exists`,
     `GET /internal/groups/{groupId}`,
     `GET /internal/groups/{groupId}/can-post`,
     `GET /internal/groups/{groupId}/can-view`,
     and `GET /internal/groups/{groupId}/can-view/{userId}`.
     Later monolith modules should use `GroupAccessPort` instead of calling
     these internal endpoints.
   - Added group error codes to shared exception handling:
     `GROUP_NOT_FOUND`, `GROUP_ALREADY_EXISTS`, `GROUP_NOT_OWNER`,
     `GROUP_NAME_REQUIRED`, `MEMBER_NOT_FOUND`, `MEMBER_ALREADY_EXISTS`,
     `MEMBER_CANNOT_REMOVE_OWNER`, `INVALID_ROLE`,
     `CANNOT_CHANGE_OWNER_ROLE`, `JOIN_REQUEST_NOT_FOUND`,
     `JOIN_REQUEST_ALREADY_EXISTS`, `ALREADY_MEMBER`,
     `INSUFFICIENT_PERMISSION`, `CANNOT_JOIN_GROUP`, and
     `POSTING_NOT_ALLOWED`.
   - No dependency changes were required for this step. Existing JPA,
     validation, MySQL, Lombok, MapStruct, web, and file upload dependencies
     were reused. No MongoDB, Kafka, Redis, Feign, gateway, or config-server
     dependency was added.
   - Added tests:
     `monolith/src/test/java/com/friendify/app/group/port/GroupAccessPortBeanTests.java`
     verifies the direct group access port bean exists;
     `monolith/src/test/java/com/friendify/app/group/service/GroupServiceTests.java`
     verifies `GroupService` implements `GroupAccessPort`, group avatar upload
     uses `FileUploadPort`, and `canPost(groupId, userId)` allows moderators
     when only admins/moderators can post.
   - Needs manual review: because this step intentionally maps legacy Mongo
     group documents to MySQL/JPA, production must include a data migration for
     existing `group-service` collections (`group`, `group_member`,
     `join_request`) if old group data must be preserved. The quoted MySQL
     table ``group`` should also be reviewed before production because `GROUP`
     is a SQL keyword.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 28, Failures: 0, Errors: 0, Skipped: 0`.

7. [x] DONE: Migrate `interaction`.
   - Move comment/like JPA entities/repositories/services/controllers.
   - Replace post/profile Feign clients with ports.
   - Remove `like.events`/`comment.events` Kafka publishing after external consumer review.
   - Keep cleanup port for post deletion.
   - Checkpoint tests: comment/reply/like flows; count/is-liked; cleanup by post id.
   Completed details:
   - Added interaction module packages under
     `monolith/src/main/java/com/friendify/app/interaction/`:
     `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, and
     `port`.
   - Migrated JPA entities:
     `Like` mapped to table `likes` and
     `Comment` mapped to table `comments`.
     Entity names were set to `InteractionLike` and `InteractionComment` for
     JPQL safety while preserving table names.
   - Migrated repositories:
     `LikeRepository` and `CommentRepository`.
     Added cleanup support methods for comment ids by post and deleting likes
     by comment id list.
   - Migrated request/response DTOs:
     `CreateLikeRequest`, `CreateCommentRequest`, `UpdateCommentRequest`,
     `LikeResponse`, and `CommentResponse`.
   - Migrated mappers:
     `LikeMapper` and `CommentMapper`.
   - Migrated services:
     `LikeService` and `CommentService`.
     They preserve like/unlike, comment/reply, update/delete, counters,
     reply loading, and profile enrichment behavior.
   - Replaced profile Feign usage with
     `com.friendify.app.profile.port.ProfileQueryPort`.
     `LikeService` and `CommentService` use the port for profile display data
     and do not use `ProfileClient`, Feign, WebClient, RestTemplate, or
     internal HTTP calls.
   - Added `com.friendify.app.interaction.port.PostQueryPort` for post
     existence checks. Step 8 replaced the temporary fail-fast adapter with
     the real post-module implementation.
   - Added interaction ports for later post migration:
     `InteractionQueryPort` with
     `countLikesByPostId(postId)`,
     `countCommentsByPostId(postId)`, and
     `isLikedByCurrentUser(postId, userId)`;
     `InteractionCleanupPort` with
     `deleteByPostId(postId)`.
     `InteractionPortService` implements both ports.
   - Post deletion cleanup is now an in-process port:
     `InteractionCleanupPort.deleteByPostId(postId)` deletes likes attached
     directly to the post, comments for the post, and likes attached to those
     comments. Step 8 post delete should call this port directly.
   - Preserved public interaction endpoints directly in the monolith:
     `POST /api/v1/interaction/likes`,
     `DELETE /api/v1/interaction/likes/{id}`,
     `DELETE /api/v1/interaction/likes/post/{postId}`,
     `DELETE /api/v1/interaction/likes/comment/{commentId}`,
     `GET /api/v1/interaction/likes/post/{postId}`,
     `POST /api/v1/interaction/comments`,
     `GET /api/v1/interaction/comments/post/{postId}`,
     `GET /api/v1/interaction/comments/{id}`,
     `GET /api/v1/interaction/comments/{id}/replies`,
     `PUT /api/v1/interaction/comments/{id}`,
     and `DELETE /api/v1/interaction/comments/{id}`.
   - Preserved temporary internal compatibility endpoints:
     `GET /internal/likes/post/{postId}/count`,
     `GET /internal/likes/post/{postId}/is-liked`, and
     `GET /internal/comments/post/{postId}/count`.
     Later monolith modules should use `InteractionQueryPort` directly instead
     of these internal endpoints.
   - Kafka was avoided in the monolith path:
     `PostEventListener`, `UserEventListener`, `KafkaTemplate`,
     `@KafkaListener`, `LikeEvent` publishing to `like.events`, and
     `CommentEvent` publishing to `comment.events` were not migrated.
   - Added interaction error codes to shared exception handling:
     `POST_NOT_FOUND`, `COMMENT_NOT_FOUND`,
     `INVALID_PARENT_COMMENT`, `LIKE_NOT_FOUND`, `ALREADY_LIKED`, and
     `INVALID_LIKE_REQUEST`.
   - No dependency changes were required. Existing JPA, validation, MySQL,
     Lombok, MapStruct, web, and security dependencies were reused. No Kafka,
     Redis, Feign, gateway, or config-server dependency was added.
   - Added tests:
     `InteractionPortBeanTests` verifies `InteractionQueryPort`,
     `InteractionCleanupPort`, and `PostQueryPort` beans exist;
     `LikeServiceTests` verifies like response enrichment uses
     `ProfileQueryPort`;
     `InteractionPortServiceTests` verifies cleanup by post id deletes related
     comments and likes.
   - Needs manual review: confirm whether any external consumers still depend
     on legacy `like.events`, `comment.events`, `post.events`, or `user.events`
     before removing old Kafka topics/deployments.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`.

8. [x] DONE: Migrate `post`.
   - Move post persistence/repositories/services/controllers.
   - Replace profile/social/interaction/group Feign with direct ports.
   - Replace post image upload Kafka with file upload port.
   - Wire post delete to interaction cleanup.
   - Checkpoint tests: feed visibility; group post permissions; post delete cleans comments/likes.
   Completed details:
   - Added post module packages under
     `monolith/src/main/java/com/friendify/app/post/`:
     `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, and
     `enums`.
   - Migrated post persistence to MySQL/JPA per current target decision:
     `Post` mapped to table `post`,
     `SavedPost` mapped to table `saved_posts`, and
     `SharedPost` mapped to table `shared_posts`.
     Post image URLs are stored in `post_image_urls`.
   - Migrated repositories:
     `PostRepository`, `SavedPostRepository`, and `SharedPostRepository`.
   - Migrated post contracts:
     `PostRequest`, `UpdatePostRequest`, `PostResponse`, and `PrivacyType`.
   - Migrated `PostMapper`, `PostService`, and the post-local
     `DateTimeFormatter` bean as `postDateTimeFormatter`.
   - Replaced internal Feign/HTTP dependencies with direct ports:
     `ProfileQueryPort` for author/profile data,
     `SocialGraphQueryPort` for friends/following/blocked visibility,
     `InteractionQueryPort` for like/comment counts and current-user liked
     state, `InteractionCleanupPort` for post delete cleanup,
     `GroupAccessPort` for group exists/can-post/can-view checks, and
     `FileUploadPort` for multipart post image uploads.
   - Replaced the Step 7 temporary `UnavailablePostQueryAdapter` with the real
     post module implementation. `PostService` now implements
     `com.friendify.app.interaction.port.PostQueryPort.exists(postId)`.
     The temporary `POST_MODULE_NOT_MIGRATED` error code was removed because it
     is no longer used.
   - Post image upload no longer uses Kafka request/reply in the monolith path.
     `PostService` calls `FileUploadPort.uploadImages(...)` directly with
     `ImageType.POST_IMAGE`.
   - Post deletion now calls `InteractionCleanupPort.deleteByPostId(postId)`
     before deleting post, saved-post, and shared-post records. The chosen
     behavior is fail-fast: if interaction cleanup fails, post deletion stops
     instead of silently leaving orphaned comments/likes.
   - Preserved public post endpoints directly in the monolith:
     `POST /api/v1/post/create`,
     `POST /api/v1/post/json`,
     `GET /api/v1/post/my-posts`,
     `POST /api/v1/post/save/{postId}`,
     `DELETE /api/v1/post/unsave/{postId}`,
     `GET /api/v1/post/saved-posts`,
     `POST /api/v1/post/share/{postId}`,
     `GET /api/v1/post/shared-posts/{postId}`,
     `GET /api/v1/post/share-count/{postId}`,
     `GET /api/v1/post/is-saved/{postId}`,
     `GET /api/v1/post/user/{userId}`,
     `GET /api/v1/post/my-shared-posts`,
     `GET /api/v1/post/saved-count`,
     `GET /api/v1/post/search`,
     `GET /api/v1/post/{postId}`,
     `PUT /api/v1/post/{postId}`,
     `PUT /api/v1/post/{postId}/json`,
     `DELETE /api/v1/post/{postId}`,
     `GET /api/v1/post/public`,
     `GET /api/v1/post/feed`, and
     `GET /api/v1/post/group/{groupId}`.
   - Preserved temporary internal compatibility endpoint:
     `GET /internal/posts/{postId}/exists`.
     Inside the monolith, interaction uses `PostQueryPort` directly instead of
     calling this endpoint.
   - Added post error codes to shared exception handling:
     `POST_EMPTY`, `POST_ALREADY_SAVED`, `POST_NOT_SAVED`,
     `POST_NOT_OWNER`, `SHARED_POST_NOT_FOUND`, and
     `POST_IMAGE_UPLOAD_FAILED`.
   - Removed MongoDB from the monolith path after the user clarified the target
     should use MySQL only. `spring-boot-starter-data-mongodb`,
     `spring.data.mongodb.uri`, and `FRIENDIFY_MONGODB_URI` were removed.
     Existing JPA/MySQL dependencies are used. No Kafka, Redis, Feign, gateway,
     or config-server dependency was added.
   - Added tests:
     `monolith/src/test/java/com/friendify/app/post/port/PostQueryPortBeanTests.java`
     verifies the interaction `PostQueryPort` is backed by `PostService`;
     `monolith/src/test/java/com/friendify/app/post/service/PostServiceTests.java`
     verifies `PostService` implements `PostQueryPort`, post multipart create
     uses `FileUploadPort`, feed visibility uses `SocialGraphQueryPort`, and
     delete calls `InteractionCleanupPort`.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 36, Failures: 0, Errors: 0, Skipped: 0`.
   - Needs manual review: legacy `post-service` data currently lives in MongoDB
     in the microservice architecture. Production needs an explicit data
     migration from Mongo collections `post`, `saved_posts`, and `shared_posts`
     into the MySQL tables above before cutting traffic to the monolith.
     Post deletion now stays inside MySQL/JPA for post records and calls the
     MySQL-backed interaction cleanup port first.

9. [x] DONE: Migrate `notification`.
   - Move notification persistence to MySQL/JPA, then migrate repository/service/controllers and Brevo client.
   - Replace `notification-delivery` Kafka listener with direct application service.
   - Decide sync vs async email delivery.
   - Checkpoint tests: email dispatch with mocked Brevo; notification persistence/read/unread count.
   Completed details:
   - Added notification module packages under
     `monolith/src/main/java/com/friendify/app/notification/`:
     `controller`, `dto`, `entity`, `mapper`, `port`, `repository`,
     `service`, and the existing `email` package from Step 3e.
   - Migrated notification persistence to MySQL/JPA per the current
     MySQL-only target:
     `Notification` is mapped as a JPA entity to table `notifications`.
     The legacy notification-service Mongo collection `notifications` now
     needs a production data migration into this table if old data matters.
   - Migrated repository:
     `NotificationRepository` extends `JpaRepository` and preserves
     `findByUserIdOrderByCreatedAtDesc`, `countByUserIdAndIsReadFalse`, and
     `findByIdAndUserId`.
   - Migrated DTO/mapper:
     `NotificationResponse` and `NotificationMapper`.
     Existing Step 3e email DTOs remain under
     `com.friendify.app.notification.email.dto`.
   - Migrated notification service behavior:
     `NotificationService.getMyNotifications`,
     `markAsRead`, `markAllAsRead`, `getUnreadCount`,
     `createNotification`, `createNotificationFromEvent`, and
     `deliver(NotificationEvent)`.
   - Added `NotificationCreatePort` with:
     `createNotification(...)` and `createNotificationFromEvent(...)`.
     `NotificationService` implements this port for direct in-process
     notification creation by future modules.
   - Replaced legacy `notification-delivery` Kafka listener behavior with the
     direct application service method `NotificationService.deliver(...)`.
     It sends email through `EmailDeliveryService` and creates a persisted
     notification when the event contains `param.userId`.
   - Consolidated email delivery from Step 3e:
     `AuthEmailNotificationAdapter` remains the single
     `com.friendify.app.auth.port.NotificationDeliveryPort` implementation.
     It delegates to `EmailDeliveryService`, which calls Brevo through
     `RestClientBrevoEmailClient`.
   - Auth email flows continue to use `NotificationDeliveryPort` directly:
     registration verification, resend verification, forgot password, and
     related auth emails are not no-op and do not use Kafka.
   - Preserved public notification paths directly in the monolith:
     `GET /api/v1/notification/notifications`,
     `PUT /api/v1/notification/notifications/{id}/read`,
     `PUT /api/v1/notification/notifications/read-all`,
     `GET /api/v1/notification/notifications/unread-count`, and
     `POST /api/v1/notification/email/send`.
   - Security decision: `/api/v1/notification/email/send` is present but is
     protected by the monolith's default authenticated security rule. The old
     gateway public allowlist is not copied automatically because public email
     sending is risky. Needs manual review if clients require it to be public.
   - Kafka was avoided in the monolith path:
     the old Kafka `NotificationController`, `@KafkaListener`,
     `KafkaTemplate`, topic `notification-delivery`, and `spring-kafka` were
     not migrated.
   - Email sending remains synchronous. No `@Async`, Kafka, or outbox was
     added. Needs manual review later if email reliability or request latency
     requirements require a local outbox.
   - No dependency changes were required. Existing JPA/MySQL, validation, web,
     security, Lombok, MapStruct, and RestClient support are reused. No Kafka,
     Redis, Feign, MongoDB, gateway, or config-server dependency was added.
   - Added notification error code:
     `NOTIFICATION_NOT_FOUND`.
   - Added tests:
     `NotificationDeliveryPortBeanTests` verifies there is exactly one real
     `NotificationDeliveryPort` implementation and it is
     `AuthEmailNotificationAdapter`;
     `NotificationServiceTests` verifies `NotificationService` implements
     `NotificationCreatePort`, list/read/create behavior, and
     `deliver(NotificationEvent)` sends email and creates a notification.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 41, Failures: 0, Errors: 0, Skipped: 0`.

10. [x] DONE: Migrate `chat`.
   - Moved chat code into `monolith/src/main/java/com/friendify/app/chat`.
   - Migrated chat persistence to MySQL/JPA, not MongoDB, using:
     - `entity/Conversation.java` mapped to table `conversation`
     - `entity/ParticipantInfo.java` as an embeddable participant snapshot in table `conversation_participants`
     - `entity/ChatMessage.java` mapped to table `chat_message`
     - `entity/ReadReceipt.java` mapped to table `read_receipt`
   - Migrated repositories:
     - `ConversationRepository`
     - `ChatMessageRepository`
     - `ReadReceiptRepository`
   - Migrated services:
     - `ConversationService`
     - `ChatMessageService`
     - `ReadReceiptService`
   - Migrated controllers:
     - `ConversationController`
     - `ChatMessageController`
     - `WebSocketController`
   - Migrated WebSocket/STOMP configuration:
     - `WebSocketConfig`
     - `WebSocketAuthInterceptor`
   - Replaced profile Feign usage with direct `ProfileQueryPort` calls in
     `ConversationService` and `ChatMessageService`. No profile HTTP,
     WebClient, RestTemplate, or Feign client was added.
   - Preserved REST endpoints under `/api/v1/chat/**`:
     - `POST /api/v1/chat/conversations`
     - `GET /api/v1/chat/conversations/my-conversations`
     - `GET /api/v1/chat/conversations/{id}`
     - `PUT /api/v1/chat/conversations/{id}`
     - `DELETE /api/v1/chat/conversations/{id}`
     - `POST /api/v1/chat/conversations/{id}/participants`
     - `DELETE /api/v1/chat/conversations/{id}/participants/{participantId}`
     - `POST /api/v1/chat/conversations/{id}/leave`
     - `POST /api/v1/chat/conversations/{id}/admins`
     - `DELETE /api/v1/chat/conversations/{id}/admins/{participantId}`
     - `POST /api/v1/chat/messages`
     - `GET /api/v1/chat/messages?conversationId=...`
     - `GET /api/v1/chat/messages/paginated`
     - `GET /api/v1/chat/messages/{id}`
     - `PUT /api/v1/chat/messages/{id}`
     - `DELETE /api/v1/chat/messages/{id}`
     - `POST /api/v1/chat/messages/{id}/read`
     - `GET /api/v1/chat/messages/{id}/read-receipts`
     - `GET /api/v1/chat/messages/unread-count`
   - Preserved STOMP behavior:
     - handshake endpoint `/ws`
     - application destination prefix `/app`
     - broker prefixes `/topic`, `/queue`, `/user`
     - user destination prefix `/user`
     - `@MessageMapping("/chat.sendMessage")` publishes to
       `/topic/conversation/{conversationId}`
     - `@MessageMapping("/chat.typing")` publishes to
       `/topic/conversation/{conversationId}/typing`
     - `@MessageMapping("/chat.addUser")` publishes to
       `/topic/conversation/{conversationId}`
     - `@MessageMapping("/chat.removeUser")` publishes to
       `/topic/conversation/{conversationId}`
   - Phase 12 update: WebSocket allowed origins are now environment-driven
     through `FRIENDIFY_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`; production should
     use exact frontend origins.
   - Updated `SecurityConfig` to permit only the WebSocket handshake paths
     while keeping STOMP token validation in `WebSocketAuthInterceptor`.
   - Added chat error codes to `shared/exception/ErrorCode.java`.
   - Added dependency:
     `spring-boot-starter-websocket`.
   - No Kafka, Redis, Feign, MongoDB, gateway, or config-server dependency was
     added.
   - Added tests:
     - `ConversationServiceTests` verifies profile data is loaded through
       `ProfileQueryPort`.
     - `ChatMessageServiceTests` verifies message sender data is loaded through
       `ProfileQueryPort`.
   - Verification: `cd monolith && mvn test` passed with `BUILD SUCCESS`;
     `Tests run: 43, Failures: 0, Errors: 0, Skipped: 0`.

11. [ ] Retire gateway/config-server.
   - Only after monolith endpoint parity is verified.
   - Keep compatibility routing inside monolith so clients still use `/api/v1/{domain}/...`.
   - Remove `api-gateway` and `config-server` from deployment, not from source, until a release rollback plan exists.
   - Phase 12 review: not safe to mark DONE yet because the final smoke test
     checklist still requires manual verification against real MySQL,
     Cloudinary, Brevo, Google OAuth2, frontend routes, and WebSocket clients.

## 10. Phase 12: Post-Migration Cleanup, Hardening, And Production Readiness

Status: [x] DONE for monolith cleanup/readiness checks. Gateway/config-server
retirement remains unchecked because it requires real environment smoke tests
and a cutover decision.

### Cleanup Summary

- Scanned `monolith/src/main/java`, `monolith/src/test/java`,
  `monolith/src/main/resources`, `monolith/src/test/resources`, and
  `monolith/pom.xml` for migration leftovers:
  - `com.tien.*`
  - Kafka runtime usage: `KafkaTemplate`, `@KafkaListener`,
    `ImageUploadKafkaService`, `ImageTopics`, `spring-kafka`
  - internal Feign/Spring Cloud leftovers:
    `spring-cloud-starter-openfeign`, `spring-cloud-config`,
    gateway dependencies
  - Redis runtime/config usage
  - Mongo runtime usage: `MongoRepository`, `@Document`,
    `spring.data.mongodb`, `spring-boot-starter-data-mongodb`
  - no-op/stub/temporary bridge markers
- No obsolete Kafka, Redis, Feign, MongoDB, gateway, config-server, or
  `com.tien.*` runtime dependency was found in the monolith.
- No old microservice source code was deleted.
- No `api-gateway` or `config-server` source code was deleted.
- Added `ArchitectureCleanupTests` to keep checking that obsolete runtime
  dependencies are not reintroduced into the monolith.
- Added explicit port wiring coverage in `ArchitectureCleanupTests` for:
  - `ProfileCreationPort`
  - `ProfileQueryPort`
  - `NotificationDeliveryPort`
  - `FileUploadPort`
  - `SocialGraphQueryPort`
  - `GroupAccessPort`
  - `InteractionQueryPort`
  - `InteractionCleanupPort`
  - `PostQueryPort`

### Hardening Changes

- Added `monolith/src/main/java/com/friendify/app/config/CorsConfig.java`.
  CORS is now controlled by:
  - `app.cors.allowed-origin-patterns`
  - `FRIENDIFY_CORS_ALLOWED_ORIGIN_PATTERNS`
- Updated `WebSocketConfig` so `/ws` allowed origins are no longer hard-coded
  as `*`. WebSocket origins are now controlled by:
  - `app.websocket.allowed-origin-patterns`
  - `FRIENDIFY_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`
- Updated `monolith/.env.example` with the CORS and WebSocket origin variables.
- Removed the hard-coded Brevo sender email default from
  `EmailDeliveryService` and `application.properties`.
- `EmailDeliveryService` now fails with
  `NOTIFICATION_DELIVERY_NOT_CONFIGURED` if the Brevo API key or sender email is
  missing. It does not return success without a real email send attempt.
- Added a test for missing sender email configuration.

### Removed Obsolete Dependencies

No dependency removal was needed in Phase 12 because the monolith `pom.xml`
already had no Kafka, Redis, Feign, Spring Cloud Config, Gateway, or MongoDB
runtime dependencies.

Current monolith runtime dependencies intentionally retained:

- Spring Web
- Spring WebSocket
- Spring Security
- OAuth2 Resource Server
- OAuth2 Client
- Spring Data JPA
- MySQL driver
- Cloudinary
- Validation
- Lombok/MapStruct

### Direct Module Port Verification

| Flow | Verified direct port |
|---|---|
| auth -> profile | `ProfileCreationPort` |
| auth -> notification/email | `NotificationDeliveryPort` |
| profile -> file | `FileUploadPort` |
| group -> profile | `ProfileQueryPort` |
| group -> file | `FileUploadPort` |
| post -> file | `FileUploadPort` |
| social -> profile | `ProfileQueryPort` |
| interaction -> profile | `ProfileQueryPort` |
| interaction -> post | `PostQueryPort` |
| post -> profile | `ProfileQueryPort` |
| post -> social | `SocialGraphQueryPort` |
| post -> interaction query | `InteractionQueryPort` |
| post -> interaction cleanup | `InteractionCleanupPort` |
| post -> group | `GroupAccessPort` |
| chat -> profile | `ProfileQueryPort` |

No internal Feign, WebClient, RestTemplate, Kafka, or Redis path was added for
these module calls.

### Endpoint Parity Summary

Detailed endpoint lists are documented above in section 3 and in each completed
migration step. Phase 12 parity summary:

| Surface | Status | Notes |
|---|---|---|
| `/api/v1/identity/**` | Verified | Controllers are present under `auth`; public auth endpoints are explicitly permitted. Needs manual smoke test. |
| `/oauth2/**` and `/login/oauth2/**` | Verified | OAuth2 framework paths are preserved. Needs manual Google OAuth2 smoke test with deployed redirect URIs. |
| `/api/v1/identity/oauth2/**` and `/api/v1/identity/login/oauth2/**` | Verified | Compatibility controller is present. Needs manual frontend smoke test. |
| `/api/v1/profile/**` | Verified | Profile CRUD/search/avatar/background endpoints are present. Needs manual media smoke test. |
| `/api/v1/file/**` | Verified | Image upload endpoints are present. Needs manual Cloudinary smoke test. |
| `/api/v1/social/**` | Verified | Friendship/follow/block endpoints are present. Needs manual workflow smoke test. |
| `/api/v1/group/**` | Verified | Group CRUD/membership/join/media endpoints are present. Needs manual workflow smoke test. |
| `/api/v1/interaction/**` | Verified | Like/comment/reply/count endpoints are present. Needs manual workflow smoke test. |
| `/api/v1/post/**` | Verified | Post CRUD/feed/save/share/group/image endpoints are present. Needs manual feed and delete-cleanup smoke test. |
| `/api/v1/notification/**` | Verified | Notification list/read/read-all/unread-count and email send endpoints are present. Needs manual Brevo smoke test. |
| `/api/v1/chat/**` | Verified | Conversation/message/read-receipt endpoints are present. Needs manual chat smoke test. |
| `/ws` | Verified | STOMP endpoint is present. Needs manual WebSocket auth/origin smoke test. |
| `/api/v1/file/media/download/**` | Intentionally removed | Gateway allowlist referenced it, but no active file-service endpoint was found. Needs manual review before re-adding. |

### Configuration Readiness

Monolith config uses placeholders/environment variables for:

- `SERVER_PORT`
- MySQL datasource:
  - `FRIENDIFY_DATASOURCE_URL`
  - `FRIENDIFY_DATASOURCE_USERNAME`
  - `FRIENDIFY_DATASOURCE_PASSWORD`
- JPA:
  - `FRIENDIFY_JPA_DDL_AUTO`
  - `FRIENDIFY_JPA_SHOW_SQL`
- JWT:
  - `FRIENDIFY_JWT_SIGNER_KEY`
  - `FRIENDIFY_JWT_ISSUER`
  - `FRIENDIFY_JWT_VALID_DURATION`
  - `FRIENDIFY_JWT_REFRESHABLE_DURATION`
- Google OAuth2:
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`
  - `GOOGLE_REDIRECT_URI`
  - `FRIENDIFY_OAUTH2_AUTHORIZED_REDIRECT_URI`
  - `FRIENDIFY_OAUTH2_COOKIE_NAME`
  - `FRIENDIFY_OAUTH2_COOKIE_SECURE`
  - `FRIENDIFY_OAUTH2_COOKIE_SAME_SITE`
  - `FRIENDIFY_OAUTH2_COOKIE_PATH`
- Brevo:
  - `BREVO_URL`
  - `BREVO_API_KEY`
  - `BREVO_SENDER_NAME`
  - `BREVO_SENDER_EMAIL`
- Cloudinary:
  - `CLOUDINARY_CLOUD_NAME`
  - `CLOUDINARY_API_KEY`
  - `CLOUDINARY_API_SECRET`
- CORS:
  - `FRIENDIFY_CORS_ALLOWED_ORIGIN_PATTERNS`
- WebSocket:
  - `FRIENDIFY_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`

MongoDB URI readiness: not applicable to the current monolith runtime because
the user clarified during migration that the target should use MySQL only.
No MongoDB dependency or `FRIENDIFY_MONGODB_URI` remains in the monolith. Needs
manual review only if the target changes back to mixed MySQL/MongoDB.

### Security Review Notes

- Public endpoints are limited in `SecurityConfig` to:
  - selected `POST /api/v1/identity/auth/**` endpoints
  - OAuth2 framework and compatibility paths
  - `/ws` handshake paths
  - temporary `/internal/users/**` profile compatibility paths
  - Swagger/OpenAPI paths
- Role/permission/user endpoints are not public and fall through to
  authenticated access.
- `/api/v1/notification/email/send` is not public in the monolith. It requires
  authentication through the default rule, unlike the old gateway allowlist.
- Swagger/OpenAPI paths are currently public for development compatibility.
  Needs manual review before production.
- CORS and WebSocket origins are now environment-driven. Production should use
  exact frontend origins, not `*`.
- WebSocket HTTP handshake is permitted, but STOMP `CONNECT`, `SEND`, and
  `SUBSCRIBE` frames are validated by `WebSocketAuthInterceptor`.
- CSRF is disabled because the backend uses JWT bearer/cookie auth. Needs manual
  production review if OAuth2 cookie auth is used for state-changing browser
  requests.

### Database Review Notes

- The current monolith uses MySQL/JPA only.
- JPA table names preserved or intentionally mapped from legacy names include:
  `user`, `role`, `permission`, `invalidated_token`, `user_otp`,
  `user_profile`, `file`, `image_versions`, `friendships`, `follows`,
  `user_blocks`, `group`, `group_member`, `join_request`, `comments`, `likes`,
  `post`, `saved_posts`, `shared_posts`, `notifications`, `conversation`,
  `conversation_participants`, `chat_message`, and `read_receipt`.
- Legacy Mongo collection names from old services were remapped to MySQL/JPA
  tables during migration after the user clarified the target. These require
  production data migration scripts if existing data must be preserved:
  `post`, `saved_posts`, `shared_posts`, `group`, `group_member`,
  `join_request`, `file`, `notifications`, `conversation`, `chat_message`,
  and `read_receipt`.
- No cross-database foreign keys were added.
- Needs manual review: production should replace `spring.jpa.hibernate.ddl-auto`
  with explicit schema migrations before cutover.

### External Integration Review

- Cloudinary upload goes through `FileUploadPort` and `ImageService`.
- Brevo email goes through `NotificationDeliveryPort`,
  `AuthEmailNotificationAdapter`, and `EmailDeliveryService`.
- Google OAuth2 uses monolith OAuth2 callback/redirect configuration.
- No external Kafka dependency remains in the monolith. Needs manual review
  before deleting old Kafka topics or deployments because external consumers may
  exist outside this repository.

### Deployment Docs

- Added `monolith/README.md` with:
  - local run commands
  - required environment variables
  - monolith runtime dependencies
  - services no longer needed at runtime
  - docker-compose monolith profile guidance
  - rollback procedure using old microservices/gateway/config-server
- No repository-level docker-compose file existed. The README documents the
  expected `monolith` compose profile shape. Needs manual review before adding a
  real production Dockerfile/image build pipeline.

### Final Smoke Test Checklist

Run these manually before switching production traffic:

- [ ] Register user.
- [ ] Verify user by OTP.
- [ ] Resend verification.
- [ ] Login.
- [ ] Refresh token.
- [ ] Logout.
- [ ] Forgot password.
- [ ] Reset password.
- [ ] OAuth2 Google login.
- [ ] Get profile.
- [ ] Update profile.
- [ ] Upload avatar.
- [ ] Upload background.
- [ ] Upload file image.
- [ ] Send friend request.
- [ ] Accept friend request.
- [ ] Reject friend request.
- [ ] Follow user.
- [ ] Unfollow user.
- [ ] Block user.
- [ ] Unblock user.
- [ ] Create group.
- [ ] Join group.
- [ ] Approve join request.
- [ ] Upload group avatar.
- [ ] Upload group cover.
- [ ] Create post with image.
- [ ] Update post.
- [ ] Save post.
- [ ] Unsave post.
- [ ] Share post.
- [ ] Public feed.
- [ ] Personal feed.
- [ ] Group feed.
- [ ] Like post.
- [ ] Unlike post.
- [ ] Comment.
- [ ] Reply.
- [ ] Delete post and verify interaction cleanup.
- [ ] Notification list.
- [ ] Mark notification read.
- [ ] Mark all notifications read.
- [ ] Unread notification count.
- [ ] Create chat conversation.
- [ ] Send chat message.
- [ ] Read receipt.
- [ ] WebSocket connect to `/ws`.
- [ ] WebSocket `sendMessage`.
- [ ] WebSocket `typing`.
- [ ] WebSocket `addUser`.
- [ ] WebSocket `removeUser`.

### Phase 12 Verification

- `cd monolith && mvn test`: `BUILD SUCCESS`;
  `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0`.
- `cd monolith && mvn verify`: `BUILD SUCCESS`;
  `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0`.

### Remaining Work Gate

No further safe source-code migration work was found in the monolith after the
Phase 12 scan. The remaining items are release/cutover tasks that must be done
against a real environment:

- Execute the final manual smoke test checklist above.
- Set production values for all secrets and origin allowlists.
- Create reviewed MySQL schema/data migration scripts for legacy service data,
  especially data previously stored in MongoDB.
- Decide whether Swagger/OpenAPI should remain public.
- Decide whether `/internal/**` compatibility endpoints are still needed by any
  external clients.
- Confirm no external Kafka consumers still depend on old topics before
  deleting Kafka infrastructure.
- Retire `api-gateway` and `config-server` from deployment only after the
  manual smoke tests pass and rollback has been rehearsed.

## 11. Risks And Manual Review Items

- Version policy: legacy services remain on Java 17/Spring Boot 3.5.5, while
  the monolith is now Java 21/Spring Boot 4.0.6 per `AGENTS.md`. Do not upgrade
  legacy services unless they still need to run in rollback mode and an explicit
  rollback compatibility review is done.
- Config-server port/config risk: services import `optional:configserver:http://localhost:8888`, while `config-server/src/main/resources/application.yaml` sets `management.server.port: 8888` but no explicit `server.port`. Confirm how config-server is actually run before relying on its current runtime behavior.
- Gateway path compatibility: current services rely on servlet context paths and gateway `StripPrefix=2`. The monolith must expose `/api/v1/identity`, `/api/v1/profile`, `/api/v1/post`, etc. directly or via controller base mappings.
- Gateway public allowlist risk: `AuthenticationFilter` permits `/api/v1/file/media/download/.*`, but only commented-out file download code was found in `CloudMediaController`. Needs manual review before copying this allowlist.
- OAuth2 framework endpoint risk: `/oauth2/**` and `/login/oauth2/**` are not visible in controller scans but are part of Spring Security Google login. Preserve and test them if OAuth2 remains enabled.
- Profile media sequencing risk: `ProfileController.updateAvatar` and `updateBackgroundImage` call profile media logic that currently depends on `profile-service/src/main/java/com/tien/profileservice/service/ImageUploadKafkaService.java`. Migrate profile core first, then complete these media endpoints immediately after the file module is available.
- Security duplication: every service has its own `SecurityConfig`, `CustomJwtDecoder`, and `JwtAuthenticationEntryPoint`. Consolidation can accidentally open or block endpoints.
- Internal endpoints: `/internal/**` endpoints are currently used by Feign clients. In monolith they should become ports, but keep temporary compatibility endpoints only if external clients still call them.
- Data ownership: identity `User.id`, profile `Profile.userId`, social ids, post `userId`, group `ownerId/member.userId`, chat participant `userId`, and notification `userId` are string references without DB FKs. Cross-module consistency must be tested.
- Post data migration: legacy `post-service` stores `post`, `saved_posts`, and
  `shared_posts` in MongoDB, but the monolith Step 8 implementation now maps
  post data to MySQL/JPA tables. Needs manual review and a production data
  migration before switching traffic.
- Post delete consistency: Step 8 wires post deletion to
  `InteractionCleanupPort.deleteByPostId(postId)` before deleting MySQL post
  records. This preserves cleanup behavior and keeps the migrated path in
  MySQL, but failure behavior is still fail-fast and should be reviewed before
  production.
- Kafka external dependencies: no consumers/producers were found for several topics in this repo, but external consumers may exist. Needs manual review before deleting Kafka topics or deployments.
- File upload failure timing: Kafka request/reply currently waits up to 30 seconds. Direct Cloudinary calls fail immediately in the request path; preserve error mapping.
- Notification reliability: Step 9 replaces Kafka notification delivery with
  synchronous direct service calls to Brevo. Registration/password reset can now
  depend on Brevo availability unless a later local outbox or async delivery
  mechanism is added.
- Auth email sequencing risk: registration verification, resend verification,
  forgot password, and reset password depend on real Brevo email delivery. The
  monolith no longer uses a no-op notification stub, but production must set
  `BREVO_API_KEY` and `BREVO_SENDER_EMAIL` and smoke test these flows.
- OAuth2 redirect: `identity-service` uses `app.oauth2.authorizedRedirectUri` and Google OAuth config. Verify callback paths after monolith route changes.
- OAuth2 cookie security: OAuth2 success now uses an HttpOnly access-token cookie instead of a query token. Production must run HTTPS with `FRIENDIFY_OAUTH2_COOKIE_SECURE=true`, verify CORS credentials behavior with the frontend, and review CSRF protection before relying on cookie-authenticated state-changing APIs.
- Duplicate DTOs: `ProfileResponse`, `UserProfileResponse`, `ApiResponse`, `PageResponse`, and exception classes are duplicated with possibly different fields. Compare fields before unifying.
- Mongo collection names: the current target is MySQL for migrated modules.
  Legacy Mongo collections already remapped to MySQL/JPA in the monolith include
  `file`, `group`, `group_member`, `join_request`, `post`, `saved_posts`,
  `shared_posts`, `notifications`, `conversation`, `chat_message`, and
  `read_receipt`. Each legacy collection needs a data migration plan if
  existing data must be preserved.
- Chat WebSocket origin risk: Phase 12 moved `/ws` allowed origins to
  `FRIENDIFY_WEBSOCKET_ALLOWED_ORIGIN_PATTERNS`. Needs manual review before
  production; set exact deployed frontend domains.
- Chat WebSocket security risk: the HTTP handshake path is permitted so SockJS
  can connect, while STOMP `CONNECT`/`SEND`/`SUBSCRIBE` frames are validated in
  `WebSocketAuthInterceptor`. Verify frontend token forwarding and close
  behavior before production.
- Chat data migration risk: legacy `chat-service` stored conversations,
  messages, read receipts, and participant snapshots in MongoDB documents; the
  monolith Step 10 implementation maps them to MySQL/JPA tables. Needs manual
  review and a production migration script before switching traffic.
- Tests are improved but still not a replacement for environment smoke tests:
  the monolith has port wiring, cleanup guard, auth, profile, file, social,
  group, interaction, post, notification, and chat focused tests, but production
  dependencies still need manual verification before deleting or disabling old
  services.

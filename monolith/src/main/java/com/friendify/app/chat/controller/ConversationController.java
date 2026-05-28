package com.friendify.app.chat.controller;

import java.util.List;

import com.friendify.app.chat.dto.request.AddAdminRequest;
import com.friendify.app.chat.dto.request.AddParticipantRequest;
import com.friendify.app.chat.dto.request.ConversationRequest;
import com.friendify.app.chat.dto.request.UpdateConversationRequest;
import com.friendify.app.chat.dto.response.ConversationResponse;
import com.friendify.app.chat.service.ConversationService;
import com.friendify.app.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat/conversations")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationController {
    ConversationService conversationService;

    @PostMapping
    ApiResponse<ConversationResponse> createConversation(@RequestBody @Valid ConversationRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.create(request))
                .build();
    }

    @GetMapping("/my-conversations")
    ApiResponse<List<ConversationResponse>> myConversations() {
        return ApiResponse.<List<ConversationResponse>>builder()
                .result(conversationService.myConversations())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ConversationResponse> getById(@PathVariable String id) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<ConversationResponse> updateConversation(
            @PathVariable String id, @RequestBody @Valid UpdateConversationRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.updateConversation(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteConversation(@PathVariable String id) {
        conversationService.deleteConversation(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/{id}/participants")
    ApiResponse<ConversationResponse> addParticipants(
            @PathVariable String id, @RequestBody @Valid AddParticipantRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.addParticipants(id, request))
                .build();
    }

    @DeleteMapping("/{id}/participants/{participantId}")
    ApiResponse<ConversationResponse> removeParticipant(@PathVariable String id, @PathVariable String participantId) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.removeParticipant(id, participantId))
                .build();
    }

    @PostMapping("/{id}/leave")
    ApiResponse<Void> leaveConversation(@PathVariable String id) {
        conversationService.leaveConversation(id);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/{id}/admins")
    ApiResponse<ConversationResponse> promoteToAdmin(
            @PathVariable String id, @RequestBody @Valid AddAdminRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.promoteToAdmin(id, request))
                .build();
    }

    @DeleteMapping("/{id}/admins/{participantId}")
    ApiResponse<ConversationResponse> demoteFromAdmin(@PathVariable String id, @PathVariable String participantId) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.demoteFromAdmin(id, participantId))
                .build();
    }
}

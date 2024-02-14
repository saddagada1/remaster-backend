package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.request.CreateRemasterRequest;
import com.saivamsi.remaster.request.UpdateRemasterRequest;
import com.saivamsi.remaster.response.PageResponse;
import com.saivamsi.remaster.response.RemasterResponse;
import com.saivamsi.remaster.service.RemasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RemasterController {

    public final RemasterService remasterService;

    @PostMapping("/user/remaster")
    public ResponseEntity<RemasterResponse> createRemaster(@RequestBody CreateRemasterRequest request, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity.ok(remasterService.createRemaster(request, user));
    }

    @PutMapping("/user/remaster")
    public ResponseEntity<RemasterResponse> updateRemaster(@RequestBody UpdateRemasterRequest request, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity.ok(remasterService.updateRemaster(request, user));
    }

    @GetMapping("/user/remaster/{id}")
    public ResponseEntity<RemasterResponse> getUserRemaster(@PathVariable UUID id, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity
                .ok(remasterService.getUserRemaster(id, user));
    }

    @GetMapping("/user/remaster")
    public ResponseEntity<PageResponse<RemasterResponse>> getAllUserRemasters(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity
                .ok(remasterService.getAllUserRemasters(user, cursor, limit));
    }

    @GetMapping("/open/remaster/{id}")
    public ResponseEntity<RemasterResponse> getRemaster(@PathVariable UUID id) {
        return ResponseEntity
                .ok(remasterService.getRemaster(id));
    }

    @GetMapping("/open/remaster/all/{id}")
    public ResponseEntity<PageResponse<RemasterResponse>> getAllRemastersByUserId(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @PathVariable UUID id) {
        return ResponseEntity
                .ok(remasterService.getAllRemastersByUserId(id, cursor, limit));
    }

    @GetMapping("/open/remaster/search")
    public ResponseEntity<PageResponse<RemasterResponse>> searchRemasters(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @RequestParam String q) {
        return ResponseEntity
                .ok(remasterService.searchRemasters(q, cursor, limit));
    }
}

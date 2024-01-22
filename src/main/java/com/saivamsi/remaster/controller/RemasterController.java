package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.request.RemasterRequest;
import com.saivamsi.remaster.response.PageResponse;
import com.saivamsi.remaster.response.RemasterResponse;
import com.saivamsi.remaster.service.RemasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user/remaster")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RemasterController {

    public final RemasterService remasterService;

    @PostMapping()
    public ResponseEntity<RemasterResponse> createRemaster(@RequestBody RemasterRequest request, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity.ok(remasterService.createRemaster(request, user));
    }

    @GetMapping("/private/{id}")
    public ResponseEntity<RemasterResponse> getUserRemaster(@PathVariable UUID id, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity
                .ok(remasterService.getUserRemaster(id, user));
    }

    @GetMapping("/private")
    public ResponseEntity<PageResponse<RemasterResponse>> getAllUserRemasters(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity
                .ok(remasterService.getAllUserRemasters(user, cursor, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RemasterResponse> getRemaster(@PathVariable UUID id) {
        return ResponseEntity
                .ok(remasterService.getRemaster(id));
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<PageResponse<RemasterResponse>> getAllRemastersByUserId(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @PathVariable UUID id) {
        return ResponseEntity
                .ok(remasterService.getAllRemastersByUserId(id, cursor, limit));
    }
}

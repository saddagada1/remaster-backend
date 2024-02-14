package com.saivamsi.remaster.service;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Remaster;
import com.saivamsi.remaster.repository.RemasterRepository;
import com.saivamsi.remaster.request.CreateRemasterRequest;
import com.saivamsi.remaster.request.UpdateRemasterRequest;
import com.saivamsi.remaster.response.PageResponse;
import com.saivamsi.remaster.response.RemasterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemasterService {
    private final RemasterRepository remasterRepository;

    public RemasterResponse createRemaster (CreateRemasterRequest remaster, ApplicationUser user) {
//        if (!user.isVerified()) {
//            throw new GlobalException(HttpStatus.BAD_REQUEST, GlobalError.builder().subject("user").message("please verify your account before proceeding").build());
//        }

        return remasterRepository.save(Remaster.builder().url(remaster.getUrl()).name(remaster.getName()).description(remaster.getDescription())
                .duration(remaster.getDuration()).key(remaster.getKey()).mode(remaster.getMode()).tempo(remaster.getTempo()).timeSignature(remaster.getTimeSignature())
                .tuning(remaster.getTuning()).user(user).build()
        ).getRemasterResponse();
    }

    public RemasterResponse updateRemaster (UpdateRemasterRequest remaster, ApplicationUser user) {
        Optional<Remaster> userRemaster = remasterRepository.findByIdAndUserId(remaster.getId(), user.getId());

        if (userRemaster.isEmpty()) {
            throw new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build());
        }

        return remasterRepository.save(userRemaster.get().updateRemaster(remaster)).getRemasterResponse();
    }

    public RemasterResponse getUserRemaster(UUID id, ApplicationUser user) {
        Optional<Remaster> userRemaster = remasterRepository.findByIdAndUserId(id, user.getId());

        if (userRemaster.isEmpty()) {
            throw new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build());
        }

        return userRemaster.get().getRemasterResponse();
    }

    public PageResponse<RemasterResponse> getAllUserRemasters(ApplicationUser user, UUID cursor, Integer limit) {
        List<Remaster> remasters;
        if (cursor == null) {
            remasters = remasterRepository.findAllByUserId(user.getId(), limit + 1);
        } else {
            remasters = remasterRepository.findAllByUserIdAndCursor(user.getId(), cursor, limit + 1);
        }

        UUID next = null;
        if (remasters.size() > limit) {
            Remaster last = remasters.removeLast();
            next = last.getId();
        }

        List<RemasterResponse> remasterResponses = remasters.stream()
                .map(Remaster::getRemasterResponse)
                .toList();

        return new PageResponse<>(next, remasterResponses);
    }

    public RemasterResponse getRemaster(UUID id) {
        Optional<Remaster> userRemaster = remasterRepository.findById(id);

        if (userRemaster.isEmpty()) {
            throw new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build());
        }

        return userRemaster.get().getRemasterResponse();
    }

    public PageResponse<RemasterResponse> getAllRemastersByUserId(UUID id, UUID cursor, Integer limit) {
        List<Remaster> remasters;
        if (cursor == null) {
            remasters = remasterRepository.findAllByUserId(id, limit + 1);
        } else {
            remasters = remasterRepository.findAllByUserIdAndCursor(id, cursor, limit + 1);
        }

        UUID next = null;
        if (remasters.size() > limit) {
            Remaster last = remasters.removeLast();
            next = last.getId();
        }

        List<RemasterResponse> remasterResponses = remasters.stream()
                .map(Remaster::getRemasterResponse)
                .toList();

        return new PageResponse<>(next, remasterResponses);
    }

    public PageResponse<RemasterResponse> searchRemasters(String query, UUID cursor, Integer limit) {
        List<Remaster> remasters;
        if (cursor == null) {
            remasters = remasterRepository.searchRemasters(query, limit + 1);
        } else {
            remasters = remasterRepository.searchRemastersWithCursor(query, cursor, limit + 1);
        }

        UUID next = null;
        if (remasters.size() > limit) {
            Remaster last = remasters.removeLast();
            next = last.getId();
        }

        List<RemasterResponse> remasterResponses = remasters.stream()
                .map(Remaster::getRemasterResponse)
                .toList();

        return new PageResponse<>(next, remasterResponses);
    }
}

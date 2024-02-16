package com.saivamsi.remaster.service;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.RemasterLike;
import com.saivamsi.remaster.model.Remaster;
import com.saivamsi.remaster.model.RemasterPlay;
import com.saivamsi.remaster.repository.RemasterLikeRepository;
import com.saivamsi.remaster.repository.RemasterPlayRepository;
import com.saivamsi.remaster.repository.RemasterRepository;
import com.saivamsi.remaster.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final RemasterLikeRepository remasterLikeRepository;
    private final RemasterPlayRepository remasterPlayRepository;

    public RemasterResponse createRemaster (CreateRemasterRequest remasterRequest, ApplicationUser user) {
//        if (!user.isVerified()) {
//            throw new GlobalException(HttpStatus.BAD_REQUEST, GlobalError.builder().subject("user").message("please verify your account before proceeding").build());
//        }

        Remaster remaster = remasterRepository.save(Remaster.builder().url(remasterRequest.getUrl()).name(remasterRequest.getName()).description(remasterRequest.getDescription())
                .duration(remasterRequest.getDuration()).key(remasterRequest.getKey()).mode(remasterRequest.getMode()).tempo(remasterRequest.getTempo())
                .timeSignature(remasterRequest.getTimeSignature())
                .tuning(remasterRequest.getTuning())
                .totalPlays(0)
                .totalLikes(0)
                .user(user).build()
        );

        user.setTotalRemasters(user.getTotalRemasters() + 1);
        userRepository.save(user);

        return remaster.getRemasterResponse();
    }

    public RemasterResponse updateRemaster (UpdateRemasterRequest remaster, ApplicationUser user) {
        Optional<Remaster> userRemaster = remasterRepository.findByIdAndUserId(UUID.fromString(remaster.getId()), user.getId());

        if (userRemaster.isEmpty()) {
            throw new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build());
        }

        return remasterRepository.save(userRemaster.get().updateRemaster(remaster)).getRemasterResponse();
    }

    public void deleteRemaster (UUID id, ApplicationUser user) {
        remasterRepository.deleteByIdAndUserId(id, user.getId());
        user.setTotalRemasters(user.getTotalRemasters() - 1);
        userRepository.save(user);
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

    public RemasterResponse getRemaster(UUID id, UUID userId) {
        RemasterResponse remaster = remasterRepository.findById(id).orElseThrow(() ->
                new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build())).getRemasterResponse();


        if (userId != null) {
            boolean userHasLiked = remasterLikeRepository.findByRemasterIdAndUserId(id, userId).isPresent();
            remaster.setLikedBySessionUser(userHasLiked);
        }

        return remaster;
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

    public void createOrUpdatePlay(UUID id, UUID userId) {
        Remaster remaster = remasterRepository.findById(id).orElseThrow(() ->
                new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build()));

        if (userId != null) {
            RemasterPlay play;
            Optional<RemasterPlay> existingPlay = remasterPlayRepository.findByRemasterIdAndUserId(id, userId);
            if (existingPlay.isEmpty()) {
                 ApplicationUser user = userRepository.findById(userId).orElseThrow(() ->
                        new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build()));
                play = remasterPlayRepository.save(RemasterPlay.builder().remaster(remaster).count(0).user(user).build());
            } else {
                play = existingPlay.get();
            }
            play = play.incrementCount();
            remasterPlayRepository.save(play);
        }

        remaster = remaster.incrementTotalPlays();
        remasterRepository.save(remaster);
    }

    public void like(UUID id, ApplicationUser user) {
        Remaster remaster = remasterRepository.findById(id).orElseThrow(() ->
                new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build()));

        remasterLikeRepository.save(RemasterLike.builder().remaster(remaster).user(user).build());
        remaster = remaster.incrementTotalLikes();
        remasterRepository.save(remaster);
    }

    public void unlike(UUID id, ApplicationUser user) {
        Remaster remaster = remasterRepository.findById(id).orElseThrow(() ->
                new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("remaster").message("remaster not found").build()));

        remasterLikeRepository.deleteByRemasterIdAndUserId(id, user.getId());
        remaster = remaster.decrementTotalLikes();
        remasterRepository.save(remaster);
    }
}

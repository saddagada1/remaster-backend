package com.saivamsi.remaster.service;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Follow;
import com.saivamsi.remaster.repository.FollowRepository;
import com.saivamsi.remaster.repository.UserRepository;
import com.saivamsi.remaster.request.UpdateUserRequest;
import com.saivamsi.remaster.response.BasicUserResponse;
import com.saivamsi.remaster.response.PageResponse;
import com.saivamsi.remaster.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    public UserResponse getUserByUsername(String username, UUID userId) {
        UserResponse user = userRepository.findByUsername(username).orElseThrow(() ->
                new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build())).getSafeUser();

        if (userId != null) {
            boolean userIsFollowing = followRepository.findByFollowedIdAndFollowerId(user.getId(), userId).isPresent();
            user.setFollowedBySessionUser(userIsFollowing);
        }

        return user;
    }
    public PageResponse<UserResponse> searchUsers(String query, UUID cursor, Integer limit) {
        List<ApplicationUser> users;
        if (cursor == null) {
            users = userRepository.searchUsers(query, limit + 1);
        } else {
            users = userRepository.searchUsersWithCursor(query, cursor, limit + 1);
        }

        UUID next = null;
        if (users.size() > limit) {
            ApplicationUser last = users.removeLast();
            next = last.getId();
        }

        List<UserResponse> userResponses = users.stream()
                .map(ApplicationUser::getSafeUser)
                .toList();

        return new PageResponse<>(next, userResponses);
    }

    public UserResponse updateUser(UpdateUserRequest userUpdate, ApplicationUser user) {
        Optional<ApplicationUser> existingUser;

        if (!userUpdate.getUsername().equals(user.getUsername())) {
            existingUser = userRepository.findByUsername(userUpdate.getUsername().toLowerCase());
            if (existingUser.isPresent()) {
                throw new GlobalException(HttpStatus.CONFLICT, GlobalError.builder().subject("username").message("username in use").build());
            }
        }

        if (!userUpdate.getEmail().equals(user.getEmail())) {
            existingUser = userRepository.findByEmail(userUpdate.getEmail().toLowerCase());
            if (existingUser.isPresent()) {
                throw new GlobalException(HttpStatus.CONFLICT, GlobalError.builder().subject("email").message("email in use").build());
            }
        }

        if (userUpdate.getPassword() != null) {
            userUpdate.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }

        return userRepository.save(user.updateUser(userUpdate)).getSafeUser();
    }

    public String uploadProfilePicture(MultipartFile image, ApplicationUser user) {
        if (user.getImage() != null) {
            s3Service.deleteObject(user.getImage(), true);
        }
        try {
            String key = "profile-pictures/%s/%s".formatted(user.getId(), UUID.randomUUID().toString());
            return s3Service.putObject(key, image.getBytes());
        } catch (IOException e) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalError.builder().subject("profile picture").message("could not upload").build());
        }
    }

    public void deleteProfilePicture(ApplicationUser user) {
        s3Service.deleteObject(user.getImage(), true);
    }

    public void follow(UUID id, ApplicationUser user) {
        ApplicationUser followed = userRepository.findById(id).orElseThrow(() ->
                new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build()));

        followRepository.save(Follow.builder().followed(followed).follower(user).build());
        followed = followed.incrementTotalFollowers();
        user = user.incrementTotalFollowing();
        userRepository.saveAll(List.of(followed, user));
    }

    public void unfollow(UUID id, ApplicationUser user) {
        ApplicationUser followed = userRepository.findById(id).orElseThrow(() ->
                new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build()));

        followRepository.deleteByFollowedIdAndFollowerId(followed.getId(), user.getId());
        followed = followed.decrementTotalFollowers();
        user = user.decrementTotalFollowing();
        userRepository.saveAll(List.of(followed, user));
    }

    public PageResponse<BasicUserResponse> getUserFollowers(ApplicationUser user, UUID cursor, Integer limit) {
        List<Follow> followers;
        if (cursor == null) {
            followers = followRepository.findAllByFollowedId(user.getId(),limit + 1);
        } else {
            followers = followRepository.findAllByFollowedIdAndCursor(user.getId(), cursor,limit + 1);
        }

        UUID next = null;
        if (followers.size() > limit) {
            Follow last = followers.removeLast();
            next = last.getId();
        }

        List<BasicUserResponse> userResponses = followers.stream()
                .map(follow -> follow.getFollower().getBasicUser())
                .toList();

        return new PageResponse<>(next, userResponses);
    }

    public PageResponse<BasicUserResponse> getUserFollowing(ApplicationUser user, UUID cursor, Integer limit) {
        List<Follow> following;
        if (cursor == null) {
            following = followRepository.findAllByFollowerId(user.getId(),limit + 1);
        } else {
            following = followRepository.findAllByFollowerIdAndCursor(user.getId(), cursor,limit + 1);
        }

        UUID next = null;
        if (following.size() > limit) {
            Follow last = following.removeLast();
            next = last.getId();
        }

        List<BasicUserResponse> userResponses = following.stream()
                .map(follow -> follow.getFollowed().getBasicUser())
                .toList();

        return new PageResponse<>(next, userResponses);
    }
}

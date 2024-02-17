package com.saivamsi.remaster.service;

import com.saivamsi.remaster.model.Remaster;
import com.saivamsi.remaster.repository.RemasterRepository;
import com.saivamsi.remaster.utils.ObjectWithCount;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RankingService {

    private static final Integer maxPastValues = 9;
    private final List<Remaster> playsToRank = new ArrayList<>();
    private final List<Remaster> likesToRank = new ArrayList<>();
    private final RemasterRepository remasterRepository;

    public void queuePlay(Remaster remaster) {
        playsToRank.add(remaster);
    }

    public void queueLike(Remaster remaster) {
        likesToRank.add(remaster);
    }

    public void dequeueLike(Remaster remaster) {
        likesToRank.remove(remaster);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void rankPlays() {
        Map<UUID, ObjectWithCount<Remaster>> countedPlays = new HashMap<>();
        for (Remaster remaster : playsToRank) {
            if (remasterRepository.existsById(remaster.getId())) {
                UUID key = remaster.getId();
                ObjectWithCount<Remaster> remasterWithCount = countedPlays.get(key);
                if (remasterWithCount == null) {
                    remasterWithCount = ObjectWithCount.<Remaster>builder().object(remaster).count(1).build();
                } else {
                    remasterWithCount.incrementCount();
                }
                countedPlays.put(key, remasterWithCount);
            }
        }
        for (Map.Entry<UUID, ObjectWithCount<Remaster>> entry : countedPlays.entrySet()) {
            Remaster remaster = entry.getValue().getObject();
            Integer count = entry.getValue().getCount();
            System.out.println("remaster: " + remaster.getRemasterResponse().toString() + ", plays: " + count);
            Float rank = calculateRank(count, remaster.getPastPlayCounts());

            List<Float> pastPlayRanks = remaster.getPastPlayRanks();
            List<Float> updatedPlayRanks = addToPastList(pastPlayRanks, pastPlayRanks.size(), remaster.getPlayRank(), maxPastValues);
            remaster.setPastPlayRanks(updatedPlayRanks);

            List<Integer> pastPlayCounts = remaster.getPastPlayCounts();
            List<Integer> updatedPlayCounts = addToPastList(pastPlayCounts, pastPlayCounts.size(), count, maxPastValues);
            remaster.setPastPlayCounts(updatedPlayCounts);

            remaster.setPlayRank(rank);

            remasterRepository.save(remaster);
        }

        playsToRank.clear();
        System.out.println("Ranked Plays");
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void rankLikes() {
        Map<UUID, ObjectWithCount<Remaster>> countedLikes = new HashMap<>();
        for (Remaster remaster : likesToRank) {
            if (remasterRepository.existsById(remaster.getId())) {
                UUID key = remaster.getId();
                ObjectWithCount<Remaster> remasterWithCount = countedLikes.get(key);
                if (remasterWithCount == null) {
                    remasterWithCount = ObjectWithCount.<Remaster>builder().object(remaster).count(1).build();
                } else {
                    remasterWithCount.incrementCount();
                }
                countedLikes.put(key, remasterWithCount);
            }
        }
        for (Map.Entry<UUID, ObjectWithCount<Remaster>> entry : countedLikes.entrySet()) {
            Remaster remaster = entry.getValue().getObject();
            Integer count = entry.getValue().getCount();
            System.out.println("remaster: " + remaster.getRemasterResponse().toString() + ", likes: " + count);
            Float rank = calculateRank(count, remaster.getPastLikeCounts());

            List<Float> pastLikeRanks = remaster.getPastLikeRanks();
            List<Float> updatedLikeRanks = addToPastList(pastLikeRanks, pastLikeRanks.size(), remaster.getLikeRank(), maxPastValues);
            remaster.setPastLikeRanks(updatedLikeRanks);

            List<Integer> pastLikeCounts = remaster.getPastLikeCounts();
            List<Integer> updatedLikeCounts = addToPastList(pastLikeCounts, pastLikeCounts.size(), count, maxPastValues);
            remaster.setPastLikeCounts(updatedLikeCounts);

            remaster.setLikeRank(rank);

            remasterRepository.save(remaster);
        }

        likesToRank.clear();
        System.out.println("Ranked Likes");
    }

    public <T> List<T> addToPastList(List<T> list, Integer size, T value, Integer maxValues) {
        list = list.subList(Math.max(size - maxValues, 0), size);
        list.add(value);
        return list;
    }

    public Float calculateRank(Integer initial, List<Integer> values) {
        float rank = 0.0F;
        if (!values.isEmpty()) {
            System.out.println("calcRank");
            Integer mean = values.stream().reduce(0, Integer::sum) / values.size();
            List<Float> distances = values.stream().map(value -> (float) Math.pow(Math.abs(value - mean), 2)).toList();
            float quotient = distances.stream().reduce(0.0F, Float::sum);
            if (values.size() > 1) {
                quotient = quotient / values.size() - 1;
            }
            Float deviation = (float) Math.sqrt(quotient);
            if (!deviation.equals(0.0F)) {
                rank = initial - mean / deviation;
            } else {
                rank = (float) initial - mean;
            }
        }
        return rank;
    }
}

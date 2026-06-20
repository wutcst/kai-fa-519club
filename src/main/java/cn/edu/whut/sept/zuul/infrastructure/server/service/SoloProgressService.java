package cn.edu.whut.sept.zuul.infrastructure.server.service;

import org.springframework.stereotype.Service;

import cn.edu.whut.sept.zuul.infrastructure.persistence.DatabaseProvider;
import cn.edu.whut.sept.zuul.infrastructure.persistence.UserLevelProgressRepository;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloLevelOptionDto;
import cn.edu.whut.sept.zuul.infrastructure.server.dto.SoloLevelSelectionDto;
import cn.edu.whut.sept.zuul.level.LevelConfig;

/**
 * 单机关卡解锁与通关进度（绑定登录账号）。
 */
@Service
public class SoloProgressService {

    private final UserLevelProgressRepository progressRepository;

    public SoloProgressService() {
        this.progressRepository = new UserLevelProgressRepository(DatabaseProvider.getDefault());
    }

    public SoloLevelSelectionDto buildLevelSelection(Long userId) {
        int highestCleared = userId == null ? 0 : progressRepository.findHighestClearedLevel(userId);
        int highestUnlocked = resolveHighestUnlocked(highestCleared);

        SoloLevelSelectionDto selection = new SoloLevelSelectionDto();
        for (int level = LevelConfig.MIN_LEVEL; level <= LevelConfig.MAX_LEVEL; level++) {
            LevelConfig config = LevelConfig.forLevel(level);
            SoloLevelOptionDto option = new SoloLevelOptionDto();
            option.setLevelNumber(level);
            option.setTitle(config.getTitle());
            option.setMissionHint(config.getMissionHint());
            option.setUnlocked(level <= highestUnlocked);
            option.setCleared(level <= highestCleared);
            selection.getLevels().add(option);
        }
        return selection;
    }

    public int resolveHighestUnlocked(int highestCleared) {
        if (highestCleared <= 0) {
            return LevelConfig.MIN_LEVEL;
        }
        return Math.min(LevelConfig.MAX_LEVEL, highestCleared + 1);
    }

    public int getHighestUnlockedForUser(Long userId) {
        int highestCleared = userId == null ? 0 : progressRepository.findHighestClearedLevel(userId);
        return resolveHighestUnlocked(highestCleared);
    }

    public void recordLevelCleared(long userId, int clearedLevel) {
        if (clearedLevel < LevelConfig.MIN_LEVEL || clearedLevel > LevelConfig.MAX_LEVEL) {
            return;
        }
        progressRepository.recordLevelCleared(userId, clearedLevel);
    }

    public void assertLevelUnlocked(Long userId, int levelNumber) {
        if (levelNumber < LevelConfig.MIN_LEVEL || levelNumber > LevelConfig.MAX_LEVEL) {
            throw new IllegalArgumentException("关卡号必须在 " + LevelConfig.MIN_LEVEL
                + "—" + LevelConfig.MAX_LEVEL + " 之间");
        }
        int highestUnlocked = getHighestUnlockedForUser(userId);
        if (levelNumber > highestUnlocked) {
            throw new IllegalArgumentException("第 " + levelNumber + " 关尚未解锁，请先通关上一关");
        }
    }
}

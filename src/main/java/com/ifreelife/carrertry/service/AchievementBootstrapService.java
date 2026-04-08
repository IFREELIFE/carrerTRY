package com.ifreelife.carrertry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AchievementBootstrapService implements ApplicationRunner {

    private final MilestoneService milestoneService;

    @Override
    public void run(ApplicationArguments args) {
        milestoneService.ensureAchievementDefinitions();
    }
}

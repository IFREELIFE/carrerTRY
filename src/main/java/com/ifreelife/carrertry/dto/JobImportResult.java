package com.ifreelife.carrertry.dto;

import com.ifreelife.carrertry.entity.JobPosting;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JobImportResult {
    private int importedCount;
    private int skippedDuplicateCount;
    private List<JobPosting> importedJobs;
}

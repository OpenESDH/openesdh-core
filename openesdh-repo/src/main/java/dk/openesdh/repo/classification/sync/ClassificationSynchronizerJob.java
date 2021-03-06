package dk.openesdh.repo.classification.sync;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by syastrov on 6/1/15.
 */
public class ClassificationSynchronizerJob extends AbstractScheduledLockedJob {

    @Override
    public void executeJob(JobExecutionContext executionContext) throws
            JobExecutionException {
        final Boolean syncEnabled = Boolean.parseBoolean((String) executionContext.getJobDetail().getJobDataMap().get("syncEnabled"));
        if (!syncEnabled) {
            return;
        }
        final ClassificationSynchronizer classificationSynchronizer
                = (ClassificationSynchronizer) executionContext
                .getJobDetail().getJobDataMap().get("classificationSynchronizer");
        AuthenticationUtil.runAsSystem(() -> {
            classificationSynchronizer.synchronize();
            return null;
        });
    }
}

package dk.openesdh.repo.classification.sync;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.schedule.AbstractScheduledLockedJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by syastrov on 6/1/15.
 */
public class ClassificationSynchronizerJob extends AbstractScheduledLockedJob {
    @Override
    public void executeJob(JobExecutionContext executionContext) throws
            JobExecutionException {
        final ClassificationSynchronizer classificationSynchronizer =
                (ClassificationSynchronizer) executionContext
                        .getJobDetail().getJobDataMap().get("classificationSynchronizer");
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                classificationSynchronizer.synchronize();
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}

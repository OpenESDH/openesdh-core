package dk.openesdh.repo.classification.sync;

/**
 * Created by syastrov on 6/1/15.
 */
public interface ClassificationSynchronizer {
    public void synchronize();

    public void synchronizeTenant(String tenantDomain);
}

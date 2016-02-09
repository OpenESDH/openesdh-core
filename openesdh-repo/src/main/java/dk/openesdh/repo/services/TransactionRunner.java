package dk.openesdh.repo.services;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;

import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("TransactionRunner")
public class TransactionRunner {

    @Autowired
    @Qualifier("retryingTransactionHelper")
    private RetryingTransactionHelper retryingTransactionHelper;

    public <R> R runInTransaction(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack) {
        return runInTransaction(callBack, false, false);
    }

    public <R> R runInNewTransaction(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack) {
        return runInTransaction(callBack, false, true);
    }

    public <R> R runInNewTransactionAsAdmin(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack) {
        return runAsAdmin(() -> runInTransaction(callBack, false, true));
    }

    private <R> R runInTransaction(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack,
            boolean readOnly, boolean newTransaction) {
        try {
            return retryingTransactionHelper.doInTransaction(callBack, readOnly, newTransaction);
        } catch (Throwable t) {
            try {
                UserTransaction userTrx = RetryingTransactionHelper.getActiveUserTransaction();
                if (userTrx != null && userTrx.getStatus() != javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
                    userTrx.setRollbackOnly();
                }
            } finally {
                //ignore all 'catch' errors and rethrow original
                throw t;
            }
        }
    }

    public <R> R runInTransactionAsAdmin(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack) {
        return runAsAdmin(() -> retryingTransactionHelper.doInTransaction(callBack));
    }

    public <R> R runAsAdmin(AuthenticationUtil.RunAsWork<R> callback) {
        return runAs(callback, getAdminUserName());
    }

}

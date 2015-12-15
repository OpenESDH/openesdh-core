package dk.openesdh.repo.services;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;

public interface RunInTransactionAsAdmin {

    TransactionService getTransactionService();

    default RetryingTransactionHelper getRetryingTransactionHelper() {
        return getTransactionService().getRetryingTransactionHelper();
    }

    default <R> R runInTransaction(RetryingTransactionCallback<R> callBack) {
        try{
            return getRetryingTransactionHelper().doInTransaction(callBack);
        } catch (Throwable t) {
            UserTransaction userTrx = RetryingTransactionHelper.getActiveUserTransaction();
            try {
                if (userTrx != null && userTrx.getStatus() != javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
                    try {
                        userTrx.setRollbackOnly();
                    } catch (Throwable t2) {}
                }
            } catch (SystemException e) {
                e.printStackTrace();
            }
            throw t;
        }
    }

    default <R> R runInTransactionAsAdmin(RetryingTransactionCallback<R> callBack) {
        return runAsAdmin(() -> runInTransaction(callBack));
    }

    default <R> R runAsAdmin(RunAsWork<R> callback) {
        return runAs(callback, getAdminUserName());
    }
}

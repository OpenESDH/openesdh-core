package dk.openesdh.repo.services;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TransactionRunner {

    @Autowired
    @Qualifier("retryingTransactionHelper")
    private RetryingTransactionHelper retryingTransactionHelper;

    public <R> R runInTransaction(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack) {
        return runInTransaction(callBack, false, false);
    }

    public <R> R runInTransaction(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack, boolean readOnly, boolean requiresNew) {
        try {
            return retryingTransactionHelper.doInTransaction(callBack, readOnly, requiresNew);
        } catch (Throwable t) {
            UserTransaction userTrx = RetryingTransactionHelper.getActiveUserTransaction();
            try {
                if (userTrx != null && userTrx.getStatus() != javax.transaction.Status.STATUS_MARKED_ROLLBACK) {
                    try {
                        userTrx.setRollbackOnly();
                    } catch (Throwable t2) {
                    }
                }
            } catch (SystemException e) {
                e.printStackTrace();
            }
            throw t;
        }
    }

    public <R> R runInTransactionAsAdmin(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack) {
        return runAsAdmin(() -> retryingTransactionHelper.doInTransaction(callBack));
    }

    public <R> R runAsAdmin(AuthenticationUtil.RunAsWork<R> callback) {
        return runAs(callback, getAdminUserName());
    }

}

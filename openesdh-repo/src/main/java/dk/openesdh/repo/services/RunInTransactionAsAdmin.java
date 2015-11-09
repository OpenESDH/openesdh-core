package dk.openesdh.repo.services;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;

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
        return getRetryingTransactionHelper().doInTransaction(callBack);
    }

    default <R> R runInTransactionAsAdmin(RetryingTransactionCallback<R> callBack) {
        return runAsAdmin(() -> getRetryingTransactionHelper().doInTransaction(callBack));
    }

    default <R> R runAsAdmin(RunAsWork<R> callback) {
        return runAs(callback, getAdminUserName());
    }
}

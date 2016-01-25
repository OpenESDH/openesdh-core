package dk.openesdh.repo.helper;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAs;

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
        return retryingTransactionHelper.doInTransaction(callBack);
    }

    public <R> R runInTransactionAsAdmin(RetryingTransactionHelper.RetryingTransactionCallback<R> callBack) {
        return runAsAdmin(() -> retryingTransactionHelper.doInTransaction(callBack));
    }

    public <R> R runAsAdmin(AuthenticationUtil.RunAsWork<R> callback) {
        return runAs(callback, getAdminUserName());
    }

}

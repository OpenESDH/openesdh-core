package dk.openesdh.repo.services.authorities;

import dk.openesdh.repo.exceptions.DomainException;

public class UserEmailExistsDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    public UserEmailExistsDomainException() {
        super(UsersService.ERROR_EMAIL_EXISTS);
    }

}

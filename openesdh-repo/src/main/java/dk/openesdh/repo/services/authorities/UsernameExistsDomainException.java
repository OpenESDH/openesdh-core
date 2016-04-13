package dk.openesdh.repo.services.authorities;

import dk.openesdh.repo.exceptions.DomainException;

public class UsernameExistsDomainException extends DomainException {

    private static final long serialVersionUID = 1L;

    public UsernameExistsDomainException() {
        super(UsersService.ERROR_USERNAME_EXISTS);
    }

}

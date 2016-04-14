package dk.openesdh.repo.services.activities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.openesdh.repo.services.actions.OpeneActionServiceAspect;

/**
 * This is a fix for the bug in the alfresco org.alfresco.repo.activities.feed.EmailUserNotifier 
 * The notifier sets recipients email address as PARAM_TO and doesn't provide recipient's locale preference which
 * results in email messages sent in improper language.
 * 
 * The MailActionExecuter determines recipients locale from preferences if only username is provided as value for the PARAM_TO.
 * 
 * @author rudinjur
 *
 */
@Service("ActivitiesFeedNotifierMailActionAspect")
public class ActivitiesFeedNotifierMailActionAspect {

    private static final String MSG_EMAIL_SUBJECT = "activities.feed.notifier.email.subject";

    @Autowired
    private PersonService personService;
    @Autowired
    private PreferenceService preferenceService;
    
    @Autowired
    @Qualifier("OpeneActionServiceAspect")
    private OpeneActionServiceAspect openeActionServiceAspect;

    @PostConstruct
    public void init(){
        Predicate<Action> predicate = action -> MSG_EMAIL_SUBJECT.equals(action.getParameterValue(MailActionExecuter.PARAM_SUBJECT));
        openeActionServiceAspect.addBeforeActionInterceptor(predicate, this::beforeExecuteAction);
    }

    private void beforeExecuteAction(Action mail) {
        String recipientEmail = (String) mail.getParameterValue(MailActionExecuter.PARAM_TO);
        getUserPreferencesLocale(recipientEmail).ifPresent(
                locale -> mail.setParameterValue(MailActionExecuter.PARAM_LOCALE, locale));
    }

    private Optional<Locale> getUserPreferencesLocale(String userEmail) {
        PersonInfo person = personService
                .getPeople(userEmail, Arrays.asList(ContentModel.PROP_EMAIL), null, new PagingRequest(1))
                .getPage()
                .stream()
                .findAny()
                .get();
        Map<String, Serializable> preferences = preferenceService.getPreferences(person.getUserName());
        return Optional.ofNullable(preferences.get("locale"))
                .map(Object::toString)
                .map(StringUtils::parseLocaleString);
    }

}

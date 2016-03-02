package dk.openesdh.repo.services.i18n;

import java.util.Arrays;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;

/**
 * Initializing OpenE extra locales before loading message bundles to prevent
 * overriding with standard Alfresco messages.
 * 
 * @author rudinjur
 *
 */
@Component("OpeneLocalesInitializer")
public class OpeneLocalesInitializer {
    
    @Value("${opene.extra.locales}")
    private String openeExtraLocales;

    @PostConstruct
    public void setOpeneExtraLocales() {
        if(StringUtils.isEmpty(openeExtraLocales)){
            return;
        }
        Arrays.asList(openeExtraLocales.split(","))
            .stream()
            .map(String::trim)
            .map(Locale::new)
            .forEach(I18NUtil::getAllMessages);
    }
}

package dk.openesdh.repo.services;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Initializing OpenE extra locales before loading message bundles to prevent
 * overriding with standard Alfresco messages.
 * 
 * @author rudinjur
 *
 */
public class OpeneLocalsInitializer {

    public void setOpeneExtraLocales(String sExtraLocales) {
        if(StringUtils.isEmpty(sExtraLocales)){
            return;
        }
        Arrays.asList(sExtraLocales.split(","))
                .stream()
                .map(String::trim)
                .map(Locale::new)
                .map(I18NUtil::getAllMessages);
    }

}

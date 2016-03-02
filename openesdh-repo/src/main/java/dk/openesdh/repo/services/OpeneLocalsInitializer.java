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
    
    private String openeExtraLocales;
    
    public void init(){
        if(StringUtils.isEmpty(openeExtraLocales)){
            return;
        }
        Arrays.asList(openeExtraLocales.split(","))
            .stream()
            .map(String::trim)
            .map(Locale::new)
            .map(I18NUtil::getAllMessages);
    }

    public void setOpeneExtraLocales(String openeExtraLocales) {
        this.openeExtraLocales = openeExtraLocales;
    }
}

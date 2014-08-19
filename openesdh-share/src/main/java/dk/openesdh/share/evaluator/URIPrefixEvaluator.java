package dk.openesdh.share.evaluator;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.ExtensionModuleEvaluator;

/**
 * Evaluates to true if the URI of the context starts with the given "uriPrefix" property
 * @author Seth Yastrov
 */
public class URIPrefixEvaluator implements ExtensionModuleEvaluator
{
    
    public boolean applyModule(RequestContext context, Map<String, String> evaluationProperties)
     {
         boolean apply = false;
         
         String uriPrefix = evaluationProperties.get("uriPrefix");
         if (uriPrefix != null) {
            if ( context.getPage() != null )
            {
                
                String uri = context.getUri();
                if (uri.startsWith(uriPrefix))
                {
                   apply = true;
                }             
            }
         }
         
         return apply;
         
     }

     public String[] getRequiredProperties()
     {
         return new String[] {"uriPrefix"};
     }


}



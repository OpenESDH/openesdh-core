package dk.openesdh.repo.webscripts.activities;

import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.rest.api.impl.Util;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

import dk.openesdh.repo.services.activities.CaseActivityService;
import dk.openesdh.repo.webscripts.utils.WebScriptUtils;

@Component
@WebScript(description = "Retrieves information about case activities (notifications)", families = "Case Activities")
public class CaseActivityWebScript {

    @Autowired
    @Qualifier("CaseActivityService")
    private CaseActivityService caseActivityService;

    @Uri(value = "/api/openesdh/activities/feed", method = HttpMethod.GET, defaultFormat = "json")
    public Resolution getCurrentUserActivities(
            @RequestParam(required = false, defaultValue = "0") final Integer skipCount,
            @RequestParam(required = false, defaultValue = "" + Paging.DEFAULT_MAX_ITEMS) final Integer maxItems) {
        Paging paging = Paging.valueOf(skipCount, maxItems);
        PagingResults<ActivityFeedEntity> activities = caseActivityService.getCurrentUserActivities(Util
                .getPagingRequest(paging));
        return WebScriptUtils.jsonResolution(activities.getPage());
    }
}

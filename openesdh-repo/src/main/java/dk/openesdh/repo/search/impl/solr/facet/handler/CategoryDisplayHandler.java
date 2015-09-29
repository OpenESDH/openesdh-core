/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package dk.openesdh.repo.search.impl.solr.facet.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.solr.facet.handler.AbstractFacetLabelDisplayHandler;
import org.alfresco.repo.search.impl.solr.facet.handler.FacetLabel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A simple handler to get the Category display label based on name and title.
 *
 * @author Seth Yastrov
 */
public class CategoryDisplayHandler extends AbstractFacetLabelDisplayHandler
{

    public CategoryDisplayHandler(Set<String> supportedFieldFacets)
    {
        ParameterCheck.mandatory("supportedFieldFacets", supportedFieldFacets);

        this.supportedFieldFacets = Collections.unmodifiableSet(new HashSet<>(supportedFieldFacets));
    }

    @Override
    public FacetLabel getDisplayLabel(String value)
    {
        NodeRef nodeRef = new NodeRef(value);
        NodeService nodeService = serviceRegistry.getNodeService();
        String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        String title = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
        String displayName = name + (title != null ? " " + title : "");
        return new FacetLabel(value, displayName, -1);
    }
}
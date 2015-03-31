/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.api;

import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;

import java.util.Set;

/**
 * APIDefinition is responsible for providing uri templates, scopes and
 * save the api definition according to the permission and visibility
 */
public abstract class APIDefinition {

    /**
     * This method extracts the URI templates from the API definition
     *
     * @return URI templates
     */
    public abstract Set<URITemplate> getURITemplatesFromDefinition(APIIdentifier apiIdentifier, String resourceConfigsJSON, API api, APIProvider apiProvider) throws APIManagementException;

    /**
     * This method extracts the scopes from the API definition
     *
     * @return scopes
     */
    public abstract Set<Scope> getScopeFromDefinition(String resourceConfigsJSON) throws APIManagementException;

    /**
     * This method saves the API definition
     *
     * @return status of the save operation
     */
    public abstract void saveAPIDefinition(APIIdentifier apiIdentifier, String resourceConfigsJSON, API api, APIProvider apiProvider) throws ParseException, APIManagementException;

}

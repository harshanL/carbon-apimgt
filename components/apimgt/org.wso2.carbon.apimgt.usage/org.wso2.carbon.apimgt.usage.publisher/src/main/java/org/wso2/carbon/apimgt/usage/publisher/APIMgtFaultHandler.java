package org.wso2.carbon.apimgt.usage.publisher;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.URL;

public class APIMgtFaultHandler extends AbstractMediator {

    private boolean enabled;

    private boolean skipEventReceiverConnection;

    private volatile APIMgtUsageDataPublisher publisher;

    public APIMgtFaultHandler() {
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            this.initializeDataPublisher();
        }
    }

    private void initializeDataPublisher() {

        enabled = DataPublisherUtil.getApiManagerAnalyticsConfiguration().isAnalyticsEnabled();
        skipEventReceiverConnection = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();
        if (!enabled || skipEventReceiverConnection) {
            return;
        }
        if (publisher == null) {
            synchronized (this) {
                if (publisher == null) {
                    String publisherClass = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                            getPublisherClass();
                    try {
                        log.debug("Instantiating Data Publisher");
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                                setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        publisher = (APIMgtUsageDataPublisher) Class.forName(publisherClass).newInstance();
                        publisher.init();
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + publisherClass);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + publisherClass);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + publisherClass);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        }
    }


    public boolean mediate(MessageContext messageContext) {

        if (publisher == null) {
            this.initializeDataPublisher();
        }
        try {
            if (!enabled || skipEventReceiverConnection) {
                return true;
            }
            long requestTime = Long.parseLong((String) messageContext.getProperty(APIMgtUsagePublisherConstants.
                                                                         REQUEST_START_TIME));

            FaultPublisherDTO faultPublisherDTO = new FaultPublisherDTO();
            faultPublisherDTO.setConsumerKey((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.CONSUMER_KEY));
            faultPublisherDTO.setContext((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.CONTEXT));
            faultPublisherDTO.setApi_version((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.API_VERSION));
            faultPublisherDTO.setApi((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.API));
            faultPublisherDTO.setResourcePath((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.RESOURCE));
            faultPublisherDTO.setMethod((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.HTTP_METHOD));
            faultPublisherDTO.setVersion((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.VERSION));
            faultPublisherDTO.setErrorCode(String.valueOf(messageContext.getProperty(
                    SynapseConstants.ERROR_CODE)));
            faultPublisherDTO.setErrorMessage((String) messageContext.getProperty(
                    SynapseConstants.ERROR_MESSAGE));
            faultPublisherDTO.setRequestTime(requestTime);
            faultPublisherDTO.setUsername((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.USER_ID));
            faultPublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(
                    faultPublisherDTO.getUsername()));
            faultPublisherDTO.setHostName((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.HOST_NAME));
            faultPublisherDTO.setApiPublisher((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.API_PUBLISHER));
            faultPublisherDTO.setApplicationName((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.APPLICATION_NAME));
            faultPublisherDTO.setApplicationId((String) messageContext.getProperty(
                    APIMgtUsagePublisherConstants.APPLICATION_ID));
            String url = (String) messageContext.getProperty(
                    RESTConstants.REST_URL_PREFIX);
            URL apiurl = new URL(url);
            int port = apiurl.getPort();
            String protocol = messageContext.getProperty(
                    SynapseConstants.TRANSPORT_IN_NAME) + "-" + port;
            faultPublisherDTO.setProtocol(protocol);

            publisher.publishEvent(faultPublisherDTO);

        } catch (Throwable e) {
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware() {
        return false;
    }
}

package com.adobe.aem.people.mboucher.impl;

import java.util.Dictionary;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.security.Authorizable;
import com.day.cq.security.User;
import com.day.cq.security.NoSuchAuthorizableException;
import com.day.cq.security.UserManager;
import com.day.cq.security.UserManagerFactory;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.ReferencePolicy;


@Component(label="Anonymous Reset",description="Resets anonymous user password",immediate=true, metatype=true)
public class AnonymousResetComponent {

    private static final Logger log = LoggerFactory.getLogger(AnonymousResetComponent.class);

    @Property(boolValue=true, propertyPrivate=true)
    private static final String SAVE_FLAG_PROPERTY =
            "save.onError";

    @Property(label = "Default Password", description = "The default password used for reset.", value="anonymous")
    private static final String DEFAULT_PASSWORD =
            "default.password";

    @Reference(policy=ReferencePolicy.STATIC)
    private SlingRepository repository;

    @Reference
    private UserManagerFactory userManagerFactory;

    @Activate
    protected void activate(org.osgi.service.component.ComponentContext context) {
        log.info("Activating anonymous Reset.");

        final Dictionary<String, Object> props = context.getProperties();
        try{
            Session s = createSession();
            User user = (User)getAuthorizable("anonymous", s);
            user.changePassword(DEFAULT_PASSWORD);
            s.save();
            log.info("Successfully reset anonymous password");
        } catch (Exception e) {
            log.error("Unable to reset anonymous password", e);
        }
    }

    /**
     * Deactivates the service.
     *
     * @param componentContext The component context
     */
    protected void deactivate(org.osgi.service.component.ComponentContext componentContext) {
        log.info("Anonymous Reset shut down");
    }

    private Session createSession() throws RepositoryException {
        return this.repository.loginAdministrative(null);
    }



    private Authorizable getAuthorizable(String authId, Session session) {
        UserManager manager = getUserManager(session);
        if (null != manager && null != authId) {
            try {
                if ("system".equals(authId)) {
                    authId = "anonymous";
                }
                return manager.get(authId);
            } catch (NoSuchAuthorizableException e) {
                log.warn("user manager did not find user {}", authId);
            }
        } else {
            log.warn("user manager or user id unavailable: {} - {}", manager, authId);
        }
        return null;
    }

    private UserManager getUserManager(Session session) {
        try {
            return userManagerFactory.createUserManager(session);
        } catch (AccessDeniedException e) {
            log.error("could not get user manager: ", e);
        }
        return null;
    }

}

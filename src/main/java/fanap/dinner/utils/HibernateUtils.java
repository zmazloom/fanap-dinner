package fanap.dinner.utils;

import fanap.dinner.exception.InternalServerException;
import fanap.dinner.exception.LogUtils;
import fanap.dinner.exception.Subject;
import fanap.dinner.message.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

/**
 * The class for util methods around Hibernate.
 */

@Slf4j
public class HibernateUtils {

    private HibernateUtils() {
    }

    /**
     * Get currentSession or throw {@link InternalServerException#getInstance()} with message {@link ErrorMessage#errorInternalServer()}
     * if anything went wrong.
     * <p>
     * This method catches {@link HibernateException} logs it and throws an {@link InternalServerException} with proper message.
     * You can use this method in your {@link org.springframework.transaction.annotation.Transactional} methods
     * in {@link org.springframework.stereotype.Repository} classes (CRUDs). When your try-catch code is in methods with
     * {@link Session} parameters.
     * <p>
     * Can be used for cleaner and shorter codes.
     * <p>
     * Notice: please call me from a {@link org.springframework.transaction.annotation.Transactional} situation!
     *
     * @param hibernate    your session factory
     * @param errorSubject used for logging {@link HibernateException}
     * @param errorText    used for logging {@link HibernateException}
     * @return session
     * @throws InternalServerException if a {@link HibernateException} occures in {@link SessionFactory#getCurrentSession()}
     */
    @NotNull
    public static Session getCurrentSessionOrThrowError(@NotNull SessionFactory hibernate, @Nullable Subject errorSubject, @NotNull String errorText) throws InternalServerException {
        Session session;
        try {
            session = hibernate.getCurrentSession();
        } catch (HibernateException ex) {
            LogUtils.error(log, ex, errorText, errorSubject);
            throw InternalServerException.getInstance(ErrorMessage.errorInternalServer());
        }
        return session;
    }
}

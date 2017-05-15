package zz.demo;

import org.springframework.context.ApplicationEvent;

/**
 * Created by zulk on 17.02.17.
 */
public class AuditEvent extends ApplicationEvent {
    public AuditEvent(Object source) {
        super(source);
    }
}

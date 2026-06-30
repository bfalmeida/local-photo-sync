package org.springframework.shell.boot;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link CompleterAutoConfiguration}.
 */
@Generated
public class CompleterAutoConfiguration__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static CompleterAutoConfiguration apply(RegisteredBean registeredBean,
      CompleterAutoConfiguration instance) {
    AutowiredFieldValueResolver.forRequiredField("shell").resolveAndSet(registeredBean, instance);
    return instance;
  }
}

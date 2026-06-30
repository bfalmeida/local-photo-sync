package com.github.bfalmeida.photosync.service;

import java.lang.String;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Bean definitions for {@link ValkeyStateService}.
 */
@Generated
public class ValkeyStateService__TestContext002_BeanDefinitions {
  /**
   * Get the bean instance supplier for 'valkeyStateService'.
   */
  private static BeanInstanceSupplier<ValkeyStateService> getValkeyStateServiceInstanceSupplier() {
    return BeanInstanceSupplier.<ValkeyStateService>forConstructor(StringRedisTemplate.class, String.class, int.class)
            .withGenerator((registeredBean, args) -> new ValkeyStateService(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'valkeyStateService'.
   */
  public static BeanDefinition getValkeyStateServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ValkeyStateService.class);
    beanDefinition.setInstanceSupplier(getValkeyStateServiceInstanceSupplier());
    return beanDefinition;
  }
}

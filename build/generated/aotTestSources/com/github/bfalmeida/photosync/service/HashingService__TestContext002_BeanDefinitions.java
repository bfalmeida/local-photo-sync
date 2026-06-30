package com.github.bfalmeida.photosync.service;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link HashingService}.
 */
@Generated
public class HashingService__TestContext002_BeanDefinitions {
  /**
   * Get the bean definition for 'hashingService'.
   */
  public static BeanDefinition getHashingServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(HashingService.class);
    beanDefinition.setInstanceSupplier(HashingService::new);
    return beanDefinition;
  }
}

package org.springframework.boot.autoconfigure.transaction;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link TransactionAutoConfiguration}.
 */
@Generated
public class TransactionAutoConfiguration__BeanDefinitions {
  /**
   * Get the bean definition for 'transactionAutoConfiguration'.
   */
  public static BeanDefinition getTransactionAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TransactionAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(TransactionAutoConfiguration::new);
    return beanDefinition;
  }
}

package org.springframework.shell.boot;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SpringShellProperties}.
 */
@Generated
public class SpringShellProperties__BeanDefinitions {
  /**
   * Get the bean definition for 'springShellProperties'.
   */
  public static BeanDefinition getSpringShellPropertiesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SpringShellProperties.class);
    beanDefinition.setInstanceSupplier(SpringShellProperties::new);
    return beanDefinition;
  }
}

package org.springframework.shell.boot;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link CompleterAutoConfiguration}.
 */
@Generated
public class CompleterAutoConfiguration__TestContext001_BeanDefinitions {
  /**
   * Get the bean definition for 'completerAutoConfiguration'.
   */
  public static BeanDefinition getCompleterAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompleterAutoConfiguration.class);
    InstanceSupplier<CompleterAutoConfiguration> instanceSupplier = InstanceSupplier.using(CompleterAutoConfiguration::new);
    instanceSupplier = instanceSupplier.andThen(CompleterAutoConfiguration__TestContext001_Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'completer'.
   */
  private static BeanInstanceSupplier<CompleterAutoConfiguration.CompleterAdapter> getCompleterInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<CompleterAutoConfiguration.CompleterAdapter>forFactoryMethod(CompleterAutoConfiguration.class, "completer")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean(CompleterAutoConfiguration.class).completer());
  }

  /**
   * Get the bean definition for 'completer'.
   */
  public static BeanDefinition getCompleterBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CompleterAutoConfiguration.CompleterAdapter.class);
    beanDefinition.setInstanceSupplier(getCompleterInstanceSupplier());
    return beanDefinition;
  }
}

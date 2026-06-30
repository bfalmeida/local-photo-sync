package com.github.bfalmeida.photosync;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;

/**
 * Bean definitions for {@link Main}.
 */
@Generated
public class Main__TestContext001_BeanDefinitions {
  /**
   * Get the bean instance supplier for 'main'.
   */
  private static BeanInstanceSupplier<Main> getMainInstanceSupplier() {
    return BeanInstanceSupplier.<Main>forConstructor(ApplicationContext.class)
            .withGenerator((registeredBean, args) -> new Main(args.get(0)));
  }

  /**
   * Get the bean definition for 'main'.
   */
  public static BeanDefinition getMainBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(Main.class);
    beanDefinition.setInstanceSupplier(getMainInstanceSupplier());
    return beanDefinition;
  }
}

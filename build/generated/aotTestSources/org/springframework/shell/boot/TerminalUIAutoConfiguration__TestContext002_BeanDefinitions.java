package org.springframework.shell.boot;

import org.jline.terminal.Terminal;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.shell.component.ViewComponentBuilder;
import org.springframework.shell.component.ViewComponentExecutor;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.style.ThemeActive;
import org.springframework.shell.style.ThemeResolver;

/**
 * Bean definitions for {@link TerminalUIAutoConfiguration}.
 */
@Generated
public class TerminalUIAutoConfiguration__TestContext002_BeanDefinitions {
  /**
   * Get the bean definition for 'terminalUIAutoConfiguration'.
   */
  public static BeanDefinition getTerminalUIAutoConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TerminalUIAutoConfiguration.class);
    beanDefinition.setInstanceSupplier(TerminalUIAutoConfiguration::new);
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'terminalUIBuilder'.
   */
  private static BeanInstanceSupplier<TerminalUIBuilder> getTerminalUIBuilderInstanceSupplier() {
    return BeanInstanceSupplier.<TerminalUIBuilder>forFactoryMethod(TerminalUIAutoConfiguration.class, "terminalUIBuilder", Terminal.class, ThemeResolver.class, ThemeActive.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean(TerminalUIAutoConfiguration.class).terminalUIBuilder(args.get(0), args.get(1), args.get(2), args.get(3)));
  }

  /**
   * Get the bean definition for 'terminalUIBuilder'.
   */
  public static BeanDefinition getTerminalUIBuilderBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(TerminalUIBuilder.class);
    beanDefinition.setScope("prototype");
    beanDefinition.setInstanceSupplier(getTerminalUIBuilderInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'viewComponentBuilder'.
   */
  private static BeanInstanceSupplier<ViewComponentBuilder> getViewComponentBuilderInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ViewComponentBuilder>forFactoryMethod(TerminalUIAutoConfiguration.class, "viewComponentBuilder", TerminalUIBuilder.class, ViewComponentExecutor.class, Terminal.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean(TerminalUIAutoConfiguration.class).viewComponentBuilder(args.get(0), args.get(1), args.get(2)));
  }

  /**
   * Get the bean definition for 'viewComponentBuilder'.
   */
  public static BeanDefinition getViewComponentBuilderBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ViewComponentBuilder.class);
    beanDefinition.setScope("prototype");
    beanDefinition.setInstanceSupplier(getViewComponentBuilderInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'viewComponentExecutor'.
   */
  private static BeanInstanceSupplier<ViewComponentExecutor> getViewComponentExecutorInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ViewComponentExecutor>forFactoryMethod(TerminalUIAutoConfiguration.class, "viewComponentExecutor")
            .withGenerator((registeredBean) -> registeredBean.getBeanFactory().getBean(TerminalUIAutoConfiguration.class).viewComponentExecutor());
  }

  /**
   * Get the bean definition for 'viewComponentExecutor'.
   */
  public static BeanDefinition getViewComponentExecutorBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ViewComponentExecutor.class);
    beanDefinition.setDestroyMethodNames("close");
    beanDefinition.setInstanceSupplier(getViewComponentExecutorInstanceSupplier());
    return beanDefinition;
  }
}

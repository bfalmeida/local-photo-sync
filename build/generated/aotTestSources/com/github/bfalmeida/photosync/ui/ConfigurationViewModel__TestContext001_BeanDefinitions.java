package com.github.bfalmeida.photosync.ui;

import com.github.bfalmeida.photosync.service.SyncService;
import com.github.bfalmeida.photosync.service.ValkeyStateService;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ConfigurationViewModel}.
 */
@Generated
public class ConfigurationViewModel__TestContext001_BeanDefinitions {
  /**
   * Get the bean instance supplier for 'configurationViewModel'.
   */
  private static BeanInstanceSupplier<ConfigurationViewModel> getConfigurationViewModelInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<ConfigurationViewModel>forConstructor(ValkeyStateService.class, SyncService.class)
            .withGenerator((registeredBean, args) -> new ConfigurationViewModel(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'configurationViewModel'.
   */
  public static BeanDefinition getConfigurationViewModelBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ConfigurationViewModel.class);
    beanDefinition.setInstanceSupplier(getConfigurationViewModelInstanceSupplier());
    return beanDefinition;
  }
}

package org.springframework.boot.autoconfigure.data.redis;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Bean definitions for {@link LettuceConnectionConfiguration}.
 */
@Generated
public class LettuceConnectionConfiguration__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration'.
   */
  private static BeanInstanceSupplier<LettuceConnectionConfiguration> getLettuceConnectionConfigurationInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<LettuceConnectionConfiguration>forConstructor(RedisProperties.class, ObjectProvider.class, ObjectProvider.class, ObjectProvider.class, RedisConnectionDetails.class, ObjectProvider.class)
            .withGenerator((registeredBean, args) -> new LettuceConnectionConfiguration(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5)));
  }

  /**
   * Get the bean definition for 'lettuceConnectionConfiguration'.
   */
  public static BeanDefinition getLettuceConnectionConfigurationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(LettuceConnectionConfiguration.class);
    beanDefinition.setInstanceSupplier(getLettuceConnectionConfigurationInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'lettuceClientResources'.
   */
  private static BeanInstanceSupplier<DefaultClientResources> getLettuceClientResourcesInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<DefaultClientResources>forFactoryMethod(LettuceConnectionConfiguration.class, "lettuceClientResources", ObjectProvider.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean(LettuceConnectionConfiguration.class).lettuceClientResources(args.get(0)));
  }

  /**
   * Get the bean definition for 'lettuceClientResources'.
   */
  public static BeanDefinition getLettuceClientResourcesBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DefaultClientResources.class);
    beanDefinition.setDestroyMethodNames("shutdown");
    beanDefinition.setInstanceSupplier(getLettuceClientResourcesInstanceSupplier());
    return beanDefinition;
  }

  /**
   * Get the bean instance supplier for 'redisConnectionFactory'.
   */
  private static BeanInstanceSupplier<LettuceConnectionFactory> getRedisConnectionFactoryInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<LettuceConnectionFactory>forFactoryMethod(LettuceConnectionConfiguration.class, "redisConnectionFactory", ObjectProvider.class, ClientResources.class)
            .withGenerator((registeredBean, args) -> registeredBean.getBeanFactory().getBean(LettuceConnectionConfiguration.class).redisConnectionFactory(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'redisConnectionFactory'.
   */
  public static BeanDefinition getRedisConnectionFactoryBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(LettuceConnectionFactory.class);
    beanDefinition.setInstanceSupplier(getRedisConnectionFactoryInstanceSupplier());
    return beanDefinition;
  }
}

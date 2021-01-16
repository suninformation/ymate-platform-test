 /*
  * Copyright 2007-2021 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.ymate.platform.test;

 import net.ymate.platform.commons.util.RuntimeUtils;
 import net.ymate.platform.core.IApplication;
 import net.ymate.platform.core.IApplicationInitializer;
 import net.ymate.platform.core.YMP;
 import net.ymate.platform.core.beans.BeanMeta;
 import net.ymate.platform.core.beans.IBeanFactory;
 import org.junit.runner.Runner;
 import org.junit.runners.Suite;
 import org.junit.runners.model.InitializationError;
 import org.junit.runners.model.RunnerBuilder;

 import java.util.Arrays;

 /**
  * @author 刘镇 (suninformation@163.com) on 2021/01/16 23:54
  * @since 1.0.0
  */
 public class YMPJUnit4Suite extends Suite {

     private static Class<?>[] getAnnotatedClasses(Class<?> klass) throws InitializationError {
         SuiteClasses annotation = klass.getAnnotation(SuiteClasses.class);
         if (annotation == null) {
             throw new InitializationError(String.format("class '%s' must have a SuiteClasses annotation", klass.getName()));
         }
         return annotation.value();
     }

     public YMPJUnit4Suite(Class<?> klass) throws InitializationError {
         super(klass, new InnerRunnerBuilder(klass, getAnnotatedClasses(klass)));
     }

     public static class InnerRunnerBuilder extends RunnerBuilder {

         private final IApplication application;

         public InnerRunnerBuilder(Class<?> klass, Class<?>[] suiteClasses) throws InitializationError {
             try {
                 System.setProperty(IApplication.SYSTEM_MAIN_CLASS, klass.getName());
                 //
                 application = YMP.run(new IApplicationInitializer() {
                     @Override
                     public void beforeBeanFactoryInit(IApplication application, IBeanFactory beanFactory) {
                         beanFactory.registerBean(BeanMeta.create(klass, true));
                         Arrays.stream(suiteClasses).map(c -> BeanMeta.create(c, true)).forEach(beanFactory::registerBean);
                     }
                 });
             } catch (Exception e) {
                 throw new InitializationError(RuntimeUtils.unwrapThrow(e));
             }
         }

         @Override
         public Runner runnerForClass(Class<?> testClass) throws Throwable {
             return new YMPJUnit4ClassRunner(application, testClass);
         }
     }
 }

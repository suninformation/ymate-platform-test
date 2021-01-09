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
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.InitializationError;

 /**
  * @author 刘镇 (suninformation@163.com) on 2021/01/04 23:00
  * @since 1.0.0
  */
 public class YMPJUnit4ClassRunner extends BlockJUnit4ClassRunner {

     private final Class<?> targetClass;

     private final IApplication application;

     public YMPJUnit4ClassRunner(Class<?> testClass) throws InitializationError {
         super(testClass);
         this.targetClass = testClass;
         try {
             System.setProperty(IApplication.SYSTEM_MAIN_CLASS, testClass.getName());
             //
             application = YMP.run(new IApplicationInitializer() {
                 @Override
                 public void beforeBeanFactoryInit(IApplication application, IBeanFactory beanFactory) {
                     beanFactory.registerBean(BeanMeta.create(testClass, true));
                 }
             });
         } catch (Exception e) {
             throw new InitializationError(RuntimeUtils.unwrapThrow(e));
         }
     }

     @Override
     public Object createTest() throws Exception {
         return application.getBeanFactory().getBean(targetClass);
     }
 }

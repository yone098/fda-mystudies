/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan({"com.fdahpstudydesigner.dao"})
public class HibernateTestConfig {

  @Bean(name = "dataSource")
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:testdb;IFEXISTS=FALSE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    dataSource.setUsername("mockit");
    dataSource.setPassword("password");
    return dataSource;
  }

  @Bean(name = "entityManagerFactory")
  public LocalSessionFactoryBean sessionFactory() {
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(dataSource());
    sessionFactory.setPackagesToScan(new String[] {"com.fdahpstudydesigner.bo"});
    Properties hibernateProperties = new Properties();
    hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    hibernateProperties.put("hibernate.show_sql", true);
    hibernateProperties.put("hibernate.hbm2ddl.auto", "create-drop");
    hibernateProperties.put("hibernate.hbm2ddl.import_files", "import_data.sql");
    // hibernateProperties.put("packagesToScan", "com.fdahpstudydesigner.bo");
    hibernateProperties.put("hibernate.current_session_context_class", "thread");
    sessionFactory.setHibernateProperties(hibernateProperties);

    return sessionFactory;
  }

  @Bean
  public HibernateTransactionManager transactionManager() {
    HibernateTransactionManager transactionManager = new HibernateTransactionManager();
    transactionManager.setSessionFactory(sessionFactory().getObject());
    return transactionManager;
  }
}

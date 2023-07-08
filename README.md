# YMATE-PLATFORM-TEST

[![Maven Central status](https://img.shields.io/maven-central/v/net.ymate.platform/ymate-platform-test.svg)](https://search.maven.org/artifact/net.ymate.module/ymate-module-captcha)
[![LICENSE](https://img.shields.io/github/license/suninformation/ymate-module-captcha.svg)](https://gitee.com/suninformation/ymate-platform-test/blob/master/LICENSE)


为 YMP 框架集成 JUnit 测试开发工具包。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-test</artifactId>
    <version>1.0.1</version>
</dependency>
```




## 示例代码

### 示例一：模拟控制器方法请求

```java
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class LoginControllerTest {

    @Inject
    private WebMVC webmvc;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLogin() throws Exception {
        MockHttpServletResponse response = MockWebRequestHelper.create(webmvc)
            .post("/login")
            .parameter("uname", "admin")
            .parameter("passwd", DigestUtils.md5Hex("admin"))
            .parameter("format", "json")
            .doFilter();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        JsonWrapper jsonWrapper = JsonWrapper.fromJson(response.getContentAsString());
        Assert.assertNotNull(jsonWrapper);
        System.out.println(jsonWrapper.getAsJsonObject().toString(true, true));
    }
}
```



### 示例二：存储器接口方法调用

```java
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class SystemConfigRepositoryTest {

    @Inject
    private JDBC database;

    @Inject
    private ISystemConfigRepository repository;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testQuerySystemConfigs() throws Exception {
        SystemConfigBean systemConfigBean = SystemConfigBean.builder()
            .siteId("ymate.net")
            .build();
        IResultSet<SystemConfigVO> systemConfigs = repository.querySystemConfigs(database, systemConfigBean, Page.create());
        Assert.assertNotNull(systemConfigs);
    }
}
```



### 示例三： 组合单元测试

```java
@RunWith(YMPJUnit4Suite.class)
@Suite.SuiteClasses({
    LoginControllerTest.class,
    SystemConfigRepositoryTest.class
})
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class ControllersTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
}
```





## One More Thing

YMP 不仅提供便捷的 Web 及其它 Java 项目的快速开发体验，也将不断提供更多丰富的项目实践经验。

感兴趣的小伙伴儿们可以加入官方 QQ 群：[480374360](https://qm.qq.com/cgi-bin/qm/qr?k=3KSXbRoridGeFxTVA8HZzyhwU_btZQJ2)，一起交流学习，帮助 YMP 成长！

如果喜欢 YMP，希望得到你的支持和鼓励！

![Donation Code](https://ymate.net/img/donation_code.png)

了解更多有关 YMP 框架的内容，请访问官网：[https://ymate.net](https://ymate.net)
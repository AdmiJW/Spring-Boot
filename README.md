# Spring Boot Notes

![Spring Boot logo](./README_res/springboot.svg)

Author: AdmiJW

> This is a quick reference for myself in the future (and anyone that wants to use this) for Spring Boot technology.

> The information from the note is mostly extracted from [Navin Reddy's Video](https://www.youtube.com/watch?v=35EQXmHKZYs)

> At the time of writing, the LTS version of Spring Boot is **2.7.2**, from [Spring Boot Initializr](https://start.spring.io/)

---
<br>

## 1. Introduction 🖐

* **Spring** is a Java framework, most commonly known for its backend language capabilities. **Spring Boot** builds on top of Spring and makes many configuration tasks much more easier. Like some other backend technologies, it boasts a rich set of features that can be used to build complex web applications quickly, such as **Dependency Injection** and **Auto Configuration**.

* We can very quickly add dependencies (packages) into our Spring Boot project by including the **dependencies** section retrieved from [mvnrepository](https://mvnrepository.com/) _(Assuming that you are using Maven)_ in the **pom.xml** file.

* With *auto-configuration*, you can configure many features of your dependencies just by adding related key-value pairs in the **application.properties** file.

* **Spring Boot** features **Dependency Injection**, using the **@Autowired** annotation, as you will see later in the section.

* A quick way to start your Spring Boot application is to download the package from [Spring Boot Initializr](https://start.spring.io/).

---
<br>

## 2. Dependency Injection 💉

* **Dependency Injection** is a design pattern that allows you to inject dependencies *(loose coupling)* into your objects instead of having to instantiate them directly in the class *(tight coupling)*.

* Conceptually, when we run our Spring Boot application, a Spring container will be created that contain the objects (called **Beans**) that we want to use in dependency injection later.

* Dependency injection can be mainly done in 2 types: **Singleton** and **Prototype**. Singleton simply means that the object will be created on application start, while Prototype means that the object will be created on every request made (lazily initialized).

* To introduce **Beans** in our Spring Boot application, simply add the **@Component** annotation to the class. If you want it to be a Prototype bean, add the **@Scope("prototype")** annotation as well.

    ```java
    @Component
    public class MyBean {
        // ...
        public MyBean() {
            // This ctor will be run once, on application start. Because it is a Singleton bean.
        }
    }
    ```
  
* Inside the main java file with the `main()` method, `SpringApplication.run(...);` actually returns a `ConfigurableApplicationContext` object, which is a container that contains the **Beans**. We can use `getBean(<class>)` to retrieve the bean we want.

    ```java
    public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(PelaburanApplication.class, args);
		// This will retrieve the bean with the class of MyBean
        ctx.getBean(MyBean.class);
	}
    ```

* Inside object classes, we can use the **@Autowired** annotation to automatically inject the dependencies.

    ```java
    public class ConsumeBean {
        // myBean will be populated automatically
        @Autowired
        private MyBean myBean;
        //...
    }
    ```

---
<br>

## 3. Spring Boot Web Application Basics 👩‍💻

* During project initialization, ensure the **Spring Web** dependency is included. If not, include into the `pom.xml` file.

* Spring Boot comes with Tomcat embedded as the web server, which makes deployment easier and more portable.

* Controller classes shall be annotated with the **@Controller** annotation. If the controller will be working as a RESTful endpoint, then use **@RestController** so that every method returns a JSON object (**@ResponseBody** default).

* Use the **@RequestMapping** annotation to map the URL to the method. You could also use the **@GetMapping** and **@PostMapping** annotations.

    ```java
    @Controller
    public class MyController {
        // At root url, you should see "Hello World" string being returned
        @RequestMapping("/")
        public String index() {
            return "Hello World";
        }
    }
    ```

* Use the **@ResponseBody** annotation to return a JSON object. **@RequestBody** annotation is used to automatically deserialize a JSON object from the request body and populate the parameter. You will see this later.

* For the view, we can use `Jakarta Server Pages (JSP)`. Start by including `Tomcat Jasper` dependency. Include JSP pages in the `webapp` folder of your `main` directory. You should be able to display JSP pages by returning the page name.

    ```java
    // Assuming you have index.jsp in the webapp folder. index.jsp will be rendered

    @Controller
    public class MyController {
        @RequestMapping("/")
        public String index() {
            return "index";
        }
    }
    ```

---
<br>

## 4. Passing Data to Spring Boot Pages 💻

* Servlet way (Old)

    * In the controller method, you can have access to `HttpServletRequest` and `HttpServletResponse` objects. Simply define them in parameter and they will be populated by dependency injection. Use the `getParameter(<name>)` method to retrieve the parameter value.

        ```java
        @RequestMapping("/")
        public String index(HttpServletRequest request, HttpServletResponse response) {
            String name = request.getParameter("name");
            return "Hi " + name;
        }
        ```

    * To pass data into JSP pages, set it into session. Use `request.getSession().setAttribute(<name>, <value>);` to set the session attribute.
    To access the data from JSP, use `<%= request.getSession().getAttribute(<name>) %>` or simply `${name}` to retrieve the session attribute.

* Spring Boot's auto configuration 
    * Use the `@RequestParam` annotation to retrieve the parameter value. Like:

        ```java
        @RequestMapping("/")
        public String index(@RequestParam("name") String name) {
            return "Hi " + name;
        }
        ```

    * To pass data into JSP, we can use `ModelAndView` object. Use the `addObject(<name>, <value>);` method to add the object to the model.

    ```java
    @RequestMapping("/")
    public ModelAndView index(@RequestParam String name) {
        ModelAndView mv = new ModelAndView("index");
        mv.addObject("name", name);
        return mv;
    }
    ```

    * Finally, if a parameter is not required / optional, use the `@RequestParam(name = <name>, required = false)` annotation.

---
<br>

## 5. Java Persistence API (JPA) 🗂

* Ensure required dependencies are included. Include **Spring Data JPA**, **Spring Web** (if required), and corresponding datastore dependency, like **H2** for in-memory database.

* Thanks to auto-configuration, we can configure the dependencies just from `application.properties` file:

    Asssuming you are using **h2**:
    ```
    spring.h2.console.enabled=true
    spring.datasource.platform=org.h2
    spring.datasource.url=jdbc:h2:mem:[name]
    ```

    Or perhaps you are using **MySQL**:
    ```
    spring.datasource.username=[username]
    spring.datasource.password=[password]
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
    ```

* JPA automatically detects the domain classes that are annotated with the `**@Entity**` annotation.

    ```java
    @Entity
    public class MyEntity {
        // ...
    }
    ```

* Include `spring.jpa.hibernate.ddl-auto` in the `application.properties` file to set how database are updated on each startup.

* Inside entities, use the `**@Id**` annotation to set the primary key. Use the `**@GeneratedValue(strategy=GenerationType.?)**` annotation to set the strategy for generating the primary key.

    ```java
    @Entity
    public class MyEntity {
        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private Long id;
        // ...
    }
    ```
    ```

* Create **Repository** (Model) **interfaces** *(Not classes!)* to interact with the database. The repository classes can be done by extending the **`CrudRepository<DomainClass, PrimaryKeyType>`** interface *(or `JpaRepository<DomainClass, PrimaryKeyType>` for complex functionalities)*. 

    ```java
    public interface MyRepository extends CrudRepository<MyEntity, Long> {
        // You don't have to implement any methods. Many are provided from parent interface. You can extend the parent interface to add more functionality by adding default methods
    }
    ```

* Inside the controller, use the **@Autowired** annotation to inject the repository.

    ```java
    @Controller
    public class MyController {
        // all methods can access the repository
        @Autowired
        private MyRepository myRepository;
        // ...
    }
    ```

* JPA is smart enough to provide implementation of certain repository methods that match a certain pattern, such as: `findBy<field>`, `findAllBy<field>`. [**Reference HERE**](https://docs.spring.io/spring-data/jpa/docs/1.5.0.RELEASE/reference/html/jpa.repositories.html). For even complex queries, write JPQL queries inside `@Query(<query>)` annotation.

    ```java
    public interface MyRepository extends CrudRepository<MyEntity, Long> {
        List<User> findByEmailAddressAndLastname(String emailAddress, String lastname);
    }
    ```

---
<br>

## 6. RESTful with Spring 📪

* We put **`@ResponseBody`** annotation on the controller method to indicate that the return value will be serialized into a JSON object.

    ```java
    @RequestMapping("/")
    @ResponseBody
    public Person index() {
        return new Person("Alex", 12);
    }
    ```

* The path mapping can accept path parameters: such as `@RequestMapping("/person/{id}")`. Retrieve the path parameter value with the `@PathVariable` annotation.

    ```java
    @RequestMapping("/person/{id}")
    @ResponseBody
    public Person index(@PathVariable("id") Long id) {
        //...
    }
    ```

* `Jackson Core` is the main JSON serializer and deserializer. It is included by default in Spring Boot. If you want to work with XML, check out the `jackson-dataformat-xml` dependency.

* Use **`@RestController`** annotation to indicate that the controller is a RESTful controller. All the methods will have the `@ResponseBody` annotation this way.

    ```java
    @RestController
    public class MyController {
        // ...
    }
    ```

* Use **`@RequestBody`** annotation to retrieve the JSON object from the request body.

    ```java
    // If the request sends a JSON that matches Person object
    @RequestMapping("/")
    @ResponseBody
    public Person index(@RequestBody Person person) {
        //...
    }
    ```


1. RESTful with Spring
   * We put @ResponseBody annotation on controller method to indicate any object returned will be serialzied into JSON
   * The mapping can accept path parameters: in mapping annotation, use @RequestMapping("path/{var}").
   * Get the path parameter by annotation in parameter list: @PathVariable("id")
   * `Jackson Core` is a dependency that is used to serialize and deserialize JSON automatically. Comes included.
   * To allow XML, you have to include `Jackson Dataform XML` form mvnrepository.com
   * To return Json data, controller methods shall return `Optional<Obj>` or `List<Obj>`.
   * Use `@RestController` to automatically set all methods to `@ResponseBody` by default.
   * Use `@RequestBody` in controller method to deserialize JSON into object.
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

* There are 2 main ways to pass data to controllers: **Query Parameters** _(Usually in GET)_ and **Request Body** _(Usually in POST)_. We talk about getting data from query parameters first:

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

* For getting data from Request Body (POST request) **JSON**, you may use the **`@RequestBody`** annotation. What's awesome is that it provides auto-mapping to your **POJO** (Plain Old Java Objects)

    ```java
    @RequestMapping("/")
    public String index(@RequestBody Person person) {
        return "Hi " + person.name;
    }
    ```

* If you just want to retrieve the key value pairs from the request body's JSON, you can use `Map<>` for this purpose

    ```java
    @RequestMapping("/")
    public String index(@RequestBody Map<String, String> params) {
        return "Hi " + params.get("name");
    }
    ```

---
<br>

## 5. Java Persistence API (JPA) 🗂

> The Java Persistence API (JPA) is the persistence standard of the Java ecosystem. It allows us to map our domain model directly to the database structure and then gives us the flexibility of manipulating objects in our code - instead of messing with cumbersome JDBC components like Connection, ResultSet, etc. JPA is an API that aims at standardizing the way we access a relational database from Java software using Object Relational Mapping (ORM).


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

* Include `spring.jpa.hibernate.ddl-auto` in the `application.properties` file to set how database are updated on each startup. Like `create-drop` will drop all tables and create them anew every time the application is started.

* Inside entities, use the **`@Id`** annotation to set the primary key. Use the **`@GeneratedValue(strategy=GenerationType.TYPE)`** annotation to set the strategy for generating the primary key.

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

* JPA is smart enough to provide implementation of certain repository methods that match a certain pattern, such as: `findBy<field>`, `findAllBy<field>`. [**Reference HERE**](https://docs.spring.io/spring-data/jpa/docs/1.5.0.RELEASE/reference/html/jpa.repositories.html)

    ```java
    public interface MyRepository extends CrudRepository<MyEntity, Long> {
        List<User> findByEmailAddressAndLastname(String emailAddress, String lastname);
    }
    ```

* For more complex queries, write JPQL queries inside `@Query(<query>)` annotation, or regular SQL if you set `nativeQuery=true`.

    ```java
    public interface MyRepository extends CrudRepository<MyEntity, Long> {
        @Query("SELECT u FROM MyEntity u WHERE u.emailAddress = :emailAddress AND u.lastname = :lastname", nativeQuery=true)
        List<User> findByEmailAddressAndLastname(@Param("emailAddress") String emailAddress, @Param("lastname") String lastname);
    }
    ```

---
<br>

## 6. More on JPA 📊


[**!! GREAT REFERENCE HERE**](https://stackabuse.com/guide-to-jpa-with-hibernate-basic-mapping/)


> JPA is simply a API, and thus doesn't provide any implementation but solely defines and standardizes the concepts of ORM in Java. For the implementations, we usually get them from the vendors, like the more well known **Hibernate**.

* Use the **`@Entity`** annotation to mark the class as an entity. JPA will detect the entity and create a table for it.

* Use the **`@Id`** annotation to mark the field as the primary key. Use `@GeneratedValue` to auto generate the values, such as for the ID primary key. There are multiple value generation strategies, which you can specify by setting the strategy flag:

    ```java
    @GeneratedValue(strategy = GenerationType.TYPE)
    ```

* For dates, legacy systems may use classes from the `java.util`, such as `Date`, `Timestamp` etc. These datatypes has to be annotated with the **`@Temporal`** annotation.

    ```java
    public class Student {
        @Temporal(TemporalType.DATE)
        private Date birthDate;
    }
    ```

* In newer JPA, we recommend use classes from the `java.time` package, such as `LocalDate`, `LocalTime` etc. These are automatically handled by JPA and thus don't need to be annotated (better).

* Enumerations are handled by JPA by using the **`@Enumerated`** annotation. We can either use the **`ORDINAL`** strategy or the **`STRING`** strategy.

    ```java
    public class Student {
        @Enumerated(EnumType.STRING)        // Default is ORDINAL
        private Gender gender;
    }
    ```

* The **`@JoinColumn`** annotation is used to specify that a foreign key column has to be used to map a relationship. Commonly used flags include `name` and `referencedColumnName`.

    ```java
    @JoinColumn(name = "TEACHER_ID", referencedColumnName = "ID")
    // The foreign key column name is teacher_id and it references the id column (primary key) of the referenced entity 
    ```

* It is a better practice to put the **`@JoinColumn`** annotation on the entity that should have the foreign key column for standard (Even if JPA is smart enough to put the column at the correct table). We call the entity that has the foreign key column the **owning side**, and the other entity, the **referenced side**. 

* Eg: We have Teacher and Course. One Teacher teaches many Course. We can simply define the referencing field in `Course.java` and not add anything to `Teacher.java`:

    ```java
    // Course.java
    @ManyToOne
    @JoinColumn(name = "TEACHER_ID", referencedColumnName = "ID")
    private Teacher teacher;
    ```

* Of course, we have to annotate the type of relationship that we are having: `@OneToMany`, `@ManyToOne`, `@ManyToMany`, `@OneToOne` etc.

* What if we want to access `Course` from `Teacher` and also vice-versa? We have to define a **bidirectional relationship**:

    ```java
    @Entity
    public class Teacher {
        // ...

        @OneToMany(mappedBy = "teacher")
        private List<Course> courses;
    }

    @Entity
    public class Course {
        // ...
        
        @ManyToOne
        @JoinColumn(name = "TEACHER_ID", referencedColumnName = "ID")
        private Teacher teacher;
    }
    ```

> **_Important: the `mappedBy` flag tells JPA that the field is already being mapped by another field from another entity. Without this, you will have foreign key in both tables!**

* A common bug in Bidirectional relationship is **Infinite Recursion**. When trying to serialize an entity into JSON, entity A might contain reference to entity B, which also contain reference to entity A. Serializing this will lead to infinite recursion. To solve this, there are [**Solutions**](https://www.baeldung.com/jackson-bidirectional-relationships-and-infinite-recursion) such as using **`@JsonManagedReference`** and **`@JsonBackReference`** so that the one with the `@JsonBackReference` will not be included in JSON

    ```java
    public class User {
        public int id;
        public String name;

        @JsonManagedReference
        public List<Item> userItems;
    }

    public class Item {
        public int id;
        public String itemName;

        @JsonBackReference
        public User owner;
    }
    ```

* By default, One to Many relationships are lazily loaded, while Many to One relationships are eagerly loaded. We can change this by using the **`fetch`** flag.

    ```java
    @OneToMany(mappedBy = "teacher", fetch = FetchType.EAGER)
    private List<Course> courses;

    @ManyToOne(fetch = FetchType.LAZY)
    private Teacher teacher;
    ```

* Relationships are optional `(NULLABLE)` by default. To change this, set the **`optional`** flag to `false`.

    ```java
    @ManyToOne(optional = false)
    @JoinColumn(name = "TEACHER_ID", referencedColumnName = "ID")
    private Teacher teacher;
    ```

* When saving entities, the objects has to be **persisted** in the correct order. We can do this with the help of **`cascade`** flag.

    ```java
    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "TEACHER_ID", referencedColumnName = "ID")
    private Teacher teacher;
    ```

* There are multiple types of cascading operations: `PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`, and `ALL` (that combines all the previous ones). Cascading will ensure that related entities are persisted before persisting the said entity.

* Many to many relationships are a bit more complex. Use the `@ManyToMany` annotation to mark the relationship. Instead of `@JoinColumn`, use the `@JoinTable` annotation (Since many to many relationships require a table to define the relationship):

    ```java
    // Course.java
    @ManyToMany
    @JoinTable(
    name = "STUDENTS_COURSES",
    // Owning side
    joinColumns = @JoinColumn(name = "COURSE_ID", referencedColumnName = "ID"), 
    // Referencing side
    inverseJoinColumns = @JoinColumn(name = "STUDENT_ID", referencedColumnName = "ID")
    )
    private List<Student> students;
    ```

---
<br>

## 7. RESTful with Spring 📪

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
